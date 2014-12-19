(ns ticker.browser.feed
  (:require [cljs.reader :as cljs.reader]
            [clojure.browser.dom :as clojure.browser.dom]
            [goog.net.XhrIo :as goog.net.XhrIo]))

;; The :as is required for ClojureScript.
;; Otherwise Closure's Javascript throws "....dom undefined" exception.

(defn trim-the-tweets
  "Trim the tweets list to max tweets by deleting from the bottom."
  [max]
  (let [tweets (clojure.browser.dom/get-element "tweets")
        n (.-length (.-childNodes tweets))]
    (when (> n max)
      (.removeChild tweets (.-lastChild tweets))
      (recur max))))

(defn add-tweets-element
  "Add tweet-element to the top of the tweets list."
  [tweets-element]
  (clojure.browser.dom/insert-at
   (clojure.browser.dom/get-element "tweets")
   tweets-element 0)
  (trim-the-tweets 4))

(defn add-tweets
  "XhrIo.send callback: Add the tweets from the XhrIo event e to the
  top of the list of tweets displayed."
  [e]
  (try
    (let [tweets (.getResponseText (.-target e))
          tweets-element (clojure.browser.dom/ensure-element tweets)]
      (add-tweets-element tweets-element))
    (catch js/Object x
      (.log js/console x))))

(defn refresh-tweets
  "setInterval callback: Refresh the list of tweets from the server."
  []
  (.send goog.net.XhrIo "/ticker/feed/tweets" add-tweets))

(js/setInterval refresh-tweets 500)
