package Main;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class mainController {
    protected static String configFolder="config/";
    protected static String prefixConfigFile=configFolder+"prefix.txt";
    protected static String datasetConfigFile=configFolder+"datasets.txt";
    protected static String taskConfigFile=configFolder+"tasks.txt";
    protected static String resultFolder="results/";
    public static ArrayList<String> prefixList=new ArrayList<String>(100);
    public static ArrayList<String> datasetList=new ArrayList<String>(100);

   // protected String acceptedFileName;

    public void initialize() {

    }

    //***************** Prefix class ************************
    public static class LD_Prefix {
        protected final SimpleStringProperty prefixName;
        protected final SimpleStringProperty prefixAddress;
        protected LD_Prefix(String fName, String prefixAddress) {
            this.prefixName = new SimpleStringProperty(fName);
            this.prefixAddress = new SimpleStringProperty(prefixAddress);
        }
        public String getPrefixName() {
            return prefixName.get();
        }
        public void setPrefixName(String fName) {
            prefixName.set(fName);
        }
        public String getPrefixAddress() {
            return prefixAddress.get();
        }
        public void setPrefixAddress(String fName) {
            prefixAddress.set(fName);
        }
    }

    //***************** Dataset class ************************
    protected static class Dataset {

        protected SimpleStringProperty datasetName=null;
        protected SimpleStringProperty endpointType=null;
        protected SimpleStringProperty endpointAddress=null;
        protected SimpleStringProperty dataDumpFormat=null;


        protected Dataset(String dName, String eType, String eAddress, String format) {
            this.datasetName = new SimpleStringProperty(dName);
            this.endpointType = new SimpleStringProperty(eType);
            this.endpointAddress = new SimpleStringProperty(eAddress);
            this.dataDumpFormat = new SimpleStringProperty(format);
        }

        public String getDatasetName() {
            return datasetName.get();
        }

        public void setDatasetName(String dName) {
            datasetName.set(dName);
        }

        public String getEndpointType() {
            return endpointType.get();
        }

        public void setEndpointType(String type) {
            endpointType.set(type);
        }

        public void setEndpointAddress(String eAddress) {
            endpointAddress.set(eAddress);
        }
        public String getEndpointAddress() {
            return endpointAddress.get();
        }
        public String getDataDumpFormat() {
            return dataDumpFormat.get();
        }

        public void setdataDumpFormat(String dName) {
            dataDumpFormat.set(dName);
        }
    }

    //***************** Task class ************************
    protected static class Task {
        protected SimpleStringProperty taskName;
        protected SimpleStringProperty sourceProperty;
        protected SimpleStringProperty targetProperty;
        protected SimpleStringProperty sourceDataset;
        protected SimpleStringProperty targetDataset;
        protected SimpleStringProperty algorithmType;
        protected SimpleStringProperty sourcePrefix;
        protected SimpleStringProperty targetPrefix;
        protected SimpleStringProperty sourceRestriction;
        protected SimpleStringProperty targetRestriction;
        protected SimpleStringProperty acceptedThreshold;


        protected Task(String tName, String sDataset,String tDataset, String prefixSource, String prefixTarget, String sProperty, String tProperty, String sourceRestrictionV, String targetRestrictionV, String algorithmV,String thresholdV) {
            this.taskName = new SimpleStringProperty(tName);
            this.sourceProperty = new SimpleStringProperty(sProperty);
            this.targetProperty = new SimpleStringProperty(tProperty);
            this.sourceDataset = new SimpleStringProperty(sDataset);
            this.targetDataset = new SimpleStringProperty(tDataset);
            this.algorithmType = new SimpleStringProperty(algorithmV);
            this.sourcePrefix = new SimpleStringProperty(prefixSource);
            this.targetPrefix = new SimpleStringProperty(prefixTarget);
            this.sourceRestriction = new SimpleStringProperty(sourceRestrictionV);
            this.targetRestriction = new SimpleStringProperty(targetRestrictionV);
            this.acceptedThreshold = new SimpleStringProperty(thresholdV);
        }

        public String getAcceptedThresholdV() {
            return acceptedThreshold.get();
        }

        public void setAcceptedThresholdV(String aName) {
            acceptedThreshold.set(aName);
        }
        public String getSourceRestriction() {
            return sourceRestriction.get();
        }

        public void setSourceRestriction(String aName) {
            sourceRestriction.set(aName);
        }
        public String getTargetRestriction() {
            return targetRestriction.get();
        }

        public void setTargetRestriction(String aName) {
            targetRestriction.set(aName);
        }
        public String getSourcePrefix() {
            return sourcePrefix.get();
        }

        public void setSourcePrefix(String aName) {
            sourcePrefix.set(aName);
        }
        public String getTargetPrefix() {
            return targetPrefix.get();
        }

        public void setTargetPrefix(String aName) {
            targetPrefix.set(aName);
        }
        public String getAlgorithmType() {
            return algorithmType.get();
        }

        public void setAlgorithmType(String aName) {
            algorithmType.set(aName);
        }
        public String getTaskName() {
            return taskName.get();
        }

        public void setTaskName(String sName) {
            taskName.set(sName);
        }

        public String getSourceProperty() {
            return sourceProperty.get();
        }

        public void setSourceProperty(String sp) {
            sourceProperty.set(sp);
        }

        public void setTargetProperty(String sp) {
            targetProperty.set(sp);
        }

        public String getTargetProperty() {
            return targetProperty.get();
        }
        public String getSourceDataset() {
            return sourceDataset.get();
        }
        public void setSourceDataset(String sp) {
            sourceDataset.set(sp);
        }
        public void setTargetDataset(String sp) {
            targetDataset.set(sp);
        }
        public String getTargetDataset() {
            return targetDataset.get();
        }

    }

    //***************** Tasks results class ************************
    protected static class TaskResult {
        protected SimpleStringProperty processedTask;
        protected SimpleStringProperty acceptedNumber;
        protected SimpleStringProperty reviewedNumber;
        protected SimpleStringProperty sourceTriples;
        protected SimpleStringProperty targetTriples;
        protected SimpleStringProperty processedTime;

        TaskResult(String PT,String AN, String RN, String PTime){//}, String ST, String TT, String Ptime){
            this.processedTask=new SimpleStringProperty(PT);
            this.acceptedNumber=new SimpleStringProperty(AN);
            this.reviewedNumber=new SimpleStringProperty(RN);
            this.processedTime=new SimpleStringProperty(PTime);
        }

        public String getProcessedTask() {
            return processedTask.get();
        }

        public void setProcessedTask(String aName) {
            processedTask.set(aName);
        }

        public String getAcceptedNumber() {
            return acceptedNumber.get();
        }
        public void setAcceptedNumber(String aName) {
            acceptedNumber.set(aName);
        }

        public String getReviewedNumber() {
            return reviewedNumber.get();
        }
        public void setReviewedNumber(String aName) {
            reviewedNumber.set(aName);
        }

        public String getSourceTriples() {
            return sourceTriples.get();
        }
        public void setSourceTriples(String aName) {
            sourceTriples.set(aName);
        }

        public String getTargetTriples() {
            return targetTriples.get();
        }
        public void setTargetTriples(String aName) {
            targetTriples.set(aName);
        }

        public String getprocessedTime() {
            return processedTime.get();
        }
        public void setProcessedTime(String aName) {
            processedTime.set(aName);
        }

    }

}
