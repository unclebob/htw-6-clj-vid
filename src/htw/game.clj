(ns htw.game
  (:require [htw.arrow :as arrow]
            [htw.cave :as cave]
            [htw.placement :as placement]
            [htw.random :as random]))

(def pit-message "YYYIIIIEEEE . . . FELL IN PIT")
(def bat-message "ZAP -- SUPER BAT SNATCH! ELSEWHEREVILLE FOR YOU!")
(def invalid-move-message "CAN'T MOVE THERE")
(def wumpus-hit-message "AHA! YOU GOT THE WUMPUS!")
(def self-hit-message "OOPS! ARROW GOT YOU!")
(def wumpus-bump-message "... OOPS! BUMPED A WUMPUS!")
(def wumpus-got-you-message "TSK TSK TSK - WUMPUS GOT YOU!")
(def missed-arrow-message "MISSED")
(def out-of-arrows-message "YOU RAN OUT OF ARROWS")
(def invalid-shot-message "CAN'T SHOOT THERE")
(def crooked-arrow-message "ARROWS AREN'T THAT CROOKED - TRY ANOTHER ROOM")

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
  (assoc (place-entities (placement/seeded-room-order seed)) :seed seed))

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
      (random/choice state
                     [:wumpus-wake (:player-room state) (:wumpus-room state) (:arrows state)]
                     (wumpus-wake-options state))))

(declare resolve-arrival)

(defn wake-wumpus [state]
  (let [state (normalize-state state)
        new-room (wake-choice-room state)
        moved (assoc state :wumpus-room new-room)]
    (if (= (:player-room moved) new-room)
      (assoc moved :status :lost)
      moved)))

(defn- wake-bumped-wumpus [state]
  (let [woken (wake-wumpus (append-message state wumpus-bump-message))]
    (if (= :lost (:status woken))
      (append-message woken wumpus-got-you-message)
      woken)))

(defn- transport-room [{:keys [bat-transport-room bat-rooms] :as state}]
  (if (and bat-transport-room (not (contains? bat-rooms bat-transport-room)))
    bat-transport-room
    (random/choice state
                   [:bat-transport (:player-room state) (:wumpus-room state)]
                   (remove bat-rooms cave/rooms))))

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
      (wake-bumped-wumpus state)

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

(defn- too-crooked? [path]
  (some true? (map = path (drop 2 path))))

(defn- invalid-shot-error [path]
  (cond
    (not (legal-shot-length? path)) invalid-shot-message
    (too-crooked? path) crooked-arrow-message))

(defn- arrow-hit-result [state visits status message]
  (-> state
      (assoc :arrow-visits visits
             :status status)
      (append-message message)))

(defn- missed-arrow-result [state visits]
  (let [spent (-> state
                  (append-message missed-arrow-message)
                  (update :arrows dec))
        woken (wake-wumpus spent)
        exhausted? (and (zero? (:arrows woken))
                        (= :in-progress (:status woken)))]
    (cond-> (assoc woken :arrow-visits visits)
      (= :lost (:status woken)) (append-message wumpus-got-you-message)
      exhausted? (assoc :status :lost)
      exhausted? (append-message out-of-arrows-message))))

(defn- terminal-visit? [state room]
  (or (= (:wumpus-room state) room)
      (= (:player-room state) room)))

