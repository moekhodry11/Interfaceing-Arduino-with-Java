
package arduinojava;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 *
 * @author emadeddin
 */


public class ArduinoJava extends Application {
    
    @Override
    public void start(Stage stage) throws Exception {
        
       
        Parent root = FXMLLoader.load(getClass().getResource("FXMLDocument.fxml"));
        String css = this.getClass().getResource("Style.css").toExternalForm();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(css);
        stage.setTitle("Arduino With Java");
        Image icon = new Image("icon.png");
        
        stage.getIcons().add(icon);
        stage.setScene(scene);
        
        stage.setResizable(false);
        
        stage.show();
        
    }

    
    public static void main(String[] args) {
        launch(args);
        
    }
    
    
    
}
