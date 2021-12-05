(ns jcpsantiago.bulgogi
  " À-la-carte transformations of data, useful in ML systems.")


(defn- resolved-features
  [features -ns]
  (->> features
       (map #(let [sym (symbol %)]
               (ns-resolve (find-ns -ns) sym)))))


(defn- transformed
  [input-data fns]
  (pmap #(% input-data) fns))


(defn preprocessed
  "
  Takes a request map with keys :input-data and :features.
  :input-data is map with the actual data needed to calculate features;
  :features is a vector of strings with the names of the functions requested.
  {:input-data {:current-amount 700
                :email \"squadron42@starfleet.ufp\"
                :items [{:brand \"Foo Industries\" :value 1234}
                        {:brand \"Baz Corp\" :value 35345}]}
   :features [\"n-digits-in-email-name\" 
              \"contains-risky-item\"
              \"diff-eur-previous-order\"]}
  Looks for the features in the namespace and applies them to the input-data
  in parallel. Returns a map of feature-keys and feature-values.
  "
  [req -ns]
  (let [{:keys [input-data features]} req
        fns (resolved-features features -ns)
        fn-ks (map keyword features)]
    (->> (transformed input-data fns)
         (zipmap fn-ks))))
