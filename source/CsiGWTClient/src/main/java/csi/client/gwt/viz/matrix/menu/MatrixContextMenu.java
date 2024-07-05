package csi.client.gwt.viz.matrix.menu;

import com.github.gwtbootstrap.client.ui.NavLink;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.matrix.MatrixPresenter;

public class MatrixContextMenu extends Composite {
    interface MyUiBinder extends UiBinder<Widget, MatrixContextMenu> {
    }
    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    private MatrixContextMenuPresenter _contextMenuPresenter;
    @UiField
    HTMLPanel panel;
    @UiField
    NavLink selectRow;
    @UiField
    NavLink selectAll;
    @UiField
    NavLink deselectAll;
    @UiField
    NavLink selectColumn;
    @UiField
    NavLink selectXY;
    @UiField
    NavLink selectCell;


    MatrixContextMenu(MatrixContextMenuPresenter contextMenuPresenter, boolean isHover){
        _contextMenuPresenter = contextMenuPresenter;
        initWidget(uiBinder.createAndBindUi(this));


        if(!isHover){
            selectCell.setVisible(false);
        }else{
            if(_contextMenuPresenter.isHoverCellSelected()){
                CentrifugeConstants _constants = CentrifugeConstantsLocator.get();
                selectCell.setText(_constants.matrixContextMenu_deselectCell());
            }
            selectCell.setVisible(true);
        }
    }

    @UiHandler("selectAll")
    public void selectAllClick(ClickEvent event) {
        _contextMenuPresenter.selectAll();
    }

    @UiHandler("deselectAll")
    public void deselectAllClick(ClickEvent event) {
        _contextMenuPresenter.deselectAll();
    }

    @UiHandler("selectRow")
    public void selectRowClick(ClickEvent event) {
            _contextMenuPresenter.selectY();

    }

    @UiHandler("selectColumn")
    public void selectColumnClick(ClickEvent event) {
        _contextMenuPresenter.selectX();
    }

    @UiHandler("selectXY")
    public void selectXYClick(ClickEvent event) {
        _contextMenuPresenter.selectXY();
    }

    @UiHandler("selectCell")
    public void selectCellClick(ClickEvent event) {
        _contextMenuPresenter.selectCell();
    }


}
