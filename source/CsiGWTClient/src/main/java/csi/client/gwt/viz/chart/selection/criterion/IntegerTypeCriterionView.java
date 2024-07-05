package csi.client.gwt.viz.chart.selection.criterion;

import com.github.gwtbootstrap.client.ui.Column;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.gwt.core.client.Callback;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;

import csi.client.gwt.viz.chart.selection.ValidationListener;
import csi.client.gwt.viz.graph.tab.pattern.settings.InputEvent;
import csi.client.gwt.viz.graph.tab.pattern.settings.InputHandler;

public abstract class IntegerTypeCriterionView implements CriterionView {
	private ValidationListener validationListener;
	
	public IntegerTypeCriterionView(ValidationListener validationListener) {
		this.validationListener = validationListener;
	}
	
	protected TextBox createTextBox(final Callback<Integer, String> callback, String placeholderText) {
		final TextBox textBox = new TextBox();
		textBox.setWidth("120px");
			{
				Style style = textBox.getElement().getStyle();
				style.setMargin(0, Unit.PX);
			}
		textBox.addBitlessDomHandler(new InputHandler() {
			@Override
			public void onInput(InputEvent inputEvent) {
				try {
					String stringValue = textBox.getValue().trim();
					Integer doubleValue = Integer.parseInt(stringValue);
					callback.onSuccess(doubleValue);
				} catch (NumberFormatException e) {
					String textBoxValue = textBox.getValue();
					if (!textBoxValue.equals("-")) {
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