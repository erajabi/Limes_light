package Main;

/**
 * Created with IntelliJ IDEA.
 * User: Enayat
 * Date: 6/9/14
 * Time: 4:59 PM
 * To change this template use File | Settings | File Templates.
 */

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

//***************** Tasks results class ************************
public class simpleRDF {
    public SimpleIntegerProperty linkID;
    public SimpleStringProperty sourceEntity;
    public SimpleStringProperty targetEntity;

    simpleRDF(Integer linkIDv,String sourceE, String targetE){
        this.linkID=new SimpleIntegerProperty(linkIDv);
        this.sourceEntity=new SimpleStringProperty(sourceE);
        this.targetEntity=new SimpleStringProperty(targetE);
    }

    public IntegerProperty getLinkID() {
        return linkID;
    }

    public void setLinkID(Integer myID) {
        linkID.set(myID);
    }
    public void setSourceEntity(String aName) {
        sourceEntity.set(aName);
    }
    public StringProperty getSourceEntity() {
        return sourceEntity;
    }
    public void setTargetEntity(String aName) {
        targetEntity.set(aName);
    }

    public StringProperty getTargetEntity() {
        return targetEntity;
    }

}
