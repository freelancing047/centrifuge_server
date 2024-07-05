package csi.client.gwt.viz.viewer;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class InitializeViewer implements ViewerImpl.ViewerPresenter {
    private Viewer viewer;

    public InitializeViewer(Viewer viewer) {
        this.viewer = viewer;
    }

    @Override
    public String mayStop() {
        return null;
    }

    @Override
    public void onCancel() {

    }

    @Override
    public void onStop() {

    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        viewer.setPresenter(this);
        viewer.ready();
    }
}
