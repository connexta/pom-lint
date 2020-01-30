(ns com.connexta.pom-lint
  (:require [clojure.pprint :refer [pprint]]
            [clojure.xml :as xml]
            [clojure.set :as set]
            [clojure.string :as str]))

(defn xml->depstruct [dep]
  (->> dep
       (map (juxt :tag (comp first :content)))
       (into {})))

(defn descriptor->depstruct [match]
  (let [[_ groupid artifactid version] match]
    {:groupId    groupid
     :artifactId artifactid
     :version    version}))

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

(defn get-all-descriptors [data]
  (->> (xml-seq data)
       (filter #(= :descriptor (:tag %)))
       (map :content)
       first
       (map str/trim)
       (map #(re-matches #"mvn:(.*)\/(.*)\/(.*)\/xml\/features" %))
       (map descriptor->depstruct)
       set))

(defn get-all-features [data]
  (->> (xml-seq data)
       (filter #(= :bundle (:tag %)))
       (map :content)
       first
       (map str/trim)
       (map #(re-matches #"mvn:(.*)\/(.*)\/(.*)" %))
       (map descriptor->depstruct)
       set))


(defn main [project-directory]
  (let [pom-xml (xml/parse (str project-directory "/pom.xml"))
        root-deps (get-root-deps pom-xml)
        all-deps (into (get-all-deps pom-xml)
                       (get-all-descriptors pom-xml))]
    (set/difference all-deps root-deps)))

(comment
  (def data (xml/parse (.getPath (clojure.java.io/resource "artifact-items/pom.xml"))))
  (def data (xml/parse (.getPath (clojure.java.io/resource "descriptors/pom.xml"))))
  (def data (xml/parse (.getPath (clojure.java.io/resource "features/src/main/resources/features.xml"))))
  (def data (xml/parse "/opt/git/ddf/pom.xml"))

  (get-root-deps data)
  (get-all-deps data)
  (get-all-descriptors data)
  (get-all-features data)

  (main (.getPath (clojure.java.io/resource "artifact-items")))
  (main (.getPath (clojure.java.io/resource "descriptors"))))
; This will generate the dependency xml to be added.


