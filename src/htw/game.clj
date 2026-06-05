(ns htw.game
  (:require [htw.cave :as cave]))

(defn- shuffled-rooms [seed]
  (let [rooms (java.util.ArrayList. cave/rooms)
        rng (java.util.Random. (long seed))]
    (java.util.Collections/shuffle rooms rng)
    (vec rooms)))

(defn start-game [seed]
  (let [[player wumpus pit-a pit-b bat-a bat-b] (shuffled-rooms seed)]
    {:player-room player
     :wumpus-room wumpus
     :pit-rooms #{pit-a pit-b}
     :bat-rooms #{bat-a bat-b}}))

(defn reuse-setup [state]
  state)

(defn hazard-rooms [{:keys [wumpus-room pit-rooms bat-rooms]}]
  (set (concat [wumpus-room] pit-rooms bat-rooms)))

(defn occupied-rooms [{:keys [player-room] :as state}]
  (conj (hazard-rooms state) player-room))

(defn adjacent-hazards [{:keys [wumpus-room pit-rooms bat-rooms]} room]
  (let [neighbors (set (cave/exits room))]
    {:wumpus (if (neighbors wumpus-room) 1 0)
     :pit (count (filter neighbors pit-rooms))
     :bat (count (filter neighbors bat-rooms))}))
