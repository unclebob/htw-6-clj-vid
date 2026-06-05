(ns htw.cli-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is]]
            [htw.cli :as cli]))

(defn- output-lines [& args]
  (str/split-lines (with-out-str (apply cli/-main args))))

(deftest inspect-prints-canonical-topology-and-seeded-setup
  (let [lines (output-lines "--seed" "1973")]
    (is (some #{"CAVE EXITS"} lines))
    (is (some #{"1: 2, 5, 8"} lines))
    (is (some #{"20: 13, 16, 19"} lines))
    (is (some #{"SETUP"} lines))
    (is (some #{"PLAYER: 16"} lines))
    (is (some #{"WUMPUS: 17"} lines))
    (is (some #{"PITS: 3, 5"} lines))
    (is (some #{"BATS: 1, 7"} lines))))

(deftest inspect-prints-reused-setup
  (let [lines (output-lines "--seed" "1973" "--same-setup" "true")
        setup (subvec (vec lines) 22 26)
        reused (subvec (vec lines) 27 31)]
    (is (= "REUSED SETUP" (nth lines 26)))
    (is (= setup reused))))

(deftest inspect-prints-explicit-adjacent-hazards
  (let [lines (output-lines "--player" "1"
                            "--wumpus" "2"
                            "--pits" "3,4"
                            "--bats" "5,6"
                            "--adjacent" "1")]
    (is (some #{"PLAYER: 1"} lines))
    (is (some #{"WUMPUS: 2"} lines))
    (is (some #{"PITS: 3, 4"} lines))
    (is (some #{"BATS: 5, 6"} lines))
    (is (some #{"ADJACENT HAZARDS FOR ROOM: 1"} lines))
    (is (some #{"WUMPUS: 1"} lines))
    (is (some #{"PITS: 0"} lines))
    (is (some #{"BATS: 1"} lines))))

(deftest inspect-defaults-to-a-deterministic-seed
  (let [lines (output-lines)]
    (is (some #{"SETUP"} lines))
    (is (some #{"PLAYER: 16"} lines))))

(deftest parser-rejects-invalid-input
  (is (thrown-with-msg? clojure.lang.ExceptionInfo
                        #"Invalid seed: nope"
                        (#'cli/parse-args ["--seed" "nope"])))
  (is (thrown-with-msg? clojure.lang.ExceptionInfo
                        #"Unknown option: --bogus"
                        (#'cli/parse-args ["--bogus" "1"])))
  (is (thrown-with-msg? clojure.lang.ExceptionInfo
                        #"Options must be supplied"
                        (#'cli/parse-args ["--seed"])))
  (is (thrown-with-msg? clojure.lang.ExceptionInfo
                        #"pits must contain exactly two rooms"
                        (#'cli/parse-args ["--pits" "1,2,3"]))))
