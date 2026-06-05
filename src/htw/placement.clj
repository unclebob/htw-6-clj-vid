(ns htw.placement
  (:require [htw.cave :as cave]))

(defn seeded-room-order [seed]
  (let [rooms (java.util.ArrayList. cave/rooms)
        rng (java.util.Random. (long seed))]
    (java.util.Collections/shuffle rooms rng)
    (vec rooms)))
