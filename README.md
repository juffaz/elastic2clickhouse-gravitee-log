# 🚀 gravitee-sync

🔄 A Clojure service to sync Gravitee logs from Elasticsearch to ClickHouse.

## 📦 Features

- Supports `@timestamp` in multiple formats (ISO8601, Java-style)
- Efficient batch insert into ClickHouse
- Auto-detects `last timestamp` for incremental sync
- LZ4 compression support
- Resilient with detailed debug logging

## ⚙️ Environment Variables

| Variable            | Description                                        |
|---------------------|----------------------------------------------------|
| `ES_HOST`           | Elasticsearch URL (e.g., `http://elastic:9200`)    |
| `CH_URL`            | ClickHouse JDBC URL (e.g., `jdbc:clickhouse://localhost:8123/default?compress=1&compress_algorithm=LZ4`) |
| `CH_USER`           | ClickHouse username                                |
| `CH_PASS`           | ClickHouse password                                |
| `BATCH_SIZE`        | Batch size for inserts (default: `500`)            |
| `SYNC_INTERVAL_SEC` | Sync interval in seconds (default: `1200`)         |

## 🐳 Quick Start (Docker)

```bash
docker build -t gravitee-sync .
docker run -d --name gravitee-sync \
  --network=host \
  -e ES_HOST=http://elastic:9200 \
  -e CH_URL=jdbc:clickhouse://localhost:8123/default?compress=1&compress_algorithm=LZ4 \
  -e CH_USER=logstash \
  -e CH_PASS=secret \
  -e BATCH_SIZE=500 \
  -e SYNC_INTERVAL_SEC=1200 \
  gravitee-sync
