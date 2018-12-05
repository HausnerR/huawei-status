(ns huawei-status.defs)

(def connection-status {201 "Connection interrupted (data limit reached)"
                        900 "Connecting"
                        901 "Connected"
                        902 "Disconnected"
                        903 "Disconnecting"
                        904 "Connecting failed"
                        905 "No connection (low signal)"
                        906 "Connection error"})

(def network-type {0 "No service"
                   1 "GSM"
                   2 "GPRS"
                   3 "EDGE"
                   4 "WCDMA"
                   5 "HSDPA"
                   6 "HSUPA"
                   7 "HSPA"
                   8 "TDSCDMA"
                   9 "HSPA+"
                   10 "EVDO rev. 0"
                   11 "EVDO rev. A"
                   12 "EVDO rev. B"
                   13 "1xRTT"
                   14 "UMB"
                   15 "1xEVDV"
                   16 "3xRTT"
                   17 "HSPA+64QAM"
                   18 "HSPA+MIMO"
                   19 "LTE"
                   21 "IS95A"
                   22 "IS95B"
                   23 "CDMA1x"
                   24 "EVDO rev. 0"
                   25 "EVDO rev. A"
                   26 "EVDO rev. B"
                   27 "Hybrid CDMA1x"
                   28 "Hybrid EVDO rev. 0"
                   29 "Hybrid EVDO rev. A"
                   30 "Hybrid EVDO rev. B"
                   31 "EHRPD rev. 0"
                   32 "EHRPD rev. A"
                   33 "EHRPD rev. B"
                   34 "Hybrid EHRPD rev. 0"
                   35 "Hybrid EHRPD rev. A"
                   36 "Hybrid EHRPD rev. B"
                   41 "WCDMA"
                   42 "HSDPA"
                   43 "HSUPA"
                   44 "HSPA"
                   45 "HSPA+"
                   46 "DC HSPA+"
                   61 "TD SCDMA"
                   62 "TD HSDPA"
                   63 "TD HSUPA"
                   64 "TD HSPA"
                   65 "TD HSPA+"
                   81 "802.16E"
                   101 "LTE"})

(def battery-status {0 ""
                     1 "Charging"
                     -1 "Low battery"
                     2 "No battery"})