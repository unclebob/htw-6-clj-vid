(ns htw.ui
  (:require [clojure.string :as str]
            [htw.cave :as cave]
            [htw.game :as game]))

(def win-taunt "HEE HEE HEE - THE WUMPUS'LL GETCHA NEXT TIME!!")
(def loss-taunt "HA HA HA - YOU LOSE!")
(def replay-prompt "SAME SET UP (Y-N)?")
(def turn-prompt "SHOOT OR MOVE (S-M)?")

(def instructions-lines
  ["WELCOME TO 'HUNT THE WUMPUS'"
   "THE WUMPUS LIVES IN A CAVE OF 20 ROOMS: EACH ROOM HAS 3 TUNNELS LEADING TO OTHER ROOMS."
   "BOTTOMLESS PITS - TWO ROOMS HAVE BOTTOMLESS PITS IN THEM"
   "SUPER BATS - TWO OTHER ROOMS HAVE SUPER BATS"
   "THE WUMPUS IS NOT BOTHERED BY THE HAZARDS"
   "ARROWS: YOU HAVE 5 ARROWS"
   "WARNINGS: I SMELL A WUMPUS, BATS NEARBY, I FEEL A DRAFT"])

(defn- parse-int [value]
  (Long/parseLong value))

(defn- tokens [command]
  (str/split (str/trim command) #"\s+"))

(defn display-turn [state]
  (let [state (if (:arrows state) state (assoc state :arrows 5))
        exits (str/join " " (cave/exits (:player-room state)))
        output (into [(str "YOU ARE IN ROOM " (:player-room state))
                      (str "TUNNELS LEAD TO " exits)]
                     (game/turn-warnings state))]
    {:state state
     :output (conj (vec output)
                   (str "ARROWS LEFT: " (:arrows state))
                   turn-prompt)}))

(defn- terminal-output [state]
  (cond
    (= :won (:status state))
    (conj (:messages state) win-taunt)

    (= :lost (:status state))
    (conj (:messages state) loss-taunt replay-prompt)

    :else
    (:output (display-turn state))))

(defn- observation-output [state]
  (cond-> [(str "PLAYER: " (:player-room state))
           (str "WUMPUS: " (:wumpus-room state))]
    (:arrow-visits state)
    (conj (str "ARROW PATH: " (str/join ", " (:arrow-visits state))))))

(defn- result [state extra-output]
  {:state state
   :output (vec (concat extra-output (observation-output state) (terminal-output state)))})

(defn- invalid-result [state message]
  {:state state
   :output (vec (concat [message] (:output (display-turn state))))})

(defn- move-command [state args]
  (if (= 1 (count args))
    (let [moved (game/try-move-player state (parse-int (first args)))]
      (if (:error moved)
        (invalid-result state (:error moved))
        (result moved [])))
    (invalid-result state "CAN'T MOVE THERE")))

(defn- shoot-command [state args]
  (let [path (mapv parse-int args)
        shot (game/try-shoot-arrow state path)]
    (if (:error shot)
      (invalid-result state (:error shot))
      (result shot []))))

(defn- remember-setup [state]
  (if (:setup state)
    state
    (assoc state :setup (select-keys state [:player-room :wumpus-room :pit-rooms :bat-rooms]))))

(defn enter-command [state command]
  (let [state (remember-setup state)
        [verb & args] (tokens command)
        verb (str/lower-case (or verb ""))]
    (case verb
      "m" (move-command state args)
      "s" (shoot-command state args)
      (invalid-result state (str (str/upper-case verb) " IS NOT A COMMAND")))))

(defn replay [state answer]
  (if (= "y" (str/lower-case (str/trim answer)))
    (-> (:setup state)
        (assoc :status :in-progress
               :messages []
               :arrows 5))
    (game/start-game 1974)))

(defn answer-instructions [state answer]
  (let [base-output (if (= "y" (str/lower-case (str/trim answer)))
                      instructions-lines
                      [])]
    (update (display-turn state) :output #(vec (concat base-output %)))))

;; clj-mutate-manifest-begin
;; {:version 1, :tested-at "2026-06-06T11:58:35.254802-05:00", :module-hash "-28101062", :forms [{:id "form/0/ns", :kind "ns", :line 1, :end-line 4, :hash "-861550946"} {:id "def/win-taunt", :kind "def", :line 6, :end-line 6, :hash "-1671476535"} {:id "def/loss-taunt", :kind "def", :line 7, :end-line 7, :hash "-659904103"} {:id "def/replay-prompt", :kind "def", :line 8, :end-line 8, :hash "918211849"} {:id "def/turn-prompt", :kind "def", :line 9, :end-line 9, :hash "-1696928107"} {:id "def/instructions-lines", :kind "def", :line 11, :end-line 18, :hash "-1516326404"} {:id "defn-/parse-int", :kind "defn-", :line 20, :end-line 21, :hash "32822557"} {:id "defn-/tokens", :kind "defn-", :line 23, :end-line 24, :hash "-781448191"} {:id "defn/display-turn", :kind "defn", :line 26, :end-line 35, :hash "-63384933"} {:id "defn-/terminal-output", :kind "defn-", :line 37, :end-line 46, :hash "1791637227"} {:id "defn-/observation-output", :kind "defn-", :line 48, :end-line 52, :hash "428437103"} {:id "defn-/result", :kind "defn-", :line 54, :end-line 56, :hash "-641323567"} {:id "defn-/invalid-result", :kind "defn-", :line 58, :end-line 60, :hash "347306135"} {:id "defn-/move-command", :kind "defn-", :line 62, :end-line 68, :hash "-1844884371"} {:id "defn-/shoot-command", :kind "defn-", :line 70, :end-line 75, :hash "2064210255"} {:id "defn-/remember-setup", :kind "defn-", :line 77, :end-line 80, :hash "-400379457"} {:id "defn/enter-command", :kind "defn", :line 82, :end-line 89, :hash "-706680405"} {:id "defn/replay", :kind "defn", :line 91, :end-line 97, :hash "1196211499"} {:id "defn/answer-instructions", :kind "defn", :line 99, :end-line 103, :hash "108555046"}]}
;; clj-mutate-manifest-end
