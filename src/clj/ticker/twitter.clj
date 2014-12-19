(ns ticker.twitter
  (:require [clojure.pprint]
            [clojure.data.json]
            [http.async.client]
            [ticker.feed]
            [ticker.state]
            [twitter.oauth]
            [twitter.callbacks]
            [twitter.callbacks.handlers]
            [twitter.api.restful]
            [twitter.api.streaming])
  (:import [twitter.callbacks.protocols AsyncStreamingCallback]
           [java.util.concurrent CancellationException]))

(def
  ^{:doc
    "The OAuth crendentials for the 'tbl3rd' Twitter application
     registered to the Twitter user with screenname 'tbl3rd'.
     Not really keeping any secrets here ... *sigh*"}
  oauth-tbl3rd
  {:application         "Twitter Ticker Demo"
   :consumer-key        "......................"
   :consumer-secret     ".........................................."
   :access-token        ".................................................."
   :access-token-secret "..........................................."
   :callback-url        "http://github.com/tbl3rd/"
   :request-token-url   "https://api.twitter.com/oauth/request_token"
   :authorize-url       "https://api.twitter.com/oauth/authorize"
   :access-token-url    "https://api.twitter.com/oauth/access_token"
   :digest-algorithm    :hmac-sha1      ; or :plaintext
   :access-level        :readonly})

(defn wrap-oauth-creds
  "Return Twitter OAuth credentials bound from cred-map."
  [cred-map]
  (apply twitter.oauth/make-oauth-creds
         (map cred-map [:consumer-key :consumer-secret
                        :access-token :access-token-secret])))

(defn ensure-an-at
  "Return an sn that begins with @ if there isn't one already."
  [sn]
  (if (= (first sn) (first "@")) sn (str "@" sn)))

(defn screen-name-to-user
  "Call Twitter to map the @ScreenName sn to info on its user.
   Ensure the :screen value begins with '@'."
  [sn]
  (try (let [resp (twitter.api.restful/show-user
                   :oauth-creds (wrap-oauth-creds oauth-tbl3rd)
                   :params {:screen-name (ensure-an-at sn)})
             body (:body resp)]
         (println (pr-str body))
         (let [sn (:screen_name body)]
           {:id     (:id_str body)
            :avatar  (:profile_image_url body)
            :name   (:name body)
            :screen (ensure-an-at sn)}))
       (catch Exception e
         (println (format "Caught %s in (screen-name-to-user '%s')" e sn))
         nil)))

(defn make-follow-query
  "Return a Twitter follow query string for statuses/filter from users."
  [users]
  (clojure.string/join "," (map :id users)))

;; (defn on-body [response baos]
;;   "response has the status and headers from Twitter"
;;   "baos is a ByteArrayOutputStream containing a chunk of the stream")
;;
;; (defn on-failure [response]
;;   "response has the failure headers with some HTTP error status.")
;;
;; (defn on-exception [response throwable]
;;   "response is a potentially incomplete response (up to the exception)."
;;   "throwable is the exception implementing the Throwable interface.")
;;
;; POST to public statuses/filter stream using the follow parameter
;; which takes a comma-separated list of user id_str values.
;;
;; AsyncStreamingCallback is 3 [on-body on-failure on-exception]
;; callbacks.
;;
(defn make-filter-stream
  "Start filtering a Twitter stream returning its response object."
  [users & {:keys [on-body on-failure on-exception]}]
  (twitter.api.streaming/statuses-filter
   :timeout (* 60 60 1000)              ; 1 hour in milliseconds
   ;; :timeout 15000                       ; 15 seconds in milliseconds
   :params {:follow (make-follow-query users)}
   :oauth-creds (wrap-oauth-creds oauth-tbl3rd)
   :callbacks (AsyncStreamingCallback. on-body on-failure on-exception)))

(def bogus-filter-stream ^{:cancel (constantly true)}
  ^{:doc "Some thing to safely ((:cancel (meta thing)))."}
  ['call 'restart-filter-stream-in-agent])

(def ^{:doc "Agent wrapping a Twitter streaming API response."}
  ^:private stream-agent (agent bogus-filter-stream))

(defn stream-on-body
  "Dump a tweet from baos onto feed-agent.  Twitter likes to send an
  occasional return/newline sequence to keep the connection alive if
  there are no tweets in the pipeline.  Of course that is not valid
  JSON and is sent on Twitter's JSON streams with a 200 status, so
  we have to ignore those responses here or die trying to parse them.

  Also have to deal with 'admin logout' disconnects and 'Easy there,
  Turbo. Enhance your calm' responses from Twitter logged as 'strange
  chunk's and not processed.
"
  [response-ignored baos]
  (let [strbaos (str baos)]
    (try
      (if (= strbaos (str \return \newline))
        (println "stream-on-body read a keepalive response from Twitter.")
        (let [chunk (clojure.data.json/read-json strbaos)
              user (:user chunk)
              tweet {:name (:name user)
                     :text (:text chunk)
                     :screen (:screen_name user)
                     :avatar (:profile_image_url user)}]
          (if (:avatar tweet)           ; Skip unless response is a tweet.
            (ticker.feed/feed-add-tweet tweet)
            (println "stream-on-body strange chunk" chunk))))
      (catch Throwable x
        (println
         (format "stream-on-body caught %s reading '%s'" x strbaos))))))

(defn callback-on-failure
  "Dump a parsable failure response, but this almost never happens."
  [response]
  (prn (twitter.callbacks.handlers/response-return-everything response)))

;; FIXME: I don't know how to work around this yet.
;;
(declare make-default-filter-stream-life-is-short)

(defn restart-filter-stream-in-agent
  "Call this on a stream-agent to restart its stream."
  [stream-agent users]
  (println "restart-filter-stream-in-agent")
  (send stream-agent
        (fn [stream]
          ((:cancel (meta stream)))
          (let [new-stream (make-default-filter-stream-life-is-short users)]
            new-stream))))

(defn stream-on-exception
  "Restart stream-agent after dumping something recognizable."
  [response throwable]
  (if (instance? CancellationException throwable)
    (println "stream-on-exception handling CancellationException.")
    (restart-filter-stream-in-agent stream-agent)))

(defn make-default-filter-stream-life-is-short
  "Close over the usual callbacks because I don't care now."
  [users]
  (make-filter-stream users
                      :on-body stream-on-body
                      :on-failure callback-on-failure
                      :on-exception stream-on-exception))

;; Restart the stream whenever the set of followed users changes,
;; and cancel it when there are no users to follow.
;;
(ticker.state/watch-followed-user-ids
 (fn [users]
   (if (empty? users)
     ((:cancel (meta @stream-agent)))
     (restart-filter-stream-in-agent stream-agent users))))

;; ((:cancel (meta @stream-agent)))
