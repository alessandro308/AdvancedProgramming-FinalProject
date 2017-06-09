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
## Exercise 2
```java
package compiler;
import graph.*;
import compiler.exception.*;
import java.io.*;
import java.util.Vector;

public class Parser {
    private String toParse;
    private StreamTokenizer input;
    private Graph g = new Graph();
    public Parser(String toParse){
        this.toParse = toParse;
        input = new StreamTokenizer(new StringReader(toParse));
        input.wordChars('a', 'z');
        input.wordChars('A', 'Z');
        input.ordinaryChar('{');
        input.ordinaryChar('}');
        input.ordinaryChar('"');
        input.ordinaryChar(':');
        input.ordinaryChar(',');
        input.ordinaryChar('[');
        input.ordinaryChar(']');
        input.wordChars('0', '9');
        input.whitespaceChars(' ', ' ');
        input.eolIsSignificant(false);
    }

    private void expect(char c) throws UnexpectedTokenException{
        try{
            input.nextToken();
            if(input.ttype != c)
                throw new UnexpectedTokenException("Expected Token "+c+" - Found "+(char)input.ttype);
        }catch (IOException e){
            throw new ParseFailed(e.getMessage());
        }
    }
    private void expect(int type) throws ParseFailed, IOException{
            int i = input.nextToken();
            if(i != type)
                throw new ParseFailed("expect error - Failed Parsing on type "+ type + " on line "+input.lineno()+ ". Found "+i);
    }
    private Shape readShape() throws IOException, UnexpectedTokenException{
        Shape shape = new Shape();
        expect('[');
        expect(StreamTokenizer.TT_NUMBER);
        shape.r = (int) input.nval;
        expect(',');
        expect(StreamTokenizer.TT_NUMBER);
        shape.c = (int) input.nval;
        expect(']');
        return shape;
    }
    private int readType() throws IOException, UnexpectedTokenException{
        /* 0 = input, 1 = intermediate */
        expect('"');
        expect(StreamTokenizer.TT_WORD);
        int type = input.sval.equals("input")?0:1;
        expect('"');
        return type;
    }
    private String readWord() throws IOException, UnexpectedTokenException {
        expect('"');
        expect(StreamTokenizer.TT_WORD);
        String i = input.sval;
        expect('"');
        return i;
    }
    private Vector<Vector<Double>> readMatrix() throws UnexpectedTokenException, IOException {
        Vector<Vector<Double>> matrix = new Vector<>();
        expect('[');
        do{
            Vector<Double> row = new Vector<>();
            expect('[');
            do{
                expect(StreamTokenizer.TT_NUMBER);
                row.add(input.nval);
                input.nextToken();
                if(input.ttype != ']' && input.ttype != ',')
                    throw new UnexpectedTokenException("Read Matrix Error - Found "+input.ttype);
            }while (input.ttype != ']');
            input.nextToken();
            if(input.ttype != ']' && input.ttype != ',')
                throw new UnexpectedTokenException("Read Matrix Error - Found "+input.ttype);
            matrix.add(row);
        }while (input.ttype != ']');
        int col = matrix.get(0).size();
        for(int i = 1; i<matrix.size(); i++){
            if(matrix.get(i).size() != col)
                throw new ParseFailed("Invalid Matrix Size");
        }
        return matrix;
    }

    private Vector<Input> readIn() throws IOException, UnexpectedTokenException{
        Vector<Input> inputs = new Vector<>();
        expect('[');
        input.nextToken();
        while(input.ttype != ']'){
            switch (input.ttype) {
                case '"':
                    expect(StreamTokenizer.TT_WORD);
                    inputs.add(new Input(input.sval));
                    expect('"');
                    break;
                case '[':
                    input.pushBack();
                    inputs.add(new Input(readMatrix()));
                    break;
                default:
                    throw new UnexpectedTokenException("Read In - Found "+ input.ttype + " on input line "+input.lineno());
            }
            input.nextToken();
            if(input.ttype == ',')
                input.nextToken();
        }
        if(input.ttype == ']')
            return inputs;
        throw new UnexpectedTokenException("No closed bracket found.");
    }

    private void parseNode(){
        String id = null;
        int type = -1;
        Shape shape = new Shape();
        Vector<Input> in = new Vector<>();
        String op = null;
        try{
            id = readWord();
            expect(':');
            expect('{');
            input.nextToken();
            while(input.ttype != '}') {
                if (input.ttype != '"' && input.ttype != ',')
                    throw new ParseFailed("Double quotes not found. Found " + (char) input.ttype);
                if(input.ttype == ',')
                    expect('"');
                expect(StreamTokenizer.TT_WORD);
                switch (input.sval) {
                    case "type":
                        expect('"');
                        expect(':');
                        type = readType(); break;
                    case "shape":
                        expect('"');
                        expect(':');
                        shape = readShape(); break;
                    case "in":
                        expect('"');
                        expect(':');
                        in = readIn(); break;
                    case "op":
                        expect('"');
                        expect(':');
                        op = readWord(); break;
                    default:
                        throw new ParseFailed("Unexpected Token " + input.sval);
                }
                input.nextToken();
            }
        }catch (UnexpectedTokenException | IOException | ParseFailed e){
            throw new ParseFailed("Node Parsing Failed - "+e.getMessage());
        }
        if(type == 0)
            g.addNode(new Node(id, shape));
        if(type == 1 && op == null)
            throw new ParseFailed("Not found OP");
        if(type == 1)
            g.addNode(new Node(id, op, in));
    }

    public void parseGraph() throws UnexpectedTokenException, IOException {
        expect('{');
        parseNode();
        input.nextToken();
        while(input.ttype == ','){
            parseNode();
            input.nextToken();
        }
        if(input.ttype == '}') {
            g.createEdge();
            return;
        }
        throw new ParseFailed("Unexpected token found. Seached } - Found "+input.ttype);
    }
```