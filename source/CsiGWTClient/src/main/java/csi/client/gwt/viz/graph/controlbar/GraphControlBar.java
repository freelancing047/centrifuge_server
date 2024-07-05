package csi.client.gwt.viz.graph.controlbar;

import com.google.gwt.user.client.ui.IsWidget;

import csi.client.gwt.viz.graph.Graph;
import csi.config.advanced.graph.ControlBarConfig;

public interface GraphControlBar extends IsWidget{
    ControlBarConfig getConfig();
    GraphControlBarView getView();
    GraphControlBarModel getModel();
    Graph getGraph();
    void play();
    void stop();
    void pause();
    void scrub(long time);
    void scrubToPercent(double time);
    void setStart(long time);
    void setEnd(long time);
    void show(boolean show);
    void step();
    void editStart();
    void editEnd();
    void editCurrent();
    void initialize();

}
