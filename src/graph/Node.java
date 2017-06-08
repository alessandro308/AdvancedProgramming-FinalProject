package graph;

import java.util.Vector;

public class Node {
    private String id = "";
    private boolean isInitial;
    private Shape shape;
    private Vector<Input> inputs;
    private String op;
    public Node(String id, int row, int col){
        this.id = id;
        this.shape.r = row;
        this.shape.c = col;
        this.isInitial=true;
    }
    public Node(String id, Shape shape){
        this.id = id;
        this.shape = shape;
        this.isInitial = true;
    }
    public Node(String id, String op, Vector<Input> inputs){
        this.id = id;
        this.op = op;
        this.inputs = inputs;
    }
    String getId(){
        return this.id;
    }

    boolean isInitial(){
        return this.isInitial;
    }

    Vector<Input> getInputs(){
        return this.inputs;
    }

    String getOp(){
        return this.op;
    }
    Shape getShape(){
        return this.shape;
    }
    void setShape(Shape newShape){
        this.shape = newShape;
    }

}
