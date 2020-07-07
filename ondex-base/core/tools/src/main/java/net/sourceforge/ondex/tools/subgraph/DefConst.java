package net.sourceforge.ondex.tools.subgraph;

import net.sourceforge.ondex.exception.type.EmptyStringException;
import net.sourceforge.ondex.exception.type.NullValueException;

/**
 * 
 * @author lysenkoa
 *
 */
public class DefConst {
	public static final String DEFATTR = "ATTRIBUTE";
	public static final String DEFEVIDENCE = "EVIDENCE";
	public static final String DEFNAME = "NAME";
	public static final String DEFACC = "ACC";
	public static final String DEFDATASOURCE = "DATASOURCE";
	public static final String DEFCC = "CC";
	public static final String DEFRT = "RT";
	public static final String DEFPID = "PID";
	public static final String NUMBER = "NUMBER";
	public static final String TEXT = "TEXT";
	public static final String INTEGER = "INTEGER";
	public static final String SMILES = "SMILES";
	public static final String COLOR = "COLOR";
	public static final String DOUBLE = "DOUBLE";
	public static final String OBJECT = "OBJECT";
	
	public DefConst(){}
	
	/*Data*/
	public static AttributePrototype defPID(int pos){
		return new AttributePrototype(DEFPID, pos);
	}
	
	public static AttributePrototype defAttribute(int pos, String attributeName){
		return new AttributePrototype(DEFATTR, attributeName, TEXT, pos);
	}
	
	public static AttributePrototype defAttribute(int pos, String attributeName, String type){
		return new AttributePrototype(DEFATTR, attributeName, type, pos);
	}
	
	public static AttributePrototype defAttribute(int pos, String attributeName, String type, String isIndexed){
		return new AttributePrototype(DEFATTR, attributeName, type, pos, isIndexed);
	}
	
	public static AttributePrototype defEvidence(int pos){
		return new AttributePrototype(DEFEVIDENCE, pos);
	}
	
	public static AttributePrototype defName(int pos){
		return new AttributePrototype(DEFNAME,"true", pos);
	}
	
	public static AttributePrototype defName(int pos, String isPreferred){
		return new AttributePrototype(DEFNAME, isPreferred, pos);
	}
	
	public static AttributePrototype defDataSource(int pos){
		return new AttributePrototype(DEFDATASOURCE, pos);
	}
	
	public static AttributePrototype defAccession(int pos, String cv){
		return new AttributePrototype(DEFACC, "false", cv, pos);
	}
	
	public static AttributePrototype defAccession(int pos, String cv, String isAmbiguous){
		return new AttributePrototype(DEFACC, isAmbiguous, cv, pos);
	}
	
	public static AttributePrototype defAccession(int pos){
		return new AttributePrototype(DEFACC, "false", "UC", pos);
	}
	
	public static AttributePrototype defCC(int pos){
		return new AttributePrototype(DEFCC, pos);
	}
	
	public static AttributePrototype defRT(int pos){
		return new AttributePrototype(DEFRT, pos);
	}
	
	/*Concstant*/
	
	public static AttributePrototype defPID(String pid){
		return new AttributePrototype(DEFPID, pid);
	}
	
	public static AttributePrototype defAttribute(String value, String attributeName, String type){
		return new AttributePrototype(DEFATTR, attributeName, type, value);
	}
	
	public static AttributePrototype defAttribute(String value, String attributeName, String type, String isIndexed){
		return new AttributePrototype(DEFATTR, attributeName, type, value, isIndexed);
	}
	
	public static AttributePrototype defAttribute(String value, String attributeName){
		return new AttributePrototype(DEFATTR, attributeName, "TEXT", value);
	}
	
	public static AttributePrototype defAttribute(String value){
		return new AttributePrototype(DEFATTR, "USER_ATT", "TEXT", value);
	}
	
	public static AttributePrototype defEvidence(String value){
		return new AttributePrototype(DEFEVIDENCE, value);
	}
	
	public static AttributePrototype defName(String value){
		return new AttributePrototype(DEFNAME, "true", value);
	}
	
	public static AttributePrototype defName(String value, String isPreferred){
		return new AttributePrototype(DEFNAME, isPreferred, value);
	}
	
	public static AttributePrototype defDataSource(String cv){
		return new AttributePrototype(DEFDATASOURCE, cv);
	}
	
	public static AttributePrototype defAccession(String cv){
		return new AttributePrototype(DEFACC, "false", cv, "UC");
	}
	
	public static AttributePrototype defAccession(String value, String cv){
		return new AttributePrototype(DEFACC, "false", cv, value);
	}
	
	public static AttributePrototype defAccession(String value, String cv, String isAmbiguous){
		return new AttributePrototype(DEFACC, isAmbiguous, cv, value);
	}
	
	public static AttributePrototype defCC(String conceptClass){
		return new AttributePrototype(DEFCC, conceptClass);
	}
	
	public static AttributePrototype defRT(String realtionType){
		return new AttributePrototype(DEFRT, realtionType);
	}
/*
	public static PathParser getParser(AbstractONDEXGraph  graph){
		PathParser result = null;
		try{
			result = new PathParser(graph, new TestDataReader());	
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}
	
	public static DelimitedReader getDelimitedReader(String file){
		return new DelimitedReader(file, "\t");	
	}
*/
	
	public interface PositionProcessor {
		public void process(AttributePrototype ap) throws NullValueException, EmptyStringException;
	}
}
