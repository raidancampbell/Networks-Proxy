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
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class Server{

    public static void main(String[] args){
        try {
            ServerSocket welcomeSocket = new ServerSocket(5005);
            System.out.println("Opened welcome socket, waiting for client");
            while (true) {
                Socket connectionSocket = welcomeSocket.accept();
                System.out.println("connection accepted");
                BufferedReader input = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                System.out.println("Got input. writing output.");
                DataOutputStream output = new DataOutputStream(connectionSocket.getOutputStream());
                System.out.println("Got output stream.  Writing to it.");

                output.writeBytes(input.readLine().toUpperCase()+'\n');

                System.out.println("done doing stuff. closing.");
                connectionSocket.close();
            }
        } catch(Exception e){
            e.printStackTrace();
        }
        System.out.println("Finished.");
    }//empty main method to initialize program

}