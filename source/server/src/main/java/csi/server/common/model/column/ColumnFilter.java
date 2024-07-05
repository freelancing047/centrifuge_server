package csi.server.common.model.column;

import java.util.ArrayList;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import csi.server.common.model.ModelObject;
import csi.server.common.model.filter.FilterOperandType;
import csi.server.common.model.filter.FilterOperatorType;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class ColumnFilter extends ModelObject {

    public String localColumnId;
    public boolean exclude = false;
    @Enumerated(EnumType.STRING)
    public FilterOperandType operandType = FilterOperandType.STATIC;
    @Enumerated(EnumType.STRING)
    public FilterOperatorType operator = FilterOperatorType.EQUALS;
    public String paramLocalId;
    @Column(length = 2147483647)
    @Lob
    public ArrayList<String> staticValues = new ArrayList<String>();
    private int ordinal;
    @ManyToOne
    private ColumnDef parent;

    public ColumnFilter() {
        super();
    }

    public ColumnDef getParent() {
        return parent;
    }

    public void setParent(ColumnDef parentIn) {
        parent = parentIn;
    }

    public int getOrdinal() {
        return ordinal;
    }

    public void setOrdinal(int ordinal) {
        this.ordinal = ordinal;
    }

    public String getLocalColumnId() {
        return localColumnId;
    }

    public void setLocalColumnId(String localColumnId) {
        this.localColumnId = localColumnId;
    }

    public boolean getExclude() {
        return exclude;
    }

    public void setExclude(boolean exclude) {
        this.exclude = exclude;
    }

    public FilterOperandType getOperandType() {
        return operandType;
    }

    public void setOperandType(FilterOperandType operandType) {
        this.operandType = operandType;
    }

    public FilterOperatorType getOperator() {
        return operator;
    }

    public void setOperator(FilterOperatorType operator) {
        this.operator = operator;
    }

    public String getParamLocalId() {
        return paramLocalId;
    }

    public void setParamLocalId(String paramLocalId) {
        this.paramLocalId = paramLocalId;
    }

    public ArrayList<String> getStaticValues() {
        return staticValues;
    }

    public void setStaticValues(ArrayList<String> staticValues) {
        this.staticValues = staticValues;
    }

    @Override
    public ColumnFilter clone() {

        ColumnFilter myClone = new ColumnFilter();

        super.cloneComponents(myClone);

        return cloneValues(myClone);
    }

    public ColumnFilter fullClone(ColumnDef parentIn) {

        ColumnFilter myClone = new ColumnFilter();

        super.fullCloneComponents(myClone);
        myClone.setParent(parentIn);

        return cloneValues(myClone);
    }

    public ColumnFilter cloneValues(ColumnFilter cloneIn) {

        cloneIn.setOrdinal(getOrdinal());
        cloneIn.setLocalColumnId(getLocalColumnId());
        cloneIn.setExclude(getExclude());
        cloneIn.setOperandType(getOperandType());
        cloneIn.setOperator(getOperator());
        cloneIn.setParamLocalId(getParamLocalId());
        cloneIn.setStaticValues(cloneStaticValues());

        return cloneIn;
    }

    @Override
    protected void debugContents(StringBuilder bufferIn, String indentIn) {

        debugObject(bufferIn, localColumnId, indentIn, "localColumnId");
        debugObject(bufferIn, exclude, indentIn, "exclude");
        debugObject(bufferIn, operandType, indentIn, "operandType");
        debugObject(bufferIn, operator, indentIn, "operator");
        debugObject(bufferIn, paramLocalId, indentIn, "paramLocalId");
        debugObject(bufferIn, staticValues, indentIn, "staticValues");
    }

    private ArrayList<String> cloneStaticValues() {

        if (null != getStaticValues()) {

            ArrayList<String> myList = new ArrayList<String>();

            for (String myItem : getStaticValues()) {

                myList.add(myItem);
            }

            return myList;

        } else {

            return null;
        }
    }
}
