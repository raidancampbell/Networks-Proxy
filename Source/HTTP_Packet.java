import java.nio.ByteBuffer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class HTTP_Packet {
    //packet text includes the payload, header text is only the header
    private String packetText;

    private static final String HOST = "Host";
    private static final String CONNECTION = "Connection";
    private static final String FORWARD = "X-Forwarded-For";

    private String headerText;

    public String getHeaderString() {
        return headerText;
    }

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
        addHeaderField(CONNECTION, "closed");
        addHeaderField(FORWARD, ipAddress);
    }

    /**
     * Finds the hostname corresponding to the HTTP packetText.
     *
     * @return a String containing the headerText of the Host header field
     */
    public String parseHost(){
        String httpHeader = headerText;
        Pattern pattern = Pattern.compile("Host:(.)*");
        Matcher matcher = pattern.matcher(httpHeader);
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
     * Creates a byte array from the HTTP packetText String.
     *
     * @return a byte array containing HTTP packetText data
     */
    public byte[] toByteArray() {
        return packetText.getBytes();
    }

    @Override
    public String toString() {
        return packetText;
    }

    /**
     * Adds a header field  to the HTTP packetText with a specified headerText. If the
     * header field  is already in use, the headerText is replaced.
     *
     * @param headerField - the header field
     * @param value - the headerText corresponding to the headerField
     */
    private void addHeaderField(String headerField, String value) {
        if(value == null) {
            return;
        }
        // find the start of the headerField field
        StringBuilder builder = new StringBuilder();
        int start = getStartOfHeaderField(headerField);
        if(start == -1) { // does not contain headerField field
            start = getNewHeaderFieldIndex();
            builder.append(packetText.substring(0, start));
            builder.append(getHeaderString()).append(": ").append(value).append("\r\n");
            builder.append(packetText.substring(start));
        } else { // contains headerField field so replace current headerText
            start += getHeaderString().length() + ": ".length();
            // find end of headerField field
            int end = start;
            while(packetText.charAt(end) != '\r') {
                end++;
            }
            builder.append(packetText.substring(0, start));
            builder.append(value);
            builder.append(packetText.substring(end));
        }
        packetText = builder.toString();
    }

    /**
     * Removes a header field from the HTTP packetText if it exists.
     *
     * @param headerField - the header field to be removed
     */
    private void removeHeaderField(String headerField) {
        int start = getStartOfHeaderField(headerField);
        if(start != -1) {
            int end = start;
            while(packetText.charAt(end) != '\n') {
                end++;
            }
            packetText = packetText.substring(0, start) + packetText.substring(end + 1);
        }
    }

    /**
     * Finds a place to insert another header field. The index returned
     * will be the field directly following the Host header field.
     *
     * @return the index of new header field
     */
    private int getNewHeaderFieldIndex() {
        // find host header field
        int index = getStartOfHeaderField(HOST);
        if(index != -1) {
            // find index of end of line
            while(packetText.charAt(index) != '\n') {
                index++;
            }
        } else {
            System.err.println("Could not resolve hostname");
            System.err.flush();
            System.exit(1);
        }
        return index + 1;
    }

    /**
     * Finds a header field in the packetText message if it exists.
     *
     * @param headerField - the header field to find
     * @return the starting index of the header field; -1 if the field doesn't
     * exist
     */
    private int getStartOfHeaderField(String headerField) {
        int index = 0;
        while(true) {
            index = packetText.indexOf(getHeaderString(), index);
            if(index == -1 || packetText.charAt(index - 1) == '\n') {
                return index;
            } else {
                index++;
            }
        }
    }
}