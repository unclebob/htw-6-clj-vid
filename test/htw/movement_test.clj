(ns htw.movement-test
  (:require [clojure.test :refer [are deftest is]]
            [htw.game :as game]))

(defn configured-game
  ([player wumpus pits bats]
   (game/configured-game player wumpus (set pits) (set bats)))
  ([player wumpus pits bats options]
   (merge (configured-game player wumpus pits bats) options)))

(deftest legal-and-illegal-movement
  (let [state (configured-game 1 13 [14 15] [16 17])
        moved (game/move-player state 2)
        rejected (game/try-move-player state 3)]
    (is (= 2 (:player-room moved)))
    (is (= :in-progress (:status moved)))
    (is (= "CAN'T MOVE THERE" (:error rejected)))
    (is (= 1 (:player-room rejected)))
    (is (= :in-progress (:status rejected)))))

(deftest pit-and-bat-resolution
  (let [pit-loss (game/move-player (configured-game 1 13 [2 15] [16 17]) 2)
        bat-safe (game/move-player
                   (configured-game 1 13 [14 15] [2 17] {:bat-transport-room 10})
                   2)
        bat-pit (game/move-player
                  (configured-game 1 13 [10 15] [2 17] {:bat-transport-room 10})
                  2)]
    (is (= :lost (:status pit-loss)))
    (is (= ["YYYIIIIEEEE . . . FELL IN PIT"] (:messages pit-loss)))
    (is (= 10 (:player-room bat-safe)))
    (is (= :in-progress (:status bat-safe)))
    (is (= ["ZAP -- SUPER BAT SNATCH! ELSEWHEREVILLE FOR YOU!"] (:messages bat-safe)))
    (is (= :lost (:status bat-pit)))
    (is (= ["ZAP -- SUPER BAT SNATCH! ELSEWHEREVILLE FOR YOU!"
            "YYYIIIIEEEE . . . FELL IN PIT"]
           (:messages bat-pit)))))

(deftest warnings-are-adjacent-stable-and-deduplicated
  (are [state warnings] (= warnings (game/turn-warnings state))
    (configured-game 1 2 [3 4] [6 7]) ["I SMELL A WUMPUS"]
    (configured-game 1 6 [2 4] [7 9]) ["I FEEL A DRAFT"]
    (configured-game 1 6 [3 4] [5 7]) ["BATS NEARBY"]
    (configured-game 1 2 [5 14] [8 17]) ["I SMELL A WUMPUS" "BATS NEARBY" "I FEEL A DRAFT"]
    (configured-game 1 6 [3 4] [7 9]) []))

(deftest wumpus-wake-options-and-movement
  (is (= [10 2 9 11] (game/wumpus-wake-options (configured-game 1 10 [] []))))
  (is (= [20 13 16 19] (game/wumpus-wake-options (configured-game 5 20 [] []))))
  (let [lost (game/move-player
               (configured-game 1 2 [14 15] [16 17] {:wumpus-wake-choice "stay"})
               2)
        escaped (game/move-player
                  (configured-game 1 2 [14 15] [16 17] {:wumpus-wake-choice "move to 3"})
                  2)
        immune (game/wake-wumpus
                 (configured-game 1 10 [2 14] [9 17] {:wumpus-wake-choice "move to 2"}))]
    (is (= :lost (:status lost)))
    (is (= 2 (:wumpus-room lost)))
    (is (= :in-progress (:status escaped)))
    (is (= 3 (:wumpus-room escaped)))
    (is (= :in-progress (:status immune)))
    (is (= 2 (:wumpus-room immune)))))
