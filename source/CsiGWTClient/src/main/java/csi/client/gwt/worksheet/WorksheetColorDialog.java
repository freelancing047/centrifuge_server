package csi.client.gwt.worksheet;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.constants.AlternateSize;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Float;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.ui.Label;
import com.sencha.gxt.widget.core.client.ColorPalette;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.widget.boot.SizeProvidingModal;
import csi.client.gwt.widget.ui.FullSizeLayoutPanel;
import csi.shared.core.color.ClientColorHelper;

public class WorksheetColorDialog {

    public static final String[] nodeColors = { "660000", "990000", "CC0000", "CC3333", "EA4C88", "D10553", "823CC8",
            "663399", "333399", "0066CC", "0099CC", "7AD9F9", "66CCCC", "74E618", "77CC33", "336600", "666600",
            "999900", "CCCC33", "EAEA26", "FFFF00", "FFCC33", "FF9900", "CE7C00", "FF6600", "CC6633", "996633",
            "AA6117", "663300", "000000", "999999", "CCCCCC" };

    private static final String HEX_PATTERN = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$";
    private SizeProvidingModal dialog;
    static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
    private WorksheetPresenter presenter;

    private ColorPalette colorPalette;
    private TextBox colorTextBox;
    private Button colorButton;
    private Label defaultColorButton;
    private com.github.gwtbootstrap.client.ui.base.InlineLabel closeButton;
    private Label label;
    private Integer color = null;
    private RegExp pattern;
    private String DEFAULT_GRAYTAB_COLOR = "#EAEAEA";



