import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;

/**
 * a single thread to take an input and write it to the requested output
 * DNS caching is seen around line 40-42 (may change based on comments and stuff)
 */
public class ProxyThread implements Runnable {

    private Socket clientSocket;
    private DNSTable dnsTable;

    public ProxyThread(Socket givenSocket, DNSTable givenDNSTable) {
        this.clientSocket = givenSocket;
        this.dnsTable = givenDNSTable;
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
                    if(packet.hasPayload()){
                        packet.readPayload(input);
                    }
                    dnsTable.query(packet.parseHost());//do an initial query to build the table if necessary.
                    InetAddress remoteAddress =  dnsTable.query(packet.parseHost()).address;//get the result
                    Socket remoteSocket = new Socket(remoteAddress, 80);//use the result
                    new Thread(new ResponseThread(clientSocket, remoteSocket)).start();
                    OutputStream output = remoteSocket.getOutputStream();
                    output.write(packet.toByteArray());
                }
            }
            clientSocket.close();
        } catch (IOException e) {
            //too verbose to output this.
            //System.err.println("Remote host closed socket.");
        }
        System.out.println("Client "+ clientSocket.getInetAddress().toString().substring(1) +" served.");
    }//end of run method
}//end of class