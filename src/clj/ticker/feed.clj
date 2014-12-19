(ns ticker.feed
  (:require [hiccup.core]
            [hiccup.element]
            [hiccup.form]
            [hiccup.page]
            [noir.core]
            [noir.response]
            [noir.server]))

(def ^{:doc "A :feed agent against which a stream-agent can dump tweets."}
  ^:private feed-agent
  (agent {:old '()                     ; Tweets rendered to feed page.
          :new '()}))                  ; Tweets not yet rendered.

(defn feed-add-tweet
  "Add this tweet to the feed managed by feed-agent."
  [tweet]
  (println "feed-add-tweet" tweet)
  (send feed-agent
        (fn [feed tweet]
          {:new (into (list) (take 5 (conj (:new feed) tweet)))
           :old (:old feed)})
        tweet))

(defn feed-wrap-tweet
  "Wrap tweet in an hiccupized HTML element."
  [tweet]
  (println "feed-wrap-tweet" tweet)
  [:div.tweet
   [:div.content
    [:div.header
     [:span [:img.avatar {:alt (:name tweet) :src (:avatar tweet)}]
      [:div.names
       [:div.fullname [:span [:strong (:name tweet)]]]
       [:div.screen [:span [:small "@" (:screen tweet)]]]]]]
    [:p.tweet-text (:text tweet)]]])

(noir.core/defpage "/ticker/feed/tweets"
  []
  (send feed-agent (fn [feed] {:old (:new feed) :new '()}))
  (noir.response/content-type
   "text/html;charset=utf-8"
    (hiccup.core/html
     (map feed-wrap-tweet (reverse (:old @feed-agent))))))

;; I have no idea why include-js works with "/ticker/js/feed.js",
;; but include-css works only with "css/feed.css".  The include-css
;; form renders as
;;     <link href="css/feed.css" rel="stylesheet" type="text/css">
;; FWIW.
;;
(noir.core/defpartial feed-script
  []
  (hiccup.element/javascript-tag "var CLOSURE_NO_DEPS = true;")
  (hiccup.page/include-js "js/feed.js"))

(noir.core/defpage "/ticker/feed"
  []
  (hiccup.core/html
   (hiccup.page/html5
    (hiccup.page/include-css "css/feed.css")
    [:head
     [:title "Twitter Feed"]]
    [:body
     [:div#tweets]]
    (feed-script))))

(def karan
  {:screen "@karanlyons"
   :name "Karan Lyons"
   :avatar
   "https://twimg0-a.akamaihd.net/profile_images/1807707473/Avatar_normal.png"
   :text "Whennever. New word. Doesn't work out loud."})

;; (feed-add-tweet karan)
