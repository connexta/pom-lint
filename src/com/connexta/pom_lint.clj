(ns com.connexta.pom-lint
  (:require [clojure.pprint :refer [pprint]]
            [clojure.xml :as xml]
            [clojure.set :as set]
            [clojure.string :as str]
            [clojure.java.io :as io]))

(defn xml->depstruct [dep]
  (->> dep
       (map (juxt :tag (comp first :content)))
       (into {})))

(defn mvncoord->depstruct [match]
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

(defn get-deps [data]
  (->> (xml-seq data)
       (filter #(#{:dependency :artifactItem} (:tag %)))
       (map :content)
       (map xml->depstruct)
       set))

(defn get-descriptors [data]
  (->> (xml-seq data)
       (filter #(= :descriptor (:tag %)))
       (map :content)
       first
       (map str/trim)
       (map #(re-matches #"mvn:(.*)\/(.*)\/(.*)\/xml\/features" %))
       (map mvncoord->depstruct)
       set))

(defn get-features [data]
  (->> (xml-seq data)
       (filter #(= :bundle (:tag %)))
       (map :content)
       first
       (map str/trim)
       (map #(re-matches #"mvn:(.*)\/(.*)\/(.*)" %))
       (map mvncoord->depstruct)
       set))

(defn parse-if-exists [path]
  (if (.exists (io/file path))
    (xml/parse path)))

(defn main [project-directory]
  (let [pom-xml (parse-if-exists (.getPath (io/file project-directory "pom.xml")))
        features-xml (parse-if-exists (.getPath (io/file project-directory "src" "main" "resources" "features.xml")))
        root-deps (get-root-deps pom-xml)
        all-deps (reduce into [(get-deps pom-xml)
                               (get-descriptors pom-xml)
                               (get-features features-xml)])]
    (set/difference all-deps root-deps)))

(comment
  (def data (xml/parse (.getPath (clojure.java.io/resource "artifact-items/pom.xml"))))
  (def data (xml/parse (.getPath (clojure.java.io/resource "descriptors/pom.xml"))))
  (def data (xml/parse (.getPath (clojure.java.io/resource "features/src/main/resources/features.xml"))))
  (def data (xml/parse "/opt/git/ddf/pom.xml"))

  (get-root-deps data)
  (get-deps data)
  (get-descriptors data)
  (get-features data)

  (main (.getPath (clojure.java.io/resource "artifact-items")))
  (main (.getPath (clojure.java.io/resource "descriptors")))
  (main (.getPath (clojure.java.io/resource "features"))))
; This will generate the dependency xml to be added.


