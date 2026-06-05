(ns htw.test-runner
  (:require [clojure.test :as test]
            [htw.domain-test]
            [htw.movement-test]
            [htw.shooting-test]
            [htw.ui-test]))

(defn -main [& _]
  (let [{:keys [fail error]} (test/run-tests 'htw.domain-test
                                             'htw.movement-test
                                             'htw.shooting-test
                                             'htw.ui-test)]
    (when (pos? (+ fail error))
      (System/exit 1))))
