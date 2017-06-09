package graph;

/**
 * Created by alessandro on 08/06/17.
 */
public class Shape {
    public int r;
    public int c;
    public Shape(){};
    public Shape(int r, int c){
        this.r = r;
        this.c = c;
    }

    public boolean equals(Object other){
            return (other instanceof Shape) &&
                    ((Shape) other).r == this.r &&
                    ((Shape) other).c == this.c;
    }

    public int size(){
        return r*c;
    }

}
