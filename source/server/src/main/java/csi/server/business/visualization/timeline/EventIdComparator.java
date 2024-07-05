package csi.server.business.visualization.timeline;

import java.util.Comparator;

import csi.shared.core.visualization.timeline.SingularTimelineEvent;

public class EventIdComparator implements Comparator<SingularTimelineEvent>{

    @Override
    public int compare(SingularTimelineEvent o1, SingularTimelineEvent o2) {
        if (o1.getEventDefinitionId() >= o2.getEventDefinitionId()) {
            return 1;
        } else {
            return -1;
        }
    }

}
