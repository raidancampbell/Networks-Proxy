import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ResponseThread implements Runnable {

    private Socket clientSocket;
    private Socket remoteSocket;

    public ResponseThread(Socket clientSocket, Socket remoteSocket) {
        this.clientSocket = clientSocket;
        this.remoteSocket = remoteSocket;
    }

    @Override
    public void run() {
        try {
            InputStream input = remoteSocket.getInputStream();
            OutputStream output = clientSocket.getOutputStream();
            int readVal;
            while((readVal = input.read()) != -1) output.write(readVal);
            remoteSocket.close();
        } catch (IOException e) {
            //too verbose to output this.
            //System.err.println("Remote host closed socket.");
        }
    }//end of run method

}