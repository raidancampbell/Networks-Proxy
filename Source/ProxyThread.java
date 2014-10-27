import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/*Created by aidan on 10/26/14.
 */
public class ProxyThread implements Runnable{

    private Socket clientSocket;

    public ProxyThread(Socket givenSocket){
     this.clientSocket = givenSocket;
    }

    /**
     * Trying out a pretty linear programming style.
     * Complexity: 1
     */
    @Override
    public void run(){
        try {
            System.out.println("Thread instantiated.");
            byte[] givenData = readFromSocket(clientSocket);
            givenData = HeaderEditor.convertConnection(givenData);
            log(givenData);
            Socket remoteRequest = forwardRequest(givenData);
            byte[] response = readFromSocket(remoteRequest);
            response = HeaderEditor.convertConnection(response);
            close(remoteRequest);
            log(response);
            writeToSocket(clientSocket, response);
            close(clientSocket);
            System.out.println("Serviced request.");
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
            boolean isEmpty = true;
            for(byte b: data)if (b != 0x0000) isEmpty = false;
            if(isEmpty) return;

            PrintWriter writer = new PrintWriter("logs/"+System.currentTimeMillis()+".log", "UTF-8");
            String text = new String(data);
            writer.println(text);
            writer.close();
        } catch(FileNotFoundException e){
            System.err.println("Error on logging request! File Not Found Exception thrown!");
        } catch (UnsupportedEncodingException e){
            System.err.println("Error on logging request! Unsupported Encoding Exception thrown!");
        } catch(NullPointerException e){
            return;
        }
    }

    /**
     * writes the given data to the given socket
     * @param connectionSocket socket to write to
     * @param data data to write to socket
     */
    private static void writeToSocket(Socket connectionSocket, byte[] data){
        try {
            boolean isEmpty = true;
            for(byte b: data)if (b != 0x0000) isEmpty = false;
            if(isEmpty) return;
            DataOutputStream output = new DataOutputStream(connectionSocket.getOutputStream());
            output.write(data);
        } catch(IOException e){
            System.err.println("Error while writing data to client!");
        } catch(NullPointerException e){
            return;
        }
    }

    /**
     * you gotta read everything, and pay attention to header + content-Length bytes.  toss everything else.
     * stream can terminate by not writing more bytes, or just writing more null bytes.
     * @param givenSocket
     * @return
     */
    private static byte[] readFromSocket(Socket givenSocket){
        //server may only send \n\n, \r\n\n, \n\r\n, or \r\n\r\n
        try{
            boolean isComplete = false;
            InputStream socketStream = givenSocket.getInputStream();
            byte[] intermediateCache = new byte[2048];
            int bytesReadFromSocket;
            ByteArrayOutputStream specialBuffer = new ByteArrayOutputStream();

            while(!isComplete){//read the header
                bytesReadFromSocket = socketStream.read(intermediateCache, 0, 2048);//read from stream, and count amount read
                if(bytesReadFromSocket < 0) break;//no bytes left in stream. done.
                specialBuffer.write(bytesReadFromSocket);
                if(HeaderEditor.getHeaderEnd(specialBuffer.toByteArray()) != -1) isComplete = true;
            }
            byte[] specialBufferAsBytes = specialBuffer.toByteArray();
            //header was read. now determine payload size
            int contentLength = HeaderEditor.parseLength(new String(specialBuffer.toByteArray()));
            if(contentLength == -1){//no payload. just return header
                byte[] header = new byte[HeaderEditor.getHeaderEnd(specialBuffer.toByteArray())];
                for(int i = 0; i< header.length; i++){
                    header[i] = specialBufferAsBytes[i];
                }
                return header;
            }
            //we had a payload.  return header + payload many bytes.
            byte[] header = new byte[HeaderEditor.getHeaderEnd(specialBuffer.toByteArray())
                    +HeaderEditor.parseLength(specialBuffer.toByteArray())];

            for(int i = 0; i< header.length; i++){
                header[i] = specialBufferAsBytes[i];
            }
            return header;
        } catch (IOException e){

        } catch (NullPointerException e){

        }
        return null;
    }

}

