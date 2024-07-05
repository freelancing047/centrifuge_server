package csi.client.gwt.viz.graph.plunk.edit.widgets;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.ControlLabel;
import com.github.gwtbootstrap.client.ui.Controls;
import com.github.gwtbootstrap.client.ui.PrependButton;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.constants.AlternateSize;
import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.sencha.gxt.core.client.dom.XDOM;
import com.sencha.gxt.widget.core.client.ColorPalette;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.widget.boot.Dialog;
import csi.shared.core.color.ClientColorHelper;

/**
 * @author Centrifuge Systems, Inc.
 */
public class ColorSelector extends Composite implements TakesValue<Integer> {

    private static final String[] AVAILABLE_COLORS = { "660000", "990000", "CC0000", "CC3333", "EA4C88", "D10553", "823CC8",//NON-NLS
            "663399", "333399", "0066CC", "0099CC", "7AD9F9", "66CCCC", "74E618", "77CC33", "336600", "666600",//NON-NLS
            "999900", "CCCC33", "EAEA26", "FFFF00", "FFCC33", "FF9900", "CE7C00", "FF6600", "CC6633", "996633",//NON-NLS
            "AA6117", "663300", "000000", "999999", "CCCCCC" };//NON-NLS

    private final CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
    private final HorizontalPanel hp = new HorizontalPanel();
    private final Controls mainPanel = new Controls();
    private final Button colorButton = new Button();
    private final TextBox colorTextBox = new TextBox();
    private final ColorPalette colorPalette = new ColorPalette(AVAILABLE_COLORS, AVAILABLE_COLORS);
    private final Button clearButton = new Button(i18n.clear());
    private final ControlLabel infoLabel = new ControlLabel(i18n.plunking_Color_Inherited_Info());

    public ColorSelector(){
        buildUI();
        initWidget(hp);
    }

    @Override
    public void setValue(Integer value) {
        if(value != null) {
            ClientColorHelper.Color color = ClientColorHelper.get().make(value);
            setColor(color.toString());
        }
    }

    @Override
    public Integer getValue() {
        String value = colorTextBox.getValue();
        if((value == null) || value.isEmpty()){
            return null;
        }
        if (value.charAt(0) == '#') {
            value = value.substring(1);
        }
        ClientColorHelper.Color color = ClientColorHelper.get().makeFromHex(value);
        return color.getIntColor();
    }

    private void buildUI() {
        PrependButton prependButton = new PrependButton();
        prependButton.add(colorButton);
        prependButton.add(colorTextBox);

        mainPanel.add(prependButton);
        mainPanel.add(colorPalette);
        mainPanel.add(clearButton);

        hp.add(mainPanel);
        hp.add(infoLabel);

        styleComponents();
        addHandlers();
    }

    private void addHandlers() {
        colorPalette.addSelectionHandler(new SelectionHandler<String>() {
            @Override
            public void onSelection(SelectionEvent<String> event) {
                String selectedColor = event.getSelectedItem();
                colorTextBox.setText(selectedColor);
                setColor(selectedColor);
                colorPalette.setVisible(false);
            }
        });
        colorButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (colorPalette.isVisible()) {
                    colorPalette.setVisible(false);
                } else {
                    colorPalette.setVisible(true);
                    colorPalette.getElement().getStyle().setZIndex(XDOM.getTopZIndex());
                }
            }
        });
        colorTextBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                setColor(colorTextBox.getText());
            }
        });
        clearButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                clearColor();
            }
        });
    }

    private void styleComponents() {
        colorPalette.addStyleName("plunked-node-edit-color-pallette");//NON-NLS
        colorPalette.setVisible(false);
        colorPalette.getElement().getStyle().setLeft(140, Style.Unit.PX);
        colorButton.setIcon(IconType.TINT);
        colorButton.getElement().getStyle().setHeight(22, Style.Unit.PX);
        colorTextBox.setAlternateSize(AlternateSize.SMALL);
        clearButton.setType(ButtonType.DANGER);
        clearButton.getElement().getStyle().setHeight(22, Style.Unit.PX);
        clearButton.getElement().getStyle().setMarginLeft(5, Style.Unit.PX);
        clearButton.getElement().getStyle().setMarginTop(-10, Style.Unit.PX);

        hp.setCellWidth(mainPanel, "200px");//NON-NLS
        infoLabel.getElement().getStyle().setColor(Dialog.txtInfoColor);
    }

    private void setColor(String selectedColor) {
        if(selectedColor == null) {
            return;
        }

        if(selectedColor.startsWith("#")) {
            selectedColor = selectedColor.substring(1);
        }

        if(isValidColor(selectedColor)) {
            String color = ClientColorHelper.get().makeFromHex(selectedColor).toString();
            colorTextBox.setValue(color);
            infoLabel.setVisible(false);
            colorButton.getElement().getStyle().setColor(color);
        }
        else {
            clearColor();
        }
    }

    private void clearColor() {
        colorTextBox.setValue("");
        colorButton.getElement().getStyle().setColor("#000");
        infoLabel.setVisible(true);
    }

    private boolean isValidColor(String selectedColor) {
        try {
            ClientColorHelper.get().makeFromHex(selectedColor);
            return true;
        }catch (Exception e){
            return false;
        }
    }

}
