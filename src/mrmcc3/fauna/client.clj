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

(defonce last-seen-txn (atom 0))

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

(defn http-request [{:keys [secret timeout query]}]
  (let [builder (HttpRequest/newBuilder (URI. "https://db.fauna.com"))]
    (.method builder "POST"
             (json-pub query))
    (.header builder "Authorization" (basic-auth secret))
    (.header builder "Content-Type" "application/json; charset=utf-8")
    (.header builder "X-FaunaDB-API-Version" "2.7")
    (when-let [last-seen @last-seen-txn]
      (.header builder "X-Last-Seen-Txn" (str last-seen)))
    (.timeout builder (Duration/ofMillis (or timeout 60000)))
    (.build builder)))

(defn request-map [req]
  (if (-> req meta :op)
    {:query req}
    req))

;; response

(defn largest [last-seen txn-time]
  (if (< last-seen txn-time)
    txn-time
    last-seen))

(def long-headers
  #{:x-query-bytes-in :x-query-bytes-out :x-read-ops :x-write-ops
    :x-storage-bytes-read :x-storage-bytes-write :x-query-time
    :x-compute-ops :x-txn-retries :x-txn-time :content-length})

(defn header [[k [v]]]
  (let [k' (keyword k)]
    [k' (if (long-headers k') (Long/parseLong v) v)]))

(defn headers-map [^HttpResponse response]
  (into {} (map header) (-> response (.headers) (.map))))

(defn json-response [^HttpResponse response {:keys [tap?]}]
  (let [{:keys [x-txn-time] :as headers} (headers-map response)
        {:keys [errors resource]} (json/read-str (.body response))]
    (when tap?
      (tap> ^::response-headers headers))
    (when x-txn-time
      (swap! last-seen-txn largest x-txn-time))
    (if errors
      (throw (ex-info "FQL Error" {:errors errors}))
      resource)))

;; client

(defn http-client [{:keys [conn-timeout]}]
  (-> (HttpClient/newBuilder)
      (.connectTimeout (Duration/ofMillis (or conn-timeout 10000)))
      (.version HttpClient$Version/HTTP_1_1)
      (.build)))

;; public api

(defn client [{:keys [secret timeout] :as opts}]
  {:http-client (http-client opts)
   :secret      secret
   :timeout     timeout
   :tap?        (:tap? opts true)})

(defn query [client req]
  (let [request (merge client (request-map req))]
    (-> (:http-client client)
        (.send (http-request request)
               (HttpResponse$BodyHandlers/ofString))
        (json-response client))))

(defn query-async [client req]
  (let [request (merge client (request-map req))]
    (-> (.sendAsync
          (:http-client client)
          (http-request request)
          (HttpResponse$BodyHandlers/ofString))
        (.thenApply
          (reify Function
            (apply [_ response]
              (json-response response client)))))))
