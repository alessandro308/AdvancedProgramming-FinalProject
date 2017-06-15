package compiler;

import compiler.exception.NotADAGException;
import graph.*;
import java.io.*;
import java.util.Vector;

/**
 * Created by alessandro on 13/06/17.
 */
public class GeneratorT {
    OutputStream file;

    public GeneratorT(OutputStream out){
        file = out;
    }
    public GeneratorT(String fileName) throws FileNotFoundException{
        file = new FileOutputStream(new File(fileName)); }
    public GeneratorT(){
        file = System.out;
    }

    public String sumString(String output, Graph g, Vector<Input> vars){
        /* Crea le operazioni per effettuare la somma dato il nome dell'output e il vettore degli input */
        StringBuilder tmp = new StringBuilder();
        if(vars.get(0).getShape() == null) vars.get(0).setShape(g.getNode(vars.get(0).id).getShape());
        if(vars.get(1).getShape() == null) vars.get(1).setShape(g.getNode(vars.get(1).id).getShape());
        for(int i = 0; i<vars.get(0).getShape().c; i++)
            for(int j = 0; j<vars.get(0).getShape().r; j++){
                tmp.append("\t"+output+"["+i+"]["+j+"]=");
                for(int n = 0; n<vars.size(); n++) {
                    tmp.append(vars.get(n).getType() == 0 ? vars.get(n).id + "[" + i + "][" + j + "]" : vars.get(n).matrix().get(i).get(j));
                    if(n < vars.size() -1) tmp.append("+");
                }
                tmp.append(";\n");
            }
        return tmp.toString();
    }


    public String mulString(String output, Graph g, Vector<Input> vars){
        /* Crea le operazioni per effettuare la mult dato il nome dell'output e il vettore degli input
        * La moltiplicazione, data la complessità, può essere effettuata solo due input alla volta.
        * Gli input in eccesso saranno ignorati.
        * */
        StringBuilder tmp = new StringBuilder();
        if(vars.get(0).getShape() == null) vars.get(0).setShape(g.getNode(vars.get(0).id).getShape());
        if(vars.get(1).getShape() == null) vars.get(1).setShape(g.getNode(vars.get(1).id).getShape());
        for(int i = 0; i<vars.get(0).getShape().r; i++){
            for(int j = 0; j<vars.get(1).getShape().c; j++){
                tmp.append("\t"+output+"["+i+"]["+j+"]=");
                for(int n = 0; n<vars.get(0).getShape().c; n++){
                    tmp.append(vars.get(0).getType() == 0 ? vars.get(0).id+"["+i+"]["+n+"]*":vars.get(0).matrix().get(i).get(n)+"*");
                    tmp.append(vars.get(1).getType() == 0 ? vars.get(1).id+"["+n+"]["+j+"]":vars.get(1).matrix().get(n).get(j));
                    tmp.append(n<vars.get(0).getShape().c - 1 ? "+" :";\n");
                }
            }
        }
        return tmp.toString();
    }

    public void generateCode(Graph g) throws IOException, NotADAGException {
        g.orderNodes();
        Vector<String> vars = new Vector<>();
        StringBuilder toWrite = new StringBuilder();

        for (Node n : g.getInitial()) {
            for (int i = 0; i < n.getShape().size(); i++) {
                vars.add(n.getId() + i);
            }
            toWrite.append("\tdouble[][] ").append(n.getId()).append(" = {");
            for (int r = 0; r < n.getShape().r; r++) {
                toWrite.append("{");
                for (int c = 0; c < n.getShape().c - 1; c++) {
                    toWrite.append(n.getId() + (r * n.getShape().c + c) + ", ");
                }
                toWrite.append(n.getId() + (r * n.getShape().c + (n.getShape().c - 1)));
                toWrite.append("}");
                if (r != n.getShape().r - 1)
                    toWrite.append(',');
            }
            toWrite.append("};\n");
        }
        this.file.write("Vector<double[][]> computeGraph(".getBytes());
        for(int i = 0; i<vars.size()-1; i++){
            this.file.write(("double "+vars.get(i)+", ").getBytes());
        }
        this.file.write(("double "+vars.get(vars.size()-1)+"){\n\tVector<double[][]> res = new Vector<>();\n\n").getBytes());
        Vector<Node> notInitial = g.getNodes();
        notInitial.removeAll(g.getInitial());
        for(Node n : notInitial){
            toWrite.append("\tdouble[][] ").append(n.getId()).append(" = new double["+n.getShape().r+"]["+n.getShape().c+"];\n");
            switch (n.getOp()){
                case "sum":
                    toWrite.append(sumString(n.getId(), g, n.getInputs()));
                    break;
                case "mult":
                    toWrite.append(mulString(n.getId(), g, n.getInputs()));
            }
            toWrite.append("\tres.add("+n.getId()+");\n\n");
        }
        toWrite.append("\treturn res;\n}");
        file.write(toWrite.toString().getBytes());

    }
}
