(ns aws-cost-notifier.core
  (:require [aws-cost-notifier.aws-cost-explorer :as aws-cost-explorer]
            [aws-cost-notifier.slack-api :as slack-api]
            [aws-cost-notifier.date :as date]
            [aws-cost-notifier.exchange-rate-api :as exchange-rate-api]
            [aws-cost-notifier.message-builder :as message-builder]))

(defn main []
  (let [period-start (date/first-date-of-month)
        period-end (date/today)

        header-message (message-builder/build-message-header period-start period-end)

        rate-jpy-usd (exchange-rate-api/get-rate-jpy-usd)
        total-jpy (aws-cost-explorer/get-total-jpy period-start period-end rate-jpy-usd)
        total-cost-message (message-builder/build-total-cost-message total-jpy)

        service-amount-list (aws-cost-explorer/get-service-amount-list period-start period-end rate-jpy-usd)
        cost-by-service-message (message-builder/build-cost-by-service-message service-amount-list)

        exchange-rate-message (message-builder/build-exchange-rate-message rate-jpy-usd)

        message (str header-message "\n\n" total-cost-message "\n" cost-by-service-message "\n" exchange-rate-message)]
    (slack-api/post-to-slack message)))