# bulgogi
A feature preprocessing system. Very tasty.

# What's in a bulgogi?

Machine learning model are cool, but they're also picky beasts.
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

Bulgogi is supposed to become the central repository for features
which need transformations ("just-in-time"), but it does not prescribe
a way to be served.
It's up to you to wrap it in an API, CLI, <add your preferred interface>.
Presently, you can try it in the REPL :)

