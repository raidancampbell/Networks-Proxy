import java.nio.ByteBuffer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class HTTP_Packet {
    //packet text includes the payload
    private String packetText;

    /**
     * Constructor of the HTTP_Packet class that reads a single packetText from
     * the byte buffer and adds the appropriate header fields for a proxy
     * application.
     *
     * @param byteBuffer - byte buffer containing a single HTTP packetText
     * @param ipAddress - IP address of the browser's host
     */
    public HTTP_Packet(ByteBuffer byteBuffer, String ipAddress) {
        byteBuffer.flip();
        StringBuilder builder = new StringBuilder();
        while(byteBuffer.hasRemaining()) {
            byte nextByte = byteBuffer.get();
            builder.append((char)nextByte);
        }
        byteBuffer.flip();
        packetText = builder.toString();
        packetText = packetText.replaceFirst("Connection: keep-alive","Connection: close");
        packetText = toggleForwardHeader(packetText, ipAddress);
    }

    /**
     * Finds the hostname corresponding to the HTTP packetText.
     *
     * @return a String containing the headerText of the Host header field
     */
    public String parseHost(){
        Pattern pattern = Pattern.compile("Host:(.)*");
        Matcher matcher = pattern.matcher(packetText);
        if(!matcher.find()){
            System.err.println("Malformed HTTP request! No Host specified!");
            return null;
        }
        String returnVar = matcher.group();
        returnVar = returnVar.trim();
        returnVar = returnVar.replaceFirst("Host: ","");
        returnVar = returnVar.replaceFirst(":(.)*", "");//get rid of the explicit port.
        returnVar = returnVar.trim();
        return returnVar;
    }

    public byte[] toByteArray() {
        return packetText.getBytes();
    }

    @Override
    public String toString() {
        return packetText;
    }


    public static String toggleForwardHeader(String request, String clientIP){
        if(request == null) return null;
        StringBuffer returnVar = new StringBuffer(request);
        returnVar = new StringBuffer(returnVar.toString().replaceFirst("X-Forwarded-For: ","X-Forwarded-For: "+clientIP.substring(1)));
        if(!returnVar.toString().contains("X-Forwarded-For: ")){
            int index = 0;
            while(returnVar.charAt(index) != '\n') {//AOOBE
                index++;
                if(index >= returnVar.length()){
                    //we've got problems
                }
            }
            index++;//go just past the CRLF
            returnVar.insert(index, "X-Forwarded-For: "+clientIP.substring(1)+'\n');
        } else {
            String temp = returnVar.toString();
            temp = temp.replace("X-Forwarded-For: "+clientIP,"");
            returnVar = new StringBuffer(temp);
        }
        return returnVar.toString();
    }

}