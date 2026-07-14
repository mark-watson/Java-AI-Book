.PHONY: install run run-tool test check fmt fmt-check verify clean

MVN := mvn -o -q

JAR := target/agentscope-gemini-1.0.0-SNAPSHOT.jar

install:
	mvn -q dependency:go-offline
	mvn -q package spotless:apply

$(JAR):
	mvn -q package

# Default run target: hello-world Main example (requires GEMINI_API_KEY).
run: $(JAR)
	java -cp $(JAR) com.markwatson.agentscope.Main

# Tool-use weather example (requires GEMINI_API_KEY).
run-tool: $(JAR)
	java -cp $(JAR) com.markwatson.agentscope.ToolUseExample

test:
	$(MVN) test

check:
	$(MVN) test-compile

fmt:
	$(MVN) spotless:apply

fmt-check:
	$(MVN) spotless:check

verify: fmt-check check

clean:
	mvn -q clean
