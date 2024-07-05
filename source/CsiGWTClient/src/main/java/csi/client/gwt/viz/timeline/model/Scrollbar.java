package csi.client.gwt.viz.timeline.model;

public class Scrollbar {
    
    private ViewPort timelineViewport;
    public static int SCROLLBAR_SIZE = 15;
    
    public Scrollbar(ViewPort timelineViewport) {
        this.timelineViewport = timelineViewport;
    }
    public double calculateStart() {if(timelineViewport.getTotalHeight() == 0){
        return 0;
    }
        return Math.abs(timelineViewport.getStart()) * timelineViewport.getCurrentHeight() / timelineViewport.getTotalHeight();
    }

    public double calculateEnd() {
        if(timelineViewport.getTotalHeight() == 0){
            return 0;
        }
        return (timelineViewport.getCurrentHeight() + Math.abs(timelineViewport.getStart()))*(timelineViewport.getCurrentHeight()/timelineViewport.getTotalHeight());
    }
    
    public double trueHeightRatio(){
        if(timelineViewport.getTotalHeight() == 0){
            return 0;
        }
        return timelineViewport.getCurrentHeight() / timelineViewport.getTotalHeight();
    }
    
    public int getX(){
        return (int) (timelineViewport.getCurrentWidth()-SCROLLBAR_SIZE);
    }

    public int getHeight(){
        return (int)(timelineViewport.getCurrentHeight());
    }
    
    public void page(int percent){
        int start = timelineViewport.getStart();
        if(percent > 0)
            start = (int) (start + timelineViewport.getCurrentHeight());
        else
            start = (int) (start - timelineViewport.getCurrentHeight());
        if(start > 0){
            start = 0;
        } else if(start +  timelineViewport.getTotalHeight() < timelineViewport.getCurrentHeight()){
            start = (int) (timelineViewport.getCurrentHeight() - timelineViewport.getTotalHeight());
        }
        
        timelineViewport.setStart(start);
    }
    
    public void scroll(int percent){
        
        int start = timelineViewport.getStart();
        if(percent > 0)
            start = (int) (start + timelineViewport.getCurrentHeight()/4);
        else
            start = (int) (start - timelineViewport.getCurrentHeight()/4);
        if(start > 0){
            start = 0;
        } else if(start +  timelineViewport.getTotalHeight() < timelineViewport.getCurrentHeight()){
            start = (int) (timelineViewport.getCurrentHeight() - timelineViewport.getTotalHeight());
        }
        
        timelineViewport.setStart(start);
//        
//        int start = timelineViewport.getStart();
//        start = (int) (start + (percent * timelineViewport.getTotalHeight())/100);
//        if(start > 0){
//            start = 0;
//        } else if(start +  timelineViewport.getTotalHeight() < getHeight()){
//            start = (int) (getHeight() - timelineViewport.getTotalHeight());
//        }
//        
//        timelineViewport.setStart(start);
    }
    public ViewPort getTimelineViewport() {
        return timelineViewport;
    }
    public void setTimelineViewport(ViewPort timelineViewport) {
        this.timelineViewport = timelineViewport;
    }
}
