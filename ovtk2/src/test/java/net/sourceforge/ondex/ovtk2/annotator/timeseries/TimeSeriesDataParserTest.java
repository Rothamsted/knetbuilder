package net.sourceforge.ondex.ovtk2.annotator.timeseries;

/*
 import java.io.File;
 import java.io.IOException;

 import net.sourceforge.ondex.ovtk2.annotator.timeseries.TimeSeriesDataParser;

 /**
 * 
 * @author hindlem
 *
 *
 public class TimeSeriesDataParserTest extends TimeSeriesDataFileWriter {


 public void testTimeSeriesDataParserParser() {

 try {

 String[] alphabetString = new String[] {
 "a", "b", "c", "d", "e", "f", "g", "h", "i", "j","k", "l", "m", "n", "o",
 "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"};

 File temp = File.createTempFile("tmp",".file");
 temp.deleteOnExit();

 writeExampleFile(temp.getAbsolutePath(), 6, 5, alphabetString);

 TimeSeriesDataParser tsd = new TimeSeriesDataParser(getClass().getName(), 2.0d, true);

 tsd.parseData(temp.getAbsolutePath(), 4, 5, 6, 6*5);

 assertEquals("incorrect number of seq parsed", alphabetString.length, tsd.getRatioIndex().getTargetSequences().size());
 assertEquals("incorrect number of timepoints parsed", 6, tsd.getRatioIndex().getTimepoints().size());
 assertEquals("incorrect number of treatments parsed", 5, tsd.getRatioIndex().getConditions().size());

 } catch (IOException e) {
 e.printStackTrace();
 }

 }

 public void test() {
 String file = "D:/Marcela/TRITIMED significant interaction all samples (CE 54).txt";

 TimeSeriesDataParser tsd = new TimeSeriesDataParser(getClass().getName(), 2.0d, false);
 tsd.parseData(file, 0, 1, 2, 5*3);

 System.out.println(" "+ tsd.getRatioIndex().getTargetSequences().size());
 System.out.println(" "+ tsd.getTargetSeqToLSD().size());
 System.out.println(" "+ tsd.getRatioIndex().getTimepoints().size());
 System.out.println(" "+ tsd.getRatioIndex().getConditions().size());

 assertEquals(tsd.getRatioIndex().getTargetSequences().size(), tsd.getRatioIndex().getTimepoint("Lahn", 1).size());
 assertEquals(tsd.getRatioIndex().getTargetSequences().size(), tsd.getRatioIndex().getTimepoint("RIL2219", 1).size());
 assertEquals(tsd.getRatioIndex().getTargetSequences().size(), tsd.getRatioIndex().getTimepoint("CHAM1", 1).size());
 }

 }
 */
