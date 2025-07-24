(defproject gravitee-sync "0.1.0-SNAPSHOT"
  :description "Sync Gravitee logs from ES to ClickHouse"
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [clj-http "3.12.3"]
                 [cheshire "5.11.0"]
                 [clj-time "0.15.2"]
                 [com.clickhouse/clickhouse-jdbc "0.4.6"]
                 [org.lz4/lz4-java "1.8.0"]
                 [org.clojure/java.jdbc "0.7.12"]]
  :main gravitee-sync.core
  :aot :all 
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :main gravitee-sync.core}})
