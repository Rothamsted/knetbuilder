package net.sourceforge.ondex.core.sql3.entities;

import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.core.base.RelationKeyImpl;
import net.sourceforge.ondex.core.sql3.SQL3Graph;
import net.sourceforge.ondex.core.sql3.entities.subcomp.SQL3RelationAttribute;
import net.sourceforge.ondex.core.sql3.factories.FactoryForSubComps;
import net.sourceforge.ondex.core.sql3.helper.EntityHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class SQL3Relation extends SQL3Entity implements ONDEXRelation {

	/**
	 * Copy of SQL2, except for handling of Attribute
	 * 
	 * @author sckuo
	 */

	public SQL3Relation(SQL3Graph s, int k) {
		super(s, k, "relation");
	}

	public Attribute createAttribute(AttributeName attrname, Object value,
			boolean doIndex) {

		int gdsno = FactoryForSubComps.createAttribute(sg, this, attrname,
				doIndex);

		Attribute g = new SQL3RelationAttribute(sg, gdsno);
		g.setValue(value);

		return g;

	}

	public boolean deleteAttribute(AttributeName attrname) {

		Connection conn = sg.getConnection();
		try {

			PreparedStatement getAttribute = conn
					.prepareStatement("select gds.key from gds where (sid, id) = (?,?) and elementType = ? and attrName = ?");

			getAttribute.setLong(1, getSID());
			getAttribute.setInt(2, id);
			getAttribute.setString(3, "relation");
			getAttribute.setString(4, attrname.getId());

			ResultSet rs = getAttribute.executeQuery();

			if (rs.next()) {

				int gdsno = rs.getInt(1);
				PreparedStatement deleteG = sg.getConnection()
						.prepareStatement("delete from gds where gds.key = ?");
				deleteG.setInt(1, gdsno);
				deleteG.execute();
				deleteG.close();

				PreparedStatement deleteG2 = null;

				String an = attrname.getDataTypeAsString();

				if (an.equals("java.lang.String")
						|| an.equals("java.lang.Character")) {
					deleteG2 = sg.getConnection().prepareStatement(
							"delete from string_gds where gdsno = ?");
				} else if (an.equals("java.lang.Integer")) {
					deleteG2 = sg.getConnection().prepareStatement(
							"delete from integer_gds where gdsno = ?");
				} else if (an.equals("java.lang.Float")
						|| an.equals("java.lang.Double")) {
					deleteG2 = sg.getConnection().prepareStatement(
							"delete from double_gds where gdsno = ?");
				} else if (an.equals("java.lang.Boolean")) {
					deleteG2 = sg.getConnection().prepareStatement(
							"delete from boolean_gds where gdsno = ?");
				} else if (an.equals("java.lang.Long")) {
					deleteG2 = sg.getConnection().prepareStatement(
							"delete from long_gds where gdsno = ?");
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

	@Override
	public Attribute getAttribute(AttributeName attrname) {

		Connection conn = sg.getConnection();
		Attribute attribute = null;

		try {
			PreparedStatement getAttribute = conn
					.prepareStatement("select gds.key from gds where (sid, id) = (?,?) and elementType = ? and attrName = ?");

			getAttribute.setLong(1, getSID());
			getAttribute.setInt(2, id);
			getAttribute.setString(3, "relation");
			getAttribute.setString(4, attrname.getId());

			ResultSet rs = getAttribute.executeQuery();

			if (rs.next()) {
				attribute = new SQL3RelationAttribute(sg, rs.getInt(1));
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
		Set<Attribute> gdss = new HashSet<Attribute>();

		try {

			PreparedStatement getAttributes = sg
					.getConnection()
					.prepareStatement(
							"select gds.key from gds where (sid, id) = (?,?) and elementType = ?");
			getAttributes.setLong(1, getSID());
			getAttributes.setInt(2, id);
			getAttributes.setString(3, tableName);

			ResultSet rs = getAttributes.executeQuery();

			while (rs.next()) {

				gdss.add(new SQL3RelationAttribute(sg, rs.getInt(1)));

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
	public ONDEXConcept getFromConcept() {
		return sg.getConcept(EntityHelper.fetchInteger(sg, "relation", "fromC",
				id));
	}

	@Override
	public RelationKey getKey() {
		long sid = getSID();
		int from = getFromConcept().getId();
		int to = getToConcept().getId();
		String ofType = getOfType().getId();

		return new RelationKeyImpl(sid, from, to, ofType);
	}

	@Override
	public RelationType getOfType() {
		String a = EntityHelper.fetchString(sg, "relation", "relationType", id);
		return sg.getMetaData().getRelationType(a);
	}

	@Override
	public ONDEXConcept getToConcept() {
		return sg.getConcept(EntityHelper.fetchInteger(sg, "relation", "toC",
				id));
	}

	@Override
	public boolean inheritedFrom(RelationType rt) {
		RelationType my_rt = getOfType();
		while (!my_rt.equals(rt)) {
			my_rt = my_rt.getSpecialisationOf();
			if (my_rt == null) {
				return false;
			}
		}
		return true;
	}

}
