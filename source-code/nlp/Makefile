.PHONY: install run test names autotagger fasttag check fmt fmt-check verify clean

MVN := mvn -o -q

install:
	mvn -q dependency:go-offline
	mvn -q test-compile spotless:apply

# Default run target: name extraction.
run: names

names:
	$(MVN) exec:java -Dexec.mainClass="com.markwatson.nlp.ExtractNames"

autotagger:
	$(MVN) exec:java -Dexec.mainClass="com.markwatson.nlp.AutoTagger"

fasttag:
	$(MVN) exec:java -Dexec.mainClass="com.markwatson.nlp.FastTag"

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
