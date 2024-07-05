package csi.client.gwt.widget.ui.surface;

import com.google.common.base.Objects;
import com.google.common.base.MoreObjects;

public class OrdinalAxisExtent {

    private int startIndex;
    private int endIndex;

    private int dataIndexStart;
    private int dataIndexEnd;

    OrdinalAxisExtent(int startIndex, int endIndex) {
        super();
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }

    public int getDataIndexStart() {
        return dataIndexStart;
    }

    public void setDataIndexStart(int dataIndexStart) {
        this.dataIndexStart = dataIndexStart;
    }

    public int getDataIndexEnd() {
        return dataIndexEnd;
    }

    public void setDataIndexEnd(int dataIndexEnd) {
        this.dataIndexEnd = dataIndexEnd;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getStartIndex(), getEndIndex());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (obj instanceof OrdinalAxisExtent == false) {
            return false;
        } else {
            OrdinalAxisExtent typed = (OrdinalAxisExtent) obj;
            return Objects.equal(getStartIndex(), typed.getStartIndex())
                    && Objects.equal(getEndIndex(), typed.getEndIndex());
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("startIndex", getStartIndex())
                .add("endIndex", getEndIndex())
                .toString();
    }
}