.PHONY: install run test check fmt fmt-check verify clean

# Offline + quiet flags keep Maven fast after the first resolve.
MVN := mvn -o -q

# First-time setup: prime the local ~/.m2 repo so subsequent -o builds work
# offline. Also warms Spotless's google-java-format cache.
install:
	mvn -q dependency:go-offline
	mvn -q test-compile spotless:apply

# Run the JUnit example (requires a running local Ollama server).
run: test

# Full test suite (requires network / local Ollama).
test:
	$(MVN) test

# Fast compile check — the inner-loop equivalent of `cargo c` / `tsc --noEmit`
check:
	$(MVN) test-compile

# Format all Java sources with Google Java Format via Spotless
fmt:
	$(MVN) spotless:apply

# Verify formatting without modifying files
fmt-check:
	$(MVN) spotless:check

# Full offline gate: format-check + compile
verify: fmt-check check

clean:
	mvn -q clean
