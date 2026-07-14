.PHONY: install run test chess tictactoe graph maze check fmt fmt-check verify clean

MVN := mvn -o -q

install:
	mvn -q dependency:go-offline
	mvn -q test-compile spotless:apply

# Default run target: tic-tac-toe (fast, non-interactive-friendly).
run: tictactoe

chess:
	$(MVN) exec:java -Dexec.mainClass="search.game.Chess"

tictactoe:
	$(MVN) exec:java -Dexec.mainClass="search.game.TicTacToe"

graph:
	$(MVN) exec:java -Dexec.mainClass="search.graph.GraphDepthFirstSearch"

maze:
	$(MVN) exec:java -Dexec.mainClass="search.maze.MazeBreadthFirstSearch"

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
