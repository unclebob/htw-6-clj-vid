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
