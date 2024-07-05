package csi.client.gwt.viz.graph.link.settings;

import static com.google.common.base.Preconditions.checkNotNull;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.CheckBox;
import com.github.gwtbootstrap.client.ui.HelpInline;
import com.github.gwtbootstrap.client.ui.RadioButton;
import com.github.gwtbootstrap.client.ui.Tab;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.common.collect.Lists;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell;
import com.sencha.gxt.core.client.dom.XDOM;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.widget.core.client.ColorPalette;
import com.sencha.gxt.widget.core.client.form.ComboBox;

import csi.client.gwt.util.FieldDefUtils;
import csi.client.gwt.util.FieldDefUtils.SortOrder;
import csi.client.gwt.viz.graph.node.settings.SizingAttribute;
import csi.client.gwt.viz.graph.node.settings.appearance.SizingAttributeComboBox;
import csi.client.gwt.viz.graph.node.settings.tooltip.TooltipFunction;
import csi.client.gwt.viz.graph.shared.AbstractGraphObjectScale.ScaleMode;
import csi.client.gwt.widget.combo_boxes.FieldDefComboBox;
import csi.server.common.model.FieldDef;
import csi.shared.core.color.ClientColorHelper;
import csi.shared.core.color.ClientColorHelper.Color;

public class LinkAppearanceTab {

