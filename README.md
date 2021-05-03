This is a solution for solving searching paths in a directed graph.

# Table of Contents

- [Problem Overview](#problem-overview)
- [Solution Approach](#solution-approach)
- [Runtime Performance](#runtime-performance)
- [Testing Methodology](#testing-methodology)
- [How to run](#how-to-run)

# Problem Overview

Asks:

- Create a file reader that processes inputs into a directed graph.
- Find the shortest distance between two nodes.
- Find the distance of a direct route between nodes.
- Find the exact and maximum number of routes between two points.
- Find the maximum number of routes between two points limited by a maximum distance.

# Solution Approach

Java is the language that was utilized; the solution was created using a test driven approach, writing tests first, then
performing a red / green refactor to a solution.

The first approach of find a direct route was using a lookup to a graph for the edges between nodes. I then completed an
algorithm for a depth first search to find the exact and maximum number of routes as well as the maximum number of
routes limited by the total distance. Once the DFS was completed I refactored the direct search to utilize the DFS code
thereby simplifying the solution, reducing code, and utilizing the tests to maintain correctness of execution.

For the shortest path search I used the [Dijkstra's algorithm](https://en.wikipedia.org/wiki/Dijkstra%27s_algorithm)
greedy approach. The initial implementation used a sorted list to maintain the proper queue, then I refactored to use
a [Priority Queue](https://docs.oracle.com/javase/7/docs/api/java/util/PriorityQueue.html). I tested a queue
implementation using the OOTB [Tree set](https://docs.oracle.com/javase/7/docs/api/java/util/TreeSet.html) for increased
performance however I found that the implementation did not support multiple equal values. If I had more time I would
implement using a heap or tree set that supports multiple weights.

# Runtime Performance

n = nodes

e = edges

Find the shortest distance between two nodes:

- O(n): O((n+e)log n)

Find the distance of a direct route between nodes:

- O(n): O(e)
- In my implementation the worst case would be searching all edges to a node.
- This can likely be improved with additional implementation.

Find the exact and maximum number of routes between two points:

- O(n): O(infinity)
- I think the worst case would be if the maximum number of routes was infinite then there would, in turn, be an infinite
  number of routes between the target nodes.

Find the maximum number of routes between two points limited by a maximum distance:

- O(n): O(infinity)
- I think the worst case would be if the maximum distance was infinite then there would, in turn, be an infinite number
  of routes between the target nodes.

_Note: I found this [site](https://www.happycoders.eu/algorithms/dijkstras-algorithm-java/) to have a very good
treatment on the runtime complexity of Dijkstra and it served an example of my initial implementation._

# Testing Methodology

I used a TDD approach, wrote the test firsts, made the tests pass, then refactored liberally with confidence that my
tests enforce the expected behavior.

# How to run
 
I created the class ```WhitespaceApplicationTest``` as a wrapper to allow the easy testing of the solution. In order to
test the solution using your own framework do the following:
1. have a test file loaded on the classpath. Use the example referencing ```TestGraph.txt``` as an example for both
referencing the file and executing the test.
2. Change the calls to the ```WhitespaceApplication``` that meets your testing specifications.
3. If you desire further automation please add the [AssertJ](https://assertj.github.io/doc/) fluid assertions.  