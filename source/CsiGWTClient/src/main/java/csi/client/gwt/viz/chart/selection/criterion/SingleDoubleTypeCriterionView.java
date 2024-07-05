package csi.client.gwt.viz.chart.selection.criterion;

import com.github.gwtbootstrap.client.ui.Column;
import com.github.gwtbootstrap.client.ui.FluidRow;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.gwt.core.client.Callback;

import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.chart.selection.ValidationListener;
import csi.server.common.model.visualization.chart.SingleDoubleTypeCriterion;

public class SingleDoubleTypeCriterionView extends DoubleTypeCriterionView {
    private SingleDoubleTypeCriterion criterion;

    public SingleDoubleTypeCriterionView(ValidationListener validationListener, SingleDoubleTypeCriterion criterion) {
        super(validationListener);
        this.criterion = criterion;
    }

    @Override
    public void setup(FluidRow row) {

        TextBox testValueTextBox = createTextBox(new Callback<Double, String>() {
            @Override
            public void onFailure(String reason) {
                criterion.setTestValue(null);
            }

            @Override
            public void onSuccess(Double result) {
                criterion.setTestValue(result);
            }
        }, CentrifugeConstantsLocator.get().chartSelectDialog_enterThresholdValue());

        row.add(testValueTextBox);

        Double testValue = criterion.getTestValue();

        if (testValue != null) {
            testValueTextBox.setText(testValue.toString());
        }
    }

    @Override
    public boolean isValid() {
        if (criterion == null) {
            return false;
        } else {
            return criterion.getTestValue() != null;
        }
    }
}
