(ns htw.property-test-runner
  (:require [clojure.test :as test]
            [htw.property-test]))

(defn -main [& _]
  (let [{:keys [fail error]} (test/run-tests 'htw.property-test)]
    (when (pos? (+ fail error))
      (System/exit 1))))
