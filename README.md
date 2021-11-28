# bulgogi
Helper for ML feature preprocessing. Very tasty.

# What's in a bulgogi?

Machine learning is cool, but those models are picky beasts.
Most statistical models don't handle anything else beyond numbers,
thus the development of techniques such as one-hot encoding for 
categorical variables (e.g. a country variable).

To create these numerical representations, and create other interesting
variables aka _features_, we must pass data through a preprocessing step
aka _feature engineering_.
Bulgogi is a system to simplify this process, via centralization of
concerns and coupling with a feature store [[1](https://www.tecton.ai/blog/what-is-a-feature-store/)]
[[2](https://medium.com/p/402ade0743b)].

Presently, Bulgogi is nothing more than an idea and this repository is a
playground for experimentation.
The original code started as a [gist here](https://gist.github.com/jcpsantiago/320e3665a9bd749fc25ede0341c6323c).

I'm giving a talk at [re:Clojure 2021](http://www.reclojure.org/#schedule) about it.

Ideas and discussion are welcome!

# How can I try it out?

The main meat in `bulgogi` is the `preprocessed` function.
It takes in a request map
```clj
{:input-data {:current-amount 700
	      :email "squadron42@starfleet.ufp"
	      :items [{:brand "Foo Industries" :value 1234}
		      {:brand "Baz Corp" :value 35345}]}
 :features ["n-digits-in-email-name" 
	    "contains-risky-item"]}
```

looks for functions with the same name as the vals in `:features` in the current 
namespace (likely to change!),
and `pmaps` those fns to the `:input-data`.
Finally, it returns a map with the preprocessed data
```clj
{:n-digits-in-email-name 2
 :contains-risky-item 1}
```
