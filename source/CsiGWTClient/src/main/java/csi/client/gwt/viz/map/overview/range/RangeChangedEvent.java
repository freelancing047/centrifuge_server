package csi.client.gwt.viz.map.overview.range;

/**
 * @author Centrifuge Systems, Inc.
 */
public class RangeChangedEvent {

    private final Range range;

    public RangeChangedEvent(Range range) {
        this.range = range;
    }

    public Range getRange() {
        return range;
    }
}
