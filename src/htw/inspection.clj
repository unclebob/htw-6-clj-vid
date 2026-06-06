(ns htw.inspection
  (:require [clojure.string :as str]
            [htw.cave :as cave]
            [htw.cli-options :as options]
            [htw.game :as game]))

(defn exit! [status]
  (System/exit status))

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
    (game/try-move-player state (options/parse-int (first args) "move room"))
    (assoc state :error game/invalid-move-message)))

(defn- scripted-shot [state args]
  (if (<= 1 (count args) 5)
    (game/try-shoot-arrow state (mapv #(options/parse-int % "shot room") args))
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

(defn inspect-main [& args]
  (try
    (let [parsed-options (options/parse-args args)
          state (options/configured-state parsed-options)]
      (print-topology!)
      (print-setup! "SETUP" state)
      (when (:same-setup parsed-options)
        (print-setup! "REUSED SETUP" (game/reuse-setup state)))
      (when-let [room (:adjacent-room parsed-options)]
        (print-adjacent-hazards! state room))
      (when-let [commands (scripted-commands parsed-options)]
        (run-script! state commands)))
    (catch Exception e
      (binding [*out* *err*]
        (println (.getMessage e)))
      (exit! 1))))

(defn inspect [& args]
  (apply inspect-main args))

;; clj-mutate-manifest-begin
;; {:version 1, :tested-at "2026-06-06T11:55:04.248569-05:00", :module-hash "-1046954339", :forms [{:id "form/0/ns", :kind "ns", :line 1, :end-line 5, :hash "681317258"} {:id "defn/exit!", :kind "defn", :line 7, :end-line 8, :hash "552446272"} {:id "defn-/room-line", :kind "defn-", :line 10, :end-line 11, :hash "-1725658454"} {:id "defn-/print-topology!", :kind "defn-", :line 13, :end-line 16, :hash "1792414841"} {:id "defn-/sorted-list", :kind "defn-", :line 18, :end-line 19, :hash "-969728569"} {:id "defn-/print-setup!", :kind "defn-", :line 21, :end-line 26, :hash "389176361"} {:id "defn-/print-adjacent-hazards!", :kind "defn-", :line 28, :end-line 33, :hash "1140572204"} {:id "defn-/print-turn!", :kind "defn-", :line 35, :end-line 41, :hash "479348580"} {:id "defn-/print-result!", :kind "defn-", :line 43, :end-line 53, :hash "-402500160"} {:id "defn-/command-tokens", :kind "defn-", :line 55, :end-line 56, :hash "2031381878"} {:id "defn-/invalid-command", :kind "defn-", :line 58, :end-line 59, :hash "-961833724"} {:id "defn-/scripted-move", :kind "defn-", :line 61, :end-line 64, :hash "1807590660"} {:id "defn-/scripted-shot", :kind "defn-", :line 66, :end-line 69, :hash "1549245436"} {:id "defn-/run-command", :kind "defn-", :line 71, :end-line 76, :hash "-23417405"} {:id "defn-/scripted-commands", :kind "defn-", :line 78, :end-line 80, :hash "1340423622"} {:id "defn-/run-script!", :kind "defn-", :line 82, :end-line 92, :hash "-315896525"} {:id "defn/inspect-main", :kind "defn", :line 94, :end-line 109, :hash "530431845"} {:id "defn/inspect", :kind "defn", :line 111, :end-line 112, :hash "749474069"}]}
;; clj-mutate-manifest-end
