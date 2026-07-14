# NLP library (name extraction, part-of-speech tagging, auto-tagging)

## Toolchain
- Java 21 (`<maven.compiler.release>` in `pom.xml`). Do not use preview features.
- Maven 3.9+.
- Test runner: JUnit Jupiter 5.
- Formatter: Spotless + Google Java Format (invoked via `make fmt`).
- All Maven commands in the inner loop use `-o -q` (offline + quiet) — run `make install` once to prime the local repo.

## Build & check
- Fast compile check: `make check`  (`mvn -o -q test-compile`)
- Format:             `make fmt`
- Format check:       `make fmt-check`
- Tests:              `make test`
- Full offline gate:  `make verify` (fmt-check + compile)
- Run individual example: `make names`, `make autotagger`, or `make fasttag`
- Default `make run` is `make names`

The PostToolUse hook (`.claude/hooks/java-check.sh`) fires on every `.java` edit — it Spotless-formats the touched file and runs `mvn -o -q test-compile`.

The Stop hook (`.claude/hooks/java-stop.sh`) runs `spotless:check` + `test-compile` when Claude finishes its turn.

## Project structure
- Package: `com.markwatson.nlp` (main), `com.markwatson.nlp.util` (utilities), `public_domain` (public-domain Porter stemmer).
- Main classes:
  - `ExtractNames` — person / place / organization NER.
  - `AutoTagger` — assigns topic tags to documents.
  - `FastTag` — lightweight part-of-speech tagger.
  - `ComparableDocument` — TF-IDF style document comparison.

## Modern Java idioms — always prefer these
- `var` for local variables where the type is obvious from the RHS.
- Records for value types — no getters, no Lombok.
- Switch expressions over classic switch statements.
- Text blocks (`"""..."""`) for multi-line strings.
- `List.of`, `Map.of`, `Set.of` for immutable collections.
- `Files.readString(path)`, `Files.writeString(path, s)` — the Java 11 one-liners.
- `StandardCharsets.UTF_8` — never a bare `"UTF-8"` string.

## Patterns to avoid
- Raw types (`List`, `Map` without `<...>`).
- `System.out.println` for logging in library code — use SLF4J.
- `throws Exception` on every method — declare the actual exception types.
- Lombok — Java 21 records + `var` remove the need.
