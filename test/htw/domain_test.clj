(ns htw.domain-test
  (:require [clojure.test :refer [are deftest is testing]]
            [htw.cave :as cave]
            [htw.game :as game]))

(deftest canonical-cave-topology
  (is (= {1 [2 5 8]
          2 [1 3 10]
          3 [2 4 12]
          4 [3 5 14]
          5 [1 4 6]
          6 [5 7 15]
          7 [6 8 17]
          8 [1 7 9]
          9 [8 10 18]
          10 [2 9 11]
          11 [10 12 19]
          12 [3 11 13]
          13 [12 14 20]
          14 [4 13 15]
          15 [6 14 16]
          16 [15 17 20]
          17 [7 16 18]
          18 [9 17 19]
          19 [11 18 20]
          20 [13 16 19]}
         cave/topology))
  (is (= 20 (count cave/rooms)))
  (is (every? #(= 3 (count (cave/exits %))) cave/rooms))
  (is (cave/bidirectional?))
  (is (not-any? #(contains? (set (cave/exits %)) %) cave/rooms))
  (is (= (set cave/rooms) (cave/reachable-from 1))))

(deftest seeded-placement-uses-distinct-valid-rooms
  (doseq [seed [1001 2002 3003 1973]]
    (testing (str "seed " seed)
      (let [state (game/start-game seed)]
        (is (contains? (set cave/rooms) (:player-room state)))
        (is (contains? (set cave/rooms) (:wumpus-room state)))
        (is (= 2 (count (:pit-rooms state))))
        (is (= 2 (count (:bat-rooms state))))
        (is (= 5 (count (game/hazard-rooms state))))
        (is (= 6 (count (game/occupied-rooms state))))
        (is (empty? (filter (complement (set cave/rooms))
                            (game/occupied-rooms state))))))))

(deftest player-starts-away-from-hazards
  (doseq [seed [404 505 606]]
    (let [{:keys [player-room] :as state} (game/start-game seed)]
      (is (not= player-room (:wumpus-room state)))
      (is (not (contains? (:pit-rooms state) player-room)))
      (is (not (contains? (:bat-rooms state) player-room))))))

(deftest seeded-setup-is-reproducible
  (doseq [seed [1973 1975 1976]]
    (is (= (game/start-game seed) (game/start-game seed)))))

(deftest same-setup-reuses-placement
  (let [state (game/start-game 1973)]
    (is (= state (game/reuse-setup state)))))

(deftest adjacent-hazards-reports-only-neighboring-hazards
  (are [player-room wumpus-room pit-rooms bat-rooms expected]
       (= expected
          (game/adjacent-hazards
            {:player-room player-room
             :wumpus-room wumpus-room
             :pit-rooms (set pit-rooms)
             :bat-rooms (set bat-rooms)}
            player-room))
    1 2 [3 4] [5 6] {:wumpus 1 :pit 0 :bat 1}
    10 11 [2 18] [9 13] {:wumpus 1 :pit 1 :bat 1}
    20 1 [13 16] [18 19] {:wumpus 0 :pit 2 :bat 1}))
