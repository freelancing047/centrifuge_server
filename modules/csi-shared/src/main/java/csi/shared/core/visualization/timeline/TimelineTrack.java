package csi.shared.core.visualization.timeline;


public class TimelineTrack implements Track<TimelineTrack> {
    public static final String EMPTY_TRACK = "";
    private String name;
    private int color;
    public static String NULL_TRACK = "!-!";
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getColor() {
        return color;
    }
    public void setColor(int color) {
        this.color = color;
    }

    @Override
    public int compareTo(TimelineTrack o) {
        return ((TimelineTrack) o).getName().compareTo(this.name);
    }
    
    @Override
    public int hashCode(){
        
        return getName().hashCode();
        
    }
    
    @Override
    public boolean equals(Object object){
        if(((TimelineTrack) object).getName() == null || this.name == null){
            return false;
        }
        return ((TimelineTrack) object).getName().equals(this.name);
    }
    @Override
    public boolean isVisible() {
        // TODO Auto-generated method stub
        return false;
    }
}
