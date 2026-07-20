(ns association-facts-test
  (:require [clojure.java.io :as io] [clojure.java.shell :as shell]
            [clojure.test :refer [deftest is testing]]
            [kotoba.compiler.core :as compiler] [kotoba.compiler.ir :as ir]))
(def source (slurp "src/association_facts.kotoba"))
(defn call [kir function & args] (ir/execute kir function (vec args)))
(defn present [option] (when (second option) (nth option 2)))
(def fields ["id" "title" "association" "isic" "country" "kind" "url"
             "url-provenance" "established-date" "retrieved-at"])
(def expected
  [{"id" "smmt.founding-1902" "title" "SMMT founding (History)"
    "association" "smmt" "isic" "2910" "country" "GBR" "kind" "governance-program"
    "url" "https://www.smmt.co.uk/about/history/" "url-provenance" "official-smmt-co-uk"
    "established-date" "1902-07-22" "retrieved-at" "2026-07-16"}
   {"id" "smmt.first-exhibition-1903"
    "title" "First SMMT motor exhibition at Crystal Palace (History)"
    "association" "smmt" "isic" "2910" "country" "GBR" "kind" "governance-program"
    "url" "https://www.smmt.co.uk/about/history/" "url-provenance" "official-smmt-co-uk"
    "established-date" "1903" "retrieved-at" "2026-07-16"}])

(deftest reference-preserves-fields-date-precision-and-topics
  (let [kir (:kir (compiler/compile-source source :js-kotoba-v1))
        observed (mapv (fn [i] (into {} (map (fn [f] [f (present (call kir 'entry-field "smmt" i f))]) fields))) [0 1])]
    (is (= expected observed))
    (is (= ["1902-07-22" "1903"] (mapv #(present (call kir 'entry-field "smmt" % "established-date")) [0 1])))
    (is (= [1 1] (mapv #(call kir 'topic-count "smmt" %) [0 1])))
    (is (= ["governance" "commerce"] (mapv #(present (call kir 'topic "smmt" % 0)) [0 1])))
    (is (= "smmt.first-exhibition-1903" (present (call kir 'by-topic-id "smmt" "commerce" 0))))
    (is (= #{} (set (:effects kir))))
    (testing "unknown values and invalid indexes fail closed"
      (is (zero? (call kir 'entry-count "vda")))
      (is (nil? (present (call kir 'entry-field "smmt" -1 "id"))))
      (is (nil? (present (call kir 'entry-field "smmt" 2 "id"))))
      (is (nil? (present (call kir 'entry-field "smmt" 0 "unknown"))))
      (is (nil? (present (call kir 'topic "smmt" 1 1))))
      (is (zero? (call kir 'by-topic-count "smmt" "labor")))
      (is (nil? (present (call kir 'by-topic-id "smmt" "commerce" 1)))))))

(defn compiler-root []
  (nth (iterate #(.getParent ^java.nio.file.Path %)
                (java.nio.file.Path/of (.toURI (io/resource "kotoba/compiler/core.clj")))) 4))
(defn base64 [value] (.encodeToString (java.util.Base64/getEncoder) value))
(deftest restricted-javascript-and-typed-wasm-conform-semantically
  (let [javascript (compiler/compile-source source :js-kotoba-v1)
        wasm (compiler/compile-source source :wasm32-browser-kotoba-v1)
        js64 (base64 (.getBytes ^String (:source javascript) "UTF-8")) wasm64 (base64 ^bytes (:bytes wasm))
        probe (shell/sh "node" "--input-type=module" "-e"
                (str "import(process.argv[1]).then(async host=>{const j=await import('data:text/javascript;base64," js64 "');"
                     "const w=await host.instantiateKotoba(Buffer.from(process.argv[2],'base64'));const run=x=>{"
                     "if(x['entry-count']('smmt')!==2n||x['entry-field']('smmt',0n,'established-date')[2]!=='1902-07-22'||x['entry-field']('smmt',1n,'established-date')[2]!=='1903')throw Error('dates');"
                     "if(x['topic-count']('smmt',0n)!==1n||x['topic']('smmt',1n,0n)[2]!=='commerce')throw Error('topics');"
                     "if(x['by-topic-id']('smmt','governance',0n)[2]!=='smmt.founding-1902'||x['topic']('smmt',1n,1n)[1]!==false)throw Error('query');};"
                     "run(j.instantiateKotoba({}));run(w.instance.exports);}).catch(e=>{console.error(e);process.exit(99)})")
                (.toString (.toUri (.resolve (compiler-root) "runtime/browser-host.mjs"))) wasm64)]
    (is (zero? (:exit probe)) (str (:out probe) (:err probe)))))
(deftest production-source-authority
  (is (= ["src/association_facts.kotoba"]
         (->> (file-seq (io/file "src")) (filter #(.isFile %)) (map str) sort vec))))
