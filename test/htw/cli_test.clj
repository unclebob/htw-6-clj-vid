(ns htw.cli-test
  (:require [clojure.test :refer [deftest is]]
            [htw.cli :as cli]))

(deftest launch-starts-with-instructions-prompt-and-random-seed
  (let [first-launch (cli/launch-game)
        second-launch (cli/launch-game)]
    (is (some #{"INSTRUCTIONS (Y-N)?"} (:output first-launch)))
    (is (integer? (:seed first-launch)))
    (is (not= (:seed first-launch) (:seed second-launch)))))

(deftest answering-instructions-shows-first-turn
  (let [launch (cli/launch-game)
        continued (cli/answer-instructions launch "n")]
    (is (some #{"SHOOT OR MOVE (S-M)?"} (:output continued)))))
