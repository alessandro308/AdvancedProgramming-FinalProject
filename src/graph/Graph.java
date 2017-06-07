package graph;

import graph.exception.NodeAlreadyExistsException;

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
    private Vector<Node> nodes;
    void addNode(Node n) throws NodeAlreadyExistsException{
        nodes.forEach(e -> {
            if (e.getId().equals(n.getId()))
                throw new NodeAlreadyExistsException("Node with id " + n.getId() + " already exists");
        });
        nodes.add(n);
    }
}
