(ns mrmcc3.fauna.query
  "A clojure implementation of FQL 2.11.0"
  {:author "Michael McClintock"}
  (:refer-clojure
    :exclude
    [ref get remove replace update merge to-array count
     distinct drop max min]))

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
  {:let (map (fn [[k v]] ^:op {k v}) (partition 2 bindings))
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

(defn create-collection
  "Create a collection"
  [params] ^:op
  {:create_collection params})

(defn create-database
  "Create a database"
  [params] ^:op
  {:create_database params})

(defn create-function
  "Create a user-defined function"
  [params] ^:op
  {:create_function params})

(defn create-index
  "Create an index"
  [params] ^:op
  {:create_index params})

(defn create-key
  "Create a key"
  [params] ^:op
  {:create_key params})

(defn create-role
  "Create a role"
  [params] ^:op
  {:create_role params})

(defn delete
  "Delete a document, key, index, collection, or database"
  [ref] ^:op
  {:delete ref})

(defn insert
  "Add an event to a document's history"
  [ref ts action params] ^:op
  {:insert ref :ts ts :action action :params params})

(defn remove
  "Remove an event from a documents history"
  [ref ts action] ^:op
  {:remove ref :ts ts :action action})

(defn replace
  "Replace a document with a new document"
  [ref params] ^:op
  {:replace ref :params params})

(defn update
  "Replace a document with a new document"
  [ref params] ^:op
  {:update ref :params params})

;; Object

(defn merge
  "Merge two objects into one, with an optional resolver lambda"
  ([obj1 obj2] ^:op {:merge obj1 :with obj2})
  ([obj1 obj2 lambda] ^:op {:merge obj1 :with obj2 :lambda lambda}))

(defn to-array
  "Converts an object to an array"
  [obj] ^:op
  {:to_array obj})

;; Array & Set

(defn all
  "Tests whether all of the provided values are true"
  [vals] ^:op
  {:all vals})

(defn any
  "Tests whether any of the provided values are true"
  [vals] ^:op
  {:any vals})

(defn append
  "Tests whether any of the provided values are true"
  [vals base] ^:op
  {:append vals :collection base})

(defn count
  "Counts the items in an array or set"
  [coll] ^:op
  {:count coll})

(defn difference
  "Returns an array of items in one array that are missing in the others"
  [& colls] ^:op
  {:difference colls})

(defn intersection
  "Returns an array of items that exist in all arrays"
  [& colls] ^:op
  {:intersection colls})

(defn union
  "Returns an array that combines the items in multiple arrays"
  [& colls] ^:op
  {:union colls})

(defn distinct
  "Returns an array of distinct items within multiple arrays"
  [coll] ^:op
  {:distinct coll})

(defn drop
  "Removes items from start of array"
  [n coll] ^:op
  {:drop n :collection coll})

(defn is-empty
  "Test whether an array is empty"
  [coll] ^:op
  {:is_empty coll})

(defn is-non-empty
  "Test whether an array contains items"
  [coll] ^:op
  {:is_nonempty coll})

(defn sum
  "Sums the items in an collection"
  [coll] ^:op
  {:sum coll})

(defn mean
  "Returns the average value in the collection"
  [coll] ^:op
  {:mean coll})

(defn max
  "Returns the largest value"
  [& args] ^:op
  {:max args})

(defn min
  "Returns the smallest value"
  [& args] ^:op
  {:min args})

;; Set

;; Logical

;; Authentication

;; String

;; Time

;; Conversion

;; Math

;; Type Checks
