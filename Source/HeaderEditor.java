import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*Created by aidan on 10/26/14.
 */
public class HeaderEditor {

    private enum TERMINATION_STAGE {reset,r1,n1,r2}
    private TERMINATION_STAGE termination_stage = TERMINATION_STAGE.reset;

    public static int parseLength(String header){
        if(header == null) return -1;
        int returnVar = 0;
        Pattern pattern = Pattern.compile("Content-Length:(.)*");
        Matcher matcher = pattern.matcher(header);
        if(!matcher.find()){
            //it was a GET, or non-payload HTTP packet
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

    public static int getHeaderEnd(byte[] header){//0x0D0A == \r\n
        int returnVar = -1;
        TERMINATION_STAGE termination_stage = TERMINATION_STAGE.reset;
        for(int i = 0; i< header.length; i++) {//0D0A
            byte b = header[i];
            switch (termination_stage){
                case reset:
                    if (b == 0x0D) termination_stage = TERMINATION_STAGE.r1;
                    break;
                case r1:
                    if(b == 0x0A) termination_stage = TERMINATION_STAGE.n1;
                    if(b == 0x0D) termination_stage = TERMINATION_STAGE.r1;
                    if(b != 0x0D && b != 0x0A) termination_stage = TERMINATION_STAGE.reset;
                    break;
                case n1:
                    if(b == 0x0D) termination_stage = TERMINATION_STAGE.r2;
                    if(b == 0x0A) termination_stage = TERMINATION_STAGE.r1;
                    if(b != 0x0D && b != 0x0A) termination_stage = TERMINATION_STAGE.reset;
                    break;
                case r2:
                    if(b == 0x0A) return i;
                    if(b == 0x0D) termination_stage = TERMINATION_STAGE.r1;
                    else termination_stage = TERMINATION_STAGE.reset;
                    break;
            }
        }
        return returnVar;
    }

    public boolean isHeaderEnded(char c){
        byte b = (byte) c;
        switch (termination_stage){
            case reset:
                if (b == 0x0D) termination_stage = TERMINATION_STAGE.r1;
                break;
            case r1:
                if(b == 0x0A) termination_stage = TERMINATION_STAGE.n1;
                if(b == 0x0D) termination_stage = TERMINATION_STAGE.r1;
                if(b != 0x0D && b != 0x0A) termination_stage = TERMINATION_STAGE.reset;
                break;
            case n1:
                if(b == 0x0D) termination_stage = TERMINATION_STAGE.r2;
                if(b == 0x0A) termination_stage = TERMINATION_STAGE.r1;
                if(b != 0x0D && b != 0x0A) termination_stage = TERMINATION_STAGE.reset;
                break;
            case r2:
                if(b == 0x0A) return true;
                if(b == 0x0D) termination_stage = TERMINATION_STAGE.r1;
                else termination_stage = TERMINATION_STAGE.reset;
                break;
        }
        return false;
    }

    public static byte[] addForwardHeader(byte[] request, String clientIP){
        if(request == null) return null;
        StringBuffer returnVar = new StringBuffer(new String(request));
        returnVar = new StringBuffer(returnVar.toString().replaceFirst("X-Forwarded-For: ","X-Forwarded-For: "+clientIP));
        if(!returnVar.toString().contains("X-Forwarded-For: ")){
            int index = 0;
            while(returnVar.charAt(index) != '\n') {//AOOBE
                index++;
            }
            index++;//go just past the CRLF
            returnVar.insert(index, "X-Forwarded-For: "+clientIP+'\n');
        }
        return returnVar.toString().getBytes();
    }
}