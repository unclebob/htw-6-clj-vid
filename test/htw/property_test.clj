(ns htw.property-test
  (:require [clojure.test :refer [deftest is testing]]
            [htw.cave :as cave]
            [htw.game :as game]
            [htw.placement :as placement]))

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
