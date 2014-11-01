import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*Created by aidan on 10/26/14.
 */
public class HeaderEditor {

    /**
     * Grabs the host from the HTTP request
     * @param request HTTP request as a string
     * @return the hostname as a string
     *
     * Complexity: 2
     */
    public static String parseHost(byte[] request){
        if(request == null) return null;
        String httpHeader = new String(request);
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

    public static byte[] convertConnection(byte[] request){
        if(request == null) return null;
        String returnVar = new String(request);
        returnVar = returnVar.replaceFirst("Connection: keep-alive","Connection: close");
        return returnVar.getBytes();
    }

    public static byte[] toggleForwardHeader(byte[] request, String clientIP){
        if(request == null) return null;
        StringBuffer returnVar = new StringBuffer(new String(request));
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
        return returnVar.toString().getBytes();
    }
}