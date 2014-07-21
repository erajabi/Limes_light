package Main;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import java.util.Date;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author Narayan
 */
public class Transaction {

    private IntegerProperty linkID;
    private StringProperty sourceEntity;
    private StringProperty targetEntity;

    public Transaction(Integer linkIDV, String sourceEntityV,String targetEntityV) {
        linkID = new SimpleIntegerProperty(linkIDV);
        sourceEntity = new SimpleStringProperty(sourceEntityV);
        targetEntity = new SimpleStringProperty(targetEntityV);

    }
    public IntegerProperty linkIDProperty() {
        return linkID;
    }

    public StringProperty sourceEntityProperty() {
        return sourceEntity;
    }
    public StringProperty targetEntityProperty() {
        return targetEntity;
    }


}