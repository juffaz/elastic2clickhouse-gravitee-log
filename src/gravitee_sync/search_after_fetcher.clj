(ns gravitee-sync.search-after-fetcher
  (:require [clj-http.client :as http]
            [cheshire.core :as json]
            [gravitee-sync.fetcher :refer [DataFetcher]]
            [gravitee-sync.config :refer [config]]))

(defrecord SearchAfterFetcher []
  DataFetcher
  (fetch-incremental [_ last-marker]
    (let [query-base {:query {:match_all {}}
                  :size (:batch-size config)
                  :sort [{"@timestamp" {"order" "asc"}} {"_id" {"order" "asc"}}]}]
      (try
        (loop [search-after nil results []]
          (println "DEBUG: Sending ES request with from:" from " search_after:" search-after)  ; Отладка запроса
          (let [q (if search-after (assoc query-base :search_after search-after) query-base)
                url (str (:es-host config) "/" (:index-pattern config) "/_search")
                resp (http/post url {:body (json/generate-string q)
                                     :headers {"Content-Type" "application/json"}
                                     :socket-timeout 60000 :connection-timeout 60000})
                body (json/parse-string (:body resp) true)]
            (println "DEBUG: ES response status:" (:status resp) " hits count:" (count (get-in body [:hits :hits])))  ; Отладка ответа
            (if (= 200 (:status resp))
              (let [hits (get-in body [:hits :hits])
                    new-search-after (get (last hits) :sort)]
                (if (or (empty? hits) (nil? new-search-after))
                  (conj results hits)
                  (recur new-search-after (conj results hits))))
              (do
                (println "ERROR: ES response error:" (:status resp))
                results))))
        (catch Exception e
          (println "ERROR: Fetch failed:" (.getMessage e))
          [])))))
