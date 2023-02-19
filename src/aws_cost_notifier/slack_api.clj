(ns aws-cost-notifier.slack-api
  (:require [clj-http.client :as client]))

(defn- get-url [] (System/getenv "INCOMING_WEBHOOK_URL"))

(defn post-to-slack [text]
  (client/post (get-url) {:content-type :json :form-params {:text text}}))