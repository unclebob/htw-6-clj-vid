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

;; clj-mutate-manifest-begin
;; {:version 1, :tested-at "2026-06-05T14:45:49.612174-05:00", :module-hash "1143932432", :forms [{:id "form/0/ns", :kind "ns", :line 1, :end-line 3, :hash "-878077022"} {:id "defn-/place-entities", :kind "defn-", :line 5, :end-line 10, :hash "1431118401"} {:id "defn/start-game", :kind "defn", :line 12, :end-line 13, :hash "1737958242"} {:id "defn/reuse-setup", :kind "defn", :line 15, :end-line 16, :hash "-1770652719"} {:id "defn/hazard-rooms", :kind "defn", :line 18, :end-line 19, :hash "-1217263101"} {:id "defn/occupied-rooms", :kind "defn", :line 21, :end-line 22, :hash "751994569"} {:id "defn/adjacent-hazards", :kind "defn", :line 24, :end-line 28, :hash "-882909919"}]}
;; clj-mutate-manifest-end
