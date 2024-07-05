package csi.client.gwt.viz.chart.settings;

import com.sencha.gxt.data.shared.LabelProvider;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.server.common.model.visualization.AbstractAttributeDefinition;
import csi.server.common.model.visualization.chart.CategoryDefinition;
import csi.server.common.model.visualization.chart.MeasureDefinition;
import csi.server.common.model.visualization.matrix.MatrixCategoryDefinition;
import csi.server.common.model.visualization.matrix.MatrixMeasureDefinition;

public class AttributeDefinitionLabelProvider implements LabelProvider<AbstractAttributeDefinition> {

    static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
	@Override
	public String getLabel(AbstractAttributeDefinition item) {
		if(item instanceof MatrixMeasureDefinition){
			return i18n.matrixMeasure();
		} else if(item instanceof MatrixCategoryDefinition){
			return i18n.matrixCategory();
		} else if(item instanceof CategoryDefinition){
			return i18n.matrixCategory();
		} else if(item instanceof MeasureDefinition){
			return i18n.matrixMeasure();
		} 
		return item.getDefinitionName();
		
	}

}
