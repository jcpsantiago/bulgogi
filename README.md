[![CI](https://github.com/jcpsantiago/bulgogi/actions/workflows/run-tests.yaml/badge.svg)](https://github.com/jcpsantiago/bulgogi/actions/workflows/run-tests.yaml)

# bulgogi
Helper for ML feature preprocessing. Very tasty.

# What's in a bulgogi?

Machine learning is cool, but those models are picky beasts.
Most statistical models don't handle anything else beyond numbers,
thus the development of techniques such as one-hot encoding for 
categorical variables (e.g. a country variable).

To create these numerical representations, and create other interesting
variables aka _features_, we must pass data through a preprocessing step
aka _feature engineering_. If you work in an environment with several data science teams, it is likely several teams have reimplemented the same feature engineering code in different projects. This is especially true in polyglot teams (e.g. some people prefer R, others Python, yet a growing number Clojure).

`bulgogi` is a system to simplify this process, via centralization of
concerns and coupling with a feature store [[1](https://www.tecton.ai/blog/what-is-a-feature-store/)]
[[2](https://medium.com/p/402ade0743b)]. The final goal is to increase sharing of code, and ensure point-in-time correctness of training data.

![a diagram showing bulgogi getting requests from a model in production, storing the results to a database and training a new model with data from that database without redoing feature engineering](/doc/bulgogi_diagram.png "Bulgogi as the central feature repository")

Presently, `bulgogi` is an idea and this repository is a
playground for experimentation. Examples of applications and use-cases will be added as development evolves.
The original code started as a [gist here](https://gist.github.com/jcpsantiago/320e3665a9bd749fc25ede0341c6323c). 

Although this introduction and examples in [/example](https://github.com/jcpsantiago/bulgogi/tree/main/example) 
focus on using `bulgogi` within the context of production ML models, this library is 
generic enough for other use-cases where mapping arbitrary functions over data is useful.

I gave a [talk](https://youtu.be/3C1QQXEg_F8?t=25091) at [re:Clojure 2021](http://www.reclojure.org/#schedule) about it.

Ideas and discussion are welcome!


# What bulgogi is not
This is definitly not a replacement for ML pipelines (sci-kit learn pipelines, tidymodels workflows) in all situations.
If the cost of higher latency (no benchmarks yet about how much) is higher than the cost of quicker collaboration, then by all means use a pipeline.

# Pros and cons
## Pros
In environments where multiple teams, or multiple people, need to ship ML models to live production environments, Bulgogi can
* reduce the time it takes to engineer features -- maybe a teammate has build what you need already
* decouple model training from deployment, so you ship smaller files, which load faster and need to track fewer things
* allow ML practitioners to use whatever language they need to create models
* reduce the amount of data cleaning and wrangling needed before training models

## Cons
* adds latency in comparison to inlined code (benchmarks soon!)
* naming features needs to be explicit, and potentially long to avoid conflicts between namespaces e.g. if two areas of your company/team call different things by the same
* all features must be written in Clojure (only a con if nobody knows Clojure in your team/company)

# Installation

`bulgogi` is not in Clojars yet, but you can try it with `deps.edn`:
```clj
{:deps {io.github.jcpsantiago/bulgogi {:git/url "https://github.com/jcpsantiago/bulgogi/"
				       :git/sha "278ce2738f26d4100b3470f133f682ad450662c4"}}
```

# Usage
You can see an example implementation in [/example](https://github.com/jcpsantiago/bulgogi/tree/main/example).

The main meat in `bulgogi` is the `preprocessed` function.
It takes in a request map with keys `:input-data` (another map) and `:features` (a vector of strings) e.g.
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

# Contributing
Issues, PRs, ideas, criticism are all welcome :)

# TODO
* Create a _reference implementation_, potentially dockerized for fast deployment and testing
* Benchmark Bulgogi vs inlined code (gold standard) and other libraries
* Experiment wrapping Pathom 3 to get more generalised dependency resolution
* Declarative interface for calling external APIs as co-effects

# License
Bulgogi is shared under the Eclipse Public License 1.0.
