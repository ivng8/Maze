import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;

import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;

// represents a utils class that has useful methods
class Utils {

  // it will find the representative of a node by sifting through a hashmap
  // it will only return itself if it is its own representative
  // other wise it will search for what the node gets from the Hashmap until
  // you do find a node that gets itself
  Vertex find(HashMap<Vertex, Vertex> representatives, Vertex node) {
    if (representatives.get(node).isEqual(node)) {
      return node;
    }
    else {
      return find(representatives, representatives.get(node));
    }
  }

  // for the purposes of a search algorithm
  // this method reconstructs a Hashmap of vertex to Edges using a vertex
  // it does this by starting a list of vertices that we called path
  // what it does is it will add the given Vertex to the path which is going to be
  // the
  // ending square of the maze
  // and as long as the HashMap is not empty
  // it will get the edge that the Vertex is apart of from the Hashmap
  // look at the other end of that Edge and add it to the path
  // then it removes the edge from the Hashmap and recursively calls the process
  // again
  // on the other end of the edge
  // this will accumulate a path of vertices that lead back to the beginning of
  // the maze
  ArrayList<Vertex> reconstruct(HashMap<Vertex, Edge> cameFromEdge, Vertex ending) {
    ArrayList<Vertex> path = new ArrayList<Vertex>();
    path.add(ending);
    Vertex pivot = ending;
    while (!cameFromEdge.isEmpty()) {
      Vertex other = cameFromEdge.get(pivot).getOtherEnd(pivot);
      cameFromEdge.remove(pivot);
      path.add(other);
      pivot = other;
    }
    return path;
  }
}

// Represents a mutable collection of items
interface ICollection<T> {
  
  // Is this collection empty?
  boolean isEmpty();

  // EFFECT: adds the item to the collection
  void add(T item);

  // Returns the first item of the collection
  // EFFECT: removes that first item
  T remove();
}

// represents a stack collection that is used for depth search
class Stack<T> implements ICollection<T> {
  Deque<T> contents;

  Stack() {
    this.contents = new ArrayDeque<T>();
  }

  // is the stack empty?
  public boolean isEmpty() {
    return this.contents.isEmpty();
  }

  // returns the element that it is removing from the front of the stack
  public T remove() {
    return this.contents.removeFirst();
  }

  // adds an element to the front of the stack
  public void add(T item) {
    this.contents.addFirst(item);
  }
}

// represents a queue collection that is used for breadth search
class Queue<T> implements ICollection<T> {
  Deque<T> contents;

  Queue() {
    this.contents = new ArrayDeque<T>();
  }

  // is the queue empty?
  public boolean isEmpty() {
    return this.contents.isEmpty();
  }

  // returns the element that it is removing from the front
  public T remove() {
    return this.contents.removeFirst();
  }

  // adds an element to the back
  public void add(T item) {
    this.contents.addLast(item);
  }
}

// represents the game
class Maze extends World {
  private HashMap<Posn, Vertex> vertices;
  private final int width;
  private final int height;
  private Posn player;

