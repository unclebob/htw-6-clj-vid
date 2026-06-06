(ns htw.shell
  (:require [htw.cli-options :as options]
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

(defn- explicit-launch-options? [parsed-options]
  (or (:seed parsed-options) (options/explicit-setup? parsed-options)))

(defn- with-observed-seed [launch show-seed?]
  (if show-seed?
    (update launch :output conj (str "SEED: " (:seed launch)))
    launch))

(defn- initial-launch [args]
  (let [parsed-options (options/parse-args args)]
    (cond
      (explicit-launch-options? parsed-options)
      (with-observed-seed {:seed (:seed parsed-options)
                           :state (options/configured-state parsed-options)
                           :output [instructions-prompt]}
                          (:show-seed parsed-options))

      :else
      (with-observed-seed (launch-game) (:show-seed parsed-options)))))

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

(defn main [& args]
  (let [launch (initial-launch args)]
    (print-lines! (:output launch))
    (when-let [answer (read-line)]
      (let [{:keys [state output]} (answer-instructions launch answer)]
        (print-lines! output)
        (run-game-loop! state)))))

;; clj-mutate-manifest-begin
;; {:version 1, :tested-at "2026-06-06T12:22:06.857306-05:00", :module-hash "-1120935677", :forms [{:id "form/0/ns", :kind "ns", :line 1, :end-line 4, :hash "966412087"} {:id "def/instructions-prompt", :kind "def", :line 6, :end-line 6, :hash "282711655"} {:id "form/2/defonce", :kind "defonce", :line 8, :end-line 8, :hash "650038718"} {:id "defn/random-seed", :kind "defn", :line 10, :end-line 11, :hash "363677862"} {:id "defn/launch-game", :kind "defn", :line 13, :end-line 17, :hash "2085306560"} {:id "defn/answer-instructions", :kind "defn", :line 19, :end-line 21, :hash "-305170808"} {:id "defn-/explicit-launch-options?", :kind "defn-", :line 23, :end-line 24, :hash "-440117795"} {:id "defn-/with-observed-seed", :kind "defn-", :line 26, :end-line 29, :hash "-1910644406"} {:id "defn-/initial-launch", :kind "defn-", :line 31, :end-line 41, :hash "368112718"} {:id "defn-/print-lines!", :kind "defn-", :line 43, :end-line 46, :hash "211376327"} {:id "defn-/continue-after-terminal", :kind "defn-", :line 48, :end-line 54, :hash "2131642771"} {:id "defn-/next-loop-state", :kind "defn-", :line 56, :end-line 59, :hash "-1591562258"} {:id "defn-/continue-loop?", :kind "defn-", :line 61, :end-line 62, :hash "-713442341"} {:id "defn-/enter-shell-command", :kind "defn-", :line 64, :end-line 67, :hash "-654402039"} {:id "defn-/read-next-state", :kind "defn-", :line 69, :end-line 72, :hash "-805143145"} {:id "defn-/run-game-loop!", :kind "defn-", :line 74, :end-line 77, :hash "-1259505018"} {:id "defn/main", :kind "defn", :line 79, :end-line 85, :hash "-465328876"}]}
;; clj-mutate-manifest-end
