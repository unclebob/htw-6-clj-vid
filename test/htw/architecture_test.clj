(ns htw.architecture-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]))

(def source-boundaries
  {"src/htw/game.clj" ["java.util.Random"
                       "java.util.Collections"
                       "clojure.string"
                       "slurp"
                       "spit"
                       "System/exit"
                       "htw.acceptance"
                       "htw.ui"
                       "htw.cli"
                       "htw.shell"
                       "htw.inspection"
                       "htw.cli-options"]
   "src/htw/arrow.clj" ["htw.game"
                        "htw.ui"
                        "htw.cli"
                        "htw.shell"
                        "htw.inspection"
                        "htw.cli-options"
                        "htw.acceptance"]
   "src/htw/ui.clj" ["htw.cli"
                     "htw.shell"
                     "htw.inspection"
                     "htw.cli-options"
                     "read-line"
                     "println"]
   "src/htw/cli.clj" ["htw.game"
                      "htw.ui"
                      "htw.cave"
                      "read-line"
                      "println"]
   "src/htw/cli_options.clj" ["read-line"
                              "println"
                              "htw.ui"
                              "htw.shell"
                              "htw.inspection"]
   "src/htw/random.clj" ["read-line"
                         "println"
                         "System/exit"
                         "htw.ui"
                         "htw.cli"
                         "htw.shell"
                         "htw.inspection"
                         "htw.game"
                         "htw.arrow"
                         "htw.acceptance"]})

(defn- file-contains? [path token]
  (str/includes? (slurp path) token))

(deftest game-policy-stays-independent-from-low-level-details
  (doseq [[path forbidden-tokens] source-boundaries
          token forbidden-tokens]
    (testing (str path " must not contain " token)
      (is (not (file-contains? path token))))))