  Maze(int width, int height) {
    this.vertices = new HashMap<Posn, Vertex>();
    this.width = width;
    this.height = width;
    this.player = new Posn(0, this.height - 1);

    // uses a nested for loop that is supposed to use every combination
    // of Posn's using the mazes desired height and width
    // to make a Vertex for each point
    // the for loop uses if cases that mark the top left as
    // the starting point
    // and the bottom right as the end goal
    HashMap<Vertex, Vertex> representatives = new HashMap<Vertex, Vertex>();
    // represents a list where a vertex gets its representative of its tree
    // initializes every vertex as its own representative
    for (int j = 0; j < height; j += 1) {
      for (int i = 0; i < width; i += 1) {
        if (i == 0 && j == height - 1) {
          this.vertices.put(new Posn(i, j), new Vertex(true, false, i, j));
          representatives.put(this.vertices.get(new Posn(i, j)), this.vertices.get(new Posn(i, j)));
        }
        else if (i == width - 1 && j == 0) {
          this.vertices.put(new Posn(i, j), new Vertex(false, true, i, j));
          representatives.put(this.vertices.get(new Posn(i, j)), this.vertices.get(new Posn(i, j)));
        }
        else {
          this.vertices.put(new Posn(i, j), new Vertex(false, false, i, j));
          representatives.put(this.vertices.get(new Posn(i, j)), this.vertices.get(new Posn(i, j)));
        }
      }
    }

    // the constructor will then make a list of a edges
    // in between every Vertex for the sake of randomizing the graph
    ArrayList<Edge> edges = new ArrayList<Edge>();
    // uses a nested for loop where for every Posn of the maze
    // it makes a edge between them
    for (int j = 0; j < height; j += 1) {
      Edge leftEdge = null;
      Edge downEdge = null;
      for (int i = 0; i < width; i += 1) {
        if (i > 0 && j == 0) {
          leftEdge = new Edge(this.vertices.get(new Posn(i - 1, j)),
              this.vertices.get(new Posn(i, j)));
          edges.add(leftEdge);
        }
        else if (i == 0 && j > 0) {
          downEdge = new Edge(this.vertices.get(new Posn(i, j - 1)),
              this.vertices.get(new Posn(i, j)));
          edges.add(downEdge);
        }
        else if (i > 0 && j > 0) {
          leftEdge = new Edge(this.vertices.get(new Posn(i - 1, j)),
              this.vertices.get(new Posn(i, j)));
          downEdge = new Edge(this.vertices.get(new Posn(i, j - 1)),
              this.vertices.get(new Posn(i, j)));
          edges.add(leftEdge);
          edges.add(downEdge);
        }
      }
    }

    // sorts the list from lowest to highest weight
    edges.sort(new CompareEdge());

    // represents the list of edges in the spanning tree
    ArrayList<Edge> spanning = new ArrayList<Edge>();

    for (int z = 0; z < edges.size(); z += 1) {
      if (edges.get(z).validSpan(representatives)) {
        spanning.add(edges.get(z));
        edges.get(z).union(representatives);
      }
    }

    // for every edge in the spanning list it will remove the walls on
    // the vertices to make that pathway
    for (int p = 0; p < spanning.size(); p += 1) {
      spanning.get(p).removeWall();
    }
  }

  // makes the world scene
  public WorldScene makeScene() {
    WorldScene scene = new WorldScene((this.width + 1) * 10, (this.height + 1) * 10);
    WorldImage maze = new EmptyImage();
    for (int y = 0; y < this.height; y += 1) {
      WorldImage row = new EmptyImage();
      for (int x = 0; x < this.width; x += 1) {
        Vertex current = this.vertices.get(new Posn(x, y));
        WorldImage currentBlock = null;
        currentBlock = current.imageAdder();
        row = new BesideImage(row, currentBlock);
      }
      maze = new AboveImage(row, maze);
    }
    scene.placeImageXY(maze, this.width * 6, this.height * 6);
    return scene;
  }

  // ends the world if detects that the player is on the ending square
  public boolean shouldWorldEnd() {
    if (this.player == new Posn(0, this.width - 1)) {
      this.endOfWorld("Congrats you won!");
      return true;
    }
    return false;
  }

  // handles key events
  public void onKeyEvent(String key) {
    if (key.equals("up")) {
      if (this.vertices.get(player).isMoveable("up")) {
        Vertex newV = this.vertices.get(new Posn(player.x, player.y + 1));
        newV.markVisited();
        player = new Posn(this.player.x, this.player.y + 1);
      }
    }
    if (key.equals("down")) {
      if (this.vertices.get(player).isMoveable("down")) {
        Vertex newV = this.vertices.get(new Posn(player.x, player.y - 1));
        newV.markVisited();
        player = new Posn(this.player.x, this.player.y - 1);
      }
    }
    if (key.equals("left")) {
      if (this.vertices.get(player).isMoveable("left")) {
        Vertex newV = this.vertices.get(new Posn(player.x - 1, player.y));
        newV.markVisited();
        player = new Posn(this.player.x - 1, this.player.y);
      }
    }
    if (key.equals("right")) {
      if (this.vertices.get(player).isMoveable("right")) {
        Vertex newV = this.vertices.get(new Posn(player.x + 1, player.y));
        newV.markVisited();
        player = new Posn(this.player.x + 1, this.player.y);
      }
    }
    if (key.equals("b")) {
      this.breadthSearch();
    }
    if (key.equals("d")) {
      this.depthSearch();
    }

    if (this.player == new Posn(this.width - 1, 0)) {
      this.endOfWorld("Congrats you won!");
    }
  }

