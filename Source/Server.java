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

import java.io.*;
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
                System.out.println("Ready to service request.");
                Socket clientSocket = welcomeSocket.accept();
                byte[] givenData = readFromSocket(clientSocket);
                givenData = HeaderEditor.convertConnection(givenData);
                log(givenData);
                Socket remoteRequest = forwardRequest(givenData);
                byte[] response = readFromSocket(remoteRequest);
                HeaderEditor.convertConnection(response);
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
        String host = HeaderEditor.parseHost(givenData); //I may return null
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
     * writes the given data to a file.  Useful for debugging.
     * @param data data to log
     */
    private static void log(byte[] data){
        try {
            if(data.length == 0) return;
            PrintWriter writer = new PrintWriter("logs/"+System.currentTimeMillis()+".log", "UTF-8");
            String text = new String(data);
            writer.println(text);
            writer.close();
        } catch(FileNotFoundException e){
            System.err.println("Error on logging request! File Not Found Exception thrown!");
        } catch (UnsupportedEncodingException e){
            System.err.println("Error on logging request! Unsupported Encoding Exception thrown!");
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
     *
     * read the header, and parse the Content-Length field
     * after the header, read Content-Length more bytes.
     */
    private static byte[] readFromSocket(Socket connectionSocket) {
        //bytes are read, and written to this buffer,
        //buffer is read at end to return the byte array
        //this method was chosen for flexible length of byte array
        ByteArrayOutputStream specialBuffer = new ByteArrayOutputStream();
        try {
            InputStream is = connectionSocket.getInputStream();
            byte [] data = new byte[4096];
            boolean isHeaderDone = false;
            int bytesRead = 0;
            while(!isHeaderDone) {
                is.read(data, 0, 4096);
                specialBuffer.write(data);
                //if the last 4 bytes of data are CRLF/CRLF, then the header is done
                String finishedChecker = new String(data);
                for(byte b:data){
                    isHeaderDone = (b == 0x0000);
                    if(!isHeaderDone) break;
                }
                if(finishedChecker.contains("\r\n\r\n")) isHeaderDone = true;
                //reset the bytesRead to be number of bytes after \r\n\r\n
                bytesRead = finishedChecker.substring(finishedChecker.indexOf("\r\n\r\n")+4).length();

                //counts unused bytes from read function. bad. TODO: fix it.
            }
            int contentLength = HeaderEditor.parseLength(new String(specialBuffer.toByteArray()));
            if(contentLength == -1) return  specialBuffer.toByteArray();
            while(bytesRead < contentLength){
                bytesRead += is.read(data, 0, 2048);
                specialBuffer.write(data);
            }
        } catch (IOException e) {
            System.err.println("Error while reading input from client!");
        } catch (NullPointerException e){
            System.err.println("A bad socket was given!");
        }
        return specialBuffer.toByteArray();
    }
}