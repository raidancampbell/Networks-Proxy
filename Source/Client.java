import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class Client {

    public static void main(String[] args) {
        try {
            System.out.println("Began. connecting to socket.");
            Socket clientSocket = new Socket("localhost", 5005);
            System.out.println("Connected to socket. preparing to make request");
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
            System.out.println("Opened request.");
            writeData(outToServer);
            clientSocket.shutdownOutput();
            //tricky, tricky.  Don't close the output stream.  The close call gets passed to the socket.
            System.out.println("Made request, waiting for response.");
            System.out.println("Response: "+inFromServer.readLine());
            clientSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Complete");
    }//empty main method to initialize program


    private static void writeData(DataOutputStream outToServer) {
        try {
            outToServer.writeBytes("hello \n");//the '\n' is necessary
            outToServer.flush();
        } catch(Exception e){
            //todo: fill this if it's still around
        }
    }
}