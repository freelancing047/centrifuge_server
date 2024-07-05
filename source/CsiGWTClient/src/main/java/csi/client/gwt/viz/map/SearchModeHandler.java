package csi.client.gwt.viz.map;

import com.github.gwtbootstrap.client.ui.Button;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.graph.button.VizButtonHandler;
import csi.client.gwt.viz.map.presenter.MapPresenter;
import csi.client.gwt.viz.map.view.MapView;

public class SearchModeHandler implements VizButtonHandler, ClickHandler {
	private MapPresenter presenter;

	public SearchModeHandler(MapPresenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void onClick(ClickEvent event) {
		MapView view = presenter.getView();
		view.toggleSearch();
	}

	@Override
	public void bind(Button button) {
		button.addClickHandler(this);
	}

	@Override
	public String getTooltipText() {
		return CentrifugeConstantsLocator.get().mapViewSearch();
	}
}
