package kth.csc.inda;
import java.util.Random;
/**
 * This class can generate random Graphs
 * @author Marcus
 */
public class GraphGenerator {
    private Random r = new Random();
    
    /**
     * This method will generate a random HashGraph with n nodes.
     * @param n number of nodes in graph
     * @return A new random HashGraph with n nodes.
     * @throws IllegalArgumentException
     *             if v is out of range
     */
    public Graph[] randomGraph(int n)throws IllegalArgumentException{
        checkVertexParameter(n);
        HashGraph graph1 = new HashGraph(n);
        MatrixGraph graph2 = new MatrixGraph(n);
        int numOfEdges = 0;
        while(numOfEdges<n){
            int node1 = r.nextInt(n);
            int node2 = r.nextInt(n);
            if(!graph1.hasEdge(node1, node2)){
                graph1.addBi(node1, node2);
                graph2.addBi(node1, node2);
                numOfEdges++;
            }
        }
        return new Graph[]{graph1, graph2};
    }
    
    
    /**
	 * Checks a single vertex parameter v.
	 * 
	 * @throws IllegalArgumentException
	 *             if v is out of range
	 */
	private void checkVertexParameter(int v) {
		if (v < 0)
			throw new IllegalArgumentException("Out of range: v = " + v + ".");
	}
}
