(ns htw.property-test
  (:require [clojure.test :refer [deftest is testing]]
            [htw.arrow :as arrow]
            [htw.cave :as cave]
            [htw.game :as game]
            [htw.placement :as placement]
            [htw.random :as random]))

(def sample-seeds (range 0 500))

(deftest seeded-placement-properties
  (doseq [seed sample-seeds]
    (testing (str "seed " seed)
      (let [state (game/start-game seed)]
        (is (= 6 (count (game/occupied-rooms state))))
        (is (= 5 (count (game/hazard-rooms state))))
        (is (every? cave/room? (game/occupied-rooms state)))
        (is (not (contains? (game/hazard-rooms state)
                            (:player-room state))))))))

(deftest seeded-room-order-properties
  (doseq [seed sample-seeds]
    (testing (str "seed " seed)
      (let [ordered-rooms (placement/seeded-room-order seed)]
        (is (= (count cave/rooms) (count ordered-rooms)))
        (is (= (set cave/rooms) (set ordered-rooms)))))))

(deftest topology-properties
  (doseq [room cave/rooms
          exit (cave/exits room)]
    (testing (str room " <-> " exit)
      (is (cave/connected? exit room))
      (is (not= room exit)))))

(deftest movement-properties
  (doseq [room cave/rooms
          destination cave/rooms
          :let [safe-wumpus-room (first (remove (hash-set room destination) cave/rooms))
                state (game/configured-game room safe-wumpus-room #{} #{})
                moved (game/try-move-player state destination)]]
    (testing (str room " -> " destination)
      (if (cave/connected? room destination)
        (do
          (is (= destination (:player-room moved)))
          (is (= :in-progress (:status moved))))
        (do
          (is (= room (:player-room moved)))
          (is (= game/invalid-move-message (:error moved))))))))

(deftest warning-properties
  (doseq [room cave/rooms]
    (testing (str "room " room)
      (let [neighbor (first (cave/exits room))
            state (game/configured-game room neighbor
                                        (set (rest (cave/exits room)))
                                        #{})]
        (is (= ["I SMELL A WUMPUS" "I FEEL A DRAFT"]
               (game/turn-warnings state)))))))

(deftest straight-arrow-path-properties
  (doseq [start-room cave/rooms
          first-room (cave/exits start-room)
          second-room (cave/exits first-room)
          :let [state (game/configured-game start-room 20 #{} #{})
                path [first-room second-room]]]
    (testing (str start-room " -> " path)
      (is (= path (arrow/visits state path))))))

(deftest seeded-random-choice-properties
  (doseq [seed sample-seeds
          salt [[:bat-transport 1 13]
                [:wumpus-wake 1 10 5]
                [:arrow-fallback 0 nil 1 20]]
          :let [options [2 5 8]]]
    (testing (str "seed " seed " salt " salt)
      (let [state {:seed seed}
            choice (random/choice state salt options)]
        (is (some #{choice} options))
        (is (= choice (random/choice state salt options)))))))

(deftest unseeded-random-choice-properties
  (is (= 2 (random/choice {} [:salt] [2 5 8])))
  (is (nil? (random/choice {:seed 1} [:salt] []))))
