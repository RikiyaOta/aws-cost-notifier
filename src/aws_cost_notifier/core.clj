(ns aws-cost-notifier.core
  (:require [clojure.edn :as edn]
            [amazonica.aws.costexplorer :as costexplorer]
            [defun.core :refer [defun]]))

; TODO: Use environment variables.
(def aws-credentials {:profile "rikiya-dev" :endpoint "ap-northeast-1"})

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
  {:service service :amount (edn/read-string amount-str)})

(defn- transform-groups [groups] #_{:clj-kondo/ignore [:unresolved-symbol]}
  (map transform-group groups))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn get-cost-and-usage
  []
  (let [{results-by-time :results-by-time} (costexplorer/get-cost-and-usage aws-credentials query-params)
        time-period (get-time-period results-by-time)
        groups (get-groups results-by-time)]
    {:time-period time-period :groups (transform-groups groups)}))

