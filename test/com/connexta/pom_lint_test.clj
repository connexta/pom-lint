(ns com.connexta.pom-lint-test
  (:require [com.connexta.pom-lint :as pl]
            [clojure.test :refer [deftest is]]
            [clojure.java.io :as io]))

(deftest pom-missing-dependency
  (is (= (pl/main (.getPath (io/resource "artifact-items/pom.xml")))
         #{{:groupId    ["ddf.lib"],
            :artifactId ["grunt-port-allocator"],
            :version    ["${project.version}"],
            :type       ["tar.gz"],
            :classifier ["npm"]}})))