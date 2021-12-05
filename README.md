# bulgogi
Helper for ML feature preprocessing. Very tasty.

# What's in a bulgogi?

Machine learning is cool, but those models are picky beasts.
Most statistical models don't handle anything else beyond numbers,
thus the development of techniques such as one-hot encoding for 
categorical variables (e.g. a country variable).

To create these numerical representations, and create other interesting
variables aka _features_, we must pass data through a preprocessing step
aka _feature engineering_. If you work in an environment with several data science teams, it is likely several teams have reimplemented the same feature engineering code in different projects. This is especially true in polyglot teams (e.g. some people prefer R, others Python).

Bulgogi is a system to simplify this process, via centralization of
concerns and coupling with a feature store [[1](https://www.tecton.ai/blog/what-is-a-feature-store/)]
[[2](https://medium.com/p/402ade0743b)]. The final goal is to increase sharing of code, and ensure point-in-time correctness of training data.

![a diagram showing bulgogi getting requests from a model in production, storing the results to a database and training a new model with data from that database without redoing feature engineering](/doc/bulgogi_diagram.png "Bulgogi as the central feature repository")

Presently, Bulgogi is nothing more than an idea and this repository is a
playground for experimentation.
The original code started as a [gist here](https://gist.github.com/jcpsantiago/320e3665a9bd749fc25ede0341c6323c).

I gave a [talk](https://youtu.be/3C1QQXEg_F8?t=25091) at [re:Clojure 2021](http://www.reclojure.org/#schedule) about it.

Ideas and discussion are welcome!

# How can I try it out?

You can see an example implementation in the `/example` dir.

The main meat in `bulgogi` is the `preprocessed` function.
It takes in a request map with keys `:input-data` (another map) and `:features` (a vector of strings)
```clj
{:input-data {:current-amount 700
	      :email "squadron42@starfleet.ufp"
 :features ["n-digits-in-email-name" 
	    "contains-risky-item"]}
```

and a namespace e.g. `'example.main'` to look for functions with the same name as the vals in `:features`,
then `pmaps` those fns over the `:input-data`.
Finally, it returns a map with the preprocessed data
```clj
{:n-digits-in-email-name 2
 :contains-risky-item 1}
```
