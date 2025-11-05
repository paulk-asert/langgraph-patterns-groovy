# Groovy with Embabel for langgraph patterns

Groovy version of this repo: https://github.com/embabel/langgraph-patterns

See also: [related blog post](https://medium.com/@springrod/build-better-agents-in-java-vs-python-embabel-vs-langgraph-f7951a0d855c)

# Running

Use the Gradle `run` task to start Embabel under Spring Shell:

```bash
./gradle run
```

When the Embabel shell comes up, run the blog title generator like this:

```
blogs topics "Why Groovy is a great option to run your Embabel applications"
```

Invoke the draft and refine agent like this:

```
x "Draft and refine [text you want]"
```

Invoke the sentiment analysis flow like this:

```
x "Analyze sentiment and respond to '[text you want to respond to]' "
```
