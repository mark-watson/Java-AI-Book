Search  {#search}
======

Unless you write the AI for game programs and entertainment systems (which I have done for Angel Studios, Nintendo, and Disney), the material in the chapter may not be relevant to your work. That said I recommend that you develop some knowledge of defining search spaces for problems and techniques to search these spaces. I hope that you have fun with the material in this chapter.

Early AI research emphasized the optimization of search algorithms. At this time in the 1950s and 1960s this approach made sense because many AI tasks can be solved effectively by defining state spaces and using search algorithms to define and explore search trees in this state space. This approach for AI research encountered some early success in game playing systems like checkers and chess which reinforced confidence in viewing many AI problems as search problems.

I now consider this form of classic search to be a well understood problem but that does not mean that we will not see exciting improvements in search algorithms in the future. This book does not cover Monte Carlo Search or game search using Reinforcement Learning with Monte Carlo Search that Alpha Go uses.

We will cover depth-first and breadth-first search. The basic implementation for depth-first and breadth-first search is the same with one key difference. When searching from any location in state space we start by calculating nearby locations that can be moved to in one search cycle. For depth-first search we store new locations to be searched in a stack data structure and for breadth-first search we store new locations to search in a queue data structure. As we will shortly see this simple change has a large impact on search quality (usually breadth-first search will produce better results) and computational resources (depth-first search requires less storage).

It is customary to cover search in AI books but to be honest I have only used search techniques in one interactive planning system in the 1980s and much later while doing the "game AI" in two Nintendo games, a PC hovercraft racing game and a VR system for Disney. Still, you should understand how to optimize search.

What are the limitations of search? Early on, success in applying search to problems like checkers and chess misled early researchers into underestimating the extreme difficulty of writing software that performs tasks in domains that require general world knowledge or deal with complex and changing environments. These types of problems usually require the understanding and the implementation of domain specific knowledge.

In this chapter, we will use three search problem domains for studying search algorithms: path finding in a maze, path finding in a graph, and alpha-beta search in the games tic-tac-toe and chess.

If you want to try the examples before we proceed to the implementation then you can do that right now using the **Makefile** in the **search** directory:

{lang="makefile",linenos=off}
~~~~~~~~
chess:
	mvn install
	mvn exec:java -Dexec.mainClass="search.game.Chess"

graph:
	mvn install
	mvn exec:java -Dexec.mainClass="search.graph.GraphDepthFirstSearch"

maze:
	mvn install
	mvn exec:java -Dexec.mainClass="search.maze.MazeBreadthFirstSearch"
~~~~~~~~

You can run the examples using:

{linenos=off}
~~~~~~~~
make maze
make graph
make chess
~~~~~~~~


## Representation of Search State Space and Search Operators

We will use a single search tree representation in graph search and maze search examples in this chapter. Search trees consist of nodes that define locations in state space and links to other nodes. For some small problems, the search tree can be pre-computed and cover all of the search space. For most problems however it is impossible to completely enumerate a search tree for a state space so we must define successor node search operators that for a given node produce all nodes that can be reached from the current node in one step. For example, in the game of chess we can not possibly enumerate the search tree for all possible games of chess, so we define a successor node search operator that given a board position (represented by a node in the search tree) calculates all possible moves for either the white or black pieces. The possible
Chess moves are calculated by a successor node search operator and are represented by newly calculated nodes that are linked to the previous node. Note that even when it is simple to fully enumerate a search tree, as in the small maze example, we still want to use the general implementation strategy of generating the search tree dynamically as we will do in this chapter.

For calculating a search tree we use a graph. We will represent graphs as nodes with links between some of the nodes. For solving puzzles and for game related search, we will represent positions in the search space with Java objects called nodes. Nodes contain arrays of references to child nodes and for some applications we also might store links back to parent nodes. A search space using this node representation can be viewed as a **directed graph** or a **tree**. The node that has no parent nodes is the root node and all nodes that have no child nodes a called leaf nodes.

Search operators are used to move from one point in the search space to another. We deal with quantized search spaces in this chapter, but search spaces can also be continuous in some applications (e.g., a robot's position while moving in the real world). In general search spaces are either very large or are infinite. We implicitly define a search space using some algorithm for extending the space from our reference position in the space. The figure [Search Space Representations](#searchspace)
shows representations of search space as both connected nodes in a graph
and as a two-dimensional grid with arrows indicating possible movement
from a reference point denoted by **R**.

{#searchspace}
![Search Space Representations](images/search_space.png)

When we specify a search space as a two-dimensional array, search
operators will move the point of reference in the search space from a
specific grid location to an adjoining grid location. For some
applications, search operators are limited to moving up/down/left/right
and in other applications operators can additionally move the reference
location diagonally.

When we specify a search space using node representation, search operators can move the reference point down to any child node or up to the parent node. For search spaces that are represented implicitly, search operators are also responsible for determining legal child nodes, if any, from the reference point.

Note that I created different libraries for the maze and graph search examples.

## Finding Paths in Mazes

The example program used in this section is **MazeSearch.java** in the
directory **search/src/main/java/search/maze** and I assume that you have cloned the [GitHub repository for this book](https://github.com/mark-watson/Java-AI-Book-Code). The figure [UML Diagram for Search Classes](#search-uml) shows an overview of
the maze search strategies: depth-first and breadth-first search. The abstract base class **AbstractSearchEngine**
contains common code and data that is required by both the classes **DepthFirstSearch** and **BreadthFirstSearch**. The class **Maze** is used to
record the data for a two-dimensional maze, including which grid locations contain walls or obstacles. The class **Maze** defines three
static short integer values used to indicate obstacles, the starting location, and the ending location.


{#search-uml}
![UML Diagram for Search Classes](images/search_uml.png)


The Java class **Maze** defines the search space. This class allocates a two-dimensional array of short integers to represent the state of any grid location in the maze. Whenever we need to store a pair of integers,
we will use an instance of the standard Java class **java.awt.Dimension**, which has two integer data components: width and height. Whenever we
need to store an x-y grid location, we create a new Dimension object (if required), and store the x coordinate in **Dimension.width** and the y coordinate in **Dimension.height**. As in the right-hand side of figure [Search Space](#searchspace), the operator for moving through the search space from given x-y coordinates allows a transition to any adjacent grid location that is empty. The Maze class also contains the x-y location for the starting location (**startLoc**) and goal location (**goalLoc**). Note that for these examples, the class Maze sets the starting location to grid coordinates 0-0 (upper left corner of the maze in the figures to follow) and the goal node in (width - 1) - (height - 1) (lower right corner in the following figures).

The abstract class **AbstractSearchEngine** is the base class for both the
depth-first (uses a stack to store moves) search class
**DepthFirstSearchEngine** and the breadth-first (uses a queue to store moves) search class **BreadthFirstSearchEngine**. We will start by looking
at the common data and behavior defined in **AbstractSearchEngine**. The
class constructor has two required arguments: the width and height of the maze, measured in grid cells. The constructor defines an instance of
the **Maze** class of the desired size and then calls the utility method initSearch to allocate an array **searchPath** of **Dimension** objects, which will be used to record the path traversed through the maze. The abstract base class also defines other utility methods:

-   **equals(Dimension d1, Dimension d2)** – checks to see if two
    arguments of type **Dimension** are the same.

-   **getPossibleMoves(Dimension location)** – returns an array of
    **Dimension** objects that can be moved to from the specified
    location. This implements the movement operator.

Now, we will look at the depth-first search procedure. The constructor
for the derived class **DepthFirstSearchEngine** calls the base class
constructor and then solves the search problem by calling the method
**iterateSearch**. We will look at this method in some detail. The
arguments to **iterateSearch** specify the current location and the
current search depth:

{lang="java",linenos=off}
~~~~~~~~
    private void iterateSearch(Dimension loc, int depth) {
~~~~~~~~

The class variable **isSearching** is used to halt search, avoiding more
solutions, once one path to the goal is found.

{lang="java",linenos=off}
~~~~~~~~
      if (isSearching == false) return;
~~~~~~~~

We set the maze value to the depth for display purposes only:

{lang="java",linenos=off}
~~~~~~~~
      maze.setValue(loc.width, loc.height, (short)depth);
~~~~~~~~

Here, we use the super class **getPossibleMoves** method to get an array
of possible neighboring squares that we could move to; we then loop over
the four possible moves (a null value in the array indicates an illegal
move):

{lang="java",linenos=off}
~~~~~~~~
      Dimension [] moves = getPossibleMoves(loc);
      for (int i=0; i<4; i++) {
        if (moves[i] == null) break; // out of possible moves
                                     // from this location
~~~~~~~~

Record the next move in the search path array and check to see if we are
done:

{lang="java",linenos=off}
~~~~~~~~
      searchPath[depth] = moves[i];
      if (equals(moves[i], goalLoc)) {
         System.out.println("Found the goal at " +
                            moves[i].width +
                            ``, " + moves[i].height);
         isSearching = false;
         maxDepth = depth;
         return;
      } else {
~~~~~~~~

If the next possible move is not the goal move, we recursively call the iterateSearch method again, but starting from this new location and increasing the depth counter by one:

{lang="java",linenos=off}
~~~~~~~~
        iterateSearch(moves[i], depth + 1);
        if (isSearching == false) return;
      }
~~~~~~~~

The [figure showing the depth-first search in a maze](#search-depth-maze) shows how poor a path a depth-first search can find between the start and goal locations in the maze. The maze is a 10-by-10 grid. The letter S marks the starting location in the upper left corner and the goal position is marked with a G in the lower right corner of the grid. Blocked grid cells are painted light gray. The basic problem with the depth-first search is that the search engine will often start searching in a bad direction, but still find a path eventually, even given a poor start. The advantage of a depth-first search over a
breadth-first search is that the depth-first search requires much less memory. We will see that possible moves for depth-first search are stored on a stack (last in, first out data structure) and possible moves for a breadth-first search are stored in a queue (first in, first out data structure).

{#search-depth-maze}
![Depth-first search of a maze](images/search_depth.png)


The derived class **BreadthFirstSearch** is similar to the
**DepthFirstSearch** procedure with one major difference: from a specified
search location we calculate all possible moves, and make one possible
trial move at a time. We use a queue data structure for storing possible
moves, placing possible moves on the back of the queue as they are
calculated, and pulling test moves from the front of the queue. The
effect of a breadth-first search is that it “fans out” uniformly from
the starting node until the goal node is found.

The class constructor for **BreadthFirstSearch** calls the super class
constructor to initialize the maze, and then uses the auxiliary method
**doSearchOn2Dgrid** for performing a breadth-first search for the goal.
We will look at the class **BreadthFirstSearch** in some detail. Breadth
first search uses a queue instead of a stack (depth-first search) to
store possible moves. The utility class **DimensionQueue** implements a
standard queue data structure that handles instances of the class
**Dimension**.

The method **doSearchOn2Dgrid** is not recursive, it uses a loop to add
new search positions to the end of an instance of class **DimensionQueue**
and to remove and test new locations from the front of the queue. The
two-dimensional array **allReadyVisited** keeps us from searching the same
location twice. To calculate the shortest path after the goal is found,
we use the predecessor array:

{lang="java",linenos=off}
~~~~~~~~
        private void doSearchOn2DGrid() {
          int width = maze.getWidth();
          int height = maze.getHeight();
          boolean alReadyVisitedFlag[][] =
                  new boolean[width][height];
          Dimension predecessor[][] =
                new Dimension[width][height];
          DimensionQueue queue =
                        new DimensionQueue();
          for (int i=0; i<width; i++) {
            for (int j=0; j<height; j++) {
              alReadyVisitedFlag[i][j] = false;
              predecessor[i][j] = null;
            }
          }
~~~~~~~~

We start the search by setting the already visited flag for the starting location to true value and adding the starting location to the back of the queue:

{lang="java",linenos=off}
~~~~~~~~
          alReadyVisitedFlag[startLoc.width][startLoc.height]
             = true;
          queue.addToBackOfQueue(startLoc);
          boolean success = false;
~~~~~~~~

This outer loop runs until either the queue is empty or the goal is
found:

{lang="java",linenos=off}
~~~~~~~~
        outer:      
          while (queue.isEmpty() == false) {
~~~~~~~~

We peek at the **Dimension** object at the front of the queue (but do not
remove it) and get the adjacent locations to the current position in the
maze:

{lang="java",linenos=off}
~~~~~~~~
            Dimension head = queue.peekAtFrontOfQueue();
            Dimension [] connected =
                             getPossibleMoves(head);
~~~~~~~~

We loop over each possible move; if the possible move is valid (i.e., not null) and if we have not already visited the possible move location, then we add the possible move to the back of the queue and set the predecessor array for the new location to the last square visited (head is the value from the front of the queue). If we find the goal, break out of the loop:

{lang="java",linenos=off}
~~~~~~~~
            for (int i=0; i<4; i++) {
              if (connected[i] == null) break;
              int w = connected[i].width;
              int h = connected[i].height;
              if (alReadyVisitedFlag[w][h] == false) {
                alReadyVisitedFlag[w][h] = true;
                predecessor[w][h] = head;
                queue.addToBackOfQueue(connected[i]);
                if (equals(connected[i], goalLoc)) {
                  success = true;
                  break outer; // we are done
                }
              }
            }
~~~~~~~~

We have processed the location at the front of the queue (in the
variable head), so remove it:

{lang="java",linenos=off}
~~~~~~~~
            queue.removeFromFrontOfQueue();
          }
~~~~~~~~

Now that we are out of the main loop, we need to use the predecessor
array to get the shortest path. Note that we fill in the **searchPath**
array in reverse order, starting with the goal location:

{lang="java",linenos=off}
~~~~~~~~
          maxDepth = 0;
          if (success) {
            searchPath[maxDepth++] = goalLoc;
            for (int i=0; i<100; i++) {
              searchPath[maxDepth] =
                 predecessor[searchPath[maxDepth - 1].
                   width][searchPath[maxDepth - 1].
                   height];
              maxDepth++;
              if (equals(searchPath[maxDepth - 1],
                         startLoc))
                break;  // back to starting node
            }
          }
        }
~~~~~~~~

The [figure of breadth search of a maze](#search-breadth-maze) shows a good path solution between starting and goal nodes. Starting from the initial position, the breadth-first search engine adds all possible moves to the back of a queue data structure. For each possible move added to this queue in one search cycle, all possible moves are added to the queue for each new move recorded. Visually, think of possible moves added to the queue as “fanning out” like a wave from the starting location. The breadth-first search engine stops when this “wave” reaches the goal location. In general, I prefer breadth-first search techniques to depth-first search techniques when memory storage for the queue used in the search process is not an issue. In general, the memory requirements for performing depth-first search is much less than breadth-first search.

{#search-breadth-maze}
![Beadth-first Search of a Maze](images/search_breadth.png)

Note that the classes **MazeDepthFirstSearch** and
**MazeBreadthFirstSearch** are simple Java JFC applications that produced the [figure showing the depth-first search in a maze](#search-depth-maze) and  the [figure of breadth search of a maze](#search-breadth-maze). The interested reader can read through the source code for the GUI test programs, but we will only cover the core AI code in this book. If you are interested in the GUI test programs and you are not familiar with the Java JFC (or Swing) classes, there are several good tutorials on JFC programming on the web.

## Finding Paths in Graphs

In the last section, we used both depth-first and breadth-first search techniques to find a path between a starting location and a goal location in a maze. Another common type of search space is represented by a graph. A graph is a set of nodes and links. We characterize nodes as containing the following data:

-   A name and/or other data
-   Zero or more links to other nodes
-   A position in space (this is optional, usually for display or visualization purposes)

Links between nodes are often called edges. The algorithms used for finding paths in graphs are very similar to finding paths in a two-dimensional maze. The primary difference is the operators that allow us to move from one node to another. In the last section we saw that in a maze, an agent can move from one grid space to another if the target space is empty. For graph search, a movement operator allows movement to another node if there is a link to the target node.

The [figure showing UML Diagram for Search Classes](#search-uml) shows the UML class diagram for the graph search Java classes that we will use in this section. The abstract class **AbstractGraphSearch** class is the base class for both **DepthFirstSearch** and **BreadthFirstSearch**. The classes **GraphDepthFirstSearch** and **GraphBreadthFirstSearch** and test programs also provide a Java Foundation Class (JFC) or Swing based user interface. These two test programs produced figures [Search Depth-First](#gsearch-depth-maze) and [Search Breadth-First](#gsearch-breadth-maze).

{#gsearch-uml}
![UML Diagram for Graphics Search Demo](images/gsearch_uml.png)

As seen in the previous figure, most of the data for the search
operations (i.e., nodes, links, etc.) is defined in the abstract class
**AbstractGraphSearch**. This abstract class is customized through
inheritance to use a stack for storing possible moves (i.e., the array
path) for depth-first search and a queue for breadth-first search.

The abstract class **AbstractGraphSearch** allocates data required by both
derived classes:

{lang="java",linenos=off}
~~~~~~~~
        final public static int MAX = 50;
        protected int [] path = 
              new int[AbstractGraphSearch.MAX];
        protected int num_path = 0;
        // for nodes:
        protected String [] nodeNames = 
                               new String[MAX];
        protected int [] node_x = new int[MAX];
        protected int [] node_y = new int[MAX];
        // for links between nodes:
        protected int [] link_1 = new int[MAX];
        protected int [] link_2 = new int[MAX];
        protected int [] lengths = new int[MAX];
        protected int numNodes = 0;
        protected int numLinks = 0;
        protected int goalNodeIndex = -1,
                      startNodeIndex = -1;
~~~~~~~~

The abstract base class also provides several common utility methods:

-   addNode(String name, int x, int y) – adds a new node
-   addLink(int n1, int n2) – adds a bidirectional link between nodes
    indexed by n1 and n2. Node indexes start at zero and are in the
    order of calling addNode.
-   addLink(String n1, String n2) – adds a bidirectional link between
    nodes specified by their names
-   getNumNodes() – returns the number of nodes
-   getNumLinks() – returns the number of links
-   getNodeName(int index) – returns a node’s name
-   getNodeX(), getNodeY() – return the coordinates of a node
-   getNodeIndex(String name) – gets the index of a node, given its name

The abstract base class defines an abstract method **findPath** that must be overridden. We will start with the derived class **DepthFirstSearch**, looking at its implementation of findPath. The **findPath** method returns an array of node indices indicating the calculated path:

{lang="java",linenos=off}
~~~~~~~~
      public int [] findPath(int start_node,
                             int goal_node) {
~~~~~~~~

The class variable path is an array that is used for temporary storage; we set the first element to the starting node index, and call the utility method **findPathHelper**:

{lang="java",linenos=off}
~~~~~~~~
        path[0] = start_node; // the starting node
        return findPathHelper(path, 1, goal_node);
      }
~~~~~~~~

The method **findPathHelper** is the interesting method in this class that actually performs the depth-first search; we will look at it in some detail:

The path array is used as a stack to keep track of which nodes are being visited during the search. The argument **num\_path** is the number of locations in the path, which is also the search depth:

{lang="java",linenos=off}
~~~~~~~~
      public int [] findPathHelper(int [] path,
                                   int num_path,
                                   int goal_node) {
~~~~~~~~

First, re-check to see if we have reached the goal node; if we have, make a new array of the current size and copy the path into it. This new array is returned as the value of the method:

{lang="java",linenos=off}
~~~~~~~~
        if (goal_node == path[num_path - 1]) {
          int [] ret = new int[num_path];
          for (int i=0; i<num_path; i++) {
             ret[i] = path[i];
          }
          return ret;  // we are done!
        }
~~~~~~~~

We have not found the goal node, so call the method **connected\_nodes** to find all nodes connected to the current node that are not already on the search path (see the source code for the implementation of **connected\_nodes**):

{lang="java",linenos=off}
~~~~~~~~
        int [] new_nodes = connected_nodes(path,
                                           num_path);
~~~~~~~~

If there are still connected nodes to search, add the next possible “node to visit” to the top of the stack (variable **path** in the program) and recursively call the method **findPathHelper** again:

{lang="java",linenos=off}
~~~~~~~~
        if (new_nodes != null) {
          for (int j=0; j<new_nodes.length; j++) {
            path[num_path] = new_nodes[j];
            int [] test = findPathHelper(new_path,
                                         num_path + 1,
                                         goal_node);
            if (test != null) {
              if (test[test.length-1] == goal_node) {
                  return test;
              }
            }
          }
        }
~~~~~~~~

If we have not found the goal node, return null, instead of an array of node indices:

{lang="java",linenos=off}
~~~~~~~~
        return null;
      }
~~~~~~~~

Derived class **BreadthFirstSearch** also must define abstract method **findPath**. This method is very similar to the breadth-first search method used for finding a path in a maze: a queue is used to store possible moves. For a maze, we used a queue class that stored instances of the class Dimension, so for this problem, the queue only needs to store integer node indices. The return value of **findPath** is an array of node indices that make up the path from the starting node to the goal.

{lang="java",linenos=off}
~~~~~~~~
      public int [] findPath(int start_node,
                             int goal_node) {
~~~~~~~~

We start by setting up a flag array **alreadyVisited** to prevent visiting the same node twice, and allocating a predecessors array that we will use to find the shortest path once the goal is reached:

{lang="java",linenos=off}
~~~~~~~~
        // data structures for depth-first search:
        boolean [] alreadyVisitedFlag =
                               new boolean[numNodes];
        int [] predecessor = new int[numNodes];
~~~~~~~~

The class **IntQueue** is a private class defined in the file BreadthFirstSearch.java; it implements a standard queue:

{lang="java",linenos=off}
~~~~~~~~
        IntQueue queue = new IntQueue(numNodes + 2);
~~~~~~~~

Before the main loop, we need to initialize the already visited predecessor arrays, set the visited flag for the starting node to true, and add the starting node index to the back of the queue:

{lang="java",linenos=off}
~~~~~~~~
        for (int i=0; i<numNodes; i++) {
          alreadyVisitedFlag[i] = false;
          predecessor[i] = -1;
        }
        alreadyVisitedFlag[start_node] = true;
        queue.addToBackOfQueue(start_node);
~~~~~~~~

The main loop runs until we find the goal node or the search queue is empty:

{lang="java",linenos=off}
~~~~~~~~
        outer:  while (queue.isEmpty() == false) {
~~~~~~~~

We will read (without removing) the node index at the front of the queue and calculate the nodes that are connected to the current node (but not already on the visited list) using the **connected\_nodes** method (the
interested reader can see the implementation in the source code for this class):

{lang="java",linenos=off}
~~~~~~~~
          int head = queue.peekAtFrontOfQueue();
          int [] connected = connected_nodes(head);
          if (connected != null) {
~~~~~~~~

If each node connected by a link to the current node has not already been visited, set the predecessor array and add the new node index to the back of the search queue; we stop if the goal is found:

{lang="java",linenos=off}
~~~~~~~~
          for (int i=0; i<connected.length; i++) {
             if (alreadyVisitedFlag[connected[i]] == false) {
               predecessor[connected[i]] = head;
               queue.addToBackOfQueue(connected[i]);
               if (connected[i] == goal_node) break outer;
              }
            }
            alreadyVisitedFlag[head] = true;
            queue.removeFromQueue(); // ignore return value
          }
        }
~~~~~~~~

Now that the goal node has been found, we can build a new array of returned node indices for the calculated path using the predecessor array:

{lang="java",linenos=off}
~~~~~~~~
        int [] ret = new int[numNodes + 1];
        int count = 0;
        ret[count++] = goal_node;
        for (int i=0; i<numNodes; i++) {
          ret[count] = predecessor[ret[count - 1]];
          count++;
          if (ret[count - 1] == start_node)  break;
        }
        int [] ret2 = new int[count];
        for (int i=0; i<count; i++) {
          ret2[i] = ret[count - 1 - i];
        }
        return ret2;
       }
~~~~~~~~

In order to run both the depth-first and breadth-first graph search examples, change directory to src-search-maze and type the following commands:

{lang="java",linenos=off}
~~~~~~~~
    javac *.java
    java GraphDepthFirstSearch
    java GraphBeadthFirstSearch
~~~~~~~~

The following figure shows the results of finding a route from node 1 to node 9 in the small test graph. Like the depth-first results seen in the maze search, this path is not optimal.

{#gsearch-depth-graph}
![Depth-first Search in a Graph](images/gsearch_depth.png)

The next figure shows an optimal path found using a breadth-first search. As we saw in the maze search example, we find optimal solutions using breadth-first search at the cost of extra memory required for the breadth-first search.

{#gsearch-breadth-graph}
![Breadth-first Search in a Graph](images/gsearch_breadth.png)


## Adding Heuristics to Breadth-first Search

We can usually make breadth-first search more efficient by ordering the search order for all branches from a given position in the search space. For example, when adding new nodes from a specified reference point in the search space, we might want to add nodes to the search queue first that are “in the direction” of the goal location: in a two-dimensional search like our maze search, we might want to search connected grid cells first that were closest to the goal grid space. In this case, pre-sorting nodes (in order of closest distance to the goal) added to the breadth-first search queue could have a dramatic effect on search efficiency. The alpha-beta additions to breadth-first search are seen in in the next section.

## Heuristic Search and Game Playing: Tic-Tac-Toe and Chess

Now that a computer program has won a match against the human world champion, perhaps people’s expectations of AI systems will be prematurely optimistic. Game search techniques are not real AI, but rather, standard programming techniques. A better platform for doing AI research is the game of Go. There are so many possible moves in the game of Go that brute force look ahead (as is used in Chess playing programs) simply does not work. In 2016 the Alpha Go program became stronger than human players by using Reinforcement Learning and Monte Carlo search.

Min-max type search algorithms with alpha-beta cutoff
optimizations are an important programming technique and will be covered in some detail in the remainder of this chapter. We will design an abstract Java class library for implementing alpha-beta enhanced min-max search, and then use this framework to write programs to play
tic-tac-toe and chess.

### Alpha-Beta Search

The first game that we will implement will be tic-tac-toe, so we will
use this simple game to explain how the min-max search (with alpha-beta
cutoffs) works.

The [figure showing possible moves for tic-tac-toe](#alphabeta-tictactoe) shows some of the possible moves generated
from a tic-tac-toe position where X has made three moves and O has made
two moves; it is O’s turn to move. This is “level 0” in this figure. At level 0, O has four possible moves. How
do we assign a fitness value to each of O’s possible moves at level 0?
The basic min-max search algorithm provides a simple solution to this
problem: for each possible move by O in level 1, make the move and store
the resulting 4 board positions. Now, at level 1, it is X’s turn to
move. How do we assign values to each of X’s possible three moves in
the [figure showing possible moves for tic-tac-toe](#alphabeta-tictactoe)? Simple, we continue to search by
making each of X’s possible moves and storing each possible board
position for level 2. We keep recursively applying this algorithm until
we either reach a maximum search depth, or there is a win, loss, or draw
detected in a generated move. We assume that there is a fitness function
available that rates a given board position relative to either side.
Note that the value of any board position for X is the negative of the
value for O.

{#alphabeta-tictactoe}
![Alpha Beta Search for Tic-Tac-Toe](images/alphabeta_tictactoe.png)


To make the search more efficient, we maintain values for alpha and beta
for each search level. Alpha and beta determine the best possible/worst
possible move available at a given level. If we reach a situation like
the second position in level 2 where X has won, then we can immediately
determine that O’s last move in level 1 that produced this position (of
allowing X an instant win) is a low valued move for O (but a high valued
move for X). This allows us to immediately “prune” the search tree by
ignoring all other possible positions arising from the first O move in
level 1. This alpha-beta cutoff (or tree pruning) procedure can save a
large percentage of search time, especially if we can set the search
order at each level with “probably best” moves considered first.

While tree diagrams as seen in the [figure showing possible moves for tic-tac-toe](#alphabeta-tictactoe) quickly
get complicated, it is easy for a computer program to generate possible
moves, calculate new possible board positions and temporarily store
them, and recursively apply the same procedure to the next search level
(but switching min-max “sides” in the board evaluation). We will see in
the next section that it only requires about 100 lines of Java code to
implement an abstract class framework for handling the details of
performing an alpha-beta enhanced search. The additional game specific
classes for tic-tac-toe require about an additional 150 lines of code to
implement; chess requires an additional 450 lines of code.

### A Java Framework for Search and Game Playing

The general interface for the Java classes that we will develop in this
section was inspired by the Common LISP game-playing framework written
by Kevin Knight and described in (Rich, Knight 1991). The abstract class
GameSearch contains the code for running a two-player game and
performing an alpha-beta search. This class needs to be sub-classed to
provide the eight methods:

{lang="java",linenos=off}
~~~~~~~~
      public abstract boolean drawnPosition(Position p)
      public abstract boolean wonPosition(Position p,
                                          boolean player)
                      positionEvaluation(Position p,
                                         boolean player)
      public abstract void printPosition(Position p)
      public abstract Position []
                      possibleMoves(Position p,
                                    boolean player)
      public abstract Position makeMove(Position p,
                                        boolean player,
                                        Move move)
      public abstract boolean reachedMaxDepth(Position p,
                                              int depth)
      public abstract Move getMove()
~~~~~~~~

The method **drawnPosition** should return a Boolean true value if the
given position evaluates to a draw situation. The method **wonPosition**
should return a true value if the input position is won for the
indicated player. By convention, I use a Boolean true value to represent
the computer and a Boolean false value to represent the human opponent.
The method **positionEvaluation** returns a position evaluation for a
specified board position and player. Note that if we call
positionEvaluation switching the player for the same board position,
then the value returned is the negative of the value calculated for the
opposing player. The method **possibleMoves** returns an array of objects
belonging to the class Position. In an actual game like chess, the
position objects will actually belong to a chess-specific refinement of
the Position class (e.g., for the chess program developed later in this
chapter, the method **possibleMoves** will return an array of
**ChessPosition** objects). The method **makeMove** will return a new
position object for a specified board position, side to move, and move.
The method **reachedMaxDepth** returns a Boolean true value if the search
process has reached a satisfactory depth. For the tic-tac-toe program,
the method **reachedMaxDepth** does not return true unless either side has
won the game or the board is full; for the chess program, the method
**reachedMaxDepth** returns true if the search has reached a depth of 4
half moves deep (this is not the best strategy, but it has the advantage
of making the example program short and easy to understand). The method
**getMove** returns an object of a class derived from the class **Move**
(e.g., **TicTacToeMove** or **ChessMove**).

The **GameSearch** class implements the following methods to perform game
search:

{lang="java",linenos=off}
~~~~~~~~
      protected Vector alphaBeta(int depth, Position p,
                                 boolean player)
      protected Vector alphaBetaHelper(int depth,
                                       Position p,
                                       boolean player,
                                       float alpha,
                                       float beta)
      public void playGame(Position startingPosition,
                           boolean humanPlayFirst)
~~~~~~~~

The method **alphaBeta** is simple; it calls the helper method
**alphaBetaHelper** with initial search conditions; the method
**alphaBetaHelper** then calls itself recursively. The code for
**alphaBeta** is:

{lang="java",linenos=off}
~~~~~~~~
      protected Vector alphaBeta(int depth,
                                 Position p,
                                 boolean player)  {
        Vector v = alphaBetaHelper(depth, p, player,
                                   1000000.0f,
                                  -1000000.0f);
        return v;
      }
~~~~~~~~

It is important to understand what is in the vector returned by the methods **alphaBeta** and **alphaBetaHelper**. The first element is a floating point position evaluation for the point of view of the player whose turn it is to move; the remaining values are the “best move” for each side to the last search depth. As an example, if I let the tic-tac-toe program play first, it places a marker at square index 0, then I place my marker in the center of the board an index 4. At this point, to calculate the next computer move, **alphaBeta** is called and returns the following elements in a vector:

{lang="java",linenos=off}
~~~~~~~~
     next element: 0.0
     next element: [-1,0,0,0,1,0,0,0,0,]
     next element: [-1,1,0,0,1,0,0,0,0,]
     next element: [-1,1,0,0,1,0,0,-1,0,]
     next element: [-1,1,0,1,1,0,0,-1,0,]
     next element: [-1,1,0,1,1,-1,0,-1,0,]
     next element: [-1,1,1,1,1,-1,0,-1,0,]
     next element: [-1,1,1,1,1,-1,-1,-1,0,]
     next element: [-1,1,1,1,1,-1,-1,-1,1,]
~~~~~~~~

Here, the alpha-beta enhanced min-max search looked all the way to the end of the game and these board positions represent what the search procedure calculated as the best moves for each side. Note that the class **TicTacToePosition** (derived from the abstract class **Position**) has a toString method to print the board values to a string.

The same printout of the returned vector from **alphaBeta** for the chess program is:

{lang="java",linenos=off}
~~~~~~~~
    next element: 5.4
     next element:
         [4,2,3,5,9,3,2,4,7,7,1,1,1,0,1,1,1,1,7,7,
          0,0,0,0,0,0,0,0,7,7,0,0,0,1,0,0,0,0,7,7,
          0,0,0,0,0,0,0,0,7,7,0,0,0,0,-1,0,0,0,7,7,
          -1,-1,-1,-1,0,-1,-1,-1,7,7,-4,-2,-3,-5,-9,
          -3,-2,-4,]
     next element:
         [4,2,3,0,9,3,2,4,7,7,1,1,1,5,1,1,1,1,7,7,
          0,0,0,0,0,0,0,0,7,7,0,0,0,1,0,0,0,0,7,7,
          0,0,0,0,0,0,0,0,7,7,0,0,0,0,-1,0,0,0,7,7,
          -1,-1,-1,-1,0,-1,-1,-1,7,7,-4,-2,-3,-5,-9,
          -3,-2,-4,]
     next element:
         [4,2,3,0,9,3,2,4,7,7,1,1,1,5,1,1,1,1,7,7,
          0,0,0,0,0,0,0,0,7,7,0,0,0,1,0,0,0,0,7,7,
          0,0,0,0,0,0,0,0,7,7,0,0,0,0,-1,-5,0,0,7,7,
          -1,-1,-1,-1,0,-1,-1,-1,7,7,-4,-2,-3,0,-9,
          -3,-2,-4,]
     next element:
         [4,2,3,0,9,3,0,4,7,7,1,1,1,5,1,1,1,1,7,7,
          0,0,0,0,0,2,0,0,7,7,0,0,0,1,0,0,0,0,7,7,
          0,0,0,,0,0,0,0,0,7,7,0,0,0,0,-1,-5,0,0,7,7,
          -1,-1,-1,-1,0,-1,-1,-1,7,7,-4,-2,-3,0,-9,
          -3,-2,-4,]
     next element:
         [4,2,3,0,9,3,0,4,7,7,1,1,1,5,1,1,1,1,7,7,
          0,0,0,0,0,2,0,0,7,7,0,0,0,1,0,0,0,0,7,7,
          -1,0,0,0,0,0,0,0,7,7,0,0,0,0,-1,-5,0,0,7,7,
          0,-1,-1,-1,0,-1,-1,-1,7,7,-4,-2,-3,0,-9,
          -3,-2,-4,]
~~~~~~~~

Here, the search procedure assigned the side to move (the computer) a position evaluation score of 5.4; this is an artifact of searching to a fixed depth. Notice that the board representation is different for chess, but because the **GameSearch** class manipulates objects derived from the classes **Position** and **Move**, the GameSearch class does not need to have any knowledge of the rules for a specific game. We will discuss the format of the chess position class **ChessPosition** in more detail when we develop the chess program.

The classes Move and Position contain no data and methods at all. The classes Move and Position are used as placeholders for derived classes for specific games. The search methods in the abstract GameSearch class manipulate objects derived from the classes Move and Position.

Now that we have seen the debug printout of the contents of the vector returned from the methods **alphaBeta** and **alphaBetaHelper**, it will be easier to understand how the method **alphaBetaHelper** works. The following text shows code fragments from the **alphaBetaHelper** method interspersed with book text:

{lang="java",linenos=off}
~~~~~~~~
      protected Vector alphaBetaHelper(int depth,
                                       Position p,                
                                       boolean player,
                                       float alpha,
                                       float beta) {
~~~~~~~~

Here, we notice that the method signature is the same as for **alphaBeta**, except that we pass floating point alpha and beta values. The important point in understanding min-max search is that most of the
evaluation work is done while “backing up” the search tree; that is, the search proceeds to a leaf node (a node is a leaf if the method **reachedMaxDepth** return a Boolean true value), and then a return vector for the leaf node is created by making a new vector and setting its
first element to the position evaluation of the position at the leaf node and setting the second element of the return vector to the board position at the leaf node:

{lang="java",linenos=off}
~~~~~~~~
        if (reachedMaxDepth(p, depth)) {
          Vector v = new Vector(2);
          float value = positionEvaluation(p, player);
          v.addElement(new Float(value));
          v.addElement(p);
          return v;
        }
~~~~~~~~

If we have not reached the maximum search depth (i.e., we are not yet at a leaf node in the search tree), then we enumerate all possible moves from the current position using the method **possibleMoves** and recursively call **alphaBetaHelper** for each new generated board position. In terms of the [figure showing possible moves for tic-tac-toe](#alphabeta-tictactoe), at this point we are moving down to another search level (e.g., from level 1 to level 2; the level in the [figure showing possible moves for tic-tac-toe](#alphabeta-tictactoe) corresponds to depth argument in **alphaBetaHelper**):

{lang="java",linenos=off}
~~~~~~~~
      Vector best = new Vector();
      Position [] moves = possibleMoves(p, player);
      for (int i=0; i<moves.length; i++) {
        Vector v2 = alphaBetaHelper(depth + 1, moves[i],
                                    !player,
                                    -beta, -alpha);
        float value = -((Float)v2.elementAt(0)).floatValue();
        if (value > beta) {
          if(GameSearch.DEBUG)
            System.out.println(" ! ! ! value="+
                               value+ 
                               ",beta="+beta);
          beta = value;
          best = new Vector();
          best.addElement(moves[i]);
          Enumeration enum = v2.elements();
          enum.nextElement(); // skip previous value
          while (enum.hasMoreElements()) {
            Object o = enum.nextElement();
            if (o != null) best.addElement(o);
          }
        }
        /**
         * Use the alpha-beta cutoff test to abort
         * search if we found a move that proves that
         * the previous move in the move chain was dubious
         */
        if (beta >= alpha) {
          break;
        }
      }
~~~~~~~~

Notice that when we recursively call **alphaBetaHelper**, we are “flipping” the player argument to the opposite Boolean value. After calculating the best move at this depth (or level), we add it to the end of the return vector:

{lang="java",linenos=off}
~~~~~~~~
        Vector v3 = new Vector();
        v3.addElement(new Float(beta));
        Enumeration enum = best.elements();
        while (enum.hasMoreElements()) {
          v3.addElement(enum.nextElement());
        }
        return v3;
~~~~~~~~

When the recursive calls back up and the first call to **alphaBetaHelper** returns a vector to the method **alphaBeta**, all of the “best” moves for each side are stored in the return vector, along with the evaluation of the board position for the side to move.

The class **GameSearch** method **playGame** is fairly simple; the following code fragment is a partial listing of playGame showing how to call **alphaBeta**, **getMove**, and **makeMove**:

{lang="java",linenos=off}
~~~~~~~~
      public void playGame(Position startingPosition,
                           boolean humanPlayFirst) {
        System.out.println("Your move:");
        Move move = getMove();
        startingPosition = makeMove(startingPosition,
                                    HUMAN, move);
        printPosition(startingPosition);
        Vector v = alphaBeta(0, startingPosition, PROGRAM);
        startingPosition = (Position)v.elementAt(1);        
        }
      }
~~~~~~~~

The debug printout of the vector returned from the method **alphaBeta** seen earlier in this section was printed using the following code immediately after the call to the method **alphaBeta**:

{lang="java",linenos=off}
~~~~~~~~
          Enumeration enum = v.elements();
          while (enum.hasMoreElements()) {
            System.out.println(" next element: " + 
                               enum.nextElement());
          }
~~~~~~~~

In the next few sections, we will implement a tic-tac-toe program and a chess-playing program using this Java class framework.

### Tic-Tac-Toe Using the Alpha-Beta Search Algorithm

Using the Java class framework of **GameSearch**, **Position**, and **Move**, it is simple to write a basic tic-tac-toe program by writing three new derived classes (as seen in the next figure showing a UML Class Diagram)
**TicTacToe** (derived from **GameSearch**), **TicTacToeMove** (derived from **Move**), and **TicTacToePosition** (derived from **Position**).

{#alphabaeta-tictactoe-classes}
![UML Diagram for Tic-Tac-Toe Classes](images/alphabaeta_tictactoe_classes.png)

I assume that the reader has the book example code installed and available for viewing. In this section, I will only discuss the most interesting details of the tic-tac-toe class refinements; I assume that the reader can look at the source code. We will start by looking at the
refinements for the position and move classes. The **TicTacToeMove** class
is trivial, adding a single integer value to record the square index for the new move:

{lang="java",linenos=off}
~~~~~~~~
    public class TicTacToeMove extends Move {
      public int moveIndex;
    }
~~~~~~~~

The board position indices are in the range of [0..8] and can be considered to be in the following order:

{lang="java",linenos=off}
~~~~~~~~
       0 1 2
       3 4 5
       6 7 8
~~~~~~~~

The class **TicTacToePosition** is also simple:

{lang="java",linenos=off}
~~~~~~~~
    public class TicTacToePosition extends Position {
        final static public int BLANK = 0;
        final static public int HUMAN = 1;
        final static public int PROGRAM = -1;
        int [] board = new int[9];
        public String toString() {
            StringBuffer sb = new StringBuffer("[");
            for (int i=0; i<9; i++)
              sb.append(""+board[i]+",");
            sb.append("]");
            return sb.toString();
        }
    }
~~~~~~~~

This class allocates an array of nine integers to represent the board, defines constant values for blank, human, and computer squares, and defines a toString method to print out the board representation to a string.

The **TicTacToe** class must define the following abstract methods from the base class **GameSearch**:

{lang="java",linenos=off}
~~~~~~~~
     public abstract boolean drawnPosition(Position p)
     public abstract boolean wonPosition(Position p,
                                         boolean player)
     public abstract float positionEvaluation(Position p,
                                              boolean player)
     public abstract void printPosition(Position p)
     public abstract Position [] possibleMoves(Position p,
                                               boolean player)
     public abstract Position makeMove(Position p,
                                       boolean player,
                                       Move move)
     public abstract boolean reachedMaxDepth(Position p,
                                            int depth)
     public abstract Move getMove()
~~~~~~~~

The implementation of these methods uses the refined classes **TicTacToeMove** and **TicTacToePosition**. For example, consider the method **drawnPosition** that is responsible for selecting a drawn (or tied) position:

{lang="java",linenos=off}
~~~~~~~~
      public boolean drawnPosition(Position p) {
        boolean ret = true;
        TicTacToePosition pos = (TicTacToePosition)p;
        for (int i=0; i<9; i++) {
          if (pos.board[i] == TicTacToePosition.BLANK){
            ret = false;
            break;
          }
        }
        return ret;
      }
~~~~~~~~

The overridden methods from the **GameSearch** base class must always cast arguments of type **Position** and **Move** to **TicTacToePosition** and **TicTacToeMove**. Note that in the method **drawnPosition**, the argument
of class **Position** is cast to the class **TicTacToePosition**. A position is considered to be a draw if all of the squares are full. We will see that checks for a won position are always made before checks for a drawn position, so that the method **drawnPosition** does not need to make a redundant check for a won position. The method **wonPosition** is also simple; it uses a private helper method **winCheck** to test for all
possible winning patterns in tic-tac-toe. The method
**positionEvaluation** uses the following board features to assign a fitness value from the point of view of either player:

-  The number of blank squares on the board
-  If the position is won by either side
-  If the center square is taken

The method **positionEvaluation** is simple, and is a good place for the interested reader to start modifying both the tic-tac-toe and chess programs:

{lang="java",linenos=off}
~~~~~~~~
        public float positionEvaluation(Position p,
                                        boolean player) {
            int count = 0;
            TicTacToePosition pos = (TicTacToePosition)p;
            for (int i=0; i<9; i++) {
                if (pos.board[i] == 0) count++;
            }
            count = 10 - count;
            // prefer the center square:
            float base = 1.0f;
            if (pos.board[4] == TicTacToePosition.HUMAN &&
                player) {
                base += 0.4f;
            }
            if (pos.board[4] == TicTacToePosition.PROGRAM &&
                !player) {
                base -= 0.4f;
            }
            float ret = (base - 1.0f);
            if (wonPosition(p, player))  {
                return base + (1.0f / count);
            }
            if (wonPosition(p, !player))  {
                return -(base + (1.0f / count));
            }
            return ret;
        }
~~~~~~~~

The only other method that we will look at here is **possibleMoves**; the interested reader can look at the implementation of the other (very simple) methods in the source code. The method **possibleMoves** is called with a current position, and the side to move (i.e., program or human):

{lang="java",linenos=off}
~~~~~~~~
        public Position [] possibleMoves(Position p,
                                         boolean player) {
            TicTacToePosition pos = (TicTacToePosition)p;
            int count = 0;
            for (int i=0; i<9; i++) {
                if (pos.board[i] == 0) count++;
            }
            if (count == 0) return null;
            Position [] ret = new Position[count];
            count = 0;
            for (int i=0; i<9; i++) {
                if (pos.board[i] == 0) {
                    TicTacToePosition pos2 =
                               new  TicTacToePosition();
                    for (int j=0; j<9; j++)
                         pos2.board[j] = pos.board[j];
                    if (player) pos2.board[i] = 1;
                    else pos2.board[i] = -1;
                    ret[count++] = pos2;
                }
            }
            return ret;
        }
~~~~~~~~

It is very simple to generate possible moves: every blank square is a legal move. (This method will not be as straightforward in the example chess program!)

It is simple to compile and run the example tic-tac-toe program: change directory to src-search-game and type:

{lang="java",linenos=off}
~~~~~~~~
    mvn install
    mvn exec:java -Dexec.mainClass="search.game.TicTacToe"
~~~~~~~~

When asked to enter moves, enter an integer between 0 and 8 for a square that is currently blank (i.e., has a zero value). The following shows this labeling of squares on the tic-tac-toe board:

{linenos=off}
~~~~~~~~
    0 1 2
    3 4 5
    6 7 8
~~~~~~~~

You might need to enter two return (enter) keys after entering your move square when using a macOS terminal.

### Chess Using the Alpha-Beta Search Algorithm

Using the Java class framework of **GameSearch**, **Position**, and **Move**,
it is reasonably easy to write a simple chess program by writing three
new derived classes (see the [figure showing a UML diagram for the chess game casses](#alphabeta-chess-classes)) **Chess**
(derived from **GameSearch**), **ChessMove** (derived from **Move**), and
**ChessPosition** (derived from **Position**). The chess program developed
in this section is intended to be an easy to understand example of using
alpha-beta min-max search; as such, it ignores several details that a
fully implemented chess program would implement:

-   Allow the computer to play either side (computer always plays black
    in this example).
-   Allow en-passant pawn captures.
-   Allow the player to take back a move after making a mistake.

The reader is assumed to have read the last section on implementing the
tic-tac-toe game; details of refining the GameSearch, Move, and Position
classes are not repeated in this section.

The following [figure showing a UML diagram for the chess game casses](#alphabeta-chess-classes) shows the UML class diagram for
both the general purpose GameSearch framework and the classes derived to
implement chess specific data and behavior.

{#alphabeta-chess-classes}
![UML Diagram for Chess Game Classes](images/alphabeta_chess_classes.png)


The class ChessMove contains data for recording from and to square
indices:

{lang="java",linenos=off}
~~~~~~~~
    public class ChessMove extends Move {
        public int from;
        public int to;
    }
~~~~~~~~

The board is represented as an integer array with 120 elements. A
chessboard only has 64 squares; the remaining board values are set to a
special value of 7, which indicates an “off board” square. The initial
board setup is defined statically in the Chess class and the off-board
squares have a value of “7”:

{lang="java",linenos=off}
~~~~~~~~
    private static int [] initialBoard = {
     7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
     7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
     4, 2, 3, 5, 9, 3, 2, 4, 7, 7,   // white pieces
     1, 1, 1, 1, 1, 1, 1, 1, 7, 7,   // white pawns
     0, 0, 0, 0, 0, 0, 0, 0, 7, 7,   // 8 blank squares
     0, 0, 0, 0, 0, 0, 0, 0, 7, 7,   // 8 blank squares
     0, 0, 0, 0, 0, 0, 0, 0, 7, 7,   // 8 blank squares
     0, 0, 0, 0, 0, 0, 0, 0, 7, 7,   // 8 blank squares
     -1,-1,-1,-1,-1,-1,-1,-1, 7, 7,  // black pawns
     -4,-2,-3,-5,-9,-3,-2,-4, 7, 7,  // black pieces
     7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
     7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7
    };
~~~~~~~~

{#chessgame1a}
![First example chess board](images/chess_game_1a.png)

It is difficult to see from this listing of the board square values but
in effect a regular chess board if padded on all sides with two rows and
columns of \`\`7" values.

We see the start of a sample chess game in the previous figure and
the continuation of this same game in the next figure.The
lookahead is limited to 2 moves (4 ply).

{#chessgame1b}
![Second example chess board](images/chess_game_1b.png)

The class **ChessPosition** contains data for this representation and
defines constant values for playing sides and piece types:

{lang="java",linenos=off}
~~~~~~~~
      public class ChessPosition extends Position {
        final static public int BLANK = 0;
        final static public int HUMAN = 1;
        final static public int PROGRAM = -1;
        final static public int PAWN = 1;
        final static public int KNIGHT = 2;
        final static public int BISHOP = 3;
        final static public int ROOK = 4;
        final static public int QUEEN = 5;
        final static public int KING = 6;
        int [] board = new int[120];
        public String toString() {
            StringBuffer sb = new StringBuffer("[");
            for (int i=22; i<100; i++) {
                sb.append(""+board[i]+",");
            }
            sb.append("]");
            return sb.toString();
        }
      }
~~~~~~~~

The class **Chess** also defines other static data. The following array is
used to encode the values assigned to each piece type (e.g., pawns are
worth one point, knights and bishops are worth 3 points, etc.):

{lang="java",linenos=off}
~~~~~~~~
      private static int [] value = {
        0, 1, 3, 3, 5, 9, 0, 0, 0, 12
      };
~~~~~~~~

The following array is used to codify the possible incremental moves for
pieces:

{lang="java",linenos=off}
~~~~~~~~
      private static int [] pieceMovementTable = {
        0, -1, 1, 10, -10, 0, -1, 1, 10, -10, -9, -11, 9,
        11, 0, 8, -8, 12, -12, 19, -19, 21, -21, 0, 10, 20,
        0, 0, 0, 0, 0, 0, 0, 0
      };
~~~~~~~~

The starting index into the pieceMovementTable array is calculated by
indexing the following array with the piece type index (e.g., pawns are
piece type 1, knights are piece type 2, bishops are piece type 3, rooks
are piece type 4, etc.:

{lang="java",linenos=off}
~~~~~~~~
      private static int [] index = {
        0, 12, 15, 10, 1, 6, 0, 0, 0, 6
      };
~~~~~~~~

When we implement the method **possibleMoves** for the class **Chess**, we
will see that except for pawn moves, all other possible piece type moves
are very easy to calculate using this static data. The method
**possibleMoves** is simple because it uses a private helper method
**calcPieceMoves** to do the real work. The method **possibleMoves**
calculates all possible moves for a given board position and side to
move by calling **calcPieceMove** for each square index that references a
piece for the side to move.

We need to perform similar actions for calculating possible moves and
squares that are controlled by each side. In the first version of the
class Chess that I wrote, I used a single method for calculating both
possible move squares and controlled squares. However, the code was
difficult to read, so I split this initial move generating method out
into three methods:

-   possibleMoves – required because this was an abstract method in GameSearch. This method calls calcPieceMoves for all squares containing pieces for the side to move, and collects all possible
    moves.
-   calcPieceMoves – responsible to calculating pawn moves and other piece type moves for a specified square index.
-   setControlData – sets the global array computerControl and humanControl. This method is similar to a combination of possibleMoves and calcPieceMoves, but takes into effect “moves” onto squares that belong to the same side for calculating the effect of one piece guarding another. This control data is used in the board position evaluation method **positionEvaluation**.

We will discuss **calcPieceMoves** here, and leave it as an exercise to
carefully read the similar method **setControlData** in the source code.
This method places the calculated piece movement data in static storage
(the array piece\_moves) to avoid creating a new Java object whenever
this method is called; method **calcPieceMoves** returns an integer count
of the number of items placed in the static array piece\_moves. The
method **calcPieceMoves** is called with a position and a square index;
first, the piece type and side are determined for the square index:

{lang="java",linenos=off}
~~~~~~~~
      private int calcPieceMoves(ChessPosition pos,
                                 int square_index) {
        int [] b = pos.board;
        int piece = b[square_index];
        int piece_type = piece;
        if (piece_type < 0) piece_type = -piece_type;
        int piece_index = index[piece_type];
        int move_index = pieceMovementTable[piece_index];
        if (piece < 0) side_index = -1;
        else              side_index = 1;
~~~~~~~~

Then, a switch statement controls move generation for each type of chess
piece (movement generation code is not shown – see the file Chess.java):

{lang="java",linenos=off}
~~~~~~~~
        switch (piece_type) {
        case ChessPosition.PAWN:
          break;
        case ChessPosition.KNIGHT:
        case ChessPosition.BISHOP:
        case ChessPosition.ROOK:
        case ChessPosition.KING:
        case ChessPosition.QUEEN:
          break;
        }
~~~~~~~~

The logic for pawn moves is a little complex but the implementation is
simple. We start by checking for pawn captures of pieces of the opposite
color. Then check for initial pawn moves of two squares forward, and
finally, normal pawn moves of one square forward. Generated possible
moves are placed in the static array piece\_moves and a possible move
count is incremented. The move logic for knights, bishops, rooks,
queens, and kings is very simple since it is all table driven. First, we
use the piece type as an index into the static array index; this value
is then used as an index into the static array **pieceMovementTable**.
There are two loops: an outer loop fetches the next piece movement delta
from the **pieceMovementTable** array and the inner loop applies the piece
movement delta set in the outer loop until the new square index is off
the board or “runs into” a piece on the same side. Note that for kings
and knights, the inner loop is only executed one time per iteration
through the outer loop:

{lang="java",linenos=off}
~~~~~~~~
        move_index = piece;
        if (move_index < 0) move_index = -move_index;
        move_index = index[move_index];
        //System.out.println("move_index="+move_index);
        next_square =
            square_index + pieceMovementTable[move_index];
      outer:
        while (true) {
          inner:
            while (true) {
              if (next_square > 99) break inner;
              if (next_square < 22) break inner;
              if (b[next_square] == 7) break inner;
                
              // check for piece on the same side:
              if (side_index < 0 && b[next_square] < 0)                 
                 break inner;
              if (side_index >0 && b[next_square]  > 0) 
                 break inner;
                
              piece_moves[count++] = next_square;
              if (b[next_square] != 0) break inner;
              if (piece_type == ChessPosition.KNIGHT)
                 break inner;
              if (piece_type == ChessPosition.KING)
                 break inner;
              next_square += pieceMovementTable[move_index];
           }
           move_index += 1;
           if (pieceMovementTable[move_index] == 0)
              break outer;
           next_square = square_index + 
                            pieceMovementTable[move_index];
       }
~~~~~~~~

The figures show the start of a second example game. The
computer was making too many trivial mistakes in the first game so here
I increased the lookahead to 2 1/2 moves. Now the computer takes one to
two seconds per move and plays a better game. Increasing the lookahead
to 3 full moves yields a better game but then the program can take up to
about ten seconds per move.

![After 1 d4 e6 2 e4 Qh4 Black (the computer) increases the mobility of its pieces by bringing out the queen early but we will see that this soon gets black in trouble.](images/chess_game_2a.png)

![After 3 Nc3 Nf6 4 Bd3 Bb4 5 Nf3 Qh5 Black continues to develop pieces and puts pressure on the pawn on E4 but the vulnerable queen makes this a weak position for black.](images/chess_game_2b.png)

The method **setControlData** is very similar to this method; I leave it
as an exercise to the reader to read through the source code. Method
**setControlData** differs in also considering moves that protect pieces
of the same color; calculated square control data is stored in the
static arrays **computerControl** and **humanControl**. This square control
data is used in the method **positionEvaluation** that assigns a numerical
rating to a specified chessboard position on either the computer or
human side. The following aspects of a chessboard position are used for
the evaluation:

-   material count (pawns count 1 point, knights and bishops 3 points, etc.)
-   count of which squares are controlled by each side
-   extra credit for control of the center of the board
-   credit for attacked enemy pieces

Notice that the evaluation is calculated initially assuming the
computer’s side to move. If the position if evaluated from the human
player’s perspective, the evaluation value is multiplied by minus one.
The implementation of **positionEvaluation** is:

{lang="java",linenos=off}
~~~~~~~~
      public float positionEvaluation(Position p,
                                      boolean player) {
        ChessPosition pos = (ChessPosition)p;
        int [] b = pos.board;
        float ret = 0.0f;
        // adjust for material:
        for (int i=22; i<100; i++) {
          if (b[i] != 0 && b[i] != 7)  ret += b[i];
        }
        
        // adjust for positional advantages:
        setControlData(pos);
        int control = 0;
        for (int i=22; i<100; i++) {
          control += humanControl[i];
          control -= computerControl[i];
        }
        // Count center squares extra:
        control += humanControl[55] - computerControl[55];
        control += humanControl[56] - computerControl[56];
        control += humanControl[65] - computerControl[65];
        control += humanControl[66] - computerControl[66];

        control /= 10.0f;
        ret += control;

        // credit for attacked pieces:
        for (int i=22; i<100; i++) {
          if (b[i] == 0 || b[i] == 7) continue;
          if (b[i] < 0) {
            if (humanControl[i] > computerControl[i]) {
                ret += 0.9f * value[-b[i]];
            }
          } 
          if (b[i] > 0) {
            if (humanControl[i] < computerControl[i]) {
              ret -= 0.9f * value[b[i]];
            }
          }
        }
        // adjust if computer side to move:
        if (!player) ret = -ret;
        return ret;
      }
~~~~~~~~

It is simple to compile and run the example chess program by typing in the **search** directory:

{linenos=off}
~~~~~~~~
    make chess
~~~~~~~~

When asked to enter moves, enter string like “d2d4” to enter a move in chess algebraic notation. Here is sample output from the program:

{linenos=off}
~~~~~~~~
    Board position:

     BR BN BB .  BK BB BN BR
     BP BP BP BP .  BP BP BP
        .     .  BP BQ    . 
     .     .     .     .    
        .     WP    .     . 
     .     .     .  WN .    
     WP WP WP .  WP WP WP WP
     WR WN WB WQ WK WB .  WR
    Your move:
    c2c4
~~~~~~~~

Note that the newest code in the GitHub repository uses Unicode characters to display graphics for Chess pieces.

The example chess program plays in general good moves, but its play
could be greatly enhanced with an “opening book” of common chess opening
move sequences. If you run the example chess program, depending on the
speed of your computer and your Java runtime system, the program takes a
while to move (about 5 seconds per move on my PC). Where is the time
spent in the chess program? The following table shows the
total runtime (i.e., time for a method and recursively all called
methods) and method-only time for the most time consuming methods.
Methods that show zero percent method only time used less than 0.1
percent of the time so they print as zero values.

{linenos=off}
~~~~~~~~
  Class.method name            % of total runtime       % in this method
  ---------------------------- ------------------------ ----------------
  Chess.main                   97.7                     0.0
  GameSearch.playGame          96.5                     0.0
  GameSearch.alphaBeta         82.6                     0.0
  GameSearch.alphaBetaHelper   82.6                     0.0
  Chess.positionEvaluate       42.9                     13.9
  Chess.setControlData         29.1                     29.1
  Chess.possibleMoves          23.2                     11.3
  Chess.calcPossibleMoves      1.7                      0.8
  Chess.calcPieceMoves         1.7                      0.8
~~~~~~~~

The interested reader is encouraged to choose a simple two-player game, and using the game search class framework, implement your own game-playing program.
