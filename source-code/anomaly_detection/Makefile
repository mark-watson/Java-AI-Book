.PHONY: install run wisconsin test check fmt fmt-check verify clean

MVN := mvn -o -q

install:
	mvn -q dependency:go-offline
	mvn -q test-compile spotless:apply

# Default run target: Wisconsin Breast Cancer anomaly detection example.
run: wisconsin

wisconsin:
	$(MVN) exec:java -Dexec.mainClass="com.markwatson.anomaly_detection.WisconsinAnomalyDetection"

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
