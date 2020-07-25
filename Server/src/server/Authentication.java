
package server;
import java.io.Serializable;

public class Authentication implements Serializable{
    
boolean auth;
 String Error;
   public Authentication(boolean b, String s) {
      auth=b;
      Error=s;
    }
}