    public WorksheetColorDialog(WorksheetPresenter worksheetPresenter) {
        super();
        pattern = pattern.compile(HEX_PATTERN);
        dialog = new SizeProvidingModal();
        dialog.setWidth("200px");
        dialog.setHeight("160px");
        dialog.getElement().getStyle().setProperty("boxShadow", "none");
        presenter = worksheetPresenter;




        FullSizeLayoutPanel colorPanel = new FullSizeLayoutPanel();

        colorPalette = new ColorPalette(nodeColors, nodeColors);
        colorPalette.addSelectionHandler(event -> {
            String selectedItem = event.getSelectedItem();
            color = ClientColorHelper.get().makeFromHex(selectedItem).getIntColor();
            presenter.setColor(color);
            setColor(ClientColorHelper.get().make(color));
            dialog.hide();
        });

        label = new Label();
        label.setText(i18n.worksheetTabPanelColorTextBox());

        colorButton = new Button();
        colorButton.setIcon(IconType.TINT);
        colorButton.getElement().getStyle().setHeight(22, Style.Unit.PX);
        colorButton.getElement().getStyle().setWidth(20.0, Style.Unit.PX);
        colorButton.getElement().getStyle().setPadding(4.0 , Style.Unit.PX);
        colorButton.getElement().getStyle().setFontSize(18.0, Style.Unit.PX);
        if(worksheetPresenter.getColor() == null) {
            ClientColorHelper.Color currentColor = ClientColorHelper.get().makeFromHex(DEFAULT_GRAYTAB_COLOR.substring(1));
            colorButton.getElement().getStyle().setColor(currentColor.toString());
        } else if(worksheetPresenter.getColor() != null) {
            ClientColorHelper.Color currentColor = ClientColorHelper.get().make(worksheetPresenter.getColor());
            colorButton.getElement().getStyle().setColor(currentColor.toString());
        }

        defaultColorButton = new Label();
        defaultColorButton.setText(i18n.worksheetTabColorDialog_noColor());
        defaultColorButton.getElement().getStyle().setHeight(10, Style.Unit.PX);
        defaultColorButton.getElement().getStyle().setWidth(20.0, Style.Unit.PX);
        defaultColorButton.getElement().getStyle().setPadding(1.0 , Style.Unit.PX);
        defaultColorButton.getElement().getStyle().setFontSize(12.0, Style.Unit.PX);
        defaultColorButton.getElement().getStyle().setColor("#0000FF");
        defaultColorButton.getElement().getStyle().setCursor(Style.Cursor.POINTER);
        defaultColorButton.addMouseOverHandler(event -> {
            defaultColorButton.getElement().getStyle().setTextDecoration(Style.TextDecoration.UNDERLINE);
        });
        defaultColorButton.addMouseOutHandler(event -> {
            defaultColorButton.getElement().getStyle().setTextDecoration(Style.TextDecoration.NONE);
        });
        defaultColorButton.addClickHandler(event -> {
            color = ClientColorHelper.get().makeFromHex(DEFAULT_GRAYTAB_COLOR.substring(1)).getIntColor();
            presenter.setColor(color);
            setColor(ClientColorHelper.get().make(color));
            dialog.hide();
        });

        closeButton = new com.github.gwtbootstrap.client.ui.base.InlineLabel();
        closeButton.getElement().getStyle().setFontWeight(Style.FontWeight.BOLD);
        closeButton.getElement().getStyle().setFloat(Float.RIGHT);
        closeButton.getElement().getStyle().setColor("#C0C0C0");
        closeButton.getElement().getStyle().setCursor(Style.Cursor.POINTER);
        closeButton.setText(" X ");
        closeButton.addClickHandler(event -> {
            dialog.hide();
        });

        colorTextBox = new TextBox();
        colorTextBox.setAlternateSize(AlternateSize.SMALL);
        if(worksheetPresenter.getColor() == null) {
            colorTextBox.setValue(DEFAULT_GRAYTAB_COLOR);
//            colorTextBox.getElement().setPropertyString("placeholder", DEFAULT_GRAYTAB_COLOR);
        } else if(worksheetPresenter.getColor() != null) {
            colorTextBox.setValue(ClientColorHelper.get().make(worksheetPresenter.getColor()).toString());
//            colorTextBox.getElement().setPropertyString("placeholder", ClientColorHelper.get().make(worksheetPresenter.getColor()).toString());
        }
        colorTextBox.addValueChangeHandler(event -> {
            String value = event.getValue();
            if (value.charAt(0) == '#') {
                value = value.substring(1);
            }
            if (validateHex(value)) {
                color = ClientColorHelper.get().makeFromHex(value).getIntColor();
                presenter.setColor(ClientColorHelper.get().makeFromHex(value).getIntColor());
                setColor(ClientColorHelper.get().make(color));
                dialog.hide();
            }

        });

        colorTextBox.addKeyUpHandler(event -> {
            if (validateHex(colorTextBox.getValue())) {
                String value = colorTextBox.getValue();
                if (value.charAt(0) == '#') {
                    value = value.substring(1);
                }
                ClientColorHelper.Color currentColor = ClientColorHelper.get().makeFromHex(value);
                colorButton.getElement().getStyle().setColor(currentColor.toString());
            }
        });


        colorPanel.setWidth("190px");
        colorPanel.setHeight("140px");


        colorPanel.add(colorPalette);
        colorPanel.add(label);
        colorPanel.add(colorButton);
        colorPanel.add(colorTextBox);
        colorPanel.add(defaultColorButton);
        colorPanel.add(closeButton);

        colorPalette.setWidth("170px");
        colorPalette.setHeight("100px");
        label.setWidth("10px");
        label.setHeight("10px");
        colorButton.setWidth("15px");
        colorButton.setHeight("15px");
        colorTextBox.setWidth("60px");
        colorTextBox.setHeight("15px");
        defaultColorButton.setWidth("100px");
        defaultColorButton.setHeight("15px");
        closeButton.setWidth("5px");
        closeButton.setHeight("5px");

        colorPanel.setWidgetTopHeight(colorPalette, 0.0, Style.Unit.PX, 100.0, Style.Unit.PX);
        colorPanel.setWidgetLeftWidth(colorPalette, 0.0, Style.Unit.PX, 170.0, Style.Unit.PX);
        colorPanel.setWidgetTopHeight(label, 110.0, Style.Unit.PX, 25.0, Style.Unit.PX);
        colorPanel.setWidgetLeftWidth(label, 10.0, Style.Unit.PX, 40.0, Style.Unit.PX);
        colorPanel.setWidgetTopHeight(colorButton, 110.0, Style.Unit.PX, 25.0, Style.Unit.PX);
        colorPanel.setWidgetLeftWidth(colorButton, 50.0, Style.Unit.PX, 25.0, Style.Unit.PX);
        colorPanel.setWidgetTopHeight(colorTextBox, 110.0, Style.Unit.PX, 25.0, Style.Unit.PX);
        colorPanel.setWidgetLeftWidth(colorTextBox, 80.0, Style.Unit.PX, 80.0, Style.Unit.PX);
        colorPanel.setWidgetTopHeight(defaultColorButton, 85.0, Style.Unit.PX, 20.0, Style.Unit.PX);
        colorPanel.setWidgetLeftWidth(defaultColorButton, 10.0, Style.Unit.PX, 150.0, Style.Unit.PX);
        colorPanel.setWidgetTopHeight(closeButton,0.0, Style.Unit.PX, 20.0, Style.Unit.PX);
        colorPanel.setWidgetLeftWidth(closeButton, 170.0, Style.Unit.PX, 15.0, Style.Unit.PX);

        dialog.add(colorPanel);
        colorPalette.setVisible(true);
        dialog.showWithoutSettingMargin();

    }




    public void setColor(ClientColorHelper.Color color) {
        if (validateHex(color.toString())) {
            colorPalette.setValue(color.toString().substring(1).toUpperCase());
            colorTextBox.getElement().setPropertyString("placeholder", color.toString());
            colorTextBox.setValue(color.toString().toUpperCase());
            colorButton.getElement().getStyle().setColor(color.toString());
        }
    }

    public boolean validateHex(String hex) {
        if (!hex.startsWith("#")) {
            hex = "#" + hex;
        }
        MatchResult matcher = pattern.exec(hex);
        boolean isValid = matcher != null;
        return isValid;
    }

    public void show() { dialog.showWithoutSettingMargin();
    dialog.getBackdrop().addDomHandler(new MouseUpHandler() {
        @Override
        public void onMouseUp(MouseUpEvent event) {
            dialog.hide();
        }
    }, MouseUpEvent.getType());
    }

    public void setPosition(double top, double left, double windowRight) {
        if (windowRight - left > 200.0) {
            dialog.getElement().getStyle().setTop(top, Style.Unit.PX);
            dialog.getElement().getStyle().setLeft(left, Style.Unit.PX);
        } else {
            dialog.getElement().getStyle().setTop(top, Style.Unit.PX);
            dialog.getElement().getStyle().setLeft(windowRight - 200, Style.Unit.PX);
        }

    }

    public Integer getColor() {
        return this.color;
    }

    public void hide() { dialog.hide(); }

}