  // initializes breadth search
  // does this by having a hashMap of vertex to Edge in order to save a path
  // it also uses a Queue as a workingList
  // what this will do is initialize the workingList with the beginning square
  // and start the while loop where it will keep
  // processing the Queue as long as it is not empty
  // in the case that hashMap does contain the Vertex as
  // a key which it won't on first instance
  // because it is new, it will do nothing with the Vertex that is removed from
  // the Queue
  // if the Vertex removed is the ending square
  // that means that you've reach your solution and it will beginning
  // reconstructing the path
  // that leads to the ending and highlighting it
  // for all other instances it will expand the search onto the Vertex's
  // valid neighbors using a helper
  // and add them to the Queue
  void breadthSearch() {
    HashMap<Vertex, Edge> cameFromEdge = new HashMap<Vertex, Edge>();
    Queue<Vertex> workingList = new Queue<Vertex>();
    workingList.add(this.vertices.get(new Posn(0, this.height - 1)));
    while (!workingList.isEmpty()) {
      Vertex next = workingList.remove();
      if (next.getEnd()) {
        ArrayList<Vertex> solution = new Utils().reconstruct(cameFromEdge, next);
        for (int i = 0; i < solution.size(); i += 1) {
          solution.get(i).markSolution();
        }
      }
      else if (!cameFromEdge.containsKey(next)) {
        ArrayList<Vertex> neighbors = next.searchNeighbors(this.vertices);
        for (int i = 0; i < neighbors.size(); i += 1) {
          workingList.add(neighbors.get(i));
          cameFromEdge.put(neighbors.get(i), new Edge(next, neighbors.get(i)));
        }
      }
    }
  }

  // initializes depth search
  // it follows the same logic as the breadth search above
  // except it uses a Stack for the workingList which removes and adds
  // from the front meaning that it will
  // flesh out one path
  void depthSearch() {
    HashMap<Vertex, Edge> cameFromEdge = new HashMap<Vertex, Edge>();
    Stack<Vertex> workingList = new Stack<Vertex>();
    workingList.add(this.vertices.get(new Posn(0, this.height - 1)));
    while (!workingList.isEmpty()) {
      Vertex next = workingList.remove();
      if (next.getEnd()) {
        ArrayList<Vertex> solution = new Utils().reconstruct(cameFromEdge, next);
        for (int i = 0; i < solution.size() - 1; i += 1) {
          solution.get(i).markSolution();
        }
      }
      else if (!cameFromEdge.containsKey(next)) {
        ArrayList<Vertex> neighbors = next.searchNeighbors(this.vertices);
        for (int i = 0; i < neighbors.size(); i += 1) {
          workingList.add(neighbors.get(i));
          cameFromEdge.put(neighbors.get(i), new Edge(next, neighbors.get(i)));
        }
      }
    }
  }
}

// represents a comparator sorts edges by their weight
class CompareEdge implements Comparator<Edge> {

  // returns the difference in weights which will result in sorting
  // smallest to largest
  public int compare(Edge o1, Edge o2) {
    return o1.getWeight() - o2.getWeight();
  }
}

// represents an edge connection between two vertices
class Edge {
  private final Vertex from;
  private final Vertex to;
  private final int weight;

  // has a random weight for the purpose of generating the maze
  Edge(Vertex from, Vertex to) {
    this.from = from;
    this.to = to;
    this.weight = (int) (Math.random() * 100);
  }

  // calls to see if the Edge contains a certain Vertex
  // by calling equality on both of its ends
  boolean contains(Vertex node) {
    return this.from.isEqual(node) || this.to.isEqual(node);
  }

  // due to the spanning tree algorithm
  // in essence having the edge is represented by the lack of a wall
  // on a Vertex so it removes the wall between two Vertices
  void removeWall() {
    this.to.deleteWall(this.from);
  }

  // is a getter with the sole purpose of using a comparator
  int getWeight() {
    return this.weight;
  }

  // returns if the edge is valid to be put into the minimum spanning tree
  // by testing if both vertices in the edge will be found in the
  // hashmap because if they are both foudn it means that they are already linked
  boolean validSpan(HashMap<Vertex, Vertex> representatives) {
    return !new Utils().find(representatives, this.from)
        .isEqual(new Utils().find(representatives, this.to));
  }

  // it unionizes the spanning tree by setting one vertexes representative
  // as the other vertexes representative
  void union(HashMap<Vertex, Vertex> representatives) {
    representatives.replace(new Utils().find(representatives, this.from),
        new Utils().find(representatives, this.to));
  }

