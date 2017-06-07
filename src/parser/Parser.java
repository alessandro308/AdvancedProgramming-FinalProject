package parser;

import graph.Graph;
import parser.exception.ParseFailed;

import java.util.StringTokenizer;

/**
 * Implement a recursive descent parser, which takes a JSON representation of a graph and builds the corresponding graph.
 */
public class Parser {

    private String toParse;
    private StringTokenizer input;
    /*
        Inizialize the Parser with a string to parse
     */
    public Parser(String json){
        this.toParse = json;
        input = new StringTokenizer(this.toParse);
    }

    public Graph getGraph() throws ParseFailed{
        Graph g = new Graph();
        return g;
    }


}
