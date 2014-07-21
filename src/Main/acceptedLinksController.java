package Main;

    import java.awt.*;
    import java.io.*;
    import java.net.MalformedURLException;
    import java.net.URI;
    import java.net.URL;
    import java.util.*;
    import java.util.List;
    import java.util.prefs.Preferences;

    import com.hp.hpl.jena.rdf.model.*;
    import com.hp.hpl.jena.util.FileManager;
    import com.hp.hpl.jena.util.FileUtils;
    import com.hp.hpl.jena.vocabulary.OWL;
    import com.hp.hpl.jena.vocabulary.RDF;
    import com.hp.hpl.jena.vocabulary.VCARD;
    import javafx.beans.property.IntegerProperty;
    import javafx.beans.property.SimpleIntegerProperty;
    import javafx.beans.property.StringProperty;
    import javafx.beans.value.ChangeListener;
    import javafx.beans.value.ObservableValue;
    import javafx.collections.FXCollections;
    import javafx.collections.ObservableList;
    import javafx.event.ActionEvent;
    import javafx.event.EventHandler;
    import javafx.fxml.FXML;
    import javafx.fxml.Initializable;
    import javafx.geometry.Pos;
    import javafx.scene.control.*;
    import javafx.scene.control.TableColumn.CellDataFeatures;
    import javafx.scene.control.TextField;
    import javafx.scene.input.MouseButton;
    import javafx.scene.input.MouseEvent;
    import javafx.scene.layout.AnchorPane;
    import javafx.scene.layout.StackPane;
    import javafx.scene.text.Text;
    import javafx.util.Callback;
    import se.mbaeumer.fxmessagebox.MessageBox;
    import se.mbaeumer.fxmessagebox.MessageBoxResult;
    import se.mbaeumer.fxmessagebox.MessageBoxType;

