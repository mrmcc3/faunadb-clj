(ns mrmcc3.fauna.query
  "Based on FQL 2.11.0"
  {:author "Michael McClintock"}
  (:refer-clojure :exclude [ref])
  (:require [clojure.alpha.spec :as s]))

(defn new-id
  "Generates a unique, numeric id"
  []
  {:new_id nil})

(s/def ::new-id-schema
  (s/schema {:new_id nil?}))

(s/fdef new-id
  :args (s/cat)
  :ret (s/select ::new-id-schema [:new_id]))

(defn abort
  "Terminates the current transaction"
  [message]
  {:abort message})

(s/def ::abort-schema
  (s/schema {:abort string?}))

(s/fdef abort
  :args (s/cat :message string?)
  :ret (s/select ::abort-schema [:abort]))

(defn database
  "Returns the ref for a database"
  ([name] {:database name})
  ([name database] {:database name :scope database}))

(s/def ::database-schema
  (s/schema {:database string?
             :scope    (s/nest ::database-schema)}))

(s/def ::database-ref
  (s/select ::database-schema [:database]))

(s/fdef database
  :args (s/cat :name string? :db (s/? ::database-ref))
  :ret ::database-ref)

(defn databases
  "Returns an array of refs for all collections"
  ([] {:databases nil})
  ([database] {:databases database}))

(s/def ::databases-schema
  (s/schema {:databases (s/nilable ::database-ref)}))

(s/fdef databases
  :args (s/cat :db (s/? ::database-ref))
  :ret (s/select ::databases-schema [:databases]))

(defn move-database
  "Moves a database into another, parent database"
  [from to]
  {:move_database from :to to})

(s/def ::move-database-schema
  (s/schema {:move_database ::database-schema
             :to            ::database-schema}))

(s/fdef move-database
  :args (s/cat :from ::database-ref :to ::database-ref)
  :ret (s/select ::move-database-schema [:move_database :to]))

(defn collection
  "Returns the ref for a collection"
  ([name] {:collection name})
  ([name database] {:collection name :scope database}))

(s/def ::collection-schema
  (s/schema {:collection string? :scope ::database-schema}))

(s/def ::collection-ref
  (s/select ::collection-schema [:collection]))

(s/fdef collection
  :args (s/cat :name string? :db (s/? ::database-ref))
  :ret ::collection-ref)

(defn collections
  "Returns an array of refs for all collections"
  ([] {:collections nil})
  ([database] {:collections database}))

(s/def ::collections-schema
  (s/schema {:collections (s/nilable ::database-ref)}))

(s/fdef collections
  :args (s/cat :db (s/? ::database-ref))
  :ret (s/select ::collections-schema [:collections]))

(defn function
  "Returns the ref for a user defined function"
  ([name] {:function name})
  ([name database] {:function name :scope database}))

(s/def ::function-schema
  (s/schema {:function string? :scope ::database-schema}))

(s/def ::function-ref
  (s/select ::function-schema [:function]))

(s/fdef function
  :args (s/cat :name string? :db (s/? ::database-ref))
  :ret ::function-ref)

(defn functions
  "Returns an array of refs for all user-defined functions"
  ([] {:functions nil})
  ([database] {:functions database}))

(s/def ::functions-schema
  (s/schema {:functions (s/nilable ::database-ref)}))

(s/fdef functions
  :args (s/cat :db (s/? ::database-ref))
  :ret (s/select ::functions-schema [:functions]))

(defn index
  "Returns the ref for an index"
  ([name] {:index name})
  ([name database] {:index name :scope database}))

(s/def ::index-schema
  (s/schema {:index string? :scope ::database-schema}))

(s/def ::index-ref
  (s/select ::index-schema [:index]))

(s/fdef index
  :args (s/cat :name string? :db (s/? ::database-ref))
  :ret ::index-ref)

(defn indexes
  "Returns an array of refs for all indexes"
  ([] {:indexes nil})
  ([database] {:indexes database}))

(s/def ::indexes-schema
  (s/schema {:indexes (s/nilable ::database-ref)}))

(s/fdef indexes
  :args (s/cat :db (s/? ::database-ref))
  :ret (s/select ::indexes-schema [:indexes]))

(defn role
  "Returns the ref for a user-defined role"
  ([name] {:role name})
  ([name database] {:role name :scope database}))

(s/def ::role-schema
  (s/schema {:role string? :scope ::database-schema}))

(s/def ::role-ref
  (s/select ::role-schema [:role]))

(s/fdef role
  :args (s/cat :name string? :db (s/? ::database-ref))
  :ret ::role-ref)

(defn roles
  "Returns an array of refs for all user-defined roles"
  ([] {:roles nil})
  ([database] {:roles database}))

(s/def ::roles-schema
  (s/schema {:roles (s/nilable ::database-ref)}))

(s/fdef roles
  :args (s/cat :db (s/? ::database-ref))
  :ret (s/select ::roles-schema [:roles]))

(defn documents
  "Returns the set of documents within a collection"
  [collection]
  {:documents collection})

(s/def ::documents-schema
  (s/schema {:documents ::collection-schema}))

(s/fdef documents
  :args (s/cat :collection ::collection-ref)
  :ret (s/select ::documents-schema [:documents]))

(defn ref
  "Returns a reference to a specific document in a collection"
  [schema-ref id]
  {:ref schema-ref :id id})

(s/def ::schema-ref
  (s/or
    :database-ref ::database-ref
    :collection-ref ::collection-ref
    :function-ref ::function-ref
    :index-ref ::index-ref
    :role-ref ::role-ref))

(s/def ::schema-ref-schema
  (s/schema {:ref ::schema-ref :id string?}))

(s/fdef ref
  :args (s/cat :schema-ref ::schema-ref :id string?)
  :ret (s/select ::ref-schema [:ref :id]))

(comment

  (require
    '[clojure.alpha.spec.test :as test]
    '[clojure.alpha.spec.gen :as gen]
    '[mrmcc3.fauna.interop :as i]
    '[mrmcc3.fauna.http :as http])

  (test/instrument)
  (test/check `ref)
  (s/exercise-fn `ref)

  (import '(com.faunadb.client.query Language))

  (i/data
    (Language/Ref (Language/Collection "a") "asdf"))

  (time
    (http/invoke
      (http/client {})
      {:data (databases)}))

  )
