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
                [:i.add-cart.glyphicon.glyphicon-shopping-cart
                 {:onClick (fn [e] (om/transact! this `[(cart/add-product ~props) :products/cart]))}]]]))))
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
                [:i.remove-cart.glyphicon.glyphicon-minus
                 {:onClick (fn [e] (om/transact! this `[(cart/remove-product ~props) :products/cart]))}]]]))))
(def cart-product (om/factory CartProduct))

(defui ^:once PurchaseProduct
  static om/Ident
  (ident [this {:keys [purchase/product]}]
         [:product/by-number product])
  static om/IQuery
  (query [this]
         [:product/number :product/name :product/price :purchase/count])
  Object
  (render [this]
          (let [{:keys [product/number product/name product/price purchase/count]} (om/props this)]
            (html
              [:tr
               [:td number]
               [:td name]
               [:td (str "$" price)]
               [:td (or count 0)]]))))
(def purchase-product (om/factory PurchaseProduct))

(defui ^:once RootView
  static om/IQuery
  (query [this]
         (let [list-subquery (om/get-query ListProduct)
               cart-subquery (om/get-query CartProduct)
               purchase-subquery (om/get-query PurchaseProduct)]
           `[{:products/list ~list-subquery}
             {:products/cart ~cart-subquery}
             {:products/purchase ~purchase-subquery}]))
  Object
  (render [this]
          (let [{:keys [products/list products/cart products/purchase]} (om/props this)]
            (html
              [:div.container
               [:div.raw
                [:div.col.col-md-8.list
                 [:h2 "Shopping List"]
                 [:table
                  [:thead
                   [:tr
                    [:th "#"]
                    [:th "Name"]
                    [:th "Price"]
                    [:th]]]
                  [:tbody
                   (for [p list]
                     (list-product p))]]]
                [:div.col.col-md-4
                 [:div.raw
                  [:div.col.col-md-12.cart
                   [:h2 "Shopping Cart"]
                   [:div.cartHeader
                    [:label (str "Sum: $"
                                 (->> cart
                                      (map #(let [{:keys [product/price product/in-cart] :or {product/in-cart 0}} %]
                                              (* price in-cart)))
                                      (apply +)))]
                    [:button {:onClick (fn [e] (om/transact! this `[(products/purchase {:products/cart ~cart})]))}
                     "Purchase"]]
                   [:table
                    [:thead
                     [:tr
                      [:th "#"]
                      [:th "Name"]
                      [:th "Price"]
                      [:th "In Cart"]
                      [:th]]]
                    [:tbody
                     (map #(cart-product %) cart)]]]]
                 [:div.raw
                  [:div.col.col-md-12.purchase
                   [:h2 "Purchase History"]
                   [:table
                    [:thead
                     [:tr
                      [:th "#"]
                      [:th "Name"]
                      [:th "Price"]
                      [:th "Count"]]]
                    [:tbody
                     (map #(purchase-product %) purchase)]]]]]]]))))
