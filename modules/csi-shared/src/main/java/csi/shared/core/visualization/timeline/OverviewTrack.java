package csi.shared.core.visualization.timeline;

import com.google.common.collect.Lists;
import csi.shared.gwt.viz.timeline.TimeUnit;

import java.util.List;

public class OverviewTrack implements Track<MeasuredTrack>{

    private String name;
    private Long startTime;

    private boolean visible = true;
    private int index;

    private List<Integer> measures = Lists.newArrayList();
    //    private List<Boolean> drawAsSelectedMaybe = Lists.newArrayList();
    private TimeUnit timeUnit;
    private double height;

    private Long overviewStart;
    private Long overviewEnd;

    public Long getOverviewStart() {
        return overviewStart;
    }
    public void setOverviewStart(Long overviewStart) {
        this.overviewStart = overviewStart;
    }
    public Long getOverviewEnd() {
        return overviewEnd;
    }
    public void setOverviewEnd(Long overviewEnd) {
        this.overviewEnd = overviewEnd;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int compareTo(MeasuredTrack o) {
        return ((MeasuredTrack) o).getName().compareTo(this.name);
    }

    @Override
    public int hashCode(){
        return getName().hashCode();

    }

    @Override
    public boolean equals(Object object){
        if(((MeasuredTrack) object).getName() == null || this.name == null){
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

    public List<Integer> getMeasures() {
        return measures;
    }

    public void setMeasures(List<Integer> measures) {
        this.measures = measures;
    }


    public void setHeight(double height) {
        this.height = height;
    }

    public double getHeight() {
        return height;
    }


    
}
