(ns cljs.user
  (:require [devtools.core :as devtools]
            [figwheel.client :as figwheel]
            [remoting.example.client.core :as core]))

(js/console.info "Starting in development mode")

(enable-console-print!)

(devtools/install!)

(figwheel/start {:websocket-url "ws://localhost:3450/figwheel-ws"})

(defn log [& args]
  (.apply js/console.log js/console (apply array args)))

(core/init!)
