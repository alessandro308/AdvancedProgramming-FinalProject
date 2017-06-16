package graph;

import compiler.exception.NotADAGException;
import graph.exception.NodeAlreadyExistsException;
import graph.exception.ShapeCompatibilityException;

import java.util.HashMap;
import java.util.Vector;

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
        /* Restituisce l'insieme dei nodi che non hanno archi entranti */
        Vector<Node> init = new Vector<>();
        for(Node n : nodes){
            if(n.isInitial())
                init.add(n);
        }
        return init;
    }

    //------------------ISDAG() HANDLERS------------------
    private Vector<Edge> filterEdge(Vector<Edge> edges, String start, String end){
        /*Restituisce gli archi di edges, filtrandoli secondo i parametri start ed end, eventualmente null*/
        Vector<Edge> res = new Vector<>();
        if(start == null && end == null) return null;
        else if (start == null){ for(Edge e : edges) if(e.end.equals(end)) res.add(e);}
        else if (end == null) {for(Edge e : edges) if(e.start.equals(start)) res.add(e);}
        else { for(Edge e : edges) if(e.start.equals(start) && e.end.equals(end)) res.add(e); }
        return res.size() == 0 ? null : res;
    }
    public Vector<String> getOrderNodes(){
        /* Restituisce i nodi ordinati topologicamente.
         * L'algoritmo usato è il Kahn's algorithm // https://en.wikipedia.org/wiki/Topological_sorting
         * */
        Vector<String> L = new Vector<>();
        Vector<String> S = new Vector<>();
        Vector<Edge> edges2 = (Vector<Edge>) this.edges.clone();
        for(Node n : nodes) if(filterEdge(edges2, null, n.getId()) == null) S.add(n.getId());

        while(S.size() != 0){
            String n = S.remove(0);
            L.add(n);
            Vector<Edge> filteredEdge = filterEdge(edges2, n, null);
            if(filteredEdge != null)
                for(Edge e : filteredEdge){
                    edges2.remove(e);
                    if(filterEdge(edges2, null, e.end) == null) S.add(e.end);
                }
        }
        return edges2.size() <= 0 ? L : null;
    }

    public Vector<Node> orderNodes() throws NotADAGException{
        //Edit this.nodes with results of Kahn's algorithm
        Vector<String> orderedNodesID = getOrderNodes();
        if(orderedNodesID == null || this.nodes.size() != orderedNodesID.size()) throw new NotADAGException();
        Vector<Node> orderedNode = new Vector<>();
        for(String s : orderedNodesID) orderedNode.add(this.getNode(s));
        this.nodes = orderedNode;
        return this.nodes;
    }

    public boolean isDAG(){
        /* If the graph is a DAG, a solution will be contained in the list L (the solution is not necessarily unique).
         Otherwise, the graph must have at least one cycle and therefore a topological sorting is impossible. */
        return getOrderNodes() != null;
    }

    // --------ISVALID handlers-----
    /* This two function is a sort of signature function table. They have two parameters and compute the output shape,
     * if the inputs are corrects..
     */
    private Shape mulShape(Shape s1, Shape s2){ return (s1.c == s2.r) ? new Shape(s1.r, s2.c) : null; }
    private Shape sumShape(Shape s1, Shape s2) { return (s1.equals(s2)) ? s1 : null; }

    Shape computingShape(Input in) throws ShapeCompatibilityException{
        // Compute shape recursively.
        // return null on failure
        if(in.getType()==1) return in.shape;
        Node n = getNode(in.id);
        if(n.isInitial()) return n.getShape();
        if(n.getShape() != null) return n.getShape(); //The shape was already computed
        Vector<Input> inputs = n.getInputs();
        Shape res = computingShape(inputs.get(0)); // If inputs == null, n.isInitial() == true
        switch (n.getOp()){
            case "sum":
                for(int i = 1; i<inputs.size(); i++){
                    Input input = inputs.get(i);
                    if((res = sumShape(res, computingShape(input)))==null) throw new ShapeCompatibilityException();
                }
                n.setShape(res); //Equal to components
                break;
            case "mult":
                for(int i = 1; i<inputs.size(); i++)
                    if((res = mulShape(res, computingShape(inputs.get(i)))) == null) throw new ShapeCompatibilityException();
                n.setShape(res);
                break;
        }
        return n.getShape();
    }

    public boolean isValid(){
        /* Compute the shape compatibility of inputs and the correct node input id*/
        for(Node n : nodes)
            if(!n.isInitial())
                for(Input input : n.getInputs())
                    if(input.getType() == 0 && getNode(input.id) == null) {
                        System.out.println("Node " + input.id + " not exists");
                        return false;
                    }
        try{
            for(Node n: sink()) computingShape(new Input(n.getId()));
        }catch (ShapeCompatibilityException e){
            System.out.println("Invalid shape");
            return false;
        }
        return true;
    }


    public Node getNode(String id){
        for(Node n : nodes) if(n.getId().equals(id)) return n;
        return null;
    }

    public void createEdge(){
        /* Compute the edges given the nodes and their inputs */
        for(Node n : nodes){
            if(!n.isInitial()) for(Input in : n.getInputs()) if(in.getType() == 0) edges.add(new Edge(in.id, n.getId()));
        }
    }

    public Vector<Node> sink(){
        //return all nodes that has no outcoming edges
        Vector<Node> nodes = (Vector<Node>) this.nodes.clone();
        for(Node no : this.nodes){
            if(!no.isInitial()) for(Input in : no.getInputs()) nodes.remove(getNode(in.id));
        }
        return nodes.size() == 0 ? null : nodes;
    }

    private int countEdge(String start, String end){
        int res = 0;
        for(Edge e : this.edges){
            if(start != null && end != null && e.end.equals(end) && e.start.equals(start)) res++;
            if(start == null && end != null && e.end.equals(end)) res++;
            if(end == null && start != null && e.start.equals(start)) res++;
            if(start == null && end == null) res++;
        }
        return res;
    }
    private void removeEdge(String start, String end){
        Vector<Edge> toRemove = new Vector<>();
        for(Edge e:this.edges) if(e.start.equals(start) && e.end.equals(end)) toRemove.add(e);
        this.edges.removeAll(toRemove);
    }

    private void optimize(Node n) {
        if (n.isOptimized()) return;
        Vector<Input> toAdd = new Vector<>();
        Vector<Input> merged = new Vector<>();
        for (Input in : n.getInputs()) {
            if (in.getType() == 0) {
                optimize(this.getNode(in.id));
                if (countEdge(in.id, null) == 1 // if il risultato non serve ad altri nodi
                        && !this.getNode(in.id).isInitial() // Se non è un nodo di input
                        && this.getNode(in.id).getOp().equals("sum") && n.getOp().equals("sum")) { // Sono entrambe somme
                    for (Edge e : this.edges) { // Aggiorno archi
                        if (e.end.equals(in.id))
                            e.end = n.getId();
                    }
                    toAdd = this.getNode(in.id).getInputs(); //Prendo input del nodo unificato
                    merged.add(in);
                }
            }
        }
        /* Modifico grafo dato il merge appena effettuato */
        n.addInputs(toAdd);
        n.getInputs().removeAll(merged);
        for(Input in : merged) {
            this.nodes.remove(this.getNode(in.id));
            removeEdge(in.id, n.getId());
        }
        n.setOptimized(); // Imposto il nodo come ottimizzato
    }

    public void optimizedGraph(){
        /* Richiama la funzione di ottimizzazione su ogni nodo finale*/
        for(Node n : this.getInitial()){
            n.setOptimized();
        }
        Vector<Node> sink = this.sink();
        for(int i = 0; i<sink.size(); i++){
            optimize(sink.get(i));
        }
    }

}
