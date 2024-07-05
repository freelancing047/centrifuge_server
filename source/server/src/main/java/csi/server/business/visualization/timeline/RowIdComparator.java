package csi.server.business.visualization.timeline;

import csi.shared.core.visualization.timeline.SingularTimelineEvent;

import java.util.Comparator;

public class RowIdComparator implements Comparator<SingularTimelineEvent> {

    @Override
    public int compare(SingularTimelineEvent o1, SingularTimelineEvent o2) {
        return Integer.compare(o1.getRowId(), o2.getRowId());
    }

}
