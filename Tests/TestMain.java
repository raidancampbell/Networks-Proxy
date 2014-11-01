import junit.framework.TestCase;

public class TestMain extends TestCase{

    public void testParseHost(){
        String s = "GET http://www.reddit.com/r/random HTTP/1.1\n";
        s += "Host: www.reddit.com\n";
        s += "Proxy-Connection: keep-alive\n";
        s += "Cache-Control: max-age=0\n";
        byte[] data = s.getBytes();
        assertTrue(HeaderEditor.parseHost(data).equals("www.reddit.com"));

        s = "GET http://www.reddit.com/r/random HTTP/1.1\n";
        s += "Host: www.reddit.com:80\n";
        s += "Proxy-Connection: keep-alive\n";
        s += "Cache-Control: max-age=0\n";
        data = s.getBytes();
        assertTrue(HeaderEditor.parseHost(data).equals("www.reddit.com"));

        s = "GET http://api.mywot.com/0.4/query?target=46tlYBx6XEIEP5pMNUosqpY%3D&id=18fea7a5518ed7b8c85fb175768555cfc16432&nonce=7eec2c863de98c271115693fa2f3c3d3789eff3&lang=en-US&version=chrome-20140612&auth=721723ae0969d625c01eb07a958ad3bdb303a7b HTTP/1.1\n" +
                "Host: api.mywot.com\n" +
                "Proxy-Connection: keep-alive\n" +
                "Accept: application/xml, text/xml, */*; q=0.01\n" +
                "User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/38.0.2125.104 Safari/537.36\n" +
                "Accept-Encoding: gzip,deflate,sdch\n" +
                "Accept-Language: en-US,en;q=0.8\n" +
                "Cookie: SESSf6ce7e3db235723091e5a653e7d96f2=f0idvludhe8mfu344rar6gmsd0; wot_lang=en; ss_activity_info=1398915203%3Aversion_ab3%3A0%3A0%3A0; authid=18fea47a5518ed7b8c8fb5175768555cfc16432; __utma=90529926.1105390895.139895198.1398915198.1407759358.2; __utmc=90529926; __utmz=90529926.1407759358.2.2.utmcsr=addon|utmccn=(not%20set)|utmcmd=(not%20set)|utmcct=contextmenu\n" +
                "\n" +
                "mz=90529926.1407759358.2.2.utmcsr=addon|utmccn=(not%20set)|utmcmd=(no\n";
        data = s.getBytes();
        assertTrue(HeaderEditor.parseHost(data).equals("api.mywot.com"));
    }
}