/*
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