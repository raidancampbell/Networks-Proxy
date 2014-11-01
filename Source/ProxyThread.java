import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;


public class ProxyThread implements Runnable {

    private Socket clientSocket;

    public ProxyThread(Socket givenSocket) {
        this.clientSocket = givenSocket;
    }

    @Override
    public void run() {
        System.out.println("Ready to serve client: "+clientSocket.getInetAddress().toString().substring(1));
        InetAddress clientIP = clientSocket.getInetAddress();
        ByteBuffer byteBuffer;
        TerminationStage ts = new TerminationStage();
        try {
            InputStream input = clientSocket.getInputStream();
            byteBuffer = ByteBuffer.allocate(2048);
            int readVal;
            while((readVal = input.read()) != -1) {
                byteBuffer.put((byte) readVal);
                if(ts.isHeaderEnded((char)readVal)) {
                    HTTP_Packet packet = new HTTP_Packet(byteBuffer, clientIP.getHostAddress());
                    byteBuffer.clear();
                    Socket remoteSocket = new Socket(packet.parseHost(), 80);
                    new Thread(new ResponseThread(clientSocket, remoteSocket)).start();
                    OutputStream output = remoteSocket.getOutputStream();
                    output.write(packet.toByteArray());
                }
            }
            clientSocket.close();
        } catch (IOException e) {
            System.err.println("Remote host closed socket.");
        }
        System.out.println("Client served.");
    }//end of run method
}//end of class