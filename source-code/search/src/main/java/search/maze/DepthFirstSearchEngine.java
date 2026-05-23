package search.maze;

import java.util.List;

/**
 * 2D Maze Search: Performs a depth first search in a maze
 *
 * <p/>
 * Copyright 1998-2012 by Mark Watson. All rights reserved.
 * <p/>
 * This software is can be used under either of the following licenses:
 * <p/>
 * 1. LGPL v3<br/>
 * 2. Apache 2
 * <p/>
 */
public class DepthFirstSearchEngine extends AbstractSearchEngine {
    public DepthFirstSearchEngine(int width, int height) {
        super(width, height);
        iterateSearch(startLoc, 1);
    }

    private void iterateSearch(Location loc, int depth) {
        if (!isSearching) return;
        maze.setValue(loc.x(), loc.y(), (short)depth);
        List<Location> moves = getPossibleMoves(loc);
        for (Location move : moves) {
            searchPath[depth] = move;
            if (equals(move, goalLoc)) {
                System.out.println("Found the goal at " + move.x() +
                                   ", " + move.y());
                isSearching = false;
                maxDepth = depth;
                return;
            } else {
                iterateSearch(move, depth + 1);
                if (!isSearching) return;
            }
        }
    }
}
