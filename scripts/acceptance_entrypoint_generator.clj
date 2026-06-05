(ns acceptance-entrypoint-generator
  (:require [cheshire.core :as json]
            [clojure.java.io :as io]
            [clojure.string :as str])
  (:import [java.security MessageDigest]))

(defn- usage! []
  (binding [*out* *err*]
    (println "usage: acceptance-entrypoint-generator <json-ir> <generated-test-output>"))
  (System/exit 2))

(defn- slug [text]
  (let [slugged (-> text
                    str/lower-case
                    (str/replace #"[^a-z0-9]+" "-")
                    (str/replace #"(^-+|-+$)" ""))]
    (if (seq slugged) slugged "feature")))

(defn- sha256 [text]
  (let [digest (.digest (MessageDigest/getInstance "SHA-256")
                        (.getBytes text "UTF-8"))]
    (str "sha256:"
         (apply str (map #(format "%02x" (bit-and % 0xff)) digest)))))

(defn- write-file! [path content]
  (io/make-parents path)
  (spit path content))

(defn- generated-source [feature-name ir-path]
  (str "(ns generated." (slug feature-name) "-acceptance-test\n"
       "  (:require [htw.acceptance.runtime :as runtime]))\n\n"
       "(runtime/run-feature! " (pr-str ir-path) ")\n"))

(defn -main [& args]
  (when-not (= 2 (count args))
    (usage!))
  (let [[ir-path generated-output] args
        ir (json/parse-string (slurp ir-path) true)
        feature-name (:name ir)
        generated-dir (io/file generated-output)
        test-path (io/file generated-dir (str (slug feature-name) "_acceptance_test.clj"))
        source (generated-source feature-name ir-path)
        metadata-path (io/file generated-dir "metadata" (str (slug ir-path) ".json"))
        metadata {:schema_version 1
                  :feature_path (str "features/domain/" (slug feature-name) ".feature")
                  :ir_path ir-path
                  :implementation_hash (sha256 source)
                  :hash_scope "generated_files"
                  :generated_files [(.getPath test-path)]}]
    (write-file! test-path source)
    (write-file! metadata-path (json/generate-string metadata {:pretty true}))))

(apply -main *command-line-args*)
