(ns mrmcc3.fauna.json
  "Support for encoding FQL data as JSON."
  {:author "Michael McClintock"}
  (:require
    [clojure.data.json :as json]
    [clojure.walk :as walk])
  (:import
    (java.util Date Base64)
    (java.time Instant LocalDate)
    (java.time.format DateTimeFormatter)))

;; FQL fundamentally deals with richer information then JSON provides.
;; This is a flaw in JSON and means that we have to overload JSON types
;; (the canonical FQL impl. chooses to overload JSON objects).

;; The overloading is needed to support

;; 1. Additional primitives. That is FQL special types:
;; Byte (byte[]) -> {"@bytes" "base64 url encoded string"}
;; Date (LocalDate) -> {"@date" "1970-01-01"}
;; Timestamp (Instant) -> {"@ts" "1970-01-01T00:00:00Z"}

;; 2. FQL function calls. These are represented as plain objects
;; where the keys act as reserved words when decoded by Fauna.
;; (new-id) -> {"new_id" null}

;; 3. Actual data. Encoding maps as plain objects is a problem as it runs the
;; risk of being interpreted as a FQL function call unintentionally. They're
;; handled by nesting.
;; {"new_id" "oops"} -> {"object" {"new_id" "oops"}}

;; clojure.data.json only supports custom types in objects. so it won't work
;; cheshire/jsonista/jackson can handle it and performance is very good
;; but it's low level. We can choose this route later for now let's
;; introduce a postwalk step on both encoding and decoding

(defn encode-fql [v]
  (cond
    (bytes? v)
    {"@bytes" (.encodeToString (Base64/getUrlEncoder) v)}
    (instance? Instant v)
    {"@ts" (.toString v)}
    (instance? LocalDate v)
    {"@date" (.toString v)}
    (-> v meta :op) v
    (map? v)
    {:object (with-meta v {:op true})}
    :else v))

(defn write-str [data]
  (json/write-str (walk/postwalk encode-fql data)))

;; only support special types for now. not sure what to do here yet
(defn decode-fql [v]
  (cond
    (not (map? v)) v
    (contains? v "@bytes")
    (.decode (Base64/getUrlDecoder) ^String (get v "@bytes"))
    (contains? v "@ts")
    (Instant/from
      (.parse (DateTimeFormatter/ISO_OFFSET_DATE_TIME) (get v "@ts")))
    (contains? v "@date")
    (LocalDate/parse (get v "@date"))
    :else v))

(defn key-fn [k]
  (if (.startsWith k "@") k (keyword k)))

(defn read-str [s]
  (walk/postwalk decode-fql (json/read-str s :key-fn key-fn)))

