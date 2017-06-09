package graph;

import java.util.Vector;

public class Input {
    private Vector<Vector<Double>> matrix = new Vector<>();
    public String id;
    Shape shape;
    private int type; // 0: id, 1: matrix
    public Input(String id){
        this.type = 0;
        this.id = id;
    }
    public Input(Vector<Vector<Double>> m){
        this.matrix = m;
        shape = new Shape(m.size(), m.get(0).size());
        this.type = 1;
    }
    public int getType(){
        return type;
    }
    public Vector<Vector<Double>> matrix(){
        return (Vector<Vector<Double>>) matrix.clone();
    }
}
