(ns aws-cost-notifier.date
  (:require [clojure.string :as string]))

(defn today [] (.toString (java.time.LocalDate/now)))

(defn first-date-of-month [] (clojure.string/replace (today) #"\d{2}$" "01"))
