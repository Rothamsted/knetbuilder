package net.sourceforge.ondex.core.sql3.entities.subcomp;

import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.sql3.SQL3Graph;
import net.sourceforge.ondex.core.sql3.helper.SubCompHelper;
import net.sourceforge.ondex.exception.type.AccessDeniedException;

import java.io.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class SQL3Attribute implements Attribute {

    /*
      * This class clones SQL2 and basically just packages up the Attributees differently, where
      * SQL2 puts everything in as a Java object, SQL3 separates things out into Float/Double,
      * String/Char, Long, Int, and Booleans. This does nothing for efficiency actually, but
      * it does make the database searchable by value.
      *
      */

    protected String tableName = "gds";

    protected SQL3Graph sg;
    protected int gdsno;

    public SQL3Attribute(SQL3Graph s, int i) {
        sg = s;
        gdsno = i;
    }

    public Object getValue() {

        String an = getOfType().getDataTypeAsString();
        if (an.equals("java.lang.String") || an.equals("java.lang.Character")) {

            try {

                PreparedStatement getValueStmt = sg.getConnection().prepareStatement("select value from string_gds where string_gds.gdsno = ?");

                getValueStmt.setInt(1, gdsno);

                ResultSet rs = getValueStmt.executeQuery();
                String a = null;

                if (rs.next()) {
                    a = rs.getString("value");
                }

                rs.close();
                getValueStmt.close();

                if (an.equals("java.lang.String")) {
                    return a;
                } else {
                    return a.charAt(0);
                }

            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        } else if (an.equals("java.lang.Integer")) {

            try {

                PreparedStatement getValueStmt = sg.getConnection().prepareStatement("select value from integer_gds where integer_gds.gdsno = ?");

                getValueStmt.setInt(1, gdsno);

                ResultSet rs = getValueStmt.executeQuery();
                Integer a = null;

                if (rs.next()) {
                    a = rs.getInt("value");
                }

                rs.close();
                getValueStmt.close();

                return a;

            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        } else if (an.equals("java.lang.Float") || an.equals("java.lang.Double")) {

            try {

                PreparedStatement getValueStmt = sg.getConnection().prepareStatement("select value from double_gds where double_gds.gdsno = ?");

                getValueStmt.setInt(1, gdsno);

                ResultSet rs = getValueStmt.executeQuery();
                Double a = null;
                if (rs.next()) {
                    a = rs.getDouble("value");
                }
                rs.close();
                getValueStmt.close();

                if (an.equals("java.lang.Float")) {
                    return (float) (double) a;
                } else {
                    return a;
                }

            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        } else if (an.equals("java.lang.Boolean")) {

            try {

                PreparedStatement getValueStmt = sg.getConnection().prepareStatement("select value from boolean_gds where boolean_gds.gdsno = ?");

                getValueStmt.setInt(1, gdsno);

                ResultSet rs = getValueStmt.executeQuery();
                Boolean a = null;
                if (rs.next()) {
                    a = rs.getBoolean("value");
                }
                rs.close();
                getValueStmt.close();

                return a;

            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        } else if (an.equals("java.lang.Long")) {

            try {

                PreparedStatement getValueStmt = sg.getConnection().prepareStatement("select value from long_gds where long_gds.gdsno = ?");

                getValueStmt.setInt(1, gdsno);

                ResultSet rs = getValueStmt.executeQuery();
                Long a = null;
                if (rs.next()) {
                    a = rs.getLong("value");
                }
                rs.close();
                getValueStmt.close();

                return a;

            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        } else {

            try {

                PreparedStatement getValueStmt = sg.getConnection().prepareStatement("select value from " + tableName + " where blob_gds.gdsno = ?");

                getValueStmt.setInt(1, gdsno);

                ResultSet rs = getValueStmt.executeQuery();

                Object x = null;

                if (rs.next()) {

                    byte[] abc = rs.getBytes("value");
                    ByteArrayInputStream bias = new ByteArrayInputStream(abc);

                    ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(bias));
                    x = ois.readObject();

                    ois.close();
                    rs.close();
                    getValueStmt.close();
                }

                return x;


            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return null;
    }

    public void setValue(Object value) {

        String an = getOfType().getDataTypeAsString();

        if (an.equals("java.lang.String") || an.equals("java.lang.Character")) {

            try {

                PreparedStatement setString = sg.getConnection().prepareStatement("update string_gds set value = ? where string_gds.gdsno = ?");

                setString.setString(1, value.toString());
                setString.setInt(2, gdsno);

                setString.execute();
                setString.close();

            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        } else if (an.equals("java.lang.Integer")) {

            try {

                PreparedStatement setString = sg.getConnection().prepareStatement("update integer_gds set value = ? where integer_gds.gdsno = ?");

                setString.setInt(1, (Integer) value);
                setString.setInt(2, gdsno);

                setString.execute();
                setString.close();

            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        } else if (an.equals("java.lang.Float") || an.equals("java.lang.Double")) {

            try {

                PreparedStatement setString = sg.getConnection().prepareStatement("update double_gds set value = ? where double_gds.gdsno = ?");
                if (value instanceof Double) {
                    setString.setDouble(1, (Double) value);
                } else if (value instanceof Float) {
                    float v = (Float) value;
                    double v2 = v;
                    setString.setDouble(1, v2);
                }
                setString.setInt(2, gdsno);

                setString.execute();
                setString.close();

            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        } else if (an.equals("java.lang.Boolean")) {

            try {

                PreparedStatement setString = sg.getConnection().prepareStatement("update boolean_gds set value = ? where boolean_gds.gdsno = ?");

                setString.setBoolean(1, (Boolean) value);
                setString.setInt(2, gdsno);

                setString.execute();
                setString.close();

            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        } else if (an.equals("java.lang.Long")) {

            try {

                PreparedStatement setString = sg.getConnection().prepareStatement("update long_gds set value = ? where long_gds.gdsno = ?");

                setString.setLong(1, (Long) value);
                setString.setInt(2, gdsno);

                setString.execute();
                setString.close();

            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        } else {

            try {

                PreparedStatement setBlobItem = sg.getConnection().prepareStatement("update blob_gds set value = ? where blob_gds.gdsno = ?");

                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutputStream out = new ObjectOutputStream(bos);

                out.writeObject(value);
                out.flush();
                out.close();

                byte[] buf = bos.toByteArray();
                bos.flush();
                bos.close();

                setBlobItem.setBytes(1, buf);
                setBlobItem.setInt(2, gdsno);

                setBlobItem.execute();
                setBlobItem.close();

            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    @Override
    public AttributeName getOfType() {
        String anId = SubCompHelper.fetchString(sg, tableName, "attrName", gdsno);
        return sg.getMetaData().getAttributeName(anId);
    }

    @Override
    public int getOwnerId() {
        return SubCompHelper.fetchInteger(sg, tableName, "id", gdsno);
    }

    @Override
    public boolean isDoIndex() {
        return SubCompHelper.fetchBoolean(sg, tableName, "isDoIndex", gdsno);
    }

    @Override
    public void setDoIndex(boolean doIndex) throws AccessDeniedException {
        SubCompHelper.setBoolean(sg, tableName, "isDoIndex", gdsno, doIndex);

    }

    @Override
    public long getSID() {
        return sg.getSID();
    }

    @Override
    public boolean inheritedFrom(AttributeName attributeName) {
        return getOfType().isAssignableTo(attributeName);
    }

	@Override
	public int compareTo(Attribute o) {
		return getOfType().compareTo(o.getOfType());
	}

}
