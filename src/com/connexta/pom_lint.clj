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

(defn extract-deps [deplst]
  (let [baz (fn [dep]
              (->> dep
                   (map #(map % [:tag :content]))
                   (map vec)
                   vec
                   (into {})))]
    (->> deplst
         (map :content)
         (map baz)
         set
         )))

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


(def pomfile (read-pom "/opt/projects/cx/pomfix/resources/pom.xml"))
(def root-dependencies (build-node-list pomfile :dependencies :dependency))
(def root-plugin-dependencies (build-node-list pomfile :build :plugins :plugin :dependencies :dependency))
(def profile-plugin-dependencies (build-node-list pomfile :profiles :profile :build :plugins :plugin :dependencies :dependency))

(def root-deps (extract-deps root-dependencies))
(def plugin-deps (concat (extract-deps root-plugin-dependencies) (extract-deps profile-plugin-dependencies)))

(def missing (missing-deps root-deps plugin-deps))


; (map miss missing)
