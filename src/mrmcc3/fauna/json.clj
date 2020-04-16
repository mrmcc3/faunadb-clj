(ns mrmcc3.fauna.json
  "Support for FaunaDB special types in clojure.data.json"
  (:require
    [clojure.data.json :as json])
  (:import
    (java.util Date Base64)
    (java.time Instant LocalDate)
    (java.time.format DateTimeFormatter)))

(def b64-enc (Base64/getUrlEncoder))
(def b64-dec (Base64/getUrlDecoder))
(def formatter (DateTimeFormatter/ISO_OFFSET_DATE_TIME))

(defn value-fn [_ v]
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
  (json/read-str s :key-fn key-fn :value-fn value-fn))

(extend-protocol json/JSONWriter
  (Class/forName "[B")
  (-write [o out]
    (json/-write {"@bytes" (.encodeToString b64-enc o)} out))
  Date
  (-write [o out]
    (json/-write {"@ts" (str (.toInstant o))} out))
  Instant
  (-write [o out]
    (json/-write {"@ts" (str o)} out))
  LocalDate
  (-write [o out]
    (json/-write {"@date" (str o)} out)))

(defn write-str [data]
  (json/write-str data))



