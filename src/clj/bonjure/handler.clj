(ns bonjure.handler
  (:require [compojure.core :refer [defroutes routes wrap-routes]]
            [bonjure.layout :refer [error-page]]
            [bonjure.routes.home :refer [home-routes]]
            [bonjure.routes.websockets :refer [websocket-routes]]
            [bonjure.middleware :as middleware]
            [clojure.tools.logging :as log]
            [compojure.route :as route]
            [config.core :refer [env]]
            [bonjure.config :refer [defaults]]
            [mount.core :as mount]
            [luminus.logger :as logger]))

(defroutes base-routes
           (route/resources "/")
           (route/not-found "Not Found"))

(defn init
  "init will be called once when
   app is deployed as a servlet on
   an app server such as Tomcat
   put any initialization code here"
  []
  (logger/init env)
  (doseq [component (:started (mount/start))]
    (log/info component "started"))
  ((:init defaults)))

(defn destroy
  "destroy will be called when your application
   shuts down, put any clean up code here"
  []
  (log/info "bonjure is shutting down...")
  (doseq [component (:stopped (mount/stop))]
    (log/info component "stopped"))
  (log/info "shutdown complete!"))

(def app-routes
  (routes
    (wrap-routes #'home-routes middleware/wrap-csrf)
    (route/not-found
      (:body
        (error-page {:status 404
                     :title "page not found"})))))


(def app
  (-> (routes
        websocket-routes
        (wrap-routes home-routes middleware/wrap-csrf)
        base-routes)
      middleware/wrap-base
      ))
