(ns htw.cli
  (:require [htw.cli-options :as options]
            [htw.inspection :as inspection]
            [htw.shell :as shell]))

(def instructions-prompt shell/instructions-prompt)

(defn random-seed []
  (shell/random-seed))

(defn launch-game []
  (shell/launch-game))

(defn answer-instructions [launch answer]
  (shell/answer-instructions launch answer))

(defn parse-args [args]
  (options/parse-args args))

(defn inspect-main [& args]
  (apply inspection/inspect-main args))

(defn inspect [& args]
  (apply inspection/inspect args))

(defn -main [& args]
  (apply shell/main args))
