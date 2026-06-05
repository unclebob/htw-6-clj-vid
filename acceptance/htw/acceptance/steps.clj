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

(defn- assert= [expected actual message]
  (when-not (= expected actual)
    (fail! (str message " expected " (pr-str expected) " but was " (pr-str actual)))))

(defn- one-room? [value]
  (contains? (set cave/rooms) value))

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
    (when (some #(contains? (set (cave/exits %)) %) cave/rooms)
      (fail! "a room exits to itself"))

    "every room is reachable from room 1"
    (assert= (set cave/rooms) (cave/reachable-from 1) "reachable rooms")

    "a game is started with seed <seed>"
    (swap! world assoc :game (game/start-game (parse-int (:seed example))))

    "a second game is started with seed <seed>"
    (swap! world assoc :second-game (game/start-game (parse-int (:seed example))))

    "the player occupies one room"
    (when-not (one-room? (:player-room (:game @world)))
      (fail! "player does not occupy one valid room"))

    "the Wumpus occupies one room"
    (when-not (one-room? (:wumpus-room (:game @world)))
      (fail! "Wumpus does not occupy one valid room"))

    "there are 2 pit rooms"
    (assert= 2 (count (:pit-rooms (:game @world))) "pit room count")

    "there are 2 bat rooms"
    (assert= 2 (count (:bat-rooms (:game @world))) "bat room count")

    "all occupied rooms are distinct"
    (assert= 6 (count (game/occupied-rooms (:game @world))) "occupied room count")

    "every occupied room is numbered from 1 through 20"
    (when-not (every? one-room? (game/occupied-rooms (:game @world)))
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

    "the Wumpus is in room <wumpus_room>"
    (swap! world assoc-in [:custom-game :wumpus-room] (parse-int (:wumpus_room example)))

    "pits are in rooms <pit_rooms>"
    (swap! world assoc-in [:custom-game :pit-rooms] (set (parse-int-list (:pit_rooms example))))

    "bats are in rooms <bat_rooms>"
    (swap! world assoc-in [:custom-game :bat-rooms] (set (parse-int-list (:bat_rooms example))))

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