(defn- visits-through-hit [state visits]
  (let [[before after] (split-with #(not (terminal-visit? state %)) visits)]
    (vec (concat before (take 1 after)))))

(defn- shot-result [state visits]
  (let [resolved-visits (visits-through-hit state visits)
        terminal-room (last resolved-visits)]
    (cond
      (= (:wumpus-room state) terminal-room)
      (arrow-hit-result state resolved-visits :won wumpus-hit-message)

      (= (:player-room state) terminal-room)
      (arrow-hit-result state resolved-visits :lost self-hit-message)

      :else
      (missed-arrow-result state visits))))

(defn try-shoot-arrow [state path]
  (let [state (normalize-state state)]
    (if-let [error (invalid-shot-error path)]
      (assoc state :error error)
      (shot-result state (arrow/visits state path)))))

(defn shoot-arrow [state path]
  (try-shoot-arrow state path))

;; clj-mutate-manifest-begin
;; {:version 1, :tested-at "2026-06-06T12:20:49.365036-05:00", :module-hash "1248423706", :forms [{:id "form/0/ns", :kind "ns", :line 1, :end-line 5, :hash "-1994572790"} {:id "def/pit-message", :kind "def", :line 7, :end-line 7, :hash "-786903927"} {:id "def/bat-message", :kind "def", :line 8, :end-line 8, :hash "-2114143883"} {:id "def/invalid-move-message", :kind "def", :line 9, :end-line 9, :hash "-1975420308"} {:id "def/wumpus-hit-message", :kind "def", :line 10, :end-line 10, :hash "-590885553"} {:id "def/self-hit-message", :kind "def", :line 11, :end-line 11, :hash "2000715607"} {:id "def/out-of-arrows-message", :kind "def", :line 12, :end-line 12, :hash "-1285208638"} {:id "def/invalid-shot-message", :kind "def", :line 13, :end-line 13, :hash "-751353568"} {:id "defn-/place-entities", :kind "defn-", :line 15, :end-line 22, :hash "2103194020"} {:id "defn/configured-game", :kind "defn", :line 24, :end-line 30, :hash "572511432"} {:id "defn-/normalize-state", :kind "defn-", :line 32, :end-line 38, :hash "-1300842534"} {:id "defn/start-game", :kind "defn", :line 40, :end-line 41, :hash "6658786"} {:id "defn/reuse-setup", :kind "defn", :line 43, :end-line 44, :hash "-1770652719"} {:id "defn/hazard-rooms", :kind "defn", :line 46, :end-line 47, :hash "-1217263101"} {:id "defn/occupied-rooms", :kind "defn", :line 49, :end-line 50, :hash "751994569"} {:id "defn/adjacent-hazards", :kind "defn", :line 52, :end-line 56, :hash "-882909919"} {:id "defn/turn-warnings", :kind "defn", :line 58, :end-line 64, :hash "1045481678"} {:id "defn/wumpus-wake-options", :kind "defn", :line 66, :end-line 67, :hash "441918983"} {:id "defn-/append-message", :kind "defn-", :line 69, :end-line 70, :hash "-2115918181"} {:id "defn-/move-to-choice?", :kind "defn-", :line 72, :end-line 73, :hash "415600035"} {:id "defn-/wake-choice-handlers", :kind "defn-", :line 75, :end-line 78, :hash "1165950794"} {:id "defn-/selected-wake-room", :kind "defn-", :line 80, :end-line 84, :hash "-1923414951"} {:id "defn-/wake-choice-room", :kind "defn-", :line 86, :end-line 90, :hash "-1605337701"} {:id "form/23/declare", :kind "declare", :line 92, :end-line 92, :hash "-404549809"} {:id "defn/wake-wumpus", :kind "defn", :line 94, :end-line 100, :hash "-1544172563"} {:id "defn-/transport-room", :kind "defn-", :line 102, :end-line 107, :hash "492463688"} {:id "defn-/resolve-arrival", :kind "defn-", :line 109, :end-line 126, :hash "-1395146857"} {:id "defn/try-move-player", :kind "defn", :line 128, :end-line 132, :hash "-334120482"} {:id "defn/move-player", :kind "defn", :line 134, :end-line 135, :hash "2048385396"} {:id "defn-/legal-shot-length?", :kind "defn-", :line 137, :end-line 138, :hash "1969989613"} {:id "defn-/arrow-hit-result", :kind "defn-", :line 140, :end-line 144, :hash "1958350931"} {:id "defn-/missed-arrow-result", :kind "defn-", :line 146, :end-line 153, :hash "-1567399539"} {:id "defn-/terminal-visit?", :kind "defn-", :line 155, :end-line 157, :hash "1100031903"} {:id "defn-/visits-through-hit", :kind "defn-", :line 159, :end-line 161, :hash "829699521"} {:id "defn-/shot-result", :kind "defn-", :line 163, :end-line 174, :hash "-198114627"} {:id "defn/try-shoot-arrow", :kind "defn", :line 176, :end-line 180, :hash "-1093287084"} {:id "defn/shoot-arrow", :kind "defn", :line 182, :end-line 183, :hash "-1106796450"}]}
;; clj-mutate-manifest-end
