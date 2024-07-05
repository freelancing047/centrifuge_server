package csi.shared.core.visualization.timeline;

import java.util.HashMap;
import java.util.List;
import java.util.Set;


public class SummaryTimelineResult extends TimelineResult<SummarizedTimelineEvent> {

    private String groupName;
    private List<SingularTimelineEvent> singularEvents;
    private Set<String> legendInfo;

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public List<SingularTimelineEvent> getSingularEvents() {
        return singularEvents;
    }

    public void setSingularEvents(List<SingularTimelineEvent> singularEvents) {
        this.singularEvents = singularEvents;
    }

    public Set<String> getLegendInfo() {
        return legendInfo;
    }

    public void setLegendInfo(Set<String> legendInfo) {
        this.legendInfo = legendInfo;
    }
    

    
}
