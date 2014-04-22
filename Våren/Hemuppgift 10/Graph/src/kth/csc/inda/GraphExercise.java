package kth.csc.inda;
import java.util.Random;
/**
 * This program creates a random graph and performs a calculation of the number of componenets and the biggest component using 
 * an example implementation of depth first search.
 * 
 * @author Marcus Larsson
 * @version 2014-04-12
 */
public class GraphExercise {
	private int[] componentsSize;
        private Graph hashGraph, matrixGraph;
        private Stopwatch clock;
        private Counter counter = new Counter();
    
      /**
	 * 
	 * Will start the program.
	 * @param args
	 *            not used
	 */
	public static void main(String[] args) {
            GraphExercise GE = new GraphExercise();    		
            GE.run();
	}
        
        /**
         * Sets up all the variables and generates random graphs.
         */
        public GraphExercise(){
            clock = new Stopwatch();
            Random r = new Random();
            int size = r.nextInt(5000)+1;
            componentsSize = new int[size];
            GraphGenerator gen = new GraphGenerator();
            Graph[] graphs = gen.randomGraph(size);
            hashGraph = graphs[0]; // index 0 contains hashGraph
            matrixGraph = graphs[1]; // index 1 contains matrixgraph
        }
        
        /**
         * Runs the calculation test.
         * First it will print out the graph. Then do the calculations on the graph represented by a HashGraph and then represented by a MatrixGraph.
         * In the end print the results.
         */
        public void run(){
            System.out.printf("The graph: %s%n", hashGraph);
            System.out.printf("%n%s%d%n","Size of graph: ", hashGraph.numVertices());
            
            clock.reset().start();
            int num1 = calculateComponents(hashGraph);
            long time = clock.stop().nanoseconds();
            System.out.printf("%n%s: %d %s%n","Time as HashGraph", time, "nanoseconds");
            
            clock.reset().start();
            int num2 = calculateComponents(matrixGraph);
            time = clock.stop().nanoseconds();
            System.out.printf("%n%s: %d %s%n","Time as MatrixGraph", time, "nanoseconds");
            
            System.out.printf("%n%s: %d%n" ,"Number of components", num1);
            System.out.printf("%n%s: %d%n", "Biggest component", max(componentsSize));
            
        }
        
        /**
         * Calculates the max value in the given array.
         * @param v The array to check
         * @return  The maximum value in the array.
         */
        private int max(int[] v){
            int max = v[0];
            for (int i:v){
                max = Math.max(i, max);
            }
            return max;
        }
        
        /**
         * This method will print all values in the given array to stdout. One value on each row.
         * @param v The array to print.
         */
        private void printList(int[] v){
            for (int i : v){
                System.out.println(i);
            }
        }   

        /**
         * This method will calculate the size of every component and enter it in the componentsSize list.
         * Also it will calculate the number of components and return that value.
         * @param g the graph to calculate.
         * @return The number of components in the graph.
         */
        private int calculateComponents(Graph g){
            int numOfComponents = 0;
            
            VertexAction countVertex = new VertexAction() {
			@Override
			public void act(Graph g, int v) {
                                counter.count();
			}
		};
		int n = g.numVertices();
		boolean[] visited = new boolean[n];
		for (int v = 0; v < n; v++) {
                        counter.resetCounter();
			if (!visited[v]) {
				dfs(g, v, visited, countVertex);
                                numOfComponents++;
                                componentsSize[v]=counter.getValue();
			}
		}
                return numOfComponents;
        }
	
	/**
	 * Traverses the nodes of g that have not yet been visited. The nodes are
	 * visited in depth-first order starting at v. The act() method in the
	 * VertexAction object is called once for each node.
	 * 
	 * @param g
	 *            an undirected graph
	 * @param v
	 *            start vertex
	 * @param visited
	 *            visited[i] is true if node i has been visited
	 */
	private void dfs(Graph g, int v, boolean[] visited,
			VertexAction action) {
		if (visited[v])
			return;
		visited[v] = true;
		action.act(g, v);
		for (VertexIterator it = g.neighbors(v); it.hasNext();)
			dfs(g, it.next(), visited, action);
	}
}
