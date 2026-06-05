(ns htw.cli
  (:require [clojure.string :as str]
            [htw.cave :as cave]
            [htw.game :as game]))

(defn- parse-int [value label]
  (let [trimmed (str/trim value)]
    (when-not (re-matches #"\d+" trimmed)
      (throw (ex-info (str "Invalid " label ": " value) {})))
    (Long/parseLong trimmed)))

(defn- parse-room-list [value label]
  (let [rooms (mapv #(parse-int % label) (str/split value #","))]
    (when-not (= 2 (count rooms))
      (throw (ex-info (str label " must contain exactly two rooms") {})))
    (set rooms)))

(def option-parsers
  {"--seed" [:seed #(parse-int % "seed")]
   "--same-setup" [:same-setup (constantly true)]
   "--player" [:player-room #(parse-int % "player room")]
   "--wumpus" [:wumpus-room #(parse-int % "Wumpus room")]
   "--pits" [:pit-rooms #(parse-room-list % "pits")]
   "--bats" [:bat-rooms #(parse-room-list % "bats")]
   "--adjacent" [:adjacent-room #(parse-int % "adjacent room")]})

(defn- parse-option [[option value]]
  (let [[parsed-key parser] (get option-parsers option)]
    (when-not parser
      (throw (ex-info (str "Unknown option: " option) {})))
    [parsed-key (parser value)]))

(defn- parse-args [args]
  (when (odd? (count args))
    (throw (ex-info "Options must be supplied as --name value pairs" {})))
  (into {} (map parse-option) (partition 2 args)))

(defn- explicit-setup? [options]
  (every? options [:player-room :wumpus-room :pit-rooms :bat-rooms]))

(defn- setup-from [options]
  (cond
    (explicit-setup? options)
    (select-keys options [:player-room :wumpus-room :pit-rooms :bat-rooms])

    (:seed options)
    (game/start-game (:seed options))

    :else
    (game/start-game 1973)))

(defn- room-line [room]
  (str room ": " (str/join ", " (cave/exits room))))

(defn- print-topology! []
  (println "CAVE EXITS")
  (doseq [room cave/rooms]
    (println (room-line room))))

(defn- sorted-list [rooms]
  (str/join ", " (sort rooms)))

(defn- print-setup! [heading state]
  (println heading)
  (println "PLAYER:" (:player-room state))
  (println "WUMPUS:" (:wumpus-room state))
  (println "PITS:" (sorted-list (:pit-rooms state)))
  (println "BATS:" (sorted-list (:bat-rooms state))))

(defn- print-adjacent-hazards! [state room]
  (let [{:keys [wumpus pit bat]} (game/adjacent-hazards state room)]
    (println "ADJACENT HAZARDS FOR ROOM:" room)
    (println "WUMPUS:" wumpus)
    (println "PITS:" pit)
    (println "BATS:" bat)))

(defn -main [& args]
  (try
    (let [options (parse-args args)
          state (setup-from options)]
      (print-topology!)
      (print-setup! "SETUP" state)
      (when (:same-setup options)
        (print-setup! "REUSED SETUP" (game/reuse-setup state)))
      (when-let [room (:adjacent-room options)]
        (print-adjacent-hazards! state room)))
    (catch Exception e
      (binding [*out* *err*]
        (println (.getMessage e)))
      (System/exit 1))))
