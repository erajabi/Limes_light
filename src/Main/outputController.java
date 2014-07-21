package Main;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.json.simple.JSONObject;
import se.mbaeumer.fxmessagebox.MessageBox;
import se.mbaeumer.fxmessagebox.MessageBoxType;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;
import java.util.Scanner;

/**
 * Created with IntelliJ IDEA.
 * User: Enayat
 * Date: 6/2/14
 * Time: 12:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class outputController extends mainController implements Runnable{
    @FXML
    public TextArea outputTextArea;
    public Label taskNameLabel;
    public TextField taskNameTextbox;
    public Label sourceLabel;
    public Label targetLabel;
    public Button viewAcceptedButton;
    public Button viewLogButton;
    Process process=null;
    protected String mytask;
    protected Task currentTask;
    private String configFileName;
    private boolean finishFlag=false;
    private Thread outputThread=null;
    public Button stopButton;

    // results variables
    private String acceptedNumber=null;
    private String reviewedNumber=null;
    private String processTime=null;
    private String taskResultsFileName;
    private String line;

    public void initialize() {}

    // Handle RUN button
    public void handleRunButton() throws InterruptedException {
        stopButton.setDisable(false);
        try{
            configFileName=configFolder+currentTask.getTaskName()+".xml";
            ProcessBuilder pb = new ProcessBuilder("java", "-jar", "-Xmx1G", "LIMES.jar",configFileName);
            process = pb.start();
            outputThread= new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        InputStreamReader isr = new InputStreamReader(process.getErrorStream());
                        BufferedReader bri = new BufferedReader(isr);
                        while ((line = bri.readLine()) != null) {
                            log(line);
                        }
                        if((line = bri.readLine()) == null){
                            stopButton.setDisable(true);
                            if(new File(resultFolder+currentTask.getTaskName()+"_accepted.nt").exists())
                                viewAcceptedButton.setDisable(false);
                            if(new File(configFolder+currentTask.getTaskName()+".log").exists()){
                                viewLogButton.setDisable(false);
                                readLogFile(configFolder+currentTask.getTaskName()+".log");
                            }
                            outputThread.stop();
                            process.destroy();
                            finishFlag=true;
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        System.out.println("UnExcepted Error");
                    }
                }
            });
            outputThread.start();
        }
        catch (IOException jarFileException){
            System.out.println(jarFileException.getMessage());
        }
    }

    // Writing LIMES' output in the textarea
    private void log(final String st) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                outputTextArea.setText(outputTextArea.getText() + "\n" + st);
                outputTextArea.setScrollTop(Double.MAX_VALUE);
            }
        });
    }

    //Open the log file in NOTEPAD
    public void handleViewLogButtonButton(){
        try{
            ProcessBuilder pb = new ProcessBuilder("Notepad.exe", configFolder+currentTask.getTaskName()+".log");
            pb.start();

        }catch(IOException io){
            System.out.println(io.getMessage());
        }
    }

    // Show accepted table view
    public void handleAcceptedButton(){
            try{
                Random rand=new Random();
                Stage acceptedTableStage=new Stage();
                // Reading the accepted file name
                String acceptedFileName=resultFolder+"/"+currentTask.getTaskName()+"_accepted.nt";
                try{
                    BufferedWriter bw=new BufferedWriter(new FileWriter("temp.cfg"));
                    bw.write(acceptedFileName);
                    bw.close();
                }catch (IOException tempIO){}
                // what if the file is too big
                File acceptedFile=new File(acceptedFileName);
                if(acceptedFile.length()>100000000){//100000000) {
                    System.out.println("File is too big -->"+(acceptedFile.length())/100000000 + " MB. Can not open!");
                    MessageBox mb = new MessageBox("File "+acceptedFile.getName()+" is too big! You can find it in 'results' folder", MessageBoxType.OK_ONLY);
                    mb.showAndWait();
                    return;
                }
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("acceptedLinksTable.fxml"));
                Parent acceptedParent =(Parent) fxmlLoader.load();
                Scene acceptedScene=  new Scene(acceptedParent);
                int  n = rand.nextInt(100) + 1;
                acceptedTableStage.setX(n);
                acceptedTableStage.setY(n);
                acceptedTableStage.setScene(acceptedScene);
                acceptedTableStage.setResizable(false);
                acceptedTableStage.show();
            }
            catch(IOException f){
                System.out.print(f.getMessage());
            }
    }

    // Set current task
    public void setCurrentTask(Task task){
        taskNameLabel.setText(task.getTaskName());
        taskNameLabel.setTextFill(Color.GREEN);
        sourceLabel.setText(task.getSourceDataset());
        sourceLabel.setTextFill(Color.GREEN);
        targetLabel.setText(task.getTargetDataset());
        targetLabel.setTextFill(Color.GREEN);
        outputTextArea.setEditable(false);
        outputTextArea.setStyle("-fx-text-fill: white;" +
                "-fx-background-color: black;" +
                "-fx-font-family: Times;" +
                "-fx-font-size: 12;");
        currentTask=task;
    }

    // When stop button clicks
    @FXML
    private void handleStopButton()throws Exception{
        if (process!=null ) {
            outputThread.stop();
            process.destroy();
            finishFlag=true;
            viewLogButton.setDisable(false);
        }
    }

    @Override
    public void run() {
        outputThread.interrupt();
    }

    // Parse LIMES log file
    void readLogFile(String logFileName) throws IOException {
        Path path = Paths.get(logFileName);
        Scanner scanner =  new Scanner(path, StandardCharsets.UTF_8.name());
        try{
            while (scanner.hasNextLine()){
                String line=scanner.nextLine();
                if((line.toLowerCase().contains(": returned"))){
                    acceptedNumber=line.substring(line.indexOf("Returned") + 9, line.indexOf("links") - 1);
                    line=scanner.nextLine();
                    reviewedNumber=line.substring(line.indexOf("Returned") + 9, line.indexOf("links") - 1);
                    line=scanner.nextLine();
                    processTime=line.substring(line.indexOf("Mapping carried out in") + 23, line.indexOf("seconds") - 1);
                    break;
                }
            }

            try{
                TaskResult taskResult=new TaskResult(currentTask.getTaskName(),acceptedNumber,reviewedNumber,processTime);
                taskResultsFileName=resultFolder+currentTask.getTaskName()+"_results.txt";
                BufferedWriter bw=new BufferedWriter(new FileWriter(taskResultsFileName, false));
                bw.write("[");
                try{
                    JSONObject obj=new JSONObject();
                    obj.put("taskName", taskResult.getProcessedTask());
                    obj.put("acceptedNumber", taskResult.getAcceptedNumber());
                    obj.put("reviewedNumber", taskResult.getReviewedNumber());
                    obj.put("processedTime", taskResult.getprocessedTime());
                    obj.writeJSONString(bw);
                }catch (Exception e){
                    System.err.println("Can not write if file. Error: " + e.getMessage());
                }
                bw.write("]");
                bw.close();
            }catch (IOException e){
                System.err.println("Can not write if file. Error: " + e.getMessage());
            }
        }finally{
            scanner.close();
        }

    }

}

