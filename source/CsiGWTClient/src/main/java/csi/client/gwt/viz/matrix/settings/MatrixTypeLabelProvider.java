package csi.client.gwt.viz.matrix.settings;

import com.sencha.gxt.data.shared.LabelProvider;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.server.common.model.visualization.matrix.MatrixType;

public class MatrixTypeLabelProvider implements LabelProvider<MatrixType> {

    static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();

	@Override
	public String getLabel(MatrixType item) {
		switch(item){
			case BUBBLE: return i18n.matrixTypeBubble();
			case CO_OCCURRENCE: return i18n.matrixTypeCoOccurrence();
			case CO_OCCURRENCE_DIR: return i18n.matrixTypeCoOccurrenceDir();
			case HEAT_MAP: return i18n.matrixTypeHeatMap();
			default: return item.getLabel();
		}
	}

}
