(ns htw.cli
  (:require [htw.cli-options :as options]
            [htw.inspection :as inspection]
            [htw.shell :as shell]))

(def instructions-prompt shell/instructions-prompt)

(defn exit! [status]
  (inspection/exit! status))

(defn random-seed []
  (shell/random-seed))

(defn launch-game []
  (shell/launch-game))

(defn answer-instructions [launch answer]
  (shell/answer-instructions launch answer))

(defn parse-args [args]
  (options/parse-args args))

(defn inspect-main [& args]
  (binding [inspection/*exit!* exit!]
    (apply inspection/inspect-main args)))

(defn inspect [& args]
  (apply inspection/inspect args))

(defn -main [& args]
  (apply shell/main args))

;; clj-mutate-manifest-begin
;; {:version 1, :tested-at "2026-06-06T12:20:16.498612-05:00", :module-hash "-1238236609", :forms [{:id "form/0/ns", :kind "ns", :line 1, :end-line 4, :hash "-1121148030"} {:id "def/instructions-prompt", :kind "def", :line 6, :end-line 6, :hash "305512910"} {:id "defn/exit!", :kind "defn", :line 8, :end-line 9, :hash "-514603491"} {:id "defn/random-seed", :kind "defn", :line 11, :end-line 12, :hash "126149863"} {:id "defn/launch-game", :kind "defn", :line 14, :end-line 15, :hash "187000269"} {:id "defn/answer-instructions", :kind "defn", :line 17, :end-line 18, :hash "2070605604"} {:id "defn/parse-args", :kind "defn", :line 20, :end-line 21, :hash "403071476"} {:id "defn/inspect-main", :kind "defn", :line 23, :end-line 25, :hash "1094984666"} {:id "defn/inspect", :kind "defn", :line 27, :end-line 28, :hash "2068061861"} {:id "defn/-main", :kind "defn", :line 30, :end-line 31, :hash "66010406"}]}
;; clj-mutate-manifest-end
