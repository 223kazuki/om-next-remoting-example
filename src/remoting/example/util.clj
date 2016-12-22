(ns remoting.example.util)

(defprotocol ^:once IDataSource
  (query*         [this q params])
  (pull           [this pattern eid])
  (transact*      [this transaction])
  (resolve-tempid [this tempids tempid]))
