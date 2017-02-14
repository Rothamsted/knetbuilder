package net.sourceforge.ondex.core.sql3.factories;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.DataSource;

import net.sourceforge.ondex.core.sql3.SQL3Graph;
import net.sourceforge.ondex.core.sql3.entities.SQL3Concept;
import net.sourceforge.ondex.core.sql3.entities.SQL3Entity;

public class FactoryForSubComps {
	
	/**
	 * Duplicate of the ones in SQL2, except accepting SQL3Graph & stuff rather than SQL2Graph
	 * 
	 * @author sckuo
	 */
	
	public static int createConceptName (SQL3Graph sg, SQL3Concept c, String n, boolean isP) {
		
		int nameno = 0;
		try {
			PreparedStatement createRow = sg.getConnection().prepareStatement("insert into conceptname (sid, id, name, pref) values (?,?,?,?)");
			createRow.setLong(1, sg.getSID());
			createRow.setInt(2, c.getId());
			createRow.setString(3, n);
			createRow.setBoolean(4, isP);
			
			createRow.executeUpdate();
			ResultSet ks = createRow.getGeneratedKeys();
			if(ks.next()) {
				nameno = ks.getInt(1);
			}
			ks.close();
			createRow.close();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return nameno;
		
	}
	
	public static int createConceptAcc (SQL3Graph sg, SQL3Concept c, String acc, boolean ambi, DataSource dataSource) {
		
		int accno = 0;
		try {
			PreparedStatement createRow = sg.getConnection().prepareStatement("insert into conceptaccession (sid, id, accession, ambi, DataSource) values (?,?,?,?,?)");
			createRow.setLong(1, sg.getSID());
			createRow.setInt(2, c.getId());
			createRow.setString(3, acc);
			createRow.setBoolean(4, ambi);
			createRow.setString(5, dataSource.getId());
			
			createRow.executeUpdate();
			ResultSet ks = createRow.getGeneratedKeys();
			if(ks.next()) {
				accno = ks.getInt(1);
			}
			ks.close();
			createRow.close();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return accno;
		
	}
	
	public static int createAttribute(SQL3Graph sg, SQL3Entity se, AttributeName attrname, boolean dI) {
		
		int gdsno = 0;
		
		try {
			
			PreparedStatement createRow = sg.getConnection().prepareStatement("insert into gds (sid, id, elementType, attrName, isDoIndex) values (?,?,?,?,?)");

			createRow.setLong(1, sg.getSID());
			createRow.setInt(2, se.getId());
			createRow.setString(3, se.type());
			createRow.setString(4, attrname.getId());
			createRow.setBoolean(5, dI);
			

			createRow.executeUpdate();
			ResultSet ks = createRow.getGeneratedKeys();
			if(ks.next()) {
				gdsno = ks.getInt(1);
			}
			ks.close();
			createRow.close();
			
			String an = attrname.getDataTypeAsString();
			
			if (an.equals("java.lang.String") || an.equals("java.lang.Character")) {
				
				try {
					
					PreparedStatement setString = sg.getConnection().prepareStatement("insert into string_gds values (?, null)");
				
					setString.setInt(1, gdsno);
					
					setString.execute();
					setString.close();
					
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			} else if (an.equals("java.lang.Integer")) {
				
				try {
					
					PreparedStatement setString = sg.getConnection().prepareStatement("insert into integer_gds values (?, null)");
				
					setString.setInt(1, gdsno);
					
					setString.execute();
					setString.close();
					
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			} else if (an.equals("java.lang.Float") || an.equals("java.lang.Double")) {
				
				try {
					
					PreparedStatement setString = sg.getConnection().prepareStatement("insert into double_gds values (?, null)");
				
					setString.setInt(1, gdsno);
					
					setString.execute();
					setString.close();
					
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}		
				
			} else if (an.equals("java.lang.Boolean")) {
				
				try {
					
					PreparedStatement setString = sg.getConnection().prepareStatement("insert into boolean_gds values (?, null)");
				
					setString.setInt(1, gdsno);
					
					setString.execute();
					setString.close();
					
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	
				
			} else if (an.equals("java.lang.Long")) {
				
				try {
					
					PreparedStatement setString = sg.getConnection().prepareStatement("insert into long_gds values (?, null)");
								
					setString.setInt(1, gdsno);
					
					setString.execute();
					setString.close();
					
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	
				
			} else {
			
				try {
					
					PreparedStatement setBlobItem = sg.getConnection().prepareStatement("insert into blob_gds values (?, null)");
					
					setBlobItem.setInt(1, gdsno);
					
					setBlobItem.execute();
					setBlobItem.close();
					
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return gdsno;
		
	}

}
