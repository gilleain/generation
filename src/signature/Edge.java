package signature;

/**
 * Used in the signature at various points to avoid duplications, such as 
 * when printing the signature, or constructing the DAG. 
 * 
 * @author maclean
 *
 */
public class Edge {
    public int a;
    public int b;

    public Edge(int a, int b) {
        this.a = a;
        this.b = b;
    }

    public boolean equals(Object other) {
        if (other instanceof Edge) {
            Edge o = (Edge) other;
            return (this.a == o.a && this.b == o.b)
                    || (this.a == o.b && this.b == o.a);
        } else {
            return false;
        }
    }

    public String toString() {
        return String.format("%s-%s", a, b);
    }
}
