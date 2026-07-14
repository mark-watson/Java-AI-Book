# Genetic algorithms example

## Toolchain
- Java 21 (`<maven.compiler.release>` in `pom.xml`). Do not use preview features.
- Maven 3.9+.
- Test runner: JUnit Jupiter 5 (there are no unit tests in this project — the runnable `TestGenetic` demonstrates the algorithm).
- Formatter: Spotless + Google Java Format (invoked via `make fmt`).
- All Maven commands in the inner loop use `-o -q` (offline + quiet) — run `make install` once to prime the local repo.

## Build & check
- Fast compile check: `make check`  (`mvn -o -q test-compile`)
- Format:             `make fmt`
- Format check:       `make fmt-check`
- Tests:              `make test`
- Full offline gate:  `make verify` (fmt-check + compile)
- Run:                `make run` — runs `TestGenetic`

The PostToolUse hook (`.claude/hooks/java-check.sh`) fires on every `.java` edit — it Spotless-formats the touched file and runs `mvn -o -q test-compile`.

The Stop hook (`.claude/hooks/java-stop.sh`) runs `spotless:check` + `test-compile` when Claude finishes its turn.

## Project structure
- Package: `com.markwatson.geneticalgorithm`
- Main classes:
  - `Genetic` — abstract GA base class (fitness / crossover / mutation).
  - `TestGenetic` — concrete example that evolves a bitstring toward all-ones.

## Modern Java idioms — always prefer these
- `var` for local variables where the type is obvious from the RHS.
- Records for value types — no getters, no Lombok.
- Switch expressions over classic switch statements.
- `List.of`, `Map.of`, `Set.of` for immutable collections.
- `java.util.random.RandomGenerator` (Java 17+) over legacy `java.util.Random` in new code.

## Patterns to avoid
- Raw types (`List`, `Map` without `<...>`).
- `System.out.println` for logging in library code — use SLF4J.
- `throws Exception` on every method — declare the actual exception types.
- Lombok — Java 21 records + `var` remove the need.
