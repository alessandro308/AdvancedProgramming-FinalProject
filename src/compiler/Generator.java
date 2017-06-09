package compiler;
import compiler.exception.NotADAGException;
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

    void print(double[][] x){
        for(int i = 0; i<x.length; i++){
            for(int j = 0; j<x[0].length; j++){
                System.out.print(x[i][j]+ " ");
            }
            System.out.println("");
        }
    }

    public void generateCode(Graph g) throws IOException, NotADAGException {
        g.orderNodes();
        Vector<String> vars = new Vector<>();
        StringBuilder toWrite = new StringBuilder();
        String sumFunction = "double[][] sum(double[][]... x){\n" +
                "      if(x.length == 0)\n" +
                "            return new double[0][0];\n" +
                "        double[][] res = new double[x[0].length][x[0][0].length];\n" +
                "        for(int i = 0; i<x.length; i++) {\n" +
                "            for (int r = 0; r < x[i].length; r++)\n" +
                "                for (int c = 0; c < x[i][r].length; c++) {\n" +
                "                    res[r][c] += x[i][r][c];\n" +
                "                }\n" +
                "        }\n" +
                "        return res;\n" +
                "    }\n\n";
        String mulFunction = "double vectorMul(double[] row, double[] col){\n" +
                "        double res = 0;\n" +
                "        for(int i = 0; i<row.length; i++)\n" +
                "            res += row[i]*col[i];\n" +
                "        return res;\n" +
                "}\n" +
                "double[] getColumn(double[][] matrix, int index){\n" +
                "        double[] vector = new double[matrix.length];\n" +
                "        for(int i = 0; i<matrix.length; i++){\n" +
                "            vector[i]=matrix[i][index];\n" +
                "        }\n" +
                "        return vector;\n" +
                "}\n" +
                "double[][] matmatMul(double[][] m1, double[][] m2){\n" +
                "        double[][] res = new double[m1.length][m2[0].length];\n" +
                "        for(int i = 0; i<res.length; i++){\n" +
                "            for(int j = 0; j<res[0].length; j++){\n" +
                "                res[i][j] = vectorMul(m1[i], getColumn(m2, j));\n" +
                "            }\n" +
                "        }\n" +
                "        return res;\n" +
                "}\n" +
                "double[][] mult(double[][]... x){\n" +
                "        if(x.length == 0)\n" +
                "            return new double[0][0];\n" +
                "        double[][] res = x[0].clone();\n" +
                "        for(int i = 1; i<x.length; i++){\n" +
                "            res = matmatMul(res, x[i]);\n" +
                "        }\n" +
                "        return res;\n" +
                "}\n";
        String classHeader = "import java.util.Vector;\npublic class MainClass{\n";
        //String mainHeader = "public static void main(String[] args){\n";
        file.write(classHeader.getBytes());
        file.write(sumFunction.getBytes());
        file.write(mulFunction.getBytes());
        //file.write(mainHeader.getBytes());

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
        this.file.write("Vector<double[][]> computeGraph(".getBytes());
        for(int i = 0; i<vars.size()-1; i++){
            this.file.write(("double "+vars.get(i)+", ").getBytes());
        }
        this.file.write(("double "+vars.get(vars.size()-1)+"){\n").getBytes());
        this.file.write(toWrite.toString().getBytes());
        toWrite = new StringBuilder();
        Vector<Node> notInitial = g.getNodes();
        notInitial.removeAll(g.getInitial());
        for(Node n : notInitial){
            toWrite.append("\tdouble[][] ").append(n.getId()).append(" = ");
            toWrite.append(n.getOp()+"( ");
            for(int t = 0; t<n.getInputs().size(); t++){
                Input input = n.getInputs().get(t);
                if(input.getType() == 0)
                    toWrite.append(input.id);
                else{
                    Vector<Vector<Double>> matrix = input.matrix();
                    toWrite.append('{');
                    for(int i = 0; i<matrix.size(); i++){
                        toWrite.append('{');
                        for(int j = 0; j<matrix.get(i).size(); j++){
                            toWrite.append(matrix.get(i).get(j)+" ");
                            if(j<matrix.get(0).size()-1)
                                toWrite.append(", ");
                        }
                        toWrite.append('}');
                        if(i<matrix.size()-1)
                            toWrite.append(", ");
                    }
                    toWrite.append('}');
                }
                if(t < n.getInputs().size()-1) toWrite.append(", ");

            }
            toWrite.append(");\n");
        }
        toWrite.append("\tVector<double[][]>  res = new Vector<>();\n");
        for(Node n : g.sink()){
            toWrite.append("\tres.add("+n.getId()+");\n");
        }
        toWrite.append("\treturn res;\n}}");
        this.file.write(toWrite.toString().getBytes());
    }
}