  // for the sake of reconstruction it returns the vertex of an edge
  // that is not the vertex given
  Vertex getOtherEnd(Vertex pivot) {
    if (this.to.isEqual(pivot)) {
      return this.from;
    }
    else {
      return this.to;
    }
  }
}

// represents a cell in the maze
class Vertex {
  private boolean starting;
  private boolean ending;
  private boolean left;
  private boolean up;
  private boolean right;
  private boolean down;
  private final int x;
  private final int y;
  private boolean searched;
  private boolean visited;
  private boolean solution;

  // has booleans in the 4 directions that represent
  // if that side of the cell has a wall
  // starts off as true in all direction until mutated in the
  // creation of a maze
  Vertex(boolean starting, boolean ending, int x, int y) {
    this.starting = starting;
    this.ending = ending;
    this.left = true;
    this.up = true;
    this.right = true;
    this.down = true;
    this.x = x;
    this.y = y;
    this.searched = false;
    this.visited = false;
    this.solution = false;
  }

  // tests equality for Vertices by comparing their posns because
  // all vertices have a unique posn
  boolean isEqual(Vertex node) {
    return this.x == node.x && this.y == node.y;
  }

  // is a getting for the purpose of identifying if it is the ending square
  boolean getEnd() {
    return this.ending;
  }

  // highlights the Vertex as part of the path in the solution
  void markSolution() {
    this.solution = true;
  }

  // searches the valid neighbors of a Vertex for the purpose of search algorithms
  // it will return a list of a valid neighbors by determining if there is a wall
  // in that direction
  // during the process it also highlights itself to identify to the user
  // that the vertex was part of the search algorithm
  ArrayList<Vertex> searchNeighbors(HashMap<Posn, Vertex> vertices) {
    ArrayList<Vertex> neighbors = new ArrayList<Vertex>();
    if (!this.up) {
      this.searched = true;
      neighbors.add(vertices.get(new Posn(x, y + 1)));
    }
    if (!this.left) {
      this.searched = true;
      neighbors.add(vertices.get(new Posn(x - 1, y)));
    }
    if (!this.down) {
      this.searched = true;
      neighbors.add(vertices.get(new Posn(x, y - 1)));
    }
    if (!this.right) {
      this.searched = true;
      neighbors.add(vertices.get(new Posn(x + 1, y)));
    }
    return neighbors;
  }

  // it removes the figurative wall represented by a direction and boolean
  // between two vertices
  void deleteWall(Vertex from) {
    if (from.y - this.y < 0) {
      from.up = false;
      this.down = false;
    }
    else if (from.y - this.y > 0) {
      from.down = false;
      this.up = false;
    }
    else if (from.x - this.x < 0) {
      from.right = false;
      this.left = false;
    }
    else if (from.x - this.x > 0) {
      from.left = false;
      this.right = false;
    }
  }

  // based on the movement, it adjusts the color to the corresponding path
  void markVisited() {
    this.visited = true;
  }

  // checks if can move depending on the direction
  boolean isMoveable(String d) {
    if (d.equals("up")) {
      return !this.up;
    }
    if (d.equals("down")) {
      return !this.down;
    }
    if (d.equals("left")) {
      return !this.left;
    }
    if (d.equals("right")) {
      return !this.right;
    }
    return false;
  }

  // getter used only for testing purposes only
  public boolean isSolution() {
    return this.solution;
  }

  // getter used only for testing purposes only
  public boolean getUp() {
    return this.up;
  }

  // getter used only for testing purposes only
  public boolean getDown() {
    return this.down;
  }

