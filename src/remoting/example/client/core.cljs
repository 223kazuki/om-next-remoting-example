(ns remoting.example.client.core
  (:require [goog.dom :as gdom]
            [om.next :as om]
            [remoting.example.client.parser :refer [read mutate]]
            [remoting.example.client.view :as view]
            [cognitect.transit :as transit])
  (:import [goog.net XhrIo]))

(defonce app-state (atom {}))

(defonce reconciler
  (om/reconciler
    {:state  app-state
     :normalize true
     :parser (om/parser {:read read :mutate mutate})
     :send   (fn [{query :remote} callback]
               (.send XhrIo "/api/query"
                      (fn [e]
                        (this-as this
                                 (callback (transit/read (transit/reader :json) (.getResponseText this)))))
                      "POST" (transit/write (transit/writer :json) query)
                      #js {"Content-Type" "application/transit+json"}))}))

(defonce root (atom nil))

(defn init! []
  (if (nil? @root)
    (let [target (gdom/getElement "app")]
      (om/add-root! reconciler view/RootView target)
      (reset! root view/RootView))
    (.forceUpdate (om/class->any reconciler view/RootView))))
