# AdvancedProgramming-FinalProject
## Exercise 1
### Graph
```java
public class Graph {
    private Vector<Node> nodes = new Vector<>(); //Nodes of the graph
    private Vector<Edge> edges = new Vector<>(); //Edges of the graf
    public void addNode(Node n) throws NodeAlreadyExistsException; //Add node to graph
    public Vector<String> getOrderNodes();  //Return a node list ordered in topological order
    public Vector<Node> orderNodes() throws NotADAGException; //Order the internal node list in a topological order
    public boolean isDAG(); //Return if graph is a DAG
    public boolean isValid(); //Check if graph has only valid inputs and their shapes are correct
    public void createEdge(); //Compute the edge, given the node list
    public Vector<Node> sink(); //Get the sink(), i.e. the final node
}
```
### Node
```java
public class Node {
    public Node(String id, int row, int col); // Constructors
    public Node(String id, Shape shape);
    public Node(String id, String op, Vector<Input> inputs);
    public String getId(): // Return the node ID
    public boolean isInitial(); // Return true if there is no incoming edge
    public Vector<Input> getInputs(); // Return the list of the input, i.e. incoming nodes
    public String getOp(); // If node is computational, returns function name, otherwise null
    public Shape getShape(); // Return a shape
    void setShape(Shape newShape); // Set a new shape
```
### Shape
```java
public class Shape {
    public int r;
    public int c;
}
```
### Input
```java
public class Input {
    private Vector<Vector<Double>> matrix = new Vector<>();
    public String id;
    Shape shape;
    private int type; // 0: id, 1: matrix
    public Input(String id); // Constructor to create intermidate node
    public Input(Vector<Vector<Double>> m); // Constructor to create an initial node
    public int getType(); //Return if node is intermediate or initial
    public Vector<Vector<Double>> matrix(); // Return a matrix if is initial, null otherwise
}
```
### Edge
``` Edge
public class Edge {
    String start;
    String end;}
```