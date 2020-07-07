package net.sourceforge.ondex.core.sql3.metadata;

import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.Hierarchy;
import net.sourceforge.ondex.core.Unit;
import net.sourceforge.ondex.core.sql3.SQL3Graph;
import net.sourceforge.ondex.core.sql3.helper.MetaDataHelper;

public class SQL3AttrName extends SQL3MetaData implements AttributeName {

    /**
     * Duplicate of the ones in SQL2, except accepting SQL3Graph rather than SQL2Graph
     *
     * @author sckuo
     */


    public SQL3AttrName(SQL3Graph s, String id) {
        super(s, id, "attributename");

    }

    @Override
    public Class<?> getDataType() {
        try {
            return Class.forName(getDataTypeAsString());
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public String getDataTypeAsString() {

        return MetaDataHelper.fetchString(sg, tableName, "class", mid);

    }

    @Override
    public AttributeName getSpecialisationOf() {

        String aName = MetaDataHelper.fetchString(sg, tableName, "specOf", mid);

        if (aName == null || aName.isEmpty()) {
            return null;
        }

        return sg.getMetaData().getAttributeName(aName);
    }

    @Override
    public Unit getUnit() {
        String uName = MetaDataHelper.fetchString(sg, tableName, "unit", mid);

        if (uName == null || uName.isEmpty()) {
            return null;
        }

        return sg.getMetaData().getUnit(uName);
    }

    @Override
    public void setSpecialisationOf(AttributeName specialisationOf) {

        MetaDataHelper.setString(sg, tableName, "specOf", mid, specialisationOf.getId());

    }

    @Override
    public boolean isAssignableTo(AttributeName possibleAncestor) {
        return Hierarchy.Helper.transitiveParent(possibleAncestor, this);
    }

    @Override
    public boolean isAssignableFrom(AttributeName possibleDescendant) {
        return Hierarchy.Helper.transitiveParent(this, possibleDescendant);
    }

    @Override
    public void setUnit(Unit unit) {

        String value = unit.getId();
        MetaDataHelper.setString(sg, tableName, "unit", mid, value);

    }

    public int compareTo(AttributeName o) {

        if (o != null) {
            return this.getId().compareTo(o.getId());
        } else {
            throw new RuntimeException("Invalid comparison object");
        }
	}

}
