(ns bonjure.config
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[bonjure started successfully]=-"))
   :middleware identity})
