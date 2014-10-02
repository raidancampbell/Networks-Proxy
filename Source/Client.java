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
            outToServer.writeBytes("hello \n");
            outToServer.flush();
            System.out.println("Made request, waiting for response.");
            System.out.println(inFromServer.readLine());
            clientSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Complete");
    }//empty main method to initialize program


}