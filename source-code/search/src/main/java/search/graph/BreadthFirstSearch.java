package search.graph;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

/**
 * Breadth First Graph search
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
public class BreadthFirstSearch extends AbstractGraphSearch {

    /** findPath - abstract method in super class */
    public int [] findPath(int start_node, int goal_node) { // return an array of node indices
        System.out.println("Entered BreadthFirstSearch.findPath(" +
                           start_node + ", " + goal_node + ")");
        // data structures for breadth first search:
        boolean [] alreadyVisitedFlag = new boolean[numNodes];
        int [] predecessor = new int[numNodes];
        Deque<Integer> queue = new ArrayDeque<>(numNodes + 2);

        Arrays.fill(alreadyVisitedFlag, false);
        Arrays.fill(predecessor, -1);

        alreadyVisitedFlag[start_node] = true;
        queue.addLast(start_node);
    outer:
        while (!queue.isEmpty()) {
            int head = queue.peekFirst();
            int [] connected = connected_nodes(head);
            if (connected != null) {
                for (int node : connected) {
                    if (!alreadyVisitedFlag[node]) {
                        predecessor[node] = head;
                        queue.addLast(node);
                        if (node == goal_node) break outer; // we are done
                    }
                }
                alreadyVisitedFlag[head] = true;
                queue.removeFirst(); // ignore return value
            }
        }
        // now calculate the shortest path from the predecessor array:
        int [] ret = new int[numNodes + 1];
        int count = 0;
        ret[count++] = goal_node;
        for (int i=0; i<numNodes; i++) {
            ret[count] = predecessor[ret[count - 1]];
            count++;
            if (ret[count - 1] == start_node)  break;  // back to starting node
        }
        int [] ret2 = new int[count];
        for (int i=0; i<count; i++) {
            ret2[i] = ret[count - 1 - i];
        }
        return ret2;
    }

    protected int [] connected_nodes(int node) {
        int [] ret = new int[AbstractGraphSearch.MAX];
        int num = 0;

        for (int n=0; n<numNodes; n++) {
            boolean connected = false;
            // See if there is a link between node 'node' and 'n':
            for (int i=0; i<numLinks; i++) {
                if (link_1[i] == node && link_2[i] == n) {
                    connected = true;
                    break;
                }
                if (link_2[i] == node && link_1[i] == n) {
                    connected = true;
                    break;
                }
            }
            if (connected) {
                ret[num++] = n;
            }
        }
        if (num == 0)  return null;
        return Arrays.copyOf(ret, num);
    }

}
