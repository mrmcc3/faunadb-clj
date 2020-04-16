(ns mrmcc3.fauna.json
  "Support for encoding FQL data as JSON"
  {:author "Michael McClintock"}
  (:require
    [clojure.data.json :as json])
  (:import
    (java.util Date Base64)
    (java.time Instant LocalDate)
    (java.time.format DateTimeFormatter)))

(def b64-enc (Base64/getUrlEncoder))
(def b64-dec (Base64/getUrlDecoder))
(def formatter (DateTimeFormatter/ISO_OFFSET_DATE_TIME))

;; support decoding FQL special types.
;; This wont work if they're not in a object!

(defn read-value-fn [_ v]
  (cond
    (not (map? v)) v
    (contains? v "@bytes")
    (.decode b64-dec (get v "@bytes"))
    (contains? v "@ts")
    (Instant/from (.parse formatter (get v "@ts")))
    (contains? v "@date")
    (LocalDate/parse (get v "@date"))
    :else v))

(defn key-fn [k]
  (if (.startsWith k "@") k (keyword k)))

(defn read-str [s]
  (json/read-str s :key-fn key-fn :value-fn read-value-fn))

;; because of JSONs lack of many literal data types
;; FQL chooses to overload JSON objects
;; special types are objects with keys prefixed by @
;; query ops are represented as objects directly
;; actual map data is nested {:object data}

;; support encoding FQL special types
(extend-protocol json/JSONWriter
  (Class/forName "[B")
  (-write [o out] (json/-write {"@bytes" (.encodeToString b64-enc o)} out))
  Date
  (-write [o out] (json/-write {"@ts" (-> o .toInstant .toString)} out))
  Instant
  (-write [o out] (json/-write {"@ts" (.toString o)} out))
  LocalDate
  (-write [o out] (json/-write {"@date" (.toString o)} out)))

;; nest maps if :op metadata doesn't exist. have to tag all query ops
(defn write-value-fn [_ v]
  (cond
    (-> v meta :op) v
    (map? v) {:object (with-meta v {:op true})} ;; only nest one level
    :else v))

(defn write-str [data]
  (json/write-str data :value-fn write-value-fn))

