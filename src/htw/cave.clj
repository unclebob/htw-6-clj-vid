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

;; clj-mutate-manifest-begin
;; {:version 1, :tested-at "2026-06-06T11:40:30.252041-05:00", :module-hash "1780202113", :forms [{:id "form/0/ns", :kind "ns", :line 1, :end-line 1, :hash "1642982847"} {:id "def/topology", :kind "def", :line 3, :end-line 23, :hash "-1342340074"} {:id "def/rooms", :kind "def", :line 25, :end-line 25, :hash "-1936942802"} {:id "def/room-set", :kind "def", :line 27, :end-line 27, :hash "1138383904"} {:id "def/exit-sets", :kind "def", :line 29, :end-line 33, :hash "-2004503629"} {:id "defn/room?", :kind "defn", :line 35, :end-line 36, :hash "1304361587"} {:id "defn/exits", :kind "defn", :line 38, :end-line 39, :hash "644973506"} {:id "defn/connected?", :kind "defn", :line 41, :end-line 42, :hash "736091314"} {:id "defn/self-exiting?", :kind "defn", :line 44, :end-line 45, :hash "438638884"} {:id "defn/bidirectional?", :kind "defn", :line 47, :end-line 51, :hash "-388166467"} {:id "defn-/expand-reachable", :kind "defn-", :line 53, :end-line 59, :hash "-1831542199"} {:id "defn/reachable-from", :kind "defn", :line 61, :end-line 66, :hash "-81612289"}]}
;; clj-mutate-manifest-end
