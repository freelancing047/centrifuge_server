package csi.client.gwt.viz.timeline.scheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.shared.EventBus;

import csi.client.gwt.viz.timeline.model.EventProxy;
import csi.client.gwt.viz.timeline.model.TimeScale;
import csi.client.gwt.viz.timeline.model.TimelineTrackModel;
import csi.client.gwt.viz.timeline.model.summary.SummaryEventProxy;
import csi.client.gwt.viz.timeline.view.summary.SummaryCompleteEvent;

public class DeferredSimpleLayoutCommand<T extends EventProxy> implements ScheduledCommand {

    private static final int MAX_COLLISIONS = 5000;
    private boolean interrupt = false;
    private List<T> events;
    private int timelineHeight;
    public static int LABEL_HEIGHT = 40;
    private static final int RIGHT_BUFFER = 10;
    private static final int LABEL_BUFFER = RIGHT_BUFFER + 25;
    private TimeScale timeScale;
    private EventBus eventBus;
    private boolean summaryBuffer = false;
    List<EventProxy> rights=new ArrayList<EventProxy>();
    
    @Override
    public void execute() {
        
        List<T> collisions = new ArrayList<T>();
        
        int startY = TimelineTrackModel.CELL_HEIGHT;
        int endY = (timelineHeight + startY);
            
        int numCells=Math.max(1, rights.size() + (endY - startY)/TimelineTrackModel.CELL_HEIGHT);


        int i=0;
        EventProxy last=null;
        int largestY = 0;
        for (T event: events)
        {
            if(collisions.size() > MAX_COLLISIONS){
                collisions.add(event);
                continue;
            }
            if (!event.isVisible() || event.getStartTime() == null)
                continue;
            
            if(interrupt){
                return;
            }
            if(event.getLabel() != null) {
                event.setSpaceToRight(LABEL_BUFFER);
            } else {
                event.setSpaceToRight(RIGHT_BUFFER);
            }
            double num = getTimeScale().toNum(event.getStartTime());
            int x=(int)num;
            
            int cell = 0;
            int y;
            
            y= cell*TimelineTrackModel.CELL_HEIGHT + startY;
            
            event.setX(x);
            
            if(event.getEndTime() != null){
                double endNum = getTimeScale().toNum(event.getEndTime());
                event.setEndX((int) endNum);
            }
            
            boolean collision = true;
            
            while(cell < numCells){
                if(rights.size() > cell){
                    if(rights.get(cell).getEndX() + rights.get(cell).getSpaceToRight() < x) {
                        int space = x - rights.get(cell).getX();
                        rights.get(cell).setSpaceToRight(space);
                        rights.get(cell).setRight(timeScale.toTime(rights.get(cell).getX() + space));
                        event.setY(rights.get(cell).getY());
                        y = event.getY();
                        rights.set(cell, event);
                        collision = false;
                        break;
                    } else {
                        cell++;
                    }
                } else {
                    event.setY(cell*TimelineTrackModel.CELL_HEIGHT + startY);
                    y = event.getY();
                    rights.add(event);
                    collision = false;
                    break;
                }
            }
            
            
            
            if(collision){
                collisions.add(event);
            } else {

                if(y > largestY){
                    largestY = y;
                }
                last=event;
            }
            

        }
        
        
        events = collisions;

        if(collisions.size() == 0){
            eventBus.fireEvent(new SummaryCompleteEvent(largestY));
        } else {
            Scheduler.get().scheduleDeferred(this);
        }
       
    }

    public void determineSinglePointCollision(SortedMap<Integer, SummaryEventProxy> collisionMap,
            List<SummaryEventProxy> collisions, SummaryEventProxy event, SummaryEventProxy collider) {
        if(collider.getEndTime() != null){
            if(event.getX() >= collider.getX() && event.getX() <= collider.getEndX()){
                collisions.add(event);
            } else {
                if(event.getX() > collider.getEndX())
                    collisionMap.put(event.getY(), event);
            }
            
        } else if(event.getX() == collider.getX()){
            collisions.add(event);
        } else {
            collisionMap.put(event.getY(), event);
        }
    }

    private static int gcd(int a, int b)
    {
        int mod=a%b;
        if (a%b==0)
            return b;
        return gcd(b, mod);
    }
    
    private static int nearAndRelPrime(int target, int modulus)
    {
        if (target<2)
            return 1;
        while (gcd(modulus, target)>1)
            target--;
        return target;
    }
    
    

    public boolean isInterrupt() {
        return interrupt;
    }

    public void setInterrupt(boolean interrupt) {
        this.interrupt = interrupt;
    }

    public List<T> getEvents() {
        return events;
    }

    public void setEvents(List<T> events) {
        this.events = events;
    }


    public int getTimelineHeight() {
        return timelineHeight;
    }

    public void setTimelineHeight(int timelineHeight) {
        this.timelineHeight = timelineHeight;
    }

    public int getLabelHeight() {
        return LABEL_HEIGHT;
    }
   
    public EventBus getEventBus() {
        return eventBus;
    }

    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public boolean isSummaryBuffer() {
        return summaryBuffer;
    }

    public void setSummaryBuffer(boolean summaryBuffer) {
        this.summaryBuffer = summaryBuffer;
    }

    public TimeScale getTimeScale() {
        return timeScale;
    }

    public void setTimeScale(TimeScale timeScale) {
        this.timeScale = timeScale;
    }

}
