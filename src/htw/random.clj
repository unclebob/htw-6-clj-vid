(ns htw.random)

(defn- mixed-seed [seed salt]
  (long (hash [seed salt])))

(defn choice [state salt options]
  (let [choices (vec options)]
    (when (seq choices)
      (if-let [seed (:seed state)]
        (let [rng (java.util.Random. (mixed-seed seed salt))]
          (nth choices (.nextInt rng (count choices))))
        (first choices)))))
