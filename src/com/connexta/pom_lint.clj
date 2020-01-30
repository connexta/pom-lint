(ns com.connexta.pom-lint
  (:require [clojure.pprint :refer [pprint]]
            [clojure.xml :as xml]
            [clojure.set :as set]))

(defn xml->depstruct [dep]
  (->> dep
       (map (juxt :tag :content))
       (into {})))

(defn get-root-deps [data]
  (->> (:content data)
       (filter #(= :dependencies (:tag %)))
       first
       :content
       (map :content)
       (map xml->depstruct)
       set))

(defn get-all-deps [data]
  (->> (xml-seq data)
       (filter #(#{:dependency :artifactItem} (:tag %)))
       (map :content)
       (map xml->depstruct)
       set))

(defn main [fname]
  (let [data (xml/parse fname)
        root-deps (get-root-deps data)
        all-deps (get-all-deps data)]
    (set/difference all-deps root-deps)))

(comment
  (def data (xml/parse (.getPath (clojure.java.io/resource "artifact-items/pom.xml"))))
  (def data (xml/parse "/opt/git/ddf/pom.xml"))

  (get-root-deps data)
  (get-all-deps data)

  (main (.getPath (clojure.java.io/resource "artifact-items/pom.xml"))))
; This will generate the dependency xml to be added.


