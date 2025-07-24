(ns gravitee-sync.clickhouse
  (:require [clojure.java.jdbc :as jdbc]
            [gravitee-sync.config :refer [config]]))

(def db-spec {:classname "com.clickhouse.jdbc.ClickHouseDriver"
              :subprotocol "clickhouse"
              :subname (str "//" (:ch-host config) ":" (:ch-port config) "/" (:ch-db config) "?compress=1&compress_algorithm=LZ4")
              :user (:ch-user config)
              :password (:ch-pass config)})

(defn get-last-timestamp []
  (try
    (let [result (jdbc/query db-spec ["SELECT MAX(timestamp) AS max_ts FROM gravitee_requests"])]
      (println "DEBUG: get-last-timestamp result:" (first result))
      (:max_ts (first result)))
    (catch Exception e
      (println "ERROR: Get last timestamp failed:" (.getMessage e))
      nil)))

(defn insert-batch [records]
  (let [n (count records)]
    (println "DEBUG: Inserting batch of size:" n)
    (try
      (if (seq records)
        (let [columns [:timestamp :_id :index_name :request_path :client_ip :status :method]
              values (mapv (fn [r] (mapv r columns)) records)]
          (jdbc/insert-multi! db-spec :gravitee_requests columns values)
          (println "✅ Successfully inserted" n "records"))
        (println "🟡 Empty batch, nothing to insert"))
      (catch Exception e
        (println "❌ Insert failed:" (.getMessage e))
        (println "💡 Full error:" (str e))
        (when (seq records)
          (println "📄 Sample record:" (pr-str (first records))))
        (throw e)))))