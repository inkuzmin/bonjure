(ns bonjure.datastore
  (:require [taoensso.carmine :as redis]))


(def pool (redis/make-conn-pool))

(defn parse-uri [u]
  (let [uri (bean (java.net.URI. u))
        userInfo (:userInfo uri)
        splittedUserInfo (seq (.split (if userInfo userInfo "") ":"))]
    (merge uri {:user (first splittedUserInfo) :password (second splittedUserInfo)})))

(def redis-url
  (get (System/getenv) "REDIS_URL" "redis://127.0.0.1:6379"))

(def redis-config
  (merge (parse-uri redis-url) {:timeout 0 :db 0}))

(defmacro with-conn [& body] `(redis/with-conn pool redis-config ~@body))

(defmacro deftrans
  [fname args & body]
  `(defn ~fname ~args
     (redis/with-conn pool redis-config ~@body)))

(deftrans clear []
  (redis/flushall))


(deftrans add-user-nx [id data]
  (redis/hsetnx "users" id data))

(deftrans get-user [id]
  (redis/hmget* "users" id))

(deftrans set-user [id data]
  (redis/hset "users" id data))

(deftrans get-users []
  (redis/hgetall* "users"))

(deftrans remove-user [id]
  (redis/hdel  "users" id))

(deftrans remove-users []
  (redis/del  "users"))

(deftrans add-message [msg]
  (redis/lpush "messages" msg))

(deftrans get-all-messages []
  (redis/lrange "messages" 0 -1))

(deftrans get-last-message []
  (redis/lrange "messages" 0 1))

(deftrans get-messages [from to]
  (redis/lrange "messages" from to))

;(add-message {:test 123})
;(get-last-message)

;(add-user-nx 1 {:test 123})



;(with-conn
;  (redis/ttl "users" "efb3d2b7-9824-4fda-8d94-511b5c895eee")
;  )
;(->> first vals {:a {:b 1}})

;(first (vals {:a {:b 1}}))


;(remove-users)
;(get-users)
;(get (get-user "c34613e4-0a87-4033-909a-b0eed005f02b") "c34613e4-0a87-4033-909a-b0eed005f02b")

;(get-user "a772bc8a-a22c-4fc3-82f4-f2fb33aaeff6")

;(add-user-nx 1 {:test 123})
;
;(get-user "61bb502c-2c82-435a-abac-34898eb165c2")
;
;(clear)


;(deftrans add-user [user]
;          (redis/lpush user))
;
;(defun add-user [user]
;       (with-conn
;         (redis/lpush user))


;(defn clear []
;  (with-conn
;    (redis/flushall)))

;(defn add-user []
;  (with-conn

;))

;(with-conn (redis/ping))