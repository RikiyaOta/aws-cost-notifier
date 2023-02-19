(ns aws-cost-notifier.message-builder
  (:require [clojure.string :as string]))

(defn- get-aws-account [] (System/getenv "TARGET_AWS_ACCOUNT"))

(defn build-message-header
  [period-start period-end]
  (let [title "----- AWS 料金通知 -----"
        aws-account-line (str "AWS account: " (get-aws-account))
        period-line (str "期間: " period-start " ~ " period-end)]
    (str title "\n\n" aws-account-line "\n" period-line)))

(defn build-total-cost-message
  [total-jpy]
  (str "```\n" "Total: " total-jpy " JPY\n```"))

(defn- get-max-service-length [service-amount-list]
  (let [services (map #(:service %1) service-amount-list)
        length-list (map count services)]
    (apply max length-list)))

(defn- format-service-width [service-amount-list]
  (let [max-service-length (get-max-service-length service-amount-list)
        fmt-str (str "%-" (+ max-service-length 1) "s")
        convert (fn [service-amount] (let [formatted-service (format fmt-str (service-amount :service))] (assoc service-amount :service formatted-service)))]
    (map convert service-amount-list)))

(defn- format-service-amount [service-amount]
  (let [{service :service amount :amount} service-amount]
    (str service ": " amount " JPY")))

(defn build-cost-by-service-message
  [service-amount-list]
  (let [service-amount-list (format-service-width service-amount-list)
        message-line-list (map format-service-amount service-amount-list)]
    (str "```\n" (string/join "\n" message-line-list) "\n```")))

(defn build-exchange-rate-message
  [rate-jpy-usd]
  (str "JPY/USD = " rate-jpy-usd))