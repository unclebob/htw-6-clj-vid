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

(defn- wake-choice-handlers [state]
  [[#(= :stay %) (constantly (:wumpus-room state))]
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

;; clj-mutate-manifest-begin
;; {:version 1, :tested-at "2026-06-05T15:06:06.722257-05:00", :module-hash "-327587831", :forms [{:id "form/0/ns", :kind "ns", :line 1, :end-line 4, :hash "-200258023"} {:id "def/pit-message", :kind "def", :line 6, :end-line 6, :hash "-786903927"} {:id "def/bat-message", :kind "def", :line 7, :end-line 7, :hash "-2114143883"} {:id "def/invalid-move-message", :kind "def", :line 8, :end-line 8, :hash "-1975420308"} {:id "def/wumpus-hit-message", :kind "def", :line 9, :end-line 9, :hash "-590885553"} {:id "def/self-hit-message", :kind "def", :line 10, :end-line 10, :hash "2000715607"} {:id "def/out-of-arrows-message", :kind "def", :line 11, :end-line 11, :hash "-1285208638"} {:id "def/invalid-shot-message", :kind "def", :line 12, :end-line 12, :hash "-751353568"} {:id "defn-/place-entities", :kind "defn-", :line 14, :end-line 21, :hash "2103194020"} {:id "defn/configured-game", :kind "defn", :line 23, :end-line 29, :hash "572511432"} {:id "defn-/normalize-state", :kind "defn-", :line 31, :end-line 37, :hash "-1300842534"} {:id "defn/start-game", :kind "defn", :line 39, :end-line 40, :hash "1737958242"} {:id "defn/reuse-setup", :kind "defn", :line 42, :end-line 43, :hash "-1770652719"} {:id "defn/hazard-rooms", :kind "defn", :line 45, :end-line 46, :hash "-1217263101"} {:id "defn/occupied-rooms", :kind "defn", :line 48, :end-line 49, :hash "751994569"} {:id "defn/adjacent-hazards", :kind "defn", :line 51, :end-line 55, :hash "-882909919"} {:id "defn/turn-warnings", :kind "defn", :line 57, :end-line 63, :hash "1045481678"} {:id "defn/wumpus-wake-options", :kind "defn", :line 65, :end-line 66, :hash "441918983"} {:id "defn-/append-message", :kind "defn-", :line 68, :end-line 69, :hash "-2115918181"} {:id "defn-/wake-choice-handlers", :kind "defn-", :line 71, :end-line 73, :hash "1048816146"} {:id "defn-/selected-wake-room", :kind "defn-", :line 75, :end-line 79, :hash "-1923414951"} {:id "defn-/wake-choice-room", :kind "defn-", :line 81, :end-line 83, :hash "305017871"} {:id "form/22/declare", :kind "declare", :line 85, :end-line 85, :hash "-404549809"} {:id "defn/wake-wumpus", :kind "defn", :line 87, :end-line 93, :hash "-1544172563"} {:id "defn-/transport-room", :kind "defn-", :line 95, :end-line 96, :hash "-217651789"} {:id "defn-/resolve-arrival", :kind "defn-", :line 98, :end-line 115, :hash "-1395146857"} {:id "defn/try-move-player", :kind "defn", :line 117, :end-line 121, :hash "-334120482"} {:id "defn/move-player", :kind "defn", :line 123, :end-line 124, :hash "2048385396"} {:id "defn-/legal-shot-length?", :kind "defn-", :line 126, :end-line 127, :hash "1969989613"} {:id "defn-/arrow-hit-result", :kind "defn-", :line 129, :end-line 133, :hash "1958350931"} {:id "defn-/missed-arrow-result", :kind "defn-", :line 135, :end-line 142, :hash "-1567399539"} {:id "defn-/shot-result", :kind "defn-", :line 144, :end-line 153, :hash "435965206"} {:id "defn/try-shoot-arrow", :kind "defn", :line 155, :end-line 159, :hash "-1093287084"} {:id "defn/shoot-arrow", :kind "defn", :line 161, :end-line 162, :hash "-1106796450"}]}
;; clj-mutate-manifest-end
