(ns remoting.example.endpoint.api
  (:refer-clojure :exclude [read])
  (:require [compojure.core :refer [POST]]
            [om.next.server :as om]
            [cognitect.transit :as transit]
            [remoting.example.component.parser :as p])
  (:import [java.io ByteArrayOutputStream]))

(defn handle-query [{:keys [parser] :as component} req]
  (let [query  (transit/read (transit/reader (:body req) :json))
        result (p/parse-query parser query)]
    {:status 200
     :body (let [out-stream (ByteArrayOutputStream.)]
             (transit/write (transit/writer out-stream :json) result)
             (.toString out-stream))}))

(defn api-endpoint [component]
  (POST "/api/query" req (handle-query component req)))
