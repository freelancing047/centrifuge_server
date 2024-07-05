package csi.server.common.dto;



import java.util.ArrayList;

import com.google.common.base.Objects;
import com.google.common.base.MoreObjects;
import com.google.gwt.user.client.rpc.IsSerializable;


public class ChartDimensionDTO implements IsSerializable {

    public String label;
    public String type;
    public int count;
    public ArrayList<String> ranges;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public ArrayList<String> getRanges() {
        return ranges;
    }

    public void setRanges(ArrayList<String> ranges) {
        this.ranges = ranges;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this) //
                .add("label", getLabel()) //
                .add("type", getType()) //
                .add("count", getCount()) //
                .add("ranges", getRanges()) //
                .omitNullValues() //
                .toString();
    }
}
