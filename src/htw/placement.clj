(ns htw.placement
  (:require [htw.cave :as cave]))

(defn seeded-room-order [seed]
  (let [rooms (java.util.ArrayList. cave/rooms)
        rng (java.util.Random. (long seed))]
    (java.util.Collections/shuffle rooms rng)
    (vec rooms)))

;; clj-mutate-manifest-begin
;; {:version 1, :tested-at "2026-06-05T15:05:39.792998-05:00", :module-hash "-628445964", :forms [{:id "form/0/ns", :kind "ns", :line 1, :end-line 2, :hash "-907929487"} {:id "defn/seeded-room-order", :kind "defn", :line 4, :end-line 8, :hash "725131667"}]}
;; clj-mutate-manifest-end
