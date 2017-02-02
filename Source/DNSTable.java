import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

/*R. Aidan Campbell
This class is an object meant to be instantiated once, and passed throughout the program
I haven't noticed any multithreaded issues, and everything still seems to function fine

The object, DNSTable, wraps around a hashmap containing the hostname as a string, and
another object, DNSEntry as the value

DNSEntry contains the expiration time, and the InetAddress of the given hostname

There may exist an issue where multiple threads access this at once and possibly cause
duplicate records.  But caching benefits dominate the duplicate query cost.
 */
public class DNSTable {

    private ConcurrentHashMap<String,DNSEntry> lookupTable;

    public DNSTable(){
        lookupTable = new ConcurrentHashMap<String, DNSEntry>();
    }

    DNSEntry query(String hostname){
        if(lookupTable.containsKey(hostname)){
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


    /**
     * helper object to represent the result of a DNS query.
     */
    class DNSEntry {
        Date expirationDate;
        InetAddress address;

        DNSEntry(String givenhostname) {
            expirationDate = new Date(System.currentTimeMillis()+30000);//expires 30 seconds after creation
            try {
                address = InetAddress.getByName(givenhostname);

            } catch (UnknownHostException e) {
                System.err.println("Error! host " + givenhostname + " not found!");
            }
        }

        /**
         * @return whether the entry is expired (30 seconds have passed)
         */
        boolean isExpired(){
            return new Date(System.currentTimeMillis()).after(expirationDate);
        }
    }//end of DNS entry
}//end of DNS Table
