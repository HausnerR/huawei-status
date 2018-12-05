(ns huawei-status.core
  (:require [clojure.string :as str]
            [clojure.java.io :as io]
            [clojure.java.shell :refer [sh]]

            [huawei-status.utils :refer :all]
            [huawei-status.defs :as defs]
            [huawei-status.api :refer :all])

  (:import [java.awt SystemTray TrayIcon Toolkit PopupMenu MenuItem Menu Image]
           [java.awt.event ActionListener MouseListener]
           [java.awt.image BufferedImage]
           [javax.swing JFrame JOptionPane ImageIcon])

  (:gen-class))


(def tray-icon (atom nil))
(def router-ip (atom nil))
(def panel-cookies (atom nil))


(defn make-tray-icon []
  (let [icon (atom {:disabled true
                    :variant nil})

        tray-icon (when (. SystemTray isSupported)
                    (let [tray (SystemTray/getSystemTray)
                          tray-icon (TrayIcon. (BufferedImage. 1 1 BufferedImage/TYPE_INT_ARGB))]
                      (.setImageAutoSize tray-icon true)
                      (.add tray tray-icon)
                      #_(.addMouseListener tray-icon
                                          (proxy [MouseListener] []
                                            (mousePressed [event]
                                              ;TODO: Refresh data
                                              (fn event))))
                      tray-icon))

        popup-menu (PopupMenu.)
        device-name-item (MenuItem.)
        connection-status-item (MenuItem.)
        battery-status-item (MenuItem.)
        download-item (MenuItem.)
        upload-item (MenuItem.)
        connection-time-item (MenuItem.)
        about-item (MenuItem. "About")
        exit-item (MenuItem. "Exit")]

    ;TODO: Check if tray-icon is not null!

    ;Adjust items
    (.disable device-name-item)
    (.addActionListener about-item
                        (proxy [ActionListener] []
                          (actionPerformed [event]
                            (JOptionPane/showMessageDialog (JFrame.)
                                                           "Huawei Status\nCopyright © 2017 Jakub Pachciarek\nhttps://github.com/HausnerR"
                                                           "About"
                                                           JOptionPane/INFORMATION_MESSAGE
                                                           (-> (.getImage (Toolkit/getDefaultToolkit) (io/resource "icon.png"))
                                                               (.getScaledInstance 64 64 Image/SCALE_SMOOTH)
                                                               ImageIcon.)))))
    (.addActionListener exit-item
                        (proxy [ActionListener] []
                          (actionPerformed [event]
                            (System/exit 0))))

    ;Set tray menu
    (.setPopupMenu tray-icon popup-menu)

    ;Update enabled icon if changed
    (add-watch icon :update-icon
               (fn [_ _ old-icon icon]
                 (when (not= old-icon icon)
                   (let [n (str "icon-" (name (:variant icon)) (when (:disabled icon) "-disabled") ".png")]
                     (.setImage tray-icon (.getImage (Toolkit/getDefaultToolkit) (io/resource n)))))))

    ;Hide unnecessary items if router not found
    (add-watch panel-cookies :show-hide-popup-menu-items
               (fn [_ _ old-c c]
                 (when (not= old-c c)
                   (swap! icon assoc :disabled (not c))
                   (.removeAll popup-menu)
                   (.add popup-menu device-name-item)
                   (when-not c
                     (.setLabel device-name-item "Device not found"))
                   (.addSeparator popup-menu)
                   (when c
                     (.add popup-menu connection-status-item)
                     (.add popup-menu battery-status-item)
                     (.addSeparator popup-menu)
                     (.add popup-menu download-item)
                     (.add popup-menu upload-item)
                     (.add popup-menu connection-time-item)
                     (.addSeparator popup-menu))
                   (.add popup-menu about-item)
                   (.add popup-menu exit-item))))

    ;Handle dark mode on Yosemite and above
    (cond (= os-type :osx)
          (future
            (while true
              (swap! icon assoc :variant (if (= (try
                                                  (:out (sh "defaults" "read" "-g" "AppleInterfaceStyle"))
                                                  (catch Exception e nil)) "Dark\n")
                                           :osx-darkmode
                                           :osx))
              (Thread/sleep 5000)))

          :else
          (swap! icon assoc :variant :normal))

    {:device-name-item device-name-item
     :connection-status-item connection-status-item
     :battery-status-item battery-status-item
     :download-item download-item
     :upload-item upload-item
     :connection-time-item connection-time-item}))


