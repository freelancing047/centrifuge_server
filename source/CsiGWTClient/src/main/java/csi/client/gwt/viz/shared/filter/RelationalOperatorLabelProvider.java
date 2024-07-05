package csi.client.gwt.viz.shared.filter;

import com.sencha.gxt.data.shared.LabelProvider;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.server.common.enumerations.RelationalOperator;

public class RelationalOperatorLabelProvider implements LabelProvider<RelationalOperator> {

    private CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
	@Override
	public String getLabel(RelationalOperator item) {

        return item.getLabel();
	}
}
