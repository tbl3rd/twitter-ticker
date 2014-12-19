(ns ^{:doc
      "The state of the server.  Now just a map of Twitter
       screen_name to info for the users to follow."}
  ticker.state)

;; Seed this with
;;
;; {"@BarackObama"
;;  {:id "813286",
;;   :avatar "http://a0.twimg.com/profile_images/2325704772/wrrmef61i6jl91kwkmzq_normal.png",
;;   :name "Barack Obama",
;;   :screen "@BarackObama"},
;;  "@twitterapi"
;;  {:id "6253282",
;;   :avatar "http://a0.twimg.com/profile_images/2284174872/7df3h38zabcvjylnyfe3_normal.png",
;;   :name "Twitter API",
;;   :screen "@twitterapi"}}
;;
;; for testing without the controller client.
;;
(def ^{:private true
       :doc "Map of a :screen to a user {:screen :name :avatar} map."}
  users (atom {}))

(defn forget-all-screennames
  "Forget all the screennames such that the feed is following nobody."
  []
  (println "forget-all-screennames")
  (reset! users {}))

(defn forget-screenname
  "Forget the user with screename sn."
  [sn]
  (println
   (format "forget-screenname {%s %s}"
           (pr-str sn) (pr-str (@users sn))))
  (swap! users dissoc sn))

(defn remember-user
  "Remember user indexed by @ScreenName."
  [user]
  (println (format "remember-user %s." (pr-str user)))
  (swap! users assoc (:screen user) user))

(defn all-users
  "Return a sequence of all screennames."
  []
  (let [allusers @users
        sks (sort (keys allusers))]
    (map allusers sks)))

(defn all-userids
  "Return a sequence of user ID strings for all users."
  []
  (map :id (vals @users)))

(defn watch-followed-user-ids
  "Call (f (vals users)) when the followed users changes."
  [f]
  (add-watch users f
             (fn [key-ignored ref-ignored old-state new-state]
               (if (not= old-state new-state)
                 (f (vals new-state))))))
