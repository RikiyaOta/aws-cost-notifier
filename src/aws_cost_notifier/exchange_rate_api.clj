(ns aws-cost-notifier.exchange-rate-api
  (:require [clj-http.client :as client]
            [cheshire.core :as cheshire]))

(defn get-rate-jpy-usd
  "Get JPY/USD rate."
  []
  (let [raw-rate-result (client/get "https://api.aoikujira.com/kawase/get.php?format=json&code=usd&to=JPY")
        rate-result (cheshire/parse-string (:body raw-rate-result))
        jpy-str (get rate-result "JPY")]
    (Double/parseDouble jpy-str)))