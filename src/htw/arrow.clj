(ns htw.arrow
  (:require [htw.cave :as cave]))

(defn- fallback-room [previous-room current-room]
  (or (first (remove (hash-set previous-room) (cave/exits current-room)))
      (first (cave/exits current-room))))

(defn- next-room [state previous-room current-room requested-room deviation-used?]
  (if (cave/connected? current-room requested-room)
    {:room requested-room :deviation-used? deviation-used?}
    (if (and (:arrow-deviation-room state) (not deviation-used?))
      {:room (:arrow-deviation-room state) :deviation-used? true}
      {:room (fallback-room previous-room current-room)
       :deviation-used? deviation-used?})))

(defn visits [state path]
  (loop [current-room (:player-room state)
         previous-room nil
         remaining path
         visited []
         deviation-used? false]
    (if-let [requested-room (first remaining)]
      (let [{:keys [room deviation-used?]}
            (next-room state previous-room current-room requested-room deviation-used?)]
        (recur room current-room (rest remaining) (conj visited room) deviation-used?))
      visited)))

;; clj-mutate-manifest-begin
;; {:version 1, :tested-at "2026-06-06T11:52:51.878659-05:00", :module-hash "95074093", :forms [{:id "form/0/ns", :kind "ns", :line 1, :end-line 2, :hash "1606095398"} {:id "defn-/fallback-room", :kind "defn-", :line 4, :end-line 6, :hash "-1507227820"} {:id "defn-/next-room", :kind "defn-", :line 8, :end-line 14, :hash "331777666"} {:id "defn/visits", :kind "defn", :line 16, :end-line 26, :hash "-1427565324"}]}
;; clj-mutate-manifest-end
