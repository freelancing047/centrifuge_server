package csi.server.common.model.operator;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.google.common.base.Objects;

import csi.server.common.enumerations.ComparingToken;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.model.DataSetOp;
import csi.server.common.model.ModelObject;
import csi.server.common.model.column.ColumnDef;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class OpMapItem extends ModelObject {

    @ManyToOne
    protected DataSetOp parent;
    protected String leftTableLocalId;
    protected String leftColumnLocalId;
    protected String rightTableLocalId;
    protected String rightColumnLocalId;
    @Enumerated(value = EnumType.STRING)
    protected CsiDataType castToType;
    @Enumerated(value = EnumType.STRING)
    protected ComparingToken comparingToken;

    public OpMapItem() {
        super();
    }

    public OpMapItem(DataSetOp parentIn, String leftTableLocalIdIn, String leftColumnLocalIdIn,
                     String rightTableLocalIdIn, String rightColumnLocalIdIn) {
        super();

        parent = parentIn;
        leftTableLocalId = leftTableLocalIdIn;
        leftColumnLocalId = leftColumnLocalIdIn;
        rightTableLocalId = rightTableLocalIdIn;
        rightColumnLocalId = rightColumnLocalIdIn;
    }

    public String getLeftColumnKey() {

        return leftColumnLocalId;
    }

    public String getRightColumnKey() {

        return rightColumnLocalId;
    }

    public String getOtherColumnId(String columnIdIn) {

        return (null == columnIdIn)
                    ? null
                    : (columnIdIn.equals(getRightColumnLocalId()))
                        ? getLeftColumnLocalId()
                        : ((columnIdIn.equals(getLeftColumnLocalId()))
                            ? getRightColumnLocalId()
                            : null);
    }

    public DataSetOp getParent() {
        return parent;
    }

    public void setParent(DataSetOp parentIn) {
        parent = parentIn;
    }

    public String getLeftTableLocalId() {
        return leftTableLocalId;
    }

    public void setLeftTableLocalId(String leftTableLocalId) {
        this.leftTableLocalId = leftTableLocalId;
    }

    public String getLeftColumnLocalId() {
        return leftColumnLocalId;
    }

    public void setLeftColumnLocalId(String leftColumnLocalId) {
        this.leftColumnLocalId = leftColumnLocalId;
    }

    public String getRightTableLocalId() {
        return rightTableLocalId;
    }

    public void setRightTableLocalId(String rightTableLocalId) {
        this.rightTableLocalId = rightTableLocalId;
    }

    public String getRightColumnLocalId() {
        return rightColumnLocalId;
    }

    public void setRightColumnLocalId(String rightColumnLocalId) {
        this.rightColumnLocalId = rightColumnLocalId;
    }

    public void setCastToType(CsiDataType castToTypeIn) {

        castToType = castToTypeIn;
    }

    public CsiDataType getCastToType() {

        return castToType;
    }

    public void setComparingToken(ComparingToken comparingTokenIn) {

        comparingToken = comparingTokenIn;
    }

    public ComparingToken getComparingToken() {

        return comparingToken;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getLeftTableLocalId(), getLeftColumnLocalId(), getRightTableLocalId(),
                getRightColumnLocalId());
    }

    /**
     * @param column
     * @return The table that is the counterpart for the mapping involving the given column or null if no such
     * counterpart exists (i.e., column doesn't participate in the mapping).
     */
    public String getCounterpartTableInvolving(ColumnDef column) {
        // Column local-ids are uuids so we don't need to test table as well.
        if (column.getLocalId().equals(getLeftColumnLocalId())) {
            return getRightTableLocalId();
        } else if (column.getLocalId().equals(getRightColumnLocalId())) {
            return getLeftTableLocalId();
        } else {
            return null;
        }
    }

   @Override
   public boolean equals(Object obj) {
      return (this == obj) ||
             ((obj != null) &&
              (obj instanceof OpMapItem) &&
              Objects.equal(getLeftTableLocalId(), ((OpMapItem) obj).getLeftTableLocalId()) &&
              Objects.equal(getLeftColumnLocalId(), ((OpMapItem) obj).getLeftColumnLocalId()) &&
              Objects.equal(getRightTableLocalId(), ((OpMapItem) obj).getRightTableLocalId()) &&
              Objects.equal(getRightColumnLocalId(), ((OpMapItem) obj).getRightColumnLocalId()) &&
              Objects.equal(getCastToType(), ((OpMapItem) obj).getCastToType()) &&
              Objects.equal(getComparingToken(), ((OpMapItem) obj).getComparingToken()));
   }

    @Override
    public OpMapItem clone() {

        OpMapItem myClone = new OpMapItem();

        super.cloneComponents(myClone);
        myClone.setParent(parent);

        return cloneValues(myClone);
    }

    public OpMapItem fullClone(DataSetOp parentIn) {

        OpMapItem myClone = new OpMapItem();

        super.fullCloneComponents(myClone);
        myClone.setParent(parentIn);

        return cloneValues(myClone);
    }

    public OpMapItem cloneValues(OpMapItem cloneIn) {

        cloneIn.setLeftTableLocalId(getLeftTableLocalId());
        cloneIn.setLeftColumnLocalId(getLeftColumnLocalId());
        cloneIn.setRightTableLocalId(getRightTableLocalId());
        cloneIn.setRightColumnLocalId(getRightColumnLocalId());
        cloneIn.setCastToType(getCastToType());
        cloneIn.setComparingToken(getComparingToken());

        return cloneIn;
    }

    @Override
    protected void debugContents(StringBuilder bufferIn, String indentIn) {

        debugObject(bufferIn, leftTableLocalId, indentIn, "leftTableLocalId");
        debugObject(bufferIn, leftColumnLocalId, indentIn, "leftColumnLocalId");
        debugObject(bufferIn, rightTableLocalId, indentIn, "rightTableLocalId");
        debugObject(bufferIn, rightColumnLocalId, indentIn, "rightColumnLocalId");
        debugObject(bufferIn, castToType, indentIn, "castToType");
        debugObject(bufferIn, comparingToken, indentIn, "comparingToken");
    }
}
