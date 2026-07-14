.PHONY: install run test 1H 2H 2H_momentum check fmt fmt-check verify clean

MVN := mvn -o -q

install:
	mvn -q dependency:go-offline
	mvn -q test-compile spotless:apply

# Default run target: run all three neural-network variants.
run: 1H 2H 2H_momentum

# Individual variants
1H:
	$(MVN) test -Dtest=NeuralNetwork_1H_Test

2H:
	$(MVN) test -Dtest=NeuralNetwork_2H_Test

2H_momentum:
	$(MVN) test -Dtest=NeuralNetwork_2H_momentum_Test

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
