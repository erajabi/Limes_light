package Main;

import de.uni_leipzig.simba.organizer.LimesOrganizer;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import javafx.event.ActionEvent;
import se.mbaeumer.fxmessagebox.MessageBox;
import se.mbaeumer.fxmessagebox.MessageBoxResult;
import se.mbaeumer.fxmessagebox.MessageBoxType;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;


public class TasksController extends mainController{
    @FXML public ComboBox<String> algorithmCombo;
    @FXML public TextField sourceRestrictionText;
    @FXML public TextField targetRestrictionText;
    public Label errorLabel;
    public Label errorLabel1;
    public Label errorLabel3;
    public Label errorLabel2;
    public ComboBox <String> thresholdCombo;
    public Pane mainPane;
    public Label errorLabelThreshold;
    @FXML public static TableView<Task> taskTable;
    @FXML private static ObservableList<Task> taskData;
    @FXML public TextField taskNameText;

    @FXML protected ComboBox<String> sourcePrefixCombo;
    @FXML protected TextField sourcePropertyText;
    @FXML protected TextField targetPropertyText;
    @FXML protected ComboBox<String> targetPrefixCombo;
    @FXML protected ComboBox<String> targetDatasetCombo;
    @FXML protected ComboBox<String> sourceDatasetCombo;
    @FXML protected TextArea outputInterlinking;
    public ArrayList<String> thresholdList=new ArrayList<String>();
    private int modifiedCell=-1;
    private IntegerProperty index = new SimpleIntegerProperty();

    protected  double getIndex() {
        return index.get();
    }
    protected  void setIndex(Integer value) {
        index.set(value);
    }
    public IntegerProperty indexProperty() {
        return index;
    }


    //************** Initializing the pane *********************************
    public void initialize() {
        hideErrors();
        algorithmCombo.getItems().setAll("trigrams","levenshteinDistance");
        loadTasksIntoTable(taskConfigFile);
        final ObservableList<Task> taskSelected = taskTable.getSelectionModel().getSelectedItems();
        taskSelected.addListener(selectedTaskInTable);
    }

    // Delete a row
    public void handleDeleteAction(ActionEvent event) {
        hideErrors();
        setIndex(modifiedCell);
        if(index.get()<0){
            System.out.println("Wrong row!");
            MessageBox mb = new MessageBox("Wrong row selected!", MessageBoxType.OK_ONLY);
            mb.showAndWait();
            return;

        }
        MessageBox mb = new MessageBox("Do you want really to delete?", MessageBoxType.YES_NO);
        mb.showAndWait();
        if (mb.getMessageBoxResult() == MessageBoxResult.YES){
            taskData.remove(index.get());
            saveTask(taskConfigFile);
        }else{
            System.out.println("Cancel");
        }
    }

    // Clear the controls
    public void handleClearAction(ActionEvent event) {
        clearComponents();
    }

    // Clear the controls
    public void hideErrors() {
        errorLabel.setVisible(false);
        errorLabel1.setVisible(false);
        errorLabel2.setVisible(false);
        errorLabel3.setVisible(false);
        errorLabelThreshold.setVisible(false);
    }
    // Clear the controls
    public void showErrors() {
        errorLabel.setVisible(true);
        errorLabel1.setVisible(true);
        errorLabel2.setVisible(true);
        errorLabel3.setVisible(true);
        errorLabelThreshold.setVisible(true);
    }

    //Checks if the component is empty
    private boolean isEmptyComponents(){
        if(taskNameText.getText().isEmpty() || sourceDatasetCombo.getValue()==null || thresholdCombo.getValue()==null  || targetDatasetCombo.getValue()==null  || sourcePrefixCombo.getValue()==null
                || targetPrefixCombo.getValue()==null  || sourcePropertyText.getText().isEmpty()|| targetPropertyText.getText().isEmpty() || algorithmCombo==null || thresholdCombo.getValue()==null){
            return true;
        }
        return false;
    }

