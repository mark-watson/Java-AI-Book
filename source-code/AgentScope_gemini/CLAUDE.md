# AgentScope + Gemini example

## Toolchain
- Java 21 (`<maven.compiler.source>` / `<maven.compiler.target>` in `pom.xml`). Do not use preview features.
- Maven 3.9+.
- Formatter: Spotless + Google Java Format (invoked via `make fmt`).
- All Maven commands in the inner loop use `-o -q` (offline + quiet) — run `make install` once to prime the local repo.
- Uses `maven-shade-plugin` to produce a fat/uber jar `target/agentscope-gemini-1.0.0-SNAPSHOT.jar`.

## Build & check
- Fast compile check: `make check`  (`mvn -o -q test-compile`)
- Format:             `make fmt`
- Format check:       `make fmt-check`
- Tests:              `make test`
- Full offline gate:  `make verify` (fmt-check + compile)
- Run hello-world Main example: `make run` (requires `GEMINI_API_KEY`)
- Run tool-use weather example: `make run-tool` (requires `GEMINI_API_KEY`)

The PostToolUse hook (`.claude/hooks/java-check.sh`) fires on every `.java` edit — it Spotless-formats the touched file and runs `mvn -o -q test-compile`.

The Stop hook (`.claude/hooks/java-stop.sh`) runs `spotless:check` + `test-compile` when Claude finishes its turn.

## Project structure
- Package: `com.markwatson.agentscope`
- Main classes:
  - `Main` — ReActAgent hello-world.
  - `ToolUseExample` — weather-tool function-calling demo.
  - `GeminiConfig` — configures the AgentScope `Model` for Gemini via the Google GenAI SDK.

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
- Uses live Gemini API — set `GEMINI_API_KEY`. Tests are minimal; the runnable Main classes are the primary demonstrations.
