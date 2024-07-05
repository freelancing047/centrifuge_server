package csi.client.gwt.viz.viewer;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class LoadingViewer implements Activity, ViewerImpl.ViewerPresenter {
    private ViewerImpl viewer;

    public LoadingViewer(ViewerImpl viewer) {
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

    }
}
