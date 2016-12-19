(ns remoting.example.endpoint.api
  (:refer-clojure :exclude [read])
  (:require [compojure.core :refer :all]
            [clojure.java.io :as io]
            [om.next.server :as om]
            [cognitect.transit :as transit]
            [remoting.example.component.datomic :as d])
  (:import [java.io ByteArrayOutputStream]))

(defmulti read om/dispatch)

(defmethod read :list/products
  [{:keys [state ast datomic] :as env} k {:keys [query]}]
  (let [v (d/query datomic
                   '[:find [(pull ?p [*]) ...]
                     :where [?p :product/number]])]
    {:value v}))

(defmethod read :default
  [{:keys [state ast] :as env} k {:keys [query]}]
  "OK")

(def remote-parser
  (om/parser {:read read}))

(defn handle-query [datomic req]
  (let [query (transit/read (transit/reader (:body req) :json))
        result (remote-parser {:datomic datomic} query)]
    {:status 200
     :body (let [out-stream (ByteArrayOutputStream.)]
             (transit/write (transit/writer out-stream :json) result)
             (.toString out-stream))}))

(defn get-todos [datomic]
  (d/query datomic
           '[:find [(pull ?t [*]) ...]
             :where [?t :todo/title]]))

(defn api-endpoint [{:keys [datomic]}]
  (context "/api" []
           (GET "/ping" [] "pong")
           (POST "/query" req (handle-query datomic req))))
