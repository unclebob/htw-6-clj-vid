(ns htw.acceptance.steps
  (:require [clojure.string :as str]
            [htw.cave :as cave]
            [htw.game :as game]
            [htw.ui :as ui]))

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

(defn- assert= [expected actual message]
  (when-not (= expected actual)
    (fail! (str message " expected " (pr-str expected) " but was " (pr-str actual)))))

(defn- assert-message-heard [state message]
  (when-not (some #{message} (:messages state))
    (fail! (str "message not heard: " message))))

(defn- assert-output-contains [world line label]
  (when-not (some #{line} (:output @world))
    (fail! (str "output missing " label ": " line))))

(defn- seed-from-start-step [expanded]
  (when-let [[_ seed] (re-matches #"a game is started with seed ([0-9]+)" expanded)]
    (parse-int seed)))

(defn handle! [world text expanded example]
  (case text
    "a new Hunt the Wumpus game specification"
    (swap! world assoc :specification true)

    "the terminal game is started with a scripted setup"
    (swap! world assoc :terminal true :custom-game (game/configured-game 1 2 #{} #{}))

    "the terminal game has not started play"
    (swap! world assoc :custom-game (game/start-game 1973) :output [])

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

    "the player is in room <player_room>"
    (swap! world assoc-in [:custom-game :player-room] (parse-int (:player_room example)))

    "the player is in room <start_room>"
    (if (or (:error (:custom-game @world)) (:output @world))
      (assert= (parse-int (:start_room example))
               (:player-room (:custom-game @world))
               "player room")
      (swap! world assoc-in [:custom-game :player-room] (parse-int (:start_room example))))

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
    (swap! world assoc-in [:custom-game :wumpus-wake-choice] (:wake_choice example))

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

    "the player has <starting_arrows> arrows"
    (swap! world assoc-in [:custom-game :arrows] (parse-int (:starting_arrows example)))

    "the player has <arrows> arrows"
    (swap! world assoc-in [:custom-game :arrows] (parse-int (:arrows example)))

    "invalid arrow movement will choose room <deviation_room>"
    (swap! world assoc-in [:custom-game :arrow-deviation-room] (parse-int (:deviation_room example)))

    "the player shoots path <path>"
    (swap! world assoc :custom-game
           (game/shoot-arrow (:custom-game @world)
                             (parse-int-list (:path example))))

    "the player tries to shoot path <path>"
    (swap! world assoc :custom-game
           (game/try-shoot-arrow
             (:custom-game @world)
             (if (= "none" (:path example)) [] (parse-int-list (:path example)))))

    "the arrow visits rooms <visited_rooms>"
    (assert= (parse-int-list (:visited_rooms example))
             (:arrow-visits (:custom-game @world))
             "arrow visits")

    "the player has <remaining_arrows> arrows"
    (assert= (parse-int (:remaining_arrows example))
             (:arrows (:custom-game @world))
             "remaining arrows")

    "the game is won"
    (assert= :won (:status (:custom-game @world)) "game status")

    "the shot is rejected with message <message>"
    (assert= (:message example) (:error (:custom-game @world)) "shot rejection")

    "the player enters command <command>"
    (let [{:keys [state output]} (ui/enter-command (:custom-game @world) (:command example))]
      (swap! world assoc :custom-game state :output output))

    "the next turn shows room <expected_room>"
    (assert= (parse-int (:expected_room example))
             (:player-room (:custom-game @world))
             "next turn room")

    "the next prompt shows room <start_room>"
    (when-not (some #{(str "YOU ARE IN ROOM " (:start_room example))} (:output @world))
      (fail! (str "next prompt did not show room " (:start_room example))))

    "the player still has <arrows> arrows"
    (assert= (parse-int (:arrows example))
             (:arrows (:custom-game @world))
             "arrows")

    "the output contains line <message>"
    (assert-output-contains world (:message example) "line")

    "the output contains line <win_message>"
    (assert-output-contains world (:win_message example) "line")

    "the output contains line <taunt_message>"
    (assert-output-contains world (:taunt_message example) "line")

    "the output contains line <loss_message>"
    (assert-output-contains world (:loss_message example) "line")

    "the output contains line <bat_message>"
    (assert-output-contains world (:bat_message example) "line")

    "the output contains prompt <replay_prompt>"
    (assert-output-contains world (:replay_prompt example) "prompt")

    "the output contains prompt <prompt>"
    (assert-output-contains world (:prompt example) "prompt")

    "the next turn is displayed"
    (let [{:keys [state output]} (ui/display-turn (:custom-game @world))]
      (swap! world assoc :custom-game state :output output))

    "the output contains line <room_line>"
    (assert-output-contains world (:room_line example) "line")

    "the output contains line <tunnel_line>"
    (assert-output-contains world (:tunnel_line example) "line")

    "the output contains line <arrows_line>"
    (assert-output-contains world (:arrows_line example) "line")

    "the output contains warnings <warnings>"
    (let [warnings (parse-warnings (:warnings example))]
      (if (seq warnings)
        (doseq [warning warnings]
          (when-not (some #{warning} (:output @world))
            (fail! (str "output missing warning: " warning))))
        (doseq [warning ["I SMELL A WUMPUS" "BATS NEARBY" "I FEEL A DRAFT"]]
          (when (some #{warning} (:output @world))
            (fail! (str "unexpected warning: " warning))))))

    "the player loses with command <loss_command>"
    (let [{:keys [state output]} (ui/enter-command (:custom-game @world) (:loss_command example))]
      (swap! world assoc :custom-game state :output output))

    "the player answers same setup prompt with <answer>"
    (swap! world assoc :next-game (ui/replay (:custom-game @world) (:answer example)))

    "the next game has player room <player_room>"
    (assert= (parse-int (:player_room example)) (:player-room (:next-game @world)) "next player room")

    "the next game has Wumpus room <wumpus_room>"
    (assert= (parse-int (:wumpus_room example)) (:wumpus-room (:next-game @world)) "next Wumpus room")

    "the next game has pit rooms <pit_rooms>"
    (assert= (set (parse-int-list (:pit_rooms example))) (:pit-rooms (:next-game @world)) "next pit rooms")

    "the next game has bat rooms <bat_rooms>"
    (assert= (set (parse-int-list (:bat_rooms example))) (:bat-rooms (:next-game @world)) "next bat rooms")

    "the next game has a valid placement"
    (when-not (every? (set cave/rooms) (game/occupied-rooms (:next-game @world)))
      (fail! "next game placement is invalid"))

    "the next game is not required to preserve the previous placement"
    true

    "the player answers instructions prompt with <answer>"
    (let [{:keys [state output]} (ui/answer-instructions (:custom-game @world) (:answer example))]
      (swap! world assoc :custom-game state :output output))

    "the output includes instructions text <includes_instructions>"
    (let [expected? (= "true" (:includes_instructions example))
          present? (boolean (some #{"WELCOME TO 'HUNT THE WUMPUS'"} (:output @world)))]
      (assert= expected? present? "instructions presence"))

    "the first turn is displayed"
    (when-not (some #{ui/turn-prompt} (:output @world))
      (fail! "first turn was not displayed"))

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
