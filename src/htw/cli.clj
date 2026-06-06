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

(defn- invalid-command [state action]
  (assoc state :error (str (str/upper-case (or action "")) " IS NOT A COMMAND")))

(defn- scripted-move [state args]
  (if (= 1 (count args))
    (game/try-move-player state (parse-int (first args) "move room"))
    (assoc state :error game/invalid-move-message)))

(defn- scripted-shot [state args]
  (if (<= 1 (count args) 5)
    (game/try-shoot-arrow state (mapv #(parse-int % "shot room") args))
    (assoc state :error game/invalid-shot-message)))

(defn- run-command [state command]
  (let [[action & args] (command-tokens command)]
    (case (some-> action str/lower-case)
      "m" (scripted-move state args)
      "s" (scripted-shot state args)
      (invalid-command state action))))

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

(defn- next-loop-state [state]
  (if (= :lost (:status state))
    (continue-after-terminal state)
    state))

(defn- continue-loop? [state]
  (= :in-progress (:status state :in-progress)))

(defn- enter-shell-command [state command]
  (let [{:keys [state output]} (ui/enter-command state command)]
    (print-lines! output)
    (next-loop-state state)))

(defn- read-next-state [state]
  (when (continue-loop? state)
    (when-let [command (read-line)]
      (enter-shell-command state command))))

(defn- run-game-loop! [state]
  (loop [state state]
    (when-let [state (and state (read-next-state state))]
      (recur state))))

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

(defn inspect [& args]
  (apply inspect-main args))

;; clj-mutate-manifest-begin
;; {:version 1, :tested-at "2026-06-06T11:55:21.254401-05:00", :module-hash "2143156959", :forms [{:id "form/0/ns", :kind "ns", :line 1, :end-line 5, :hash "1095944540"} {:id "def/instructions-prompt", :kind "def", :line 7, :end-line 7, :hash "282711655"} {:id "form/2/defonce", :kind "defonce", :line 9, :end-line 9, :hash "650038718"} {:id "defn/exit!", :kind "defn", :line 11, :end-line 12, :hash "552446272"} {:id "defn/random-seed", :kind "defn", :line 14, :end-line 15, :hash "363677862"} {:id "defn/launch-game", :kind "defn", :line 17, :end-line 21, :hash "2085306560"} {:id "defn/answer-instructions", :kind "defn", :line 23, :end-line 25, :hash "-305170808"} {:id "defn-/parse-int", :kind "defn-", :line 27, :end-line 31, :hash "1836147578"} {:id "defn-/parse-room-list", :kind "defn-", :line 33, :end-line 37, :hash "-97128823"} {:id "defn-/parse-wake-choice", :kind "defn-", :line 39, :end-line 42, :hash "-1858852301"} {:id "def/option-parsers", :kind "def", :line 44, :end-line 57, :hash "-1081062010"} {:id "defn-/parse-option", :kind "defn-", :line 59, :end-line 63, :hash "-2129025786"} {:id "defn-/parse-args", :kind "defn-", :line 65, :end-line 68, :hash "-1374538191"} {:id "defn-/explicit-setup?", :kind "defn-", :line 70, :end-line 71, :hash "1583514206"} {:id "defn-/setup-from", :kind "defn-", :line 73, :end-line 85, :hash "111081715"} {:id "def/state-options", :kind "def", :line 87, :end-line 88, :hash "-369228174"} {:id "defn-/configured-state", :kind "defn-", :line 90, :end-line 91, :hash "-1662635440"} {:id "defn-/room-line", :kind "defn-", :line 93, :end-line 94, :hash "-1725658454"} {:id "defn-/print-topology!", :kind "defn-", :line 96, :end-line 99, :hash "1792414841"} {:id "defn-/sorted-list", :kind "defn-", :line 101, :end-line 102, :hash "-969728569"} {:id "defn-/print-setup!", :kind "defn-", :line 104, :end-line 109, :hash "389176361"} {:id "defn-/print-adjacent-hazards!", :kind "defn-", :line 111, :end-line 116, :hash "1140572204"} {:id "defn-/print-turn!", :kind "defn-", :line 118, :end-line 124, :hash "479348580"} {:id "defn-/print-result!", :kind "defn-", :line 126, :end-line 136, :hash "-402500160"} {:id "defn-/command-tokens", :kind "defn-", :line 138, :end-line 139, :hash "2031381878"} {:id "defn-/invalid-command", :kind "defn-", :line 141, :end-line 142, :hash "-961833724"} {:id "defn-/scripted-move", :kind "defn-", :line 144, :end-line 147, :hash "-972026345"} {:id "defn-/scripted-shot", :kind "defn-", :line 149, :end-line 152, :hash "-559936024"} {:id "defn-/run-command", :kind "defn-", :line 154, :end-line 159, :hash "-23417405"} {:id "defn-/scripted-commands", :kind "defn-", :line 161, :end-line 163, :hash "1340423622"} {:id "defn-/run-script!", :kind "defn-", :line 165, :end-line 175, :hash "-315896525"} {:id "defn-/explicit-launch-options?", :kind "defn-", :line 177, :end-line 178, :hash "1868215521"} {:id "defn-/with-observed-seed", :kind "defn-", :line 180, :end-line 183, :hash "-1910644406"} {:id "defn-/initial-launch", :kind "defn-", :line 185, :end-line 195, :hash "1888578743"} {:id "defn-/print-lines!", :kind "defn-", :line 197, :end-line 200, :hash "211376327"} {:id "defn-/continue-after-terminal", :kind "defn-", :line 202, :end-line 208, :hash "2131642771"} {:id "defn-/next-loop-state", :kind "defn-", :line 210, :end-line 213, :hash "-1591562258"} {:id "defn-/continue-loop?", :kind "defn-", :line 215, :end-line 216, :hash "-713442341"} {:id "defn-/enter-shell-command", :kind "defn-", :line 218, :end-line 221, :hash "-654402039"} {:id "defn-/read-next-state", :kind "defn-", :line 223, :end-line 226, :hash "-805143145"} {:id "defn-/run-game-loop!", :kind "defn-", :line 228, :end-line 231, :hash "-1259505018"} {:id "defn/-main", :kind "defn", :line 233, :end-line 239, :hash "-1679600654"} {:id "defn/inspect-main", :kind "defn", :line 241, :end-line 256, :hash "-583779460"} {:id "defn/inspect", :kind "defn", :line 258, :end-line 259, :hash "749474069"}]}
;; clj-mutate-manifest-end
