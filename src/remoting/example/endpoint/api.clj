(ns remoting.example.endpoint.api
  (:refer-clojure :exclude [read])
  (:require [compojure.core :refer :all]
            [om.next.server :as om]
            [cognitect.transit :as transit]
            [remoting.example.component.datomic :as d])
  (:import [java.io ByteArrayOutputStream]))

(defmulti read om/dispatch)
(defmethod read :products/list
  [{:keys [datomic] :as env} k _]
  (let [v (d/query datomic
                   '[:find [(pull ?p [*]) ...]
                     :where [?p :product/number]])]
    {:value v}))
(defmethod read :products/purchase
  [{:keys [datomic] :as env} k _]
  (let [v (->> (d/query datomic
                        '[:find [(pull ?p [*]) ...]
                          :where [?p :purchase/product]])
               (group-by :purchase/product)
               (map (fn [m] (assoc {}
                              :purchase/product (key m)
                              :purchase/count (reduce + (map :purchase/count (val m))))))
               vec)]
    {:value v}))

(defmulti mutate om/dispatch)
(defmethod mutate 'products/purchase
  [{:keys [datomic]} k {:keys [products/cart]}]
  (->> cart
       (map #(assoc {}
               :db/id (d/tempid :db.part/user)
               :purchase/product (:product/number %)
               :purchase/count (:product/in-cart %)))
       vec
       (d/transact datomic)))

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
