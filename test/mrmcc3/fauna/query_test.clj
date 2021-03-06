(ns mrmcc3.fauna.query-test
  (:require
    [mrmcc3.fauna.query :as q]
    [mrmcc3.fauna.macros :as m]
    [mrmcc3.fauna.json :as json]
    [clojure.data.json :as data.json]
    [clojure.test :refer :all]
    [kaocha.repl :as k])
  (:import
    (com.faunadb.client.query Language Language$Action)
    (com.fasterxml.jackson.databind ObjectMapper)
    (java.time Instant LocalDate)))

(def ow (.writer (ObjectMapper.)))

(defn lang->data [o]
  (data.json/read-str (.writeValueAsString ow o)))

(defn round-trip-encode [v]
  (data.json/read-str (json/write-str v)))

(defn round-trip-decode [v]
  (json/read-str (data.json/write-str v)))

(deftest query-encoding
  (let [db1   (Language/Database "db1")
        db1'  (q/database "db1")
        c1    (Language/Collection "c1")
        c1'   (q/collection "c1")
        l1    (Language/Lambda
                (Language/Arr [(Language/Value "a")])
                (Language/Var "a"))
        l1'   (q/lambda ["a"] (q/var' "a"))
        now   (Instant/now)
        now-d (LocalDate/now)
        obj1  (Language/Obj "data" (Language/Obj "name" (Language/Value "n")))
        obj1' {:data {:name "n"}}]
    (are [jvm clj]
      (= (lang->data jvm) (round-trip-encode clj))

      ;; Special Types

      ;; --- Instant
      (Language/Value now)
      now

      ;; --- Date
      (Language/Value now-d)
      now-d

      ;; --- byte[]
      (Language/Value (.getBytes "test"))
      (.getBytes "test")

      ;; Basic

      ;; --- at
      (Language/At now (Language/Value "f"))
      (q/at now "f")

      ;; --- call
      (Language/Call (Language/Function "f") [])
      (q/call (q/function "f"))
      (Language/Call (Language/Function "f") [(Language/Value 1)])
      (q/call (q/function "f") 1)
      (Language/Call
        (Language/Function "f")
        [(Language/Value 1) (Language/Value 2)])
      (q/call (q/function "f") 1 2)

      ;; --- do
      (Language/Do [db1 c1 (Language/Ref db1 "1")])
      (q/do' db1' c1' (q/ref db1' "1"))

      ;; --- if
      (Language/If (Language/Value true) db1 c1)
      (q/if' true db1' c1')

      ;; --- lambda
      l1 l1'

      ;; --- let
      (.in
        (Language/Let
          "x" (Language/Value 1)
          "y" (Language/Value 2))
        (Language/Do [(Language/Var "x") (Language/Var "y")]))
      (q/let' ["x" 1 "y" 2] (q/do' (q/var' "x") (q/var' "y")))

      ;; --- var
      (Language/Var "x")
      (q/var' "x")

      ;; Miscellaneous

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
      (Language/Query l1)
      (q/query l1')

      ;; Read

      ;; --- get
      (Language/Get c1)
      (q/get c1')
      (Language/Get c1 now)
      (q/get c1' now)

      ;; --- key-from-secret
      (Language/KeyFromSecret "s")
      (q/key-from-secret "s")

      ;; --- paginate
      (Language/Paginate c1)
      (q/paginate c1')

      ;; --- select
      (Language/Select
        (Language/Arr [(Language/Value "a") (Language/Value 1)])
        (Language/Obj "a" (Language/Value true) "b" (Language/Value 2)))
      (q/select ["a" 1] {:a true :b 2})
      (Language/Select
        (Language/Arr [(Language/Value "a") (Language/Value 1)])
        (Language/Obj "a" (Language/Value true) "b" (Language/Value 2))
        (Language/Obj "b" (Language/Value now)))
      (q/select [:a 1] {:a true :b 2} {:b now})

      ;; Write

      ;; --- create
      (Language/Create c1 obj1)
      (q/create c1' obj1')

      ;; --- create-collection
      (Language/CreateCollection obj1)
      (q/create-collection obj1')

      ;; --- create-database
      (Language/CreateDatabase obj1)
      (q/create-database obj1')

      ;; --- create-function
      (Language/CreateFunction obj1)
      (q/create-function obj1')

      ;; --- create-index
      (Language/CreateIndex obj1)
      (q/create-index obj1')

      ;; --- create-key
      (Language/CreateKey obj1)
      (q/create-key obj1')

      ;; --- create-role
      (Language/CreateRole obj1)
      (q/create-role obj1')

      ;; --- delete
      (Language/Delete c1)
      (q/delete c1')

      ;; --- insert
      (Language/Insert c1 (Language/Value 1) (Language$Action/CREATE) obj1)
      (q/insert c1' 1 :create obj1')

      ;; --- remove
      (Language/Remove c1 (Language/Value 1) (Language$Action/DELETE))
      (q/remove c1' 1 :delete)

      ;; --- replace
      (Language/Replace c1 obj1)
      (q/replace c1' obj1')

      ;; --- update
      (Language/Update c1 obj1)
      (q/update c1' obj1')

      ;; Object

      ;; --- merge
      (Language/Merge obj1 obj1)
      (q/merge obj1' obj1')
      (Language/Merge obj1 obj1 l1)
      (q/merge obj1' obj1' l1')

      ;; --- to-array
      (Language/ToArray obj1)
      (q/to-array obj1')

      ;; Array & Set

      ;; --- all
      (Language/All (Language/Arr [(Language/Value true)]))
      (q/all [true])

      ;; --- any
      (Language/Any (Language/Arr [(Language/Value true)]))
      (q/any [true])

      ;; --- append
      (Language/Append
        (Language/Arr [(Language/Value 1)])
        (Language/Arr [(Language/Value 2)]))
      (q/append [1] [2])

      ;; --- prepend
      (Language/Prepend
        (Language/Arr [(Language/Value 1)])
        (Language/Arr [(Language/Value 2)]))
      (q/prepend [1] [2])

      ;; --- count
      (Language/Count (Language/Arr [(Language/Value 1)]))
      (q/count [1])

      ;; --- difference
      (Language/Difference (repeat 3 (Language/Arr [obj1])))
      (apply q/difference (repeat 3 [obj1']))

      ;; --- intersection
      (Language/Intersection (repeat 3 (Language/Arr [obj1])))
      (apply q/intersection (repeat 3 [obj1']))

      ;; --- union
      (Language/Union (repeat 3 (Language/Arr [1])))
      (apply q/union (repeat 3 [1]))

      ;; --- distinct
      (Language/Distinct (Language/Arr [(Language/Value 1) obj1]))
      (q/distinct [1 obj1'])

      ;; --- drop
      (Language/Drop
        (Language/Value 1)
        (Language/Arr [(Language/Value 1)]))
      (q/drop 1 [1])

      ;; --- take
      (Language/Take
        (Language/Value 1)
        (Language/Arr [(Language/Value "a")]))
      (q/take 1 ["a"])

      ;; --- is-empty
      (Language/IsEmpty (Language/Arr []))
      (q/is-empty [])

      ;; --- is-non-empty
      (Language/IsNonEmpty (Language/Arr []))
      (q/is-non-empty [])

      ;; --- sum
      (Language/Sum (Language/Arr [1 2 3]))
      (q/sum [1 2 3])

      ;; --- mean
      (Language/Mean (Language/Arr [1 2 3]))
      (q/mean [1 2 3])

      ;; --- max
      (Language/Max [1 2 3])
      (q/max 1 2 3)

      ;; --- min
      (Language/Min [1 2 3])
      (q/min 1 2 3)

      ;; --- map
      (Language/Map (Language/Arr [1 2 3]) l1)
      (q/map [1 2 3] l1')

      ;; --- filter
      (Language/Filter (Language/Arr [1 2 3]) l1)
      (q/filter [1 2 3] l1')

      ;; --- for-each
      (Language/Foreach (Language/Arr [1 2 3]) l1)
      (q/foreach [1 2 3] l1')

      ;; --- reduce
      (Language/Reduce l1 (Language/Value now) (Language/Arr [1 2 3]))
      (q/reduce l1' now [1 2 3])

      ;; --- events
      (Language/Events c1)
      (q/events c1')

      ;; --- join
      (Language/Join c1 c1)
      (q/join c1' c1')

      ;; --- match
      (Language/Match (Language/Index "i"))
      (q/match (q/index "i"))
      (Language/Match (Language/Index "i") (Language/Value "v"))
      (q/match (q/index "i") "v")

      ;; --- range
      (Language/Range
        (Language/Match (Language/Index "i"))
        (Language/Null)
        (Language/Value now))
      (q/range (q/match (q/index "i")) nil now)

      ;; --- singleton
      (Language/Singleton c1)
      (q/singleton c1')

      ;; --- to-object
      (Language/ToObject
        (Language/Arr
          [(Language/Arr [1 2])
           (Language/Arr [3 4])]))
      (q/to-object [[1 2] [3 4]])

      ;; Logical

      ;; --- not
      (Language/Not (Language/Value true))
      (q/not true)

      ;; --- and
      (Language/And [(Language/Value true) (Language/Value false)])
      (q/and true false)

      ;; --- or
      (Language/Or [(Language/Value true) (Language/Value false)])
      (q/or true false)

      ;; --- equals
      (Language/Equals [(Language/Value true) (Language/Value false)])
      (q/equals true false)

      ;; --- LT
      (Language/LT [(Language/Value 1) (Language/Value 2)])
      (q/LT 1 2)

      ;; --- LTE
      (Language/LTE [(Language/Value 1) (Language/Value 2)])
      (q/LTE 1 2)

      ;; --- GT
      (Language/GT [(Language/Value 1) (Language/Value 2)])
      (q/GT 1 2)

      ;; --- GTE
      (Language/GTE [(Language/Value 1) (Language/Value 2)])
      (q/GTE 1 2)

      ;; --- contains
      (Language/Contains
        (Language/Arr [(Language/Value "a") (Language/Value "b")])
        (Language/Obj "a" (Language/Obj "b" (Language/Value now))))
      (q/contains ["a" "b"] {:a {:b now}})

      ;; --- exists
      (Language/Exists (Language/Ref c1 "1"))
      (q/exists (q/ref c1' "1"))
      (Language/Exists (Language/Ref c1 "1") (Language/Value now))
      (q/exists (q/ref c1' "1") now)

      ;; Authentication

      ;; --- has-identity
      (Language/HasIdentity)
      (q/has-identity)

      ;; --- identify
      (Language/Identify (Language/Ref c1 "1") (Language/Value "p"))
      (q/identify (q/ref c1' "1") "p")

      ;; --- identity
      (Language/Identity)
      (q/identity)

      ;; --- keys
      (Language/Keys)
      (q/keys)
      (Language/Keys db1)
      (q/keys db1')

      ;; --- tokens (not in docs?)
      (Language/Tokens)
      (q/tokens)
      (Language/Tokens db1)
      (q/tokens db1')

      ;; --- login
      (Language/Login (Language/Ref c1 "1") obj1)
      (q/login (q/ref c1' "1") obj1')

      ;; --- logout
      (Language/Logout true)
      (q/logout true)

      ;; String

      (Language/FindStr "abc" "a")
      (q/find-str "abc" "a")
      (Language/FindStrRegex "abc" "^abc$")
      (q/find-str-regex "abc" "^abc$")
      (Language/ContainsStrRegex "abc" "^abc$")
      (q/contains-str-regex "abc" "^abc$")

      ;; Time

      (Language/Now)
      (q/now)

      (Language/TimeDiff (Language/Now) (Language/Now) "hours")
      (q/time-diff (q/now) (q/now) "hours")

      (Language/Epoch 1590489809930000 "microseconds")
      (q/epoch 1590489809930000 "microseconds")

      ;; Conversion

      ;; Math

      ;; Type Checks

      )))

(deftest response-decoding
  (are [a b]
    (= (round-trip-decode a) b)

    {"@ref" {:id "users" :collection {"@ref" {:id "collections"}}}}
    (q/collection "users")


    {"@ref" {:id         "264933792182960650",
             :collection {"@ref" {:id "users", :collection {"@ref" {:id "collections"}}}}}}
    (q/ref (q/collection "users") "264933792182960650")

    ))

(deftest macros
  (let [posts (q/collection "posts")]
    (are [a b]
      (= a b)

      (q/map
        ["My cat and other marvels"
         "Pondering during a commute"
         "Deep meanings in a latte"]
        (q/lambda
          ["title"]
          (q/create
            posts
            {:data {:title (q/var' "title")}})))
      (m/map
        (m/fn [title] (q/create posts {:data {:title title}}))
        ["My cat and other marvels"
         "Pondering during a commute"
         "Deep meanings in a latte"])

      )))