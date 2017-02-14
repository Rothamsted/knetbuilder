package net.sourceforge.ondex.core.sql3.factories;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;

import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.sql3.SQL3Graph;

public class FactoryForEntities {
	
	/**
	 * Duplicate of the ones in SQL2, except accepting SQL3Graph & stuff rather than SQL2Graph
	 * 
	 * @author sckuo
	 */

	
	public static void createConcept(SQL3Graph sg, int concept_id, String pid, String annotation,
			String description, DataSource elementOf, ConceptClass ofType,
			Collection<EvidenceType> evidence) {
		
		try {
			// populate concept
			
			PreparedStatement createRow = sg.getConnection().prepareStatement("insert into concept (sid, id, parser_id, DataSource, conceptClass) values (?,?,?,?,?)");
			
			createRow.setLong(1, sg.getSID());
			createRow.setInt(2, concept_id);
			createRow.setString(3, pid);
			createRow.setString(4, elementOf.getId());
			createRow.setString(5, ofType.getId());
			
			createRow.execute();
			createRow.close();
			// populate concept_extras
			
			PreparedStatement createRow2 = sg.getConnection().prepareStatement("insert into concept_extras (sid, id, annotation, description) values (?,?,?,?)");
			
			createRow2.setLong(1, sg.getSID());
			createRow2.setInt(2, concept_id);
			createRow2.setString(3, annotation);
			createRow2.setString(4, description);
			
			createRow2.execute();
			createRow2.close();

			// deal with evidence
			PreparedStatement createEvidenceRow = sg.getConnection().prepareStatement("insert into evidence (sid, id, elementType, evidence_id) values (?,?,?,?)");
			Iterator<EvidenceType> it = evidence.iterator();
			while (it.hasNext()) {
			
				EvidenceType et = it.next();
				createEvidenceRow.clearParameters();
				createEvidenceRow.setLong(1, sg.getSID());
				createEvidenceRow.setInt(2, concept_id);
				createEvidenceRow.setString(3, "concept");
				createEvidenceRow.setString(4, et.getId());
				
				createEvidenceRow.execute();
			}
			createEvidenceRow.close();
			
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	
	}
	
	public static void createRelation(SQL3Graph sg, int rid, ONDEXConcept fromConcept, ONDEXConcept toConcept, 
			RelationType ofType, Collection<EvidenceType> evidence) { 

			try {
				
				// populate relation
								
				PreparedStatement createRow = sg.getConnection().prepareStatement("insert into relation (sid, id, fromC, toC, relationType) values (?,?,?,?,?)");
				
				createRow.setLong(1, sg.getSID());
				createRow.setInt(2, rid);
				createRow.setInt(3, fromConcept.getId());
				createRow.setInt(4, toConcept.getId());
				createRow.setString(5, ofType.getId());
				
				createRow.execute();
				createRow.close();
				
				// deal with evidence
				PreparedStatement createEvidenceRow = sg.getConnection().prepareStatement("insert into evidence (sid, id, elementType, evidence_id) values (?,?,?,?)");
				Iterator<EvidenceType> it = evidence.iterator();
				while (it.hasNext()) {
				
					EvidenceType et = it.next();
					createEvidenceRow.clearParameters();
					createEvidenceRow.setLong(1, sg.getSID());
					createEvidenceRow.setInt(2, rid);
					createEvidenceRow.setString(3, "relation");
					createEvidenceRow.setString(4, et.getId());
					
					createEvidenceRow.execute();
				}
				createEvidenceRow.close();
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		

	}
	
}
