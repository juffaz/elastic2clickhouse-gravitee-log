(ns gravitee-sync.config)

(defn env [k default]
  (or (System/getenv k) default))

(def config
  {:es-host (env "ES_HOST" "http://elastic-cluster.service.prod-consul:9200")
   :ch-url (env "CH_URL" "jdbc:clickhouse://clickhouse:8123/default")
   :ch-user (env "CH_USER" "default")
   :ch-pass (env "CH_PASS" "")
   :index-pattern (env "INDEX_PATTERN" "gravitee-request-*")
   :sync-interval (Integer/parseInt (env "SYNC_INTERVAL_SEC" "1200"))
   :batch-size (Integer/parseInt (env "BATCH_SIZE" "500"))})