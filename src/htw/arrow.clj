(ns htw.arrow
  (:require [htw.cave :as cave]
            [htw.random :as random]))

(defn- fallback-options [previous-room current-room]
  (let [options (remove (hash-set previous-room) (cave/exits current-room))]
    (or (seq options) (cave/exits current-room))))

(defn- next-room [state previous-room current-room requested-room step-index deviation-used?]
  (if (cave/connected? current-room requested-room)
    {:room requested-room :deviation-used? deviation-used?}
    (if (and (:arrow-deviation-room state) (not deviation-used?))
      {:room (:arrow-deviation-room state) :deviation-used? true}
      {:room (random/choice state
                            [:arrow-fallback step-index previous-room current-room requested-room]
                            (fallback-options previous-room current-room))
       :deviation-used? deviation-used?})))

(defn visits [state path]
  (loop [current-room (:player-room state)
         previous-room nil
         remaining path
         visited []
         step-index 0
         deviation-used? false]
    (if-let [requested-room (first remaining)]
      (let [{:keys [room deviation-used?]}
            (next-room state previous-room current-room requested-room step-index deviation-used?)]
        (recur room current-room (rest remaining) (conj visited room) (inc step-index) deviation-used?))
      visited)))

;; clj-mutate-manifest-begin
;; {:version 1, :tested-at "2026-06-05T15:25:54.633955-05:00", :module-hash "95074093", :forms [{:id "form/0/ns", :kind "ns", :line 1, :end-line 2, :hash "1606095398"} {:id "defn-/fallback-room", :kind "defn-", :line 4, :end-line 6, :hash "-1507227820"} {:id "defn-/next-room", :kind "defn-", :line 8, :end-line 14, :hash "331777666"} {:id "defn/visits", :kind "defn", :line 16, :end-line 26, :hash "-1427565324"}]}
;; clj-mutate-manifest-end
