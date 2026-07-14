# Google Gemini LLM client example

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
- Tests / run:        `make test` (requires `GEMINI_API_KEY` env var and network)
- Full offline gate:  `make verify` (fmt-check + compile)

The PostToolUse hook (`.claude/hooks/java-check.sh`) fires on every `.java` edit — it Spotless-formats the touched file and runs `mvn -o -q test-compile`.

The Stop hook (`.claude/hooks/java-stop.sh`) runs `spotless:check` + `test-compile` when Claude finishes its turn. Full tests are excluded because they require network access to the Gemini API.

## Project structure
- Main class: `com.markwatson.gemini.GeminiCompletions`
- Uses `java.net.http.HttpClient` to call the Gemini REST API directly (no SDK).

## Modern Java idioms — always prefer these
- `var` for local variables where the type is obvious from the RHS.
- Records for value types — no getters, no Lombok.
- Switch expressions over classic switch statements.
- Text blocks (`"""..."""`) for multi-line prompts / JSON bodies.
- `List.of`, `Map.of`, `Set.of` for immutable collections.
- `java.net.http.HttpClient` — no Apache HttpClient, no OkHttp, no `URLConnection`.
- `java.time` — `Instant`, `Duration`. Never `Date` or `Calendar`.
- `StandardCharsets.UTF_8` — never a bare `"UTF-8"` string.

## Patterns to avoid
- Raw types (`List`, `Map` without `<...>`).
- `System.out.println` for logging in library code — use SLF4J.
- `throws Exception` on every method — declare the actual exception types.
- Lombok — Java 21 records + `var` remove the need.

## Testing notes
- Tests hit the live Gemini API — set `GEMINI_API_KEY` and expect network I/O.
