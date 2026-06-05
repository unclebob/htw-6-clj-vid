(ns htw.shooting-test
  (:require [clojure.test :refer [deftest is]]
            [htw.game :as game]))

(defn configured-game
  [player wumpus options]
  (merge (game/configured-game player wumpus #{} #{})
         {:arrows 5}
         options))

(deftest arrow-hits-wumpus-and-player
  (let [hit (game/shoot-arrow (configured-game 1 11 {}) [2 10 11])
        self-hit (game/shoot-arrow (configured-game 1 13 {}) [2 1])]
    (is (= [2 10 11] (:arrow-visits hit)))
    (is (= :won (:status hit)))
    (is (= [game/wumpus-hit-message] (:messages hit)))
    (is (= [2 1] (:arrow-visits self-hit)))
    (is (= :lost (:status self-hit)))
    (is (= [game/self-hit-message] (:messages self-hit)))))

(deftest invalid-arrow-segment-deviates
  (let [state (configured-game 1 13 {:arrow-deviation-room 5
                                     :wumpus-wake-choice :stay})
        result (game/shoot-arrow state [3 4])]
    (is (= [5 4] (:arrow-visits result)))
    (is (= 4 (:arrows result)))
    (is (= :in-progress (:status result)))))

(deftest missed-arrow-wakes-wumpus-and-can-lose-on-exhaustion
  (let [miss (game/shoot-arrow
               (configured-game 1 10 {:wumpus-wake-choice 2})
               [5])
        eaten (game/shoot-arrow
                (configured-game 1 10 {:wumpus-wake-choice 1})
                [5])
        out (game/shoot-arrow
              (configured-game 1 10 {:arrows 1
                                     :wumpus-wake-choice :stay})
              [5])]
    (is (= 4 (:arrows miss)))
    (is (= 2 (:wumpus-room miss)))
    (is (= :in-progress (:status miss)))
    (is (= :lost (:status eaten)))
    (is (= 0 (:arrows out)))
    (is (= :lost (:status out)))
    (is (= [game/out-of-arrows-message] (:messages out)))))

(deftest invalid-shot-is-rejected-without-spending-arrow
  (let [empty-shot (game/try-shoot-arrow (configured-game 1 10 {}) [])
        long-shot (game/try-shoot-arrow (configured-game 1 10 {}) [2 10 11 12 13 14])]
    (is (= 5 (:arrows empty-shot)))
    (is (= :in-progress (:status empty-shot)))
    (is (= game/invalid-shot-message (:error empty-shot)))
    (is (= game/invalid-shot-message (:error long-shot)))
    (is (= 5 (:arrows long-shot)))))
