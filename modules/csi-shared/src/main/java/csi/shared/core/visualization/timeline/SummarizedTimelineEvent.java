package csi.shared.core.visualization.timeline;

import java.util.HashSet;
import java.util.Set;

import csi.shared.core.util.IntCollection;

public class SummarizedTimelineEvent extends BaseTimelineEvent {

    private IntCollection eventIds;
    private Set<String> colors = new HashSet<String>();
    
    public SummarizedTimelineEvent(BaseTimelineEvent event) {
       this();
       combine(event);
       this.setEndTime(event.getEndTime());
       this.setStartTime(event.getStartTime());
       this.setTrackValue(event.getTrackValue());
       
       this.setDotSize(event.getDotSize());
       this.setSelected(event.isSelected());
    }
    public SummarizedTimelineEvent() {
        eventIds = new IntCollection();
        if(getEventDefinitionId() > 0)
            eventIds.add(getEventDefinitionId());
    }
    

    public void combine(BaseTimelineEvent event){
        if(event instanceof SingularTimelineEvent){
            eventIds.add(((SingularTimelineEvent) event).getEventDefinitionId());
            
        } else if(event instanceof SummarizedTimelineEvent){
            eventIds.addAll(((SummarizedTimelineEvent) event).getEventIds());
        }
        
        if(event.getColorValue() != null){
            if(getColorValue() == null){
                this.setColorValue(event.getColorValue());
            }
            colors.add(event.getColorValue()); 

        }
        

        if(event.isSelected()){
            setSelected(true);
        }
    }
    
    @Override
    public int compareTo(BaseTimelineEvent o1) {
        if (o1.getStartTime() == null) {
            return -1;
        }
        if (getStartTime() == null) {
            return 1;
        }
        if (o1.getStartTime().longValue() == getStartTime().longValue()) {
            try {
                if(o1.getEventDefinitionId() == 0){
                    return -1;
                }
                
                if(getEventDefinitionId() == 0){
                    return o1.getEventDefinitionId() < getEventIds().get(0) ? 1: -1;
                }
                                
                if (o1.getEventDefinitionId() >= getEventDefinitionId()) {
                    return 1;
                } else {
                    return -1;
                }
            } catch (Exception exception) {
                //no-op
            }
        }
        return o1.getStartTime().longValue() < getStartTime().longValue() ? 1 : -1;
    }
    
    
    
    public IntCollection getEventIds() {
        return eventIds;
    }
    public void setEventIds(IntCollection eventIds) {
        this.eventIds = eventIds;
    }
    public Set<String> getColors() {
        return colors;
    }
    public void setColors(Set<String> colors) {
        this.colors = colors;
    }
    
}
