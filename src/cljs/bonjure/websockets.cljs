(ns bonjure.websockets
  (:require [cognitect.transit :as t]
            [cljs.reader :as reader :refer [read-string]]))

(defonce ws-chan (atom nil))
;(def json-reader (t/reader :json))
;(def json-writer (t/writer :json))

(defonce uid (.-uid js/window))

;(def log (.-log js/console))
;(defonce username (.-username js/window))


(defn receive-event! [update-fn]
  (fn [event]
    (let [data (read-string (.-data event))]
    (update-fn data))))



(defn send-msg!
  [username msg]
  (if @ws-chan
    (.send @ws-chan (str {:type :msg
                          :message msg
                          :uid uid}))
    (throw (js/Error. "Websocket is not available!"))))


(defn send-cmd!
  [cmd params]
  (if @ws-chan
    (.send @ws-chan (str (merge {:type :cmd
                                 :command cmd} params)))
    (throw (js/Error. "Websocket is not available!"))))


(defn send-ping! []
  (if @ws-chan
    (.send @ws-chan (str {
                          :type :ping
                          :uid uid
                          }))))


(.addEventListener js/window "beforeunload" (fn []
                                              (send-cmd! :user-disconnect {:uid uid})
                                              ))

(defn make-websocket! [url receive-handler]
  (println "attempting to connect websocket")
  (if-let [chan (js/WebSocket. url)]
    (do
      (set! (.-onmessage chan) (receive-event! receive-handler))
      (reset! ws-chan chan)
      (.addEventListener @ws-chan "open" (fn []
                                           (send-cmd! :user-connect {:uid uid})))
      (println "Websocket connection established with: " url))
    (throw (js/Error. "Websocket connection failed!"))))

