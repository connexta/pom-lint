(ns com.connexta.pom-lint-test
  (:require [com.connexta.pom-lint :as pl]
            [clojure.test :refer [deftest is are]]
            [clojure.java.io :as io]))

(deftest pom-missing-dependency
  (are [file-name missing-deps]
    (= (pl/main (.getPath (io/resource file-name)))
       missing-deps)
    "artifact-items"
    #{{:groupId    "ddf.lib",
       :artifactId "grunt-port-allocator",
       :version    "${project.version}",
       :type       "tar.gz",
       :classifier "npm"}}
    "descriptors"
    #{{:groupId    "ddf.features"
       :artifactId "install-profiles"
       :version    "${project.version}"}}
    "features"
    #{{:artifactId "catalog-security-ingestplugin"
       :groupId    "ddf.catalog.security"
       :version    "${project.version}"}}
    "selects-corrects-dep"
    #{{:groupId    "ddf.features"
       :artifactId "install-profiles"
       :version    "${project.version}"}}))
