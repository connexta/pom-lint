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

(defn xml->depstruct
  "Converts xml to a dependency structure object. This is a map of the form:
    ```clojure
     {:groupId GROUP_ID
      :artifactId ARTIFACT_ID
      :version VERSION}
    ```"
  [dep]
  (select-keys
    (->> dep
       (map (juxt :tag (comp first :content)))
       (into {}))
    #{:groupId :artifactId :version :type :classifier}))

(defn mvncoord->depstruct
  "Takes a regex match object for maven coordinates and restructures it as a dependency structure
  object. This is a map of the form:
    ```clojure
     {:groupId GROUP_ID
      :artifactId ARTIFACT_ID
      :version VERSION}
    ```"
  [match]
  (let [[_ groupid artifactid version] match]
    {:groupId    groupid
     :artifactId artifactid
     :version    version}))

(defn by-tags
  "Predicate that returns true if the tags provided are found in the xml under test."
  [tags]
  (fn [xml] (tags (:tag xml))))

(defn project-dependency [dep]
  (= (:version dep) "${project.version}"))

(defn get-root-deps
  "Returns a sequence of dependency structure maps representing the root dependencies of a maven
  pom file. These are the dependencies located at the `project->dependencies->dependency` path."
  [data]
  (->> (xml-seq data)
       (filter (by-tags #{:dependencies}))
       (mapcat :content)
       (map :content)
       (map xml->depstruct)
       (map #(merge {:version "${project.version}"} %))
       (filter project-dependency)
       set))

(defn get-deps
  "Returns all dependencies in a pom file, at any depth. This includes those maven dependencies
  defined in `artifactItem` configurations."
  [data]
  (->> (xml-seq data)
       (filter (by-tags #{:artifactItem}))
       (map :content)
       (map xml->depstruct)
       (filter project-dependency)
       set))

(defn get-descriptors
  "Returns sequence of regex match objects of strings representing maven coordinates in a pom file."
  [data]
  (->> (xml-seq data)
       (filter (by-tags #{:descriptor}))
       (mapcat :content)
       (map str/trim)
       (map #(re-matches #"mvn:(.*)\/(.*)\/(.*)\/xml\/features" %))
       (filter some?)
       (map mvncoord->depstruct)
       (filter project-dependency)
       (map #(merge % {:classifier "features" :type "xml"}))
       set))

(defn get-features
  "Returns sequence of regex match objects of strings representing maven coordinates in a feature file."
  [data]
  (->> (xml-seq data)
       (filter (by-tags #{:bundle}))
       (mapcat :content)
       (map str/trim)
       (map #(re-matches #"mvn:(.*)\/(.*)\/(.*)" %))
       (map mvncoord->depstruct)
       (filter project-dependency)
       set))

(defn parse-if-exists
  "Parses an xml file if it exists."
  [& path]
  (let [path (.getPath (apply io/file path))]
    (if (.exists (io/file path))
      (xml/parse path))))

(defn main
  "Main entry point to the library."
  [project-directory]
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
               (str "Found missing dependencies in "
                    project-directory
                    "\n"
                    (with-out-str (pprint missing))))))))

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


