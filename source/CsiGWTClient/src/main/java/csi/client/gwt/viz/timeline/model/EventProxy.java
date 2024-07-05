package csi.client.gwt.viz.timeline.model;

public interface EventProxy {
    public int getX();
    public void setX(int x);
    public int getY();
    public void setY(int y);
    public void setEndX(int num);
    public int getEndX();
    public void setSpaceToRight(int defaultRight);
    public int getSpaceToRight();
    public Long getStartTime();
    public boolean isVisible();
    public Long getEndTime();
    public boolean isSelected();
    public String getColorValue();
    public String getLabel();
    public void setRight(Long time);
}
