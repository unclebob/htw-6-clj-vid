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

;; clj-mutate-manifest-begin
;; {:version 1, :tested-at "2026-06-05T15:26:25.930914-05:00", :module-hash "-1938609947", :forms [{:id "form/0/ns", :kind "ns", :line 1, :end-line 3, :hash "1097170160"} {:id "def/instructions-prompt", :kind "def", :line 5, :end-line 5, :hash "282711655"} {:id "form/2/defonce", :kind "defonce", :line 7, :end-line 7, :hash "650038718"} {:id "defn/random-seed", :kind "defn", :line 9, :end-line 10, :hash "363677862"} {:id "defn/launch-game", :kind "defn", :line 12, :end-line 16, :hash "2085306560"} {:id "defn/answer-instructions", :kind "defn", :line 18, :end-line 20, :hash "-305170808"} {:id "defn/-main", :kind "defn", :line 22, :end-line 28, :hash "-549527165"}]}
;; clj-mutate-manifest-end
