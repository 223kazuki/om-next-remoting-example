(ns remoting.example.endpoint.api
  (:require [compojure.core :refer :all]
            [clojure.java.io :as io]))

(defn api-endpoint [config]
  (context "/api" []
    (GET "/" [] "OK")))
