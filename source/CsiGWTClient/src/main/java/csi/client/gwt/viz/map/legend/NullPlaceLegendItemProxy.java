package csi.client.gwt.viz.map.legend;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.constants.ButtonType;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.map.presenter.MapPresenter;
import csi.server.business.visualization.legend.PlaceLegendItem;

public class NullPlaceLegendItemProxy extends PlaceLegendItemProxy {
	
    private static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
	
	public NullPlaceLegendItemProxy(PlaceLegendItem item, MapPresenter mapPresenter) {
        super(item, mapPresenter, true);
    }

	@Override
    protected void createLabel() {
		gatherLabelAttributes();
		label = new Button();
		label.addStyleName("legend-item-label");// NON-NLS
		label.setType(ButtonType.LINK);
		label.setText(i18n.null_label());
	}


}
