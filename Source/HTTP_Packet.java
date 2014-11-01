import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HTTP_Packet {
    private String headerText;
    private byte[] payload;


    /**
     * constructor
     * @param byteBuffer buffer containing the header
     * @param ipAddress Client's address
     */
    public HTTP_Packet(ByteBuffer byteBuffer, String ipAddress) {
        byteBuffer.flip();
        StringBuilder builder = new StringBuilder();
        while(byteBuffer.hasRemaining()) {
            byte nextByte = byteBuffer.get();
            builder.append((char)nextByte);
        }
        byteBuffer.flip();
        headerText = builder.toString();
        headerText = headerText.replaceFirst("Connection: keep-alive","Connection: close");
        headerText = addForwardHeader(ipAddress);
        payload = new byte[0];
    }


    /**
     * grabs the destination host of the packet
     * @return the host as a string
     */
    public String parseHost(){
        Pattern pattern = Pattern.compile("Host:(.)*");
        Matcher matcher = pattern.matcher(headerText);
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

    /**
     * @return the byte array of the packet
     */
    public byte[] toByteArray() {
        if(!hasPayload()){
            return headerText.getBytes();
        }
        ByteBuffer temp = ByteBuffer.allocate(8192);
        temp.put(headerText.getBytes());
        temp.put(payload);
        byte[] returnVar = new byte[temp.remaining()];
        temp.get(returnVar, 0, temp.remaining());
        return returnVar;
    }

    @Override
    public String toString() {
        return headerText;
    }

    /**
     * adds the 'X-Forwarded-For' header
     * @param clientIP IP being forwarded for
     * @return the new header with the field added
     */
    public String addForwardHeader(String clientIP){
        if(headerText == null) return null;
        StringBuffer returnVar = new StringBuffer(headerText);
        returnVar = new StringBuffer(returnVar.toString().replaceFirst("X-Forwarded-For: ",
                "X-Forwarded-For: "+clientIP.substring(1)));

        if(!returnVar.toString().contains("X-Forwarded-For: ")){
            int index = 0;
            while(returnVar.charAt(index) != '\n'){ index++;}
            index++;//go just past the CRLF
            returnVar.insert(index, "X-Forwarded-For: "+clientIP.substring(1)+"\r\n");
        } else {
            String temp = returnVar.toString();
            temp = temp.replace("X-Forwarded-For: "+clientIP,"");
            returnVar = new StringBuffer(temp);
        }
        return returnVar.toString();
    }

    /**
     * @return whether the packet has a payload
     */
    public boolean hasPayload(){
        return headerText.contains("Content-Length");
    }

    /**
     * unimplemented
     * reads the payload, putting it into the payload[] field
     * @param inputStream stream to read from
     */
    public void readPayload(InputStream inputStream){
        ByteBuffer temp = ByteBuffer.allocate(8192);
    }
}