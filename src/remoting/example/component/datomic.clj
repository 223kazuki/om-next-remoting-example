(ns remoting.example.component.datomic
  (:require [datomic.api :as d]
            [com.stuartsierra.component :as component]
            [meta-merge.core :refer [meta-merge]]
            [clojure.java.io :as io]
            [remoting.example.util :as u])
  (:import datomic.Util))

(defn tempid [part]
  (d/tempid part))

(defn query [component q & params]
  (u/query* component q params))

(defn transact [component transaction]
  (u/transact* component transaction))

(defrecord DatomicDataSource [uri schema initial-data connection]
  component/Lifecycle
  (start [component]
         (d/create-database uri)
         (let [c (d/connect uri)]
           @(d/transact c schema)
           @(d/transact c initial-data)
           (assoc component :connection c)))
  (stop [component]
        (d/delete-database uri)
        (assoc component :connection nil))

  u/IDataSource
  (query* [{:keys [connection] :as component} q params]
          (let [db (d/db connection)]
            (apply d/q q db params)))

  (pull [{:keys [connection] :as component} pattern eid]
        (let [db (d/db connection)]
          (d/pull db pattern eid)))

  (transact* [{:keys [connection] :as component} transaction]
             @(d/transact connection transaction))

  (resolve-tempid [{:keys [connection] :as component} tempids tempid]
                  (let [db (d/db connection)]
                    (d/resolve-tempid db tempids tempid))))

(defn datomic-component [options]
  (let [uri (:uri options "datomic:free://localhost:4334/job-streamer")]
    (map->DatomicDataSource
      (meta-merge options {:uri uri
                           :schema       (first (Util/readAll (io/reader (io/resource "data/schema.edn"))))
                           :initial-data (first (Util/readAll (io/reader (io/resource "data/initial.edn"))))}))))
