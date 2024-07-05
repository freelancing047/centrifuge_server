package csi.client.gwt.viz.chart.selection.criterion;

import com.github.gwtbootstrap.client.ui.FluidRow;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.gwt.core.client.Callback;
import com.google.gwt.dom.client.Style;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.chart.selection.ValidationListener;
import csi.client.gwt.viz.graph.tab.pattern.settings.InputEvent;
import csi.client.gwt.viz.graph.tab.pattern.settings.InputHandler;
import csi.server.common.model.visualization.chart.PositiveIntegerTypeCriterion;

public class PositiveIntegerTypeCriterionView implements CriterionView {
    private PositiveIntegerTypeCriterion criterion;

    private ValidationListener validationListener;

    public PositiveIntegerTypeCriterionView(ValidationListener listener, PositiveIntegerTypeCriterion criterion) {
        this.criterion = criterion;
        this.validationListener = listener;
    }

    @Override
    public void setup(FluidRow row) {
        TextBox testValueTextBox = createTextBox(new Callback<Integer, String>() {
            @Override
            public void onFailure(String reason) { criterion.setTestValue(null); }
            @Override
            public void onSuccess(Integer result) { criterion.setTestValue(result); }
        }, CentrifugeConstantsLocator.get().chartSelectDialog_enterThresholdValue());
        row.add(testValueTextBox);

        Integer testValue = criterion.getTestValue();
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

    protected TextBox createTextBox(final Callback<Integer, String> callback, String placeholderText) {
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
                    Integer integerValue = Integer.parseInt(stringValue);
                    if(integerValue <= 0) {
                        integerValue = 1;
                        textBox.setValue(integerValue.toString());
                    }
                    callback.onSuccess(integerValue);
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
