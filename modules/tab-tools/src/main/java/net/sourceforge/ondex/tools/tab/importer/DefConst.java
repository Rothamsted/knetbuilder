// TODO: Remove.
//package net.sourceforge.ondex.tools.tab.importer;
//
//import net.sourceforge.ondex.exception.type.EmptyStringException;
//import net.sourceforge.ondex.exception.type.NullValueException;
//import net.sourceforge.ondex.tools.subgraph.AttributePrototype;
//
///**
// * @author lysenkoa
// * @deprecated <b>WARNING</b> this appears to be copy-pasted from {@link net.sourceforge.ondex.tools.subgraph.DefConst}.
// * Not only is this very bad, but we also noted working problems with CSV-imported graphs. Please try to not
// * use the function in this class, use the original one instead.
// * 
// */
//@Deprecated
//public class DefConst {
//    public static final String DEFGDS = "Attribute";
//    public static final String DEFEVIDENCE = "EVIDENCE";
//    public static final String DEFNAME = "NAME";
//    public static final String DEFACC = "ACC";
//    public static final String DEFCV = "DataSource";
//    public static final String DEFCC = "CC";
//    public static final String DEFRT = "RT";
//    public static final String DEFPID = "PID";
//    public static final String NUMBER = "NUMBER";
//    public static final String TEXT = "TEXT";
//    public static final String INTEGER = "INTEGER";
//    public static final String COLOR = "COLOR";
//    public static final String DOUBLE = "DOUBLE";
//    public static final String OBJECT = "OBJECT";
//
//    public DefConst() {
//    }
//
//    /*Data*/
//
//    public static AttributePrototype defPID(Integer pos) {
//        return new AttributePrototype(DEFPID, pos);
//    }
//
//    public static AttributePrototype defGDS(Integer pos, String attributeName) {
//        return new AttributePrototype(DEFGDS, attributeName, TEXT, pos);
//    }
//
//    public static AttributePrototype defGDS(Integer pos, String attributeName, String type) {
//        return new AttributePrototype(DEFGDS, attributeName, type, pos);
//    }
//
//    public static AttributePrototype defGDS(Integer pos, String attributeName, String type, String isIndexed) {
//        return new AttributePrototype(DEFGDS, attributeName, type, pos, isIndexed);
//    }
//
//    public static AttributePrototype defEvidence(Integer pos) {
//        return new AttributePrototype(DEFEVIDENCE, pos);
//    }
//
//    public static AttributePrototype defName(Integer pos) {
//        return new AttributePrototype(DEFNAME, "true", pos);
//    }
//
//    public static AttributePrototype defName(Integer pos, String isPreferred) {
//        return new AttributePrototype(DEFNAME, isPreferred, pos);
//    }
//
//    public static AttributePrototype defCV(Integer pos) {
//        return new AttributePrototype(DEFCV, pos);
//    }
//
//    public static AttributePrototype defAccession(Integer pos, String cv) {
//        return new AttributePrototype(DEFACC, "false", cv, pos);
//    }
//
//    public static AttributePrototype defAccession(Integer pos, String cv, String isAmbiguous) {
//        return new AttributePrototype(DEFACC, isAmbiguous, cv, pos);
//    }
//
//    public static AttributePrototype defAccession(Integer pos) {
//        return new AttributePrototype(DEFACC, "false", "UC", pos);
//    }
//
//    public static AttributePrototype defCC(Integer pos) {
//        return new AttributePrototype(DEFCC, pos);
//    }
//
//    public static AttributePrototype defRT(Integer pos) {
//        return new AttributePrototype(DEFRT, pos);
//    }
//
//    /*Concstant*/
//
//    public static AttributePrototype defPID(String pid) {
//        return new AttributePrototype(DEFPID, pid);
//    }
//
//    public static AttributePrototype defGDS(String value, String attributeName, String type) {
//        return new AttributePrototype(DEFGDS, attributeName, type, value);
//    }
//
//    public static AttributePrototype defGDS(String value, String attributeName, String type, String isIndexed) {
//        return new AttributePrototype(DEFGDS, attributeName, type, value, isIndexed);
//    }
//
//    public static AttributePrototype defGDS(String value, String attributeName) {
//        return new AttributePrototype(DEFGDS, attributeName, "TEXT", value);
//    }
//
//    public static AttributePrototype defGDS(String value) {
//        return new AttributePrototype(DEFGDS, "USER_ATT", "TEXT", value);
//    }
//
//    public static AttributePrototype defEvidence(String value) {
//        return new AttributePrototype(DEFEVIDENCE, value);
//    }
//
//    public static AttributePrototype defName(String value) {
//        return new AttributePrototype(DEFNAME, "true", value);
//    }
//
//    public static AttributePrototype defName(String value, String isPreferred) {
//        return new AttributePrototype(DEFNAME, isPreferred, value);
//    }
//
//    public static AttributePrototype defCV(String cv) {
//        return new AttributePrototype(DEFCV, cv);
//    }
//
//    public static AttributePrototype defAccession(String cv) {
//        return new AttributePrototype(DEFACC, "false", cv, "UC");
//    }
//
//    public static AttributePrototype defAccession(String value, String cv) {
//        return new AttributePrototype(DEFACC, "false", cv, value);
//    }
//
//    public static AttributePrototype defAccession(String value, String cv, String isAmbiguous) {
//        return new AttributePrototype(DEFACC, isAmbiguous, cv, value);
//    }
//
//    public static AttributePrototype defCC(String conceptClass) {
//        return new AttributePrototype(DEFCC, conceptClass);
//    }
//
//    public static AttributePrototype defRT(String realtionType) {
//        return new AttributePrototype(DEFRT, realtionType);
//    }
///*
//	public static PathParser getParser(AbstractONDEXGraph  graph){
//		PathParser result = null;
//		try{
//			result = new PathParser(graph, new TestDataReader());	
//		}
//		catch(Exception e){
//			e.printStackTrace();
//		}
//		return result;
//	}
//	
//	public static DelimitedReader getDelimitedReader(String file){
//		return new DelimitedReader(file, "\t");	
//	}
//*/
//
//    public static DataReader getDelimitedReader(String file, String delimiter) throws Exception {
//        return new DelimitedReader(file, delimiter);
//    }
//
//    public static DataReader getDelimitedReader(String file, int fromLine) throws Exception {
//        DelimitedReader result = new DelimitedReader(file, "	");
//        result.setLine(fromLine);
//        return result;
//    }
//
//    public static DataReader getDelimitedReader(String file, String delimiter, int fromLine) throws Exception {
//        DelimitedReader result = new DelimitedReader(file, delimiter);
//        result.setLine(fromLine);
//        return result;
//    }
//
//    protected interface PositionProcessor {
//        public void process(AttributePrototype ap) throws NullValueException, EmptyStringException;
//    }
//}
