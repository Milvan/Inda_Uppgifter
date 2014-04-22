
import java.io.*;
import java.util.StringTokenizer;


/**
 * This class provides the function to read one token at a time in a BufferedReader object.
 * The token "//" is treated as a comment and everything after that on the same line will be ignored.
 * @author Marcus Larsson
 * @version 2014-04-22
 */
public class SpecialBufferReader {
    private final BufferedReader in;
    private StringTokenizer st;
    
    /**
     * Creates a SpecialBufferReader that provides the functionality to read from a BufferedReader and ignoring all after "//" on the same line.
     * @param in The BufferedReader to scan
     * @throws IOException From BufferedReader.readLine().
     */
    public SpecialBufferReader(BufferedReader in) throws IOException{
        this.in = in;
        nextLine();
    }
    
    /**
     * Reads in the next line to this SpecialBufferReader.
     * @throws IOException From BufferedReader.readLine().
     */
    private void nextLine()throws IOException{
        String line;
        if((line = in.readLine())!=null){
            st = new StringTokenizer(line);
        } else {
            st = null;
        }
    }
    
    /**
     * Returns the next token from this SpecialBufferReader or null if it reaches the end of the buffer.
     * @return The next token or null if end of buffer
     * @throws IOException From BufferedReader.readLine()
     */
    public String getNextToken()throws IOException{
            while(st!=null){
                if (st.hasMoreTokens()){
                    String token = st.nextToken();
                    if (token.contains("//")){
                        if(token.startsWith("//")){
                            nextLine(); // go to next line when found // since it's treated as comment
                        } else {
                            return token.substring(0, token.indexOf("//"));
                        }
                        
                    } else {
                        return token;
                    }
                    
                } else {
                    nextLine(); // go to next line when line is empty 
                }
            
            }
        return null;
    }
}
