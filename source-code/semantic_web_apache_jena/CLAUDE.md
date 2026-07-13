# Apache Jena Semantic Web project

## Toolchain
- Java 21 (`<maven.compiler.release>` in `pom.xml`). Do not use preview features.
- Maven 3.9+.
- Test runner: JUnit Jupiter 5.
- Formatter: Spotless + Google Java Format (invoked via `make fmt`).
- All Maven commands in the inner loop use `-o -q` (offline + quiet) — run `make install` once to prime the local repo.

## Build & check (run after every edit)
- Fast compile check: `make check`  (`mvn -o -q test-compile`)
- Format:             `make fmt`    (Spotless + Google Java Format)
- Format check:       `make fmt-check`
- Tests:              `make test`   (requires network — hits remote SPARQL endpoints)
- Full offline gate:  `make verify` (fmt-check + compile — same gate the Stop hook runs)
- Run the example:    `make run`

The PostToolUse hook (`.claude/hooks/java-check.sh`) fires on every `.java` edit — it Spotless-formats the touched file and runs `mvn -o -q test-compile`. Fix any reported errors before moving on.

The Stop hook (`.claude/hooks/java-stop.sh`) runs `spotless:check` + `test-compile` when Claude finishes its turn. Full tests are excluded because they require live network access to DBPedia/SPARQL endpoints.

## Project structure
- Main class: `com.markwatson.semanticweb.JenaApis`
- RDF data files: `data/` directory (`.nt`, `.n3` formats)
- Key classes: `JenaApis` (main API), `Cache` (SPARQL result caching), `QueryResult` (result wrapper)

## Modern Java idioms — always prefer these
- `var` for local variables where the type is obvious from the RHS.
- Records for value types — no getters, no Lombok.
- Switch expressions over classic switch statements.
- Text blocks (`"""..."""`) for multi-line SPARQL queries.
- `List.of`, `Map.of`, `Set.of` for immutable collections.
- `java.time` — `Instant`, `Duration`. Never `Date` or `Calendar`.
- `StandardCharsets.UTF_8` — never a bare `"UTF-8"` string.

## Patterns to avoid
- Raw types (`List`, `Map` without `<...>`).
- `System.out.println` for logging in library code — use SLF4J.
- `throws Exception` on every method — declare the actual exception types.
- Lombok — Java 21 records + `var` remove the need.

## Testing notes
- Tests hit live remote endpoints (DBPedia SPARQL) — they cannot run offline.
- Run `make test` manually when you have internet access; do not add network tests to the offline gate.
- One test class per production class, mirroring package layout under `src/test/java`.
