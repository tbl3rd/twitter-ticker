(ns ticker.follow
  (:require [hiccup.core]
            [hiccup.element]
            [hiccup.form]
            [hiccup.page]
            [noir.core]
            [noir.response]
            [noir.server]
            [ticker.state]
            [ticker.twitter])
  (:gen-class))

(defn get-user-from-screenname
  "Map the @ScreenName sn to the canonicalized user map.
   The map looks like this {:keys [id image name screen]}."
  [sn]
  (and (not (empty? sn))
       (not-any? #(clojure.string/blank? (str %)) sn)
       (not-any? #(= (first "@") (first (str %))) (rest sn))
       (ticker.twitter/screen-name-to-user sn)))

(defn follow-screenname
  "Start following the @ScreenName sn if it is OK and let caller know."
  [sn]
  (println "Following:" sn)
  (let [user (get-user-from-screenname sn)]
    (if user (ticker.state/remember-user user))))

(noir.core/defpage [:post "/ticker/follow/add"]
  {:keys [add]}
  (println "In :post /ticker/follow/add")
  (if (follow-screenname add)
    (noir.response/redirect "/ticker/follow")
    (noir.response/redirect "/ticker/follow")))

(noir.core/defpage [:post "/ticker/follow/stop"]
  {:keys [remove]}
  (println "In :post /ticker/follow/stop")
  (ticker.state/forget-screenname remove)
  (noir.response/redirect "/ticker/follow"))

(noir.core/defpage [:post "/ticker/follow/reset"]
  []
  (println "In :post /ticker/follow/reset")
  (ticker.state/forget-all-screennames)
  (noir.response/redirect "/ticker/follow"))

(noir.core/defpartial follow-add
  []
  [:h "Follow Twitter users."]
  [:div.add
   (hiccup.form/form-to
    [:post "/ticker/follow/add"]
    (hiccup.form/label :add "Follow: ")
    (hiccup.form/text-field {:id "add-field" :placeholder "@ScreenName"} :add)
    (hiccup.form/submit-button {:id "follow-submit"} "Follow"))])

(noir.core/defpartial follow-feedback
  []
  (let [default-feedback "Click Follow to follow @ScreenName."]
    [:div.feedback
     (hiccup.form/hidden-field :default-feedback default-feedback)
     [:span#add-feedback default-feedback]]))

(noir.core/defpartial follow-stop-user
  [user]
  (hiccup.form/form-to
   [:post "/ticker/follow/stop"]
   [:span.stop
    (hiccup.form/submit-button {:id :stop-button} "Stop")
    (hiccup.form/hidden-field :remove (:screen user))
    [:img.avatar {:alt (:name user) :src (:avatar user)}]
    [:p.names
     [:text.name (:name user)]
     [:br]
     [:small [:text.screen (:screen user)]]]]))

(noir.core/defpartial follow-stop
  []
  [:div#following.following
   (let [users (ticker.state/all-users)
         user-count (count users)
         msg (cond
              (= 0 user-count) "Not following anyone ... yet."
              (= 1 user-count) "Following one user."
              :else (format "Following %d users." user-count))]
     (into [:div [:h msg]] (mapcat follow-stop-user users)))])

(noir.core/defpartial follow-reset
  []
  [:div#stop-all.stop-all-form
   (hiccup.form/form-to
    [:post "/ticker/follow/reset"]
    (hiccup.form/submit-button "Stop Following All"))])

;; The CLOSURE_NO_DEPS hack works around a bug in ClojureScript.
;;
(noir.core/defpartial follow-script
  []
  (hiccup.element/javascript-tag "var CLOSURE_NO_DEPS = true;")
  (hiccup.page/include-js "/ticker/js/follow.js"))

(noir.core/defpage "/ticker/follow"
  []
  (println "In :get /ticker/follow")
  (hiccup.core/html
   (hiccup.page/html5
    (hiccup.page/include-css "css/follow.css")
    [:head
     [:title "Twitter Ticker"]]
    [:body
     (follow-feedback)
     (follow-add)
     (follow-stop)
     (follow-reset)]
    (follow-script))))

(noir.core/defpage "/ticker/js/:js"
  {:keys [js]}
  (let [file (format "ticker/js/%s" js)]
    (println "Serving" file)
    (noir.response/content-type
     "text/javascript"
     (clojure.java.io/input-stream file))))

(noir.core/defpage "/ticker/css/:css"
  {:keys [css]}
  (let [file (format "ticker/css/%s" css)]
    (println "Serving" file)
    (noir.response/content-type
     "text/css"
     (clojure.java.io/input-stream file))))

(defn -main [& args]
  (let [port (Integer/parseInt (or (first args) "80"))]
    (noir.server/start port)))

;; (def server (noir.server/start 8080))
;; (noir.server/stop server)
