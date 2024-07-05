package csi.client.gwt.viz.shared.export.view.widget;

import java.util.List;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.shared.export.ExportTypeHandler;
import csi.client.gwt.viz.shared.export.model.ExportType;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.buttons.Button;
import csi.client.gwt.widget.combo_boxes.StringComboBox;

/**
 * Shown when changing the export type.
 * @author Centrifuge Systems, Inc.
 */
public class ExportTypeDropdownWidget extends Composite {
	private CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    private final VerticalPanel mainPanel = new VerticalPanel();
    private final List<ExportType> availableExportTypes;
    private final ExportType initialExportType;
    private final ExportTypeHandler handler;
    private final StringComboBox dropdown = new StringComboBox();

    public ExportTypeDropdownWidget(List<ExportType> availableExportTypes, ExportType initialExportType, ExportTypeHandler handler) {
        this.availableExportTypes = availableExportTypes;
        this.initialExportType = initialExportType;
        this.handler = handler;

        buildUI();
        initWidget(mainPanel);
    }

    private void buildUI() {
        mainPanel.add(buildInstructionText());
        mainPanel.add(buildFileTypeControl());
        mainPanel.add(buildButtonBar());

    }

    private Label buildInstructionText() {
        Label instructionLabel = new Label(_constants.exportTypeDropdownWidget_instruction());
        instructionLabel.getElement().getStyle().setColor(Dialog.txtInfoColor);
        return instructionLabel;
    }

    private HorizontalPanel buildFileTypeControl(){
        HorizontalPanel hp = new HorizontalPanel();
        Label label = new Label(_constants.exportTypeDropdownWidget_fileType());
        label.getElement().getStyle().setMarginTop(5, Style.Unit.PX);
        buildDropdown();

        hp.add(label);
        hp.add(dropdown);
        hp.setHeight("35px");
        hp.setCellWidth(label, "60px");
        return hp;
    }

    private HorizontalPanel buildButtonBar() {
        Button cancelButton = new Button(_constants.cancel());
        cancelButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (handler != null)
                    handler.onSetExportType(initialExportType);
            }
        });

        Button applyButton = buildSetTypeButton();

        HorizontalPanel hp = new HorizontalPanel();
        hp.add(applyButton);
        hp.add(cancelButton);
        hp.setCellWidth(applyButton, "60px");
        return hp;
    }

    private Button buildSetTypeButton() {
        Button apply = new Button(_constants.dialog_ApplyButton());
        apply.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                ExportType exportType = ExportType.valueOf(dropdown.getValue());
                if (handler != null)
                    handler.onSetExportType(exportType);
            }
        });
        return apply;
    }

    private StringComboBox buildDropdown() {
        for (ExportType exportType : availableExportTypes) {
            dropdown.getStore().add(exportType.name());
        }
        dropdown.setValue(initialExportType.name(), true);

        return dropdown;
    }

}
