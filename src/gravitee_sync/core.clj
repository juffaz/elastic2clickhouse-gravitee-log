(ns gravitee-sync.core
  (:gen-class)
  (:require [gravitee-sync.config :refer [config]]
            [gravitee-sync.sync :as sync]
            [gravitee-sync.metrics :as m]
            [gravitee-sync.search-after-fetcher :as sa]))  ; Правильный require

(defn -main [& args]
  (let [fetcher (sa/->SearchAfterFetcher)
        interval-ms (* 1000 (:sync-interval config))]
    (println "🚀 gravitee-request sync started. Interval:" (:sync-interval config) "sec")
    (loop []
      (m/timed "sync_cycle"
        (try
          (sync/sync-new-data fetcher)
          (catch Exception e
            (println "❌ Sync failed:" (.getMessage e)))))
      (println "💤 Sleeping..." (/ interval-ms 1000) "sec")
      (Thread/sleep interval-ms)
      (recur))))
