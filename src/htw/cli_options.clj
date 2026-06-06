(ns htw.cli-options
  (:require [clojure.string :as str]
            [htw.game :as game]))

(defn parse-int [value label]
  (let [trimmed (str/trim value)]
    (when-not (re-matches #"\d+" trimmed)
      (throw (ex-info (str "Invalid " label ": " value) {})))
    (Long/parseLong trimmed)))

(defn- parse-room-list [value label]
  (let [rooms (mapv #(parse-int % label) (str/split value #","))]
    (when-not (= 2 (count rooms))
      (throw (ex-info (str label " must contain exactly two rooms") {})))
    (set rooms)))

(defn- parse-wake-choice [value]
  (if (= "stay" (str/lower-case (str/trim value)))
    :stay
    (parse-int value "Wumpus wake choice")))

(def option-parsers
  {"--seed" [:seed #(parse-int % "seed")]
   "--same-setup" [:same-setup (constantly true)]
   "--player" [:player-room #(parse-int % "player room")]
   "--wumpus" [:wumpus-room #(parse-int % "Wumpus room")]
   "--pits" [:pit-rooms #(parse-room-list % "pits")]
   "--bats" [:bat-rooms #(parse-room-list % "bats")]
   "--adjacent" [:adjacent-room #(parse-int % "adjacent room")]
   "--bat-transport" [:bat-transport-room #(parse-int % "bat transport room")]
   "--wumpus-wake" [:wumpus-wake-choice parse-wake-choice]
   "--arrow-deviation" [:arrow-deviation-room #(parse-int % "arrow deviation room")]
   "--arrows" [:arrows #(parse-int % "arrows")]
   "--show-seed" [:show-seed (constantly true)]
   "--commands" [:commands identity]})

(defn- parse-option [[option value]]
  (let [[parsed-key parser] (get option-parsers option)]
    (when-not parser
      (throw (ex-info (str "Unknown option: " option) {})))
    [parsed-key (parser value)]))

(defn parse-args [args]
  (when (odd? (count args))
    (throw (ex-info "Options must be supplied as --name value pairs" {})))
  (into {} (map parse-option) (partition 2 args)))

(defn explicit-setup? [options]
  (every? options [:player-room :wumpus-room :pit-rooms :bat-rooms]))

(defn- setup-from [options]
  (cond
    (explicit-setup? options)
    (game/configured-game (:player-room options)
                          (:wumpus-room options)
                          (:pit-rooms options)
                          (:bat-rooms options))

    (:seed options)
    (game/start-game (:seed options))

    :else
    (game/start-game 1973)))

(def state-options
  [:arrows :bat-transport-room :wumpus-wake-choice :arrow-deviation-room])

(defn configured-state [options]
  (merge (setup-from options) (select-keys options state-options)))
