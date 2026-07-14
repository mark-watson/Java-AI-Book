.PHONY: install run test check fmt fmt-check verify clean

MVN := mvn -o -q

install:
	mvn -q dependency:go-offline
	mvn -q test-compile spotless:apply

# Run the JUnit example (requires OPENAI_API_KEY env var and network).
run: test

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
