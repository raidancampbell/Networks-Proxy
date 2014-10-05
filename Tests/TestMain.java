import junit.framework.TestCase;

public class TestMain extends TestCase{

    public void testParseHost(){
        String s = "GET http://www.reddit.com/r/random HTTP/1.1\n";
        s += "Host: www.reddit.com\n";
        s += "Proxy-Connection: keep-alive\n";
        s += "Cache-Control: max-age=0\n";
        byte[] data = s.getBytes();
        assertTrue(Server.parseHost(data).equals("www.reddit.com"));
    }
}