(ns aws-cost-notifier.core
  (:require [clojure.string :as string]
            [amazonica.aws.costexplorer :as costexplorer]
            [clj-http.client :as client]
            [defun.core :refer [defun]]))

; ---------- Get Data from AWS CostExplorer -------------

; TODO: Use environment variables.

(defn get-aws-profile [] (System/getenv "AWS_PROFILE"))

(def aws-credentials {:profile (get-aws-profile) :endpoint "ap-northeast-1"})

; TODO: Get today dynamically.
(def query-params {:granularity "MONTHLY"
                   :metrics ["NetUnblendedCost"]
                   :time-period {:start "2023-02-01" :end "2023-02-28"}
                   :group-by [{:key "SERVICE" :type "DIMENSION"}]})

(defn- get-groups [results-by-time]
  (:groups (first results-by-time)))

(defn- get-time-period [results-by-time]
  (:time-period (first results-by-time)))

#_{:clj-kondo/ignore [:unresolved-symbol]}
(defun transform-group
  [{:keys [service] :metrics {:NetUnblendedCost {:amount amount-str}}}]
  {:service service :amount amount-str})

(defn- transform-groups [groups] #_{:clj-kondo/ignore [:unresolved-symbol]}
  (map transform-group groups))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn get-cost-and-usage
  []
  (let [{results-by-time :results-by-time} (costexplorer/get-cost-and-usage aws-credentials query-params)
        time-period (get-time-period results-by-time)
        groups (get-groups results-by-time)]
    {:time-period time-period :groups (transform-groups groups)}))

; --------------- Notify via Slack -------------------

(defn get-max-service-length [groups]
  (let [services (map #(:service %1) groups)
        length-list (map count services)]
    (apply max length-list)))

(defn gen-format-string [max-length] (str "%-" (+ max-length 1) "s"))

(defn gen-group-formatter [max-length]
  (fn [group] (str (format (gen-format-string max-length) (:service group)) ": " (:amount group) " USD")))

(defn select-groups [groups]
  (let [f (fn [group] (not= (:amount group) "0"))]
    (filter f groups)))

#_{:clj-kondo/ignore [:unresolved-symbol]}
(defun build-notification-message [{:time-period _ :groups groups}]
  (let [groups (select-groups groups)
        max-length (get-max-service-length groups)
        format-group (gen-group-formatter max-length)
        formatted-groups (map format-group groups)]
    (str "```" (string/join "\n" formatted-groups) "```")))

(defn get-url [] (System/getenv "INCOMING_WEBHOOK_URL"))

(defn post-to-slack [text]
  (client/post (get-url) {:content-type :json :form-params {:text text}}))

; ------------------- Main Logic ---------------------
(defn main []
  (let [result (get-cost-and-usage)
        message #_{:clj-kondo/ignore [:unresolved-symbol]}
        (build-notification-message result)]
    (post-to-slack message)))