    //Clear components
    private void clearComponents(){
        taskNameText.setText("");
        //algorithmCombo.setValue("trigrams");
        algorithmCombo.getItems().setAll("trigrams","levenshteinDistance");
        sourceRestrictionText.setText("");
        targetRestrictionText.setText("");
        sourcePropertyText.setText("");
        targetPropertyText.setText("");
        sourceDatasetCombo.setValue("");
        targetDatasetCombo.setValue("");
        sourcePrefixCombo.setValue("");
        targetPrefixCombo.setValue("");
        thresholdCombo.setValue("");
    }

    //************** Handle add button *********************************
    public void handleAddAction(ActionEvent e) {
        if(isEmptyComponents()){
            System.out.println("Components are empty");
            showErrors();
        } else if(findDuplicateTasks(taskNameText.getText())){
            errorLabel.setVisible(true);
        }
       else{
            Task taskObj=new Task(taskNameText.getText(), sourceDatasetCombo.getValue(),targetDatasetCombo.getValue(),sourcePrefixCombo.getValue()
                    ,targetPrefixCombo.getValue(),sourcePropertyText.getText(),targetPropertyText.getText(),sourceRestrictionText.getText(),targetRestrictionText.getText(), algorithmCombo.getValue(), thresholdCombo.getValue());
            taskData = taskTable.getItems();
            taskData.add(taskObj);
            saveTask(taskConfigFile);
            clearComponents();
            hideErrors();
       }
    }

    //************** Handle update button *********************************
    public void handleUpdateAction(ActionEvent e) {
        hideErrors();
        if(  modifiedCell<0){
            // new AlertDialog(stage, "Empty prefix!", AlertDialog.ICON_INFO).showAndWait();
            return;
        }
        else{
            Task taskObj=new Task(taskNameText.getText(), sourceDatasetCombo.getValue(),targetDatasetCombo.getValue(),sourcePrefixCombo.getValue()
                    ,targetPrefixCombo.getValue(),sourcePropertyText.getText(),targetPropertyText.getText(),sourceRestrictionText.getText(),targetRestrictionText.getText(),algorithmCombo.getValue(),thresholdCombo.getValue());
            taskData.set(modifiedCell, taskObj);
            saveTask(taskConfigFile);
            //handleUpdateData();
        }
    }

    //************** Load tasks into the table *********************************
    private void loadTasksIntoTable(String Filename){
        taskData = taskTable.getItems();
        try{
            JSONParser parser = new JSONParser();
            JSONArray a = (JSONArray) parser.parse(new FileReader(Filename));
            for (Object o : a)
            {
                JSONObject taskS = (JSONObject) o;
                String taskName = (String) taskS.get("taskName");
                String sourceProperty = (String) taskS.get("sourceProperty");
                String targetProperty = (String) taskS.get("targetProperty");
                String sourceDataset = (String) taskS.get("sourceDataset");
                String targetDataset = (String) taskS.get("targetDataset");
                String sourcePrefix = (String) taskS.get("sourcePrefix");
                String targetPrefix = (String) taskS.get("targetPrefix");
                String sourceRestriction = (String) taskS.get("sourceRestriction");
                String targetRestriction = (String) taskS.get("targetRestriction");
                String algorithmType = (String) taskS.get("algorithmType");
                String thresholdValue = (String) taskS.get("threshold");
                taskData.add(new Task(taskName,sourceDataset,targetDataset,sourcePrefix,targetPrefix,sourceProperty,targetProperty,sourceRestriction,targetRestriction,algorithmType,thresholdValue));
            }
        }catch (ParseException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }
        //thresholdList={"0.75,0.80,0.85,0.90,0.95,1"}
        thresholdCombo.getItems().addAll("0.75","0.80","0.85","0.90","0.95","1");
    }

    //************** Refresh comboboxes (prefixes) *********************************
    public void handleRefreshPrefixComboBox(){
        prefixList.clear();
        try{
            JSONParser parser = new JSONParser();
            JSONArray a = (JSONArray) parser.parse(new FileReader(prefixConfigFile));
            for (Object o : a)
            {
                JSONObject dataset = (JSONObject) o;
                prefixList.add((String) dataset.get("prefixName"));
            }
        }catch (ParseException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }
        sourcePrefixCombo.getItems().setAll(prefixList);
        targetPrefixCombo.getItems().setAll(prefixList);
    }

