package search.maze;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * 2D Maze Search
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
public class BreadthFirstSearchEngine extends AbstractSearchEngine {
    public BreadthFirstSearchEngine(int width, int height) {
        super(width, height);
        doSearchOn2DGrid();
    }

    private void doSearchOn2DGrid() {
        int width = maze.getWidth();
        int height = maze.getHeight();
        boolean[][] alreadyVisitedFlag = new boolean[width][height];
        Location[][] predecessor = new Location[width][height];
        Deque<Location> queue = new ArrayDeque<>();

        for (int i=0; i<width; i++) {
            for (int j=0; j<height; j++) {
                alreadyVisitedFlag[i][j] = false;
                predecessor[i][j] = null;
            }
        }

        alreadyVisitedFlag[startLoc.x()][startLoc.y()] = true;
        queue.addLast(startLoc);
        boolean success = false;
    outer:      
        while (!queue.isEmpty()) {
            Location head = queue.peekFirst();
            if (head == null) break;
            List<Location> connected = getPossibleMoves(head);
            for (Location loc : connected) {
                int w = loc.x();
                int h = loc.y();
                if (!alreadyVisitedFlag[w][h]) {
                    alreadyVisitedFlag[w][h] = true;
                    predecessor[w][h] = head;
                    queue.addLast(loc);
                    if (equals(loc, goalLoc)) {
                        success = true;
                        break outer; // we are done
                    }
                }
            }
            queue.removeFirst(); // ignore return value
        }
        // now calculate the shortest path from the predecessor array:
        maxDepth = 0;
        if (success) {
            searchPath[maxDepth++] = goalLoc;
            for (int i=0; i<100; i++) {
                searchPath[maxDepth] = predecessor[searchPath[maxDepth - 1].x()][searchPath[maxDepth - 1].y()];
                maxDepth++;
                if (equals(searchPath[maxDepth - 1], startLoc))  break;  // back to starting node
            }
        }
    }
}
