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
  [:seed :arrows :bat-transport-room :wumpus-wake-choice :arrow-deviation-room])

(defn configured-state [options]
  (merge (setup-from options) (select-keys options state-options)))

;; clj-mutate-manifest-begin
;; {:version 1, :tested-at "2026-06-06T11:54:15.557989-05:00", :module-hash "-1732907862", :forms [{:id "form/0/ns", :kind "ns", :line 1, :end-line 3, :hash "2013944622"} {:id "defn/parse-int", :kind "defn", :line 5, :end-line 9, :hash "-489437070"} {:id "defn-/parse-room-list", :kind "defn-", :line 11, :end-line 15, :hash "-97128823"} {:id "defn-/parse-wake-choice", :kind "defn-", :line 17, :end-line 20, :hash "-1858852301"} {:id "def/option-parsers", :kind "def", :line 22, :end-line 35, :hash "-1081062010"} {:id "defn-/parse-option", :kind "defn-", :line 37, :end-line 41, :hash "-2129025786"} {:id "defn/parse-args", :kind "defn", :line 43, :end-line 46, :hash "-2093151120"} {:id "defn/explicit-setup?", :kind "defn", :line 48, :end-line 49, :hash "-12698034"} {:id "defn-/setup-from", :kind "defn-", :line 51, :end-line 63, :hash "111081715"} {:id "def/state-options", :kind "def", :line 65, :end-line 66, :hash "-369228174"} {:id "defn/configured-state", :kind "defn", :line 68, :end-line 69, :hash "1589847890"}]}
;; clj-mutate-manifest-end
