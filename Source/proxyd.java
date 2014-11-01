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

import java.net.ServerSocket;

public class proxyd {

    static int PORT_NUMBER = 5005;

    /**
     * @param args used to override the default port of 5005
     * Complexity: 1
     */
    public static void main(String[] args) {
        try {
            //simple attempt to parse the port number from the arguments
            //mostly because I don't feel like setting up the Apache CLI junk
            if (args.length > 1) PORT_NUMBER = Integer.parseInt(args[1]);
            if (args.length == 1) PORT_NUMBER = Integer.parseInt(args[0]);
        } catch(NumberFormatException e){
            System.err.println("Bad port number, "+args[0]+" was given!\n exiting...");
            System.exit(1);
        }
        System.out.println("Listening on socket "+PORT_NUMBER);
        try {
            ServerSocket welcomeSocket = new ServerSocket(PORT_NUMBER);
            DNSTable dnsTable = new DNSTable();
            while (true) {
                new Thread(new ProxyThread(welcomeSocket.accept(), dnsTable)).start();
            }
        } catch (Exception e) {
            System.err.println("An uncaught error was encountered!");
            e.printStackTrace();
        }
    }
}