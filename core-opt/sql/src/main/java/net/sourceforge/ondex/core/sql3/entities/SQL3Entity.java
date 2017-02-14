package net.sourceforge.ondex.core.sql3.entities;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXEntity;
import net.sourceforge.ondex.core.sql3.SQL3Graph;
import net.sourceforge.ondex.core.sql3.metadata.SQL3EvidenceType;
import net.sourceforge.ondex.core.util.BitSetFunctions;
import net.sourceforge.ondex.exception.type.AccessDeniedException;
import net.sourceforge.ondex.exception.type.NullValueException;

public abstract class SQL3Entity implements ONDEXEntity {
	
	/**
	 * Duplicate of the ones in SQL2, except accepting SQL3Graph & stuff rather than SQL2Graph
	 * 
	 * @author sckuo
	 */
	
	protected int id;
	protected SQL3Graph sg;
	protected String tableName;
	
	public SQL3Entity(SQL3Graph s, int k, String table) {
		
		id = k;
		sg = s;
		tableName = table;
		
	}
	
	public String type() {
		return tableName;
	}

	public long getSID() {
		return sg.getSID();
	}
	
	@Override
	public int getId() {
		return id;
	}

	@Override
	public void addTag(ONDEXConcept ac) throws AccessDeniedException,
			NullValueException {
		
		try {
			
			PreparedStatement createRowItem = sg.getConnection().prepareStatement("insert into context (sid, id, elementType, context) values (?,?,?,?)");
			
			// set params
			createRowItem.setLong(1, sg.getSID());
			createRowItem.setInt(2, id);
			createRowItem.setString(3, tableName);
			createRowItem.setInt(4, ac.getId());
			
			// run
			createRowItem.execute();
			createRowItem.close();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
	
	@Override
	public boolean removeTag(ONDEXConcept ac) {
		
		try {
			
			PreparedStatement deleteTag = sg.getConnection().prepareStatement("delete from context where (sid,id,elementType) = (?,?,?) and context = ?");
			
			deleteTag.setLong(1, sg.getSID());
			deleteTag.setInt(2, id);
			deleteTag.setString(3, tableName);
			deleteTag.setInt(4, ac.getId());
			
			int rowCountAffected = deleteTag.executeUpdate();
			
			deleteTag.close();
			
			if (rowCountAffected > 0) {
				return true;
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return false;
	}
	
	@Override
	public boolean removeEvidenceType(EvidenceType evidencetype) {
		
		try {
			
			PreparedStatement deleteEvidenceType = sg.getConnection().prepareStatement("delete from evidence where (sid, id, elementType, evidence_id) = (?,?,?,?)");
			
			deleteEvidenceType.setLong(1, sg.getSID());
			deleteEvidenceType.setInt(2, id);
			deleteEvidenceType.setString(3, tableName);
			deleteEvidenceType.setString(4, evidencetype.getId());
			
			deleteEvidenceType.execute();
			deleteEvidenceType.close();
			
			return true;
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	@Override
	public void addEvidenceType(EvidenceType evidencetype) {
		
		try {
			
			PreparedStatement createRowItem = sg.getConnection().prepareStatement("insert into evidence (sid, id, elementType, evidence_id) values (?,?,?,?)");
			
			createRowItem.setLong(1, sg.getSID());
			createRowItem.setInt(2, id);
			createRowItem.setString(3, tableName);
			createRowItem.setString(4, evidencetype.getId());
			
			createRowItem.execute();
			createRowItem.close();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	@Override
	public Set<EvidenceType> getEvidence() {
		
		//System.out.println("getEvidence " + this.type() + id);
		Set<EvidenceType> ets = new HashSet<EvidenceType>();
		
		try {
			PreparedStatement getETs = sg.getConnection().prepareStatement("select evidence_id from evidence where (sid, id) = (?,?) and elementType = ?");
			getETs.setLong(1, getSID());
			getETs.setInt(2, id);
			getETs.setString(3, tableName);
						
			ResultSet rs = getETs.executeQuery();
			
			while(rs.next()) {
			
				ets.add(new SQL3EvidenceType(sg, rs.getString(1)));
				
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
	public Set<ONDEXConcept> getTags() {
		//System.out.println("getTags " + this.type() + id);
		Set<Integer> is = new HashSet<Integer>();
		
		try {
			PreparedStatement getTags = sg.getConnection().prepareStatement("select context.context from context where (sid, id) = (?,?) and elementType = ?");
			getTags.setLong(1, getSID());
			getTags.setInt(2, id);
			getTags.setString(3, tableName);
			
			ResultSet rs = getTags.executeQuery();
			
			while(rs.next()) {
			
				is.add(rs.getInt(1));
				
			}
			
			rs.close();
			getTags.close();
		
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return BitSetFunctions.create(sg, ONDEXConcept.class, is);
		
	}

}