    //************** Refresh comboboxes (datasets)*********************************
    public void handleRefreshDatasetComboBox(){
        datasetList.clear();
        try{
            JSONParser parser = new JSONParser();
            JSONArray a = (JSONArray) parser.parse(new FileReader(datasetConfigFile));
            for (Object o : a)
            {
                JSONObject dataset = (JSONObject) o;
                datasetList.add((String) dataset.get("datasetName"));
            }
        }catch (ParseException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }
        sourceDatasetCombo.getItems().setAll(datasetList);
        targetDatasetCombo.getItems().setAll(datasetList);
    }

    //save tasks in JSON
    public static void saveTask(String fileName){
        int prefixNumbers=taskTable.getItems().toArray().length;
        try{
            BufferedWriter bw=new BufferedWriter(new FileWriter(fileName, false));
            bw.write("[");
            for(int i=0;i<prefixNumbers;i++){
                try{
                    JSONObject obj=new JSONObject();
                    obj.put("taskName", taskTable.getItems().get(i).getTaskName());
                    obj.put("sourceProperty", taskTable.getItems().get(i).getSourceProperty());
                    obj.put("targetProperty", taskTable.getItems().get(i).getTargetProperty());
                    obj.put("sourceDataset", taskTable.getItems().get(i).getSourceDataset());
                    obj.put("targetDataset", taskTable.getItems().get(i).getTargetDataset());
                    obj.put("sourcePrefix", taskTable.getItems().get(i).getSourcePrefix());
                    obj.put("targetPrefix", taskTable.getItems().get(i).getTargetPrefix());
                    obj.put("sourceRestriction", taskTable.getItems().get(i).getSourceRestriction());
                    obj.put("targetRestriction", taskTable.getItems().get(i).getTargetRestriction());
                    obj.put("algorithmType", taskTable.getItems().get(i).getAlgorithmType());
                    obj.put("threshold", taskTable.getItems().get(i).getAcceptedThresholdV());
                    obj.writeJSONString(bw);
                }catch (Exception e){//Catch exception if any
                    System.err.println("Can not write if file. Error: " + e.getMessage());
                }
            }
            bw.write("]");
            bw.close();
        }catch (IOException e){//Catch exception if any
            System.err.println("Can not write if file. Error: " + e.getMessage());
        }
    }

    //Find duplicates
    protected boolean findDuplicateTasks(String taskValue){
        for(int i=0;i<taskData.size();i++)
            if(taskData.get(i).getTaskName().equals(taskValue))
                return true;
        return false;
    }

    //************** Find selected row in the table *********************************
    public Task getTableSelectedData() {
        if (taskTable != null) {
            List<Task> tabla = taskTable.getSelectionModel().getSelectedItems();
            if (tabla.size() == 1) {
                final Task selectedRow = tabla.get(0);
                return selectedRow;
            }
        }
        return null;
    }
    private final ListChangeListener<Task> selectedTaskInTable =
            new ListChangeListener<Task>() {
                @Override
                public void onChanged(Change<? extends Task> c) {
                    PutModifiedRecord();
                }
            };
     private void PutModifiedRecord() {
        final Task taskS = getTableSelectedData();
        modifiedCell = taskData.indexOf(taskS);

        if (taskS != null) {
            taskNameText.setText(taskS.getTaskName());
            sourceDatasetCombo.setValue(taskS.getSourceDataset());
            targetDatasetCombo.setValue(taskS.getTargetDataset());
            sourcePrefixCombo.setValue(taskS.getSourcePrefix());
            sourcePrefixCombo.getSelectionModel().select(taskS.getSourcePrefix());
            targetPrefixCombo.setValue(taskS.getTargetPrefix());
            sourcePropertyText.setText(taskS.getSourceProperty());
            targetPropertyText.setText(taskS.getTargetProperty());
            sourceRestrictionText.setText(taskS.getSourceRestriction());
            targetRestrictionText.setText(taskS.getTargetRestriction());
            algorithmCombo.setValue(taskS.getAlgorithmType());
            thresholdCombo.setValue(taskS.getAcceptedThresholdV());
        }
    }

