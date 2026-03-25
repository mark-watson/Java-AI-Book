JAR := target/agentscope-gemini-1.0.0-SNAPSHOT.jar

.PHONY: run run-tool clean

$(JAR):
	mvn package -q

## Run the hello-world Main example
run: $(JAR)
	java -cp $(JAR) com.markwatson.agentscope.Main

## Run the tool-use weather example
run-tool: $(JAR)
	java -cp $(JAR) com.markwatson.agentscope.ToolUseExample

clean:
	mvn clean -q
