package csi.client.gwt.viz.chart.selection.criterion;

import com.github.gwtbootstrap.client.ui.FluidRow;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.gwt.core.client.Callback;
import com.google.gwt.dom.client.Style;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.chart.selection.ValidationListener;
import csi.client.gwt.viz.graph.tab.pattern.settings.InputEvent;
import csi.client.gwt.viz.graph.tab.pattern.settings.InputHandler;
import csi.server.common.model.visualization.chart.ZeroToOneTypeCriterion;

public class ZeroToOneTypeCriterionView implements CriterionView {
    private ZeroToOneTypeCriterion criterion;

    private ValidationListener validationListener;

    public ZeroToOneTypeCriterionView(ValidationListener validationListener, ZeroToOneTypeCriterion criterion) {
        this.criterion = criterion;
        this.validationListener = validationListener;
    }

    @Override
    public void setup(FluidRow row) {
        TextBox testValueTextBox = createTextBox(new Callback<Double, String>() {
            @Override
            public void onFailure(String reason) { criterion.setTestValue(null); }
            @Override
            public void onSuccess(Double result) { criterion.setTestValue(result); }
        }, CentrifugeConstantsLocator.get().chartSelectDialog_enterThresholdValue());
        row.add(testValueTextBox);

        Double testValue = criterion.getTestValue();
        if(testValue != null) {
            testValueTextBox.setText(testValue.toString());
        }
    }

    @Override
    public boolean isValid() {
        if(criterion == null) {
            return false;
        } else {
            return criterion.getTestValue() != null;
        }
    }

    protected TextBox createTextBox(final Callback<Double, String> callback, String placeholderText) {
        final TextBox textBox = new TextBox();
        textBox.setWidth("120px");
        {
            Style style = textBox.getElement().getStyle();
            style.setMargin(0, Style.Unit.PX);
        }
        textBox.addBitlessDomHandler(new InputHandler() {
            @Override
            public void onInput(InputEvent inputEvent) {
                try {
                    String stringValue = textBox.getValue().trim();
                    Double percentageValue = Double.parseDouble(stringValue);
                    if(percentageValue > 100D) {
                        percentageValue = 100D;
                        textBox.setValue(percentageValue.toString());
                    } else if(percentageValue <= 0) {
                        percentageValue = 1D;
                        textBox.setValue(percentageValue.toString());
                    }
                    callback.onSuccess(percentageValue);
                } catch (NumberFormatException e) {
                    String textBoxValue = textBox.getValue();
                    if(!textBoxValue.equals("-")) {
                        textBox.setValue("");
                    }
                    callback.onFailure("NumberFormatException");
                } finally {
                    validationListener.checkCriteriaValidity();
                }
            }
        }, InputEvent.getType());
        textBox.getElement().setPropertyString("placeholder", placeholderText);
        return textBox;
    }
}
