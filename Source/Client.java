import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.*;
import java.lang.Exception;
import java.lang.SuppressWarnings;
import java.lang.System;

public class Client {

    public static void main(String[] args) {
        try {
            Socket clientSocket = new Socket("localhost", 5005);
            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            outToServer.writeBytes("hello");
            System.out.println(inFromServer.readLine());
            clientSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }//empty main method to initialize program


}