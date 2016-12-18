(ns remoting.example.client.parser
  (:require [clojure.string :as string]
            [om.next :as om]))

(defmulti read om/dispatch)

(defmethod read :dashboard/items
  [{:keys [state ast]} k _]
  (let [st @state]
    {:value   (into [] (map #(get-in st %)) (get st k))
     :dynamic (update-in ast [:query]
                #(->> (for [[k _] %]
                        [k [:favorites]])
                  (into {})))
     :static  (update-in ast [:query]
                #(->> (for [[k v] %]
                        [k (into [] (remove #{:favorites}) v)])
                  (into {})))}))

(defmulti mutate om/dispatch)

(defmethod mutate 'dashboard/favorite
  [{:keys [state]} k {:keys [ref]}]
  {:action
   (fn []
     (swap! state update-in (conj ref :favorites) inc))})
