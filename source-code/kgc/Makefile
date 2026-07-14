.PHONY: install run test check fmt fmt-check verify clean

MVN := mvn -o -q

install:
	mvn -q dependency:go-offline
	mvn -q test-compile spotless:apply

# Run the knowledge-graph-creator pipeline: extract RDF from source text, then
# de-dup statements. Requires network for DBPedia SPARQL lookups.
run:
	$(MVN) test
	@echo "Removing duplicate RDF statements"
	awk '!visited[$$0]++' output_with_duplicates.rdf > output.rdf
	rm -f output_with_duplicates.rdf

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
	rm -f output.rdf output_with_duplicates.rdf
