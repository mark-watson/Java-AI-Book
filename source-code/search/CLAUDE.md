# Search algorithms (game trees + graph traversal)

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
- Run individual example: `make chess`, `make tictactoe`, `make graph`, or `make maze`
- Default `make run` is `make tictactoe`

The PostToolUse hook (`.claude/hooks/java-check.sh`) fires on every `.java` edit — it Spotless-formats the touched file and runs `mvn -o -q test-compile`.

The Stop hook (`.claude/hooks/java-stop.sh`) runs `spotless:check` + `test-compile` when Claude finishes its turn.

## Project structure
- Root package: `search` with sub-packages:
  - `search.game` — `Chess`, `TicTacToe`, minimax with alpha-beta pruning.
  - `search.graph` — breadth-first / depth-first search over abstract graphs.
  - `search.maze` — BFS/DFS over a grid maze.

## Modern Java idioms — always prefer these
- `var` for local variables where the type is obvious from the RHS.
- Records for value types — no getters, no Lombok.
- Sealed classes + pattern matching over `instanceof` chains — a natural fit for `Position` and `Move` hierarchies.
- Switch expressions over classic switch statements.
- `List.of`, `Map.of`, `Set.of` for immutable collections.

## Patterns to avoid
- Raw types (`List`, `Map` without `<...>`).
- `System.out.println` for logging in library code — use SLF4J.
- `throws Exception` on every method — declare the actual exception types.
- Lombok — Java 21 records + `var` remove the need.
- Static mutable state in the search classes — pass state on the stack instead.