/**
     * FXML Controller class
     *
     * @author Narayan
     */
    public class acceptedLinksController implements Initializable {

        public Text totalLinks;
        public Text sourceAndTarget;
        public AnchorPane mainPane;
        @FXML
        private TableView<simpleRDF> table;
        @FXML
        private TableColumn<simpleRDF,String> sourceCol;
        @FXML
        private TableColumn<simpleRDF,String> targetCol;
        @FXML
        private TableColumn<simpleRDF,Integer> linkIDCol;
        @FXML
        private StackPane pagePanel;
        @FXML
        private Pagination pagination;
        private IntegerProperty limit;
        private Set <String> uniqueSources=new TreeSet();
        private Set <String> uniqueTargets=new TreeSet();
        //************************ My variables  *************************
        private ArrayList sourceEntities=new ArrayList<StringProperty>();
        private ArrayList targetEntities=new ArrayList<StringProperty>();
        private String acceptedFileName=null;
        ObservableList<simpleRDF> links;
        private String selectedSource=null;
        private String selectedTarget=null;
          /**
         * Initializes the controller class.
         */
        @Override
        // Reads the information and show them in a table
        public void initialize(URL url, ResourceBundle rb) {

            table.setEditable(false);
            mainPane.setMaxSize(800,700);

            try{
                BufferedReader br=new BufferedReader(new FileReader("temp.cfg"));
                acceptedFileName=br.readLine();
                br.close();
            }catch (IOException tempIO){}

            //acceptedFileName="C:\\Enayat\\Final_Package20130313\\Papers\\GLOBE\\LIMES\\Results\\Aalto\\GLOBE_Aalto_Taxon_English_accepted_20140515.nt";
            // Read the accepted file
            NTreader();

            // Limitation per page
            limit = new SimpleIntegerProperty(20);
            links= FXCollections.observableArrayList();
            totalLinks.setText(String.valueOf(sourceEntities.size())+"  links");

            for(Integer i=0; i<sourceEntities.size(); i++){
                simpleRDF p=new simpleRDF(i,sourceEntities.get(i).toString(),targetEntities.get(i).toString());
                links.add(p);
            }
            sourceAndTarget.setText(String.valueOf(uniqueSources.size())+"<--->"+String.valueOf(uniqueTargets.size()));

            pagination.currentPageIndexProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    changeTableView(newValue.intValue(), limit.get());
                }

            });

            limit.addListener(new ChangeListener<Number>(){

                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    changeTableView(pagination.getCurrentPageIndex(),newValue.intValue());
                }

            });
            StackPane.setAlignment(pagination, Pos.CENTER);
            setValueFactory();
            init();
            table.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<simpleRDF>() {

                @Override
                public void changed(ObservableValue<? extends simpleRDF> observable,
                                    simpleRDF oldValue, simpleRDF newValue) {
                    if(newValue!=null){
                        selectedSource=newValue.getSourceEntity().getValue();
                        selectedTarget=newValue.getTargetEntity().getValue();
                    }
                }
            });
            sourceCol.setCellFactory(new Callback<TableColumn<simpleRDF, String>, TableCell<simpleRDF, String>>() {
                @Override
                public TableCell<simpleRDF, String> call(TableColumn<simpleRDF, String> col) {
                    final TableCell<simpleRDF, String> cell = new TableCell<>();
                    cell.textProperty().bind(cell.itemProperty()); // in general might need to subclass TableCell and override updateItem(...) here
                    cell.setOnMouseEntered(new EventHandler<MouseEvent>() {
                        public void handle(MouseEvent event) {
                            final Tooltip tooltip = new Tooltip("Double click to open in a browser");
                            cell.setTooltip(tooltip);
                        }
                    });
                    cell.setOnMouseClicked(new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(MouseEvent event) {
                            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                                Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
                                if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
                                    try {
                                        URI url = new URI(selectedSource);
                                        desktop.browse(url);
                                    } catch (MalformedURLException e1) {
                                        e1.printStackTrace();
                                    }catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    });
                    return cell ;
                }
            });

            // When a user right clicks on a row
            targetCol.setCellFactory(new Callback<TableColumn<simpleRDF, String>, TableCell<simpleRDF, String>>() {
                @Override
                public TableCell<simpleRDF, String> call(TableColumn<simpleRDF, String> col) {
                    final TableCell<simpleRDF, String> cell = new TableCell<>();
                    cell.textProperty().bind(cell.itemProperty()); // in general might need to subclass TableCell and override updateItem(...) here
                    cell.setOnMouseEntered(new EventHandler<MouseEvent>() {
                        public void handle(MouseEvent event) {
                            final Tooltip tooltip = new Tooltip("Double click to open in a browser");
                            cell.setTooltip(tooltip);
                        }
                    });
                    cell.setOnMouseClicked(new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(MouseEvent event) {
                            //  MouseButton.SECONDARY means double click
                            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                                Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
                                if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
                                    try {
                                        URI url = new URI(selectedTarget);
                                        desktop.browse(url);
                                    } catch (MalformedURLException e1) {
                                        e1.printStackTrace();
                                    }catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    });
                    return cell ;
                }
            });


       }

        /**
         * This is the function for setting up the visual representation
         * on the startup of the program
         */
        public void init(){
            resetPage();
            pagination.setCurrentPageIndex(0);
            changeTableView(0,limit.get());
        }

        /**
         * This function resets the pagination pagecount
         */
        public void resetPage(){
            pagination.setPageCount((int)(Math.ceil(links.size()*1.0/limit.get())));
        }
        /**
         * This function is responsible for changing the TableView according
         * to the index and limit provided
         * @param index
         * @param limit
         */
        public void changeTableView(int index,int limit){
            int newIndex = index*limit;

            List<simpleRDF> trans = links.subList(Math.min(newIndex,links.size()),Math.min(links.size(),newIndex+limit));
            table.getItems().clear();
            table.setItems(null);
            ObservableList<simpleRDF> l = FXCollections.observableArrayList();
            table.setItems(l);
            for(simpleRDF t: trans){
                l.add(t);
            }
        }

        /**
         * This function is called from the FXML when the user enters
         * new limit
         * @param evt
         */
        @FXML
        public void changeLimit(ActionEvent evt){

            TextField txt=(TextField)evt.getSource();
            if(txt != null){
                try{
                    int i =Integer.parseInt(txt.getText());
                    limit.set(i);
                    resetPage();
                    //System.out.println(table.getItems().size());
                    //pagination.setCurrentPageIndex(Math.min(pagination.getCurrentPageIndex(),table.getItems().size()/limit.get()));
                }catch(NumberFormatException nfe){
                    System.err.println("NFE error");
                }
            }

        }

        /**
         * This function helps to set the cellValueFactory of all column
         */
        private void setValueFactory(){
            linkIDCol.prefWidthProperty().bind(table.widthProperty().divide(5));
            linkIDCol.setCellValueFactory(new Callback<CellDataFeatures<simpleRDF,Integer>,ObservableValue<Integer>>(){
                @Override
                public ObservableValue call(CellDataFeatures<simpleRDF, Integer> param) {
                    if(table.getItems().contains(param.getValue()) && param.getValue() != null)
                        return param.getValue().getLinkID();
                    return null;
                }

            });

            sourceCol.prefWidthProperty().bind(table.widthProperty().divide(5));
            sourceCol.setCellValueFactory(new Callback<CellDataFeatures<simpleRDF,String>,ObservableValue<String>>(){
                @Override
                public ObservableValue<String> call(CellDataFeatures<simpleRDF, String> param) {
                    return param.getValue().getSourceEntity();
                }

            });
            targetCol.prefWidthProperty().bind(table.widthProperty().divide(5));
            targetCol.setCellValueFactory(new Callback<CellDataFeatures<simpleRDF,String>,ObservableValue<String>>(){
                @Override
                public ObservableValue<String> call(CellDataFeatures<simpleRDF, String> param) {
                    return param.getValue().getTargetEntity();
                }
            });
        }

        // Read accepted file
        private void NTreader(){
            Model model = ModelFactory.createDefaultModel();
            InputStream in = FileManager.get().open(acceptedFileName );
            if (in == null) {
                throw new IllegalArgumentException( "File: " + acceptedFileName + " not found");
            }
            BufferedReader NTreader = FileUtils.asBufferedUTF8(in) ;
            model.read(NTreader, acceptedFileName, "N-TRIPLE");
            //Property owlSameAs=model.createProperty(OWL.sameAs);
            StmtIterator iter = model.listStatements(null,OWL.sameAs,(Literal)null);
            int j=0;
            System.out.println("Please wait ... takes time to read the output file...");
            while (iter.hasNext()) {
                Statement r = iter.next();
                sourceEntities.add(r.getSubject());
                targetEntities.add(r.getObject());
                uniqueSources.add(sourceEntities.get(j).toString());
                uniqueTargets.add(targetEntities.get(j).toString());

                if(j%5000==0){
                    System.out.println("Reading line "+j+" th");
                }
                j++;
            }
        }
    }