package csi.server.business.visualization.deprecated.timeline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import csi.server.business.visualization.graph.base.property.Property;

public class TemporalEvent implements Comparable<TemporalEvent> {

    protected String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    protected long start;
    protected long end;
    protected long duration;
    protected List<Integer> supportingRows;

    protected Map<String, List<Integer>> definitionSupportingRows;

    @SuppressWarnings("unchecked")
    protected List participants;

    Map<String, Property> properties;

    public TemporalEvent() {
        definitionSupportingRows = new HashMap<String, List<Integer>>();
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getDuration() {
        return duration;
    }

    @SuppressWarnings("unchecked")
    public List getParticipants() {
        if (participants == null) {
            participants = new ArrayList();
        }

        return participants;
    }

    public Map<String, Property> getProperties() {
        if (properties == null) {
            properties = new HashMap<String, Property>();
        }
        return properties;
    }

    public int compareTo(TemporalEvent that) {
        if (that == null) {
            return 1;
        }

        return this.id.compareTo(that.id);

    }

    public String toString() {
        return "Foo Bar";
    }

    public void addSupportingRow(String def, int row) {
        List<Integer> list = definitionSupportingRows.get(def);
        if (list == null) {
            list = new ArrayList<Integer>();
            definitionSupportingRows.put(def, list);
        }

        list.add(row);
    }

    public Map<String, List<Integer>> getSupportingRows() {
        if (definitionSupportingRows == null) {
            definitionSupportingRows = new HashMap<String, List<Integer>>();
        }
        return definitionSupportingRows;
    }
}
