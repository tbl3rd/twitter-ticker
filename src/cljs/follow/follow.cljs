(ns ticker.browser.follow
  (:require [clojure.browser.dom :as clojure.browser.dom]
            [clojure.browser.event :as clojure.browser.event]
            [clojure.string :as clojure.string]))

;; The :as is required for ClojureScript.
;; Otherwise Closure's Javascript throws "....dom undefined" exception.

;; Extract some elements of interest by their IDs.
;;
(def follow-submit (clojure.browser.dom/get-element "follow-submit"))
(def add-field (clojure.browser.dom/get-element "add-field"))
(def add-feedback (clojure.browser.dom/get-element "add-feedback"))
(def default-feedback (clojure.browser.dom/get-element "default-feedback"))

(defn restore-default-feedback
  "Restore the feedback to the default from server."
  [e]
  (let [feedback (clojure.browser.dom/get-value default-feedback)]
    (clojure.browser.dom/set-text add-feedback feedback))
  e)

(defn judge-screenname
  "Update feedback and Follow button according to current @ScreenName sn."
  [e]
  (let [sn (clojure.browser.dom/get-value add-field)
        ok "OK, that's more like it ..."
        feedback (cond (empty? sn)
                       "Twitter names look like '@ScreenName'."
                       (some #(clojure.string/blank? (str %)) sn)
                       "Only one @ScreenName at a time -- no blanks."
                       (some #(= (first "@") (first %)) (rest sn))
                       "Too many '@'s in the @ScreenName ..."
                       :else ok)]
    (clojure.browser.dom/set-properties
     follow-submit {"disabled" (not= ok feedback)})
    (clojure.browser.dom/set-text add-feedback feedback))
  e)

(defn ensure-an-at
  "Return an sn that begins with @ if there isn't one already."
  [sn]
  (if (= (first sn) (first "@")) sn (str "@" sn)))

(defn advise-on-following
  "Update feedback according to add-field and follow-submit button.
   The (str ...) call works around a lack of Clojure's (format ...)"
  [e]
  (let [sn (clojure.browser.dom/get-value add-field)]
    (if (not (. follow-submit -disabled))
      (clojure.browser.dom/set-text
       add-feedback
       (str "Click Follow to follow " (ensure-an-at sn) ".")))
    e))

(clojure.browser.event/listen add-field :focus judge-screenname)
(clojure.browser.event/listen add-field :blur advise-on-following)
(clojure.browser.event/listen add-field :keyup judge-screenname)
