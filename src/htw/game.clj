(ns htw.game
  (:require [htw.arrow :as arrow]
            [htw.cave :as cave]
            [htw.placement :as placement]))

(def pit-message "YYYIIIIEEEE . . . FELL IN PIT")
(def bat-message "ZAP -- SUPER BAT SNATCH! ELSEWHEREVILLE FOR YOU!")
(def invalid-move-message "CAN'T MOVE THERE")
(def wumpus-hit-message "AHA! YOU GOT THE WUMPUS!")
(def self-hit-message "OOPS! ARROW GOT YOU!")
(def out-of-arrows-message "YOU RAN OUT OF ARROWS")
(def invalid-shot-message "CAN'T SHOOT THERE")

(defn- place-entities [room-order]
  (let [[player wumpus pit-a pit-b bat-a bat-b] room-order]
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
      (update :status #(or % :in-progress))
      (update :arrows #(or % 5))))

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

(defn- move-to-choice? [choice]
  (and (string? choice) (.startsWith choice "move to ")))

(defn- wake-choice-handlers [state]
  [[#(or (= :stay %) (= "stay" %)) (constantly (:wumpus-room state))]
   [integer? identity]
   [move-to-choice? #(Long/parseLong (subs % (count "move to ")))]])

(defn- selected-wake-room [state choice]
  (some (fn [[matches? room]]
          (when (matches? choice)
            (room choice)))
        (wake-choice-handlers state)))

(defn- wake-choice-room [state]
  (or (selected-wake-room state (:wumpus-wake-choice state))
      (first (wumpus-wake-options state))))

(declare resolve-arrival)

(defn wake-wumpus [state]
  (let [state (normalize-state state)
        new-room (wake-choice-room state)
        moved (assoc state :wumpus-room new-room)]
    (if (= (:player-room moved) new-room)
      (assoc moved :status :lost)
      moved)))

(defn- transport-room [{:keys [bat-transport-room bat-rooms]}]
  (if (and bat-transport-room (not (contains? bat-rooms bat-transport-room)))
    bat-transport-room
    (rand-nth (vec (remove bat-rooms cave/rooms)))))

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
    (if (cave/connected? (:player-room state) destination-room)
      (resolve-arrival (assoc state :player-room destination-room))
      (assoc state :error invalid-move-message))))

(defn move-player [state destination-room]
  (try-move-player state destination-room))

(defn- legal-shot-length? [path]
  (<= 1 (count path) 5))

(defn- arrow-hit-result [state visits status message]
  (-> state
      (assoc :arrow-visits visits
             :status status)
      (append-message message)))

(defn- missed-arrow-result [state visits]
  (let [spent (update state :arrows dec)
        woken (wake-wumpus spent)
        exhausted? (and (zero? (:arrows woken))
                        (= :in-progress (:status woken)))]
    (cond-> (assoc woken :arrow-visits visits)
      exhausted? (assoc :status :lost)
      exhausted? (append-message out-of-arrows-message))))

(defn- shot-result [state visits]
  (cond
    (some #{(:wumpus-room state)} visits)
    (arrow-hit-result state visits :won wumpus-hit-message)

    (some #{(:player-room state)} visits)
    (arrow-hit-result state visits :lost self-hit-message)

    :else
    (missed-arrow-result state visits)))

(defn try-shoot-arrow [state path]
  (let [state (normalize-state state)]
    (if-not (legal-shot-length? path)
      (assoc state :error invalid-shot-message)
      (shot-result state (arrow/visits state path)))))

(defn shoot-arrow [state path]
  (try-shoot-arrow state path))
