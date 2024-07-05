package csi.client.gwt.viz.matrix.menu;

import com.google.gwt.animation.client.AnimationScheduler;
import csi.client.gwt.viz.Visualization;
import csi.client.gwt.viz.matrix.MatrixPresenter;
import csi.client.gwt.viz.matrix.MatrixView;
import csi.client.gwt.viz.shared.AbstractVisualizationPresenter;
import csi.client.gwt.viz.shared.menu.AbstractMenuManager;
import csi.client.gwt.viz.shared.menu.CsiMenuEvent;
import csi.client.gwt.viz.shared.menu.VizExportMenuHandler;
import csi.server.common.model.visualization.matrix.MatrixViewDef;

public class MatrixExportMenuHandler<M extends AbstractVisualizationPresenter<MatrixViewDef, MatrixView>, A> extends VizExportMenuHandler {

    public MatrixExportMenuHandler(Visualization presenter, AbstractMenuManager menuManager) {
        super(presenter, menuManager);
    }
    @Override
    public void onMenuEvent(CsiMenuEvent event) {
        if (getPresenter() instanceof MatrixPresenter) {
            MatrixPresenter mp = (MatrixPresenter) getPresenter();
            mp.getView().setVizTitle(mp.getName());
            mp.getView().refresh();
        }
        AnimationScheduler.get().requestAnimationFrame(timestamp -> delayedEvent(event));
    }

    private void delayedEvent(CsiMenuEvent event) {
        super.onMenuEvent(event);
        if (getPresenter() instanceof MatrixPresenter) {
            MatrixPresenter mp = (MatrixPresenter) getPresenter();
            mp.getView().setVizTitle("");
            mp.getView().refresh();
        }
    }
}
