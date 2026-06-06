(ns htw.cli
  (:require [clojure.string :as str]
            [htw.cave :as cave]
            [htw.game :as game]
            [htw.ui :as ui]))

(def instructions-prompt "INSTRUCTIONS (Y-N)?")

(defonce seed-counter (atom 0))

(defn exit! [status]
  (System/exit status))

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

(declare configured-state parse-args)

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

(defn- parse-option [[option value]]
  (let [[parsed-key parser] (get option-parsers option)]
    (when-not parser
      (throw (ex-info (str "Unknown option: " option) {})))
    [parsed-key (parser value)]))

(defn- parse-args [args]
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
      (exit! 1))))

;; clj-mutate-manifest-begin
;; {:version 1, :tested-at "2026-06-06T11:40:55.627134-05:00", :module-hash "1267674950", :forms [{:id "form/0/ns", :kind "ns", :line 1, :end-line 5, :hash "1095944540"} {:id "def/instructions-prompt", :kind "def", :line 7, :end-line 7, :hash "282711655"} {:id "form/2/defonce", :kind "defonce", :line 9, :end-line 9, :hash "650038718"} {:id "defn/exit!", :kind "defn", :line 11, :end-line 12, :hash "552446272"} {:id "defn/random-seed", :kind "defn", :line 14, :end-line 15, :hash "363677862"} {:id "defn/launch-game", :kind "defn", :line 17, :end-line 21, :hash "2085306560"} {:id "defn/answer-instructions", :kind "defn", :line 23, :end-line 25, :hash "-305170808"} {:id "form/7/declare", :kind "declare", :line 27, :end-line 27, :hash "1014617203"} {:id "defn-/parse-int", :kind "defn-", :line 29, :end-line 33, :hash "1836147578"} {:id "defn-/parse-room-list", :kind "defn-", :line 35, :end-line 39, :hash "-97128823"} {:id "defn-/parse-wake-choice", :kind "defn-", :line 41, :end-line 44, :hash "-1858852301"} {:id "def/option-parsers", :kind "def", :line 46, :end-line 59, :hash "-1081062010"} {:id "defn-/parse-option", :kind "defn-", :line 61, :end-line 65, :hash "-2129025786"} {:id "defn-/parse-args", :kind "defn-", :line 67, :end-line 70, :hash "-1374538191"} {:id "defn-/explicit-setup?", :kind "defn-", :line 72, :end-line 73, :hash "1583514206"} {:id "defn-/setup-from", :kind "defn-", :line 75, :end-line 87, :hash "111081715"} {:id "def/state-options", :kind "def", :line 89, :end-line 90, :hash "-369228174"} {:id "defn-/configured-state", :kind "defn-", :line 92, :end-line 93, :hash "-1662635440"} {:id "defn-/room-line", :kind "defn-", :line 95, :end-line 96, :hash "-1725658454"} {:id "defn-/print-topology!", :kind "defn-", :line 98, :end-line 101, :hash "1792414841"} {:id "defn-/sorted-list", :kind "defn-", :line 103, :end-line 104, :hash "-969728569"} {:id "defn-/print-setup!", :kind "defn-", :line 106, :end-line 111, :hash "389176361"} {:id "defn-/print-adjacent-hazards!", :kind "defn-", :line 113, :end-line 118, :hash "1140572204"} {:id "defn-/print-turn!", :kind "defn-", :line 120, :end-line 126, :hash "479348580"} {:id "defn-/print-result!", :kind "defn-", :line 128, :end-line 138, :hash "-402500160"} {:id "defn-/command-tokens", :kind "defn-", :line 140, :end-line 141, :hash "2031381878"} {:id "defn-/run-command", :kind "defn-", :line 143, :end-line 152, :hash "-1564383719"} {:id "defn-/scripted-commands", :kind "defn-", :line 154, :end-line 156, :hash "1340423622"} {:id "defn-/run-script!", :kind "defn-", :line 158, :end-line 168, :hash "-315896525"} {:id "defn-/explicit-launch-options?", :kind "defn-", :line 170, :end-line 171, :hash "1868215521"} {:id "defn-/with-observed-seed", :kind "defn-", :line 173, :end-line 176, :hash "-1910644406"} {:id "defn-/initial-launch", :kind "defn-", :line 178, :end-line 188, :hash "1888578743"} {:id "defn-/print-lines!", :kind "defn-", :line 190, :end-line 193, :hash "211376327"} {:id "defn-/continue-after-terminal", :kind "defn-", :line 195, :end-line 201, :hash "2131642771"} {:id "defn-/run-game-loop!", :kind "defn-", :line 203, :end-line 212, :hash "843688723"} {:id "defn/-main", :kind "defn", :line 214, :end-line 220, :hash "-1679600654"} {:id "defn/inspect-main", :kind "defn", :line 222, :end-line 237, :hash "-583779460"}]}
;; clj-mutate-manifest-end
