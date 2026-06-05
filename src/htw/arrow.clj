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
