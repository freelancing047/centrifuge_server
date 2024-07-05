package csi.client.gwt.viz.shared.export.view.widget;

import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.shared.export.PickResolutionHandler;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.buttons.Button;

/**
 * Shown when changing the resolution on an image.
 * @author Centrifuge Systems, Inc.
 */
public class ResolutionWidget extends Composite {
	private CentrifugeConstants _constants = CentrifugeConstantsLocator.get();
	
    private final VerticalPanel mainPanel = new VerticalPanel();
    private final TextBox widthBox = new TextBox();
    private final TextBox heightBox = new TextBox();

    private final int initialWidth;
    private final int initialHeight;
    private final PickResolutionHandler pickResolutionHandler;

    public ResolutionWidget(int initialWidth, int initialHeight, PickResolutionHandler pickResolutionHandler) {
        this.initialWidth = initialWidth;
        this.initialHeight = initialHeight;
        this.pickResolutionHandler = pickResolutionHandler;
        buildUI();

        setInitialValue(initialWidth, initialHeight);

        initWidget(mainPanel);
    }

    private void setInitialValue(int desiredWidth, int desiredHeight) {
        if(desiredWidth <= 0)
            desiredWidth = 0;
        if(desiredHeight <= 0)
            desiredHeight = 0;
        widthBox.setText("" + desiredWidth);
        heightBox.setText("" + desiredHeight);
    }

    private void buildUI() {
        HorizontalPanel widthPanel = createWidthPanel();
        HorizontalPanel heightPanel = createHeightPanel();

        sizeTextBoxes();

        mainPanel.add(buildInstructionText());
        mainPanel.add(createWidthHeightPanel(widthPanel, heightPanel));
        mainPanel.add(buildButtonBar());
    }

    private HorizontalPanel buildButtonBar() {
        Button cancelButton = new Button(_constants.dialog_CancelButton());
        cancelButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                pickResolutionHandler.onResolution(initialWidth, initialHeight);
            }
        });

        Button applyButton = buildSetResolutionButton();

        HorizontalPanel hp = new HorizontalPanel();
        hp.add(applyButton);
        hp.add(cancelButton);
        hp.setCellWidth(applyButton, "60px");
        return hp;
    }

    private HorizontalPanel createWidthHeightPanel(HorizontalPanel widthPanel, HorizontalPanel heightPanel) {
        HorizontalPanel hp = new HorizontalPanel();
        hp.add(widthPanel);
        hp.add(heightPanel);
        return hp;
    }

    private Label buildInstructionText() {
        Label instructionLabel = new Label(_constants.resolutionWidget_instruction());
        instructionLabel.getElement().getStyle().setColor(Dialog.txtInfoColor);
        return instructionLabel;
    }

    private void sizeTextBoxes() {
        widthBox.setWidth("50px");
        heightBox.setWidth("50px");
    }

    private HorizontalPanel createWidthPanel() {
        HorizontalPanel widthPanel = new HorizontalPanel();
        Label widthLabel = new Label(_constants.resolutionWidget_desiredWidth());
        widthPanel.add(widthLabel);
        widthPanel.add(widthBox);
        widthLabel.getElement().getStyle().setMarginTop(5, Style.Unit.PX);
        widthPanel.setCellWidth(widthLabel, "95px");
        widthPanel.setCellWidth(widthBox, "90px");

        return widthPanel;
    }

    private HorizontalPanel createHeightPanel() {
        HorizontalPanel heightPanel = new HorizontalPanel();
        Label heightLabel = new Label(_constants.resolutionWidget_desiredHeight());
        heightPanel.add(heightLabel);
        heightPanel.add(heightBox);
        heightLabel.getElement().getStyle().setMarginTop(5, Style.Unit.PX);
        heightPanel.setCellWidth(heightLabel, "100px");
        return heightPanel;
    }

    private Button buildSetResolutionButton() {
        Button apply = new Button(_constants.dialog_ApplyButton());
        apply.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (pickResolutionHandler == null)
                    return;

                int width = getIntValue(widthBox);
                int height = getIntValue(heightBox);

                pickResolutionHandler.onResolution(width, height);
            }
        });
        return apply;
    }

    private int getIntValue(TextBox textBox) {
        int value = 0;
        try{
            value = Integer.parseInt(textBox.getText());
        }catch (Exception e){
            //do nothing
        }
        return value;
    }

}
