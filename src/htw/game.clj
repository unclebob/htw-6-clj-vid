(ns htw.game
  (:require [clojure.string :as str]
            [htw.cave :as cave]))

(def pit-message "YYYIIIIEEEE . . . FELL IN PIT")
(def bat-message "ZAP -- SUPER BAT SNATCH! ELSEWHEREVILLE FOR YOU!")
(def invalid-move-message "CAN'T MOVE THERE")
(def move-choice-prefix "move to ")
(def wumpus-hit-message "AHA! YOU GOT THE WUMPUS!")
(def self-hit-message "OOPS! ARROW GOT YOU!")
(def out-of-arrows-message "YOU RAN OUT OF ARROWS")

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
      (update :status #(or % :in-progress))
      (update :arrows #(or % 5))))

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

(defn- move-choice? [choice]
  (and (string? choice)
       (str/starts-with? choice move-choice-prefix)))

(defn- parse-move-choice [choice]
  (Long/parseLong (subs choice (count move-choice-prefix))))

(defn- wake-choice-handlers [state]
  [[#(= "stay" %) (constantly (:wumpus-room state))]
   [move-choice? parse-move-choice]
   [integer? identity]])

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
    (if (cave/connected? (:player-room state) destination-room)
      (resolve-arrival (assoc state :player-room destination-room))
      (assoc state :error invalid-move-message))))

(defn move-player [state destination-room]
  (try-move-player state destination-room))

(defn- legal-shot-length? [path]
  (<= 1 (count path) 5))

(defn- fallback-arrow-room [previous-room current-room]
  (or (first (remove #{previous-room} (cave/exits current-room)))
      (first (cave/exits current-room))))

(defn- next-arrow-room [state previous-room current-room requested-room deviation-used?]
  (if (cave/connected? current-room requested-room)
    {:room requested-room :deviation-used? deviation-used?}
    (if (and (:arrow-deviation-room state) (not deviation-used?))
      {:room (:arrow-deviation-room state) :deviation-used? true}
      {:room (fallback-arrow-room previous-room current-room)
       :deviation-used? deviation-used?})))

(defn- arrow-visits [state path]
  (loop [current-room (:player-room state)
         previous-room nil
         remaining path
         visits []
         deviation-used? false]
    (if-let [requested-room (first remaining)]
      (let [{:keys [room deviation-used?]}
            (next-arrow-room state previous-room current-room requested-room deviation-used?)]
        (recur room current-room (rest remaining) (conj visits room) deviation-used?))
      visits)))

(defn try-shoot-arrow [state path]
  (let [state (normalize-state state)]
    (if-not (legal-shot-length? path)
      (assoc state :error "CAN'T SHOOT THERE")
      (let [visits (arrow-visits state path)]
        (cond
          (some #{(:wumpus-room state)} visits)
          (-> state
              (assoc :arrow-visits visits
                     :status :won)
              (append-message wumpus-hit-message))

          (some #{(:player-room state)} visits)
          (-> state
              (assoc :arrow-visits visits
                     :status :lost)
              (append-message self-hit-message))

          :else
          (let [spent (update state :arrows dec)
                woken (wake-wumpus spent)
                exhausted? (and (zero? (:arrows woken))
                                (= :in-progress (:status woken)))]
            (cond-> (assoc woken :arrow-visits visits)
              exhausted? (assoc :status :lost)
              exhausted? (append-message out-of-arrows-message))))))))

(defn shoot-arrow [state path]
  (try-shoot-arrow state path))
