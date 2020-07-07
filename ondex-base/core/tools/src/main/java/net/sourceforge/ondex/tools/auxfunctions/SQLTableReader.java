package net.sourceforge.ondex.tools.auxfunctions;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * 
 * Auxiliary functions for parsers
 * SQL Table Reader
 * 
 * Parses individual table lines into TabArrayObjects
 * 
 * Usage:
 * 
 * SQLTableReader sr = new SQLTableReader(Connection conn, String query, Class[] c); 
 * // where conn is a SQL connection string, query is the SQL query to be executed
 * // and c is the list of classes expected by each line 
 * // i.e.
 * // Connection conn = DriverManager.getConnection("jdbc:mysql://someserver:3306/sometable", "user", "pass");
 * // SQLTableReader(Connection c, "select a,b,c from d", {String.class, Integer.class, Boolean.class});
 * 
 *  
 * TabArrayObject tao = sr.getNext();
 * // gets the next line and delivers it in TabArrayObject form.  
 * 
 * close()
 * // Closes the database resources;  
 *
 * 
 * !!!!! WARNING !!!!!
 * This will only work for the following classes (although it can be extended to other SQL types I guess):
 * 
 * String
 * Integer
 * Float
 * Double
 * Boolean
 * 
 * @author sckuo
 * 
 */

public class SQLTableReader {
	
	private Connection conn;
	private ResultSet rs;
	PreparedStatement fetchTable;
	private Class[] cls;
	
	public SQLTableReader(Connection con, String query, Class[] c) {
		
		try {
			
			this.conn = con;
			this.fetchTable = conn.prepareStatement(query);
			this.rs = fetchTable.executeQuery();
			this.cls = c;
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public TabArrayObject getNext() {
		
		TabArrayObject tao = new TabArrayObject(cls.length);
		
		try {
			
			if (rs.next()) {
				
				for (int i = 0; i < cls.length; i++) {
					
					int j = i+1;
					if (cls[i] == String.class) {
						
						tao.setElement(i, rs.getString(j));
						
					} else if (cls[i] == Integer.class) {
						
						tao.setElement(i, rs.getInt(j));
						
					} else if (cls[i] == Float.class) {
						
						tao.setElement(i, rs.getFloat(j));
						
					} else if (cls[i] == Double.class) {
						
						tao.setElement(i, rs.getDouble(j));
						
					} else if (cls[i] == Boolean.class) {
						
						tao.setElement(i, rs.getBoolean(j));
						
					}			
					
				}
				
				return tao;
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	public void close() {
		
		try {
			rs.close();
			fetchTable.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
	}

}
