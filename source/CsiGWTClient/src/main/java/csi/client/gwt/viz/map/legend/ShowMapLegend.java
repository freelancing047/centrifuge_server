package csi.client.gwt.viz.map.legend;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sencha.gxt.widget.core.client.ContentPanel;

import csi.client.gwt.i18n.CentrifugeConstantsLocator;

public class ShowMapLegend implements MapLegend.Presenter {
	private MapLegend mapLegend;
	
	public ShowMapLegend(MapLegend mapLegend) {
		this.mapLegend = mapLegend;
	}

	@Override
	public String mayStop() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCancel() {
		// TODO Auto-generated method stub
	}

	@Override
	public void onStop() {
		// TODO Auto-generated method stub
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		if (panel instanceof ContentPanel) {
            ((ContentPanel) panel).setHeading(CentrifugeConstantsLocator.get().menuKeyConstants_show_legend());
        }
	}
}
