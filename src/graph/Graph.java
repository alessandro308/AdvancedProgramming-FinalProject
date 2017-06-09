package graph;

import graph.exception.NodeAlreadyExistsException;
import graph.exception.ShapeCompatibilityException;

import java.util.Vector;

/**
 * A computational graph formalism like the one used by Machine Learning libraries such as Theano or TensorFlow. A
 * computational graph consists of nodes and edges. There are two kinds of nodes:
 *      1. Input nodes, which are placeholders for input data.
 *      2. Computational nodes, which take values from nodes on the incoming edges and compute a function on those values.
 * Edges connect nodes forming a Directed Acyclic Graph (DAG). The computation expressed by a graph is compiled into
 * executable code in a target language that gets data from inputs and performs the operations expressed in the graph.
 */
public class Graph {
    private Vector<Node> nodes = new Vector<>();
    private Vector<Edge> edges = new Vector<>();

    public void addNode(Node n) throws NodeAlreadyExistsException{
        nodes.forEach(e -> {
            if (e.getId().equals(n.getId()))
                throw new NodeAlreadyExistsException("Node with id " + n.getId() + " already exists");
        });
        nodes.add(n);
    }
    public Vector<Node> getNodes(){
        return (Vector<Node>) nodes.clone();
    }
    public Vector<Node> getInitial(){
        Vector<Node> init = new Vector<>();
        for(Node n : nodes){
            if(n.isInitial())
                init.add(n);
        }
        return init;
    }

    //------------------ISDAG() HANDLERS------------------
    private Vector<Edge> filterEdge(Vector<Edge> edges, String start, String end){
        Vector<Edge> res = new Vector<>();
        if(start == null && end == null){
            return null;
        } else if (start == null){
            for(Edge e : edges){
                if(e.end.equals(end))
                    res.add(e);
            }
        } else if (end == null){
            for(Edge e : edges){
                if(e.start.equals(start))
                    res.add(e);
            }
        } else{
            for(Edge e : edges){
                if(e.start.equals(start) && e.end.equals(end))
                    res.add(e);
            }
        }

        return res.size() == 0 ? null : res;
    }
    public boolean isDAG(){
        System.out.println(this.getNode("d").getInputs().get(1).shape.r);
        /* Kahn's algorithm // https://en.wikipedia.org/wiki/Topological_sorting */
        Vector<String> L = new Vector<>();
        Vector<String> S = new Vector<>();
        Vector<Edge> edges2 = (Vector<Edge>) this.edges.clone();
        for(Node n : nodes){
            if(filterEdge(edges2, null, n.getId()) == null)
                S.add(n.getId());
        }
        while(S.size() != 0){
            String n = S.remove(0);
            L.add(n);
            Vector<Edge> filteredEdge = filterEdge(edges2, n, null);
            if(filteredEdge != null)
                for(Edge e : filteredEdge){
                    edges2.remove(e);
                    if(filterEdge(edges2, null, e.end) == null){
                        S.add(e.end);
                    }
                }
        }
        return edges2.size() <= 0;
    }

    // --------ISVALID handlers-----
    private Shape mulShape(Shape s1, Shape s2){
        if(s1.c == s2.r){
            return new Shape(s1.r, s2.c);
        }
        return null;
    }
    Shape computingShape(Input in) throws ShapeCompatibilityException{
        // Compute shape recursively.
        // return null on failure
        if(in.getType()==1)
            return in.shape;
        Node n = getNode(in.id);
        if(n.isInitial())
            return n.getShape();
        if(n.getShape() != null)
            return n.getShape(); //The shape was already computed
        Vector<Input> inputs = n.getInputs();
        switch (n.getOp()){
            case "sum":
                Shape referenceShape = computingShape(inputs.get(0));
                for(int i = 1; i<inputs.size(); i++){
                    Input input = inputs.get(i);
                    if(!referenceShape.equals(computingShape(input))) {
                        System.out.println("SHAPE ERROR "+n.getId());
                        throw new ShapeCompatibilityException();
                    }
                }
                n.setShape(referenceShape); //Equal to components
                break;
            case "mult":
                Shape res = computingShape(inputs.get(0));
                for(int i = 1; i<inputs.size(); i++){
                    Shape nextShape = computingShape(inputs.get(i));
                    if((res = mulShape(res, nextShape)) == null) {
                        System.out.println("SHAPE ERROR2 " + n.getOp());
                        throw new ShapeCompatibilityException();
                    }
                }
                n.setShape(res);
                break;
        }
        return n.getShape();

    }

    public boolean isValid(){
        for(Node n : nodes){
            if(!n.isInitial()){
                for(Input input : n.getInputs()){
                    if(input.getType() == 0 && getNode(input.id) == null) {
                        System.out.println("Node " + input.id + " not exists");
                        return false;
                    }
                }
            }
        }
        try{
            for(Node n: sink()){
                computingShape(new Input(n.getId()));
            }
        }catch (ShapeCompatibilityException e){
            System.out.println("Invalid shape");
            return false;
        }
        return true;
    }


    Node getNode(String id){
        for(Node n : nodes){
            if(n.getId().equals(id))
                return n;
        }
        return null;
    }

    public void createEdge(){
        for(Node n : nodes){
            if(!n.isInitial()){
                for(Input in : n.getInputs()){
                    if(in.getType() == 0)
                        edges.add(new Edge(in.id, n.getId()));
                }
            }
        }
    }

    private Vector<Node> sink(){
        //returns first sink found
        Vector<Node> nodes = (Vector<Node>) this.nodes.clone();
        for(Node no : this.nodes){
            if(!no.isInitial()){
                for(Input in : no.getInputs()){
                    nodes.remove(getNode(in.id));
                }
            }
        }
        return nodes.size() == 0 ? null : nodes;
    }

}
