package csi.client.gwt.viz.viewer;

import com.google.gwt.user.client.ui.IsWidget;

public interface ViewerContainer {
    void show();
    void hide();

    void add(IsWidget viewer);
}
