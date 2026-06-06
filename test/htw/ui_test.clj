(ns htw.ui-test
  (:require [clojure.test :refer [deftest is]]
            [htw.game :as game]
            [htw.ui :as ui]))

(defn configured-game
  [player wumpus pits bats]
  (game/configured-game player wumpus (set pits) (set bats)))

(deftest turn-display-shows-state-and-prompt
  (let [output (:output (ui/display-turn (assoc (configured-game 1 2 [5 14] [8 17])
                                                :arrows 5)))]
    (is (some #{"YOU ARE IN ROOM 1"} output))
    (is (some #{"TUNNELS LEAD TO 2 5 8"} output))
    (is (some #{"I SMELL A WUMPUS"} output))
    (is (some #{"BATS NEARBY"} output))
    (is (some #{"I FEEL A DRAFT"} output))
    (is (some #{"ARROWS LEFT: 5"} output))
    (is (some #{"SHOOT OR MOVE (S-M)?"} output))))

(deftest commands-are-case-insensitive-and-invalid-input-does-not-advance
  (let [moved (:state (ui/enter-command (configured-game 1 13 [14 15] [16 17]) "M 5"))
        won (ui/enter-command (configured-game 1 5 [14 15] [16 17]) "S 5")
        rejected (ui/enter-command (assoc (configured-game 1 13 [14 15] [16 17]) :arrows 5)
                                   "x")]
    (is (= 5 (:player-room moved)))
    (is (= :won (:status (:state won))))
    (is (some #{"AHA! YOU GOT THE WUMPUS!"} (:output won)))
    (is (= 1 (:player-room (:state rejected))))
    (is (some #{"X IS NOT A COMMAND"} (:output rejected)))))

(deftest terminal-outcomes-include-taunts-and-replay-prompt
  (let [pit (ui/enter-command (configured-game 1 13 [2 15] [16 17]) "m 2")
        win (ui/enter-command (configured-game 1 2 [14 15] [16 17]) "s 2")]
    (is (= :lost (:status (:state pit))))
    (is (some #{"HA HA HA - YOU LOSE!"} (:output pit)))
    (is (some #{"SAME SET UP (Y-N)?"} (:output pit)))
    (is (= :won (:status (:state win))))
    (is (some #{"HEE HEE HEE - THE WUMPUS'LL GETCHA NEXT TIME!!"} (:output win)))))

(deftest in-progress-event-messages-are-printed-once
  (let [bat-state (assoc (configured-game 1 13 [14 15] [2 17])
                         :bat-transport-room 10)
        transported (ui/enter-command bat-state "m 2")
        continued (ui/enter-command (:state transported) "m 11")]
    (is (= :in-progress (:status (:state transported))))
    (is (some #{game/bat-message} (:output transported)))
    (is (= [] (:messages (:state transported))))
    (is (not-any? #{game/bat-message} (:output continued)))))

(deftest replay-can-preserve-setup
  (let [state (configured-game 1 13 [2 15] [16 17])
        lost (:state (ui/enter-command state "m 2"))
        next-game (ui/replay lost "y")]
    (is (= 1 (:player-room next-game)))
    (is (= 13 (:wumpus-room next-game)))
    (is (= #{2 15} (:pit-rooms next-game)))
    (is (= #{16 17} (:bat-rooms next-game)))
    (is (= :in-progress (:status next-game)))))
