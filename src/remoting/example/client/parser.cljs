(ns remoting.example.client.parser
  (:require [om.next :as om]))

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
(defmethod read :products/purchase
  [{:keys [state ast] :as env} key params]
  (let [st @state]
    (if (contains? st key)
      {:value (get-product state key)}
      {:remote ast})))

(defmulti mutate om/dispatch)
(defmethod mutate 'cart/add-product
  [{:keys [state]} k {:keys [product/number]}]
  {:action
   (fn []
     (swap! state update-in [:products/cart]
            #(set (conj % [:product/by-number number])))
     (swap! state update-in [:product/by-number number :product/in-cart]
            #(if-let [n %] (inc n) 1)))})
(defmethod mutate 'cart/remove-product
  [{:keys [state]} k {:keys [product/number]}]
  {:action
   (fn []
     (swap! state update-in [:product/by-number number :product/in-cart]
            #(if-let [n %] (if (<= n 0) 0 (dec n)) 0)))})
(defmethod mutate 'products/purchase
  [{:keys [state ast]} k {:keys [products/cart]}]
  (merge
    {:action
     (fn []
       (swap! state dissoc :products/cart)
       (doall
         (map (fn [{:keys [product/number product/in-cart]}]
                (swap! state update-in [:products/purchase]
                       #(set (conj % [:product/by-number number])))
                (swap! state assoc-in  [:product/by-number number :product/in-cart] 0)
                (swap! state update-in [:product/by-number number :purchase/count]
                       #(if-let [n %] (+ n in-cart) in-cart)))
              cart)))}
    (when (seq cart) {:remote ast})))
