(ns acceptance-mutation-runner
  (:require [cheshire.core :as json]
            [clojure.java.io :as io]
            [clojure.string :as str]))

(defn- generated-tests [generated-dir]
  (->> (file-seq (io/file generated-dir))
       (filter #(.isFile %))
       (map #(.getPath %))
       (filter #(str/ends-with? % "_acceptance_test.clj"))
       sort))

(defn- slurp-stream [stream]
  (slurp stream))

(defn- run-generated-test [feature-json generated-test]
  (let [process (-> (ProcessBuilder. ["bb" generated-test feature-json])
                    (.redirectErrorStream true)
                    .start)
        output (slurp-stream (.getInputStream process))
        exit (.waitFor process)]
    {:exit exit :output output}))

(defn- run-generated-tests [feature-json generated-dir]
  (let [tests (generated-tests generated-dir)]
    (if (seq tests)
      (reduce
        (fn [acc generated-test]
          (let [result (run-generated-test feature-json generated-test)]
            (cond-> (update acc :output str (:output result))
              (not (zero? (:exit result))) (assoc :failed? true))))
        {:failed? false :output ""}
        tests)
      {:failed? true
       :output (str "no generated acceptance tests in " generated-dir)})))

(defn- response [{:keys [id feature_json generated_dir]}]
  (let [started-at (System/nanoTime)
        {:keys [failed? output]} (run-generated-tests feature_json generated_dir)
        duration (quot (- (System/nanoTime) started-at) 1000000)]
    {:id id
     :outcome (if failed? "test_failure" "test_success")
     :output output
     :error ""
     :duration duration}))

(defn- infrastructure-error [id error]
  {:id id
   :outcome "infrastructure_error"
   :output ""
   :error (.getMessage error)
   :duration 0})

(defn- handle-request [line]
  (let [request (json/parse-string line true)]
    (try
      (response request)
      (catch Exception error
        (infrastructure-error (:id request) error)))))

(doseq [line (line-seq (java.io.BufferedReader. *in*))]
  (println (json/generate-string (handle-request line)))
  (flush))
