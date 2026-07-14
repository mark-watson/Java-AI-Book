.PHONY: install run main test check fmt fmt-check verify clean

MVN := mvn -o -q

# First-time setup: prime the local ~/.m2 repo (also builds the fat jar so
# `make run` works). Note: requires ner_dbpedia and semantic_web_apache_jena
# to have been installed first (they are `mvn install`-ed dependencies).
install:
	mvn -q dependency:go-offline
	mvn -q package -DskipTests spotless:apply

# Default run target: interactive Knowledge Graph Navigator.
run: main

main:
	mvn -q package -DskipTests
	java -jar target/KGN/KGN.jar

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
