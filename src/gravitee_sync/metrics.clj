(ns gravitee-sync.metrics)

(defn log-metric [k v]
  (println (format "METRIC %s %s" (name k) v)))

(defmacro timed [label & body]
  `(let [start# (System/currentTimeMillis)]
     (log-metric :start ~label)
     (let [result# (do ~@body)
           duration# (- (System/currentTimeMillis) start#)]
       (log-metric :duration_ms duration#)
       (log-metric :end ~label)
       result#)))
