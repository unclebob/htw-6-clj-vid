(ns htw.cli-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is]]
            [htw.cli :as cli]))

(defn- inspection-output-lines [& args]
  (str/split-lines (with-out-str (apply cli/inspect args))))

(defn- output-lines [& args]
  (apply inspection-output-lines args))

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

(deftest inspect-prints-canonical-topology-and-seeded-setup
  (let [lines (inspection-output-lines "--seed" "1973")]
    (is (some #{"CAVE EXITS"} lines))
    (is (some #{"1: 2, 5, 8"} lines))
    (is (some #{"20: 13, 16, 19"} lines))
    (is (some #{"SETUP"} lines))
    (is (some #{"PLAYER: 16"} lines))
    (is (some #{"WUMPUS: 17"} lines))
    (is (some #{"PITS: 3, 5"} lines))
    (is (some #{"BATS: 1, 7"} lines))))

(deftest inspect-prints-reused-setup
  (let [lines (inspection-output-lines "--seed" "1973" "--same-setup" "true")
        setup (subvec (vec lines) 22 26)
        reused (subvec (vec lines) 27 31)]
    (is (= "REUSED SETUP" (nth lines 26)))
    (is (= setup reused))))

(deftest inspect-prints-explicit-adjacent-hazards
  (let [lines (inspection-output-lines "--player" "1"
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
  (let [lines (inspection-output-lines)]
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

(deftest scripted-move-command-prints-visible-state
  (let [lines (output-lines "--player" "1"
                            "--wumpus" "13"
                            "--pits" "14,15"
                            "--bats" "16,17"
                            "--commands" "m 2")]
    (is (some #{"COMMAND: m 2"} lines))
    (is (some #{"PLAYER: 2"} lines))
    (is (some #{"STATUS: IN-PROGRESS"} lines))))

(deftest scripted-shoot-command-prints-arrow-diagnostics
  (let [lines (output-lines "--player" "1"
                            "--wumpus" "13"
                            "--pits" "14,15"
                            "--bats" "16,17"
                            "--arrow-deviation" "5"
                            "--wumpus-wake" "stay"
                            "--commands" "s 3 4")]
    (is (some #{"ARROW PATH: 5, 4"} lines))
    (is (some #{"ARROWS: 4"} lines))
    (is (some #{"STATUS: IN-PROGRESS"} lines))))

(deftest scripted-command-options-control-random-outcomes
  (let [bat-lines (output-lines "--player" "1"
                                "--wumpus" "13"
                                "--pits" "14,15"
                                "--bats" "2,17"
                                "--bat-transport" "10"
                                "--commands" "m 2")
        wake-lines (output-lines "--player" "1"
                                 "--wumpus" "10"
                                 "--pits" "14,15"
                                 "--bats" "16,17"
                                 "--arrows" "5"
                                 "--wumpus-wake" "1"
                                 "--commands" "s 5")]
    (is (some #{"ZAP -- SUPER BAT SNATCH! ELSEWHEREVILLE FOR YOU!"} bat-lines))
    (is (some #{"PLAYER: 10"} bat-lines))
    (is (some #{"STATUS: LOST"} wake-lines))))

(deftest scripted-invalid-commands-print-errors-without-advancing
  (let [lines (output-lines "--player" "1"
                            "--wumpus" "10"
                            "--pits" "14,15"
                            "--bats" "16,17"
                            "--arrows" "5"
                            "--commands" "m 3;s;x")]
    (is (some #{"CAN'T MOVE THERE"} lines))
    (is (some #{"CAN'T SHOOT THERE"} lines))
    (is (some #{"X IS NOT A COMMAND"} lines))
    (is (some #{"PLAYER: 1"} lines))
    (is (some #{"ARROWS: 5"} lines))))
