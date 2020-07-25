
package chat_app;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;

public class LoginWindow extends Application {
   Stage window;
   FXMLLoader loader;
   LoginWindowController controller;
   AnchorPane display;
    
    @Override
    public void start(Stage primaryStage) throws Exception
    {
       loader=new FXMLLoader(getClass().getResource("LoginWindow.fxml"));
       display=loader.load();
       controller=loader.getController();
       primaryStage.setScene(new Scene(display));
       controller.window=primaryStage;
       window=primaryStage;
       window.setTitle("log in");
       window.show();
       
    }

     public static void main(String[] args) {
        launch(args);
    }
    
}
