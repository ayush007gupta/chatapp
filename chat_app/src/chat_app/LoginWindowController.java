package chat_app;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import  javafx.geometry.*;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class LoginWindowController 
{
   
    Socket socket;
    @FXML
    TextField name;
    @FXML
    TextField pass;
   
    int type;
    public Stage window;
     
    public void Signup()
    {
         try{
             Signup signup=new Signup();
             signup.start(window);
         }
         catch(Exception e)
         {
             e.printStackTrace();
         }
    }   
    
    public void login()throws Exception
    {
       window=(Stage)name.getScene().getWindow();//setting up stage
       socket=new Socket("127.0.0.1",4000);
       System.out.println("connected ");
       user data =new user(name.getText(),pass.getText());
       ObjectOutputStream oos =new  ObjectOutputStream(socket.getOutputStream()); 
       oos.writeObject(data);
       oos.flush();
       System.out.println("waiting for approval");
       ObjectInputStream ois=new ObjectInputStream(socket.getInputStream());
       
        // till here we passed a steam and connected to server
        // now we read from srver  
      
      try
      {
          System.out.println("reading");
          Object temp=ois.readObject();
          // return object only have to typecast it back
          // authentication should have serializable interface for object convert  
          Authentication a=(Authentication)temp;
          // here we recieve signal from server of autentiacation
          if(a.auth)
          {
              // if user valid then open chat window
              Chat_App_Window c=new Chat_App_Window();
              c.oos=oos;     // passing streams
              c.ois=ois;
              c.socket=socket;
              c.username=name.getText();
              c.start(window);//pasing stage
          }
          else 
          {
            // predefined in javafx to show dialog box for alert
              Alert alert=new Alert(Alert.AlertType.INFORMATION);
              alert.setTitle("failed login");
              alert.setContentText(a.Error);
              alert.setHeaderText("please check details");
              alert.show();
          }
      }  
      catch(Exception e)
      {
          System.out.println("class not found or some other problem");
      }
    } 
}
