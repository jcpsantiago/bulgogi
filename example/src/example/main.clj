(ns example.main
  (:gen-class)
  (:require
    [clojure.string :as s]
    [compojure.core :refer [defroutes POST]]
    [jcpsantiago.bulgogi :as b]
    [next.jdbc :as jdbc]
    [next.jdbc.sql :as sql]
    [org.httpkit.server :as http]
    [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
    [ring.util.response :refer [response]]
    [tick.core :as t]))


;; --- Database connection and migrations ----
(def postgresql-host
  {:host "0.0.0.0"
   :user "postgres"
   :dbtype "postgresql"})


(def db postgresql-host)
(def ds (jdbc/get-datasource db))


(defn migrated?
  [table]
  (-> (sql/query
        ds [(str "select * from information_schema.tables "
                 "where table_name='" table "'")])
      count
      pos?))


(defn migrate
  []
  (when (not (migrated? "calculation_history"))
    (jdbc/execute!
      ds
      ["
       CREATE TABLE calculation_history (
         id SERIAL PRIMARY KEY,
         trigger_id varchar(255),
         feature_name varchar(255),
         feature_value varchar(255),
         created_at timestamp DEFAULT CURRENT_TIMESTAMP
      )"])))


;; -- some features --
(defn email-name
  "Lower-cased name of an email address (the bit before @)"
  [{email :email}]
  (-> email
      s/lower-case
      (s/replace-first #"@.*" "")))


(defn n-digits-in-email-name
  "Number of digits in the email name"
  [input-data]
  ;; we get dependency management for free because everything is just functions
  (->> (email-name input-data)
       (re-seq #"\d")
       count))


(defn n-chars-in-email-name
  "Number of characters in the email name i.e. length of the email name"
  [input-data]
  (-> (email-name input-data)
      count))


(defn hour
  [{created_at :created_at}]
  (t/hour (t/instant created_at)))


(comment
  ;; try it in the REPL 
  (def req
    "Example request. :input-data should be as flat as possible"
    {:trigger-id "42"
     :input-data {:current-amount 700
                  :previous-amount 400
                  :email "squadron42@starfleet.ufp"
                  :created_at "2021-12-05T10:34:33Z"
                  :items [{:brand "Foo Industries" :value 1234}
                          {:brand "Baz Corp" :value 35345}]}
     :features ["hour" 
                "n-chars-in-email-name"
                "n-digits-in-email-name"]})
  (def res
    (-> req
        (b/preprocessed 'example.main))))


(defroutes routes
  (POST "/preprocess" req
        (let [body (:body req)
              trigger-id (:trigger-id body)
              ;; FIXME there must be a nicer way to pass the namespace
              preprocessed (b/preprocessed body 'example.main)]
          (future (doseq [[k v] preprocessed]
                    (sql/insert!
                      ds :calculation_history
                      {:trigger_id trigger-id
                       :feature_name (name k)
                       :feature_value v})))
          (response preprocessed))))


(defn -main
  [& _]
  (println "Migrating database..")
  (migrate)
  (println "Done!")
  (println "Starting server on port 4242")
  (http/run-server
    (-> routes
        (wrap-json-body {:keywords? true})
        wrap-json-response)
    {:port 4242}))
