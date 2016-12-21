(ns remoting.example.client.view
  (:require [om.next :as om :refer-macros [defui]]
            [sablono.core :refer-macros [html]]))

(defui ^:once ListProduct
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
               [:td number]
               [:td name]
               [:td (str "$" price)]
               [:td
                [:i {:class "add-cart glyphicon glyphicon-shopping-cart"
                     :onClick (fn [e] (om/transact! this `[(cart/add-product ~props) :products/cart]))}]]]))))
(def list-product (om/factory ListProduct))

(defui ^:once CartProduct
  static om/Ident
  (ident [this {:keys [product/number]}]
         [:product/by-number number])
  static om/IQuery
  (query [this]
         [:product/number :product/name :product/price :product/in-cart])
  Object
  (render [this]
          (let [{:keys [product/number product/name product/price product/in-cart] :as props} (om/props this)]
            (html
              [:tr
               [:td number]
               [:td name]
               [:td (str "$" price)]
               [:td (or in-cart 0)]
               [:td
                [:i {:class "remove-cart glyphicon glyphicon-minus"
                     :onClick (fn [e] (om/transact! this `[(cart/remove-product ~props) :products/cart]))}]]]))))
(def cart-product (om/factory CartProduct))

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
              [:div.container
               [:div.raw
                [:div.col.col-md-8#list
                 [:div
                  [:h2 "Products List"]
                  [:table.list
                   [:tbody
                    (for [p list]
                      (list-product p))]]]]
                [:div.col.col-md-4
                 [:div.raw
                  [:div.col.col-md-12#cart
                   [:div
                    [:h2 "Products Cart"]
                    [:div {:style {:width "100%" :margin-bottom "10px"}}
                     [:labale (str "Sum: $" (->> cart
                                                 (map #(let [{:keys [product/price product/in-cart] :or {product/in-cart 0}} %]
                                                         (* price in-cart)))
                                                 (apply +)))]
                     [:button {:style {:float "right"}
                               :onClick (fn [e] (om/transact! this `[(products/purchase ~cart) :products/cart]))}
                      "Buy"]]
                    [:table.cart
                     [:tbody
                      (map #(cart-product %) cart)]]]]]
                 [:div.raw]]]]))))
