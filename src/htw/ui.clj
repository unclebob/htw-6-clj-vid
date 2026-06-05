(ns htw.ui
  (:require [clojure.string :as str]
            [htw.cave :as cave]
            [htw.game :as game]))

(def win-taunt "HEE HEE HEE - THE WUMPUS'LL GETCHA NEXT TIME!!")
(def loss-taunt "HA HA HA - YOU LOSE!")
(def replay-prompt "SAME SET UP (Y-N)?")
(def turn-prompt "SHOOT OR MOVE (S-M)?")

(def instructions-lines
  ["WELCOME TO 'HUNT THE WUMPUS'"
   "THE WUMPUS LIVES IN A CAVE OF 20 ROOMS: EACH ROOM HAS 3 TUNNELS LEADING TO OTHER ROOMS."])

(defn- parse-int [value]
  (Long/parseLong value))

(defn- tokens [command]
  (str/split (str/trim command) #"\s+"))

(defn display-turn [state]
  (let [state (if (:arrows state) state (assoc state :arrows 5))
        exits (str/join " " (cave/exits (:player-room state)))
        output (into [(str "YOU ARE IN ROOM " (:player-room state))
                      (str "TUNNELS LEAD TO " exits)]
                     (game/turn-warnings state))]
    {:state state
     :output (conj (vec output)
                   (str "ARROWS LEFT: " (:arrows state))
                   turn-prompt)}))

(defn- terminal-output [state]
  (cond
    (= :won (:status state))
    (conj (:messages state) win-taunt)

    (= :lost (:status state))
    (conj (:messages state) loss-taunt replay-prompt)

    :else
    (:output (display-turn state))))

(defn- result [state extra-output]
  {:state state
   :output (vec (concat extra-output (terminal-output state)))})

(defn- invalid-result [state message]
  {:state state
   :output (vec (concat [message] (:output (display-turn state))))})

(defn- move-command [state args]
  (if (= 1 (count args))
    (let [moved (game/try-move-player state (parse-int (first args)))]
      (if (:error moved)
        (invalid-result state (:error moved))
        (result moved [])))
    (invalid-result state "CAN'T MOVE THERE")))

(defn- shoot-command [state args]
  (let [path (mapv parse-int args)
        shot (game/try-shoot-arrow state path)]
    (if (:error shot)
      (invalid-result state (:error shot))
      (result shot []))))

(defn- remember-setup [state]
  (if (:setup state)
    state
    (assoc state :setup (select-keys state [:player-room :wumpus-room :pit-rooms :bat-rooms]))))

(defn enter-command [state command]
  (let [state (remember-setup state)
        [verb & args] (tokens command)
        verb (str/lower-case (or verb ""))]
    (case verb
      "m" (move-command state args)
      "s" (shoot-command state args)
      (invalid-result state (str (str/upper-case verb) " IS NOT A COMMAND")))))

(defn replay [state answer]
  (if (= "y" (str/lower-case (str/trim answer)))
    (-> (:setup state)
        (assoc :status :in-progress
               :messages []
               :arrows 5))
    (game/start-game 1974)))

(defn answer-instructions [state answer]
  (let [base-output (if (= "y" (str/lower-case (str/trim answer)))
                      instructions-lines
                      [])]
    (update (display-turn state) :output #(vec (concat base-output %)))))
