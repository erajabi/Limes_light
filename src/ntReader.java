import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.util.FileUtils;

import java.io.BufferedReader;
import java.io.InputStream;
/**
 * Created with IntelliJ IDEA.
 * User: Enayat
 * Date: 9/23/13
 * Time: 11:01 AM
 * To change this template use File | Settings | File Templates.
 */
public class ntReader {
    /**
     * Created with IntelliJ IDEA.
     * User: Enayat
     * Date: 5/6/13
     * Time: 3:31 PM
     * To change this template use File | Settings | File Templates.
     */

    static public final String NL = System.getProperty("line.separator") ;
    static final String inputFileName  = "src/Skos2Skos_accepted.nt";
    static public String skos=null, skosPrefix=null, objectTerm=null;

    public void printNT(Model model){
        Property owlSameAs=model.createProperty("http://www.w3.org/2002/07/owl#sameAs");
        StmtIterator iter = model.listStatements(null,owlSameAs,(Literal)null);
        int j=0;
        while (iter.hasNext()) {
            Statement r = iter.next();
            System.out.print("Subject=" +r.getSubject());
            System.out.println("     Object=" +r.getObject());
                 return;
            }
    }

    public static void main(String[] args)
    {

        Model model = ModelFactory.createDefaultModel();
        InputStream in = FileManager.get().open( inputFileName );
        if (in == null) {
            throw new IllegalArgumentException( "File: " + inputFileName + " not found");
        }
        BufferedReader NTreader = FileUtils.asBufferedUTF8(in) ;
        model.read(NTreader, inputFileName, "N-TRIPLE");
        ntReader ntReader=new ntReader();
        ntReader.printNT(model);
        Property owlSameAs=model.createProperty("http://www.w3.org/2002/07/owl#sameAs");
        StmtIterator iter = model.listStatements(null,owlSameAs,(Literal)null);
        int j=0;
        while (iter.hasNext()) {
            Statement r = iter.next();
            System.out.print("Subject=" +r.getSubject());
            System.out.println("     Object=" +r.getObject());
            return;
        }

    }
}



