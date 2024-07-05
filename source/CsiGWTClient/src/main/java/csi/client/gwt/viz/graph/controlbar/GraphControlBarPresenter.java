package csi.client.gwt.viz.graph.controlbar;

import com.google.gwt.activity.shared.Activity;

public interface GraphControlBarPresenter extends Activity{
    void togglePlaying();

    void stopPlayer();

    void scrubMoved(double newRelativePosition);

    void scrubDragStart();
}
