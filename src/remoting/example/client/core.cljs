(ns remoting.example.client.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [goog.dom :as gdom]
            [cljs.core.async :as async :refer [<! >! put! chan]]
            [om.next :as om]
            [remoting.example.client.parser :refer [read]]
            [remoting.example.client.view :as view]
            [cognitect.transit :as transit])
  (:import [goog Uri]
           [goog.net Jsonp]))

(defonce app-state (atom {:search/results []}))

(def base-url
  "http://en.wikipedia.org/w/api.php?action=opensearch&format=json&search=")

(defn jsonp
  ([uri] (jsonp (chan) uri))
  ([c uri]
   (let [gjsonp (Jsonp. (Uri. uri))]
     (.send gjsonp nil #(put! c %))
     c)))

(defn search-loop [c]
  (go
    (loop [[query cb] (<! c)]
      (let [[_ results] (<! (jsonp (str base-url query)))]
        (cb {:search/results results}))
      (recur (<! c)))))

(defn send-to-chan [c]
  (fn [{:keys [search]} cb]
    (when search
      (let [{[search] :children} (om/query->ast search)
            query (get-in search [:params :query])]
        (put! c [query cb])))))

(def send-chan (chan))

(defonce reconciler
  (om/reconciler
    {:state   app-state
     :parser  (om/parser {:read read})
     :send    (send-to-chan send-chan)
     :remotes [:search]}))

(defn init! []
  (search-loop send-chan)
  (om/add-root! reconciler view/AutoCompleter
                (gdom/getElement "app")))
