
package net.sourceforge.ondex.ovtk2.filter.kpmfilter;

import java.io.File;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.exception.type.MetaDataMissingException;
import net.sourceforge.ondex.exception.type.ParsingFailedException;
/**
 *
 * MAInterface ma = new MAImplementation();
 * ma.setGraph(graph);
 * DataSource[] dataSources = ma.getGeneDataSources();
 * //fill a ComboBox with the contents of 'dataSources'
 * //the user picks one DataSource 'ns'.
 * //get a file 'file' from some GUI file opener.
 *
 * ma.parseMaFile(file, ns);
 */
public interface MAInterface {

    void setGraph(ONDEXGraph g) throws MetaDataMissingException;

    DataSource[] getGeneDataSources();

    void parseMaFile(File file, DataSource ns) throws ParsingFailedException ;

    String getWarnings();

}