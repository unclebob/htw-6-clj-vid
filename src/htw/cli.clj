(ns htw.cli
  (:require [clojure.string :as str]
            [htw.cave :as cave]
            [htw.game :as game]
            [htw.ui :as ui]))

(def instructions-prompt "INSTRUCTIONS (Y-N)?")

(defonce seed-counter (atom 0))

(defn random-seed []
  (+ (System/nanoTime) (swap! seed-counter inc)))

(defn launch-game []
  (let [seed (random-seed)]
    {:seed seed
     :state (game/start-game seed)
     :output [instructions-prompt]}))

(defn answer-instructions [{:keys [state] :as launch} answer]
  (let [{:keys [state output]} (ui/answer-instructions state answer)]
    (assoc launch :state state :output output)))

(defn- parse-int [value label]
  (let [trimmed (str/trim value)]
    (when-not (re-matches #"\d+" trimmed)
      (throw (ex-info (str "Invalid " label ": " value) {})))
    (Long/parseLong trimmed)))

(defn- parse-room-list [value label]
  (let [rooms (mapv #(parse-int % label) (str/split value #","))]
    (when-not (= 2 (count rooms))
      (throw (ex-info (str label " must contain exactly two rooms") {})))
    (set rooms)))

(defn- parse-wake-choice [value]
  (if (= "stay" (str/lower-case (str/trim value)))
    :stay
    (parse-int value "Wumpus wake choice")))

(def option-parsers
  {"--seed" [:seed #(parse-int % "seed")]
   "--same-setup" [:same-setup (constantly true)]
   "--player" [:player-room #(parse-int % "player room")]
   "--wumpus" [:wumpus-room #(parse-int % "Wumpus room")]
   "--pits" [:pit-rooms #(parse-room-list % "pits")]
   "--bats" [:bat-rooms #(parse-room-list % "bats")]
   "--adjacent" [:adjacent-room #(parse-int % "adjacent room")]
   "--bat-transport" [:bat-transport-room #(parse-int % "bat transport room")]
   "--wumpus-wake" [:wumpus-wake-choice parse-wake-choice]
   "--arrow-deviation" [:arrow-deviation-room #(parse-int % "arrow deviation room")]
   "--arrows" [:arrows #(parse-int % "arrows")]
   "--show-seed" [:show-seed (constantly true)]
   "--commands" [:commands identity]})

(defn parse-option [[option value]]
  (let [[parsed-key parser] (get option-parsers option)]
    (when-not parser
      (throw (ex-info (str "Unknown option: " option) {})))
    [parsed-key (parser value)]))

(defn parse-args [args]
  (when (odd? (count args))
    (throw (ex-info "Options must be supplied as --name value pairs" {})))
  (into {} (map parse-option) (partition 2 args)))

(defn- explicit-setup? [options]
  (every? options [:player-room :wumpus-room :pit-rooms :bat-rooms]))

(defn- setup-from [options]
  (cond
    (explicit-setup? options)
    (game/configured-game (:player-room options)
                          (:wumpus-room options)
                          (:pit-rooms options)
                          (:bat-rooms options))

    (:seed options)
    (game/start-game (:seed options))

    :else
    (game/start-game 1973)))

(def state-options
  [:arrows :bat-transport-room :wumpus-wake-choice :arrow-deviation-room])

(defn- configured-state [options]
  (merge (setup-from options) (select-keys options state-options)))

(defn- room-line [room]
  (str room ": " (str/join ", " (cave/exits room))))

(defn- print-topology! []
  (println "CAVE EXITS")
  (doseq [room cave/rooms]
    (println (room-line room))))

(defn- sorted-list [rooms]
  (str/join ", " (sort rooms)))

(defn- print-setup! [heading state]
  (println heading)
  (println "PLAYER:" (:player-room state))
  (println "WUMPUS:" (:wumpus-room state))
  (println "PITS:" (sorted-list (:pit-rooms state)))
  (println "BATS:" (sorted-list (:bat-rooms state))))

(defn- print-adjacent-hazards! [state room]
  (let [{:keys [wumpus pit bat]} (game/adjacent-hazards state room)]
    (println "ADJACENT HAZARDS FOR ROOM:" room)
    (println "WUMPUS:" wumpus)
    (println "PITS:" pit)
    (println "BATS:" bat)))

(defn- print-turn! [state]
  (println "YOU ARE IN ROOM:" (:player-room state))
  (println "TUNNELS:" (sorted-list (cave/exits (:player-room state))))
  (doseq [warning (game/turn-warnings state)]
    (println warning))
  (println "ARROWS:" (:arrows state 5))
  (println "STATUS:" (-> (:status state :in-progress) name str/upper-case)))

(defn- print-result! [state]
  (doseq [message (:messages state)]
    (println message))
  (when-let [error (:error state)]
    (println error))
  (when-let [visits (:arrow-visits state)]
    (println "ARROW PATH:" (str/join ", " visits)))
  (println "PLAYER:" (:player-room state))
  (println "WUMPUS:" (:wumpus-room state))
  (println "ARROWS:" (:arrows state 5))
  (println "STATUS:" (-> (:status state :in-progress) name str/upper-case)))

(defn- command-tokens [command]
  (remove str/blank? (str/split (str/trim command) #"\s+")))

(defn- run-command [state command]
  (let [[action & args] (command-tokens command)]
    (case (some-> action str/lower-case)
      "m" (if (= 1 (count args))
            (game/try-move-player state (parse-int (first args) "move room"))
            (assoc state :error game/invalid-move-message))
      "s" (if (<= 1 (count args) 5)
            (game/try-shoot-arrow state (mapv #(parse-int % "shot room") args))
            (assoc state :error game/invalid-shot-message))
      (assoc state :error (str (str/upper-case (or action "")) " IS NOT A COMMAND")))))

(defn- scripted-commands [options]
  (when-let [commands (:commands options)]
    (remove str/blank? (str/split commands #";"))))

(defn- run-script! [state commands]
  (loop [state state
         remaining commands]
    (print-turn! state)
    (when-let [command (first remaining)]
      (println "COMMAND:" command)
      (let [next-state (run-command (assoc state :messages [] :error nil) command)]
        (print-result! next-state)
        (when (and (= :in-progress (:status next-state :in-progress))
                   (seq (rest remaining)))
          (recur next-state (rest remaining)))))))

(defn- explicit-launch-options? [options]
  (or (:seed options) (explicit-setup? options)))

(defn- with-observed-seed [launch show-seed?]
  (if show-seed?
    (update launch :output conj (str "SEED: " (:seed launch)))
    launch))

(defn- initial-launch [args]
  (let [options (parse-args args)]
    (cond
      (explicit-launch-options? options)
      (with-observed-seed {:seed (:seed options)
                           :state (configured-state options)
                           :output [instructions-prompt]}
                          (:show-seed options))

      :else
      (with-observed-seed (launch-game) (:show-seed options)))))

(defn- print-lines! [lines]
  (doseq [line lines]
    (println line))
  (flush))

(defn- continue-after-terminal [state]
  (when (= :lost (:status state))
    (when-let [answer (read-line)]
      (let [next-state (ui/replay state answer)
            turn (ui/display-turn next-state)]
        (print-lines! (:output turn))
        (:state turn)))))

(defn- run-game-loop! [state]
  (loop [state state]
    (when (and state (= :in-progress (:status state :in-progress)))
      (when-let [command (read-line)]
        (let [{:keys [state output]} (ui/enter-command state command)]
          (print-lines! output)
          (if (= :lost (:status state))
            (recur (continue-after-terminal state))
            (when (= :in-progress (:status state :in-progress))
              (recur state))))))))

(defn -main [& args]
  (let [launch (initial-launch args)]
    (print-lines! (:output launch))
    (when-let [answer (read-line)]
      (let [{:keys [state output]} (answer-instructions launch answer)]
        (print-lines! output)
        (run-game-loop! state)))))

(defn inspect-main [& args]
  (try
    (let [options (parse-args args)
          state (configured-state options)]
      (print-topology!)
      (print-setup! "SETUP" state)
      (when (:same-setup options)
        (print-setup! "REUSED SETUP" (game/reuse-setup state)))
      (when-let [room (:adjacent-room options)]
        (print-adjacent-hazards! state room))
      (when-let [commands (scripted-commands options)]
        (run-script! state commands)))
    (catch Exception e
      (binding [*out* *err*]
        (println (.getMessage e)))
      (System/exit 1))))
