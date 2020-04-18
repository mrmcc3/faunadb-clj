
### faunadb-clj

An experimental clojure driver for FaunaDB. Requires Java 11.

### Todo

- complete query ops
- attempt macros
- look at fauna response decoding
- REPL use
- REBL use, datafy/nav seems like a fit
- Spec integration
- Add examples/usage

```
$ clojure -A:test
```

### notes

I'm unsure how to represent `Ref` types returned from fauna.
They come back as a confusing nested map with `@ref` keys. 
As far as I can tell they can't be sent back in say a get 
query without some form of transformation.

I've thought more about datafy/nav and it should work well.
The current plan is if you make a query then the response 
can implement navigable and include the client used
to make the request as metadata. This means nav can make 
further api requests. It could also grab the 
timestamp and scope all further reads with At.

This will be very interesting to impl for things
like pagination, graph refs etc. you could in theory explore 
the database lazily as an immutable value.
