(ns com.connexta.pom-lint
  (:require [clojure.pprint :refer [pprint]]
            [clojure.xml :as xml]
            [clojure.set :as set]
            [clojure.string :as str]
            [clojure.java.io :as io])
  (:import (org.apache.maven.plugin MojoExecutionException))
  (:gen-class
   :name com.connexta.PomLint
   :main false
   :methods [^:static [main [String] void]]))

(defn xml->depstruct [dep]
  (->> dep
       (map (juxt :tag (comp first :content)))
       (into {})))

(defn mvncoord->depstruct [match]
  (let [[_ groupid artifactid version] match]
    {:groupId    groupid
     :artifactId artifactid
     :version    version}))

(defn by-tags [tags]
  (fn [xml] (tags (:tag xml))))

(defn get-root-deps [data]
  (->> (:content data)
       (filter (by-tags #{:dependencies}))
       first
       :content
       (map :content)
       (map xml->depstruct)
       set))

(defn get-deps [data]
  (->> (xml-seq data)
       (filter (by-tags #{:dependency :artifactItem}))
       (map :content)
       (map xml->depstruct)
       set))

(defn get-descriptors [data]
  (->> (xml-seq data)
       (filter (by-tags #{:descriptor}))
       (map :content)
       first
       (map str/trim)
       (map #(re-matches #"mvn:(.*)\/(.*)\/(.*)\/xml\/features" %))
       (map mvncoord->depstruct)
       set))

(defn get-features [data]
  (->> (xml-seq data)
       (filter (by-tags #{:bundle}))
       (map :content)
       first
       (map str/trim)
       (map #(re-matches #"mvn:(.*)\/(.*)\/(.*)" %))
       (map mvncoord->depstruct)
       set))

(defn parse-if-exists [& path]
  (let [path (.getPath (apply io/file path))]
    (if (.exists (io/file path))
      (xml/parse path))))

(defn main [project-directory]
  (let [pom-xml (parse-if-exists project-directory "pom.xml")
        features-xml (parse-if-exists project-directory "src" "main" "resources" "features.xml")
        root-deps (get-root-deps pom-xml)
        all-deps (reduce into [(get-deps pom-xml)
                               (get-descriptors pom-xml)
                               (get-features features-xml)])]
    (set/difference all-deps root-deps)))

(defn -main [project-directory]
  (let [missing (main project-directory)]
    (if-not (empty? missing)
      (throw (MojoExecutionException.
               (str "Found missing dependencies: " (pr-str missing)))))))

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


