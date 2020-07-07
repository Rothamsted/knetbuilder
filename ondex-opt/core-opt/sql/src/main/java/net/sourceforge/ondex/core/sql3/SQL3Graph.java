package net.sourceforge.ondex.core.sql3;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EntityFactory;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXGraphMetaData;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.sql3.entities.SQL3Concept;
import net.sourceforge.ondex.core.sql3.entities.SQL3Relation;
import net.sourceforge.ondex.core.sql3.factories.FactoryForEntities;
import net.sourceforge.ondex.core.util.BitSetFunctions;

public class SQL3Graph implements ONDEXGraph {

	protected long sid;
	private int conceptId = 1;
	private int relationId = 1;

	protected EntityFactory ef;
	protected SQL3GraphMetaData sgm;

	protected Connection conn;

	public SQL3Graph(int id, String connectionString, String user, String pass) {

		this(connectionString, user, pass);

		sid = id;

		try {
			PreparedStatement fetchLastConceptID = conn
					.prepareStatement("select id from concept where sid = ? order by id desc limit 1");
			fetchLastConceptID.setLong(1, sid);
			ResultSet rs = fetchLastConceptID.executeQuery();
			if (rs.next()) {
				conceptId = rs.getInt("id");
			} else {
				conceptId = 0;
			}
			rs.close();
			fetchLastConceptID.close();

		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			PreparedStatement fetchLastConceptID = conn
					.prepareStatement("select id from relation where sid = ? order by id desc limit 1");
			fetchLastConceptID.setLong(1, sid);
			ResultSet rs = fetchLastConceptID.executeQuery();
			if (rs.next()) {
				relationId = rs.getInt("id");
			} else {
				relationId = 0;
			}
			rs.close();
			fetchLastConceptID.close();

		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		sgm = new SQL3GraphMetaData(this);
	}

	public SQL3Graph(String connectionString, String user, String pass) {

		try {
			conn = DriverManager.getConnection(connectionString, user, pass);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ef = new EntityFactory(this);

	}

	public SQL3Graph(String name, String connectionString, String user,
			String pass) {

		this(connectionString, user, pass);

		int s = 0;
		try {
			PreparedStatement fetchLastID = conn
					.prepareStatement("select sid from graph order by sid desc limit 1");
			ResultSet rs = fetchLastID.executeQuery();
			if (rs.next()) {
				s = rs.getInt("sid");
			} else {
				s = 0;
			}
			rs.close();
			fetchLastID.close();

		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		sid = s + 1;

		try {
			PreparedStatement createRowItem = conn
					.prepareStatement("insert into graph values (?,?,?,?)");

			createRowItem.setLong(1, sid);
			createRowItem.setString(2, name);
			createRowItem.setString(3, "");
			createRowItem.setBoolean(4, false);

			createRowItem.execute();
			createRowItem.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sgm = new SQL3GraphMetaData(this);

	}

	@Override
	public ONDEXConcept createConcept(String pid, String annotation,
			String description, DataSource elementOf, ConceptClass ofType,
			Collection<EvidenceType> evidence) {
		int cid = conceptId;
		conceptId++;

		FactoryForEntities.createConcept(this, cid, pid, annotation,
				description, elementOf, ofType, evidence);

		return getConcept(cid);
	}

	@Override
	public ONDEXRelation createRelation(ONDEXConcept fromConcept,
			ONDEXConcept toConcept, RelationType ofType,
			Collection<EvidenceType> evidence) {
		int rid = relationId;
		relationId++;

		FactoryForEntities.createRelation(this, rid, fromConcept, toConcept,
				ofType, evidence);

		return getRelation(rid);
	}

	public boolean deleteConcept(int id) {

		try {
			// delete all associated relations

			for (ONDEXRelation r : getRelationsOfConcept(getConcept(id))) {

				deleteRelation(r.getId());

			}

			// delete from concept

			PreparedStatement deleteC = conn
					.prepareStatement("delete from concept where (sid,id) = (?,?)");

			deleteC.setLong(1, sid);
			deleteC.setInt(2, id);
			deleteC.execute();
			deleteC.close();

			// delete from concept_extras

			PreparedStatement deleteC2 = conn
					.prepareStatement("delete from concept_extras where (sid,id) = (?,?)");

			deleteC2.setLong(1, sid);
			deleteC2.setInt(2, id);
			deleteC2.execute();
			deleteC2.close();

			// delete from conceptaccession

			PreparedStatement deleteC3 = conn
					.prepareStatement("delete from conceptaccession where (sid,id) = (?,?)");

			deleteC3.setLong(1, sid);
			deleteC3.setInt(2, id);
			deleteC3.execute();
			deleteC3.close();

			// delete from conceptname

			PreparedStatement deleteC4 = conn
					.prepareStatement("delete from conceptaccession where (sid,id) = (?,?)");

			deleteC4.setLong(1, sid);
			deleteC4.setInt(2, id);
			deleteC4.execute();
			deleteC4.close();

			// delete from tag

			PreparedStatement deleteC5 = conn
					.prepareStatement("delete from context where (sid,id) = (?,?) and elementType = ?");

			deleteC5.setLong(1, sid);
			deleteC5.setInt(2, id);
			deleteC5.setString(3, "concept");

			deleteC5.execute();
			deleteC5.close();

			// delete it as a tag for other things
			PreparedStatement deleteC6 = conn
					.prepareStatement("delete from context where sid = ? and context = ?");

			deleteC6.setLong(1, sid);
			deleteC6.setInt(2, id);

			deleteC6.execute();
			deleteC6.close();

			// delete from evidence
			PreparedStatement deleteC7 = conn
					.prepareStatement("delete from evidence where (sid,id) = (?,?) and elementType = ?");

			deleteC7.setLong(1, sid);
			deleteC7.setInt(2, id);
			deleteC7.setString(3, "concept");

			deleteC7.execute();
			deleteC7.close();

			// delete gds values

			PreparedStatement selectAttribute = conn
					.prepareStatement("select gds.key,attrName from gds where (sid,id) = (?,?) and elementType = ?");
			selectAttribute.setLong(1, sid);
			selectAttribute.setInt(2, id);
			selectAttribute.setString(3, "concept");

			ResultSet rs = selectAttribute.executeQuery();

			while (rs.next()) {

				String an = sgm.getAttributeName(rs.getString("attrName"))
						.getDataTypeAsString();
				int gdsno = rs.getInt("gds.key");

				PreparedStatement deleteG2 = null;

				if (an.equals("java.lang.String")
						|| an.equals("java.lang.Character")) {
					deleteG2 = conn
							.prepareStatement("delete from string_gds where gdsno = ?");
				} else if (an.equals("java.lang.Integer")) {
					deleteG2 = conn
							.prepareStatement("delete from integer_gds where gdsno = ?");
				} else if (an.equals("java.lang.Float")
						|| an.equals("java.lang.Double")) {
					deleteG2 = conn
							.prepareStatement("delete from double_gds where gdsno = ?");
				} else if (an.equals("java.lang.Boolean")) {
					deleteG2 = conn
							.prepareStatement("delete from boolean_gds where gdsno = ?");
				} else if (an.equals("java.lang.Long")) {
					deleteG2 = conn
							.prepareStatement("delete from long_gds where gdsno = ?");
				}

				deleteG2.setInt(1, gdsno);
				deleteG2.execute();
				deleteG2.close();

			}
			rs.close();
			selectAttribute.close();

			// delete from gds
			PreparedStatement deleteC8 = conn
					.prepareStatement("delete from gds where (sid,id) = (?,?) and elementType = ?");

			deleteC8.setLong(1, sid);
			deleteC8.setInt(2, id);
			deleteC8.setString(3, "concept");

			deleteC8.execute();
			deleteC8.close();

			// create return
			return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return false;
	}

	@Override
	public boolean deleteRelation(int id) {
		try {

			// first delete the relation
			PreparedStatement deleteR = conn
					.prepareStatement("delete from relation where (sid,id) = (?,?)");
			deleteR.setLong(1, sid);
			deleteR.setInt(2, id);
			deleteR.execute();
			deleteR.close();

			// then delete all relation in all tags
			PreparedStatement deleteR2 = conn
					.prepareStatement("delete from context where (sid,id) = (?,?) and elementType = ?");
			deleteR2.setLong(1, sid);
			deleteR2.setInt(2, id);
			deleteR2.setString(3, "relation");
			deleteR2.execute();
			deleteR2.close();

			// then delete all evidence for this relation
			PreparedStatement deleteR3 = conn
					.prepareStatement("delete from evidence where (sid,id) = (?,?) and elementType = ?");
			deleteR3.setLong(1, sid);
			deleteR3.setInt(2, id);
			deleteR3.setString(3, "relation");
			deleteR3.execute();
			deleteR3.close();

			// delete gds values

			PreparedStatement selectAttribute = conn
					.prepareStatement("select gds.key,attrName from gds where (sid,id) = (?,?) and elementType = ?");
			selectAttribute.setLong(1, sid);
			selectAttribute.setInt(2, id);
			selectAttribute.setString(3, "relation");

			ResultSet rs = selectAttribute.executeQuery();

			while (rs.next()) {

				String an = sgm.getAttributeName(rs.getString("attrName"))
						.getDataTypeAsString();
				int gdsno = rs.getInt("gds.key");

				PreparedStatement deleteG2 = null;

				if (an.equals("java.lang.String")
						|| an.equals("java.lang.Character")) {
					deleteG2 = conn
							.prepareStatement("delete from string_gds where gdsno = ?");
				} else if (an.equals("java.lang.Integer")) {
					deleteG2 = conn
							.prepareStatement("delete from integer_gds where gdsno = ?");
				} else if (an.equals("java.lang.Float")
						|| an.equals("java.lang.Double")) {
					deleteG2 = conn
							.prepareStatement("delete from double_gds where gdsno = ?");
				} else if (an.equals("java.lang.Boolean")) {
					deleteG2 = conn
							.prepareStatement("delete from boolean_gds where gdsno = ?");
				} else if (an.equals("java.lang.Long")) {
					deleteG2 = conn
							.prepareStatement("delete from long_gds where gdsno = ?");
				}

				deleteG2.setInt(1, gdsno);
				deleteG2.execute();
				deleteG2.close();

			}
			rs.close();
			selectAttribute.close();

			// then delete all Attribute for this relation
			PreparedStatement deleteR4 = conn
					.prepareStatement("delete from gds where (sid,id) = (?,?) and elementType = ?");
			deleteR4.setLong(1, sid);
			deleteR4.setInt(2, id);
			deleteR4.setString(3, "relation");
			deleteR4.execute();
			deleteR4.close();

			return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return false;
	}

	@Override
	public boolean deleteRelation(ONDEXConcept fromConcept,
			ONDEXConcept toConcept, RelationType ofType) {

		ONDEXRelation r = getRelation(fromConcept, toConcept, ofType);
		return deleteRelation(r.getId());
	}

	@Override
	public Set<ONDEXConcept> getAllTags() {
		Set<Integer> is = new HashSet<Integer>();

		try {
			PreparedStatement getCons = conn
					.prepareStatement("select distinct context.context from context where sid = ? and elementType = ?");
			getCons.setLong(1, sid);
			getCons.setString(2, "concept");

			ResultSet rs = getCons.executeQuery();

			while (rs.next()) {

				is.add(rs.getInt(1));

			}

			rs.close();
			getCons.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return BitSetFunctions.create(this, ONDEXConcept.class, is);
	}

	@Override
	public ONDEXConcept getConcept(int id) {
		ONDEXConcept a = null;
		try {
			PreparedStatement getConceptSQL = conn
					.prepareStatement("select distinct id from concept where (sid,id) = (?,?)");
			getConceptSQL.setLong(1, sid);
			getConceptSQL.setInt(2, id);

			ResultSet rsResultSet = getConceptSQL.executeQuery();

			if (rsResultSet.next()) {
				a = new SQL3Concept(this, id);
			}

			rsResultSet.close();
			getConceptSQL.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return a;
	}

	@Override
	public Set<ONDEXConcept> getConcepts() {
		Set<Integer> is = new HashSet<Integer>();

		try {
			PreparedStatement getCs = conn
					.prepareStatement("select id from concept where sid = ?");
			getCs.setLong(1, sid);

			ResultSet rs = getCs.executeQuery();

			while (rs.next()) {

				is.add(rs.getInt(1));

			}

			rs.close();
			getCs.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return BitSetFunctions.create(this, ONDEXConcept.class, is);
	}

	public Set<ONDEXConcept> getConceptsOfAttributeName(AttributeName an) {
		// System.out.println("Get all concepts of attribute " +an.getId());
		Set<Integer> is = new HashSet<Integer>();

		try {
			PreparedStatement getCsofAN = conn
					.prepareStatement("select distinct id from gds where sid = ? and elementType = ? and attrName = ?");
			getCsofAN.setLong(1, sid);
			getCsofAN.setString(2, "concept");
			getCsofAN.setString(3, an.getId());

			ResultSet rs = getCsofAN.executeQuery();

			while (rs.next()) {

				is.add(rs.getInt(1));

			}

			rs.close();
			getCsofAN.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return BitSetFunctions.unmodifiableSet(BitSetFunctions.create(this,
				ONDEXConcept.class, is));
	}

	@Override
	public Set<ONDEXConcept> getConceptsOfConceptClass(ConceptClass cc) {
		Set<Integer> is = new HashSet<Integer>();

		try {
			PreparedStatement getCofCC = conn
					.prepareStatement("select id from concept where sid = ? and conceptClass = ?");
			getCofCC.setLong(1, sid);
			getCofCC.setString(2, cc.getId());

			ResultSet rs = getCofCC.executeQuery();

			while (rs.next()) {

				is.add(rs.getInt(1));

			}

			rs.close();
			getCofCC.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return BitSetFunctions.create(this, ONDEXConcept.class, is);
	}

	@Override
	public Set<ONDEXConcept> getConceptsOfDataSource(DataSource dataSource) {
		Set<Integer> is = new HashSet<Integer>();

		try {
			PreparedStatement getCofDataSource = conn
					.prepareStatement("select id from concept where sid = ? and DataSource = ?");
			getCofDataSource.setLong(1, sid);
			getCofDataSource.setString(2, dataSource.getId());

			ResultSet rs = getCofDataSource.executeQuery();

			while (rs.next()) {

				is.add(rs.getInt(1));

			}

			rs.close();
			getCofDataSource.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return BitSetFunctions.create(this, ONDEXConcept.class, is);
	}

	@Override
	public Set<ONDEXConcept> getConceptsOfEvidenceType(EvidenceType et) {
		Set<Integer> is = new HashSet<Integer>();

		try {
			PreparedStatement getCsofET = conn
					.prepareStatement("select distinct id from evidence where sid = ? and elementType = ? and evidence_id = ?");
			getCsofET.setLong(1, sid);
			getCsofET.setString(2, "concept");
			getCsofET.setString(3, et.getId());

			ResultSet rs = getCsofET.executeQuery();

			while (rs.next()) {

				is.add(rs.getInt(1));

			}

			rs.close();
			getCsofET.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return BitSetFunctions.create(this, ONDEXConcept.class, is);
	}

	@Override
	public Set<ONDEXConcept> getConceptsOfTag(ONDEXConcept ac) {
		Set<Integer> is = new HashSet<Integer>();

		try {
			PreparedStatement getCofTag = conn
					.prepareStatement("select id from context where sid = ? and elementType = ? and context.context = ?");
			getCofTag.setLong(1, sid);
			getCofTag.setString(2, "concept");
			getCofTag.setInt(3, ac.getId());

			ResultSet rs = getCofTag.executeQuery();

			while (rs.next()) {

				is.add(rs.getInt(1));

			}

			rs.close();
			getCofTag.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return BitSetFunctions.create(this, ONDEXConcept.class, is);
	}

	public Connection getConnection() {

		return conn;

	}

	@Override
	public EntityFactory getFactory() {
		return ef;
	}

	@Override
	public ONDEXGraphMetaData getMetaData() {
		return sgm;
	}

	@Override
	public String getName() {
		String n = null;
		try {

			PreparedStatement isR = conn
					.prepareStatement("select name from graph where sid = ?");

			isR.setLong(1, sid);

			ResultSet rs = isR.executeQuery();

			if (rs.next()) {
				n = rs.getString(1);
			}
			rs.close();
			isR.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return n;
	}

	@Override
	public ONDEXRelation getRelation(int id) {
		ONDEXRelation a = null;
		try {
			PreparedStatement getRelation = conn
					.prepareStatement("select distinct id from relation where (sid,id) = (?,?)");
			getRelation.setLong(1, sid);
			getRelation.setInt(2, id);

			ResultSet rsResultSet = getRelation.executeQuery();

			if (rsResultSet.next()) {
				a = new SQL3Relation(this, id);
			}

			rsResultSet.close();
			getRelation.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return a;
	}

	@Override
	public ONDEXRelation getRelation(ONDEXConcept fromConcept,
			ONDEXConcept toConcept, RelationType ofType) {
		ONDEXRelation r = null;

		try {
			PreparedStatement getR = conn
					.prepareStatement("select id from relation where sid = ? and fromC = ? and toC = ? and relationType = ?");
			getR.setLong(1, sid);
			getR.setInt(2, fromConcept.getId());
			getR.setInt(3, toConcept.getId());
			getR.setString(4, ofType.getId());

			ResultSet rs = getR.executeQuery();

			if (rs.next()) {
				r = new SQL3Relation(this, rs.getInt(1));
			}

			rs.close();
			getR.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return r;
	}

	@Override
	public Set<ONDEXRelation> getRelations() {
		Set<Integer> is = new HashSet<Integer>();

		try {
			PreparedStatement getRs = conn
					.prepareStatement("select id from relation where sid = ?");
			getRs.setLong(1, sid);

			ResultSet rs = getRs.executeQuery();

			while (rs.next()) {

				is.add(rs.getInt(1));

			}

			rs.close();
			getRs.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return BitSetFunctions.create(this, ONDEXRelation.class, is);
	}

	public Set<ONDEXRelation> getRelationsOfAttributeName(AttributeName an) {

		// System.out.println("getRelationsOfAttributeName " +an.getId());
		Set<Integer> is = new HashSet<Integer>();

		try {
			PreparedStatement getRsofAN = conn
					.prepareStatement("select distinct id from gds where sid = ? and elementType = ? and attrName = ?");
			getRsofAN.setLong(1, sid);
			getRsofAN.setString(2, "relation");
			getRsofAN.setString(3, an.getId());

			ResultSet rs = getRsofAN.executeQuery();

			while (rs.next()) {

				is.add(rs.getInt(1));

			}

			rs.close();
			getRsofAN.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return BitSetFunctions.create(this, ONDEXRelation.class, is);
	}

	@Override
	public Set<ONDEXRelation> getRelationsOfConcept(ONDEXConcept concept) {
		Set<Integer> is = new HashSet<Integer>();

		try {
			PreparedStatement getRsOfC = conn
					.prepareStatement("select distinct id from relation where sid = ? and (fromC = ? or toC = ? or qual = ?)");
			int cid = concept.getId();
			getRsOfC.setLong(1, sid);
			getRsOfC.setInt(2, cid);
			getRsOfC.setInt(3, cid);
			getRsOfC.setInt(4, cid);

			ResultSet rs = getRsOfC.executeQuery();

			while (rs.next()) {

				is.add(rs.getInt(1));

			}

			rs.close();
			getRsOfC.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return BitSetFunctions.create(this, ONDEXRelation.class, is);
	}

	@Override
	public Set<ONDEXRelation> getRelationsOfConceptClass(ConceptClass cc) {
		Set<Integer> is = new HashSet<Integer>();

		try {
			PreparedStatement getCofCC = conn
					.prepareStatement("select id from concept where sid = ? and conceptClass = ?");
			getCofCC.setLong(1, sid);
			getCofCC.setString(2, cc.getId());

			ResultSet rs = getCofCC.executeQuery();

			PreparedStatement getRsOfC = conn
					.prepareStatement("select distinct id from relation where sid = ? and (fromC = ? or toC = ? or qual = ?)");

			while (rs.next()) {

				int cid = rs.getInt(1);
				getRsOfC.clearParameters();
				getRsOfC.setLong(1, sid);
				getRsOfC.setInt(2, cid);
				getRsOfC.setInt(3, cid);
				getRsOfC.setInt(4, cid);

				ResultSet rs2 = getRsOfC.executeQuery();

				while (rs2.next()) {

					is.add(rs2.getInt(1));

				}
				rs2.close();

			}

			rs.close();
			getCofCC.close();
			getRsOfC.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return BitSetFunctions.create(this, ONDEXRelation.class, is);
	}

	@Override
	public Set<ONDEXRelation> getRelationsOfDataSource(DataSource dataSource) {
		Set<Integer> is = new HashSet<Integer>();

		try {
			PreparedStatement getCofDataSource = conn
					.prepareStatement("select id from concept where sid = ? and DataSource = ?");
			getCofDataSource.setLong(1, sid);
			getCofDataSource.setString(2, dataSource.getId());

			ResultSet rs = getCofDataSource.executeQuery();

			PreparedStatement getRsOfC = conn
					.prepareStatement("select distinct id from relation where sid = ? and (fromC = ? or toC = ? or qual = ?)");

			while (rs.next()) {

				int cid = rs.getInt(1);
				getRsOfC.clearParameters();
				getRsOfC.setLong(1, sid);
				getRsOfC.setInt(2, cid);
				getRsOfC.setInt(3, cid);
				getRsOfC.setInt(4, cid);

				ResultSet rs2 = getRsOfC.executeQuery();

				while (rs2.next()) {

					is.add(rs2.getInt(1));

				}

				rs2.close();

			}

			rs.close();
			getCofDataSource.close();
			getRsOfC.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return BitSetFunctions.create(this, ONDEXRelation.class, is);
	}

	@Override
	public Set<ONDEXRelation> getRelationsOfEvidenceType(EvidenceType et) {
		Set<Integer> is = new HashSet<Integer>();

		try {
			PreparedStatement getRsofET = conn
					.prepareStatement("select distinct id from evidence where sid = ? and elementType = ? and evidence_id = ?");
			getRsofET.setLong(1, sid);
			getRsofET.setString(2, "relation");
			getRsofET.setString(3, et.getId());

			ResultSet rs = getRsofET.executeQuery();

			while (rs.next()) {

				is.add(rs.getInt(1));

			}

			rs.close();
			getRsofET.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return BitSetFunctions.create(this, ONDEXRelation.class, is);
	}

	@Override
	public Set<ONDEXRelation> getRelationsOfRelationType(RelationType rt) {
		Set<Integer> is = new HashSet<Integer>();

		try {
			PreparedStatement getRofRT = conn
					.prepareStatement("select id from relation where sid = ? and relationType = ?");
			getRofRT.setLong(1, sid);
			getRofRT.setString(2, rt.getId());

			ResultSet rs = getRofRT.executeQuery();

			while (rs.next()) {

				is.add(rs.getInt(1));

			}

			rs.close();
			getRofRT.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return BitSetFunctions.create(this, ONDEXRelation.class, is);
	}

	@Override
	public Set<ONDEXRelation> getRelationsOfTag(ONDEXConcept ac) {
		Set<Integer> is = new HashSet<Integer>();

		try {
			PreparedStatement getRofTag = conn
					.prepareStatement("select id from context where sid = ? and elementType = ? and context.context = ?");
			getRofTag.setLong(1, sid);
			getRofTag.setString(2, "relation");
			getRofTag.setInt(3, ac.getId());

			ResultSet rs = getRofTag.executeQuery();

			while (rs.next()) {

				is.add(rs.getInt(1));

			}

			rs.close();
			getRofTag.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return BitSetFunctions.create(this, ONDEXRelation.class, is);
	}

	@Override
	public long getSID() {
		return sid;
	}

	@Override
	public boolean isReadOnly() {
		try {

			PreparedStatement isR = conn
					.prepareStatement("select readOnly from graph where sid = ?");

			isR.setLong(1, sid);

			ResultSet rs = isR.executeQuery();
			boolean b = false;
			if (rs.next()) {
				b = rs.getBoolean("readOnly");
			}
			rs.close();
			isR.close();

			return b;

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return false;
	}

}
