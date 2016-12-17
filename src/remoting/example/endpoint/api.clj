(ns remoting.example.endpoint.api
  (:require [compojure.core :refer :all]
            [clojure.java.io :as io]
            [remoting.example.component.datomic :as d]))

(defn get-todos [datomic]
  (d/query datomic
           '[:find [(pull ?t [*]) ...]
             :where [?t :todo/title]]))


(defn api-endpoint [{:keys [datomic]}]
  (context "/api" []
           (GET "/ping" [] "pong")
           (GET "/todos" [] (str (get-todos datomic)))))
