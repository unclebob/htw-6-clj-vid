(ns htw.game
  (:require [clojure.string :as str]
            [htw.cave :as cave]))

(def pit-message "YYYIIIIEEEE . . . FELL IN PIT")
(def bat-message "ZAP -- SUPER BAT SNATCH! ELSEWHEREVILLE FOR YOU!")

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
     :bat-rooms #{bat-a bat-b}
     :status :in-progress
     :messages []}))

(defn configured-game [player-room wumpus-room pit-rooms bat-rooms]
  {:player-room player-room
   :wumpus-room wumpus-room
   :pit-rooms pit-rooms
   :bat-rooms bat-rooms
   :status :in-progress
   :messages []})

(defn- normalize-state [state]
  (-> state
      (update :pit-rooms #(or % #{}))
      (update :bat-rooms #(or % #{}))
      (update :messages #(or % []))
      (update :status #(or % :in-progress))))

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

(defn turn-warnings [{:keys [player-room] :as state}]
  (let [state (normalize-state state)
        {:keys [wumpus pit bat]} (adjacent-hazards state player-room)]
    (cond-> []
      (pos? wumpus) (conj "I SMELL A WUMPUS")
      (pos? bat) (conj "BATS NEARBY")
      (pos? pit) (conj "I FEEL A DRAFT"))))

(defn wumpus-wake-options [{:keys [wumpus-room]}]
  (vec (cons wumpus-room (cave/exits wumpus-room))))

(defn- append-message [state message]
  (update state :messages (fnil conj []) message))

(defn- wake-choice-room [state]
  (let [choice (:wumpus-wake-choice state)]
    (cond
      (= choice "stay") (:wumpus-room state)
      (and (string? choice) (str/starts-with? choice "move to "))
      (Long/parseLong (subs choice (count "move to ")))
      (integer? choice) choice
      :else (first (wumpus-wake-options state)))))

(declare resolve-arrival)

(defn wake-wumpus [state]
  (let [state (normalize-state state)
        new-room (wake-choice-room state)
        moved (assoc state :wumpus-room new-room)]
    (if (= (:player-room moved) new-room)
      (assoc moved :status :lost)
      moved)))

(defn- transport-room [state]
  (or (:bat-transport-room state) (first cave/rooms)))

(defn- resolve-arrival [state]
  (let [{:keys [player-room wumpus-room pit-rooms bat-rooms]} state]
    (cond
      (contains? pit-rooms player-room)
      (-> state
          (assoc :status :lost)
          (append-message pit-message))

      (contains? bat-rooms player-room)
      (-> state
          (append-message bat-message)
          (assoc :player-room (transport-room state))
          (resolve-arrival))

      (= wumpus-room player-room)
      (wake-wumpus state)

      :else state)))

(defn try-move-player [state destination-room]
  (let [state (normalize-state state)]
    (if (contains? (set (cave/exits (:player-room state))) destination-room)
      (resolve-arrival (assoc state :player-room destination-room))
      (assoc state :error "CAN'T MOVE THERE"))))

(defn move-player [state destination-room]
  (try-move-player state destination-room))
