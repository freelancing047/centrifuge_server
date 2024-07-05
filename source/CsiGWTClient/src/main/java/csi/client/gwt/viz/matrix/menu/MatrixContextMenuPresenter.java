package csi.client.gwt.viz.matrix.menu;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.PopupPanel;
import com.sencha.gxt.core.client.dom.XDOM;
import csi.client.gwt.mainapp.SecurityBanner;
import csi.client.gwt.util.VortexUtil;
import csi.client.gwt.viz.graph.surface.ContextMenuInfo;
import csi.client.gwt.viz.matrix.MatrixModel;
import csi.client.gwt.viz.matrix.MatrixPresenter;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.Vortex;
import csi.client.gwt.vortex.VortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.server.common.service.api.MatrixActionsServiceProtocol;
import csi.shared.core.visualization.matrix.Cell;
import csi.shared.core.visualization.matrix.MatrixDataRequest;
import csi.shared.core.visualization.matrix.MatrixWrapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MatrixContextMenuPresenter {
    private MatrixContextMenu menu;
    private PopupPanel popup;
    private int matrixX, matrixY;
    MatrixPresenter _presenter;
    Cell hoverCell;

    public MatrixContextMenuPresenter(MatrixPresenter presenter){
        _presenter = presenter;
    }

    public void showMenuAt(int x,int y, int surface_x, int surface_y, Cell onCell){
        matrixX = surface_x;
        matrixY = surface_y;
        hoverCell = onCell;
        menu = new MatrixContextMenu(this, onCell != null);
        popup = new PopupPanel(true);
        popup.setGlassEnabled(true);
        popup.add(menu);

        x = ensureContextMenuWithinWindowWidth(x);
        y = ensureContextMenuWithinWindowHeight(y);


        popup.setPopupPosition(x, y);
        popup.getElement().getStyle().setZIndex(XDOM.getTopZIndex());
        popup.show();
    }

    public void selectAll(){
        new SelectAllHandler(_presenter, null).onMenuEvent(null);
        popup.hide();
    }

    public void deselectAll(){
        new DeselectAllHandler(_presenter, null).onMenuEvent(null);
        popup.hide();
    }

    void selectY(){
        int binCountForAxisY = _presenter.getModel().getBinCountForAxis((int) _presenter.getModel().getHeight());
        int rowToSel = hoverCell != null? hoverCell.getY() - (binCountForAxisY/2): matrixY - (binCountForAxisY/2);
        MatrixDataRequest axisSelectionRequest = _presenter.getModel().getAxisSelectionRequest(rowToSel, MatrixModel.Axis.Y);
        requestSelection(axisSelectionRequest);

        popup.hide();
    }

    void selectX(){
        int binCountForAxisX = _presenter.getModel().getBinCountForAxis((int) _presenter.getModel().getWidth());
        int rowToSel = hoverCell!= null ? hoverCell.getX() - (binCountForAxisX/2): matrixX - (binCountForAxisX/2);
        MatrixDataRequest axisSelectionRequest = _presenter.getModel().getAxisSelectionRequest(rowToSel, MatrixModel.Axis.X);
        requestSelection(axisSelectionRequest);

        popup.hide();
    }

    void selectXY(){
        _presenter.getView().setLoadingIndicator(true);
        int binCountForAxisY = _presenter.getModel().getBinCountForAxis((int) _presenter.getModel().getHeight());
        int rowToSel = hoverCell != null? hoverCell.getY() - (binCountForAxisY/2): matrixY - (binCountForAxisY/2);
        MatrixDataRequest axisSelectionRequest = _presenter.getModel().getAxisSelectionRequest(rowToSel, MatrixModel.Axis.Y);

        int binCountForAxisX = _presenter.getModel().getBinCountForAxis((int) _presenter.getModel().getWidth());
        int col = hoverCell!= null ? hoverCell.getX() - (binCountForAxisX/2): matrixX - (binCountForAxisX/2);
        MatrixDataRequest colSelection = _presenter.getModel().getAxisSelectionRequest(col, MatrixModel.Axis.X);

        List<VortexFuture<MatrixWrapper>> twoFeat = new ArrayList<>();

        Vortex vortex = _presenter.getVortex();
        VortexFuture<MatrixWrapper> vortexFuture = vortex.createFuture();

        vortexFuture.addEventHandler(new AbstractVortexEventHandler<MatrixWrapper>() {
            @Override
            public void onSuccess(MatrixWrapper result) {
                result.getData().getCells().forEach(cell -> {
                    _presenter.getModel().selectCell(cell);
                });
            }
        });

        vortexFuture.execute(MatrixActionsServiceProtocol.class).getCellsInRegionForSelection(axisSelectionRequest);

        VortexFuture<MatrixWrapper> vortexFuture1 = vortex.createFuture();

        vortexFuture1.addEventHandler(new AbstractVortexEventHandler<MatrixWrapper>() {
            @Override
            public void onSuccess(MatrixWrapper result) {
                result.getData().getCells().forEach(cell -> {
                    _presenter.getModel().selectCell(cell);
                });
            }
        });

        vortexFuture1.execute(MatrixActionsServiceProtocol.class).getCellsInRegionForSelection(colSelection);

        twoFeat.add(vortexFuture);
        twoFeat.add(vortexFuture1);

        VortexUtil.afterAllFutures(twoFeat, new VortexEventHandler<Collection<MatrixWrapper>>() {
            @Override
            public void onSuccess(Collection<MatrixWrapper> result) {
                _presenter.getView().setLoadingIndicator(false);
                _presenter.getView().refresh();
            }

            @Override
            public boolean onError(Throwable t) {
                return false;
            }

            @Override
            public void onUpdate(int taskProgess, String taskMessage) {

            }

            @Override
            public void onCancel() {

            }
        });




        popup.hide();

    }





    //trash
    /*
       int binCountForAxisY = _presenter.getModel().getBinCountForAxis((int) _presenter.getModel().getHeight());
        MatrixDataRequest yAxis = _presenter.getModel().getAxisSelectionRequest(matrixY - (binCountForAxisY/2), MatrixModel.Axis.Y);

        int binCountForAxisX = _presenter.getModel().getBinCountForAxis((int) _presenter.getModel().getWidth());
        MatrixDataRequest xAxis= _presenter.getModel().getAxisSelectionRequest(matrixX - (binCountForAxisX/2), MatrixModel.Axis.X);

        // use the Y axis request as a house for the x range, this should give us everything we need.
        yAxis.setEndX(xAxis.getEndX());
        yAxis.setStartX(xAxis.getStartX());


        Vortex vortex = _presenter.getVortex();
        VortexFuture<MatrixWrapper> vortexFuture = vortex.createFuture();

        vortexFuture.addEventHandler(new AbstractVortexEventHandler<MatrixWrapper>() {
            @Override
            public void onSuccess(MatrixWrapper result) {

                result.getData().getCells().forEach(cell -> {
                    _presenter.getModel().selectCell(cell);
                });

                _presenter.getView().setLoadingIndicator(false);
                _presenter.getView().refresh();
            }
        });

        _presenter.getView().setLoadingIndicator(true);
        vortexFuture.execute(MatrixActionsServiceProtocol.class).getIntersectionSelection(yAxis);


        popup.hide();

     */

    protected void requestSelection(MatrixDataRequest axisSelectionRequest) {
        Vortex vortex = _presenter.getVortex();
        VortexFuture<MatrixWrapper> vortexFuture = vortex.createFuture();

        vortexFuture.addEventHandler(new AbstractVortexEventHandler<MatrixWrapper>() {
            @Override
            public void onSuccess(MatrixWrapper result) {

                result.getData().getCells().forEach(cell -> {
                    _presenter.getModel().selectCell(cell);
                });

                _presenter.getView().setLoadingIndicator(false);
                _presenter.getView().refresh();
            }
        });

        _presenter.getView().setLoadingIndicator(true);
        vortexFuture.execute(MatrixActionsServiceProtocol.class).getCellsInRegionForSelection(axisSelectionRequest);
    }

    public void selectCell() {
        if(hoverCell != null){
            _presenter.selectCell(hoverCell, _presenter.getModel().isSelected(hoverCell));
            _presenter.getView().refresh();
        }

        popup.hide();
    }

    public boolean isHoverCellSelected() {
        return _presenter.getModel().isSelected(hoverCell);
    }


    private int ensureContextMenuWithinWindowHeight(int y1) {
        int myMaxHeight = Window.getClientHeight() - SecurityBanner.getHeight();
        if(y1 + ContextMenuInfo.CONTEXT_MENU_HEIGHT > myMaxHeight){
            y1 = myMaxHeight - ContextMenuInfo.CONTEXT_MENU_HEIGHT;
        }
        return y1;
    }

    private int ensureContextMenuWithinWindowWidth(int x1) {
        if(x1 + ContextMenuInfo.CONTEXT_MENU_WIDTH > Window.getClientWidth()){
            x1 = Window.getClientWidth() - ContextMenuInfo.CONTEXT_MENU_WIDTH;
        }
        return x1;
    }

}
