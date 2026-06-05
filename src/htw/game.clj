(ns htw.game
  (:require [htw.cave :as cave]
            [htw.placement :as placement]))

(defn- place-entities [room-order]
  (let [[player wumpus pit-a pit-b bat-a bat-b] room-order]
    {:player-room player
     :wumpus-room wumpus
     :pit-rooms #{pit-a pit-b}
     :bat-rooms #{bat-a bat-b}}))

(defn start-game [seed]
  (place-entities (placement/seeded-room-order seed)))

(defn reuse-setup [state]
  state)

(defn hazard-rooms [{:keys [wumpus-room pit-rooms bat-rooms]}]
  (into #{wumpus-room} (concat pit-rooms bat-rooms)))

(defn occupied-rooms [{:keys [player-room] :as state}]
  (into #{player-room} (hazard-rooms state)))

(defn adjacent-hazards [{:keys [wumpus-room pit-rooms bat-rooms]} room]
  (let [neighbors (set (cave/exits room))]
    {:wumpus (if (neighbors wumpus-room) 1 0)
     :pit (count (filter neighbors pit-rooms))
     :bat (count (filter neighbors bat-rooms))}))
