(ns remoting.example.client.view
  (:require [om.next :as om :refer-macros [defui]]
            [sablono.core :refer-macros [html]]))

(defn result-list [results]
  (html
    [:ul {:key "result-list"}
     (map #(vec [:li {:key %} %]) results)]))

(defn search-field [ac query]
  (html
    [:input
     {:key "search-field"
      :value query
      :onChange
      (fn [e]
        (om/set-query! ac
                       {:params {:query (.. e -target -value)}}))}]))

(defui AutoCompleter
  static om/IQueryParams
  (params [_]
          {:query ""})
  static om/IQuery
  (query [_]
         '[(:search/results {:query ?query})])
  Object
  (render [this]
          (let [{:keys [search/results]} (om/props this)]
            (html
              [:div 
               [:h2 "Autocompleter?"]
               (cond->
                 [(search-field this (:query (om/get-params this)))]
                 (not (empty? results)) (conj (result-list results)))]))))
