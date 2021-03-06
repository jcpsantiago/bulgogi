# A mini feature store using bulgogi
This example uses `bulgogi` to build a mini feature store.
It's _mini,_ because it only implements just-in-time features.

## Taking it for a spin
Before trying it out, you need a running Postgres instance e.g.:
```sh
docker run -p 5432:5432 -e POSTGRES_HOST_AUTH_METHOD=trust postgres
```
start the system with `clojure -m example.main` and hit it with a request e.g. using [httpie](https://httpie.io/cli)
```sh
printf '{
  "trigger-id": "42",
  "input-data": {
    "email": "squadron42@starfleet.ufp",
    "created_at": "2021-12-05T10:34:33Z",
  },
  "features": [
    "n-chars-in-email-name",
    "n-digits-in-email-name"
  ]
}'| http  --follow --timeout 3600 POST 'localhost:4242/preprocess' \
 Content-Type:'application/json'
 ```
 which should result in
 ```json
 {
    "n-chars-in-email-name": 10,
    "n-digits-in-email-name": 2
}
 ```
 and three rows in the `calculation_history` table, one for each feature name-value pair.
 Try the same request but add `hour` to the `features` array.
 
 ## Expanding it
 This example only has three features: `n-chars-in-email-name`, `n-digits-in-email-name` and `hour`.
 To expand it and add more, you can simply add new functions which expect an `input-data` map as input to the `example.main` namespace.
 
 It's possible to refactor the single namespace into any sort of organisation you like, as long as a single namespace is passed to `preprocessed`. Although be careful with naming conflicts, a problem we haven't solved yet (see discussion in https://github.com/jcpsantiago/bulgogi/issues/6).
 
 ## TODO
 * train simple model using data from the feature store
