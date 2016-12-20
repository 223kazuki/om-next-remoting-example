(ns remoting.example.client.view
  (:require [om.next :as om :refer-macros [defui]]
            [sablono.core :refer-macros [html]]))

(defui ^:once CartProduct
  static om/Ident
  (ident [this {:keys [product/number]}]
         [:product/by-number number])
  static om/IQuery
  (query [this]
         [:product/number :product/name :product/price])
  Object
  (render [this]
          (let [{:keys [product/number product/name product/price] :as props} (om/props this)]
            (html
              [:tr
               [:td
                (str number ":" name ":" price)]]))))

(def cart-product (om/factory CartProduct))

(defui ^:once ListProduct
  static om/Ident
  (ident [this {:keys [product/number]}]
         [:product/by-number number])
  static om/IQuery
  (query [this]
         [:product/number :product/name :product/price])
  Object
  (render [this]
          (println this)
          (let [{:keys [product/number product/name product/price] :as props} (om/props this)]
            (html
              [:tr
               [:td
                (str number ":" name ":" price)
                [:button {:onClick
                          (fn [e]
                            (om/transact! this `[(cart/add-product ~props) :products/cart]))} "Add Cart"]]]))))

(def list-product (om/factory ListProduct))

(defui ^:once ListView
  Object
  (render [this]
          (let [list (om/props this)]
            (html
              [:div
               [:h2 "Products List"]
               [:table
                [:tbody
                 (for [p list]
                   (list-product p))]]]))))

(def list-view (om/factory ListView))

(defui ^:once CartView
  Object
  (render [this]
          (let [list (om/props this)]
            (html
              [:div
               [:h2 "Products Cart"]
               [:table
                [:tbody
                 (map #(cart-product %) list)]]]))))

(def cart-view (om/factory CartView))

(defui ^:once RootView
  static om/IQuery
  (query [this]
         (let [list-subquery (om/get-query ListProduct)
               cart-subquery (om/get-query CartProduct)]
           `[{:products/list ~list-subquery}
             {:products/cart ~cart-subquery}]))
  Object
  (render [this]
          (let [{:keys [products/list products/cart]} (om/props this)]
            (html
              [:div
               (list-view list)
               (cart-view cart)]))))
