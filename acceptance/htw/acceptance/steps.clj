(ns htw.acceptance.steps
  (:require [clojure.string :as str]
            [htw.cave :as cave]
            [htw.game :as game]))

(defn- fail! [message]
  (throw (ex-info message {})))

(defn- parse-int [value]
  (Long/parseLong (str/trim (str value))))

(defn- parse-int-list [value]
  (if (str/blank? (str value))
    []
    (mapv parse-int (str/split (str value) #"\s*,\s*"))))

(defn- parse-warnings [value]
  (if (= "none" (str/trim (str value)))
    []
    (mapv str/trim (str/split (str value) #"\s*,\s*"))))

(defn- outcome-status [value]
  (case (str/trim (str value))
    "lost" :lost
    "in progress" :in-progress))

(defn- parse-wake-choice [value]
  (let [choice (str/trim (str value))]
    (case choice
      "stay" :stay
      (if-let [[_ room] (re-matches #"move to ([0-9]+)" choice)]
        (parse-int room)
        (fail! (str "unsupported Wumpus wake choice: " choice))))))

(defn- assert= [expected actual message]
  (when-not (= expected actual)
    (fail! (str message " expected " (pr-str expected) " but was " (pr-str actual)))))

(defn- assert-message-heard [state message]
  (when-not (some #{message} (:messages state))
    (fail! (str "message not heard: " message))))

(defn- seed-from-start-step [expanded]
  (when-let [[_ seed] (re-matches #"a game is started with seed ([0-9]+)" expanded)]
    (parse-int seed)))

(defn handle! [world text expanded example]
  (case text
    "a new Hunt the Wumpus game specification"
    (swap! world assoc :specification true)

    "the cave topology is inspected"
    (swap! world assoc :topology cave/topology)

    "room <room> has exits <exits>"
    (assert= (parse-int-list (:exits example))
             (cave/exits (parse-int (:room example)))
             "room exits")

    "the cave contains 20 rooms"
    (assert= 20 (count cave/rooms) "room count")

    "every room has 3 exits"
    (when-not (every? #(= 3 (count (cave/exits %))) cave/rooms)
      (fail! "not every room has 3 exits"))

    "every tunnel has a matching return tunnel"
    (when-not (cave/bidirectional?)
      (fail! "not every tunnel is bidirectional"))

    "no room has an exit to itself"
    (when (some cave/self-exiting? cave/rooms)
      (fail! "a room exits to itself"))

    "every room is reachable from room 1"
    (assert= (set cave/rooms) (cave/reachable-from 1) "reachable rooms")

    "a game is started with seed <seed>"
    (swap! world assoc :game (game/start-game (parse-int (:seed example))))

    "a second game is started with seed <seed>"
    (swap! world assoc :second-game (game/start-game (parse-int (:seed example))))

    "the player occupies one room"
    (when-not (cave/room? (:player-room (:game @world)))
      (fail! "player does not occupy one valid room"))

    "the Wumpus occupies one room"
    (when-not (cave/room? (:wumpus-room (:game @world)))
      (fail! "Wumpus does not occupy one valid room"))

    "there are 2 pit rooms"
    (assert= 2 (count (:pit-rooms (:game @world))) "pit room count")

    "there are 2 bat rooms"
    (assert= 2 (count (:bat-rooms (:game @world))) "bat room count")

    "all occupied rooms are distinct"
    (assert= 6 (count (game/occupied-rooms (:game @world))) "occupied room count")

    "every occupied room is numbered from 1 through 20"
    (when-not (every? cave/room? (game/occupied-rooms (:game @world)))
      (fail! "an occupied room is outside 1 through 20"))

    "there are 5 hazard rooms"
    (assert= 5 (count (game/hazard-rooms (:game @world))) "hazard room count")

    "all hazard rooms are distinct"
    (assert= 5 (count (game/hazard-rooms (:game @world))) "distinct hazard count")

    "the player room is not a Wumpus room"
    (let [{:keys [player-room wumpus-room]} (:game @world)]
      (when (= player-room wumpus-room)
        (fail! "player started on Wumpus")))

    "the player room is not a pit room"
    (let [{:keys [player-room pit-rooms]} (:game @world)]
      (when (contains? pit-rooms player-room)
        (fail! "player started in pit")))

    "the player room is not a bat room"
    (let [{:keys [player-room bat-rooms]} (:game @world)]
      (when (contains? bat-rooms player-room)
        (fail! "player started with bats")))

    "a game has the player in room <player_room>"
    (swap! world assoc-in [:custom-game :player-room] (parse-int (:player_room example)))

    "a game has the player in room <start_room>"
    (swap! world assoc-in [:custom-game :player-room] (parse-int (:start_room example)))

    "the Wumpus is in room <wumpus_room>"
    (swap! world assoc-in [:custom-game :wumpus-room] (parse-int (:wumpus_room example)))

    "pits are in rooms <pit_rooms>"
    (swap! world assoc-in [:custom-game :pit-rooms] (set (parse-int-list (:pit_rooms example))))

    "bats are in rooms <bat_rooms>"
    (swap! world assoc-in [:custom-game :bat-rooms] (set (parse-int-list (:bat_rooms example))))

    "bat transport will choose room <transport_room>"
    (swap! world assoc-in [:custom-game :bat-transport-room] (parse-int (:transport_room example)))

    "the Wumpus wake choice is <wake_choice>"
    (swap! world assoc-in [:custom-game :wumpus-wake-choice]
           (parse-wake-choice (:wake_choice example)))

    "adjacent hazards are requested for room <player_room>"
    (swap! world assoc :adjacent-hazards
           (game/adjacent-hazards (:custom-game @world)
                                  (parse-int (:player_room example))))

    "the adjacent Wumpus count is <wumpus_count>"
    (assert= (parse-int (:wumpus_count example))
             (:wumpus (:adjacent-hazards @world))
             "adjacent Wumpus count")

    "the adjacent pit count is <pit_count>"
    (assert= (parse-int (:pit_count example))
             (:pit (:adjacent-hazards @world))
             "adjacent pit count")

    "the adjacent bat count is <bat_count>"
    (assert= (parse-int (:bat_count example))
             (:bat (:adjacent-hazards @world))
             "adjacent bat count")

    "turn warnings are requested"
    (swap! world assoc :warnings (game/turn-warnings (:custom-game @world)))

    "the visible warnings are <warnings>"
    (assert= (parse-warnings (:warnings example))
             (:warnings @world)
             "visible warnings")

    "warning number 1 is <first_warning>"
    (assert= (:first_warning example) (first (:warnings @world)) "first warning")

    "warning number 2 is <second_warning>"
    (assert= (:second_warning example) (second (:warnings @world)) "second warning")

    "warning number 3 is <third_warning>"
    (assert= (:third_warning example) (nth (:warnings @world) 2 nil) "third warning")

    "the player moves to room <destination_room>"
    (swap! world assoc :custom-game
           (game/move-player (:custom-game @world)
                             (parse-int (:destination_room example))))

    "the player tries to move to room <destination_room>"
    (swap! world assoc :custom-game
           (game/try-move-player (:custom-game @world)
                                 (parse-int (:destination_room example))))

    "the player is in room <destination_room>"
    (assert= (parse-int (:destination_room example))
             (:player-room (:custom-game @world))
             "player room")

    "the player is in room <start_room>"
    (assert= (parse-int (:start_room example))
             (:player-room (:custom-game @world))
             "player room")

    "the player is in room <transport_room>"
    (assert= (parse-int (:transport_room example))
             (:player-room (:custom-game @world))
             "transported player room")

    "the game is still in progress"
    (assert= :in-progress (:status (:custom-game @world)) "game status")

    "the game is lost"
    (assert= :lost (:status (:custom-game @world)) "game status")

    "the game is <outcome>"
    (assert= (outcome-status (:outcome example))
             (:status (:custom-game @world))
             "game status")

    "the move is rejected with message <message>"
    (assert= (:message example) (:error (:custom-game @world)) "move rejection")

    "the player hears message <message>"
    (assert-message-heard (:custom-game @world) (:message example))

    "the player hears message <bat_message>"
    (assert-message-heard (:custom-game @world) (:bat_message example))

    "the player hears message <pit_message>"
    (assert-message-heard (:custom-game @world) (:pit_message example))

    "the Wumpus is in room <expected_wumpus_room>"
    (assert= (parse-int (:expected_wumpus_room example))
             (:wumpus-room (:custom-game @world))
             "Wumpus room")

    "Wumpus wake options are requested"
    (swap! world assoc :wake-options (game/wumpus-wake-options (:custom-game @world)))

    "the Wumpus wake options are <wake_options>"
    (assert= (parse-int-list (:wake_options example))
             (:wake-options @world)
             "Wumpus wake options")

    "the Wumpus wakes"
    (swap! world assoc :custom-game (game/wake-wumpus (:custom-game @world)))

    "both games have the same player room"
    (assert= (:player-room (:game @world))
             (:player-room (:second-game @world))
             "player room")

    "both games have the same Wumpus room"
    (assert= (:wumpus-room (:game @world))
             (:wumpus-room (:second-game @world))
             "Wumpus room")

    "both games have the same pit rooms"
    (assert= (:pit-rooms (:game @world))
             (:pit-rooms (:second-game @world))
             "pit rooms")

    "both games have the same bat rooms"
    (assert= (:bat-rooms (:game @world))
             (:bat-rooms (:second-game @world))
             "bat rooms")

    "a game was started with seed 1973"
    (swap! world assoc :game (game/start-game 1973))

    "the same setup is reused"
    (swap! world assoc :reused-game (game/reuse-setup (:game @world)))

    "the reused setup has the same player room"
    (assert= (:player-room (:game @world))
             (:player-room (:reused-game @world))
             "reused player room")

    "the reused setup has the same Wumpus room"
    (assert= (:wumpus-room (:game @world))
             (:wumpus-room (:reused-game @world))
             "reused Wumpus room")

    "the reused setup has the same pit rooms"
    (assert= (:pit-rooms (:game @world))
             (:pit-rooms (:reused-game @world))
             "reused pit rooms")

    "the reused setup has the same bat rooms"
    (assert= (:bat-rooms (:game @world))
             (:bat-rooms (:reused-game @world))
             "reused bat rooms")

    (if-let [seed (seed-from-start-step expanded)]
      (swap! world assoc :game (game/start-game seed))
      (fail! (str "unsupported step: " expanded)))))
