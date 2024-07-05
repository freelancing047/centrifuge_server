package csi.client.gwt.viz.timeline.model;

public class ViewPort {

    private int start = 0;
    private double currentHeight;
    private double currentWidth;
    private double totalHeight;
    
    public int getStart() {
        return start;
    }
    public void setStart(int start) {
        this.start = start;
    }

    public double getTotalHeight() {
        return totalHeight;
    }
    public void setTotalHeight(double d) {
        this.totalHeight = d;
    }
    public double getCurrentHeight() {
        return currentHeight;
    }
    public void setCurrentHeight(double currentHeight) {
        this.currentHeight = currentHeight;
    }
    public double getCurrentWidth() {
        return currentWidth;
    }
    public void setCurrentWidth(double currentWidth) {
        this.currentWidth = currentWidth;
    }
    
    public double getStartPercent(){
        return (start + totalHeight)/totalHeight;
    }
    
    public void setViewport(double startPercent, double totalHeight){
        this.totalHeight = totalHeight;
        this.start = (int) (totalHeight * startPercent * -1);
    }
    public void setViewport(double startPercent) {
        setViewport(startPercent, this.totalHeight);
    }

}
