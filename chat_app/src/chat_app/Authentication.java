
package chat_app;

import java.io.Serializable;

class Authentication implements Serializable 
{
 boolean auth;
 String Error;
   public Authentication(boolean b, String s) {
      auth=b;
      Error=s;
    }
    
}
