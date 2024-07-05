package csi.shared.core.visualization.timeline;

import java.util.List;

import com.google.common.collect.Lists;

import csi.shared.gwt.viz.timeline.TimeUnit;

public class MeasuredTrack implements Track<MeasuredTrack>, CommonTrack {

    private String name;
    private Long startTime;
    private boolean visible = true;
    private int index;
    private List<MeasuredTrackItem> measures = Lists.newArrayList();
    private TimeUnit timeUnit;
    private double height;

    private String nameOverride=null;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNameOverride() {
        return nameOverride;
    }

    public void setNameOverride(String nameOverride) {
        this.nameOverride = nameOverride;
    }

    @Override
    public int compareTo(MeasuredTrack o) {
        return ((MeasuredTrack) o).getName().compareTo(this.name);
    }

    @Override
    public int hashCode() {
        return getName().hashCode();

    }

    @Override
    public boolean equals(Object object) {
        if (((MeasuredTrack) object).getName() == null || this.name == null) {
            return false;
        }

        return ((MeasuredTrack) object).getName().equals(this.name);
    }


    @Override
    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public void setTimeUnit(TimeUnit unit) {
        this.timeUnit = unit;
    }

    public TimeUnit getTimeUnit() {
        return this.timeUnit;
    }

    public List<MeasuredTrackItem> getMeasures() {
        return measures;
    }

    public void setMeasures(List<MeasuredTrackItem> measures) {
        this.measures = measures;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public double getHeight() {
        return height;
    }

    @Override
    public String getLabel() {
        // TODO Auto-generated method stub
        return name;
    }

    @Override
    public void setCollapsed(boolean collapse) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean isCollapsed() {
        // TODO Auto-generated method stub
        return false;
    }
}
