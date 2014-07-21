package Main;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import se.mbaeumer.fxmessagebox.MessageBox;
import se.mbaeumer.fxmessagebox.MessageBoxResult;
import se.mbaeumer.fxmessagebox.MessageBoxType;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class prefixController extends mainController{
    @FXML public static TableView<LD_Prefix> prefixTable;
    @FXML private static ObservableList<LD_Prefix> prefixTableData;
    @FXML private TextField addPrefixName;
    @FXML private TextField addPrefixAddress;
    @FXML private Label nameErrorLabel;
    @FXML private Label addressErrorLabel;
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
        loadTable(prefixConfigFile);
        final ObservableList<LD_Prefix> prefixSelected = prefixTable.getSelectionModel().getSelectedItems();
        prefixSelected.addListener(selectedPrefixInTable);
    }

    public void handleDeleteAction(ActionEvent event) {
        nameErrorLabel.setVisible(false);
        addressErrorLabel.setVisible(false);

        setIndex(modifiedCell);
        if(index.get()<0){
            System.out.println("Wrong row!");
            return;
        }
        MessageBox mb = new MessageBox("Do you want really to delete?", MessageBoxType.YES_NO);
        mb.showAndWait();
        if (mb.getMessageBoxResult() == MessageBoxResult.YES){
            prefixTableData.remove(index.get());
            savePrefix(prefixConfigFile);
        }
    }

    public void handleClearAction(ActionEvent e) {
        nameErrorLabel.setVisible(false);
        addressErrorLabel.setVisible(false);
        addPrefixName.setText("");
        addPrefixAddress.setText("");

    }
    public void handleAddAction(ActionEvent e) {
        nameErrorLabel.setVisible(false);
        addressErrorLabel.setVisible(false);
        if( addPrefixName.getText().isEmpty() || addPrefixAddress.getText().isEmpty() ){
            nameErrorLabel.setVisible(true);
            addressErrorLabel.setVisible(true);
        }  else if(findDuplicatePrefixes(addPrefixName.getText())){
            nameErrorLabel.setVisible(true);
        }
        else{
            LD_Prefix l=new LD_Prefix(addPrefixName.getText(),
                    addPrefixAddress.getText());
            prefixTableData.add(l);
            savePrefix(prefixConfigFile);
            addPrefixName.clear();
            addPrefixAddress.clear();
        }
    }

    public void handleUpdateAction(ActionEvent e) {
        nameErrorLabel.setVisible(false);
        addressErrorLabel.setVisible(false);
        if(  addPrefixName.getText().isEmpty() || addPrefixAddress.getText().isEmpty()){
            nameErrorLabel.setVisible(true);
            addressErrorLabel.setVisible(true);
        }  else if(modifiedCell>0) {
              LD_Prefix l=new LD_Prefix(addPrefixName.getText(),
                    addPrefixAddress.getText());
            prefixTableData.set(modifiedCell,l);
            savePrefix(prefixConfigFile);
            addPrefixName.clear();
            addPrefixAddress.clear();
        }
    }

    public void handleNewAction(ActionEvent e) {
        addPrefixName.setText("");
        addPrefixAddress.setText("");
    }

    private void loadTable(String Filename){
        prefixTableData = prefixTable.getItems();
        // Reading Prefix File
        try{
            JSONParser parser = new JSONParser();
            JSONArray a = (JSONArray) parser.parse(new FileReader(Filename));
            for (Object o : a)
            {
                JSONObject person = (JSONObject) o;
                String prefixName = (String) person.get("prefixName");
                String prefixAddress = (String) person.get("prefixAddress");
                prefixTableData.add(new LD_Prefix(prefixName,prefixAddress));
                prefixList.add(prefixName);
            }
        }catch (ParseException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void savePrefix(String fileName){
        int prefixNumbers=prefixTable.getItems().toArray().length;
        try{
            BufferedWriter bw=new BufferedWriter(new FileWriter(fileName, false));
            bw.write("[");
            for(int i=0;i<prefixNumbers;i++){
                try{
                    JSONObject obj=new JSONObject();
                    obj.put("prefixName", prefixTable.getItems().get(i).getPrefixName());
                    obj.put("prefixAddress", prefixTable.getItems().get(i).getPrefixAddress());
                    obj.writeJSONString(bw);
                }catch (Exception e){
                    System.err.println("Can not write if file. Error: " + e.getMessage());
                }
            }
            bw.write("]");
            bw.close();
        }catch (IOException e){
            System.err.println("Can not write if file. Error: " + e.getMessage());
        }
    }

    public LD_Prefix getSelectedData() {
        if (prefixTable != null) {
            List<LD_Prefix> tabla = prefixTable.getSelectionModel().getSelectedItems();
            if (tabla.size() == 1) {
                final LD_Prefix selectedData = tabla.get(0);
                return selectedData;
            }
        }
        return null;
    }

    private final ListChangeListener<LD_Prefix> selectedPrefixInTable =
            new ListChangeListener<LD_Prefix>() {
                @Override
                public void onChanged(Change<? extends LD_Prefix> c) {
                    PutModifiedRecord();
                }
            };

    private void PutModifiedRecord() {
        final LD_Prefix datasetS = getSelectedData();
        modifiedCell = prefixTableData.indexOf(datasetS);
        if (datasetS != null) {
            nameErrorLabel.setVisible(false);
            addressErrorLabel.setVisible(false);
            addPrefixName.setText(datasetS.getPrefixName());
            addPrefixAddress.setText(datasetS.getPrefixAddress());
        }
    }

    protected boolean findDuplicatePrefixes(String prefixValue){
            for(int i=0;i<prefixTableData.size();i++)
                if(prefixTableData.get(i).getPrefixName().equals(prefixValue))
                    return true;
        return false;
    }



}
