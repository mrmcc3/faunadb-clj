(ns mrmcc3.fauna.query
  "A clojure implementation of FQL 2.11.0"
  {:author "Michael McClintock"}
  (:refer-clojure
    :exclude
    [ref get remove replace update merge to-array count
     distinct drop take max min map filter reduce range
     not and or identity keys])
  (:require
    [clojure.core :as c]))

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
  {:let (c/map (fn [[k v]] ^:op {k v}) (c/partition 2 bindings))
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
  ([name scope]
   (if scope
     ^:op {:database name :scope scope}
     (database name))))

(defn databases
  "Returns an array of refs for all collections"
  ([] (databases nil))
  ([scope] ^:op {:databases scope}))

(defn move-database
  "Moves a database into another, parent database"
  [from to] ^:op
  {:move_database from :to to})

(defn collection
  "Returns the ref for a collection"
  ([name] ^:op {:collection name})
  ([name scope]
   (if scope
     ^:op {:collection name :scope scope}
     (collection name))))

(defn collections
  "Returns an array of refs for all collections"
  ([] (collections nil))
  ([scope] ^:op {:collections scope}))

(defn function
  "Returns the ref for a user defined function"
  ([name] ^:op {:function name})
  ([name scope]
   (if scope
     ^:op {:function name :scope scope}
     (function name))))

(defn functions
  "Returns an array of refs for all user-defined functions"
  ([] (functions nil))
  ([scope] ^:op {:functions scope}))

(defn index
  "Returns the ref for an index"
  ([name] ^:op {:index name})
  ([name scope]
   (if scope
     ^:op {:index name :scope scope}
     (index name))))

(defn indexes
  "Returns an array of refs for all indexes"
  ([] (indexes nil))
  ([scope] ^:op {:indexes scope}))

(defn role
  "Returns the ref for a user-defined role"
  ([name] ^:op {:role name})
  ([name scope]
   (if scope
     ^:op {:role name :scope scope}
     (role name))))

(defn roles
  "Returns an array of refs for all user-defined roles"
  ([] (roles nil))
  ([scope] ^:op {:roles scope}))

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
  "Adds items to the end of an array"
  [vals base] ^:op
  {:append vals :collection base})

(defn prepend
  "Adds items to the start of an array"
  [vals base] ^:op
  {:prepend vals :collection base})

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

(defn take
  "Fetches items from start of array"
  [n coll] ^:op
  {:take n :collection coll})

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

(defn map
  "Applies a function to all array items"
  [coll lambda] ^:op
  {:map lambda :collection coll})

(defn filter
  "Fetches specific items from an array"
  [coll lambda] ^:op
  {:filter lambda :collection coll})

(defn foreach
  "Iterates over array items"
  [coll lambda] ^:op
  {:foreach lambda :collection coll})

(defn reduce
  "Reduces a collection using a lambda function"
  [reducer init coll] ^:op
  {:reduce reducer :initial init :collection coll})

(defn events
  "Returns the set of events describing the history of a set"
  [set] ^:op
  {:events set})

(defn join
  "Combines the items in a set with set's indexed values"
  [source detail] ^:op
  {:join source :with detail})

(defn match
  "Returns the set of items that match search terms"
  ([index] ^:op {:match index})
  ([index terms] ^:op {:match index :terms terms}))

(defn range
  "Returns a subset of a set, in the specific range"
  [set from to] ^:op
  {:range set :from from :to to})

(defn singleton
  "produces a set containing the ref that you provide"
  [ref] ^:op
  {:singleton ref})

(defn to-object
  "Converts an array to an object"
  [arr] ^:op
  {:to_object arr})

;; Logical

(defn not
  "Return the opposite of a boolean expression"
  [val] ^:op
  {:not val})

(defn and
  "Returns true if all values are true"
  [& vals] ^:op
  {:and vals})

(defn or
  "Returns true if any value is true"
  [& vals] ^:op
  {:or vals})

(defn equals
  "Returns true if all values are equivalent"
  [& vals] ^:op
  {:equals vals})

(defn LT
  "Returns true if each value is less than all the following values"
  [& vals] ^:op
  {:lt vals})

(defn LTE
  "Returns true if each value is less than, or equal to,
  all the following values"
  [& vals] ^:op
  {:lte vals})

(defn GT
  "Returns true if each value is greater than all the following values"
  [& vals] ^:op
  {:gt vals})

(defn GTE
  "Returns true if each value is greater than, or equal to,
  all the following values"
  [& vals] ^:op
  {:gte vals})

(defn contains
  "Returns true when a value exists at the given path"
  [path in] ^:op
  {:contains path :in in})

(defn exists
  "Returns true if a document has an event at a specific time"
  ([ref] ^:op {:exists ref})
  ([ref ts] ^:op {:exists ref :ts ts}))

;; Authentication

(defn has-identity
  "Checks whether the current client has credentials"
  [] ^:op
  {:has_identity nil})

(defn identify
  "Verifies an identity's credentials"
  [ref password] ^:op
  {:identify ref :password password})

(defn identity
  "Fetches the identities auth token"
  [] ^:op
  {:identity nil})

(defn keys
  "Retrieves keys associated with the specified database"
  ([] (keys nil))
  ([db] ^:op {:keys db}))

(defn tokens
  "Retrieves tokens associated with the specified database"
  ([] (tokens nil))
  ([db] ^:op {:tokens db}))

(defn login
  "Creates an auth token for an identity"
  [ref params] ^:op
  {:login ref :params params})

(defn logout
  "Logs out of the current (or all) sessions"
  ([] (logout nil))
  ([all] ^:op {:logout all}))

;; String

(defn contains-str-regex
  "Tests whether a string contains a specific pattern"
  [string pattern] ^:op
  {:containsstrregex string :pattern pattern})

(defn find-str-regex
  "Searches for a regex pattern within a string"
  [string pattern] ^:op
  {:findstrregex string :pattern pattern})

(defn find-str
  "Searches for a string within a string"
  [string find] ^:op
  {:findstr string :find find})

;; Time

(defn now
  "Returns a timestamp representing the current transaction time"
  [] ^:op
  {:now nil})

(defn time-diff
  "Returns the difference between two timestamps/dates, in specified units"
  [start finish unit] ^:op
  {:time_diff start :other finish :unit unit})

(defn epoch
  "Creates a timestamp from an offset since 1970-01-01 in seconds,
  milliseconds, microseconds, or nanoseconds."
  [offset unit] ^:op
  {:epoch offset :unit unit})

;; Conversion

;; Math

;; Type Checks
