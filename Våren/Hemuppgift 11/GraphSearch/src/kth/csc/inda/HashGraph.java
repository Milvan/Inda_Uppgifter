package kth.csc.inda;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import static kth.csc.inda.Graph.NO_COST;

/**
 * A graph with a fixed number of vertices implemented using adjacency maps.
 * Space complexity is &Theta;(n + m) where n is the number of vertices and m
 * the number of edges.
 * 
 * @author [Name]
 * @version [Date]
 */
public class HashGraph implements Graph {
	/**
	 * The map edges[v] contains the key-value pair (w, c) if there is an edge
	 * from v to w; c is the cost assigned to this edge. The maps may be null
	 * and are allocated only when needed.
	 */
	private final Map<Integer, Integer>[] edges;
	private final static int INITIAL_MAP_SIZE = 4;

	/** Number of edges in the graph. */
	private int numEdges;

	/**
	 * Constructs a HashGraph with n vertices and no edges. Time complexity:
	 * O(n)
	 * 
	 * @throws IllegalArgumentException
	 *             if n < 0
	 */
	public HashGraph(int n) {
		if (n < 0)
			throw new IllegalArgumentException("n = " + n);

		// The array will contain only Map<Integer, Integer> instances created
		// in addEdge(). This is sufficient to ensure type safety.
		@SuppressWarnings("unchecked")
		Map<Integer, Integer>[] a = new HashMap[n];
		edges = a;
	}

	/**
	 * Add an edge without checking parameters.
	 */
	private void addEdge(int from, int to, int cost) {
		if (edges[from] == null)
			edges[from] = new HashMap<Integer, Integer>(INITIAL_MAP_SIZE);
		if (edges[from].put(to, cost) == null)
			numEdges++;
	}
        

	/**
	 * {@inheritDoc Graph} Time complexity: O(1).
	 */
	@Override
	public int numVertices() {
		return edges.length;
	}

	/**
	 * {@inheritDoc Graph} Time complexity: O(1).
	 */
	@Override
	public int numEdges() {
		return numEdges;
	}

	/**
	 * {@inheritDoc Graph}
	 */
	@Override
	public int degree(int v) throws IllegalArgumentException {
		checkVertexParameter(v);
                if(edges[v]==null){
                    return 0;
                } else {
                    return edges[v].size();
                }
	}

        
        /**
	 * {@inheritDoc Graph} 
	 */
	@Override
	public VertexIterator neighbors(int v) throws IllegalArgumentException {
		checkVertexParameter(v);

		return new NeighborIterator(v);
	}

	private class NeighborIterator implements VertexIterator {
                Map<Integer, Integer> adj;
		
                Iterator<Integer> it;
                
               NeighborIterator(int v){
                   adj = edges[v];
		   if (adj !=null){
                       it = adj.keySet().iterator();
                   } else {
                       it = new HashSet<Integer>().iterator();
                   }
                   
               }
               
               /**
                * {@inheritDoc VertexIterator} 
                */
               @Override
               public boolean hasNext(){
                   return it.hasNext();
               }
               
               /**
                * {@inheritDoc VertexIterator}
                */
                @Override
                public int next() throws NoSuchElementException {
                    return it.next();
               }
        }

	/**
	 * {@inheritDoc Graph}
	 */
	@Override
	public boolean hasEdge(int v, int w) {
		checkVertexParameters(v,w);
                if(edges[v]==null){
                    return false;
                } else {   
                    return edges[v].containsKey(w);
                }
	}

	/**
	 * {@inheritDoc Graph}
	 */
	@Override
	public int cost(int v, int w) throws IllegalArgumentException {
		if (hasEdge(v,w)){
                    return edges[v].get(w);
                }
		return NO_COST;
	}

	/**
	 * {@inheritDoc Graph}
	 */
	@Override
	public void add(int from, int to) {
                checkVertexParameters(from, to);
		addEdge(from, to, NO_COST);
	}

	/**
	 * {@inheritDoc Graph}
	 */
	@Override
	public void add(int from, int to, int c) {
                checkVertexParameters(from, to);
                checkNonNegativeCost(c);
		addEdge(from, to, c);
	}

	/**
	 * {@inheritDoc Graph}
	 */
	@Override
	public void addBi(int v, int w) {
		add(v, w);
                add(w, v);
	}

	/**
	 * {@inheritDoc Graph}
	 */
	@Override
	public void addBi(int v, int w, int c) {
		add(v, w, c);
                add(w, v, c);
	}

	/**
	 * {@inheritDoc Graph}
	 */
	@Override
	public void remove(int from, int to) {
		checkVertexParameters(from, to);
                if (edges[from].remove(to) != null)
                    numEdges--;
	}

	/**
	 * {@inheritDoc Graph}
	 */
	@Override
	public void removeBi(int v, int w) {
		checkVertexParameters(v, w);
                edges[v].remove(w);
                edges[w].remove(v);
                
	}
        
        /**
	 * Checks two vertex parameters v and w.
	 * 
	 * @throws IllegalArgumentException
	 *             if v or w is out of range
	 */
	private void checkVertexParameters(int v, int w) {
		if (v < 0 || v >= edges.length || w < 0 || w >= edges.length)
			throw new IllegalArgumentException("Out of range: v = " + v
					+ ", w = " + w + ".");
	}
        
        /**
	 * Checks a single vertex parameter v.
	 * 
	 * @throws IllegalArgumentException
	 *             if v is out of range
	 */
	private void checkVertexParameter(int v) {
		if (v < 0 || v >= edges.length)
			throw new IllegalArgumentException("Out of range: v = " + v + ".");
	}
        
        /**
	 * Checks that the cost c is non-negative.
	 * 
	 * @throws IllegalArgumentException
	 *             if c < 0
	 */
	private void checkNonNegativeCost(int c) {
		if (c < 0)
			throw new IllegalArgumentException("Illegal cost: c = " + c + ".");
	}

	/**
	 * Returns a string representation of this graph.
	 * 
	 * @return a String representation of this graph
	 */
	@Override
	public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("{");
		for (int i=0; i<edges.length;i++) {
                    Map<Integer, Integer> m = edges[i];
                    if (m!=null){
                        for (Integer j : m.keySet()){
                           int x = m.get(j);
                           switch (x) {
                                    case NO_COST:
                                        sb.append("(" + i + "," + j + "), ");
                                        break;
                                    default:
					sb.append("(" + i + "," + j + "," + x + "), ");
				}
                        }
                    }
                
                }
                int length = sb.length();
                if (length>2){
                    sb.delete(length-2, length);
                }
                sb.append("}");
		return sb.toString();
	}
}