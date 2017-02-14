package net.sourceforge.ondex.core.sql3.entities;

import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.core.sql3.SQL3Graph;
import net.sourceforge.ondex.core.sql3.entities.subcomp.SQL3ConceptAccession;
import net.sourceforge.ondex.core.sql3.entities.subcomp.SQL3ConceptName;
import net.sourceforge.ondex.core.sql3.entities.subcomp.SQL3ConceptAttribute;
import net.sourceforge.ondex.core.sql3.factories.FactoryForSubComps;
import net.sourceforge.ondex.core.sql3.helper.EntityHelper;
import net.sourceforge.ondex.exception.type.AccessDeniedException;
import net.sourceforge.ondex.exception.type.NullValueException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class SQL3Concept extends SQL3Entity implements ONDEXConcept {
	
	/**
	 * Copy of SQL2, except for handling of Attribute
	 * 
	 * @author sckuo
	 */

	
	public SQL3Concept(SQL3Graph s, int k) {
		super(s, k, "concept");
	}
	
	public Attribute getAttribute(AttributeName attrname) {

		Connection conn = sg.getConnection();
		Attribute attribute = null;
		
		try {
			PreparedStatement getAttribute = conn.prepareStatement("select gds.key from gds where (sid, id) = (?,?) and elementType = ? and attrName = ?");
			
			getAttribute.setLong(1, getSID());
			getAttribute.setInt(2, id);	
			getAttribute.setString(3, tableName);
			getAttribute.setString(4, attrname.getId());
			
			ResultSet rs = getAttribute.executeQuery();
			
			if (rs.next()) {
				 attribute = new SQL3ConceptAttribute(sg, rs.getInt(1));
			}
			
			rs.close();
			getAttribute.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return attribute;
		
	}
	
	public Set<Attribute> getAttributes() {
		//System.out.println("Concept getAttributes " + id);
		Set<Attribute> gdss = new HashSet<Attribute>();
		
		try {
		
			PreparedStatement getAttributes = sg.getConnection().prepareStatement("select gds.key from gds where (sid, id) = (?,?) and elementType = ?");
			getAttributes.setLong(1, getSID());
			getAttributes.setInt(2, id);
			getAttributes.setString(3, tableName);
						
			ResultSet rs = getAttributes.executeQuery();
			
			while(rs.next()) {
			
				gdss.add(new SQL3ConceptAttribute(sg, rs.getInt(1)));
				
			}
				
			rs.close();
			getAttributes.close();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		return new HashSet<Attribute>(gdss);
		
	}
	
	@Override
	public boolean deleteAttribute(AttributeName attrname) {
		
		Connection conn = sg.getConnection();
		try {
			
			PreparedStatement getAttribute = conn.prepareStatement("select gds.key from gds where (sid, id) = (?,?) and elementType = ? and attrName = ?");
			
			getAttribute.setLong(1, getSID());
			getAttribute.setInt(2, id);	
			getAttribute.setString(3, "concept");
			getAttribute.setString(4, attrname.getId());
			
			ResultSet rs = getAttribute.executeQuery();
			
			if (rs.next()) {
				
				int gdsno = rs.getInt(1);
				PreparedStatement deleteG = sg.getConnection().prepareStatement("delete from gds where gds.key = ?");
				deleteG.setInt(1, gdsno);
				deleteG.execute();
				deleteG.close();
				
				PreparedStatement deleteG2 = null;
				
				String an = attrname.getDataTypeAsString();
				
				if (an.equals("java.lang.String") || an.equals("java.lang.Character")) {
					deleteG2 = sg.getConnection().prepareStatement("delete from string_gds where gdsno = ?");
				} else if (an.equals("java.lang.Integer")) {
					deleteG2 = sg.getConnection().prepareStatement("delete from integer_gds where gdsno = ?");
				} else if (an.equals("java.lang.Float") || an.equals("java.lang.Double")) {
					deleteG2 = sg.getConnection().prepareStatement("delete from double_gds where gdsno = ?");
				} else if (an.equals("java.lang.Boolean")) {
					deleteG2 = sg.getConnection().prepareStatement("delete from boolean_gds where gdsno = ?");
				} else if (an.equals("java.lang.Long")) {
					deleteG2 = sg.getConnection().prepareStatement("delete from long_gds where gdsno = ?");
				}
							
				deleteG2.setInt(1, gdsno);
				deleteG2.execute();
				deleteG2.close();
				
			}
			rs.close();
			getAttribute.close();
			
			return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	public Attribute createAttribute(AttributeName attrname,
			Object value, boolean doIndex) throws AccessDeniedException,
			NullValueException {
		
		int gdsno = FactoryForSubComps.createAttribute(sg, this, attrname, doIndex);
		
		Attribute g = new SQL3ConceptAttribute(sg, gdsno);
		g.setValue(value);		
		
		return g;
	}

	@Override
	public ConceptAccession createConceptAccession(String accession,
			DataSource elementOf, boolean ambiguous) {
		return new SQL3ConceptAccession(sg, FactoryForSubComps.createConceptAcc(sg, this, accession, ambiguous, elementOf));
	}

	@Override
	public ConceptName createConceptName(String name, boolean isPreferred) {
		return new SQL3ConceptName(sg, FactoryForSubComps.createConceptName(sg, this, name, isPreferred));
	}
	
	@Override
	public void setAnnotation(String annotation) {
		EntityHelper.setString(sg, "concept_extras", "annotation", id, annotation);
		
	}

	@Override
	public void setDescription(String description) {
		EntityHelper.setString(sg, "concept_extras", "description", id, description);
		
	}
	
	@Override
	public boolean deleteConceptAccession(String accession,
			DataSource elementOf) {
		
		try {
			PreparedStatement deleteCA = sg.getConnection().prepareStatement("delete from conceptaccession where (sid,id) = (?,?) and accession = ? and DataSource = ?");
			deleteCA.setLong(1, getSID());
			deleteCA.setInt(2, id);
			deleteCA.setString(3, accession);
			deleteCA.setString(4, elementOf.getId());
			
			deleteCA.execute();
			deleteCA.close();
			
			return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
		
	}

	@Override
	public boolean deleteConceptName(String name) {
		try {
			PreparedStatement deleteCN = sg.getConnection().prepareStatement("delete from conceptname where (sid,id) = (?,?) and name = ?");
			deleteCN.setLong(1, getSID());
			deleteCN.setInt(2, id);
			deleteCN.setString(3, name);
			
			deleteCN.execute();
			deleteCN.close();
			
			return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public String getAnnotation() {
		return EntityHelper.fetchString(sg, "concept_extras", "annotation", id);
	}

	@Override
	public ConceptAccession getConceptAccession(String accession, DataSource elementOf) {
		Connection conn = sg.getConnection();
		SQL3ConceptAccession ca = null;
		try {
			PreparedStatement getConceptAcc = conn.prepareStatement("select conceptaccession.key from conceptaccession where (sid, id) = (?,?) and accession = ? and DataSource = ?");
			
			getConceptAcc.setLong(1, getSID());
			getConceptAcc.setInt(2, id);
			getConceptAcc.setString(3, accession);
			getConceptAcc.setString(4, elementOf.getId());
			
			ResultSet rs = getConceptAcc.executeQuery();
			
			if (rs.next()) {
				 ca = new SQL3ConceptAccession(sg, rs.getInt(1));
			}
			rs.close();
			getConceptAcc.close();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return ca;
	}

	@Override
	public Set<ConceptAccession> getConceptAccessions() {
		Set<ConceptAccession> cas = new HashSet<ConceptAccession>();
		
		try {
			PreparedStatement getCAs = sg.getConnection().prepareStatement("select conceptaccession.key from conceptaccession where (sid, id) = (?,?)");
			getCAs.setLong(1, getSID());
			getCAs.setInt(2, id);
						
			ResultSet rs = getCAs.executeQuery();
			
			while(rs.next()) {
			
				cas.add(new SQL3ConceptAccession(sg, rs.getInt(1)));
				
			}
				
			rs.close();
			getCAs.close();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		return new HashSet<ConceptAccession>(cas);
	}

	@Override
	public ConceptName getConceptName() {
		Connection conn = sg.getConnection();
		SQL3ConceptName cn = null;
		
		try {
			PreparedStatement getCN = conn.prepareStatement("select conceptname.key from conceptname where (sid, id) = (?,?) order by pref limit 1");
			
			getCN.setLong(1, getSID());
			getCN.setInt(2, id);	
			
			ResultSet rs = getCN.executeQuery();
			
			if (rs.next()) {
				 cn = new SQL3ConceptName(sg, rs.getInt(1));
			}
			
			rs.close();
			getCN.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return cn;
	}

	@Override
	public ConceptName getConceptName(String name) {
		Connection conn = sg.getConnection();
		SQL3ConceptName cn = null;
		
		try {
			PreparedStatement getCN = conn.prepareStatement("select conceptname.key from conceptname where (sid, id) = (?,?) and name = ?");
			
			getCN.setLong(1, getSID());
			getCN.setInt(2, id);
			getCN.setString(3, name);			
			
			ResultSet rs = getCN.executeQuery();
			
			if (rs.next()) {
				 cn = new SQL3ConceptName(sg, rs.getInt(1));
			}
			
			rs.close();
			getCN.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return cn;
	}

	@Override
	public Set<ConceptName> getConceptNames() {
		Set<ConceptName> cas = new HashSet<ConceptName>();
		
		try {
			PreparedStatement getCNs = sg.getConnection().prepareStatement("select conceptname.key from conceptname where (sid, id) = (?,?)");
			getCNs.setLong(1, getSID());
			getCNs.setInt(2, id);
						
			ResultSet rs = getCNs.executeQuery();
			
			while(rs.next()) {
			
				cas.add(new SQL3ConceptName(sg, rs.getInt(1)));
				
			}
			
			rs.close();
			getCNs.close();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		return new HashSet<ConceptName>(cas);
	}

	@Override
	public String getDescription() {
		return EntityHelper.fetchString(sg, "concept_extras", "description", id);
	}

	@Override
	public DataSource getElementOf() {
		String a = EntityHelper.fetchString(sg, "concept", "DataSource", id);
		return sg.getMetaData().getDataSource(a);
	}

	@Override
	public ConceptClass getOfType() {
		String a = EntityHelper.fetchString(sg, "concept", "conceptClass", id);
		return sg.getMetaData().getConceptClass(a);
	}

	@Override
	public String getPID() {
		return EntityHelper.fetchString(sg, "concept", "parser_id", id);
	}

    @Override
    public void setPID(String pid)
    {
        // fixme: implement!!!
    }

    @Override
	public boolean inheritedFrom(ConceptClass cc) {
    	ConceptClass my_cc = getOfType();
    	while (!my_cc.equals(cc)) {
    		my_cc = my_cc.getSpecialisationOf();
    		if (my_cc == null) {
    			return false;
    		}
    	}
    	return true;
	}

	



}
