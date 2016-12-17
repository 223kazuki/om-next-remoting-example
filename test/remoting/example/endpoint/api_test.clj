(ns remoting.example.endpoint.api-test
  (:require [com.stuartsierra.component :as component]
            [clojure.test :refer :all]
            [kerodon.core :refer :all]
            [kerodon.test :refer :all]
            [shrubbery.core :as shrub]
            [remoting.example.endpoint.api :as api]))

(def handler
  (api/api-endpoint {}))

(deftest smoke-test
  (testing "api page exists"
    (-> (session handler)
        (visit "/api")
        (has (status? 200) "page exists"))))
