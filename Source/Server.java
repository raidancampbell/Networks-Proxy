/*
Notes:
I'm working on port 5005
Direct TCP connections must be used for HTTP connections
DNS resolutions can use higher-level stuff

TODO: receiving responses (usually).  Don't know what's wrong

look at headers.  There's likely a source/destination header that needs to be changed by the proxy
Should be able to handle multiple connections

Use distinct parts:
get request.
read request.
modify request.
send request.

get response.
read response.
modify response.
send response.

Ensure there is a connection:close field in the header.  NOT connection:keep-alive
    -Since the client specified this header, you may need to add a closing statement to your
    -response, to indicate that the server will not handle keep-alive connections
Use byte buffers because you may be handling binaries
flush sockets(?)

Tips & Tricks:
   -DO NOT CLOSE THE DATAOUTPUTSTREAM.  it will close the socket.
    instead call socket.shutdownOutput
   -Each dataOutputStream write must end in a '\n'
   -Close sockets when completely done
   -use an InputStream to read byte[], not a bufferedReader
   -to get a string from byte[] call "String s = new String(byte);
   -host must be resolved using Inet tools, cannot resolve on the fly by feeding a socket the hostname
 */

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class Server {

    public final static int PORT_NUMBER = 5005;

    /**
     * Trying out a pretty linear programming style.
     * @param args *unused*
     * Complexity: 1
     */
    public static void main(String[] args) {
        try {
            ServerSocket welcomeSocket = new ServerSocket(PORT_NUMBER);
            while (true) {
                Socket clientSocket = welcomeSocket.accept();
                byte[] givenData = readFromSocket(clientSocket);
                log(givenData);
                Socket remoteRequest = forwardRequest(givenData);
                byte[] response = readFromSocket(remoteRequest);
                close(remoteRequest);
                log(response);
                writeToSocket(clientSocket, response);
                close(clientSocket);
                System.out.println("Serviced request.");
            }
        } catch (Exception e) {
            System.err.println("An uncaught error was encountered!");
            e.printStackTrace();
        }
    }//empty main method to initialize program

    /**
     * You give me the HTTP request, I send it to the intended destination
     * @param givenData HTTP request
     * @return the socket established with the destination address
     */
    private static Socket forwardRequest(byte[] givenData){
        String host = parseHost(givenData); //I may return null
        try{
            Socket clientSocket = new Socket(InetAddress.getByName(host),80);
            writeToSocket(clientSocket, givenData);
            clientSocket.shutdownOutput();
            return clientSocket;
        } catch (UnknownHostException e){
            System.err.println("Error: host '"+host+"' was not found!");
        } catch (IOException e){
            System.err.println("Error: I/O exception while contacting destination server!");
        } catch (NullPointerException e){
            System.err.println("Error: unable to determine destination host");
        }
        return null;
    }

    /**
     * closes the socket, handling if the socket was null
     * @param s socket to close
     */
    private static void close(Socket s){
        if(s == null) return;
        try {
            s.close();
        } catch(IOException e){
            System.err.println("Error closing socket!");
        }
    }

    /**
     * Grabs the host from the HTTP request
     * @param request HTTP request as a string
     * @return the hostname as a string
     *
     * Complexity: 2
     */
    public static String parseHost(byte[] request){
        String http = new String(request);
        if(http.indexOf("Host") < 0) {
            System.err.println("Malformed HTTP request!");
            return null;
        }
        String returnVar = http.substring(http.indexOf("Host:"));
        returnVar = returnVar.trim();
        returnVar = returnVar.substring(returnVar.indexOf(' '),returnVar.indexOf("\n"));
        returnVar = returnVar.trim();
        System.out.println(returnVar);
        return returnVar;
    }

    /**
     * writes the given data to a file.  Useful for debugging.
     * @param data data to log
     */
    private static void log(byte[] data){
        try {
            PrintWriter writer = new PrintWriter("logs/"+System.currentTimeMillis()+".log", "UTF-8");
            String text = new String(data);
            writer.println(text);
            writer.close();
        } catch(Exception e){
            System.err.println("Error on logging request!");
        }
    }

    /**
     * writes the given data to the given socket
     * @param connectionSocket socket to write to
     * @param data data to write to socket
     */
    private static void writeToSocket(Socket connectionSocket, byte[] data){
        try {
            DataOutputStream output = new DataOutputStream(connectionSocket.getOutputStream());
            output.write(data);
        } catch(IOException e){
            System.err.println("Error while writing data to client!");
        }
    }

    /**
     * reads data written to the socket by a remote host
     * @param connectionSocket socket to read
     * @return data read from socket
     *
     * Complexity: 1
     */
    private static byte[] readFromSocket(Socket connectionSocket) {
        byte[] returnVar = new byte[0xFF];//must be initialized...
        try {
            InputStreamReader input =new InputStreamReader(connectionSocket.getInputStream());
            returnVar = org.apache.commons.io.IOUtils.toByteArray(input);
        } catch (IOException e) {
            System.err.println("Error while reading input from client!");
        } catch (NullPointerException e){
            System.err.println("A bad socket was given!");
        }
        return returnVar;
    }
}