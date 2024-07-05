package csi.client.gwt.viz.chart.selection.criterion;

import com.github.gwtbootstrap.client.ui.FluidRow;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.gwt.core.client.Callback;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.chart.selection.ValidationListener;
import csi.server.common.model.visualization.chart.SingleIntegerTypeCriterion;

public class SingleIntegerTypeCriterionView extends IntegerTypeCriterionView {
	private SingleIntegerTypeCriterion criterion;
	
	public SingleIntegerTypeCriterionView(ValidationListener validationListener, SingleIntegerTypeCriterion criterion) {
		super(validationListener);
		this.criterion = criterion;
	}
	
	@Override
	public void setup(FluidRow row) {
		TextBox testValueTextBox = createTextBox(new Callback<Integer, String>() {
			@Override
			public void onFailure(String reason) {
				criterion.setTestValue(null);
			}
			@Override
			public void onSuccess(Integer result) {
				criterion.setTestValue(result);
			}
		}, CentrifugeConstantsLocator.get().chartSelectDialog_enterThresholdValue());
		row.add(testValueTextBox);

		Integer testValue = criterion.getTestValue();
		if (testValue != null) {
			testValueTextBox.setText(testValue.toString());
		}
	}
	
	@Override
	public boolean isValid() {
		if (criterion == null) {
            return false;
        }else {
            return criterion.getTestValue() != null;
        }
    }
}