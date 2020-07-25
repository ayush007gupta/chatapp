
package server;

import javafx.util.Pair;
import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.Serializable;
public class ClientHandler implements Runnable,Serializable{
    
    
    Socket sc;
    Server server;
    ObjectOutputStream oos;
    ObjectInputStream ois;
    MessageManager msh;
    String username, password;
    Connection connection;
    Timestamp time;
    // constructor
    public ClientHandler(Socket so, Server ss, MessageManager ms,ObjectOutputStream oos,ObjectInputStream ois,Connection connection)
    {
        sc = so;
        server=ss;
        msh=ms;
        this.oos=oos;
        this.ois=ois;
        this.connection=connection;
    }
    
     public ObjectOutputStream find(String sender)
    {
        int i=0;
        for(i=0;i<server.activelist.size();i++)//serach nactive users
        {
            if (server.activelist.get(i).getKey().equals(sender))//if found 
            {
                return server.activeUserStreams.get(i).getValue();
            }
        }
        return null;
    }
    
    public void logout(Timestamp t)
    {
        for(int i=0;i<server.activelist.size();i++)
        {
            if(server.activelist.get(i).getKey().equals(username))
            {
                // remove the current user
                server.activelist.remove(i);//To remove user from current list
                server.activeUserStreams.remove(i);
                server.handlers.remove(i);
                break;
            }   
        }
        // broadcast this to other active users
          for(int i=0;i<server.handlers.size();i++)
        {
            server.handlers.get(i).broadcast(username,time,-2);
        } 
          // update usertable last seen sql
        
        String query = "UPDATE usertable SET LastSeen = '"+time+"' WHERE UserName='" + (username) + "'";
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            preStat.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
    }
    class user implements Serializable {

    String password;
    String username;
    public user(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
    
    
    
    @Override
    public void run() {
     
        Object obj = null;
          try {
              obj = ois.readObject();
          } catch (IOException ex) {
              Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
          } catch (ClassNotFoundException ex) {
              Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
          }
       
        if(obj instanceof user)// start for client handler always some user
        {
           user temp=(user)obj;
           username=temp.username;
           password = temp.password;
            System.out.println(username);
            try {
                if(authenticate())
                {
                    msh.oos=oos;
                    msh.remove(username);
                    
                    while(true)//start recieving msg
                    {
                        try {
                            obj=ois.readObject();
                        } catch (IOException ex) {
                            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (ClassNotFoundException ex) {
                            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        
                        // if it is a msg
                        if(obj instanceof Message)
                        {
                            Message ms=(Message)obj;
                            String r=ms.getTo();
                            ObjectOutputStream oosto=find(r);
                            if(oosto!=null)//online user
                            {
                                System.out.println("User is Active");
                                ms.setRecievedTime(ms.getSentTime());
                                oosto.writeObject(ms);
                                try {
                                    oosto.flush();
                                } catch (IOException ex) {
                                    Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
                                }
                                //send the msg;
                                //now notify to user msg delievered
                                SystemMessage sm = new SystemMessage(ms.getTo(), 1, ms.getSentTime());
                                oos.writeObject(sm);
                                try {
                                    oos.flush();
                                } catch (IOException ex) {
                                    Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }else//offline
                            {
                                msh.insert(r,ms);
                            }
                        }
                        // obj of Message is over
                        else if(obj instanceof SystemMessage)
                        {
                            SystemMessage sm = (SystemMessage) obj;
                            if(sm.valid==-1)//request for logout
                            {
                                time=sm.time;//get logout time;
                                break;//claa logout methoud outside while()
                            }
                            String r=sm.sender;
                            
                            ObjectOutputStream oosTo = find(r);
                            System.out.println(oosTo);
                            if (oosTo != null)// IF USER IS ONLINE
                            {
                                System.out.println("User is Active");
                                sm.sender = username;//for person who receives seen receipt will have sender as person who sends this to him
                                oosTo.writeObject(sm);
                                try {
                                    oosTo.flush();
                                } catch (IOException ex) {
                                    Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            } else// IF USER IS OFFLINE
                            {
                                sm.sender = username;//for person who receives seen receipt will have sender as person who sends this to him
                                msh.insert(r, sm);
                            }
                        }// Sytem msg is over message seen
                    }
                    System.out.println("Logging Out");
                    logout(time);
                    try {
                        this.oos.close();
                    } catch (IOException ex) {
                        Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    try {
                        this.ois.close();
                    } catch (IOException ex) {
                        Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    try {
                        this.sc.close();
                    } catch (IOException ex) {
                        Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    return;//closing all streams
                }
            } catch (SQLException ex) {
                Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
          else//if autheniacte() fails
           {
               // sign up option for new user
            Signupclass temp=(Signupclass)obj;
            System.out.println("USER IS SIGN UP");
            try {
                msh.insertuser(temp);
            } catch (SQLException ex) {
                Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
          }     
   }//end of run now write find and logout;
    // for informing other user about this currewnt user oringin from authenticate()
    public void broadcast(String user,Timestamp time,int valid)
    {
        SystemMessage sm = new SystemMessage(user,valid,time);
        try {
            oos.writeObject(sm);
            oos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    

    private boolean authenticate() throws SQLException {
        String query = "SELECT Password FROM usertable WHERE UserName='" + (username) + "'";
        PreparedStatement preStat = connection.prepareStatement(query);
        ResultSet rs = preStat.executeQuery(query);
        if(rs.next())
        {
            String CheckPassword = rs.getString("Password");
            if (CheckPassword.equals(password))
            {
                Authentication auth = new Authentication(true,"Successful");
                try {
                    oos.writeObject(auth);
                    oos.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                server.activelist.add(new Pair<String,Socket>(username,sc));
                server.activeUserStreams.add(new Pair<>(ois,oos));
                server.handlers.add(this);
                String query2 = "SELECT * FROM usertable ";
                PreparedStatement ps = connection.prepareStatement(query2);
                ResultSet rs2 = ps.executeQuery();
                while(rs2.next())
                {
                    try {
                        oos.writeObject(new SystemMessage(rs2.getString("Username"),-2,rs2.getTimestamp("LastSeen")));
                        oos.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
                for(int i=0;i<server.handlers.size();i++)
                {
                    if(!server.handlers.get(i).equals(this))
                    {
                        server.handlers.get(i).broadcast(username,null,-3);
                    }
                }
                for(int i=0;i<server.activelist.size();i++)
                {
                    if(!server.activelist.get(i).getKey().equals(username))
                    {
                        try
                        {
                            oos.writeObject(new SystemMessage(server.activelist.get(i).getKey(),-3,null));
                            oos.flush();
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
                return true;
            }
            else
            {
                Authentication auth = new Authentication(false,"Invalid Login Credientials");
                try {
                    oos.writeObject(auth);
                    oos.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return false;
            }
        }
        else
            return false;
    }
    
    
    
}
