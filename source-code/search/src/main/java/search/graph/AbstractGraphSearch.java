package search.graph;

import java.util.Arrays;

/**
 * Graph search
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
abstract public class AbstractGraphSearch {

    public void addNode(String name, int x, int y) {
        System.out.println("Adding node: " + name + ", " + x + ", " + y);
        nodeNames[numNodes] = name;
        node_x[numNodes] = x;
        node_y[numNodes] = y;
        numNodes++;
    }

    public int getNumNodes() { return numNodes; }
    public int getNumLinks() { return numLinks; }

    public String getNodeName(int index) {
        if (index < 0 || index >= numNodes) {
            System.out.println("Error in getNodeName: index " + index + " out of bounds");
            return "no name"; // error condition
        }
        return nodeNames[index];
    }

    public int getNodeX(int index) {
        if (index < 0 || index >= numNodes) {
            System.out.println("Error in getNodeX: index " + index + " out of bounds");
            return 0;  // error condition
        }
        return node_x[index];
    }

    public int getNodeY(int index) {
        if (index < 0 || index >= numNodes) {
            System.out.println("Error in getNodeY: index " + index + " out of bounds");
            return 0;  // error condition
        }
        return node_y[index];
    }

    public int getLink1(int index) {
        return link_1[index];
    }

    public int getLink2(int index) {
        return link_2[index];
    }

    public void addLink(int node1, int node2) {
        link_1[numLinks] = node1;
        link_2[numLinks] = node2;
        int dx = node_x[node1] - node_x[node2];
        int dy = node_y[node1] - node_y[node2];
        lengths[numLinks] = (int)Math.sqrt(dx * dx + dy * dy);
        numLinks++;
    }

    public void addLink(String name1, String name2) {
        int index1 = getNodeIndex(name1);
        int index2 = getNodeIndex(name2);
        if (index1 != -1 && index2 != -1)  addLink(index1, index2);
    }


    /** findPath - abstract method that is defined in subclasses */
    abstract public int [] findPath(int start_node, int goal_node); // return an array of node indices

    protected int getNodeIndex(String name) {
        for (int i=0; i<numNodes; i++) {
            if (name.equals(nodeNames[i])) return i;
        }
        return -1; // error condition
    }

    public static final int MAX = 50; // max number of nodes and max number of links

    protected int [] path = new int[AbstractGraphSearch.MAX];
    protected int num_path = 0;

    // for nodes:
    protected String [] nodeNames = new String[MAX];
    protected int [] node_x = new int[MAX];
    protected int [] node_y = new int[MAX];
    // for links between nodes:
    protected int [] link_1 = new int[MAX];
    protected int [] link_2 = new int[MAX];
    protected int [] lengths = new int[MAX];

    protected int numNodes = 0;
    protected int numLinks = 0;

    protected int goalNodeIndex = -1, startNodeIndex = -1;

}
