(ns mrmcc3.fauna.http
  "Http Client for making requests to FaunaDB. Uses  java.net.http.HTTPClient
  which was introduced in Java 11"
  (:require
    [mrmcc3.fauna.json :as json])
  (:import
    (java.net.http
      HttpClient
      HttpClient$Version
      HttpRequest
      HttpRequest$BodyPublishers
      HttpResponse
      HttpResponse$BodyHandlers)
    (java.net URI)
    (java.time Duration)
    (java.util Base64)
    (java.util.function Function)))

(def fauna-uri (URI. "https://db.fauna.com"))
(def last-seen-txn (atom 0))

;; request

(defn basic-auth [secret]
  (as-> secret $
    (str $ ":")
    (.getBytes $ "US-ASCII")
    (.encode (Base64/getEncoder) ^bytes $)
    (String. ^bytes $ "US-ASCII")
    (str "Basic " $)))

(defn json-pub [data]
  (-> data json/write-str HttpRequest$BodyPublishers/ofString))

(defn http-request [{:keys [secret timeout data]}]
  (let [builder (HttpRequest/newBuilder fauna-uri)]
    (.method builder "POST" (json-pub data))
    (.header builder "Authorization" (basic-auth secret))
    (when-let [last-seen @last-seen-txn]
      (.header builder "X-Last-Seen-Txn" (str last-seen)))
    (.timeout builder (Duration/ofMillis (or timeout 10000)))
    (.build builder)))

;; response

(defn largest [last-seen txn-time]
  (if (< last-seen txn-time)
    txn-time
    last-seen))

(defn x-txn-time [^HttpResponse response]
  (let [opt (.firstValueAsLong (.headers response) "x-txn-time")]
    (and (.isPresent opt) (.getAsLong opt))))

(defn json-response [^HttpResponse response]
  (when-let [t (x-txn-time response)]
    (swap! last-seen-txn largest t))
  (json/read-str (.body response)))

;; client

(defn http-client [{:keys [timeout]}]
  (-> (HttpClient/newBuilder)
      (.connectTimeout (Duration/ofMillis (or timeout 10000)))
      (.version HttpClient$Version/HTTP_1_1)
      (.build)))

(defn client [{:keys [secret] :as opts}]
  {:http-client (http-client opts)
   :secret      (or secret (System/getenv "FAUNADB_SECRET"))})

;; invoke

(defn invoke [client request]
  (-> (:http-client client)
      (.send (http-request (merge client request))
             (HttpResponse$BodyHandlers/ofString))
      (json-response)))

(defn invoke-async [client request]
  (-> (.sendAsync
        (:http-client client)
        (http-request (merge client request))
        (HttpResponse$BodyHandlers/ofString))
      (.thenApply
        (reify Function
          (apply [_ response]
            (json-response response))))))

