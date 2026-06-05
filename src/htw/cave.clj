(ns htw.cave)

(def topology
  {1 [2 5 8]
   2 [1 3 10]
   3 [2 4 12]
   4 [3 5 14]
   5 [1 4 6]
   6 [5 7 15]
   7 [6 8 17]
   8 [1 7 9]
   9 [8 10 18]
   10 [2 9 11]
   11 [10 12 19]
   12 [3 11 13]
   13 [12 14 20]
   14 [4 13 15]
   15 [6 14 16]
   16 [15 17 20]
   17 [7 16 18]
   18 [9 17 19]
   19 [11 18 20]
   20 [13 16 19]})

(def rooms (vec (sort (keys topology))))

(def room-set (set rooms))

(def exit-sets
  (into {}
        (map (fn [[room exits]]
               [room (set exits)]))
        topology))

(defn room? [room]
  (contains? room-set room))

(defn exits [room]
  (get topology room))

(defn connected? [from-room to-room]
  (contains? (get exit-sets from-room #{}) to-room))

(defn self-exiting? [room]
  (connected? room room))

(defn bidirectional? []
  (every?
    (fn [[room exits]]
      (every? #(connected? % room) exits))
    topology))

(defn- expand-reachable [{:keys [seen frontier]}]
  (let [next-seen (into seen frontier)]
    {:seen next-seen
     :frontier (->> frontier
                    (mapcat exits)
                    (remove next-seen)
                    set)}))

(defn reachable-from [start-room]
  (->> {:seen #{} :frontier #{start-room}}
       (iterate expand-reachable)
       (drop-while (comp seq :frontier))
       first
       :seen))
