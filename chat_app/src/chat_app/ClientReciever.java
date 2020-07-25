
package chat_app;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import java.sql.*;
import java.sql.Timestamp;
import java.sql.Connection;

public class ClientReciever implements Runnable{
    
  ObjectInputStream ois;   
  String username;
  public  ChatWindowController controller;
  Connection connection;
 // only // ois //   as it process only input    
    ObjectOutputStream oos;
    
 @Override
    public void run()
    {  
       //accept msg from client handler
        while(true)
        {
            Object obj=null;
            try {
                obj=ois.readObject();
            } catch (Exception ex) {
                ex.printStackTrace();
                return;
            }
            //now check instance of obj;
            
            if(obj instanceof Message)
            {
              Message temp=(Message)obj;
              temp.setRecievedTime(new Timestamp(System.currentTimeMillis()));
              // insert into local datbase  
              String q="INSERT INTO Local"+username+"Chats VALUES('"+(temp.getFrom())+"','"+(temp.getTo())+"','"+(temp.getContent())+"',"+(temp.getSentTime()==null?"null":("'"+temp.getSentTime()+"'"))+","+(temp.getRecievedTime()==null?"'2019-01-01 00:00:00'":("'"+temp.getRecievedTime()+"'"))+","+(temp.getSeenTime()==null?"'2019-01-01 00:00:00'":("'"+temp.getSeenTime()+"'"))+")"; 
              try {
                    PreparedStatement ps = connection.prepareStatement(q);
                    ps.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                } 
              // if suppose we are currently in chat with sender of message
              if(temp.getFrom().equals(controller.currentUser.getText()))
              {
                  // we have add to display but on a background thread
                          Platform.runLater(new Runnable()
                          {
                             public void run()
                             {
                                 try {     
                                     controller.addMessageToDisplay(temp);
                                 } catch (IOException ex) {
                                     Logger.getLogger(ClientReciever.class.getName()).log(Level.SEVERE, null, ex);
                                 }
                                    controller.seenMessageof(temp.getFrom());
                                 
                             }
                          }  
                          );
              } 
              // addd chat to chats arraylist in controller
              controller.chats.add(temp);
              // if suppose we have no previous chat with the user
              if(!controller.friends.contains(temp.getFrom()))
              {
                  Platform.runLater(new Runnable()
                  {
                      public void run()
                      {
                          controller.addChat(temp.getFrom());
                      }   
                  });  
              }
          }
           // object of Message over recieved message update in sql 
            else if(obj instanceof SystemMessage)
            {
                SystemMessage temp=(SystemMessage)obj;
                
                if(temp.valid==-3)
                {
                    //some new user is added logged in
                    int flag=0;
                    for(int i=0;i<controller.FriendStatus.size();i++)
                    {   
                     if(controller.FriendStatus.get(i).getUser().equals(temp.sender))
                     {
                             flag=1;
                             controller.FriendStatus.get(i).setValid(1);
                             break;
                     }
                   }//for some user if they had chat in past
                    if(flag==0)
                      controller.FriendStatus.add(new Status(temp.sender,temp.time,1));  
                   
                    // to mange ui from background thread
                    // if suppose we are viewing our previos chats and user comes online
                   if(controller.currentUser.getText().equals(temp.sender))
                   {
                         Platform.runLater(new Runnable()
                         {
                            
                             public void run()
                             {
                                 controller.currentUserStatus.setText("online");
                             }      
                         });     
                   }   
              }// log in over
                else if(temp.valid==-2)
                {
                    //for all user in its list make it offline
                    for(int i=0;i<controller.FriendStatus.size();i++)
                    {
                        if(controller.FriendStatus.get(i).getUser().equals(temp.sender))
                        {
                           // flag=1;
                            controller.FriendStatus.get(i).setValid(0);
                            controller.FriendStatus.get(i).setTime(temp.time);
                            break;
                        }
                    }
                    // if suppose user is having chat and it becomes offline
                    if(controller.currentUser.getText().equals(temp.sender))
                    {
                        Platform.runLater(new Runnable()//To perform UI work from different Thread
                        {
                            @Override
                            public void run() {
                                System.out.println("Changing Status Of CurrentUSer");
                                controller.currentUserStatus.setText("Last Seen "+temp.time.toString());
                            }
                        });
                    }
               }
                else if(temp.valid==1)//someone recieved msg 
                {
                    
                     String q="UPDATE Local"+username+"Chats SET ReceivedTime = '"+temp.time+"' WHERE ReceivedTime = '2019-01-01 00:00:00' AND Receiver ='"+temp.sender+"'";
                    try {
                        PreparedStatement ps = connection.prepareStatement(q);
                        ps.executeUpdate();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                else if(temp.valid==2)//msg seen
                {
                    
                    String q="UPDATE Local"+username+"Chats SET SeenTime = '"+temp.time+"' WHERE    SeenTime = '2019-01-01 00:00:00' AND Receiver='"+temp.sender+"'";
                    try {
                        PreparedStatement ps = connection.prepareStatement(q);
                        ps.executeUpdate();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    
                }
                
                if(temp.valid==1||temp.valid==2)
                {
                    Platform.runLater(new Runnable()//To perform UI work from different Thread
                    {
                        @Override
                        public void run() {
                            System.out.println("Received receiving time refreshing");
                            for(int i=0;i<controller.chats.size();i++)
                            {
                                //System.out.println(controller.chats.get(i).getTo());
                                //System.out.println(temp.sender);
                                //System.out.println(controller.chats.get(i).getFrom());
                                //System.out.println(controller.chats.get(i).getReceivedTime());
                                if(temp.valid==1&&((controller.chats.get(i).getTo().equals(temp.sender)||controller.chats.get(i).getFrom().equals(temp.sender))&&controller.chats.get(i).getRecievedTime()==null))// check for proper object
                                {
                                    System.out.println("changed the value");
                                    controller.chats.get(i).setRecievedTime(temp.time);// update received time in message object
                                }
                                else if(temp.valid==2&&((controller.chats.get(i).getTo().equals(temp.sender)||controller.chats.get(i).getFrom().equals(temp.sender))&&controller.chats.get(i).getSeenTime()==null))// check for proper object
                                {
                                    System.out.println("changed the value");
                                    controller.chats.get(i).setSeenTime(temp.time);// update received time in message object
                                }
                            }
                            System.out.println("Updating Display");
                            controller.display(temp.sender);
                        }
                    });
                }
                
                
                
            }   //for system related message over
            else if(obj instanceof Errors)
            {
                 Errors temp=(Errors) obj;
                System.out.println(temp.errormessage);
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Some Error Occured");
                alert.setHeaderText("Error");
                alert.setContentText(temp.errormessage);
                alert.show();
                
                
                
            }
     
        }  
        
        
        
    }
}
