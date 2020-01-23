(ns com.connexta.pom-lint
  (:require [clojure.pprint :refer [pprint]]
            [clojure.xml :as xml]
            [clojure.zip :as zip]
            [clojure.data.zip.xml :refer [xml-> xml1->]]))

(defn read-pom [pomfile]
  (zip/xml-zip (xml/parse pomfile)))

(defn build-node-list [pomfile & p]
  (into []
        (for [n (apply xml-> pomfile p)]
          (zip/node n))))

(defn xml->depstruct [dep]
  (->> dep
       (map (juxt :tag :content))
       (into {})))

(defn extract-deps [deplst]
    (->> deplst
         (map :content)
         (map xml->depstruct)
         set))

(defn missing-deps [root-deps plugin-deps]
  (remove root-deps plugin-deps))

(defn miss [p]
  (let [content
        (fn [x]
          (reduce-kv (fn [m k v]
                       (conj m {:tag k :content v}))
                     []
                     x))]
    (xml/emit-element {:tag     :dependency
                       :content (content p)})))


(defn main [fname]
  (let [pomfile (read-pom fname)
        root-dependencies (build-node-list pomfile :dependencies :dependency)
        root-plugin-dependencies (build-node-list pomfile :build :plugins :plugin :dependencies :dependency)
        profile-plugin-dependencies (build-node-list pomfile :profiles :profile :build :plugins :plugin :dependencies :dependency)
        root-art-dependencies (build-node-list pomfile :build :plugins :plugin :executions :execution :configuration :artifactItems :artifactItem)
        profile-art-dependencies (build-node-list pomfile :profiles :profile :build :plugins :plugin :executions :execution :configuration :artifactItems :artifactItem)
        root-deps (extract-deps root-dependencies)
        plugin-deps (concat (extract-deps root-plugin-dependencies)
                            (extract-deps profile-plugin-dependencies)
                            (extract-deps root-art-dependencies)
                            (extract-deps profile-art-dependencies))
        missing (missing-deps root-deps plugin-deps)]
    missing))

(comment
  (main "/opt/projects/cx/pomfix/resources/pom.xml"))
