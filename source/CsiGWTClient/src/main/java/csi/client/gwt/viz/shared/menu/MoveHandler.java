package csi.client.gwt.viz.shared.menu;

import csi.client.gwt.viz.Visualization;
import csi.client.gwt.viz.shared.chrome.panel.VizPanel;
import csi.client.gwt.worksheet.WorksheetPresenter;

public class MoveHandler<T extends Visualization> extends AbstractMenuEventHandler<T, AbstractMenuManager<T>> {

	public MoveHandler(T presenter, AbstractMenuManager<T> menuManager) {
		super(presenter, menuManager);
	}

	@Override
	public void onMenuEvent(CsiMenuEvent event) {

		VizPanel viz = (VizPanel) (getPresenter().getChrome());
		WorksheetPresenter worksheetPresenter = viz.getWorksheet();
		
		worksheetPresenter.openMoveDialog(worksheetPresenter, getPresenter().getUuid());
		
	}

}
