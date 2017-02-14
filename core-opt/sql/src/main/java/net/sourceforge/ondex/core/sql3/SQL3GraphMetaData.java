package net.sourceforge.ondex.core.sql3;

import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.core.sql3.factories.FactoryForMetaData;
import net.sourceforge.ondex.core.sql3.metadata.*;
import net.sourceforge.ondex.exception.type.AccessDeniedException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class SQL3GraphMetaData implements ONDEXGraphMetaData {

	protected SQL3Graph sg;
	private MetaDataFactory mdf;
	protected long sid;
	
	/*
	 * Cache 
	 */
	
	private HashMap<String, DataSource> cvs;
	private HashMap<String, EvidenceType> evidenceTypes;
	private HashMap<String, Unit> units;
	private HashMap<String, AttributeName> attribs;
	private HashMap<String, ConceptClass> conceptClasses;
	private HashMap<String, RelationType> relationTypes;
	
	public SQL3GraphMetaData(SQL3Graph s) {
		
		sg = s;
		sid = s.getSID();
		mdf = new MetaDataFactory(this);
		
		this.evidenceTypes = new HashMap<String, EvidenceType>(2000);
		this.cvs = new HashMap<String, DataSource>(2000);
		this.units = new HashMap<String, Unit>(2000);
		this.attribs = new HashMap<String, AttributeName>(2000);
		this.conceptClasses = new HashMap<String, ConceptClass>(2000);
		this.relationTypes = new HashMap<String, RelationType>(2000);
		
	}	
	
	public boolean deleteAttributeName(String id) {
		try {
			
			// first delete the entry itself
			PreparedStatement deleteAN = sg.getConnection().prepareStatement("delete from attributename where (sid,id) = (?,?)");
			deleteAN.setLong(1, sid);
			deleteAN.setString(2, id);
			deleteAN.execute();
			deleteAN.close();
			
			// then delete all the Attribute values using this attributeName
			String an = getAttributeName(id).getDataTypeAsString();
			
			PreparedStatement selectAttribute = sg.getConnection().prepareStatement("select gds.key from gds where sid = ? and attrName = ?)");
			selectAttribute.setLong(1, sid);
			selectAttribute.setString(2, id);
			ResultSet rs = selectAttribute.executeQuery();
			
			while (rs.next()) {
				
				int gdsno = rs.getInt(1);
				
				PreparedStatement deleteG2 = null;
				
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
			selectAttribute.close();
			
			// then delete all the Attributes using this attributeName
			PreparedStatement deleteAN2 = sg.getConnection().prepareStatement("delete from gds where sid = ? and attrName = ?)");
			deleteAN2.setLong(1, sid);
			deleteAN2.setString(2, id);
			deleteAN2.execute();
			deleteAN2.close();
			
			return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return false;
	}

	@Override
	public void associateGraph(ONDEXGraph g) throws AccessDeniedException {
		
		if (sid == -1L || sid == g.getSID()) {
			sid = sg.getSID();
		} else {
			throw new AccessDeniedException("MetaData is already associated to another graph!");
		}
		
	}

	@Override
	public boolean checkAttributeName(String id) {
		if (attribs.containsKey(id)) {
			return true;
		}
		
		try {
			PreparedStatement checkExistsAN = sg.getConnection().prepareStatement("select distinct id from attributename where (sid,id) = (?,?)");
			checkExistsAN.setLong(1, sid);
			checkExistsAN.setString(2, id);
			
			ResultSet rsResultSet = checkExistsAN.executeQuery();
			
			if (rsResultSet.next()) {
				return true;
			}
			
			rsResultSet.close();
			checkExistsAN.close();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return false;
	}

	@Override
	public boolean checkDataSource(String id) {
		if (cvs.containsKey(id)) {
			return true;
		}
		
		try {
			PreparedStatement checkExistDataSource = sg.getConnection().prepareStatement("select distinct id from cv where (sid,id) = (?,?)");
			checkExistDataSource.setLong(1, sid);
			checkExistDataSource.setString(2, id);
			
			ResultSet rsResultSet = checkExistDataSource.executeQuery();
			
			if (rsResultSet.next()) {
				return true;
			}
			
			rsResultSet.close();
			checkExistDataSource.close();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return false;
	}

	@Override
	public boolean checkConceptClass(String id) {
		if (conceptClasses.containsKey(id)) {
			return true;
		}			
		
		try {
			PreparedStatement checkExistCC = sg.getConnection().prepareStatement("select distinct id from conceptclass where (sid,id) = (?,?)");
			checkExistCC.setLong(1, sid);
			checkExistCC.setString(2, id);
			
			ResultSet rsResultSet = checkExistCC.executeQuery();
			
			if (rsResultSet.next()) {
				return true;
			}
			
			rsResultSet.close();
			checkExistCC.close();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return false;
	}

	@Override
	public boolean checkEvidenceType(String id) {
		if (evidenceTypes.containsKey(id)) {
			return true;
		}
		
		try {
			PreparedStatement checkExistET = sg.getConnection().prepareStatement("select distinct id from evidencetype where (sid,id) = (?,?)");
			checkExistET.setLong(1, sid);
			checkExistET.setString(2, id);
			
			ResultSet rsResultSet = checkExistET.executeQuery();
			
			if (rsResultSet.next()) {
				return true;
			}
			
			rsResultSet.close();
			checkExistET.close();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return false;
	}

	@Override
	public boolean checkRelationType(String id) {
		if (relationTypes.containsKey(id)) {
			return true;
		}
		
		try {
			PreparedStatement checkExistRT = sg.getConnection().prepareStatement("select distinct id from relationtype where (sid,id) = (?,?)");
			checkExistRT.setLong(1, sid);
			checkExistRT.setString(2, id);
			
			ResultSet rsResultSet = checkExistRT.executeQuery();
			
			if (rsResultSet.next()) {
				return true;
			}
			
			rsResultSet.close();
			checkExistRT.close();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return false;
	}

	@Override
	public boolean checkUnit(String id) {
		if (units.containsKey(id)) {
			return true;
		}
		
		try {
			PreparedStatement checkExistUnit = sg.getConnection().prepareStatement("select distinct id from unit where (sid,id) = (?,?)");
			checkExistUnit.setLong(1, sid);
			checkExistUnit.setString(2, id);
			
			ResultSet rsResultSet = checkExistUnit.executeQuery();
			
			if (rsResultSet.next()) {
				return true;
			}
			
			rsResultSet.close();
			checkExistUnit.close();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return false;
	}

	@Override
	public AttributeName createAttributeName(String id, String fullname,
			String description, Unit unit, Class<?> datatype,
			AttributeName specialisationOf) {
		FactoryForMetaData.createAttributeName(sg, id, fullname, description, unit, datatype, specialisationOf);
		
		return getAttributeName(id);
	}

	@Override
	public DataSource createDataSource(String id, String fullname, String description) {
		FactoryForMetaData.createDataSource(sg, id, fullname, description);
		return getDataSource(id);
	}

	@Override
	public ConceptClass createConceptClass(String id, String fullname,
			String description, ConceptClass specialisationOf) {
		FactoryForMetaData.createConceptClass(sg, id, fullname, description, specialisationOf);
		return getConceptClass(id);
	}

	@Override
	public EvidenceType createEvidenceType(String id, String fullname,
			String description) {
		FactoryForMetaData.createEvidenceType(sg, id, fullname, description);
		return getEvidenceType(id);
	}

	@Override
	public RelationType createRelationType(String id, String fullname,
			String description, String inverseName, boolean isAntisymmetric,
			boolean isReflexive, boolean isSymmetric, boolean isTransitiv,
			RelationType specialisationOf) {
		FactoryForMetaData.createRelationType(sg, id, fullname, description, inverseName, isAntisymmetric, isReflexive, isSymmetric, isTransitiv, specialisationOf);
		
		return getRelationType(id);
	}

	@Override
	public Unit createUnit(String id, String fullname, String description) {
		FactoryForMetaData.createUnit(sg, id, fullname, description);
		return getUnit(id);
	}

	
	
	
	
	
	
	@Override
	public boolean deleteDataSource(String id) {
		try {
			
			// first delete this cv
			PreparedStatement deleteDataSource = sg.getConnection().prepareStatement("delete from cv where (sid,id) = (?,?)");
			deleteDataSource.setLong(1, sid);
			deleteDataSource.setString(2, id);
			deleteDataSource.execute();
			deleteDataSource.close();
			
			// now delete all the concepts using this DataSource
			PreparedStatement deleteDataSource2 = sg.getConnection().prepareStatement("select id from concept where sid = ? and DataSource = ?");
			deleteDataSource2.setLong(1, sid);
			deleteDataSource2.setString(2, id);
			ResultSet rs = deleteDataSource2.executeQuery();
			
			while (rs.next()) {
				sg.deleteConcept(rs.getInt(1));
			}
			rs.close();
			deleteDataSource2.close();
			
			// now delete all the conceptAccessions using this cv
			PreparedStatement deleteDataSource3 = sg.getConnection().prepareStatement("delete from conceptaccession where sid = ? and DataSource = ?");
			deleteDataSource3.setLong(1, sid);
			deleteDataSource3.setString(2, id);
			deleteDataSource3.execute();
			deleteDataSource3.close();
			
            return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return false;
	}

	@Override
	public boolean deleteConceptClass(String id) {
		try {
			// first delete the concept class
			PreparedStatement deleteCC = sg.getConnection().prepareStatement("delete from conceptclass where (sid,id) = (?,?)");
			deleteCC.setLong(1, sid);
			deleteCC.setString(2, id);
			deleteCC.execute();
			deleteCC.close();
			
			// then delete all the concepts using this class
			PreparedStatement deleteCC2 = sg.getConnection().prepareStatement("select id from concept where sid = ? and conceptClass = ?");
			deleteCC2.setLong(1, sid);
			deleteCC2.setString(2, id);
			ResultSet rs = deleteCC2.executeQuery();
			
			while (rs.next()) {
				sg.deleteConcept(rs.getInt(1));
			}
			rs.close();
			deleteCC2.close();

            return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return false;
	}

	@Override
	public boolean deleteEvidenceType(String id) {
		try {
			
			// first delete this evidenceType
			PreparedStatement deleteET = sg.getConnection().prepareStatement("delete from evidencetype where (sid,id) = (?,?)");
			deleteET.setLong(1, sid);
			deleteET.setString(2, id);
			deleteET.execute();
			deleteET.close();
			
			// then delete all the evidences using this evindenceType
			// TODO: Warning, what happens if this leaves concepts/relations with 0 evidence?
			PreparedStatement deleteET2 = sg.getConnection().prepareStatement("delete from evidence where sid = ? and evidence_id = ?");
			deleteET2.setLong(1, sid);
			deleteET2.setString(2, id);
			deleteET2.execute();
			deleteET2.close();

            return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return false;
	}

	@Override
	public boolean deleteRelationType(String id) {
		try {
			// first delete the relation type
			PreparedStatement deleteRT = sg.getConnection().prepareStatement("delete from relationtype where (sid,id) = (?,?)");
			deleteRT.setLong(1, sid);
			deleteRT.setString(2, id);
			deleteRT.execute();
			deleteRT.close();
			
			// then delete all the relations using this relation type
			PreparedStatement deleteRT2 = sg.getConnection().prepareStatement("select id from relation where sid = ? and relationType = ?");
			deleteRT2.setLong(1, sid);
			deleteRT2.setString(2, id);
			ResultSet rs = deleteRT2.executeQuery();
			
			while (rs.next()) {
				sg.deleteRelation(rs.getInt(1));
			}
			rs.close();
			deleteRT2.close();

            return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return false;
	}

	@Override
	public boolean deleteUnit(String id) {
		try {
			PreparedStatement deleteUnit = sg.getConnection().prepareStatement("delete from unit where (sid,id) = (?,?)");
			deleteUnit.setLong(1, sid);
			deleteUnit.setString(2, id);
			
			deleteUnit.execute();
			deleteUnit.close();
			
			return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return false;
	}

	@Override
	public AttributeName getAttributeName(String id) {
		if (attribs.containsKey(id)) {
			return attribs.get(id);
		} else {
			SQL3AttrName a = null;
			try {
				PreparedStatement checkAttributeName = sg.getConnection().prepareStatement("select distinct id from attributename where (sid,id) = (?,?)");
				checkAttributeName.setLong(1, sid);
				checkAttributeName.setString(2, id);
				
				ResultSet rsResultSet = checkAttributeName.executeQuery();
				
				if (rsResultSet.next()) {
					a = new SQL3AttrName(sg, id);
					attribs.put(id, a);
				}
				
				rsResultSet.close();
				checkAttributeName.close();
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
			return a;
		}
	}

	@Override
	public Set<AttributeName> getAttributeNames() {
		Set<AttributeName> ets = new HashSet<AttributeName>();
		
		try {
			PreparedStatement getANs = sg.getConnection().prepareStatement("select distinct id from attributename where sid = ?");
			getANs.setLong(1, sid);
						
			ResultSet rs = getANs.executeQuery();
			
			while(rs.next()) {
			
				ets.add(getAttributeName(rs.getString(1)));
				
			}
			
			rs.close();
			getANs.close();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		return new HashSet<AttributeName>(ets);
	}

	@Override
	public DataSource getDataSource(String id) {
		if (cvs.containsKey(id)) {
			return cvs.get(id);
		} else {
			SQL3DataSource a = null;
			try {
				PreparedStatement checkExistDataSource = sg.getConnection().prepareStatement("select distinct id from cv where (sid,id) = (?,?)");
				checkExistDataSource.setLong(1, sid);
				checkExistDataSource.setString(2, id);
				
				ResultSet rsResultSet = checkExistDataSource.executeQuery();
				
				if (rsResultSet.next()) {
					a = new SQL3DataSource(sg, id);
					cvs.put(id, a);
				}
				
				rsResultSet.close();
				checkExistDataSource.close();
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
			return a;
		}
	}

	@Override
	public Set<DataSource> getDataSources() {
		Set<DataSource> ets = new HashSet<DataSource>();
		
		try {
			PreparedStatement getDataSource = sg.getConnection().prepareStatement("select distinct id from cv where sid = ?");
			getDataSource.setLong(1, sid);
						
			ResultSet rs = getDataSource.executeQuery();
			
			while(rs.next()) {
			
				ets.add(getDataSource(rs.getString(1)));
				
			}
			
			rs.close();
			getDataSource.close();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		return new HashSet<DataSource>(ets);
	}

	@Override
	public ConceptClass getConceptClass(String id) {
		if (conceptClasses.containsKey(id)) {
			return conceptClasses.get(id);
		} else {
			SQL3ConceptClass a = null;
			try {
				PreparedStatement checkCC = sg.getConnection().prepareStatement("select distinct id from conceptclass where (sid,id) = (?,?)");
				checkCC.setLong(1, sid);
				checkCC.setString(2, id);
				
				ResultSet rsResultSet = checkCC.executeQuery();
				
				if (rsResultSet.next()) {
					a = new SQL3ConceptClass(sg, id);
					conceptClasses.put(id, a);
				}
				
				rsResultSet.close();
				checkCC.close();
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return a;
		}
	}

	@Override
	public Set<ConceptClass> getConceptClasses() {

		Set<ConceptClass> ets = new HashSet<ConceptClass>();
		
		try {
			PreparedStatement getCCs = sg.getConnection().prepareStatement("select distinct id from conceptclass where sid = ?");
			getCCs.setLong(1, sid);
						
			ResultSet rs = getCCs.executeQuery();
			
			while(rs.next()) {
			
				ets.add(getConceptClass(rs.getString(1)));
				
			}
			
			rs.close();
			getCCs.close();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		return new HashSet<ConceptClass>(ets);
	}

	@Override
	public EvidenceType getEvidenceType(String id) {
		if (evidenceTypes.containsKey(id)) {
			return evidenceTypes.get(id);
		} else {
			SQL3EvidenceType a = null;
			try {
				PreparedStatement checkExistET = sg.getConnection().prepareStatement("select distinct id from evidencetype where (sid,id) = (?,?)");
				checkExistET.setLong(1, sid);
				checkExistET.setString(2, id);
				
				ResultSet rsResultSet = checkExistET.executeQuery();
				
				if (rsResultSet.next()) {
					a = new SQL3EvidenceType(sg, id);
					evidenceTypes.put(id, a);
				}
				
				rsResultSet.close();
				checkExistET.close();
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return a;
		}
	}

	@Override
	public Set<EvidenceType> getEvidenceTypes() {
		Set<EvidenceType> ets = new HashSet<EvidenceType>();
		
		try {
			PreparedStatement getETs = sg.getConnection().prepareStatement("select distinct id from evidencetype where sid = ?");
			getETs.setLong(1, sid);
						
			ResultSet rs = getETs.executeQuery();
			
			while(rs.next()) {
			
				ets.add(getEvidenceType(rs.getString(1)));
				
			}
			
			rs.close();
			getETs.close();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		return new HashSet<EvidenceType>(ets);
	}

	@Override
	public MetaDataFactory getFactory() {
		return mdf;
	}

	@Override
	public RelationType getRelationType(String id) {
		if (relationTypes.containsKey(id)) {
			return relationTypes.get(id);
		} else {
			SQL3RelationType a = null;
			try {
				PreparedStatement checkExistRT = sg.getConnection().prepareStatement("select distinct id from relationtype where (sid,id) = (?,?)");
				checkExistRT.setLong(1, sid);
				checkExistRT.setString(2, id);
				
				ResultSet rsResultSet = checkExistRT.executeQuery();
				
				if (rsResultSet.next()) {
					a = new SQL3RelationType(sg, id);
					relationTypes.put(id, a);
				}
				
				rsResultSet.close();
				checkExistRT.close();
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return a;
		}
	}

	@Override
	public Set<RelationType> getRelationTypes() {
		Set<RelationType> ets = new HashSet<RelationType>();
		
		try {
			PreparedStatement getRTs = sg.getConnection().prepareStatement("select distinct id from relationtype where sid = ?");
			getRTs.setLong(1, sid);
			
			ResultSet rs = getRTs.executeQuery();
		
			while(rs.next()) {
				
				ets.add(getRelationType(rs.getString(1)));
				
			}
			
			rs.close();
			getRTs.close();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return new HashSet<RelationType>(ets);
	}

	@Override
	public Unit getUnit(String id) {
		if (units.containsKey(id)) {
			return units.get(id);
		} else {
			
			SQL3Unit a = null;
			try {
				PreparedStatement checkUnit = sg.getConnection().prepareStatement("select distinct id from unit where (sid,id) = (?,?)");
				checkUnit.setLong(1, sid);
				checkUnit.setString(2, id);
				
				ResultSet rsResultSet = checkUnit.executeQuery();
				
				if (rsResultSet.next()) {
					a = new SQL3Unit(sg, id);
					units.put(id, a);
				}
				
				rsResultSet.close();
				checkUnit.close();
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
			return a;
		}
	}

	@Override
	public Set<Unit> getUnits() {
		Set<Unit> ets = new HashSet<Unit>();
		
		try {
			PreparedStatement getU = sg.getConnection().prepareStatement("select distinct id from unit where sid = ?");
			getU.setLong(1, sid);
						
			ResultSet rs = getU.executeQuery();
			
			while(rs.next()) {
			
				ets.add(getUnit(rs.getString(1)));
				
			}
			
			rs.close();
			getU.close();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		return new HashSet<Unit>(ets);
	}

	@Override
	public long getSID() {
		return sid;
	}

}