    //************** Running the task *********************************
    public void handleRunAction(ActionEvent e){
        if(modifiedCell<0)
            return;
         Task interlinkingParameters=new Task(taskNameText.getText(), sourceDatasetCombo.getValue(),targetDatasetCombo.getValue(),sourcePrefixCombo.getValue()
                 ,targetPrefixCombo.getValue(),sourcePropertyText.getText(),targetPropertyText.getText(),sourceRestrictionText.getText(),targetRestrictionText.getText(),algorithmCombo.getValue(),thresholdCombo.getValue());
         createConfigurationFile(interlinkingParameters);
         handleTaskOutput(interlinkingParameters);
     }

    //************** Creating config file *********************************
    void createConfigurationFile(Task interlinkingParameters){
        try{
            BufferedWriter bw=new BufferedWriter(new FileWriter("config/"+interlinkingParameters.getTaskName()+".xml", false));
            bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            bw.write("<!DOCTYPE LIMES SYSTEM \"limes.dtd\">\n");
            bw.write("<LIMES>\n");

            JSONParser parser = new JSONParser();
            JSONArray a = (JSONArray) parser.parse(new FileReader(prefixConfigFile));
            for (Object o : a)
            {
                JSONObject person = (JSONObject) o;
                String prefixName = (String) person.get("prefixName");
                String prefixAddress = (String) person.get("prefixAddress");
                bw.write("   <PREFIX>\n");
                bw.write("      <NAMESPACE>");
                bw.write(prefixAddress);
                bw.write("</NAMESPACE>\n");
                bw.write("      <LABEL>");
                bw.write(prefixName);
                bw.write("</LABEL>\n");
                bw.write("   </PREFIX>\n");
            }
            Dataset sourceDataset=findDatasetAddress(interlinkingParameters.getSourceDataset());
            bw.write("   <SOURCE>\n");
            bw.write("      <ID>");
            bw.write(sourceDataset.getDatasetName());
            bw.write("</ID>\n");
            bw.write("      <ENDPOINT>");
            bw.write(sourceDataset.getEndpointAddress());
            bw.write("</ENDPOINT>\n");
            bw.write("      <VAR>?x</VAR>\n");
            bw.write("      <PAGESIZE>1000</PAGESIZE>\n");
            bw.write("      <RESTRICTION>");
            bw.write(interlinkingParameters.getSourceRestriction());
            bw.write("</RESTRICTION>\n");
            bw.write("      <PROPERTY>");
            bw.write(interlinkingParameters.getSourcePrefix()+":"+interlinkingParameters.getSourceProperty());
            bw.write("</PROPERTY>\n");
            if(sourceDataset.getEndpointType().equals("RDF_Dump")){
                bw.write("      <TYPE>");
                bw.write(sourceDataset.getDataDumpFormat());
                bw.write("</TYPE>\n");
            }
            bw.write("   </SOURCE>\n");
            Dataset targetDataset=findDatasetAddress(interlinkingParameters.getTargetDataset());
            bw.write("   <TARGET>\n");
            bw.write("      <ID>");
            bw.write(targetDataset.getDatasetName());
            bw.write("</ID>\n");
            bw.write("      <ENDPOINT>");
            bw.write(targetDataset.getEndpointAddress());
            bw.write("</ENDPOINT>\n");
            bw.write("      <VAR>?y</VAR>\n");
            bw.write("      <PAGESIZE>1000</PAGESIZE>\n");
            bw.write("      <RESTRICTION>");
            bw.write(interlinkingParameters.getTargetRestriction());
            bw.write("</RESTRICTION>\n");
            bw.write("      <PROPERTY>");
            bw.write(interlinkingParameters.getTargetPrefix()+":"+interlinkingParameters.getTargetProperty());
            bw.write("</PROPERTY>\n");
            if(targetDataset.getEndpointType().equals("RDF_Dump")){
                bw.write("      <TYPE>");
                bw.write(targetDataset.getDataDumpFormat());
                bw.write("</TYPE>\n");
            }
            bw.write("   </TARGET>\n");

            bw.write("   <METRIC>");
            if(interlinkingParameters.getAlgorithmType().equals("trigrams")){
                bw.write(interlinkingParameters.getAlgorithmType()+"(x."+interlinkingParameters.getSourcePrefix()+":"+interlinkingParameters.getSourceProperty()
                    +",y."+interlinkingParameters.getTargetPrefix()+":"+interlinkingParameters.getTargetProperty()+")");
                //trigrams(x.skos:prefLabel,y.dcterms:subject)
                //levenshteinDistance(x.skos:prefLabel,y.dcterms:subject)>0.9
            } else if(interlinkingParameters.getAlgorithmType().equals("levenshteinDistance")){
                bw.write(interlinkingParameters.getAlgorithmType()+"(x."+interlinkingParameters.getSourcePrefix()+":"+interlinkingParameters.getSourceProperty()
                        +",y."+interlinkingParameters.getTargetPrefix()+":"+interlinkingParameters.getTargetProperty()+")>"+interlinkingParameters.getAcceptedThresholdV());
            }
            bw.write("</METRIC>\n");

            bw.write("   <ACCEPTANCE>\n");
            bw.write("      <THRESHOLD>");
            bw.write(interlinkingParameters.getAcceptedThresholdV());
            bw.write("</THRESHOLD>\n");
            bw.write("      <FILE>");
            bw.write("../"+resultFolder+interlinkingParameters.getTaskName()+"_accepted.nt");
            bw.write("</FILE>\n");
            bw.write("      <RELATION>");
            bw.write("owl:sameAs");
            bw.write("</RELATION>\n");
            bw.write("   </ACCEPTANCE>\n");

            bw.write("   <REVIEW>\n");
            bw.write("      <THRESHOLD>");
            bw.write("0.75");
            bw.write("</THRESHOLD>\n");
            bw.write("      <FILE>");
            bw.write("../"+resultFolder+interlinkingParameters.getTaskName()+"_reviewed.nt");
            bw.write("</FILE>\n");
            bw.write("      <RELATION>");
            bw.write("owl:sameAs");
            bw.write("</RELATION>\n");
            bw.write("   </REVIEW>\n");

            bw.write("   <EXECUTION>");
            bw.write("nt");
            bw.write("</EXECUTION>\n");
            bw.write("   <OUTPUT>");
            bw.write("nt");
            bw.write("</OUTPUT>\n");
            bw.write("</LIMES>\n");

            bw.close();
        }catch (ParseException parseE) {
            parseE.printStackTrace();
        }catch (IOException IOe){
            System.err.println("Can not write if file. Error: " + IOe.getMessage());
        }

    }

