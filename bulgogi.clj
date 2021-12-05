(ns bulgogi.main
  "
  A bulgogi prototype. Not seasoned yet. ⚠️
  
  An exploration on how a simple feature engineering system could look like in Clojure.
  
  Features are defined as pure functions and take an input-data map as input:
  {:input-data {:email \"hackermann@unprotected.com\"
                :current-amount 3455
                :previous-amount 2344}}
  "
  (:require
    [clojure.string :as s]))


;; ---- utilities -----
(defn boolean->int
  "Cast a boolean to 1/0 integer indicator"
  [b]
  (when boolean? b
        (if (true? b) 1 0)))


(defn email-name
  "Lower-cased name of an email address (the bit before @)"
  [email]
  (-> email
      s/lower-case
      (s/replace-first #"@.*" "")))


;; ---- features -----
;; Features are variables used in Machine Learning models. They're like fn args, if a model was just
;; a function: `(model feature1 feature2 ...)`.
;; Becacuse of referential transparency, here they are just pure functions. 
;; In this prototype, they all follow the same contract taking
;; an input-map as input, extracting what they need and returning some value.
;; The following features are representative of the types I've used in production.

(defn n-digits-in-email-name
  "Number of digits in the email name"
  [{email :email}]
  ;; we get dependency management for free because everything is just functions
  (->> (email-name email)
       (re-seq #"\d")
       count))


(defn n-chars-in-email-mail
  "Number of characters in the email name i.e. length of the email name"
  [{email :email}]
  (-> (email-name email)
      count))


(defn diff-eur-previous-order
  "Difference in euros between the current order and the previous one."
  [{current-amount :current-amount previous-amount :previous-amount}]
  (- current-amount previous-amount))


(defn risky-item?
  "Boolean depending on whether an item is risky or not"
  [{brand :brand}]
  (->> brand
       s/lower-case
       (re-seq #"baz corp")
       some?
       boolean->int))


(defn contains-risky-item
  "Indicator 1/0 depending on whether a risky item is present in the cart"
  [{items :items}]
  (->> items
       (map #(risky-item? %))
       (some #(= 1 %))
       boolean->int))


;; ---- main functions part of the actual infrastructure, not features ----
(defn preprocessed
  "
  Takes a request map with keys :input-data and :features.
  The first key contains an input-data map with the actual data needed to calculate features;
  The second key contains a vector with the names of the features requested.
  Looks for the features (aka functions) in the namespace and applies them to the input-data
  in parallel. 
  
  Returns a map of feature-keys and feature-values.
  "
  [req]
  (let [{:keys [input-data features]} req
        fns (->> features
                 (map #(-> % symbol resolve)))
        fn-ks (map keyword features)]
    (->> (pmap #(% input-data) fns)
         (zipmap fn-ks))))


(defn response
  "Bundles the calculated features into a consumable response"
  [input-map preprocessed-map]
  (->> preprocessed-map
       (assoc {:request input-map} :preprocessed)))


(comment
  ;; try it in the REPL 
  (def req
    "Example request. :input-data should be as flat as possible"
    {:input-data {:current-amount 700
                  :previous-amount 400
                  :email "squadron42@starfleet.ufp"
                  :items [{:brand "Foo Industries" :value 1234}
                          {:brand "Baz Corp" :value 35345}]}
     :features ["n-digits-in-email-name" 
                "contains-risky-item"
                "diff-eur-previous-order"]}))


(let [;; req (edn/read *in*)
      res (->> req
               preprocessed
               (response req))]
  (future
    (println "Saving response to file...")
    (spit "bulgogi_response.edn" res :append true))
  res)