  // adds the image to the WorldImage
  WorldImage imageAdder() {
    WorldImage currentBlock = null;
    if (this.searched && !this.starting && this.ending) {
      currentBlock = new RectangleImage(10, 10, OutlineMode.SOLID, Color.BLUE.brighter());
    }
    else if (this.visited) {
      currentBlock = new RectangleImage(10, 10, OutlineMode.SOLID, Color.CYAN);
    }
    else if (this.ending) {
      currentBlock = new RectangleImage(10, 10, OutlineMode.SOLID, Color.MAGENTA);
    }
    else if (this.starting) {
      currentBlock = new RectangleImage(10, 10, OutlineMode.SOLID, Color.GREEN.darker().darker());
    }
    else if (this.solution) {
      currentBlock = new RectangleImage(10, 10, OutlineMode.SOLID, Color.BLUE);
    }
    else {
      currentBlock = new RectangleImage(10, 10, OutlineMode.SOLID, Color.LIGHT_GRAY);
    }
    if (this.right) {
      currentBlock = new BesideImage(currentBlock,
          new RectangleImage(1, 10, OutlineMode.SOLID, Color.BLACK));
    }
    else {
      currentBlock = new BesideImage(currentBlock,
          new RectangleImage(1, 10, OutlineMode.SOLID, Color.LIGHT_GRAY));
    }
    if (this.up) {
      currentBlock = new AboveImage(new RectangleImage(10, 1, OutlineMode.SOLID, Color.BLACK),
          currentBlock);
    }
    else {
      currentBlock = new AboveImage(new RectangleImage(10, 1, OutlineMode.SOLID, Color.LIGHT_GRAY),
          currentBlock);
    }
    if (this.down) {
      currentBlock = new AboveImage(currentBlock,
          new RectangleImage(10, 1, OutlineMode.SOLID, Color.BLACK));
    }
    else {
      currentBlock = new AboveImage(currentBlock,
          new RectangleImage(10, 1, OutlineMode.SOLID, Color.LIGHT_GRAY));
    }
    if (this.left) {
      currentBlock = new BesideImage(new RectangleImage(1, 10, OutlineMode.SOLID, Color.BLACK),
          currentBlock);
    }
    else {
      currentBlock = new BesideImage(new RectangleImage(1, 10, OutlineMode.SOLID, Color.LIGHT_GRAY),
          currentBlock);
    }
    return currentBlock;
  }
}

class Examples {

  // big bang test for the maze
  public void testMaze(Tester t) {
    Maze maze = new Maze(20, 20);
    maze.bigBang(20 * 15, 20 * 15);
  }

  Vertex v1 = new Vertex(false, false, 1, 2);
  Vertex v2 = new Vertex(false, false, 1, 3);
  Vertex v3 = new Vertex(false, false, 1, 4);
  Edge e1 = new Edge(v1, v2);
  HashMap<Vertex, Vertex> test = new HashMap<>();
  HashMap<Posn, Vertex> neighbor = new HashMap<>();

  void initData() {
    test.put(v1, v1);
    test.put(v2, v2);
    test.put(v3, v3);
    neighbor.put(new Posn(1, 2), v1);
    neighbor.put(new Posn(1, 3), v2);
    neighbor.put(new Posn(1, 4), v3);
  }

  // tests the contains method for edge
  public boolean testContains(Tester t) {
    return t.checkExpect(e1.contains(v1), true) && t.checkExpect(e1.contains(v3), false);
  }

  public boolean testCOntains(Tester t) {
    e1.removeWall();
    return t.checkExpect(v1.getUp(), false) && t.checkExpect(v2.getDown(), false);
  }

  // Tests for getEnd()
  public boolean testGetEnd(Tester t) {
    Vertex vertex1 = new Vertex(true, false, 0, 0);
    Vertex vertex2 = new Vertex(false, true, 1, 1);
    return t.checkExpect(vertex1.getEnd(), false) && t.checkExpect(vertex2.getEnd(), true);
  }

  // Tests for markSolution()
  public void testMarkSolution(Tester t) {
    Vertex vertex1 = new Vertex(true, false, 0, 0);
    Vertex vertex2 = new Vertex(false, true, 1, 1);
    vertex1.markSolution();
    vertex2.markSolution();
    t.checkExpect(vertex1.isSolution(), true);
    t.checkExpect(vertex2.isSolution(), true);
  }

  // tests valid span for an edge
  public boolean testValidSpan(Tester t) {
    initData();
    return t.checkExpect(e1.validSpan(test), true);
  }

  // tests search neighbors for vertex
  public boolean testSearchNeighbors(Tester t) {
    initData();
    return t.checkExpect(v1.searchNeighbors(neighbor).size(), 1);
  }

  // tests union
  public boolean testUnion(Tester t) {
    initData();
    e1.union(test);
    return t.checkExpect(test.get(v1), v2);
  }

  public boolean testFind(Tester t) {
    initData();
    Utils u = new Utils();
    return t.checkExpect(u.find(test, v1), v2);
  }
}