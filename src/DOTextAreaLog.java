import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class DOTextAreaLog extends Application {

    TextArea log = new TextArea();
    Process p;

    @Override
    public void start(Stage stage) {
        try {
            //ProcessBuilder pb = new ProcessBuilder("ping", "stackoverflow.com", "-n", "100");
              ProcessBuilder pb = new ProcessBuilder("java", "-jar", "LIMES.jar", "Globe2Aalto.xml");

            p = pb.start();

            // this thread will read from process without blocking an application
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {

                        //try-with-resources from jdk7, change it back if you use older jdk
                        InputStreamReader isr = new InputStreamReader(p.getErrorStream());
                        BufferedReader bri = new BufferedReader(isr);
                            String line;
                        System.out.println("here="+bri.readLine());

                        while ((line = bri.readLine()) != null) {
                                log(line);
                            }

                    } catch (IOException ex) {
                        System.out.print("hello1");
                        ex.printStackTrace();

                    }
                }
            }).start();

            stage.setScene(new Scene(new Group(log), 400, 300));
            stage.show();
        } catch (IOException ex) {
            System.out.print("hello2");
            ex.printStackTrace();        }

    }

    @Override
    public void stop() throws Exception {
        super.stop();
        // this called on fx app close, you may call it in user action handler
        if (p!=null ) {
            p.destroy();
        }
    }

    private void log(final String st) {
        // we can access fx objects only from fx thread
        // so we need to wrap log access into Platform#runLater

        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                log.setText(st + "\n" + log.getText());
            }
        });
    }

    public static void main(String[] args) {
        launch();
    }
}