# Knowledge Graph Creator (KGC)

## Toolchain
- Java 21 (`<maven.compiler.release>` in `pom.xml`). Do not use preview features.
- Maven 3.9+.
- Test runner: JUnit Jupiter 5.
- Formatter: Spotless + Google Java Format (invoked via `make fmt`).
- All Maven commands in the inner loop use `-o -q` (offline + quiet) — run `make install` once to prime the local repo.
- **Dependency ordering:** requires `ner_dbpedia` to be `make install`-ed first so its artifact is in `~/.m2`.

## Build & check
- Fast compile check: `make check`  (`mvn -o -q test-compile`)
- Format:             `make fmt`
- Format check:       `make fmt-check`
- Tests:              `make test` (hits DBPedia SPARQL endpoint — network required)
- Full offline gate:  `make verify` (fmt-check + compile — tests excluded because they require network)
- Run:                `make run` (runs the pipeline, then `awk`-dedups `output_with_duplicates.rdf` → `output.rdf`)

The PostToolUse hook (`.claude/hooks/java-check.sh`) fires on every `.java` edit — it Spotless-formats the touched file and runs `mvn -o -q test-compile`.

The Stop hook (`.claude/hooks/java-stop.sh`) runs `spotless:check` + `test-compile` when Claude finishes its turn. Full tests are excluded because they require DBPedia network access.

## Project structure
- Package: `com.knowledgegraphcreator`
- Main class: `KGC`
- Pipeline: OpenNLP tokenize/NER → resolve entities against DBPedia via `ner_dbpedia` → emit RDF N-Triples → dedup.

## Modern Java idioms — always prefer these
- `var` for local variables where the type is obvious from the RHS.
- Records for value types — no getters, no Lombok.
- Text blocks (`"""..."""`) for multi-line SPARQL queries.
- `List.of`, `Map.of`, `Set.of` for immutable collections.
- `java.time` — `Instant`, `Duration`. Never `Date` or `Calendar`.
- `StandardCharsets.UTF_8` — never a bare `"UTF-8"` string.

## Patterns to avoid
- Raw types (`List`, `Map` without `<...>`).
- `System.out.println` for logging in library code — use SLF4J.
- `throws Exception` on every method — declare the actual exception types.
- Lombok — Java 21 records + `var` remove the need.
