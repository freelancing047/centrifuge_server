package csi.client.gwt.viz.viewer;

import com.google.gwt.user.client.ui.IsWidget;
import csi.server.common.model.visualization.viewer.Objective;

public interface Viewer extends IsWidget{

    void view(Objective objective);

    ViewerImpl.ViewerView getView();

    ViewerImpl.ViewerModel getModel();

    void ready();

    void setPresenter(ViewerImpl.ViewerPresenter presenter);

    void loading();

    ViewerContainer getContainer();

    void save();
}
