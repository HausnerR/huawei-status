(ns huawei-status.utils
  (:require [clojure.contrib.humanize :refer [filesize]]))

(def os-type (cond (.contains (System/getProperty "os.name") "OS X") :osx
                   :else :unknown))

(defn parse-long [s]
  (when-let [digits (re-find  #"\-?\d+" (str s))]
    (try
      (Long. digits)
      (catch Exception e nil))))

(defn parse-filesize [size]
  (filesize size :binary true :format "%.1f "))

(defn find-value [data key]
  (-> (re-pattern (str "<" key ">([^<]*)</" key ">"))
      (re-find data)
      second))