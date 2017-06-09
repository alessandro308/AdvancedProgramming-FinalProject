package compiler;

import graph.Graph;
import graph.Input;
import graph.Node;


import java.io.*;
import java.util.Vector;

public class Generator
{
    OutputStream file;

    public Generator(OutputStream out){
        file = out;
    }
    public Generator(String fileName){
        try {
            file = new FileOutputStream(new File(fileName));
        } catch (FileNotFoundException e) {
            System.out.println("Can't create a file");
        }
    }
    public Generator(){
        file = System.out;
    }

    public void generateCode(Graph g) throws IOException {
        Vector<String> vars = new Vector<>();
        StringBuilder toWrite = new StringBuilder();
        for(Node n : g.getInitial()){
            for(int i = 0; i<n.getShape().size(); i++){
                vars.add(n.getId()+i);
            }
            toWrite.append("\tdouble[][] ").append(n.getId()).append(" = {");
            for(int r = 0; r<n.getShape().r; r++){
                toWrite.append("{");
                for(int c = 0; c<n.getShape().c-1; c++){
                    toWrite.append(n.getId()+(r*n.getShape().c + c)+", ");
                }
                toWrite.append(n.getId()+(r*n.getShape().c + (n.getShape().c-1)));
                toWrite.append("}");
                if(r != n.getShape().r -1)
                    toWrite.append(',');
            }
            toWrite.append("};\n");
        }
        this.file.write("Vector<Double> computeGraph(".getBytes());
        for(int i = 0; i<vars.size()-1; i++){
            this.file.write((vars.get(i)+", ").getBytes());
        }
        this.file.write((vars.get(vars.size()-1)+"){\n").getBytes());
        this.file.write(toWrite.toString().getBytes());
        Vector<Node> notInitial = g.getNodes();
        notInitial.removeAll(g.getInitial());
        
    }

}
