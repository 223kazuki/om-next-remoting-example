(ns remoting.example.client.core
  (:require [goog.dom :as gdom]
            [om.next :as om]
            [remoting.example.client.parser :refer [read mutate]]
            [remoting.example.client.view :as view]
            [cognitect.transit :as transit])
  (:import [goog.net XhrIo]))

(defonce init-data
  {:dashboard/items
   [{:id 0 :type :dashboard/post
     :author "Laura Smith"
     :title "A Post!"
     :content "Lorem ipsum dolor sit amet, quem atomorum te quo"
     :favorites 0}
    {:id 1 :type :dashboard/photo
     :title "A Photo!"
     :image "photo.jpg"
     :caption "Lorem ipsum"
     :favorites 0}
    {:id 2 :type :dashboard/post
     :author "Jim Jacobs"
     :title "Another Post!"
     :content "Lorem ipsum dolor sit amet, quem atomorum te quo"
     :favorites 0}
    {:id 3 :type :dashboard/graphic
     :title "Charts and Stufff!"
     :image "chart.jpg"
     :favorites 0}
    {:id 4 :type :dashboard/post
     :author "May Fields"
     :title "Yet Another Post!"
     :content "Lorem ipsum dolor sit amet, quem atomorum te quo"
     :favorites 0}]})

(defonce reconciler
  (om/reconciler
    {:state     init-data
     :parser    (om/parser {:read read :mutate mutate})
     :send      (fn [{:keys [remote]} cb]
                  (.send XhrIo "/api/query"
                         (fn [e]
                           (this-as this
                                    (cb (transit/read (om/reader :json) (.getResponseText this)))))
                         "POST" (transit/write (om/writer :json) remote)
                         #js {"Content-Type" "application/transit+json"}))}))

(defonce root (atom nil))

(defn init! []
  (if (nil? @root)
    (om/add-root! reconciler view/Dashboard
                  (gdom/getElement "app"))
    (.forceUpdate (om/class->any reconciler view/Dashboard))))
