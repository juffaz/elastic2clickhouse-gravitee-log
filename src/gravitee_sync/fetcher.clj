(ns gravitee-sync.fetcher)

(defprotocol DataFetcher
  (fetch-incremental [this last-marker]))