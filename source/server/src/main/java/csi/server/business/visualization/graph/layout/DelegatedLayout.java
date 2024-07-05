package csi.server.business.visualization.graph.layout;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import prefuse.Visualization;
import prefuse.action.layout.Layout;
import prefuse.activity.Activity;
import prefuse.activity.ActivityListener;
import prefuse.activity.Pacer;
import prefuse.visual.VisualItem;

/** 
 * Intermediate class to provide delegated operations on the provided Layout delegate.
 * 
 * This is intended to be extended, but is not absolutely required.
 */
public class DelegatedLayout extends Layout {

    protected Layout layout;

    public DelegatedLayout(Layout delegate) {
        layout = delegate;
    }

    public void addActivityListener(ActivityListener l) {
        layout.addActivityListener(l);
    }

    public void alwaysRunAfter(Activity before) {
        layout.alwaysRunAfter(before);
    }

    public void cancel() {
        layout.cancel();
    }

    public boolean equals(Object obj) {
        return layout.equals(obj);
    }

    public long getDuration() {
        return layout.getDuration();
    }

    public String getGroup() {
        return layout.getGroup();
    }

    public Point2D getLayoutAnchor() {
        return layout.getLayoutAnchor();
    }

    public Rectangle2D getLayoutBounds() {
        return layout.getLayoutBounds();
    }

    public long getNextTime() {
        return layout.getNextTime();
    }

    public double getPace(long elapsedTime) {
        return layout.getPace(elapsedTime);
    }

    public Pacer getPacingFunction() {
        return layout.getPacingFunction();
    }

    public long getStartTime() {
        return layout.getStartTime();
    }

    public long getStepTime() {
        return layout.getStepTime();
    }

    public long getStopTime() {
        return layout.getStopTime();
    }

    public Visualization getVisualization() {
        return layout.getVisualization();
    }

    public int hashCode() {
        return layout.hashCode();
    }

    public boolean isEnabled() {
        return layout.isEnabled();
    }

    public boolean isRunning() {
        return layout.isRunning();
    }

    public boolean isScheduled() {
        return layout.isScheduled();
    }

    public void removeActivityListener(ActivityListener l) {
        layout.removeActivityListener(l);
    }

    public void run() {
        layout.run();
    }

    public void run(double frac) {
        layout.run(frac);
    }

    public void runAfter(Activity before) {
        layout.runAfter(before);
    }

    public void runAt(long startTime) {
        layout.runAt(startTime);
    }

    public void setDuration(long duration) {
        layout.setDuration(duration);
    }

    public void setEnabled(boolean s) {
        layout.setEnabled(s);
    }

    public void setGroup(String group) {
        layout.setGroup(group);
    }

    public void setLayoutAnchor(Point2D a) {
        layout.setLayoutAnchor(a);
    }

    public void setLayoutBounds(Rectangle2D b) {
        layout.setLayoutBounds(b);
    }

    public void setMargin(int top, int left, int bottom, int right) {
        layout.setMargin(top, left, bottom, right);
    }

    public void setPacingFunction(Pacer pfunc) {
        layout.setPacingFunction(pfunc);
    }

    public void setStartTime(long time) {
        layout.setStartTime(time);
    }

    public void setStepTime(long time) {
        layout.setStepTime(time);
    }

    public void setVisualization(Visualization vis) {
        layout.setVisualization(vis);
    }

    public void setX(VisualItem item, VisualItem referrer, double x) {
        layout.setX(item, referrer, x);
    }

    public void setY(VisualItem item, VisualItem referrer, double y) {
        layout.setY(item, referrer, y);
    }

    public String toString() {
        return layout.toString();
    }

}
