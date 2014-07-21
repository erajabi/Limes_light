package Main;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import se.mbaeumer.fxmessagebox.MessageBox;
import se.mbaeumer.fxmessagebox.MessageBoxResult;
import se.mbaeumer.fxmessagebox.MessageBoxType;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class DatasetController extends mainController {
    @FXML private prefixController prefixController;
    private static String prefixConfigFile=null;
    @FXML public static TableView<Dataset> datasetTable;
    @FXML private static ObservableList<Dataset> datasetData;
    @FXML private TextField datasetNameText;
    @FXML private ComboBox<String> endpointTypeCombo;
    @FXML private ComboBox<String> dataDumpFormatCombo;
    @FXML private TextField endpointAddressText;
    @FXML private Button browseButton;
    @FXML private Pane paneDatasets;
    @FXML private Label nameErrorLabel;
    @FXML private Label endpointErrorLabel;
    @FXML private Label formatErrorLabel;
    @FXML private Label endpointTypeErrorLabel;
    private String selectedEndpointType=null;
    @FXML
    private String dataDumpFormat=null;
    private int modifiedCell=-1;

    private IntegerProperty index = new SimpleIntegerProperty();

    protected double getIndex() {
        return index.get();
    }

    protected void setIndex(Integer value) {
        index.set(value);
    }

    public IntegerProperty indexProperty() {
        return index;
    }


    public void initialize() {
        loadComponents(datasetConfigFile);
        final ObservableList<Dataset> datasetSelected = datasetTable.getSelectionModel().getSelectedItems();
        datasetSelected.addListener(selectedDatasetInTable);
    }


    public void handleDeleteAction(ActionEvent event) {
        setIndex(modifiedCell);
        if(index.get()>=0){
            MessageBox mb = new MessageBox("Do you want really to delete?", MessageBoxType.YES_NO);
            mb.showAndWait();
            if (mb.getMessageBoxResult() == MessageBoxResult.YES){
                datasetData.remove(index.get());
                setDatasets(datasetConfigFile);
            }
            // new AlertDialog(stage, "Select a row!", AlertDialog.ICON_INFO).showAndWait();
        }

    }

    // When endpoint type is changed
    public void handleEndpointTypeAction(ActionEvent e) {
        if(endpointTypeCombo.getValue().equals("RDF_Dump"))  {
           // browseButton.setVisible(true);
            dataDumpFormatCombo.setDisable(false);
        }
    }

    // Hide error labels
    private void setErrorsHidden(){
        nameErrorLabel.setVisible(false);
        endpointErrorLabel.setVisible(false);
        formatErrorLabel.setVisible(false);
        endpointTypeErrorLabel.setVisible(false);
    }

    //Find duplicate datasets
    protected boolean findDuplicateDatasets(String datasetValue){
        for(int i=0;i<datasetData.size();i++)
            if(datasetData.get(i).getDatasetName().equals(datasetValue))
                return true;
        return false;
    }

    // Add an element to table
    public void handleAddAction(ActionEvent e) {
        if(  datasetNameText.getText().isEmpty() || endpointAddressText.getText().isEmpty() || selectedEndpointType==null){
            // new AlertDialog(stage, "Empty prefix!", AlertDialog.ICON_INFO).showAndWait();
            nameErrorLabel.setVisible(true);
            endpointErrorLabel.setVisible(true);
            endpointTypeErrorLabel.setVisible(true);

            if(selectedEndpointType!=null)
                    if(selectedEndpointType.equals("RDF_Dump"))
                        formatErrorLabel.setVisible(true);
            return;
        }  else if(findDuplicateDatasets(datasetNameText.getText())){
            nameErrorLabel.setVisible(true);
        }
        else{
            Dataset newDataset;
            if(endpointTypeCombo.getValue().equals("SPARQL"))
                newDataset=new Dataset(datasetNameText.getText(),endpointTypeCombo.getValue(),endpointAddressText.getText(),null);
            else
                newDataset=new Dataset(datasetNameText.getText(),endpointTypeCombo.getValue(),endpointAddressText.getText(),dataDumpFormatCombo.getValue());
            datasetData.add(newDataset);
            setDatasets(datasetConfigFile);
            datasetNameText.clear();
            endpointAddressText.clear();
            endpointTypeCombo.setId("");
            setErrorsHidden();
        }
    }

    // When endpoint type is changed
    public void handleSelectAction(ActionEvent e) {
        setErrorsHidden();
        selectedEndpointType=endpointTypeCombo.getValue();
        if(selectedEndpointType.equals("SPARQL"))
            dataDumpFormatCombo.setDisable(true);
        if(selectedEndpointType.equals("RDF_Dump"))
            dataDumpFormatCombo.setDisable(false);
    }

    // Clear text and elements
    public void handleClearAction(ActionEvent e) {
        datasetNameText.setText("");
        endpointAddressText.setText("");
        endpointTypeCombo.setValue("SPARQL");
        dataDumpFormatCombo.setDisable(true);

    }

    // Updates elements
    public void handleUpdateAction(ActionEvent e) {
        setErrorsHidden();
        if(  datasetNameText.getText().isEmpty() || endpointAddressText.getText().isEmpty() || selectedEndpointType==null){
            // new AlertDialog(stage, "Empty prefix!", AlertDialog.ICON_INFO).showAndWait();
            nameErrorLabel.setVisible(true);
            endpointErrorLabel.setVisible(true);
            endpointTypeErrorLabel.setVisible(true);

            if(selectedEndpointType!=null)
                if(selectedEndpointType.equals("RDF_Dump"))
                    formatErrorLabel.setVisible(true);
            return;
        }
        else{
            Dataset updatedDataset;
            if(endpointTypeCombo.getValue().equals("SPARQL"))
                updatedDataset=new Dataset(datasetNameText.getText(),endpointTypeCombo.getValue(),endpointAddressText.getText(),null);
            else
                updatedDataset=new Dataset(datasetNameText.getText(),endpointTypeCombo.getValue(),endpointAddressText.getText(),dataDumpFormatCombo.getValue());
            datasetData.set(modifiedCell, updatedDataset);
            setDatasets(datasetConfigFile);
            datasetNameText.clear();
            endpointAddressText.clear();
            endpointTypeCombo.setValue("SPARQL");
            dataDumpFormatCombo.setDisable(true);

        }
    }

    // Loads default values to table
    private void loadComponents(String Filename){
        datasetData = datasetTable.getItems();
        try{
            JSONParser parser = new JSONParser();
            JSONArray a = (JSONArray) parser.parse(new FileReader(Filename));
            for (Object o : a)
            {
                JSONObject dataset = (JSONObject) o;
                String datasetName = (String) dataset.get("datasetName");
                String endpointType = (String) dataset.get("endpointType");
                String endpointAddress = (String) dataset.get("endpointAddress");
                String dataDumpFormat = (String) dataset.get("dataDumpFormat");
                datasetData.add(new Dataset(datasetName,endpointType,endpointAddress,dataDumpFormat));
                datasetList.add(datasetName);
            }
        }catch (ParseException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }
        endpointTypeCombo.getItems().addAll("SPARQL", "RDF_Dump");
        dataDumpFormatCombo.getItems().addAll("TURTLE", "N-TRIPLE","N3");
    }

    // saves datasets in a JSON text file
    public static void setDatasets(String fileName){
        int datasetNumbers=datasetTable.getItems().toArray().length;
        try{
            BufferedWriter bw=new BufferedWriter(new FileWriter(fileName, false));
            bw.write("[");
            for(int i=0;i<datasetNumbers;i++){
                try{
                    JSONObject obj=new JSONObject();
                    obj.put("datasetName", datasetTable.getItems().get(i).getDatasetName());
                    obj.put("endpointType", datasetTable.getItems().get(i).getEndpointType());
                    obj.put("endpointAddress", datasetTable.getItems().get(i).getEndpointAddress());
                    obj.put("dataDumpFormat", datasetTable.getItems().get(i).getDataDumpFormat());
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

    // When a row is selected
    public Dataset getSelectedData() {
        if (datasetTable != null) {
            List<Dataset> tabla = datasetTable.getSelectionModel().getSelectedItems();
            if (tabla.size() == 1) {
                final Dataset selectedData = tabla.get(0);
                return selectedData;
            }
        }
        return null;
    }

    private final ListChangeListener<Dataset> selectedDatasetInTable =
            new ListChangeListener<Dataset>() {
                @Override
                public void onChanged(Change<? extends Dataset> c) {
                    PutModifiedRecord();
                }
            };
    private void PutModifiedRecord() {
        final Dataset datasetS = getSelectedData();
        modifiedCell = datasetData.indexOf(datasetS);

        if (datasetS != null) {
            datasetNameText.setText(datasetS.getDatasetName());
            endpointAddressText.setText(datasetS.getEndpointAddress());
            endpointTypeCombo.setValue(datasetS.getEndpointType());
            dataDumpFormatCombo.setValue(datasetS.getDataDumpFormat());
        }
    }

}
