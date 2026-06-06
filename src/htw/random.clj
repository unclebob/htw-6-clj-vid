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

;; clj-mutate-manifest-begin
;; {:version 1, :tested-at "2026-06-06T12:21:47.913503-05:00", :module-hash "-1891109879", :forms [{:id "form/0/ns", :kind "ns", :line 1, :end-line 1, :hash "-768878048"} {:id "defn-/mixed-seed", :kind "defn-", :line 3, :end-line 4, :hash "1633967994"} {:id "defn/choice", :kind "defn", :line 6, :end-line 12, :hash "1930368273"}]}
;; clj-mutate-manifest-end
