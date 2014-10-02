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
Use byte buffers because you may be handling binaries
flush sockets(?)
 */

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    public static void main(String[] args) {
        try {
            ServerSocket welcomeSocket = new ServerSocket(5005);
            System.out.println("Opened welcome socket, waiting for client");
            while (true) {
                Socket connectionSocket = welcomeSocket.accept();
                String givenData = readInput(connectionSocket);
                System.out.println("read input successfully");
                String capitalizedData = givenData.toUpperCase();
                writeData(connectionSocket,capitalizedData);
                connectionSocket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Finished.");
    }//empty main method to initialize program

    private static void writeData(Socket connectionSocket,String data){
        try {
            DataOutputStream output = new DataOutputStream(connectionSocket.getOutputStream());
            output.writeBytes(data.toUpperCase() + '\n');//the '\n' is necessary
        } catch(IOException e){
            System.err.println("Error while writing data to client!");
        }
    }

    private static String readInput(Socket connectionSocket) {
        StringBuffer returnVar = new StringBuffer("");

        try {
            BufferedReader input = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            String temp;
            while ((temp = input.readLine()) != null) {
                returnVar.append(temp).append('\n');
            }
        } catch (IOException e) {
            System.err.println("Error while reading input from client!");
        }
        return returnVar.toString();
    }

}