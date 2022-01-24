(ns jcpsantiago.bulgogi-test
  (:require
    [clojure.string :as s]
    [clojure.test :refer :all]
    [jcpsantiago.bulgogi :as SUT]))


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


(defn n-digits-in-email-name
  "Number of digits in the email name"
  [{email :email}]
  (->> (email-name email)
       (re-seq #"\d")
       count))


(defn n-chars-in-email-name
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


(defn ^{:bulgogi/coeffect ::added} needs-coeffect
  [{data ::added}]
  data)


(defn added
  [_]
  {::added "some data"})


(defn email-name-as-coeffect
  [{email :email}]
  {::email-name-as-coeffect (-> email
                                s/lower-case
                                (s/replace-first #"@.*" ""))})


(defn ^{:bulgogi/coeffect ::email-name-as-coeffect} n-chars-in-email-name-w-coeffect
  [{email-name-as-coeffect ::email-name-as-coeffect}]
  (count email-name-as-coeffect))


(def test-input
  {:current-amount 700
   :previous-amount 400
   :email "squadron42@starfleet.ufp"
   :items [{:brand "Foo Industries" :value 1234}
           {:brand "Baz Corp" :value 35345}]})


(deftest preprocessed
  (testing "basics"
    (is (= {:contains-risky-item 1
            :diff-eur-previous-order 300
            :n-digits-in-email-name 2}
           (SUT/preprocessed {:input-data test-input
                              :features ["n-digits-in-email-name"
                                         "contains-risky-item"
                                         "diff-eur-previous-order"]}
                             'jcpsantiago.bulgogi-test))))
  (testing "coeffect"
    (is (= {:needs-coeffect "some data"}
           (SUT/preprocessed {:input-data test-input
                              :features ["needs-coeffect"]}
                             'jcpsantiago.bulgogi-test))))
  (testing "coeffect complex feature"
    (is (= {:n-chars-in-email-name-w-coeffect 10}
           (SUT/preprocessed {:input-data test-input
                              :features ["n-chars-in-email-name-w-coeffect"]}
                             'jcpsantiago.bulgogi-test)))))
