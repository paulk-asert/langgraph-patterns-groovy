# Groovy with Embabel for langgraph patterns

Groovy version of this repo: https://github.com/embabel/langgraph-patterns

See also:
* Rod's [blog post](https://medium.com/@springrod/build-better-agents-in-java-vs-python-embabel-vs-langgraph-f7951a0d855c)
* Groovy [blog post](https://groovy.apache.org/blog/embabel-agentic-patterns)

# Setup

Out of the box, the examples assume you have Ollama running with the mistral:7b model.
You can set that up manually, or use docker as follows:

```bash
docker run -d -v ollama:/root/.ollama -p 11434:11434 --name ollama ollama/ollama
docker exec -it ollama ollama run mistral:7b
```

You can of course use other LLMs and other models. Follow the
[Embabel documentation](https://docs.embabel.com/embabel-agent/guide/0.1.3/)
to configure such artifacts if needed.

# Running

Use the Gradle `run` task to start Embabel under Spring Shell:

```bash
./gradlew run
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
