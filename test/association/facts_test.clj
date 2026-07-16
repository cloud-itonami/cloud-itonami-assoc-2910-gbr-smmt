(ns association.facts-test
  (:require [clojure.test :refer [deftest is]]
            [association.facts :as facts]))

(deftest smmt-has-spec-basis
  (let [sb (facts/spec-basis "smmt")]
    (is (= 2 (count sb)))
    (is (every? #(= "2910" (:association-rule/isic %)) sb))
    (is (every? #(= "GBR" (:association-rule/country %)) sb))))

(deftest unknown-association-has-no-spec-basis
  (is (nil? (facts/spec-basis "vda")))
  (is (nil? (facts/spec-basis "zzz"))))

(deftest coverage-is-honest
  (let [c (facts/coverage ["smmt" "vda"])]
    (is (= 2 (:requested c)))
    (is (= 1 (:covered c)))
    (is (= ["vda"] (:missing-associations c)))))

(deftest by-topic-filters
  (is (= ["smmt.founding-1902"]
         (mapv :association-rule/id (facts/by-topic "smmt" :governance))))
  (is (empty? (facts/by-topic "smmt" :labor)))
  (is (empty? (facts/by-topic "vda" :governance))))
