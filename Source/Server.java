/*
Notes:
I'm working on port 5005
Direct TCP connections must be used for HTTP connections
DNS resolutions can use higher-level stuff

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

 */

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class Server {
    public final static int PORT_NUMBER = 5005;

    public static void main(String[] args) {
        try {
            ServerSocket welcomeSocket = new ServerSocket(PORT_NUMBER);
            while (true) {
                Socket clientSocket = welcomeSocket.accept();
                String givenData = readFromSocket(clientSocket);
                log(givenData);
                Socket remoteRequest = forwardRequest(givenData); //may be null
                String response = readFromSocket(remoteRequest);
                remoteRequest.close();
                log(response);
                writeToSocket(clientSocket, response);
                clientSocket.close();
                System.out.println("Serviced request.");
            }
        } catch (Exception e) {
            System.err.println("An uncaught error was encountered!");
            e.printStackTrace();
        }
    }//empty main method to initialize program

    private static Socket forwardRequest(String givenData){
        String[] lines = givenData.split("\n");
        String host = parseHost(lines); //I may return null
        try{
            Socket clientSocket = new Socket(host,80);
            writeToSocket(clientSocket, givenData);
            clientSocket.shutdownOutput();
            return clientSocket;
        } catch (UnknownHostException e){
            System.err.println("Error: host '"+host+"' was not found!");
        } catch (IOException e){
            System.err.println("Error: I/O exception while contacting destination server!");
        }
        return null;
    }

    public static String parseHost(String[] http){
        if(http.length < 2) return null;
        if(http[1].indexOf("Host") == -1){
            System.err.println("Malformed HTTP request!");
            return null;
        }
        String[] hostLine = http[1].split(":");
        return hostLine[1].trim();
    }

    private static void log(String text){
        try {
            PrintWriter writer = new PrintWriter("logs/"+System.currentTimeMillis()+".log", "UTF-8");
            String[] lines = text.split("\n");
            for(String s : lines) writer.println(s);
            writer.close();
        } catch(Exception e){
            System.err.println("Error on logging request!");
        }
    }

    private static void writeToSocket(Socket connectionSocket, String data){
        try {
            DataOutputStream output = new DataOutputStream(connectionSocket.getOutputStream());
            output.writeBytes(data.toUpperCase() + '\n');//the '\n' is necessary
        } catch(IOException e){
            System.err.println("Error while writing data to client!");
        }
    }

    private static String readFromSocket(Socket connectionSocket) {
        StringBuilder returnVar = new StringBuilder("");
        try {
            BufferedReader input = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            String temp;
            while ((temp = input.readLine()) != null) {
                returnVar.append(temp).append('\n');
            }
        } catch (IOException e) {
            System.err.println("Error while reading input from client!");
        } catch (NullPointerException e){
            System.err.println("A bad socket was given!");
        }
        return returnVar.toString();
    }
}