    private static final String[] linkColors = { "660000", "990000", "CC0000", "CC3333", "EA4C88", "D10553", "823CC8",
            "663399", "333399", "0066CC", "0099CC", "7AD9F9", "66CCCC", "74E618", "77CC33", "336600", "666600",
            "999900", "CCCC33", "EAEA26", "FFFF00", "FFCC33", "FF9900", "CE7C00", "FF6600", "CC6633", "996633",
            "AA6117", "663300", "000000", "999999", "CCCCCC" };
    private static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);
    @UiField(provided = true)
    FieldDefComboBox labelFdcb;
    @UiField(provided = true)
    FieldDefComboBox typeFieldList;
    @UiField
    CheckBox hideSizeCheckBox;
    @UiField
    CheckBox colorCheckBox;
    @UiField
    CheckBox labelCheckBox;
    @UiField
    RadioButton labelDynamic;
    @UiField
    RadioButton labelFixed;
    @UiField
    TextBox labelTextBox;
    @UiField
    RadioButton typeDynamic;
    @UiField
    RadioButton typeFixed;
    @UiField
    TextBox typeTextBox;
    @UiField
    TextBox nameTextBox;
    @UiField
    SizingAttributeComboBox sizeByAttribute;
    @UiField
    RadioButton sizeDynamic;
    @UiField
    RadioButton sizeFixed;
    @UiField
    HelpInline staticSizeHelp;
    @UiField
    TextBox staticSizeTextBox;
    @UiField
    Button colorButton;
    @UiField
    TextBox colorTextBox;
    @UiField(provided = true)
    ColorPalette colorPalette;
    @UiField(provided = true)
    ComboBox<TooltipFunction> transparencyFunctionComboBox;
    @UiField
    FieldDefComboBox transparencyFunctionFdcb;
    @UiField
    SizingAttributeComboBox transparencyByAttribute;
    @UiField
    HelpInline staticTransparencyHelp;
    @UiField
    TextBox staticTransparencyTextBox;
    @UiField
    RadioButton transparencyDynamic;
    @UiField
    RadioButton transparencyFixed;
    @UiField
    CheckBox hideTransaprencyCheckBox;
    @UiField
    RadioButton transparencyComputed;
    @UiField(provided = true)
    ComboBox<TooltipFunction> sizeFunctionComboBox;
    @UiField
    FieldDefComboBox sizeFunctionFdcb;
    @UiField
    RadioButton sizeComputed;
    private LinkSettings linkSettings;
    private Tab tab;
    private LinkSettingsPresenter presenter;

    public LinkAppearanceTab(LinkSettings linkSettings) {
        this.linkSettings = linkSettings;
        labelFdcb = new FieldDefComboBox();
        typeFieldList = new FieldDefComboBox();
        colorPalette = new ColorPalette(linkColors, linkColors);

        initializeSizeFunctionCb();
        initializeTransparencyFunctionCb();

        tab = uiBinder.createAndBindUi(this);


        sizeByAttribute.getStore().clear();
        sizeByAttribute.getStore().add(SizingAttribute.COUNT);
        sizeByAttribute.setValue(SizingAttribute.COUNT);
        
        transparencyByAttribute.getStore().clear();
        transparencyByAttribute.getStore().add(SizingAttribute.COUNT);
        transparencyByAttribute.setValue(SizingAttribute.COUNT);
        
        initializeTypeFdcb(linkSettings);
        initializeSizeFunctionFdcb(linkSettings);
        initializeTransparencyFunctionFdcb(linkSettings);
    }

    @UiHandler("colorButton")
    void colorButonClickHandler(ClickEvent event) {
        if (colorPalette.isVisible()) {
            colorPalette.setVisible(false);
        } else {

            colorPalette.getElement().getStyle().setZIndex(XDOM.getTopZIndex());
            colorPalette.setVisible(true);
        }
    }

    @UiHandler("colorPalette")
    public void onColorSelection(SelectionEvent<String> event) {
        presenter.setColor(ClientColorHelper.get().makeFromHex(event.getSelectedItem()));
        colorPalette.setVisible(false);
    }

    private void initializeSizeFunctionCb() {
        //sizeFunction combobox need initialization
        sizeFunctionComboBox = new ComboBox<TooltipFunction>(new ComboBoxCell<>(
                new ListStore<>(new ModelKeyProvider<TooltipFunction>() {

                    @Override
                    public String getKey(TooltipFunction item) {
                        return item.toString();
                    }
                }), new LabelProvider<TooltipFunction>() {

            @Override
            public String getLabel(TooltipFunction item) {
                return item.getDisplayString();
            }
        }));
        sizeFunctionComboBox.getStore().addAll(Lists.newArrayList(TooltipFunction.values()));
        sizeFunctionComboBox.setAllowTextSelection(false);
        sizeFunctionComboBox.setTriggerAction(ComboBoxCell.TriggerAction.ALL);

        sizeFunctionComboBox.addSelectionHandler(new SelectionHandler<TooltipFunction>() {

            @Override
            public void onSelection(SelectionEvent<TooltipFunction> event) {
                presenter.setSizeFunction(event.getSelectedItem());//FIXME
            }
        });
        
        if(sizeFunctionComboBox.getValue() == null){

            LinkSettingsModel model = linkSettings.getModel();
            TooltipFunction tooltipFunction;
            if(model.getLinkSize() != null && model.getLinkSize().getFunction() != null){
                tooltipFunction = model.getLinkSize().getFunction();
            } else {
                tooltipFunction = sizeFunctionComboBox.getStore().get(0);
            }
            model.getLinkSize().setFunction(tooltipFunction);
            sizeFunctionComboBox.setValue(tooltipFunction, true);
        }
    }

    private void initializeTransparencyFunctionCb() {
        //transparencyFunction combobox need initialization
        transparencyFunctionComboBox = new ComboBox<TooltipFunction>(new ComboBoxCell<>(
                new ListStore<>(new ModelKeyProvider<TooltipFunction>() {

                    @Override
                    public String getKey(TooltipFunction item) {
                        return item.toString();
                    }
                }), new LabelProvider<TooltipFunction>() {

            @Override
            public String getLabel(TooltipFunction item) {
                return item.getDisplayString();
            }
        }));
        transparencyFunctionComboBox.getStore().addAll(Lists.newArrayList(TooltipFunction.values()));
        transparencyFunctionComboBox.setAllowTextSelection(false);
        transparencyFunctionComboBox.setTriggerAction(ComboBoxCell.TriggerAction.ALL);
        
        if(transparencyFunctionComboBox.getValue() == null){
            TooltipFunction value;
            LinkSettingsModel model = linkSettings.getModel();
            if(model.getTransparency() != null && model.getTransparency().getFunction() != null){
                value = model.getTransparency().getFunction();
            } else {
                value = transparencyFunctionComboBox.getStore().get(0);
            }
            model.getTransparency().setFunction(value);
            transparencyFunctionComboBox.setValue(value, true);
        }

        transparencyFunctionComboBox.addSelectionHandler(new SelectionHandler<TooltipFunction>() {

            @Override
            public void onSelection(SelectionEvent<TooltipFunction> event) {
                presenter.setTransparencyFunction(event.getSelectedItem());
                //FIXME
            }
        });
    }

    private void initializeTypeFdcb(LinkSettings linkSettings) {
        labelFdcb.getStore().addAll(
                FieldDefUtils.getSortedNonStaticFields(
                        linkSettings.getGraphSettings().getDataViewDef().getModelDef(),
                        SortOrder.ALPHABETIC));
        labelFdcb.setSelectedIndex(0);
        typeFieldList.getStore().addAll(
                FieldDefUtils.getSortedNonStaticFields(
                        linkSettings.getGraphSettings().getDataViewDef().getModelDef(),
                        SortOrder.ALPHABETIC));
        typeFieldList.setSelectedIndex(0);
    }

    private void initializeSizeFunctionFdcb(LinkSettings linkSettings) {
        sizeFunctionFdcb.getStore().addAll(
                FieldDefUtils.getSortedNonStaticFields(
                        linkSettings.getGraphSettings().getDataViewDef().getModelDef(),
                        SortOrder.ALPHABETIC));
        sizeFunctionFdcb.addSelectionHandler(new SelectionHandler<FieldDef>() {

            @Override
            public void onSelection(SelectionEvent<FieldDef> event) {
                presenter.setSizeField(event.getSelectedItem());
                sizeFunctionComboBox.setEnabled(true);
            }
        });
        
        if(sizeFunctionFdcb.getValue() == null){

            LinkSettingsModel model = linkSettings.getModel();
            FieldDef value;
            if(model.getLinkSize() != null && model.getLinkSize().getField() != null){
                value = model.getLinkSize().getField();
            } else {
                value = sizeFunctionFdcb.getStore().get(0);
            }
            model.getLinkSize().setField(value);
            sizeFunctionFdcb.setValue(value, true);
        }
    }

    private void initializeTransparencyFunctionFdcb(LinkSettings linkSettings) {
        transparencyFunctionFdcb.getStore().addAll(
                FieldDefUtils.getSortedNonStaticFields(
                        linkSettings.getGraphSettings().getDataViewDef().getModelDef(),
                        SortOrder.ALPHABETIC));
        transparencyFunctionFdcb.addSelectionHandler(new SelectionHandler<FieldDef>() {

            @Override
            public void onSelection(SelectionEvent<FieldDef> event) {
                presenter.setTransparencyField(event.getSelectedItem());
            }
        });
        
        if(transparencyFunctionFdcb.getValue() == null){
            
            LinkSettingsModel model = linkSettings.getModel();
            FieldDef value;
            if(model.getTransparency() != null && model.getTransparency().getField() != null){
                value = model.getTransparency().getField();
            } else {
                value = transparencyFunctionFdcb.getStore().get(0);
            }
            model.getTransparency().setField(value);
            transparencyFunctionFdcb.setValue(value, true);
        }
    }

    protected void bind(LinkSettingsPresenter presenter) {
        this.presenter = presenter;
    }

    public Tab getTab() {
        return tab;
    }

    @UiHandler("colorTextBox")
    void onColorTextInput(ValueChangeEvent<String> event) {
        checkNotNull(event);
        String value = checkNotNull(event.getValue());
        if (value.charAt(0) == '#') {
            value = value.substring(1);
        }
        Color color = null;
        try {
            color = ClientColorHelper.get().makeFromHex(value);
        } catch (Exception e) {
            // squelch
        }
        presenter.setColor(color);
        colorPalette.setVisible(false);
    }

    @UiHandler("labelFdcb")
    public void onFieldList(SelectionEvent<FieldDef> event) {
        checkNotNull(event);
        if (presenter != null) {
            presenter.setLabelField(event.getSelectedItem());
        }
    }

    @UiHandler("hideSizeCheckBox")
    public void onHideSizeCheckBox(ClickEvent e) {
        if (presenter != null) {
            presenter.setHasSize(hideSizeCheckBox.getValue());
        }
    }
    
    @UiHandler("colorCheckBox")
    public void toggleColorSettings(ValueChangeEvent<Boolean> event) {
        enableColorPicker(event.getValue());
        presenter.updateColorSettings(event.getValue(), true);
    }
    
    public void enableColorPicker(boolean value) {
        colorButton.setEnabled(value);
        colorTextBox.setEnabled(value);
    }

    @UiHandler("hideTransaprencyCheckBox")
    public void onHideTransparencyCheckBox(ClickEvent e) {
        if (presenter != null) {
            presenter.setHasTransparency(hideTransaprencyCheckBox.getValue());
        }
    }

    @UiHandler("labelCheckBox")
    public void onLabelCheckBox(ClickEvent e) {
        if (presenter != null) {
            presenter.setHideLabel(!labelCheckBox.getValue());
        }
    }

    @UiHandler("labelDynamic")
    public void onLabelDynamic(ClickEvent e) {
        if (presenter != null) {
            presenter.setLabelFixed(!labelDynamic.getValue());
        }
    }

    @UiHandler("labelFixed")
    public void onLabelFixed(ClickEvent e) {
        if (presenter != null) {
            presenter.setLabelFixed(labelFixed.getValue());
        }
    }

    @UiHandler("labelTextBox")
    public void onLabelTextBox(ValueChangeEvent<String> e) {
        checkNotNull(e);
        if (presenter != null) {
            presenter.setLabelText(e.getValue());
        }
    }

    @UiHandler("sizeByAttribute")
    public void onSizeByAttribute(SelectionEvent<SizingAttribute> e) {
        if (presenter != null) {
            presenter.setSizeMeasure(e.getSelectedItem().getName());
        }
    }

    @UiHandler("sizeDynamic")
    public void onSizeDynamic(ClickEvent e) {
        if (presenter != null) {
            presenter.setSizeMode(ScaleMode.DYNAMIC);
            if(sizeByAttribute.getValue() != null)
                presenter.setSizeMeasure(sizeByAttribute.getValue().getName());
        }
    }

    @UiHandler("sizeComputed")
    public void onSizeComputed(ClickEvent e) {
        if (presenter != null) {
            presenter.setSizeMode(ScaleMode.COMPUTED);
            //TODO: do i need to set other values at this time?
        }
    }

    @UiHandler("sizeFixed")
    public void onSizeFixed(ClickEvent e) {
        if (presenter != null) {
            presenter.setSizeMode(ScaleMode.FIXED);
        }
    }

    @UiHandler("staticSizeTextBox")
    public void onStaticSizeTextBox(ValueChangeEvent<String> e) {
        if (presenter != null) {
            try {
                double value = Double.parseDouble(e.getValue());
                presenter.setSizeValue(value);
            } catch (NumberFormatException exception) {
                // TODO: error
            }
        }
    }

    @UiHandler("transparencyByAttribute")
    public void onTransparencyByAttribute(SelectionEvent<SizingAttribute> e) {
        if (presenter != null) {
            presenter.setTransparencyMeasure(e.getSelectedItem().getName());
        }
    }

    @UiHandler("transparencyDynamic")
    public void onTransparencyDynamic(ClickEvent e) {
        if (presenter != null) {
            presenter.setTransparencyMode(ScaleMode.DYNAMIC);
            if(transparencyByAttribute.getValue() != null)
                presenter.setTransparencyMeasure(transparencyByAttribute.getValue().getName());
        }
    }

    @UiHandler("transparencyFixed")
    public void onTransparencyFixed(ClickEvent e) {
        if (presenter != null) {
            presenter.setTransparencyMode(ScaleMode.FIXED);
        }
    }

    @UiHandler("transparencyComputed")
    public void onTransparencyComputed(ClickEvent e) {
        if (presenter != null) {
            presenter.setTransparencyMode(ScaleMode.COMPUTED);
        }
    }

    @UiHandler("staticTransparencyTextBox")
    public void onStaticTransparencyTextBox(ValueChangeEvent<String> e) {
        if (presenter != null) {
            try {
                double value = Double.parseDouble(e.getValue());
                presenter.setTransparencyValue(value);
            } catch (NumberFormatException exception) {
                // TODO: error
            }
        }
    }

    @UiHandler("typeDynamic")
    public void onTypeDynamic(ClickEvent e) {
        if (presenter != null) {
            presenter.setTypeFixed(!typeDynamic.getValue());
        }
    }

    @UiHandler("typeFieldList")
    public void onTypeFieldList(SelectionEvent<FieldDef> event) {
        checkNotNull(event);
        if (presenter != null) {
            presenter.setTypeField(event.getSelectedItem());
        }
    }

    @UiHandler("typeFixed")
    public void onTypeFixed(ClickEvent e) {
        if (presenter != null) {
            presenter.setTypeFixed(typeFixed.getValue());
        }
    }

    @UiHandler("typeTextBox")
    public void onTypeTextBox(ValueChangeEvent<String> e) {
        checkNotNull(e);
        if (presenter != null) {
            presenter.setTypeText(e.getValue());
        }
    }

    public void setColor(LinkColor color) {
        colorTextBox.setValue(color.getColor().toString().toUpperCase());
        colorButton.getElement().getStyle().setColor(color.getColor().toString());
    }

    public void setLabel(LinkLabel label) {
        checkNotNull(label);
        boolean hidden = label.isHidden();
        labelCheckBox.setValue(!hidden);

        boolean fixed = label.isFixed();
        labelFixed.setValue(fixed);
        labelDynamic.setValue(!fixed);
        labelTextBox.setValue(label.getText());
        labelTextBox.setVisible(fixed);
        labelFdcb.setValue(label.getFieldDef(), false);
        labelFdcb.setVisible(!fixed);
    }

    public void setName(String name) {
        nameTextBox.setValue(name);
    }

    public void setSize(LinkSize size) {
        checkNotNull(size);
        boolean enabled = size.isEnabled();
        hideSizeCheckBox.setValue(enabled);
        sizeFixed.setEnabled(enabled);
        sizeDynamic.setEnabled(enabled);
        staticSizeTextBox.setEnabled(enabled);
        sizeByAttribute.setEnabled(enabled);

        sizeFixed.setVisible(true);
        sizeDynamic.setVisible(true);
        ScaleMode mode = size.getMode();

        switch (mode) {
            case DYNAMIC:
                sizeFixed.setValue(false);
                sizeDynamic.setValue(true);
                sizeComputed.setValue(false);
                staticSizeTextBox.setVisible(false);
                staticSizeHelp.setVisible(false);
                sizeByAttribute.setVisible(true);
                sizeByAttribute.setValue(size.getMeasure(),false);
                sizeFunctionComboBox.setVisible(false);
                sizeFunctionFdcb.setVisible(false);
                break;
            case FIXED:
                sizeFixed.setValue(true);
                sizeDynamic.setValue(false);
                sizeComputed.setValue(false);
                staticSizeTextBox.setValue(size.getValue() + "");
                staticSizeTextBox.setVisible(true);
                staticSizeHelp.setVisible(true);
                sizeByAttribute.setVisible(false);
                sizeFunctionComboBox.setVisible(false);
                sizeFunctionFdcb.setVisible(false);
                break;
            case COMPUTED:
                sizeFixed.setValue(false);
                sizeDynamic.setValue(false);
                sizeComputed.setValue(true);
                staticSizeTextBox.setVisible(false);
                staticSizeHelp.setVisible(false);
                sizeByAttribute.setVisible(false);
                sizeFunctionComboBox.setVisible(true);
                sizeFunctionComboBox.setValue(size.getFunction());
                sizeFunctionFdcb.setVisible(true);
                sizeFunctionFdcb.setValue(size.getField());
                break;
            default:
                break;
        }
    }

    public SizingAttribute getSizeEnum(String attribute) {
        if(attribute == SizingAttribute.DEGREE.getName()){
            return SizingAttribute.DEGREE;
        } 
        return SizingAttribute.valueOf(attribute.toUpperCase());
    }

    public void setTransparency(LinkTransparency linkTransparency) {
        checkNotNull(linkTransparency);
        boolean enabled = linkTransparency.isEnabled();
        hideTransaprencyCheckBox.setValue(enabled);
        transparencyFixed.setEnabled(enabled);
        transparencyDynamic.setEnabled(enabled);
        staticTransparencyTextBox.setEnabled(enabled);
        transparencyByAttribute.setEnabled(enabled);

        transparencyFixed.setVisible(true);
        transparencyDynamic.setVisible(true);
        ScaleMode mode = linkTransparency.getMode();

        switch (mode) {
            case DYNAMIC:
                transparencyFixed.setValue(false);
                transparencyDynamic.setValue(true);
                transparencyComputed.setValue(false);
                staticTransparencyTextBox.setVisible(false);
                staticTransparencyHelp.setVisible(false);
                transparencyByAttribute.setVisible(true);
                transparencyByAttribute.setValue(linkTransparency.getMeasure(), false);
                transparencyFunctionComboBox.setVisible(false);
                transparencyFunctionFdcb.setVisible(false);
                break;
            case FIXED:
                transparencyFixed.setValue(true);
                transparencyDynamic.setValue(false);
                transparencyComputed.setValue(false);
                staticTransparencyTextBox.setValue(linkTransparency.getValue() + "");
                staticTransparencyTextBox.setVisible(true);
                staticTransparencyHelp.setVisible(true);
                transparencyByAttribute.setVisible(false);
                transparencyFunctionComboBox.setVisible(false);
                transparencyFunctionFdcb.setVisible(false);
                break;
            case COMPUTED:
                transparencyFixed.setValue(false);
                transparencyDynamic.setValue(false);
                transparencyComputed.setValue(true);
                staticTransparencyTextBox.setVisible(false);
                staticTransparencyHelp.setVisible(false);
                transparencyByAttribute.setVisible(false);
                transparencyFunctionComboBox.setVisible(true);
                transparencyFunctionComboBox.setValue(linkTransparency.getFunction());
                transparencyFunctionFdcb.setVisible(true);
                transparencyFunctionFdcb.setValue(linkTransparency.getField());
                break;
            default:
                break;
        }
    }

    public void setType(LinkType type) {
        checkNotNull(type);
        boolean hidden = type.isHidden();
        typeFixed.setEnabled(!hidden);
        typeDynamic.setEnabled(!hidden);
        typeTextBox.setEnabled(!hidden);
        typeFieldList.setEnabled(!hidden);

        boolean fixed = type.isFixed();
        typeFixed.setValue(fixed);
        typeDynamic.setValue(!fixed);
        typeTextBox.setValue(type.getText());
        typeTextBox.setVisible(fixed);
        typeFieldList.setValue(type.getFieldDef(), false);
        typeFieldList.setVisible(!fixed);
    }

    @UiHandler("nameTextBox")
    public void onNameTextBox(ValueChangeEvent<String> e) {
        checkNotNull(e);
        if (presenter != null) {
            presenter.setName(e.getValue());
        }
    }

    interface SpecificUiBinder extends UiBinder<Tab, LinkAppearanceTab> {
    }

    public void setColorCheckBox(Boolean value) {
        colorCheckBox.setValue(value);
    }

    public void setColorEnabled(boolean colorEnabled) {
       colorCheckBox.setValue(colorEnabled);
       enableColorPicker(colorEnabled);
       presenter.updateColorSettings(colorEnabled, true);
    }

    public void setWidthEnabled(Boolean widthEnabled) {
        hideSizeCheckBox.setValue(widthEnabled);
        if (presenter != null) {
            presenter.setHasSize(hideSizeCheckBox.getValue());
        }
    }
}
