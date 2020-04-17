(ns mrmcc3.fauna.query.specs
  "Specs for FQL 2.11.0"
  {:author "Michael McClintock"}
  (:require
    [clojure.alpha.spec :as s]
    [mrmcc3.fauna.query :as q]))

(s/def ::new-id-schema
  (s/schema {:new_id nil?}))

(s/fdef q/new-id
  :args (s/cat)
  :ret (s/select ::new-id-schema [:new_id]))

(s/def ::abort-schema
  (s/schema {:abort string?}))

(s/fdef q/abort
  :args (s/cat :message string?)
  :ret (s/select ::abort-schema [:abort]))

(s/def ::database-schema
  (s/schema {:database string?
             :scope    (s/nest ::database-schema)}))

(s/def ::database-ref
  (s/select ::database-schema [:database]))

(s/fdef q/database
  :args (s/cat :name string? :db (s/? ::database-ref))
  :ret ::database-ref)

(s/def ::databases-schema
  (s/schema {:databases (s/nilable ::database-ref)}))

(s/fdef q/databases
  :args (s/cat :db (s/? ::database-ref))
  :ret (s/select ::databases-schema [:databases]))

(s/def ::move-database-schema
  (s/schema {:move_database ::database-schema
             :to            ::database-schema}))

(s/fdef q/move-database
  :args (s/cat :from ::database-ref :to ::database-ref)
  :ret (s/select ::move-database-schema [:move_database :to]))

(s/def ::collection-schema
  (s/schema {:collection string? :scope ::database-schema}))

(s/def ::collection-ref
  (s/select ::collection-schema [:collection]))

(s/fdef q/collection
  :args (s/cat :name string? :db (s/? ::database-ref))
  :ret ::collection-ref)

(s/def ::collections-schema
  (s/schema {:collections (s/nilable ::database-ref)}))

(s/fdef q/collections
  :args (s/cat :db (s/? ::database-ref))
  :ret (s/select ::collections-schema [:collections]))

(s/def ::function-schema
  (s/schema {:function string? :scope ::database-schema}))

(s/def ::function-ref
  (s/select ::function-schema [:function]))

(s/fdef q/function
  :args (s/cat :name string? :db (s/? ::database-ref))
  :ret ::function-ref)

(s/def ::functions-schema
  (s/schema {:functions (s/nilable ::database-ref)}))

(s/fdef q/functions
  :args (s/cat :db (s/? ::database-ref))
  :ret (s/select ::functions-schema [:functions]))

(s/def ::index-schema
  (s/schema {:index string? :scope ::database-schema}))

(s/def ::index-ref
  (s/select ::index-schema [:index]))

(s/fdef q/index
  :args (s/cat :name string? :db (s/? ::database-ref))
  :ret ::index-ref)

(s/def ::indexes-schema
  (s/schema {:indexes (s/nilable ::database-ref)}))

(s/fdef q/indexes
  :args (s/cat :db (s/? ::database-ref))
  :ret (s/select ::indexes-schema [:indexes]))

(s/def ::role-schema
  (s/schema {:role string? :scope ::database-schema}))

(s/def ::role-ref
  (s/select ::role-schema [:role]))

(s/fdef q/role
  :args (s/cat :name string? :db (s/? ::database-ref))
  :ret ::role-ref)

(s/def ::roles-schema
  (s/schema {:roles (s/nilable ::database-ref)}))

(s/fdef q/roles
  :args (s/cat :db (s/? ::database-ref))
  :ret (s/select ::roles-schema [:roles]))

(s/def ::documents-schema
  (s/schema {:documents ::collection-schema}))

(s/fdef q/documents
  :args (s/cat :collection ::collection-ref)
  :ret (s/select ::documents-schema [:documents]))

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
  :ret (s/select ::schema-ref-schema [:ref :id]))