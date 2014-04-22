import java.io.*;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.StringTokenizer;
import kth.csc.inda.*;
import java.util.PriorityQueue;

/**
     * Searches the input file (args[0]) that contains a description of a graph
     * in the following way.
     * first row is the size of the graph given with a positive number n.
     * following rows are given:
     * v w c1
     * w v c2
     * where v and w are the index of 2 different vertices and c1 and c2 are 
     * constant positive numbers that stands for the distance between the vertices.
     * 
     * Searching for a path from args[1] to args[2]. Then prints out the path 
     * with minimum number of vertices on the way to stdout.
     * Prints an empty row if there is no path.
     * Leaves program with System.exit(n), where n is 0 if successful.
 *
 * @author Marcus Larsson
 * @version 2014-04-17
 */
public final class GraphSearch {
    private final static String NAME = GraphSearch.class.getName();
    private static SpecialBufferReader sbr;

    private GraphSearch() {}

    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.printf("Usage: java %s FILE FROM TO%n", GraphSearch.class.getSimpleName());
            System.exit(1); // Unix error handling
        }
        final String fileName = args[0];
        final String from = args[1];
        final String to = args[2];
        
        int errCode = 0; // Unix error handling
        
        try (
            // FileReader uses "the default character encoding".
            //file = new BufferedReader(new FileReader(fileName));

             // To specify an encoding, use this code instead:
             BufferedReader file = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8")); 
             ) {
            
            sbr = new SpecialBufferReader(file);
            Graph graph = getGraph(sbr);
            
            int v = Integer.parseInt(from);
            int w = Integer.parseInt(to);
            
            String pathWay1 = getShortestPath(graph, v, w);
            String pathWay2 = getEasiestPath(graph, v, w);
            System.out.println(pathWay1);
            System.out.println();
            System.out.println(pathWay2);
            
        } catch (IOException e) {
            System.err.printf("%s: %s%n", NAME, e);
            errCode = 1;
        }
        catch(NumberFormatException e){
            System.err.printf("Usage: java %s FILE FROM TO%n", GraphSearch.class.getSimpleName());
            System.err.println("Where FROM and TO are Integer index of vertices");
            errCode = 1; // Unix error handling
        } finally {
            System.exit(errCode);
        }
    }
    
    /**
     * Creates a graph object from the file description in.
     * @param in a file that describes a graph
     * @return The graph described in the file in.
     * @throws IOException if it fails to read from in.
     * @throws IllegalArgumentException if size in the file is <0
     */
    private static Graph getGraph(SpecialBufferReader in)throws IllegalArgumentException, IOException{
        int size = getSize(in);
        
        HashGraph graph;
        try{
            graph = new HashGraph(size);
            fillGraph(graph, in);
            return graph;
        } catch(IllegalArgumentException e){
            throw new IllegalArgumentException("Size: "+ size +"Size must be positive.");
        }
        
    }
    
    /**
     * Fills the given graph with the information given i the SpecialBufferReader.
     * @param graph The graph to fill
     * @param in The SpecialBufferReader that reads the information that describes the graph.
     * @throws IOException If failing to read file.
     * @throws IllegalArgumentException If arguments in SpecialBufferReader is incorrect.
     */
    private static void fillGraph(Graph graph, SpecialBufferReader in)throws IOException{
        String token;
        while((token = in.getNextToken())!=null){
            try{
                int v = Integer.parseInt(token);
                int w = Integer.parseInt(in.getNextToken());
                int c = Integer.parseInt(in.getNextToken());
                graph.add(v, w, c); //TODO: catch if v or w is out of range
            } catch(NumberFormatException e){
                throw new IllegalArgumentException("Invalid arguments in file, format must be: v w c  on every row where all "
                        + "are Ingeter values");
            } catch (IllegalArgumentException e){
                throw new IllegalArgumentException("Trying to add edges to non-existing vertices");
            }
        }
    }
    
    /**
     * Will read the SpecialBufferReader and find the first token that should be the size.
     * @param in The SpecialBufferReader that handles the file that describes the graph
     * @return The first token in SpecialBufferReader, should describe the size of graph.
     * @throws IOException If fails to read file.
     */
    private static int getSize(SpecialBufferReader in)throws IOException{
        int size=0;
        try{
           String token = in.getNextToken(); // throws IOException
           size = Integer.parseInt(token); 
        } catch(NumberFormatException e){
            throw new IllegalArgumentException("\"Invalid arguments in file, format must be: v w c  on every row where all "
             + "are Ingeter values");
        } 
        return size;
        
        
    }
    
   
    
    /**
     * Searching through graph using BFS and finds the easiest path from one vertex to another.
     * That is, the path with the minimum number of vertices.
     * Returns a string description of the easiest path.
     * @param graph The graph to search
     * @param from The starting point vertex
     * @param to The end point vertex
     * @return A string description of the shortest path from "from" to "to".
     */
    private static String getEasiestPath(Graph graph, int from, final int to){
        StringBuilder sb = new StringBuilder();
        boolean[] visited = new boolean[graph.numVertices()];
        LinkedList<Integer> queue = new LinkedList<>();
        int[] prev = undefinedList(graph.numVertices());
        visited[from] = true;
        queue.addLast(from);
        
        while(!queue.isEmpty()){
            int curr = queue.poll();
            VertexIterator it = graph.neighbors(curr);
            
            while(it.hasNext()){
                int next = it.next();
                
                if (!visited[next]){
                    visited[next]=true;
                    prev[next]=curr;
                    if(next==to){
                        sb.append("Easiest path is: \n");
                        sb.append(getPath(prev, to));
                        return sb.toString(); //returns as soon as it found a path.
                    }
                    queue.addLast(next);
                }      
            }  
        }
        return ""; //returns empty line if there is no path.
    }
    
     /**
     * Creates an array of integers, all index filled with value -1 which should be treated as undefined.
     * @param n The size of the array
     * @return An array of Integers of size n and all values are -1
     */
    private static int[] undefinedList(int n){
        int[] dist = new int[n];
        for(int i=0; i<n;i++){
            dist[i]=-1;
        }
        return dist;
    }
    
    //TODO: return empty line if the distance is infinity (-1). Since there is no path then.
    /**
     * Searching through graph and finds the shortest path from one vertex to another.
     * Returns a string description of the shortest path.
     * @param graph The graph to search
     * @param from The starting point vertex
     * @param to The end point vertex
     * @return A string description of the shortest path from "from" to "to".
     */
    private static String getShortestPath(Graph graph, int from, int to){
        StringBuilder sb= new StringBuilder();
        int[][] d = dijktras(graph, from);
        int distance = d[0][to];
        int[] prev = d[1];
        sb.append("The shortest distance is: ");
        sb.append(distance);
        sb.append("\n");
        sb.append("Path is: \n");
        sb.append(getPath(prev, to));
        
        return sb.toString();
    }
    
    //TODO: Fix so that dijktras runs with a priorityqueue for better timecomplexity with finding shortest distance.
    /**
     * Traverses through the given graph. Returns two different arrays.
     * Index 0 in returned array is an array of the distance to every vertex in the graph from source. Distance -1 means there
     * is no way to get to that vertex from source.
     * Index 1 in returned array is an array named path. Where path[x] gives what vertex was visited before x in the shortest path from source.
     * @param g the graph to traverse through
     * @param source Starting point in traverse.
     */
    private static int[][] dijktras(Graph g, int source){
        
        int size = g.numVertices();
        int[] dist = undefinedList(size);
        int[] prev = undefinedList(size);
        dist[source] = 0;
        int[][] res = new int[][]{dist, prev};
        
//        Comparator<Integer> comp = new Comparator<Integer>(){
//
//            @Override
//            public int compare(Integer o1, Integer o2) {
//                if(o1==-1){
//                    return 1;
//                } else if (o2==-1){
//                    return -1;
//                } else if (o1<o2){
//                    return -1;
//                } else if (o2>o1){
//                    return 1;
//                } else{
//                    return 0; //o1==o2
//                }
//                
//                
//            }
//            
//        };
        
        //PriorityQueue<Integer> q = new PriorityQueue<>(size, comp);
        HashSet<Integer> q = new HashSet<>(); //Order does not matter in queue since searching for shortest distance.
        fillQueue(q, size);
        while(!q.isEmpty()){
            int u = getIndexOfMinDistance(q, dist);
            q.remove(u);
          //  int u = q.poll();
            if (u==-1){
                break;
            }
            
            VertexIterator it = g.neighbors(u);
            while(it.hasNext()){
                int v = it.next();
                int alt = dist[u] + g.cost(u, v);
                if (alt < dist[v] || dist[v]==-1){
                    dist[v] = alt;
                    prev[v] = u;
                }
            }    
        }
        
        return res;
    }
    
    /**
     * Returns the index of the minimum number from the array dist selected from the index values in list.
     * @param list List of index to check
     * @param dist List of the distance to every element
     * @return the index of all the numbers in list that gives minimum result in dist.
     */
    private static int getIndexOfMinDistance(HashSet<Integer> list, int[] dist){
        int min=-1;
        for (int i:list){
            int value=dist[i];
            if(value!=-1){
                return i;
            }
        }
        return min;
    }
    
    /**
     * Fills the given HashSet with the numbers between 0 and size-1
     * @param queue The HashSet.
     * @param size The size.
     */
    private static void fillQueue(HashSet<Integer> queue, int size){
        for (int i=0; i<size;i++){
            queue.add(i);
        }
    }
    
    /**
     * Creates a string of a path where to is the endpoint.
     * @param prev A list where prev[v] is the vertex that was visited before v in the path.
     * @param to The endpoint vertex
     * @return Returns a string describing the path where to is endpoint
     * in the array prev where prev[v] is the vertex visited before v.
     */
    private static String getPath(int[] prev, int to){
        StringBuilder sb= new StringBuilder();
        sb.append("{");
        path(prev, to, sb);
        sb.deleteCharAt(sb.length()-1);
        sb.append("}");
        return sb.toString();
    }
    
    /**
     * Help method for getPath(int[], int).
     * Builds a string of the path with recursion.
     * @param prev A list where prev[v] is the vertex that was visited before v in the path.
     * @param to The endpoint vertex
     * @param sb A StringBuilder that should build String
     */
    private static void path(int[] prev, int to, StringBuilder sb){
        int v = prev[to];
        if(v==-1){
             sb.append(Integer.toString(to));
             sb.append(" ");
        } else {
            path(prev, v, sb); 
            sb.append(to);
            sb.append(" ");
        }
    }
}
   