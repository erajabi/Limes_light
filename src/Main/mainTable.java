package Main;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;


/**
 *
 * @author Narayan
 */
public class mainTable extends Application{

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        launch(args);


    }

    @Override
    public void start(Stage stage) throws Exception {

        AnchorPane pane = (AnchorPane)FXMLLoader.load(getClass().getResource("acceptedLinksTable.fxml"));
        Scene scene=  new Scene(pane);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }
}