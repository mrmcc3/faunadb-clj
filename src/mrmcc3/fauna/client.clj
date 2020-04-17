(ns mrmcc3.fauna.client
  "Http Client for making requests to FaunaDB. Uses java.net.http.HTTPClient
  which was introduced in Java 11."
  {:author "Michael McClintock"}
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

(defn http-request [{:keys [secret timeout data state]}]
  (let [builder (HttpRequest/newBuilder (URI. "https://db.fauna.com"))]
    (.method builder "POST" (json-pub data))
    (.header builder "Authorization" (basic-auth secret))
    (.header builder "X-FaunaDB-API-Version" "2.11.0")
    (when-let [t (:last-seen @state)]
      (.header builder "X-Last-Seen-Txn" (str t)))
    (.timeout builder (Duration/ofMillis (or timeout 10000)))
    (.build builder)))

;; response

(defn update-client-state
  [{:keys [last-seen read-ops write-ops query-out]
    :or   {last-seen 0 read-ops 0 write-ops 0 query-out 0}}
   {:keys [x-txn-time x-read-ops x-write-ops x-query-bytes-out]}]
  {:last-seen (if (< last-seen x-txn-time) x-txn-time last-seen)
   :read-ops  (+ read-ops x-read-ops)
   :write-ops (+ write-ops x-write-ops)
   :query-out (+ query-out x-query-bytes-out)})

(def long-headers
  #{:x-query-bytes-in :x-query-bytes-out :x-read-ops :x-write-ops
    :x-storage-bytes-read :x-storage-bytes-write :x-query-time
    :x-compute-ops :x-txn-retries :x-txn-time})

(defn header [[k [v]]]
  (let [k' (keyword k)]
    [k' (if (long-headers k') (Long/parseLong v) v)]))

(defn headers-map [^HttpResponse response]
  (into {} (map header) (-> response (.headers) (.map))))

(defn json-response [^HttpResponse response {:keys [state]}]
  (let [headers  (headers-map response)
        response (with-meta
                   (json/read-str (.body response))
                   {:headers headers})]
    (swap! state update-client-state headers)
    (if (:errors response)
      (throw (ex-info "FQL Error" response))
      response)))

;; client

(defn http-client [{:keys [timeout]}]
  (-> (HttpClient/newBuilder)
      (.connectTimeout (Duration/ofMillis (or timeout 10000)))
      (.version HttpClient$Version/HTTP_1_1)
      (.build)))

;; public api

(defn client [{:keys [secret] :as opts}]
  {:http-client (http-client opts)
   :secret      (or secret (System/getenv "FAUNADB_SECRET"))
   :state       (atom {})})

(defn invoke [client request]
  (-> (:http-client client)
      (.send (http-request (merge client request))
             (HttpResponse$BodyHandlers/ofString))
      (json-response client)))

(defn invoke-async [client request]
  (-> (.sendAsync
        (:http-client client)
        (http-request (merge client request))
        (HttpResponse$BodyHandlers/ofString))
      (.thenApply
        (reify Function
          (apply [_ response]
            (json-response response client))))))

(defn query [client data]
  (invoke client {:data data}))

(defn state [client]
  @(:state client))
