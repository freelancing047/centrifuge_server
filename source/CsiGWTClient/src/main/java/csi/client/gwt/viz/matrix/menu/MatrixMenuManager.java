/**
 *  Copyright (c) 2008 Centrifuge Systems, Inc.
 *  All rights reserved.
 *
 *  This software is the confidential and proprietary information of
 *  Centrifuge Systems, Inc. ("Confidential Information").  You shall
 *  not disclose such Confidential Information and shall use it only
 *  in accordance with the terms of the license agreement you entered
 *  into with Centrifuge Systems.
 *
 **/
package csi.client.gwt.viz.matrix.menu;
import csi.client.gwt.viz.matrix.MatrixPresenter;
import csi.client.gwt.viz.shared.filter.FilterSettingsHandler;
import csi.client.gwt.viz.shared.filter.selection.SelectionFilterView;
import csi.client.gwt.viz.shared.menu.*;
import csi.server.common.model.visualization.matrix.Axis;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class MatrixMenuManager extends AbstractMenuManager<MatrixPresenter> {

    private static final String MATRIX_LINKUP_MENUS = "linkup.matrixviz";
    private static final String MATRIX_BROADCAST_MENUS = "broadcast.matrixviz";

    public MatrixMenuManager(MatrixPresenter presenter) {
        super(presenter);
        registerMenuManager(MATRIX_LINKUP_MENUS, new LinkupMenuManager<MatrixPresenter>(presenter));
        registerMenuManager(MATRIX_BROADCAST_MENUS, new BroadcastMenuManager<MatrixPresenter>(presenter));

        register(MenuKey.MATRIX_METRICS, new AbstractMatrixMenuEventHandler(presenter, this) {
            @Override
            public void onMenuEvent(CsiMenuEvent event) {
                if(presenter.getView().isLoaded()) {
                    presenter.showMetrics();
                }
            }
        });

    }

	@Override
	public void registerPreloadMenus(boolean limitedMenu) {
        register(MenuKey.LOAD, new LoadMenuHandler<MatrixPresenter, AbstractMenuManager<MatrixPresenter>>(getPresenter(), this));

        if(!limitedMenu)
        	register(MenuKey.DELETE, new DeleteMenuHandler<MatrixPresenter, AbstractMenuManager<MatrixPresenter>>(getPresenter(), this));

        register(MenuKey.SETTINGS, new MatrixSettingsHandler(getPresenter(), this));
        register(MenuKey.FILTERS, new FilterSettingsHandler<MatrixPresenter>(getPresenter(), this));
	}

    SelectionFilterView v;

    @Override
    public void registerMenus(boolean limitedMenu) {
        super.registerMenus(limitedMenu);

        register(MenuKey.EXPORT, new MatrixExportMenuHandler<MatrixPresenter, AbstractMenuManager<MatrixPresenter>>(getPresenter(), this));
        //register(MenuKey.PUBLISH, new PublishMenuHandler<MatrixPresenter, AbstractMenuManager<MatrixPresenter>>(getPresenter(), this));
        register(MenuKey.SAVE, new SaveMenuHandler(getPresenter(), this));

        //register(MenuKey.DOWNLOAD_IMAGE, new DownloadAsImageHandler(getPresenter(), this));

        if(!limitedMenu){
	        register(MenuKey.COPY, new CopyHandler<MatrixPresenter>(getPresenter(), this));
	        register(MenuKey.MOVE, new MoveHandler<MatrixPresenter>(getPresenter(), this));
        }
        register(MenuKey.SELECT_ALL, new SelectAllHandler(getPresenter(), this));
        register(MenuKey.SELECT_DDD, new AbstractMatrixMenuEventHandler(getPresenter(), this) {
            @Override
            public void onMenuEvent(CsiMenuEvent event) {
                if(v == null) {
                    v = new SelectionFilterView(getPresenter());
                }

                v.show();
            }
        });
        register(MenuKey.DESELECT_ALL, new DeselectAllHandler(getPresenter(), this));

        if(!limitedMenu) {
            register(MenuKey.SPINOFF, new MatrixSpinoffHandler(getPresenter(), this));
            register(MenuKey.SPAWN, new MatrixSpawnTableHandler(getPresenter(), this));
        }



        register(MenuKey.CREATE_SELECTION_FILTER, new CreateSelectionFilterHandler(getPresenter(), this));

        /** QUICK SORT HANDLERS **/

        //click handler for the sort x
        register(MenuKey.TOGGLE_SORT_X, new AxisQuickSortHandler(getPresenter(), this, Axis.X));
        //click handler for the sort y
        register(MenuKey.TOGGLE_SORT_Y, new AxisQuickSortHandler(getPresenter(), this, Axis.Y));
        //click handler for quick sort by measure
        register(MenuKey.TOGGLE_SORT_MEASURE, new MeasureQuickSortHandler(getPresenter(), this));
    }


}
