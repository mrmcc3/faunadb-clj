(ns mrmcc3.fauna.query
  "A clojure implementation of FQL 2.11.0"
  {:author "Michael McClintock"}
  (:refer-clojure :exclude [ref get]))

;; Basic

(defn at
  "Retrieves documents at or before a timestamp"
  [timestamp expression] ^:op
  {:at timestamp :expr expression})

(defn call
  "Executes a user-defined function"
  [function & args] ^:op
  {:call      function
   :arguments (if (next args) args (first args))})

(defn do'
  "Execute expressions in order"
  [& exprs] ^:op
  {:do exprs})

(defn if'
  "Executes an expression based on a boolean condition"
  [test then else] ^:op
  {:if test :then then :else else})

(defn lambda
  ""
  [params expression] ^:op
  {:lambda params :expr expression})

(defn let'
  ""
  [bindings in] ^:op
  {:let (map (fn [[k v]] {k v}) (partition 2 bindings))
   :in  in})

(defn var'
  "Uses a variables value"
  [name] ^:op
  {:var name})

;; Miscellaneous

(defn new-id
  "Generates a unique, numeric id"
  [] ^:op
  {:new_id nil})

(defn abort
  "Terminates the current transaction"
  [message] ^:op
  {:abort message})

(defn database
  "Returns the ref for a database"
  ([name] ^:op {:database name})
  ([name database] ^:op {:database name :scope database}))

(defn databases
  "Returns an array of refs for all collections"
  ([] (databases nil))
  ([database] ^:op {:databases database}))

(defn move-database
  "Moves a database into another, parent database"
  [from to] ^:op
  {:move_database from :to to})

(defn collection
  "Returns the ref for a collection"
  ([name] ^:op {:collection name})
  ([name database] ^:op {:collection name :scope database}))

(defn collections
  "Returns an array of refs for all collections"
  ([] (collections nil))
  ([database] ^:op {:collections database}))

(defn function
  "Returns the ref for a user defined function"
  ([name] ^:op {:function name})
  ([name database] ^:op {:function name :scope database}))

(defn functions
  "Returns an array of refs for all user-defined functions"
  ([] (functions nil))
  ([database] ^:op {:functions database}))

(defn index
  "Returns the ref for an index"
  ([name] ^:op {:index name})
  ([name database] ^:op {:index name :scope database}))

(defn indexes
  "Returns an array of refs for all indexes"
  ([] (indexes nil))
  ([database] ^:op {:indexes database}))

(defn role
  "Returns the ref for a user-defined role"
  ([name] ^:op {:role name})
  ([name database] ^:op {:role name :scope database}))

(defn roles
  "Returns an array of refs for all user-defined roles"
  ([] (roles nil))
  ([database] ^:op {:roles database}))

(defn documents
  "Returns the set of documents within a collection"
  [collection] ^:op
  {:documents collection})

(defn ref
  "Returns a reference to a specific document in a collection"
  [schema-ref id] ^:op
  {:ref schema-ref :id id})

(defn query
  "Defers execution of a Lambda function"
  [lambda] ^:op
  {:query lambda})

;; Read

(defn get
  "Retrieves the document for the specific reference"
  ([ref] ^:op {:get ref})
  ([ref ts] ^:op {:get ref :ts ts}))

(defn key-from-secret
  "Retrieves a key based on its secret"
  [secret] ^:op
  {:key_from_secret secret})

(defn paginate
  "Returns a subset of query results"
  [input] ^:op
  {:paginate input})

(defn select
  "Retrieves a specific field value from a document"
  ([path from] ^:op
   {:select path :from from})
  ([path from default] ^:op
   {:select  path
    :from    from
    :default default}))

;; Write

(defn create
  "Create a document in a collection"
  [coll-ref params] ^:op
  {:create coll-ref :params params})

(comment

  (require '[mrmcc3.fauna.query-test :as t])
  (import '(com.faunadb.client.query Language)
          '(java.time Instant))

  (t/lang->data

    #_(Language/Query
        (Language/Lambda
          (Language/Arr [(Language/Value "a")])
          (Language/Var "a")))

    )

  (require '[mrmcc3.fauna.http :as http])

  (time
    (http/invoke
      (http/client {})
      {:data (databases)}))

  )
