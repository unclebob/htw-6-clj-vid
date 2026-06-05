(ns htw.cli
  (:require [htw.game :as game]
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

(defn -main [& _]
  (let [launch (launch-game)]
    (println instructions-prompt)
    (flush)
    (when-let [answer (read-line)]
      (doseq [line (:output (answer-instructions launch answer))]
        (println line)))))
