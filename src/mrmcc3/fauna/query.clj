(ns mrmcc3.fauna.query
  "A clojure implementation of FQL 2.11.0"
  {:author "Michael McClintock"}
  (:refer-clojure :exclude [ref]))

;; Basic

(defn at
  "Retrieves documents at or before a timestamp"
  [timestamp expression]
  {:at timestamp :expr expression})

(defn call
  "Executes a user-defined function"
  [function & args]
  {:call      function
   :arguments (if (next args) args (first args))})

(defn do'
  "Execute expressions in order"
  [& exprs]
  {:do exprs})

(defn if'
  "Executes an expression based on a boolean condition"
  [test then else]
  {:if test :then then :else else})

(defn lambda
  ""
  [params expression]
  {:lambda params :expr expression})

(defn let'
  ""
  [bindings in]
  {:let (map (fn [[k v]] {k v}) (partition 2 bindings))
   :in  in})

(defn var'
  "Uses a variables value"
  [name]
  {:var name})

;; Miscellaneous

(defn new-id
  "Generates a unique, numeric id"
  []
  {:new_id nil})

(defn abort
  "Terminates the current transaction"
  [message]
  {:abort message})

(defn database
  "Returns the ref for a database"
  ([name] {:database name})
  ([name database] {:database name :scope database}))

(defn databases
  "Returns an array of refs for all collections"
  ([] {:databases nil})
  ([database] {:databases database}))

(defn move-database
  "Moves a database into another, parent database"
  [from to]
  {:move_database from :to to})

(defn collection
  "Returns the ref for a collection"
  ([name] {:collection name})
  ([name database] {:collection name :scope database}))

(defn collections
  "Returns an array of refs for all collections"
  ([] {:collections nil})
  ([database] {:collections database}))

(defn function
  "Returns the ref for a user defined function"
  ([name] {:function name})
  ([name database] {:function name :scope database}))

(defn functions
  "Returns an array of refs for all user-defined functions"
  ([] {:functions nil})
  ([database] {:functions database}))

(defn index
  "Returns the ref for an index"
  ([name] {:index name})
  ([name database] {:index name :scope database}))

(defn indexes
  "Returns an array of refs for all indexes"
  ([] {:indexes nil})
  ([database] {:indexes database}))

(defn role
  "Returns the ref for a user-defined role"
  ([name] {:role name})
  ([name database] {:role name :scope database}))

(defn roles
  "Returns an array of refs for all user-defined roles"
  ([] {:roles nil})
  ([database] {:roles database}))

(defn documents
  "Returns the set of documents within a collection"
  [collection]
  {:documents collection})

(defn ref
  "Returns a reference to a specific document in a collection"
  [schema-ref id]
  {:ref schema-ref :id id})

(defn query
  "Defers execution of a Lambda function"
  [lambda]
  {:query lambda})

;; Read

(defn get
  "Retrieves the document for the specific reference"
  ([ref] {:get ref})
  ([ref ts] {:get ref :ts ts}))

(defn key-from-secret
  "Retrieves a key based on its secret"
  [secret]
  {:key_from_secret secret})

(defn paginate
  "Returns a subset of query results"
  [input]
  {:paginate input})

(defn select
  "Retrieves a specific field value from a document"
  ([path from]
   {:select path :from {:object from}})
  ([path from default]
   {:select  path
    :from    {:object from}
    :default {:object default}}))

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
