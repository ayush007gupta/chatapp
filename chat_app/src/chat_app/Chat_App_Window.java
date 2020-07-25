
package chat_app;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.Connection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import java.sql.*;
import java.util.ArrayList;
import server.*;

public class Chat_App_Window extends Application{
 Stage window;
 Socket socket;
 ObjectInputStream ois;
 ObjectOutputStream oos;
 FXMLLoader loader;
 AnchorPane display;
 ChatWindowController controller;
 ClientReciever reciever;
 Connection connection;
 String username;

    @Override
    public void start(Stage primaryStage) throws Exception {
       
     loader =new FXMLLoader(getClass().getResource("ChatWindow.fxml"));
     display=(AnchorPane)loader.load();
     
     controller=loader.getController();
     //load fxml file and attach controller
     
     //attach the database
     Class.forName("com.mysql.jdbc.Driver");
     String url="jdbc:mysql://127.0.0.1:3306/chatapp";//for url of server
     
     // to set connection eith database
     
     connection=DriverManager.getConnection(url,"root","password");
     // we need to start a new thread which wiill recieve msg from server
      reciever=new ClientReciever();
      reciever.ois=ois;
      reciever.oos=oos;
      reciever.controller=controller;// passing controller class object
      reciever.connection=connection;
      reciever.username=username;
      //client reciever
       controller.socket=socket;
       controller.ois=ois;
       controller.oos=oos;
       controller.connection=connection;
       controller.username=username;
       //friend active list
       controller.FriendStatus=new ArrayList<>();
       controller.refresh();
       // 
      // controller.currentstage=primaryStage;
       //controller.WindowPane=dispaly;
       
       //start thread
       Thread t=new Thread(reciever);
       t.start();
       // window appears
       primaryStage.setTitle("Chat window");
       primaryStage.setScene(new Scene(display));
       primaryStage.show();
    } 

   }
