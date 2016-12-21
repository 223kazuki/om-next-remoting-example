(ns remoting.example.client.parser
  (:require [clojure.string :as string]
            [om.next :as om]))

(defn get-product [state key]
  (let [st @state]
    (into [] (map #(get-in st %)) (get st key))))

(defmulti read om/dispatch)
(defmethod read :products/list
  [{:keys [state ast] :as env} key params]
  (let [st @state]
    (if (contains? st key)
      {:value (get-product state key)}
      {:remote ast})))
(defmethod read :products/cart
  [{:keys [state] :as env} key params]
  {:value (get-product state key)})

(defmulti mutate om/dispatch)
(defmethod mutate 'cart/add-product
  [{:keys [state]} k {:keys [product/number]}]
  {:action
   (fn []
     (swap! state update-in [:products/cart] #(set (conj % [:product/by-number number])))
     (swap! state update-in [:product/by-number number :product/in-cart] #(if-let [n %] (inc n) 1)))})
(defmethod mutate 'cart/remove-product
  [{:keys [state]} k {:keys [product/number]}]
  {:action
   (fn []
     (swap! state update-in [:product/by-number number :product/in-cart] #(if-let [n %] (if (<= n 0)  0 (dec n)) 0)))})



