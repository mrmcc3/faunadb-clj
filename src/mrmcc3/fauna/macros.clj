(ns mrmcc3.fauna.macros
  "familiar clojure macros that output FQL instead"
  {:author "Michael McClintock"}
  (:refer-clojure :exclude [fn map])
  (:require
    [mrmcc3.fauna.query :as q]
    [clojure.core :as c]))

;; very naive implementations

(defmacro fn [args expr]
  (let [names    (c/map name args)
        vars     (c/map (c/fn [n] `(q/var' ~n)) names)
        bindings (interleave args vars)]
    `(let [~@bindings] (q/lambda [~@names] ~expr))))

(defmacro map [fn coll]
  `(q/map ~coll ~fn))

