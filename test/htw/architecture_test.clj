(ns htw.architecture-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]))

(def source-boundaries
  {"src/htw/game.clj" ["java.util.Random"
                       "java.util.Collections"
                       "slurp"
                       "spit"
                       "System/exit"
                       "htw.acceptance"]})

(defn- file-contains? [path token]
  (str/includes? (slurp path) token))

(deftest game-policy-stays-independent-from-low-level-details
  (doseq [[path forbidden-tokens] source-boundaries
          token forbidden-tokens]
    (testing (str path " must not contain " token)
      (is (not (file-contains? path token))))))
