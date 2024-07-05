package csi.client.gwt.viz.viewer;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import csi.client.gwt.WebMain;
import csi.client.gwt.viz.viewer.lens.LensImageViewer;
import csi.client.gwt.viz.viewer.lens.LensImageViewerLocator;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.server.common.model.visualization.viewer.Objective;
import csi.server.common.service.api.ViewerActionServiceProtocol;
import csi.shared.gwt.viz.viewer.LensImage.LensImage;

import java.util.List;

public class ChangeObjective extends AbstractActivity implements ViewerImpl.ViewerPresenter {
    private Viewer viewer;
    private Objective objective;

    public ChangeObjective(Viewer viewer, Objective objective) {
        this.viewer = viewer;
        this.objective = objective;
    }

    @Override
    public void start(AcceptsOneWidget panel, com.google.gwt.event.shared.EventBus eventBus) {
        viewer.setPresenter(this);
        ViewerImpl.ViewerModel model = viewer.getModel();
        ViewerImpl.ViewerView view = viewer.getView();
        view.removeAllLensImageViewers();
        view.setObjective(objective);
        VortexFuture<List<LensImage>> future = WebMain.injector.getVortex().createFuture();
        String dvuuid = WebMain.injector.getMainPresenter().getDataViewPresenter(true).getUuid();
        future.execute(ViewerActionServiceProtocol.class).getLensImage(dvuuid,objective);
        future.addEventHandler(new AbstractVortexEventHandler<List<LensImage>>() {
            @Override
            public void onSuccess(List<LensImage> result) {
                for (LensImage r : result) {
                    LensImageViewer liv = LensImageViewerLocator.getLensImageViewer(r, objective, viewer);
                    if (liv.handles(objective)) {
                        view.addLensImageViewer(liv);
                    }
                }
            }
        });
        viewer.getContainer().show();
        viewer.ready();
    }
}
