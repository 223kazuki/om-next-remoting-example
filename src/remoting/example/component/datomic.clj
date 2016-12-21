(ns remoting.example.component.datomic
  (:require [datomic.api :as d]
            [com.stuartsierra.component :as component]
            [meta-merge.core :refer [meta-merge]]
            [clojure.java.io :as io]
            [remoting.example.interface :as i])
  (:import datomic.Util))

(defn tempid [part]
  (d/tempid part))

(defn query [this q & params]
  (i/query* this q params))

(defn transact [this transaction]
  (i/transact* this transaction))

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

  i/IDataSource
  (query* [{:keys [connection]} q params]
          (let [db (d/db connection)]
            (apply d/q q db params)))

  (pull [{:keys [connection]} pattern eid]
        (let [db (d/db connection)]
          (d/pull db pattern eid)))

  (transact* [{:keys [connection]} transaction]
             @(d/transact connection transaction))

  (resolve-tempid [{:keys [connection]} tempids tempid]
                  (let [db (d/db connection)]
                    (d/resolve-tempid db tempids tempid))))

(defn datomic-component [options]
  (let [uri (:uri options "datomic:free://localhost:4334/job-streamer")]
    (map->DatomicDataSource
      (meta-merge options {:uri uri
                           :schema       (first (Util/readAll (io/reader (io/resource "data/schema.edn"))))
                           :initial-data (first (Util/readAll (io/reader (io/resource "data/initial.edn"))))}))))
