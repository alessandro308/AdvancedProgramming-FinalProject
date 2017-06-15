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
        expect('[');
        expect(StreamTokenizer.TT_NUMBER);
        int r = (int) input.nval;
        expect(',');
        expect(StreamTokenizer.TT_NUMBER);
        int c = (int) input.nval;
        expect(']');
        return new Shape(r, c);
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
        /* Legge il campo input del JSON */
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
        /* Parsa la struttura nodo del JSON */
        String id = null;
        int type = -1;
        Vector<Input> in = new Vector<>();
        String op = null;
        Shape shape = null;
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
        /*
            I nodi con la moltiplicazione, seppur correttamente parsati, nel caso avessero più di 2 input,
            quelli in eccesso non saranno considerati nel generatore. Questo perchè diventava complesso fare più
            moltiplicazioni di matrici in un single nested loop.
         */
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

    public static void main(String args[]) throws Exception {
        String mg1 = "{ \"a\": {\"type\": \"input\", \"shape\": [1,1]},\n\"b\": {\"type\": \"input\", \"shape\": [1,1]},\n\"c\": {\"type\": \"comp\", \"op\": \"sum\", \"in\": [\"a\", \"b\"]}, \"d\": {\"type\":\n\"comp\", \"op\": \"sum\", \"in\": [\"b\", \n[[\n1]]]\n}, \"e\": {\"type\": \"comp\", \"op\": \"mult\", \"in\": \n[\"c\", \"d\"]}\n}";
        String mg = "{\n" +
                "\"a\": {\"type\": \"input\", \"shape\": [1,1]},\n" +
                "\"b\": {\"type\": \"input\", \"shape\": [1,1]},\n" +
                "\"c\": {\"type\": \"comp\", \"op\": \"sum\", \"in\": [\"a\", \"b\"]}, \n" +
                "\"d\": {\"type\": \"comp\", \"op\": \"sum\", \"in\": [\"b\", [[1]]]}, \n" +
                "\"e\": {\"type\": \"comp\", \"op\": \"mult\", \"in\": [\"c\", \"d\"]}, \n" +
                "\"f\": {\"type\": \"input\", \"shape\": [2, 2]},\n"+
                "\"g\": {\"type\": \"input\", \"shape\": [2, 2]},\n"+
                "\"x1\": {\"type\": \"input\", \"shape\": [2, 2]},\n"+
                "\"h\": {\"type\": \"comp\", \"op\": \"sum\", \"in\": [\"f\", \"g\", \"x1\"]}, \n" +
                "\"y\": {\"type\": \"comp\", \"op\": \"mult\", \"in\": [\"f\", \"g\"]}, \n" +
                "\"t\": {\"type\": \"comp\", \"op\": \"sum\", \"in\": [\"h\", [[2,3],[2,3]]]} \n" +
                "}";
        System.out.println(mg);
        Parser p = new Parser(mg);
        //p.printToken();
        p.parseGraph();
        System.out.println(p.g.isDAG());
        System.out.println(p.g.isValid());
        p.g.optimizedGraph();
        GeneratorT print = new GeneratorT();
        print.generateCode(p.g);
    }

}
