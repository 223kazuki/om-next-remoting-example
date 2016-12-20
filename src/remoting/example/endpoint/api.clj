(ns remoting.example.endpoint.api
  (:refer-clojure :exclude [read])
  (:require [compojure.core :refer :all]
            [om.next.server :as om]
            [cognitect.transit :as transit]
            [remoting.example.component.datomic :as d])
  (:import [java.io ByteArrayOutputStream]))

(defmulti read om/dispatch)
(defmethod read :products/list
  [{:keys [state ast datomic] :as env} k _]
  (let [v (d/query datomic
                   '[:find [(pull ?p [*]) ...]
                     :where [?p :product/number]])]
    {:value v}))
(defmethod read :default
  [{:keys [state ast] :as env} k {:keys [query]}]
  nil)

(defmulti mutate om/dispatch)

(def remote-parser
  (om/parser {:read read :mutate mutate}))

(defn handle-query [datomic req]
  (let [query (transit/read (transit/reader (:body req) :json))
        result (remote-parser {:datomic datomic} query)]
    {:status 200
     :body (let [out-stream (ByteArrayOutputStream.)]
             (transit/write (transit/writer out-stream :json) result)
             (.toString out-stream))}))

(defn api-endpoint [{:keys [datomic]}]
  (context "/api" []
           (POST "/query" req (handle-query datomic req))))
