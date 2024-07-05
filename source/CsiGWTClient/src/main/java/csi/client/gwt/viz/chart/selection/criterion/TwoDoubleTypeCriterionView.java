package csi.client.gwt.viz.chart.selection.criterion;

import com.github.gwtbootstrap.client.ui.FluidRow;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.gwt.core.client.Callback;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.chart.selection.ValidationListener;
import csi.server.common.model.visualization.chart.TwoDoubleTypeCriterion;

public class TwoDoubleTypeCriterionView extends DoubleTypeCriterionView {
	private TwoDoubleTypeCriterion criterion;

	public TwoDoubleTypeCriterionView(ValidationListener validationListener, TwoDoubleTypeCriterion criterion) {
		super(validationListener);
		this.criterion = criterion;
	}

	@Override
	public void setup(FluidRow row) {
//		row.clear();
//		Column column1 = new Column(3);
		TextBox minValueTextBox = createTextBox(new Callback<Double, String>() {
			@Override
			public void onFailure(String reason) {
				criterion.setMinValue(null);
			}

			@Override
			public void onSuccess(Double result) {
				criterion.setMinValue(result);
			}
		}, CentrifugeConstantsLocator.get().chartSelectDialog_enterMinValue());
		row.add(minValueTextBox);
//		Column column2 = new Column(3);
		TextBox maxValueTextBox = createTextBox(new Callback<Double, String>() {
			@Override
			public void onFailure(String reason) {
				criterion.setMaxValue(null);
			}

			@Override
			public void onSuccess(Double result) {
				criterion.setMaxValue(result);
			}
		}, CentrifugeConstantsLocator.get().chartSelectDialog_enterMaxValue());
		row.add(maxValueTextBox);
		
		Double minValue = criterion.getMinValue();
		Double maxValue = criterion.getMaxValue();
		if (minValue != null & maxValue != null) {
			minValueTextBox.setText(minValue.toString());
			maxValueTextBox.setText(maxValue.toString());
		}
	}
	
	@Override
	public boolean isValid() {
		if (criterion == null)
			return false;
		else {
			Double minValue = criterion.getMinValue();
			Double maxValue = criterion.getMaxValue();
			return minValue != null && maxValue != null && minValue < maxValue;
		}
			
	}
}
