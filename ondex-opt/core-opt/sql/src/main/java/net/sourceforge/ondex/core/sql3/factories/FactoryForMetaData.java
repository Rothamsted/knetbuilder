package net.sourceforge.ondex.core.sql3.factories;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.Unit;
import net.sourceforge.ondex.core.sql3.SQL3Graph;

public class FactoryForMetaData {
	
	/**
	 * Duplicate of the ones in SQL2, except accepting SQL3Graph & stuff rather than SQL2Graph
	 * 
	 * @author sckuo
	 */
	
	public static void createAttributeName (SQL3Graph sg, String id, String fullname, String description, Unit unit, Class<?> datatype, AttributeName specialisationOf) {
	
		
		try {
		
			PreparedStatement createRow = sg.getConnection().prepareStatement("insert into attributename (sid, id, fullname, description, unit, class, specOf) values (?,?,?,?,?,?,?)");
			
			createRow.setLong(1, sg.getSID());
			createRow.setString(2, id);
			createRow.setString(3, fullname);
			createRow.setString(4, description);
			if (unit == null) {
				createRow.setString(5, null);
			} else {
				createRow.setString(5, unit.getId());
			}
			createRow.setString(6, datatype.getName());
			if (specialisationOf == null) {
				createRow.setString(7, null);
			} else {
				createRow.setString(7, specialisationOf.getId());		
			}
			createRow.execute();
			createRow.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	public static void createDataSource(SQL3Graph sg, String id, String fullname, String description) {
		
		try {
			
			PreparedStatement createRow = sg.getConnection().prepareStatement("insert into cv (sid, id, fullname, description) values (?,?,?,?)");
			
			createRow.setLong(1, sg.getSID());
			createRow.setString(2, id);
			createRow.setString(3, fullname);
			createRow.setString(4, description);

			createRow.execute();
			createRow.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static void createConceptClass (SQL3Graph sg, String id, String fullname, String description, ConceptClass specialisationOf) {
	
		
		try {
		
			PreparedStatement createRow = sg.getConnection().prepareStatement("insert into conceptclass (sid, id, fullname, description, specOf) values (?,?,?,?,?)");
			
			createRow.setLong(1, sg.getSID());
			createRow.setString(2, id);
			createRow.setString(3, fullname);
			createRow.setString(4, description);
			if (specialisationOf == null) {
				createRow.setString(5, null);
			} else {
				createRow.setString(5, specialisationOf.getId());		
			}
			createRow.execute();
			createRow.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static void createEvidenceType(SQL3Graph sg, String id, String fullname, String description) {
		
		try {
			
			PreparedStatement createRow = sg.getConnection().prepareStatement("insert into evidencetype (sid, id, fullname, description) values (?,?,?,?)");
			
			createRow.setLong(1, sg.getSID());
			createRow.setString(2, id);
			createRow.setString(3, fullname);
			createRow.setString(4, description);

			createRow.execute();
			createRow.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static void createUnit(SQL3Graph sg, String id, String fullname, String description) {
		
		try {
			
			PreparedStatement createRow = sg.getConnection().prepareStatement("insert into unit (sid, id, fullname, description) values (?,?,?,?)");
			
			createRow.setLong(1, sg.getSID());
			createRow.setString(2, id);
			createRow.setString(3, fullname);
			createRow.setString(4, description);

			createRow.execute();
			createRow.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static void createRelationType (SQL3Graph sg, String id, String fullname,
			String description, String inverseName, boolean isAntisymmetric,
			boolean isReflexive, boolean isSymmetric, boolean isTransitiv,
			RelationType specialisationOf) {
	
		try {
		
			PreparedStatement createRow = sg.getConnection().prepareStatement("insert into relationtype (sid, id, fullname, description, inverse, sym, antisym, refl, trans, specOf) values (?,?,?,?,?,?,?,?,?,?)");
			createRow.setLong(1, sg.getSID());
			createRow.setString(2, id);
			createRow.setString(3, fullname);
			createRow.setString(4, description);
			createRow.setString(5, inverseName);
			createRow.setBoolean(6, isAntisymmetric);
			createRow.setBoolean(7, isSymmetric);
			createRow.setBoolean(8, isReflexive);
			createRow.setBoolean(9, isTransitiv);
			if (specialisationOf == null) {
				createRow.setString(10, null);
			} else {
				createRow.setString(10, specialisationOf.getId());
			}
			
			createRow.execute();
			createRow.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
