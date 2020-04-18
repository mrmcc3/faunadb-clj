(ns fauna.docs.quickstart
  (:require
    [mrmcc3.fauna.client :as c]
    [mrmcc3.fauna.query :as q]))

;; define a client - will load FAUNADB_SECRET env var

(def admin-client
  (c/client
    {:secret (System/getenv "FAUNADB_SECRET")}))

(c/query
  admin-client
  (q/create-database
    {:name "my_db"}))

(def my-db
  (q/database "my_db"))

;; create an admin key for my_db

(c/query
  admin-client
  (q/create-key
    {:role     :admin
     :database my-db
     :name     "my_db_admin"}))

(def my-db-client
  (c/client {:secret ""}))

(def query (partial c/query my-db-client))

;; create a collection

(query
  (q/create-collection
    {:name "posts"}))

(def posts (q/collection "posts"))

;; create an index

(query
  (q/create-index
    {:name   "posts_by_title"
     :source posts
     :terms  [{:field [:data :title]}]}))

(def posts-by-title
  (q/index "posts_by_title"))

;; add data

(query
  (q/create
    posts
    {:data {:title "what i had for breakfast .."}}))

(query
  (q/map
    ["My cat and other marvels"
     "Pondering during a commute"
     "Deep meanings in a latte"]
    (q/lambda
      "post_title"
      (q/create
        posts
        {:data {:title (q/var' "post_title")}}))))

;; get

(query (q/get (q/ref posts "")))

(query
  (q/get
    (q/match posts-by-title "Deep meanings in a latte")))

;; cleanup

(c/query
  admin-client
  (q/delete my-db))

