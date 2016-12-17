(defproject om-next-remoting-example "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.8.51"]
                 [com.stuartsierra/component "0.3.1"]
                 [compojure "1.5.1"]
                 [duct "0.8.2"]
                 [environ "1.1.0"]
                 [ring "1.5.0"]
                 [ring/ring-defaults "0.2.1"]
                 [ring-jetty-component "0.3.1"]
                 [ring-webjars "0.1.1"]
                 [org.slf4j/slf4j-nop "1.7.21"]
                 [org.webjars/normalize.css "3.0.2"]
                 [org.omcljs/om "1.0.0-alpha42"]
                 [org.clojure/core.async "0.2.395"]
                 [sablono "0.7.6"]
                 [com.datomic/datomic-free "0.9.5544"
                  :exclusions [org.slf4j/slf4j-api org.slf4j/slf4j-nop
                               org.slf4j/slf4j-log4j12 org.slf4j/log4j-over-slf4j
                               com.amazonaws/aws-java-sdk]]]
  :plugins [[lein-environ "1.0.3"]
            [lein-cljsbuild "1.1.2"]]
  :main ^:skip-aot remoting.example.main
  :target-path "target/%s/"
  :resource-paths ["resources" "target/cljsbuild"]
  :prep-tasks [["javac"] ["cljsbuild" "once"] ["compile"]]
  :cljsbuild
  {:builds
   [{:id "main"
     :jar true
     :source-paths ["src"]
     :compiler
     {:output-to     "target/cljsbuild/public/js/main.js"
      :optimizations :advanced}}]}
  :profiles
  {:dev  [:project/dev  :profiles/dev]
   :test [:project/test :profiles/test]
   :repl {:resource-paths ^:replace ["resources" "dev/resources" "target/figwheel"]
          :prep-tasks     ^:replace [["javac"] ["compile"]]}
   :uberjar {:aot :all}
   :profiles/dev  {}
   :profiles/test {}
   :project/dev   {:dependencies [[reloaded.repl "0.2.3"]
                                  [org.clojure/tools.namespace "0.2.11"]
                                  [org.clojure/tools.nrepl "0.2.12"]
                                  [eftest "0.1.1"]
                                  [com.gearswithingears/shrubbery "0.4.1"]
                                  [kerodon "0.8.0"]
                                  [binaryage/devtools "0.8.2"]
                                  [com.cemerick/piggieback "0.2.1"]
                                  [duct/figwheel-component "0.3.3"]
                                  [figwheel "0.5.8"]
                                  [alembic "0.3.2"]]
                   :source-paths   ["dev/src"]
                   :resource-paths ["dev/resources"]
                   :repl-options {:init-ns user
                                  :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
                   :env {:port "3000"}}
   :project/test  {}})
