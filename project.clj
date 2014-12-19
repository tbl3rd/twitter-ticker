(defproject twitter-ticker "0.1.0-SNAPSHOT"
  :description "A Twitter ticker demo."
  :url "https://github.com/tbl3rd/twitter-ticker.git"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :plugins [[lein-cljsbuild "0.2.7"]]
  :hooks [leiningen.cljsbuild]
  :source-paths ["src/clj"]
  :cljsbuild {:builds                   ; :optimizations :advanced
              [{:id "follow"            ; breaks the follow JavaScript
                :jar true               ; so keep :whitespace for now.
                :source-path "src/cljs/follow"
                :compiler {:output-to "ticker/js/follow.js"
                           :optimizations :whitespace
                           :pretty-print true}}
               {:id "feed"
                :jar true
                :source-path "src/cljs/feed"
                :compiler {:output-to "ticker/js/feed.js"
                           :optimizations :advanced}}]}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [hiccup "1.0.1"]
                 [noir "1.3.0-beta10"]
                 [twitter-api "0.6.11"]]
  :main ticker.follow)
