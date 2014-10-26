import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*Created by aidan on 10/26/14.
 */
public class HeaderEditor {

    public static int parseLength(String header){
        int returnVar = 0;
        Pattern pattern = Pattern.compile("Content-Length:(.)*");
        Matcher matcher = pattern.matcher(header);
        if(!matcher.find()){
            System.err.println("Malformed HTTP request! No Content-Length specified!");
            return -1;
        }
        String length = matcher.group();
        length = length.replaceFirst("Content-Length: ","");
        length = length.trim();
        try {
            returnVar = Integer.parseInt(length);
        } catch(NumberFormatException e){
            System.err.println("Error while parsing '"+length+"' to an integer!");
        }
        return returnVar;
    }

    public static int parseLength(byte[] request){
        String strRequest = new String(request);
        return parseLength(strRequest);
    }

    public static int parseLength(String[] header){
        StringBuilder sb = new StringBuilder();
        for(String s: header)sb.append(s);
        return parseLength(sb.toString());
    }

    /**
     * Grabs the host from the HTTP request
     * @param request HTTP request as a string
     * @return the hostname as a string
     *
     * Complexity: 2
     */
    public static String parseHost(byte[] request){
        String httpHeader = new String(request);
        Pattern pattern = Pattern.compile("Host:(.)*");
        Matcher matcher = pattern.matcher(httpHeader);
        if(!matcher.find()){
            System.err.println("Malformed HTTP request! No Host specified!");
            return null;
        }
        String returnVar = matcher.group();
        returnVar = returnVar.trim();
        returnVar = returnVar.replaceFirst("Host:","");
        returnVar = returnVar.replaceFirst("(.)*www\\.", "www."); //get rid of the 'Host: ' part
        returnVar = returnVar.replaceFirst(":(.)*", "");//get rid of the explicit port.
        returnVar = returnVar.trim();
        return returnVar;
    }

}
