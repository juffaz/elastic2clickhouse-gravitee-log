(ns gravitee-sync.sync
  (:require [gravitee-sync.clickhouse :as ch]
            [gravitee-sync.metrics :as m]
            [clj-time.core :as time]
            [clj-time.coerce :as tc]
            [clj-time.format :as f]
            [gravitee-sync.fetcher :refer [fetch-incremental]])
  (:import [java.time LocalDateTime ZoneId Instant]
           [java.util Date]))

(def iso-formatter (f/formatters :date-time))

(def epoch-threshold (time/date-time 2000 1 1))

(defn ->joda-time
  "Convert various timestamp types to org.joda.time.DateTime"
  [x]
  (cond
    (nil? x) nil
    (instance? org.joda.time.DateTime x) x
    (instance? Date x) (tc/from-date x)
    (instance? Instant x) (tc/from-long (.toEpochMilli ^Instant x))
    (instance? LocalDateTime x)
    (let [instant (.toInstant (.atZone ^LocalDateTime x (ZoneId/of "UTC")))]
      (tc/from-long (.toEpochMilli instant)))
    (string? x)
    (try (f/parse iso-formatter x) (catch Exception _ nil))
    (number? x)
    (tc/from-long x)
    :else
    (do
      (println "⚠️ Unsupported ts-raw type:" (type x) ", value:" x)
      nil)))


(def http-method-map
  {0 "CONNECT" 1 "DELETE" 2 "GET" 3 "HEAD" 4 "OPTIONS" 5 "OTHER" 6 "PATCH" 7 "POST" 8 "PUT" 9 "TRACE"})

(defn transform-hit [hit]
  (let [source (:_source hit)
        ts-raw (or (get source (keyword "@timestamp")) (get source :timestamp))
        keys-source (keys source)]
    (println "🔍 Source keys:" keys-source)
    (println "🕒 RAW timestamp:" ts-raw " type:" (type ts-raw))
    (let [ts (->joda-time ts-raw)]
      (if ts
        (let [uri (or (:uri source) (:path source) "/unknown")
              remote-addr (or (:remote-address source) (:local-address source) "0.0.0.0")
              status (or (:status source) 0)
              method-num (:method source)
              method-str (if (number? method-num)
                           (get http-method-map method-num (str method-num))
                           (str method-num))]
          (println "DEBUG: ✅ uri:" uri ", ip:" remote-addr ", status:" status ", method:" method-str)
          {:timestamp (java.sql.Timestamp. (.getMillis ts))  ; ✅ Правильный способ
           :_id (str (or (:_id hit) ""))
           :index_name (str (or (:_index hit) ""))
           :request_path uri
           :client_ip remote-addr
           :status status
           :method method-str})
        (do
          (println "❌ Failed to parse timestamp, skipping:" ts-raw)
          nil)))))

(defn sync-new-data [fetcher]
  (m/timed "sync_incremental"
    (let [last-ts (ch/get-last-timestamp)
          _ (println "DEBUG: last-ts value:" last-ts " type:" (type last-ts))
          hits-batches (fetch-incremental fetcher last-ts)
          _ (println "🎯 Realizing first batch...")
          _ (when (seq hits-batches)
              (println "🎯 First batch size:" (count (first hits-batches))))
          total (atom 0)]
      (doseq [batch hits-batches]
        (let [transformed (doall (keep transform-hit batch))]
           (println "🔢 Transformed count:" (count transformed) "/" (count batch))
           (when (seq transformed)
               (println "🧪 Sample record:" (pr-str (first transformed))))
           (if (seq transformed)
              (do
                (ch/insert-batch transformed)
                (println "📥 Inserted:" (count transformed) "records"))
             (println "🟡 Empty batch after transformation"))
           (swap! total + (count transformed))))
      (println "✅ Sync completed. Total inserted:" @total)
      @total)))
