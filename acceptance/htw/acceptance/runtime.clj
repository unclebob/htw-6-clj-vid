(ns htw.acceptance.runtime
  (:require [cheshire.core :as json]
            [clojure.string :as str]
            [htw.acceptance.steps :as steps]))

(defn- expand-text [text example]
  (reduce-kv
    (fn [expanded k v]
      (str/replace expanded (str "<" (name k) ">") (str v)))
    text
    example))

(defn- scenario-executions [scenario]
  (let [examples (:examples scenario)]
    (if (seq examples)
      (map-indexed (fn [idx example]
                     {:name (str (:name scenario) "/example_" (inc idx))
                      :example example})
                   examples)
      [{:name (str (:name scenario) "/example_1")
        :example {}}])))

(defn- run-step! [world example {:keys [text]}]
  (let [expanded (expand-text text example)]
    (steps/handle! world text expanded example)))

(defn- run-execution! [feature scenario execution]
  (let [world (atom {})]
    (try
      (doseq [step (concat (:background feature) (:steps scenario))]
        (run-step! world (:example execution) step))
      {:status :passed}
      (catch Exception e
        {:status :failed
         :message (str (:name execution) ": " (.getMessage e))}))))

(defn run-feature! [ir-path]
  (let [feature (json/parse-string (slurp ir-path) true)
        results (for [scenario (:scenarios feature)
                      execution (scenario-executions scenario)]
                  (run-execution! feature scenario execution))
        failures (filter #(= :failed (:status %)) results)]
    (doseq [{:keys [message]} failures]
      (binding [*out* *err*]
        (println message)))
    (when (seq failures)
      (System/exit 1))))
