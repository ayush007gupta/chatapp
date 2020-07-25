package server;
import java.sql.DriverManager;
import java.sql.Connection;
import java.util.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import javafx.util.Pair;
import java.sql.Connection;
public class Server 
{
    ArrayList<Pair<String,Socket>> activelist;
    ArrayList<Pair<ObjectInputStream,ObjectOutputStream>> activeUserStreams;
    MessageManager msh;
    ArrayList<ClientHandler> handlers;
    static Connection connection;
    public static void main(String[] args) throws SQLException, IOException {
        try{
          
             Class.forName("com.mysql.jdbc.Driver");//load the driver run the static block og deiver classs    
             System.out.println("driver loaded"); 
            }
          catch(ClassNotFoundException e){
            
             System.out.println("not loaded");
             e.printStackTrace();
             }
      
            //load the database i.e create connection 
             Server s=new Server();
             s.handlers=new ArrayList<>();
             s.activelist=new ArrayList<Pair<String,Socket>>();
             s.activeUserStreams=new  ArrayList<Pair<ObjectInputStream, ObjectOutputStream>>();
             s.msh=new MessageManager(s);
        
          
                 try
                 {
                 String url="jdbc:mysql://127.0.0.1:3307/chatapp"+"autoReconnect=true&useSSL=false";
                
                 
                 Connection connection=DriverManager.getConnection(url," root","");
                 
                 System.out.println("connection to database recieved");
                 s.msh.connection=connection;
                 }
                 catch(SQLException e)
                 { 
                      e.printStackTrace();
                 } 
        
        
         try
         {
           //System.exit(1);
            ServerSocket ss=new ServerSocket(4000);
            while(true)
            {
              Socket sc=ss.accept();
              ObjectOutputStream oos = new ObjectOutputStream(sc.getOutputStream());
              ObjectInputStream ois = new ObjectInputStream(sc.getInputStream());
        
             // create an object to deal with client
              ClientHandler au=new ClientHandler(sc,s,s.msh,oos,ois,connection);
              Thread t=new Thread(au);
              t.start();
             //  System.exit(1);
            }
          }catch(Exception e)
          {
           e.printStackTrace();
          }
   
    }
    
}
