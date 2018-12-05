(ns huawei-status.api
  (:require [clj-http.client :as client]
            [clojure.java.shell :refer [sh]]

            [huawei-status.utils :refer [os-type]]))

(def params {:socket-timeout 2000
             :conn-timeout 2000
             :retry-handler (constantly false)})

(defn get-router-ip []
  (when (= os-type :osx)
    (->> (:out (sh "route" "-n" "get" "default"))
         (re-find #"gateway\:\s+([0-9]+\.[0-9]+\.[0-9]+\.[0-9])")
         second)))

(defn get-panel-cookies [router-ip]
  (-> (client/get (str "http://" router-ip)
                  params)
      :cookies))

(defn get-session-info [router-ip]
  (-> (client/get (str "http://" router-ip "/api/webserver/SesTokInfo")
                  params)
      :body))

(defn get-device-info [router-ip panel-cookies]
  (-> (client/get (str "http://" router-ip "/api/device/basic_information")
                  (merge {:cookies panel-cookies}
                         params))
      :body))

(defn get-device-status [router-ip panel-cookies]
  (-> (client/get (str "http://" router-ip "/api/monitoring/status")
                  (merge {:cookies panel-cookies}
                         params))
      :body))

(defn get-device-stats [router-ip panel-cookies]
  (-> (client/get (str "http://" router-ip "/api/monitoring/traffic-statistics")
                  (merge {:cookies panel-cookies}
                         params))
      :body))