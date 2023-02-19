(ns aws-cost-notifier.aws-cost-explorer
  (:require [amazonica.aws.costexplorer :as costexplorer]))

(defn- get-cred []
  {:profile (System/getenv "AWS_PROFILE") :endpoint "ap-northeast-1"})

(defn- base-cost-query
  [period-start period-end]
  {:granularity "MONTHLY" :metrics ["NetUnblendedCost"] :time-period {:start period-start :end period-end}})

(defn- to-jpy [usd rate-jpy-usd] (* usd rate-jpy-usd))

(defn- to-service-amount [group rate-jpy-usd]
  (let [{services :keys metrics :metrics} group
        service (first services)
        amount-usd-str (get-in metrics [:NetUnblendedCost :amount])
        amount-usd (Double/parseDouble amount-usd-str)
        amount-jpy (to-jpy amount-usd rate-jpy-usd)]
    {:service service :amount amount-jpy}))

(defn- is-amount-zero [service-amount]
  (= (:amount service-amount) 0.0))

(defn- get-total-usd
  [period-start period-end]
  (let [query (base-cost-query period-start period-end)
        cred (get-cred)
        {results-by-time :results-by-time} (costexplorer/get-cost-and-usage cred query)
        total (:total (first results-by-time))
        {total-amount-str :amount} (:NetUnblendedCost total)]
    (Double/parseDouble total-amount-str)))

(defn get-total-jpy
  [period-start period-end rate-jpy-usd]
  (let [total-usd (get-total-usd period-start period-end)]
    (to-jpy total-usd rate-jpy-usd)))

(defn get-service-amount-list
  "Return: ({:service string :amount Double})"
  [period-start period-end rate-jpy-usd]
  (let [query (base-cost-query period-start period-end)
        query (assoc query :group-by [{:key "SERVICE" :type "DIMENSION"}])
        {results-by-time :results-by-time} (costexplorer/get-cost-and-usage (get-cred) query)
        result-groups ((first results-by-time) :groups)
        service-amount-list (map #(to-service-amount %1 rate-jpy-usd) result-groups)
        service-amount-list (remove is-amount-zero service-amount-list)
        service-amount-list (reverse (sort-by #(:amount %1) service-amount-list))]
    service-amount-list))