(ns remoting.example.client.core
  (:require [goog.dom :as gdom]
            [om.next :as om]
            [remoting.example.client.parser :refer [read mutate]]
            [remoting.example.client.view :as view]
            [cognitect.transit :as transit])
  (:import [goog.net XhrIo]))

(defonce reconciler
  (om/reconciler
    {:state  {:products/cart []}
     :parser (om/parser {:read read :mutate mutate})
     :send   (fn [{query :remote} cb]
               (.send XhrIo "/api/query"
                      (fn [e]
                        (this-as this
                                 (cb (transit/read (transit/reader :json) (.getResponseText this)))))
                      "POST" (transit/write (transit/writer :json) query)
                      #js {"Content-Type" "application/transit+json"}))}))

(defonce root (atom nil))

(defn init! []
  (if (nil? @root)
    (om/add-root! reconciler view/RootView
                  (gdom/getElement "app"))
    (.forceUpdate (om/class->any reconciler view/RootView))))
