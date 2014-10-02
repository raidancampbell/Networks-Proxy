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

import java.io.*;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.lang.Exception;
import java.lang.SuppressWarnings;
import java.lang.System;
import java.net.*;

public class Server{

    public static void main(String[] args){
        try {
            ServerSocket welcomeSocket = new ServerSocket(5005);
            while (true) {
                Socket connectionSocket = welcomeSocket.accept();
                BufferedReader input = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                DataOutputStream output = new DataOutputStream(connectionSocket.getOutputStream());
                output.writeBytes(input.toString().toUpperCase());
            }
        } catch(Exception e){
            System.out.println(e);
        }
    }//empty main method to initialize program

}