    //Find duplicates datasets
    Dataset findDatasetAddress(String datasetName){
        try{
            JSONParser parser = new JSONParser();
            JSONArray a = (JSONArray) parser.parse(new FileReader(datasetConfigFile));
            for (Object o : a)
            {
                JSONObject dataset = (JSONObject) o;
                if(dataset.get("datasetName").equals(datasetName)) {
                    Dataset datasetObj=new Dataset ((String)dataset.get("datasetName"),(String)dataset.get("endpointType"),(String)dataset.get("endpointAddress"),(String)dataset.get("dataDumpFormat"));
                    return datasetObj;
                }
            }
        }catch (ParseException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    //****************** Writing output in the textArea
    void handleTaskOutput(Task interlinkingParameters){
        try{
            Random rand=new Random();
            Stage primaryStage1=new Stage();
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("taskOutput.fxml"));
            Parent taskOutput =(Parent) fxmlLoader.load();
            outputController controller = fxmlLoader.<outputController>getController();
            controller.setCurrentTask(interlinkingParameters);
            Scene OutputScene = new Scene(taskOutput, 650, 500);
            primaryStage1.setTitle("Task page: "+interlinkingParameters.getTaskName());

            int  n = rand.nextInt(50) + 1;
            primaryStage1.setX(n);
            primaryStage1.setY(n);
            primaryStage1.setScene(OutputScene);
            primaryStage1.setResizable(false);
            primaryStage1.show();
        }
        catch(IOException f){
            System.out.print(f.getMessage());
        }
    }

}
