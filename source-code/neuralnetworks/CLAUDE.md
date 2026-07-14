# Neural networks from scratch

## Toolchain
- Java 21 (`<maven.compiler.release>` in `pom.xml`). Do not use preview features.
- Maven 3.9+.
- Test runner: JUnit Jupiter 5 (the network variants are driven from JUnit tests as `main`-style demos).
- Formatter: Spotless + Google Java Format (invoked via `make fmt`).
- All Maven commands in the inner loop use `-o -q` (offline + quiet) — run `make install` once to prime the local repo.

## Build & check
- Fast compile check: `make check`  (`mvn -o -q test-compile`)
- Format:             `make fmt`
- Format check:       `make fmt-check`
- Tests:              `make test` (runs all three variants)
- Full offline gate:  `make verify` (fmt-check + compile)
- Run individual variant: `make 1H`, `make 2H`, or `make 2H_momentum`
- Run all three:      `make run`

The PostToolUse hook (`.claude/hooks/java-check.sh`) fires on every `.java` edit — it Spotless-formats the touched file and runs `mvn -o -q test-compile`.

The Stop hook (`.claude/hooks/java-stop.sh`) runs `spotless:check` + `test-compile` when Claude finishes its turn.

## Project structure
- Package: `com.markwatson.neuralnetworks`
- Main classes:
  - `Neural_1H` — 1-hidden-layer feedforward net with back-propagation.
  - `Neural_2H` — 2-hidden-layer feedforward net.
  - `Neural_2H_momentum` — 2-hidden-layer net with momentum term.
  - `Graph`, `GraphPanel`, `Plot1DPanel`, `Plot2DPanel` — Swing visualization utilities.

## Modern Java idioms — always prefer these
- `var` for local variables where the type is obvious from the RHS.
- Records for value types — no getters, no Lombok.
- Switch expressions over classic switch statements.
- `List.of`, `Map.of`, `Set.of` for immutable collections.
- `java.util.random.RandomGenerator` (Java 17+) in new code.

## Patterns to avoid
- Raw types (`List`, `Map` without `<...>`).
- `System.out.println` for logging in library code — use SLF4J.
- `throws Exception` on every method — declare the actual exception types.
- Lombok — Java 21 records + `var` remove the need.
