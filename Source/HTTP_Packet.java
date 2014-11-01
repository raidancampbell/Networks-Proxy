import java.nio.ByteBuffer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class HTTP_Packet {
    //packet text includes the payload
    private String packetText;

    private enum HTTPHeaderField {
        HOST("Host"),
        CONNECTION("Connection"),
        FORWARD("X-Forwarded-For");

        private String fieldContents;//the contents of this particular field

        HTTPHeaderField(String fieldContents) {
            this.fieldContents = fieldContents;
        }

        public String getHeaderString() {
            return fieldContents;
        }
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
        addHeaderField(HTTPHeaderField.CONNECTION, "closed");
        addHeaderField(HTTPHeaderField.FORWARD, ipAddress);
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

    /**
     * Creates a byte array from the HTTP request String.
     *
     * @return a byte array containing HTTP request data
     */
    public byte[] toByteArray() {
        return packetText.getBytes();
    }

    @Override
    public String toString() {
        return packetText;
    }

    /**
     * Adds a header field to the HTTP request with a specified fieldContents. If the
     * header field is already in use, the fieldContents is replaced.
     *
     * @param field - the header field
     * @param newFieldValue - the fieldContents corresponding to the header field
     */
    private void addHeaderField(HTTPHeaderField field, String newFieldValue) {
        if(newFieldValue == null) {
            return;
        }
        // find the start of the header field
        StringBuilder builder = new StringBuilder();
        int start = getStartOfHeaderField(field);
        if(start == -1) { // does not contain header field
            start = getNewHeaderFieldIndex();
            builder.append(packetText.substring(0, start));
            builder.append(field.getHeaderString() + ": " + newFieldValue + "\r\n");
            builder.append(packetText.substring(start));
        } else { // contains header field so replace current fieldContents
            start += field.getHeaderString().length() + ": ".length();
            // find end of header field
            int end = start;
            while(packetText.charAt(end) != '\r') {
                end++;
            }
            builder.append(packetText.substring(0, start));
            builder.append(newFieldValue);
            builder.append(packetText.substring(end));
        }
        packetText = builder.toString();
    }

    /**
     * Removes a header field from the HTTP request if it exists.
     *
     * @param field - the header field to be removed
     */
    private void removeHeaderField(HTTPHeaderField field) {
        int start = getStartOfHeaderField(field);
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
        int index = getStartOfHeaderField(HTTPHeaderField.HOST);
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
     * Finds a header field in the request message if it exists.
     *
     * @param field - the header field to find
     * @return the starting index of the header field; -1 if the field doesn't
     * exist
     */
    private int getStartOfHeaderField(HTTPHeaderField field) {
        int index = 0;
        while(true) {
            index = packetText.indexOf(field.getHeaderString(), index);
            if(index == -1 || packetText.charAt(index - 1) == '\n') {
                return index;
            } else {
                index++;
            }
        }
    }
}