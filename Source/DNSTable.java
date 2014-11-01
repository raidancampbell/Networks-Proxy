import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Hashtable;

/**
 * Created by aidan on 11/1/14.
 */
public class DNSTable {

    Hashtable<String,DNSEntry> lookupTable;

    public DNSTable(){
        lookupTable = new Hashtable<String, DNSEntry>();
    }

    public DNSEntry query(String hostname){
        if(lookupTable.contains(hostname)){
            //already in table.  Check if it's expired.
            if(lookupTable.get(hostname).isExpired()){
                lookupTable.remove(hostname);//it's expired. delete the old one and put a new one
                return lookupTable.put(hostname, new DNSEntry(hostname));
            } //not expired. return the entry.
            else return lookupTable.get(hostname);
        } else{//not in table. add to table.
            return lookupTable.put(hostname, new DNSEntry(hostname));
        }
    }


    public class DNSEntry {
        Date createdOn;
        Date expiresOn;
        InetAddress address;
        String hostname;

        public DNSEntry(String givenhostname) {
            createdOn = new Date(System.currentTimeMillis());
            expiresOn = new Date(System.currentTimeMillis()+30000);//expires 30 seconds after creation
            try {
                hostname = givenhostname;
                address = InetAddress.getByName(hostname);

            } catch (UnknownHostException e) {
                System.err.println("Error! host " + hostname + " not found!");
            }
        }

        public boolean isExpired(){
            return new Date(System.currentTimeMillis()).after(expiresOn);
        }

    }//end of DNS entry
}//end of DNS Table
