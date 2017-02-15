package net.sourceforge.ondex.export.metadatacount;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.annotations.Authors;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.annotations.Status;
import net.sourceforge.ondex.annotations.StatusType;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.BooleanArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.event.type.InconsistencyEvent;
import net.sourceforge.ondex.exception.type.InconsistencyException;
import net.sourceforge.ondex.export.ONDEXExport;

import java.io.*;
import java.util.*;


/**
 * Detailed, configurable view of metadata counts
 *
 * @author Matthew Pocock
 */
@Authors(authors = {"Matthew Pocock"}, emails = {"drdozer at users.sourceforge.net"})
@Custodians(custodians = {"Jochen Weile"}, emails = {"jweile at users.sourceforge.net"})
@Status(description = "Changed to DISCONTINUED (by Christian) as there are two export files.", status = StatusType.DISCONTINUED)
public class Export extends ONDEXExport
{
    public static final String INCLUDE_ZERO_COUNTS = "IncludeZeroCounts";
    public static final String COUNT_FILE_BASE = "CountFileBase";
    public static final String EXPECTED_FILE_BASE = "ExpectedFileBase";

    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition<?>[] {
                new BooleanArgumentDefinition(INCLUDE_ZERO_COUNTS, "Include meta-data items with zero instances", false, false),
                new FileArgumentDefinition(COUNT_FILE_BASE, "Base for all generated count files", true, false, false, false),
                new FileArgumentDefinition(EXPECTED_FILE_BASE, "Base for all expected count files", false, false, false, false)
        };
    }

    public String getName() {
        return "MetaData Count Stats";
    }

    public String getVersion() {
        return "11-AUG-10";
    }

    @Override
    public String getId() {
        return "metadatacount";
    }

    public boolean requiresIndexedGraph() {
        return false;
    }

    public void start()
            throws InvalidPluginArgumentException, IOException, InconsistencyException
    {
        final boolean includeZeroCounts = (Boolean) getArguments().getUniqueValue(INCLUDE_ZERO_COUNTS);
        final String countFileBase = (String) getArguments().getUniqueValue(COUNT_FILE_BASE);
        if(countFileBase == null || countFileBase.length() == 0)
            throw new InvalidPluginArgumentException("You must supply a value for " + COUNT_FILE_BASE);
        final String expectedFileBase = (String) getArguments().getUniqueValue(EXPECTED_FILE_BASE);
        final boolean hasExpected = expectedFileBase != null;


        final ONDEXGraphMetaData metadata = graph.getMetaData();

        abstract class Counter<MD extends MetaData>
        {
            public Properties count()
            {
                Properties mdCounts = new Properties();

                for (MD md : loopsOver())
                {
                    int c = counting(md);
                    if(c != 0 || includeZeroCounts)
                    {
                        mdCounts.put(md.getId(), String.valueOf(c));
                    }
                }

                return mdCounts;
            }

            abstract Set<MD> loopsOver();
            abstract int counting(MD md);
        }

        // count different meta-data types
        Properties ccCounts = new Counter<ConceptClass>() {
            Set<ConceptClass> loopsOver() { return metadata.getConceptClasses(); }
            int counting(ConceptClass cc) { return graph.getConceptsOfConceptClass(cc).size(); }
        }.count();

        Properties cvCounts = new Counter<DataSource>() {
            Set<DataSource> loopsOver() { return metadata.getDataSources(); }
            int counting(DataSource dataSource) { return graph.getConceptsOfDataSource(dataSource).size(); }
        }.count();

        Properties attCounts = new Counter<AttributeName>() {
            Set<AttributeName> loopsOver() { return metadata.getAttributeNames(); }
            int counting(AttributeName an) { return
                    graph.getConceptsOfAttributeName(an).size() +
                    graph.getRelationsOfAttributeName(an).size(); }
        }.count();

        Properties rtCounts = new Counter<RelationType>() {
            Set<RelationType> loopsOver() { return metadata.getRelationTypes(); }
            int counting(RelationType rt) { return graph.getRelationsOfRelationType(rt).size(); }
        }.count();

        // write the counts to file
        writeCounts(countFileBase, "ConceptClass", ccCounts);
        writeCounts(countFileBase, "DataSource", cvCounts);
        writeCounts(countFileBase, "AttributeName", attCounts);
        writeCounts(countFileBase, "RelationType", rtCounts);

        if(hasExpected)
        {
            List<Diff> diffs = new ArrayList<Diff>();
            diffs.addAll(validateCounts(expectedFileBase, "ConceptClass", ccCounts));
            diffs.addAll(validateCounts(expectedFileBase, "DataSource", cvCounts));
            diffs.addAll(validateCounts(expectedFileBase, "AttributeName", attCounts));
            diffs.addAll(validateCounts(expectedFileBase, "RelationType", rtCounts));

            for(Iterator<Diff> di = diffs.iterator(); di.hasNext(); )
            {
                if(di.next().isDifferent()) di.remove();
            }

            if(!diffs.isEmpty())
            {
                fireEventOccurred(new GeneralOutputEvent("Metadata counts differ from expected", "validating counts"));
                for(Diff d : diffs)
                {
                    fireEventOccurred(new InconsistencyEvent(d.explain(), "validating counts"));
                }

                throw new InconsistencyException("Missmatch in metadata counts. See log for details.");
            }
        }
    }

    private void writeCounts(String countFileBase, String mdType, Properties counts)
            throws IOException
    {
        Writer out = new FileWriter(new File(countFileBase + "." + mdType + ".counts"));
        counts.store(out, "Counts for metadata of type: " + mdType);
        out.close();
    }

    @SuppressWarnings("unchecked")
    private List<Diff> validateCounts(String expectedFileBase, String mdType, Properties counts)
           throws IOException
    {
        Properties expected = new Properties();
        expected.load(new FileReader(new File(expectedFileBase + "." + mdType + ".counts")));

        Set<String> mds = new HashSet<String>();
        mds.addAll((Collection<String>) (Collection) expected.keySet());
        mds.addAll((Collection<String>) (Collection) counts.keySet());

        List<Diff> diffs = new ArrayList<Diff>(mds.size());
        for(String md: mds)
        {
            diffs.add(new Diff(mdType, md, expected.getProperty(md), counts.getProperty(md)));
        }

        return diffs;
    }

    @Override
    public String[] requiresValidators() {
        return new String[0];
    }

    private static class Diff
    {
        private final String mdType;
        private final String md;
        private final String expected;
        private final String counts;

        private Diff(String mdType, String md, String expected, String counts)
        {
            this.mdType = mdType;
            this.md = md;
            this.expected = expected == null ? "0" : expected;
            this.counts = counts == null ? "0" : counts;
        }

        public boolean isDifferent()
        {
            return !expected.equals(counts);
        }

        public String explain()
        {
            if(isDifferent())
            {
                return "Counting " + mdType + " " + md + ". Expected " + expected + " counts, but found " + counts;
            }
            else
            {
                return "Counts for " + mdType + " " + md + " match at " + expected;
            }
        }
    }
}
