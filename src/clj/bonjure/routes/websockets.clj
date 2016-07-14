(ns bonjure.routes.websockets
  (:require [compojure.core :refer [GET defroutes wrap-routes]]
            [clojure.tools.logging :as log]
            [immutant.web.async    :as async]
            [bonjure.datastore :as datastore]
            [clj-time.core :as time]
            [clj-time.coerce :as coerce]
            ))


(defonce channels (atom #{}))

(defonce channels-to-uids (atom {}))
;( {} 1)

;(get (assoc {} #{1 2 3} 1) #{1 2 3})

;(assoc map key val)

;(hash #{1})

(defn broadcast
  ([form]
   (doseq [channel @channels]
     (async/send! channel (str form))))

  ([channel form]
   (async/send! channel (str form))))

(defn remove-user [uid]
  (datastore/remove-user uid)
  (broadcast {
              :type :cmd
              :command :user-disconnect
              :users  (vals (datastore/get-users))
              } )
  )

(defn sysmess [text]
  (let [msg {:type :msg
             :message text
             :username "sysmess"
             :color "c0"}]
    (broadcast msg)
    (datastore/add-message msg)))

(defn connect! [channel]
  (log/info "channel open")
  ;(swap! channels-to-uids assoc (hash channel) nil)
  ;(log/info "> METADATA:" channels-to-uids)
  (swap! channels conj channel))

(defn disconnect! [channel {:keys [code reason]}]
  (log/info "close code:" code "reason:" reason)

  ;(remove-user (get @channels-to-uids (hash channel)))
  (swap! channels #(remove #{channel} %)))


;(defn test! [channel event]
;(remove-user (get @channels-to-uids (hash channel)))
;(log/info "ERROR" (cause event)))
(defn update-userlist []
  (let [users (datastore/get-users)
        current-time (coerce/to-long (time/now))]
    (doseq [user users]
      (when (> (- current-time (:ping (second user))) 60000)
        ;(sysmess (str "<- " (:username (second user)) " left chat"))
        (datastore/remove-user (first user)))
      )

    (broadcast {
                :type :cmd
                :command :user-disconnect
                :users  (vals (datastore/get-users))
                } )

    ))



;(re-find #"#d(\d*)" "fsd #d10fs")

(defn parse [msg]
  (clojure.string/replace msg #"#d(\d*)"
                          (fn [[_ digit]]
                            (sysmess "next message is verified by server")
                            (str "[rolls " _ " and gets " (rand-int (read-string digit)) "]"))))

(defn ping [uid]
  (let [user-data (get (datastore/get-user uid) uid)]
    (datastore/set-user uid (assoc user-data
                              :ping (coerce/to-long (time/now))))

    (update-userlist)))

(defn notify-clients! [channel msg]
  (let [m (read-string msg)
        type (:type m)]
    ;(log/info type)

    (cond
      (= type :ping) (ping (:uid m))
      (= type :cmd) (cond
                      (= (:command m) :user-connect)
                      (do
                        (swap! channels-to-uids assoc (hash channel) (:uid m))
                        (broadcast channel {
                                            :type :log
                                            :messages (reverse (datastore/get-messages 0 100))
                                            })
                        (broadcast {
                                    :type :cmd
                                    :command (:command m)
                                    :users  (vals (datastore/get-users))
                                    } )
                        ;(sysmess (str "-> " (:username (get (datastore/get-user (:uid m)) (:uid m))) " entered chat"))
                        )
                      (= (:command m) :user-disconnect) (+ 1 1))
      (= type :msg) (let [message (merge {
                                          :type :msg
                                          :message (parse (:message m))
                                          } (get (datastore/get-user (:uid m)) (:uid m)))]
                      (broadcast message)
                      (datastore/add-message message))
      )))


(def websocket-callbacks
  "WebSocket callback functions"
  {:on-open connect!
   :on-close disconnect!
   ;:on-error test!
   :on-message notify-clients!})

(defn ws-handler [request]
  (async/as-channel request websocket-callbacks))

(defroutes websocket-routes
           (GET "/ws" [] ws-handler))
