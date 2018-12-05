(defproject huawei-status "0.1.0-SNAPSHOT"
  :description ""
  :url ""
  :license {:name "MIT"}

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [clj-http "3.7.0"]
                 [clojure-humanize "0.2.2"]]

  :main ^:skip-aot huawei-status.core

  :uberjar-name "../Huawei Status.app/Contents/Java/huawei-status.jar"

  :profiles {:uberjar {:aot         :all
                       :omit-source true}})
