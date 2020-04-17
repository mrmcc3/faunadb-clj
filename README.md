
### faunadb-clj

A clojure driver for FaunaDB.


### Todo

- at some point we might have to move from c.d.json to something jackson based.
something like metosin/jsonista. But I'll see how far I can get.
    - yep i've found something it can't handle. array of objects
- impl. write query ops
- attempt macros.

```
$ clojure -A:test
```