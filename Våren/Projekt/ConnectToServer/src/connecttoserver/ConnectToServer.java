/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package connecttoserver;

import java.io.*;
import java.net.*;
import java.util.Scanner;

/**
 *
 * @author Marcus
 */
public class ConnectToServer {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
//        String urlStr = "http://localhost:8080";  
//        try{  
//        URL url = new URL(urlStr);  
//        HttpURLConnection connection= (HttpURLConnection)url.openConnection();  
//        //connection.setAllowUserInteraction(true);  
//        //connection.setRequestMethod("POST");  
//        //connection.setDoOutput(true);  
//        //connection.setUseCaches(false);  
//        Scanner scan = new Scanner(connection.getInputStream());
//          
//        System.out.println("*************CONNECTED*************");
//        System.out.println(scan.nextLine());
//        //connection.disconnect();  
//        }catch(Exception e){  
//            e.printStackTrace();  
//        }
        
        
        
        
        
//        String hostName = "localhost";
//        int portNumber = 4000;
// 
//        try (
//            Socket echoSocket = new Socket(hostName, portNumber);
//            PrintWriter out =
//                new PrintWriter(echoSocket.getOutputStream(), true);
//            BufferedReader in =
//                new BufferedReader(
//                    new InputStreamReader(echoSocket.getInputStream()));
//            BufferedReader stdIn =
//                new BufferedReader(
//                    new InputStreamReader(echoSocket.getInputStream()))
//        ) {
//            echoSocket.connect(new InetSocketAddress(4000));
//            String userInput;
//            while ((userInput = stdIn.readLine()) != null) {
//                out.println(userInput);
//                System.out.println("echo: " + in.readLine());
//            }
//        } catch (UnknownHostException e) {
//            System.err.println("Don't know about host " + hostName);
//            System.exit(1);
//        } catch (IOException e) {
//            System.err.println("Couldn't get I/O for the connection to " +
//                hostName);
//            System.exit(1);
//        } 
//    }
        String sentence;   String modifiedSentence;   
        BufferedReader inFromUser = new BufferedReader( new InputStreamReader(System.in));
        try(
        Socket clientSocket = new Socket("localhost", 4000);   
        PrintWriter outToServer = new PrintWriter(clientSocket.getOutputStream(), true);   
        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));  
        ){        
            
            while(true){
                sentence = inFromUser.readLine();   
            outToServer.println(sentence);
            modifiedSentence = inFromServer.readLine();   
            System.out.println("FROM SERVER: " + modifiedSentence);   
            }
            //clientSocket.close();  
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host ");
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " );
            System.exit(1);
        } 
    } 
    
}
