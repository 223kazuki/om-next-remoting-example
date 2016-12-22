(ns remoting.example.component.parser
  (:refer-clojure :exclude [read])
  (:require [com.stuartsierra.component :as component]
            [meta-merge.core :refer [meta-merge]]
            [om.next.server :as om]
            [remoting.example.component.datomic :as d]))

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
                              :purchase/count   (->> (val m)
                                                     (map :purchase/count)
                                                     (reduce +)))))
               vec)]
    {:value v}))

(defmulti mutate om/dispatch)
(defmethod mutate 'products/purchase
  [{:keys [datomic] :as env} k {:keys [products/cart]}]
  (->> cart
       (map #(assoc {}
               :db/id (d/tempid :db.part/user)
               :purchase/product (:product/number %)
               :purchase/count (:product/in-cart %)))
       vec
       (d/transact datomic)))

(defn parse-query [{:keys [datomic] :as component} query]
  (let [parser (om/parser {:read read :mutate mutate})]
    (parser {:datomic datomic} query)))

(defrecord ParserComponent [_]
  component/Lifecycle
  (start [component]
         component)
  (stop [component]
        component))

(defn parser-component [options]
  (map->ParserComponent options))