(defn update-panel-cookies []
  (try
    (reset! panel-cookies (when @router-ip (get-panel-cookies @router-ip)))

    (catch Exception e
      (reset! panel-cookies nil)
      (println "Failed to get panel cookies:" (.getMessage e))
      nil)))


(defn refresh-device-name []
  (try
    (when @panel-cookies
      (let [device-info (get-device-info @router-ip @panel-cookies)]
        (.setLabel (:device-name-item @tray-icon)
                   (str "Huawei " (find-value device-info "devicename")))))

    (catch Exception e
      (println "Failed to refresh device name: " (.getMessage e))
      nil)))


(defn process-device-status [raw-device-status]
  (let [data {:connection-status (parse-long (find-value raw-device-status "ConnectionStatus"))
              :signal-strength (parse-long (find-value raw-device-status "SignalIcon")) ;1-4
              :network-type (parse-long (find-value raw-device-status "CurrentNetworkType"))
              :battery-status (parse-long (find-value raw-device-status "BatteryStatus"))
              :battery-level (parse-long (find-value raw-device-status "BatteryLevel")) ;1-4
              :battery-percent (parse-long (find-value raw-device-status "BatteryPercent"))}]

    (assoc data
      :connection-status-desc (get defs/connection-status (:connection-status data))
      :network-type-desc (get defs/network-type (:network-type data))
      :battery-status-desc (get defs/battery-status (:battery-status data)))))

(defn refresh-device-status []
  (try
    (when @panel-cookies
      (when-let [raw-device-status (get-device-status @router-ip @panel-cookies)]
        (let [device-status (process-device-status raw-device-status)]

          (.setLabel (:connection-status-item @tray-icon)
                     (str (:connection-status-desc device-status)
                          ": "
                          (apply str (concat
                                       (repeat (:signal-strength device-status) "⦿")
                                       (repeat (- 4 (:signal-strength device-status)) "⊙")))
                          " "
                          (:network-type-desc device-status)))

          (.setLabel (:battery-status-item @tray-icon)
                     (cond (= (:battery-status device-status) 2)
                           "No battery"

                           (= (:battery-status device-status) -1)
                           "Low battery!"

                           :else
                           (str  "Battery: "
                                 (apply str (concat
                                              (repeat (:battery-level device-status) "⦿")
                                              (repeat (- 4 (:battery-level device-status)) "⊙")))
                                 (when (= (:battery-status device-status) 1)
                                   " ⌁")))))))

    (catch Exception e
      (println "Failed to refresh device status data: " (.getMessage e))
      nil)))


(defn refresh-device-stats []
  (try
    (when @panel-cookies
      (when-let [device-stats (get-device-stats @router-ip @panel-cookies)]
        (let [value-as-filesize #(parse-filesize (parse-long (find-value device-stats %)))

              left-seconds (parse-long (find-value device-stats "CurrentConnectTime"))
              days (int (/ left-seconds 86400))
              left-seconds (mod left-seconds 86400)
              hours (int (/ left-seconds 3600))
              left-seconds (mod left-seconds 3600)
              minutes (int (/ left-seconds 60))
              seconds (mod left-seconds 60)]

          (.setLabel (:download-item @tray-icon)
                     (str "DL: " (value-as-filesize "CurrentDownloadRate") " / " (value-as-filesize "CurrentDownload")))

          (.setLabel (:upload-item @tray-icon)
                     (str "UL: " (value-as-filesize "CurrentUploadRate") " / " (value-as-filesize "CurrentUpload")))

          (.setLabel (:connection-time-item @tray-icon)
                     (format "Time: %d days, %02d:%02d:%02d", days, hours, minutes, seconds)))))

    (catch Exception e
      (println "Failed to refresh device stats data: " (.getMessage e))
      nil)))



(defn -main [& args]
  (reset! tray-icon (make-tray-icon))

  (future
    (while true
      (let [new-router-ip (get-router-ip)]
        (when-not (= @router-ip new-router-ip)
          (reset! router-ip new-router-ip)

          (update-panel-cookies)

          (refresh-device-name)
          (refresh-device-status)
          (refresh-device-stats)))
      (Thread/sleep 10000)))

  (future
    (while true
      (Thread/sleep 5000)
      (refresh-device-status)))

  (future
    (while true
      (Thread/sleep 2000)
      (refresh-device-stats))))
