# langchain4j + Ollama example

## Toolchain
- Java 21 (`<maven.compiler.release>` in `pom.xml`). Do not use preview features.
- Maven 3.9+.
- Test runner: JUnit Jupiter 5.
- Formatter: Spotless + Google Java Format (invoked via `make fmt`).
- All Maven commands in the inner loop use `-o -q` (offline + quiet) — run `make install` once to prime the local repo.

## Build & check
- Fast compile check: `make check`  (`mvn -o -q test-compile`)
- Format:             `make fmt`    (Spotless + Google Java Format)
- Format check:       `make fmt-check`
- Tests / run:        `make test` or `make run` (requires a local Ollama server on the default port)
- Full offline gate:  `make verify` (fmt-check + compile)

The PostToolUse hook (`.claude/hooks/java-check.sh`) fires on every `.java` edit — it Spotless-formats the touched file and runs `mvn -o -q test-compile`. Fix any reported errors before moving on.

The Stop hook (`.claude/hooks/java-stop.sh`) runs `spotless:check` + `test-compile` when Claude finishes its turn. Full tests are excluded because they require a live Ollama server.

## Project structure
- Main class: `com.markwatson.langchain4j_ollama.OllamaLlmLangChain4j`
- Tests exercise the LangChain4j `OllamaChatModel` adapter against a local Ollama daemon.

## Modern Java idioms — always prefer these
- `var` for local variables where the type is obvious from the RHS.
- Records for value types — no getters, no Lombok.
- Switch expressions over classic switch statements.
- Text blocks (`"""..."""`) for multi-line prompts.
- `List.of`, `Map.of`, `Set.of` for immutable collections.
- `java.time` — `Instant`, `Duration`. Never `Date` or `Calendar`.
- `StandardCharsets.UTF_8` — never a bare `"UTF-8"` string.

## Patterns to avoid
- Raw types (`List`, `Map` without `<...>`).
- `System.out.println` for logging in library code — use SLF4J.
- `throws Exception` on every method — declare the actual exception types.
- Lombok — Java 21 records + `var` remove the need.

## Testing notes
- Tests require a running Ollama server — they cannot run in a fully offline sandbox.
- One test class per production class, mirroring package layout under `src/test/java`.
