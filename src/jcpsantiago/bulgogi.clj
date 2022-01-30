(ns jcpsantiago.bulgogi
  " À-la-carte transformations of data, useful in ML systems.")

(defn- all-special-functions
  "Returns a map of feature-name -> feature-var"
  ([fn-type]
   (all-special-functions fn-type true))
  ([fn-type namespaced?]
   (->> (all-ns)
        (filter #(fn-type (meta %)))
        (map (fn [ns] (update-keys (ns-publics ns) #(if namespaced? (symbol (str ns) (str %)) %))))
        (apply merge-with #(throw (Exception. (str "Conflict between: " %1 " and :" %2)))))))


(defn all-features
  "Returns a map of feature-name -> fn-var"
  []
  (all-special-functions ::features false))


(defn all-coeffects
  "Returns a map of coeffect-name -> fn-var"
  []
  (all-special-functions ::coeffects))


(defn- resolved-features
  [features]
  (let [all (all-features)]
    (->> features
         (map #(get all (symbol %))))))


(defn- transformed
  [input-data fns]
  (pmap #(% input-data) fns))


(defn- enriched
  [input-data fns]
  (if (empty? fns)
    input-data
    (->> fns
         (pmap #(% input-data))
         (apply merge))))


(def ^:private memoized-features
  (memoize resolved-features))


(def ^:private memoized-coeffects
  (memoize (fn [fn-vars]
             (let [all (all-coeffects)]
               (->> fn-vars
                    (map #(:bulgogi/coeffect (meta %)))
                    (remove nil?)
                    (map #(all (symbol %))))))))


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
  [req]
  (let [{:keys [input-data features]} req
        fns (memoized-features features)
        coeffects (memoized-coeffects fns)
        fn-ks (map keyword features)]
    (->> (transformed (enriched input-data coeffects) fns)
         (zipmap fn-ks))))


(defn- feature-conflicts? []
  (->> (all-features)       
       (mapcat keys)
       distinct?))

(all-features)

(comment
  (all-features)
  (feature-conflicts?))