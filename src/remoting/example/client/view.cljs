(ns remoting.example.client.view
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]))

(defn result-list [results]
  (dom/ul #js {:key "result-list"}
          (map #(dom/li #js {:key %} nil %) results)))

(defn search-field [ac query]
  (dom/input
    #js {:key "search-field"
         :value query
         :onChange
         (fn [e]
           (om/set-query! ac
                          {:params {:query (.. e -target -value)}}))}))

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
            (dom/div nil
                     (dom/h2 nil "Autocompleter?")
                     (cond->
                       [(search-field this (:query (om/get-params this)))]
                       (not (empty? results)) (conj (result-list results)))))))
