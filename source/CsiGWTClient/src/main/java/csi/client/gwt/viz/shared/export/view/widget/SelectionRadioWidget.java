package csi.client.gwt.viz.shared.export.view.widget;

import com.github.gwtbootstrap.client.ui.RadioButton;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.shared.export.SelectionOnlyHandler;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.buttons.Button;

/**
 * Show when toggling selection vs entire on a CSV.
 * @author Centrifuge Systems, Inc.
 */
public class SelectionRadioWidget extends Composite{
	private CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    private final RadioButton entire = new RadioButton("selection", _constants.selectionRadioWidget_all());
    private final RadioButton selectionOnly = new RadioButton("selection", _constants.selectionRadioWidget_currentSelection());
    private final boolean initialWidgetValue;
    private final SelectionOnlyHandler selectionOnlyHandler;

    public SelectionRadioWidget(boolean initialWidgetValue, SelectionOnlyHandler selectionOnlyHandler){
        this.initialWidgetValue = initialWidgetValue;
        this.selectionOnlyHandler = selectionOnlyHandler;
        VerticalPanel mainPanel = new VerticalPanel();

        mainPanel.add(buildInstructionText());
        mainPanel.add(buildRadioPanel());
        mainPanel.add(buildButtonBar());

        setInitialValue();

        initWidget(mainPanel);
    }

    private Label buildInstructionText() {
        Label instructionLabel = new Label(_constants.selectionRadioWidget_instruction());
        instructionLabel.getElement().getStyle().setColor(Dialog.txtInfoColor);
        return instructionLabel;
    }

    private HorizontalPanel buildButtonBar() {
        Button cancelButton = new Button(_constants.dialog_CancelButton());
        cancelButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                selectionOnlyHandler.onUseSelectionOnly(initialWidgetValue);
            }
        });

        Button applyButton = buildSetTypeButton();

        HorizontalPanel hp = new HorizontalPanel();
        hp.add(applyButton);
        hp.add(cancelButton);
        hp.setCellWidth(applyButton, "60px");
        return hp;
    }

    private HorizontalPanel buildRadioPanel() {
        HorizontalPanel hp = new HorizontalPanel();

        hp.add(entire);
        hp.add(selectionOnly);
        hp.setCellWidth(entire, "100px");
        return hp;
    }

    private void setInitialValue() {
        if(initialWidgetValue){
            selectionOnly.setValue(Boolean.TRUE);
        }
        else{
            entire.setValue(Boolean.TRUE);
        }
    }

    private Button buildSetTypeButton() {
        Button apply = new Button(_constants.dialog_ApplyButton());
        apply.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if(selectionOnlyHandler != null)
                    selectionOnlyHandler.onUseSelectionOnly(selectionOnly.getValue());
            }
        });
        return apply;
    }

}
