(ns mrmcc3.fauna.query-test
  (:require
    [mrmcc3.fauna.query :as q]
    [clojure.test :refer :all]
    [clojure.data.json :as json]
    [kaocha.repl :as k])
  (:import
    (com.faunadb.client.query Language)
    (com.fasterxml.jackson.databind ObjectMapper)))

(def ow (.writer (ObjectMapper.)))

(defn lang->data [o]
  (json/read-str (.writeValueAsString ow o)))

(defn round-trip [v]
  (json/read-str (json/write-str v)))

(deftest query
  (let [db1  (Language/Database "db1")
        db1' (q/database "db1")
        c1   (Language/Collection "c1")
        c1'  (q/collection "c1")]
    (are [jvm clj]
      (= (lang->data jvm) (round-trip clj))

      ;; --- new-id
      (Language/NewId)
      (q/new-id)

      ;; --- abort
      (Language/Abort "msg")
      (q/abort "msg")

      ;; --- database
      db1 db1'
      (Language/Database "db1" (Language/Database "db2"))
      (q/database "db1" (q/database "db2"))

      ;; --- databases
      (Language/Databases)
      (q/databases)
      (Language/Databases db1)
      (q/databases db1')

      ;; --- move-database
      (Language/MoveDatabase db1 (Language/Database "db2"))
      (q/move-database db1' (q/database "db2"))

      ;; --- collection
      (Language/Collection "blah")
      (q/collection "blah")
      (Language/Collection "blah" db1)
      (q/collection "blah" db1')

      ;; --- collections
      (Language/Collections)
      (q/collections)
      (Language/Collections db1)
      (q/collections db1')

      ;; --- function
      (Language/Function "f")
      (q/function "f")
      (Language/Function "f" db1)
      (q/function "f" db1')

      ;; --- functions
      (Language/Functions)
      (q/functions)
      (Language/Functions db1)
      (q/functions db1')

      ;; --- index
      (Language/Index "i")
      (q/index "i")
      (Language/Index "i" db1)
      (q/index "i" db1')

      ;; --- indexes
      (Language/Indexes)
      (q/indexes)
      (Language/Indexes db1)
      (q/indexes db1')

      ;; --- role
      (Language/Role "r")
      (q/role "r")
      (Language/Role "r" db1)
      (q/role "r" db1')

      ;; --- roles
      (Language/Roles)
      (q/roles)
      (Language/Roles db1)
      (q/roles db1')

      ;; --- documents
      (Language/Documents c1)
      (q/documents c1')

      ;; --- ref
      (Language/Ref db1 "1")
      (q/ref db1' "1")
      (Language/Ref c1 "1")
      (q/ref c1' "1")
      (Language/Ref (Language/Role "r") "1")
      (q/ref (q/role "r") "1")
      (Language/Ref (Language/Index "i") "1")
      (q/ref (q/index "i") "1")
      (Language/Ref (Language/Function "f") "1")
      (q/ref (q/function "f") "1")

      ;; --- query

      ;; --- var
      (Language/Var "x")
      (q/var "x")

      ;; --- do
      (Language/Do [db1 c1 (Language/Ref db1 "1")])
      (q/do db1' c1' (q/ref db1' "1"))

      ;; --- if
      (Language/If (Language/Value true) db1 c1)
      (q/if true db1' c1')

      ;; --- let
      (.in
        (Language/Let
          "x" (Language/Value 1)
          "y" (Language/Value 2))
        (Language/Do [(Language/Var "x") (Language/Var "y")]))
      (q/let* ['x 1 'y 2] (q/do (q/var 'x) (q/var 'y)))

      ))

  )










