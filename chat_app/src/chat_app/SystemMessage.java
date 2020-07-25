

package chat_app;

import java.io.Serializable;
import java.sql.Timestamp;


class SystemMessage implements Serializable {

    String sender;
    int valid;
    Timestamp time;
    
    public SystemMessage(String s, int i, Timestamp t) {
      sender=s;
      valid=i;
      time=t;
       }

    
}
    

