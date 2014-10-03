import junit.framework.TestCase;
import org.junit.Test;

public class TestMain extends TestCase{

    @Test
    public void testParseHost(){
        String l1 = "GET http://www.reddit.com/r/random HTTP/1.1";
        String l2 = "Host: www.reddit.com";
        String l3 = "Proxy-Connection: keep-alive";
        String l4 = "Cache-Control: max-age=0";
        String[] sampleRequest = new String[] {l1,l2,l3,l4};
        assertTrue(Server.parseHost(sampleRequest).equals("www.reddit.com"));
    }
}