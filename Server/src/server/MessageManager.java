
package server;

import java.io.*;
import java.sql.*;
import java.time.LocalDateTime;



public class MessageManager
{
   Server server;
   ObjectOutputStream oos;
   Connection connection;
  
   public MessageManager(Server s)
   {
       server=s;
   }   
   // if user is offline
   public void insert(String reciever,Object obj) throws SQLException//users =recievers,and obj is message or sysytem mesaage
   {
        int valid=-1;
        String table=null,sender = null,content=null;
        Timestamp time=null;
        if(obj instanceof Message)
        {
            Message m=(Message)obj;
            valid=0;//messaage send
            content=m.getContent();
            time=m.getSentTime();
            sender=m.getFrom();
        } 
        else if(obj instanceof SystemMessage)
        {
            SystemMessage sm=(SystemMessage)obj;
            valid=sm.valid;
            sender=sm.sender;
             time=sm.time;
        }    
       // in database
       String t=reciever+"Table";
       String q="INSERT INTO "+t+"VALUES(?,?,?,?)";
       PreparedStatement pre=connection.prepareStatement(q);
        
        pre.setString(1, sender);
        pre.setInt(2, valid);
        pre.setString(3, content);
        pre.setTimestamp(4, time);
        pre.executeUpdate();
   }     
    // insert new user
   public void insertuser(Signupclass sc) throws SQLException
   {
     // query to add new user
       String q="NSERT INTO usertable Values (?,?,?)";
       PreparedStatement preStat = connection.prepareStatement(q);
        preStat.setString(1, sc.user);
        preStat.setString(2, sc.password);
        preStat.setTimestamp(3,new Timestamp(System.currentTimeMillis()));
        preStat.executeUpdate();
        
        // create a new databse for new user
   
        String table=sc.user+"Table";
        q="CREATE TABLE " + (table) + " ( Sender varchar(11),Valid int(255),Message text(2000),Time timestamp)";
        preStat = connection.prepareStatement(q);
        preStat.executeUpdate();
   }   
   
    public ObjectOutputStream find(String sender)
    {
        int i=0;
        for(i=0;i<server.activelist.size();i++)
        {
            if (server.activelist.get(i).getKey().equals(sender))
            {
                return server.activeUserStreams.get(i).getValue();
            }
        }
        return null;
    }// find the stream
    
   public void remove(String username) throws SQLException, IOException
   {
       String t=username+"Table";
       String q="SELECT * FROM"+t+"";
       PreparedStatement preStat = connection.prepareStatement(q);
       ResultSet result = preStat.executeQuery();
       //
        while (result.next())
        {
            String sender = result.getString("Sender");
            int valid = result.getInt("Valid");
            Timestamp time = result.getTimestamp("Time");
            String content = result.getString("Message");
            if(valid==1||valid==2)//sent msg recieved or seen
           {
            SystemMessage sm=new SystemMessage(sender ,valid, time);
                oos.writeObject(sm);//USER GET INFO OF RECEIVING  AND SEEN TIME
                oos.flush();
            }
            else
          {
              ObjectOutputStream oosTo=find(sender);
              Message ms=new Message(sender,username,content,time,null,null);
              oos.writeObject(ms);
              oos.flush();
               
              LocalDateTime d=LocalDateTime.now();
              time=Timestamp.valueOf(d);
               if (oosTo != null)// if Sender is online
                {
                    System.out.println("User is Active");
                    SystemMessage sm=new SystemMessage(username ,1,time );
                    oosTo.writeObject(sm);//Sender get Info of receiving time
                    oosTo.flush();
                }
                else//if sender is offline
                {
                    SystemMessage sm=new SystemMessage(username,1,time);//Sender get Receiving time of his message
                    insert(sender,sm);
                } 
          }  
       }
        q = "TRUNCATE " + (t) + "";
        preStat = connection.prepareStatement(q);
        preStat.executeUpdate();
   }
   
}



