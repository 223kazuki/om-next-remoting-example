(ns remoting.example.client.view
  (:require [om.next :as om :refer-macros [defui]]
            [sablono.core :refer-macros [html]]))

(defui ^:once Post
  static om/IQuery
  (query [this]
         [:id :type :title :author :content])
  Object
  (render [this]
          (html
            (let [{:keys [title author content] :as props} (om/props this)]
              [:div
               [:h3 title]
               [:h4 author]
               [:p content]]))))

(def post (om/factory Post))

(defui ^:once Photo
  static om/IQuery
  (query [this]
         [:id :type :title :image :caption])
  Object
  (render [this]
          (html
            (let [{:keys [title image caption]} (om/props this)]
              [:div
               [:h3 (str "Photo: " title)]
               [:div image]
               [:p (str "Caption: " caption)]]))))

(def photo (om/factory Photo))

(defui ^:once Graphic
  static om/IQuery
  (query [this]
         [:id :type :title :image])
  Object
  (render [this]
          (html
            (let [{:keys [title image]} (om/props this)]
              [:div
               [:h3 (str "Graphic: " title)]
               [:div image]]))))

(def graphic (om/factory Graphic))

(defui ^:once DashboardItem
  static om/Ident
  (ident [this {:keys [id type]}]
         [type id])
  static om/IQuery
  (query [this]
         (zipmap
           [:dashboard/post :dashboard/photo :dashboard/graphic]
           (map #(conj % :favorites)
                [(om/get-query Post)
                 (om/get-query Photo)
                 (om/get-query Graphic)])))
  Object
  (render [this]
          (html
            (let [{:keys [id type favorites] :as props} (om/props this)]
              [:li {:key id :style {:padding 10 :borderBottom "1px solid black"}}
               [:div
                (({:dashboard/post    post
                   :dashboard/photo   photo
                   :dashboard/graphic graphic} type)
                 (om/props this))]
               [:div
                [:p (str "Favorites: " favorites)]
                [:button
                 {:onClick
                  (fn [e]
                    (om/transact! this
                                  `[(dashboard/favorite {:ref [~type ~id]})]))}
                 "Favorite!"]]]))))

(def dashboard-item (om/factory DashboardItem))

(defui ^:once Dashboard
  static om/IQuery
  (query [this]
         [{:dashboard/items (om/get-query DashboardItem)}])
  Object
  (render [this]
          (html
            (let [{:keys [dashboard/items]} (om/props this)]
              [:ul {:style {:padding 0}}
               (map dashboard-item items)]))))

(defui ^:once Product
  static om/IQuery
  (query [this]
         [:product/id :product/name :product/price])
  Object
  (render [this]
          (html [:h2 "product"])))



(defui ^:once Market
  static om/IQuery
  (query [this]
         [:list/products])
  Object
  (render [this]
          (html
            (let [{:keys [list/products]} (om/props this)]
              [:ul {:style {:padding 0}}
               (map #(vec [:li {:key (:product/number %)} (str (:product/name %) ":" (:product/price %))]) products)]))))
