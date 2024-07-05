package csi.client.gwt.viz.graph.node.settings.appearance;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.CheckBox;
import com.github.gwtbootstrap.client.ui.RadioButton;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.*;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.*;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell;
import com.sencha.gxt.core.client.dom.XDOM;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.widget.core.client.ColorPalette;
import com.sencha.gxt.widget.core.client.form.ComboBox;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.icon.IconSelectionEvent;
import csi.client.gwt.icon.IconSelectionHandler;
import csi.client.gwt.icon.ui.IconManager;
import csi.client.gwt.util.FieldDefUtils;
import csi.client.gwt.util.FieldDefUtils.SortOrder;
import csi.client.gwt.viz.graph.node.settings.NodeSettings;
import csi.client.gwt.viz.graph.node.settings.NodeSettingsModel;
import csi.client.gwt.viz.graph.node.settings.ShowNodeSettings;
import csi.client.gwt.viz.graph.node.settings.SizingAttribute;
import csi.client.gwt.viz.graph.node.settings.tooltip.TooltipFunction;
import csi.client.gwt.widget.combo_boxes.FieldDefComboBox;
import csi.server.common.graphics.shapes.ShapeType;
import csi.server.common.model.FieldDef;
import csi.shared.core.color.ClientColorHelper;
import csi.shared.core.color.ClientColorHelper.Color;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class AppearanceTab {

    public static final String[] nodeColors = { "660000", "990000", "CC0000", "CC3333", "EA4C88", "D10553", "823CC8",
            "663399", "333399", "0066CC", "0099CC", "7AD9F9", "66CCCC", "74E618", "77CC33", "336600", "666600",
            "999900", "CCCC33", "EAEA26", "FFFF00", "FFCC33", "FF9900", "CE7C00", "FF6600", "CC6633", "996633",
            "AA6117", "663300", "000000", "999999", "CCCCCC" };
    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
    @UiField
    Button colorButton;
    @UiField(provided = true)
    ColorPalette colorPalette;
    @UiField
    TextBox colorTextBox;
    @UiField(provided = true)
    FieldDefComboBox fieldList;
    @UiField(provided = true)
    FieldDefComboBox typeFieldList;
    @UiField
    CheckBox hideScaleCheckBox;
    @UiField
    CheckBox hideShapeCheckBox;
    @UiField
    CheckBox colorCheckBox;
    @UiField
    CheckBox iconCheckBox;
    @UiField
    SimplePanel uploadContainer;
    @UiField
    VerticalPanel iconPanel;
    @UiField
    Image imagePreview;
    @UiField
    CheckBox labelCheckBox;
    @UiField
    RadioButton labelDynamic;
    @UiField
    RadioButton labelFixed;
    @UiField
    TextBox labelTextBox;
    @UiField
    InlineLabel typeCheckBox;
    @UiField
    RadioButton typeDynamic;
    @UiField
    RadioButton typeFixed;
    @UiField
    TextBox typeTextBox;
    @UiField
    TextBox nameTextBox;
    @UiField
    HTMLPanel previewImagePanel;
    @UiField
    SizingAttributeComboBox sizeByAttribute;
    @UiField
    RadioButton sizeDynamic;
    @UiField
    RadioButton sizeFixed;
    @UiField
    HTMLPanel shapePanel;
    @UiField
    HelpInline staticSizeHelp;
    @UiField
    TextBox staticSizeTextBox;
    @UiField
    RadioButton iconFixed;
    @UiField
    RadioButton iconDynamic;
    @UiField(provided = true)
    FieldDefComboBox iconFieldList;
    @UiField
    DisclosurePanel advancedDisclosurePanel;
    @UiField
    RadioButton sizeComputed;
    @UiField(provided = true)
    ComboBox<TooltipFunction> sizeFunctionComboBox;
    @UiField
    FieldDefComboBox sizeFunctionFdcb;
    @UiField
    RadioButton transparencyFixed;
    @UiField
    RadioButton transparencyDynamic;
    @UiField
    TextBox staticTransparencyTextBox;
    @UiField
    HelpInline staticTransparencyHelp;
    @UiField
    SizingAttributeComboBox transparencyByAttribute;
    @UiField(provided = true)
    ComboBox<TooltipFunction> transparencyFunctionComboBox;
    @UiField
    FieldDefComboBox transparencyFunctionFdcb;
    @UiField
    RadioButton transparencyComputed;
    @UiField
    CheckBox hideTransparencyCheckBox;
    @UiField
    RadioButton identityDynamic;
    @UiField
    RadioButton identityFixed;
    @UiField
    InlineLabel identityCheckBox;
    @UiField(provided = true)
    FieldDefComboBox identityFieldList;
    @UiField
    TextBox identityTextBox;
    private Image originalRenderedNode;
    private ShowNodeSettings presenter;
    private Image previewRenderedNode;
    private Tab tab;
    private Button iconDialogButton;

    public AppearanceTab(NodeSettings nodeSettings) {
        
        colorPalette = new ColorPalette(nodeColors, nodeColors);
        fieldList = new FieldDefComboBox();
        colorPalette.setVisible(false);
        fieldList.getStore().addAll(
                FieldDefUtils.getSortedNonStaticFields(nodeSettings.getGraphSettings().getDataViewDef().getModelDef(),
                        SortOrder.ALPHABETIC));
        fieldList.setSelectedIndex(0);
        fieldList.addSelectionHandler(new SelectionHandler<FieldDef>() {

            @Override
            public void onSelection(SelectionEvent<FieldDef> event) {
                presenter.setLabelField(event.getSelectedItem());
            }
        });

        typeFieldList = new FieldDefComboBox();
        typeFieldList.getStore().addAll(
                FieldDefUtils.getSortedNonStaticFields(nodeSettings.getGraphSettings().getDataViewDef().getModelDef(),
                        SortOrder.ALPHABETIC));
        typeFieldList.setSelectedIndex(0);
        typeFieldList.addSelectionHandler(new SelectionHandler<FieldDef>() {

            @Override
            public void onSelection(SelectionEvent<FieldDef> event) {
                presenter.setTypeField(event.getSelectedItem());
            }
        });

        identityFieldList = new FieldDefComboBox();
        identityFieldList.getStore().addAll(
                FieldDefUtils.getSortedNonStaticFields(nodeSettings.getGraphSettings().getDataViewDef().getModelDef(),
                        SortOrder.ALPHABETIC));
        identityFieldList.setSelectedIndex(0);
        identityFieldList.addSelectionHandler(new SelectionHandler<FieldDef>() {

            @Override
            public void onSelection(SelectionEvent<FieldDef> event) {
                presenter.setIdentityField(event.getSelectedItem());
            }
        });

        iconFieldList = new FieldDefComboBox();
        iconFieldList.getStore().addAll(
                FieldDefUtils.getAllSortedFields(nodeSettings.getGraphSettings().getDataViewDef().getModelDef(),
                        SortOrder.ALPHABETIC)
        );
        iconFieldList.setSelectedIndex(0);
        iconFieldList.addSelectionHandler(new SelectionHandler<FieldDef>() {

            @Override
            public void onSelection(SelectionEvent<FieldDef> event) {
                presenter.setIconField(event.getSelectedItem());
            }
        });

        //sizeFunction combobox need initialization
        sizeFunctionComboBox = new ComboBox<>(new ComboBoxCell<>(
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
        sizeFunctionComboBox.setAllowBlank(false);
        sizeFunctionComboBox.setTriggerAction(ComboBoxCell.TriggerAction.ALL);
        sizeFunctionComboBox.addStyleName("string-combo-style");
        sizeFunctionComboBox.setWidth(200);
        sizeFunctionComboBox.setEnabled(false);
        sizeFunctionComboBox.addSelectionHandler(new SelectionHandler<TooltipFunction>() {

            @Override
            public void onSelection(SelectionEvent<TooltipFunction> event) {
                presenter.setSizeFunction(event.getSelectedItem());//FIXME
            }
        });
        if(sizeFunctionComboBox.getValue() == null){

            NodeSettingsModel model = nodeSettings.getModel();
            TooltipFunction tooltipFunction;
            if(model.getSize() != null && model.getSize().getFunction() != null){
                tooltipFunction = model.getSize().getFunction();
            } else {
                tooltipFunction = sizeFunctionComboBox.getStore().get(0);
            }
            model.getSize().setFunction(tooltipFunction);
            sizeFunctionComboBox.setValue(tooltipFunction, true);
        }
        //transparencyFunction combobox need initialization
        transparencyFunctionComboBox = new ComboBox<>(new ComboBoxCell<>(
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
        transparencyFunctionComboBox.addStyleName("string-combo-style");
        transparencyFunctionComboBox.setWidth(200);
        transparencyFunctionComboBox.addSelectionHandler(new SelectionHandler<TooltipFunction>() {

            @Override
            public void onSelection(SelectionEvent<TooltipFunction> event) {
                presenter.setTransparencyFunction(event.getSelectedItem());
            }
        });

        tab = uiBinder.createAndBindUi(this);
        
        iconDialogButton = new Button();
        iconDialogButton.setEnabled(false);
        iconDialogButton.setText(CentrifugeConstantsLocator.get().iconPicker_browseButton());
        iconDialogButton.setHeight("22px");
        iconDialogButton.setWidth("56px");

        iconDialogButton.addClickHandler(new ClickHandler(){

            @Override
            public void onClick(ClickEvent event) {
                
                
                IconManager iconManager = new IconManager(new IconSelectionHandler(){

                    @Override
                    public void onSelect(IconSelectionEvent iconSelectionEvent) {
                        imagePreview.setUrl(iconSelectionEvent.getIconUrl());
                        presenter.setIcon(iconSelectionEvent.getIconUuid());
                    }});
                
                
                iconManager.show();
            }});
        
        uploadContainer.add(iconDialogButton);
        //Set copy of disclosure panel header
        advancedDisclosurePanel.getHeaderTextAccessor().setText(CentrifugeConstantsLocator.get().advanced());
        //initialize Listbox copy
//        sizeByAttribute.getStore().replaceAll(new ArrayList<String>(){{
//            add(CentrifugeConstantsLocator.get().numberOfNeighbors());
//            add(CentrifugeConstantsLocator.get().betweenness());
//            add(CentrifugeConstantsLocator.get().closeness());
//            add(CentrifugeConstantsLocator.get().eigenvector());
//            add(CentrifugeConstantsLocator.get().occurrence());
//            }});
        //Initizalize Fielddef combo box for size funtion
        sizeFunctionFdcb.getStore().addAll(
                FieldDefUtils.getSortedNonStaticFields(
                        nodeSettings.getGraphSettings().getDataViewDef().getModelDef(),
                        SortOrder.ALPHABETIC));
        
        sizeFunctionFdcb.addSelectionHandler(new SelectionHandler<FieldDef>() {

            @Override
            public void onSelection(SelectionEvent<FieldDef> event) {
                presenter.setSizeField(event.getSelectedItem());
                sizeFunctionComboBox.setEnabled(true);
            }
        });
        
        if(sizeFunctionFdcb.getValue() == null){

            NodeSettingsModel model = nodeSettings.getModel();
            FieldDef value;
            if(model.getSize() != null && model.getSize().getField() != null){
                value = model.getSize().getField();
            } else {
                value = sizeFunctionFdcb.getStore().get(0);
            }
            model.getSize().setField(value);
            sizeFunctionFdcb.setValue(value, true);
        }
        
              
        //initialize Listbox copy
        //transparencyByAttribute.getStore().clear();
//        transparencyByAttribute.getStore().addAll(new ArrayList<String>(){{
//            add(CentrifugeConstantsLocator.get().numberOfNeighbors());
//            add(CentrifugeConstantsLocator.get().betweenness());
//            add(CentrifugeConstantsLocator.get().closeness());
//            add(CentrifugeConstantsLocator.get().eigenvector());
//            add(CentrifugeConstantsLocator.get().occurrence());
//            
//        }});
        //Initizalize Fielddef combo box for transparency funtion
        transparencyFunctionFdcb.getStore().addAll(
                FieldDefUtils.getSortedNonStaticFields(
                        nodeSettings.getGraphSettings().getDataViewDef().getModelDef(),
                        SortOrder.ALPHABETIC));
        
        
        if(transparencyFunctionComboBox.getValue() == null){
            TooltipFunction value;
            NodeSettingsModel model = nodeSettings.getModel();
            if(model.getTransparency() != null && model.getTransparency().getFunction() != null){
                value = model.getTransparency().getFunction();
            } else {
                value = transparencyFunctionComboBox.getStore().get(0);
            }
            model.getTransparency().setFunction(value);
            transparencyFunctionComboBox.setValue(value, true);
        }
        
        transparencyFunctionFdcb.addSelectionHandler(new SelectionHandler<FieldDef>() {

            @Override
            public void onSelection(SelectionEvent<FieldDef> event) {
                presenter.setTransparencyField(event.getSelectedItem());
            }
        });
        

        if(transparencyFunctionFdcb.getValue() == null){
            
            NodeSettingsModel model = nodeSettings.getModel();
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

    public void setTypeText(String text) {
        typeTextBox.setValue(text);
    }

    public void setTypeField(FieldDef fieldDef) {
        typeFieldList.setValue(fieldDef);
    }

    public void setTypeFixed(boolean value) {
        typeFixed.setValue(value);
        typeDynamic.setValue(value == false);
        typeTextBox.setVisible(value);
        typeFieldList.setVisible(value == false);
    }

    public void setIconFixed(boolean fixed) {
        iconFixed.setValue(fixed);
        iconDynamic.setValue(!fixed);
        //iconCategoryListBox.setVisible(fixed);
        iconPanel.setVisible(fixed);
        iconFieldList.setVisible(!fixed);
    }

    public void setIconField(FieldDef fieldDef) {
        iconFieldList.setValue(fieldDef);
    }

    public void setSizeByComputed() {
        sizeFixed.setValue(false);
        sizeDynamic.setValue(false);
        sizeComputed.setValue(true);
        staticSizeTextBox.setVisible(false);
        staticSizeHelp.setVisible(false);
        sizeByAttribute.setVisible(false);
        sizeFunctionFdcb.setVisible(true);
        sizeFunctionComboBox.setVisible(true);
    }

    public void setSizeByMetric() {
        sizeFixed.setValue(false);
        sizeDynamic.setValue(true);
        sizeComputed.setValue(false);
        staticSizeTextBox.setVisible(false);
        staticSizeHelp.setVisible(false);
        sizeByAttribute.setVisible(true);
        sizeFunctionFdcb.setVisible(false);
        sizeFunctionComboBox.setVisible(false);
    }

    public void setTransparencyByComputed() {
        transparencyFixed.setValue(false);
        transparencyDynamic.setValue(false);
        transparencyComputed.setValue(true);
        staticTransparencyTextBox.setVisible(false);
        staticTransparencyHelp.setVisible(false);
        transparencyByAttribute.setVisible(false);
        transparencyFunctionFdcb.setVisible(true);
        transparencyFunctionComboBox.setVisible(true);
    }

    public void setTransparencyByMetric() {
        transparencyFixed.setValue(false);
        transparencyDynamic.setValue(true);
        transparencyComputed.setValue(false);
        staticTransparencyTextBox.setVisible(false);
        staticTransparencyHelp.setVisible(false);
        transparencyByAttribute.setVisible(true);
        transparencyFunctionFdcb.setVisible(false);
        transparencyFunctionComboBox.setVisible(false);
    }

    public void setTransparencyByFixed() {
        transparencyFixed.setValue(true);
        transparencyDynamic.setValue(false);
        transparencyComputed.setValue(false);
        staticTransparencyTextBox.setVisible(true);
        staticTransparencyHelp.setVisible(true);
        transparencyByAttribute.setVisible(false);
        transparencyFunctionFdcb.setVisible(false);
        transparencyFunctionComboBox.setVisible(false);
    }

    public void setSizeComputedFunction(final TooltipFunction function) {
        //FIXME:
        if (function != null) {
            Scheduler.get().scheduleFixedDelay(new Scheduler.RepeatingCommand() {
                @Override
                public boolean execute() {
                    sizeFunctionComboBox.setValue(function);
                    return false;
                }
            }, 500);
        }
    }

    public void setSizeComputedField(FieldDef field) {
        sizeFunctionFdcb.setValue(field);
    }

    public void setTransparencyField(SizingAttribute measure) {
        transparencyByAttribute.setValue(measure, true);

    }

    public void setTransparencyComputedField(FieldDef field) {
        transparencyFunctionFdcb.setValue(field);
    }

    public void setTransparencyComputedFunction(TooltipFunction function) {
        transparencyFunctionComboBox.setValue(function);

    }

    public void enableAllTransparencyOptions(boolean visible) {
        hideTransparencyCheckBox.setValue(visible);
        {//Enable radio options
            transparencyFixed.setEnabled(visible);
            transparencyDynamic.setEnabled(visible);
            transparencyComputed.setEnabled(visible);
        }
        {//Enable all controls
            staticTransparencyTextBox.setEnabled(visible);
            transparencyFunctionFdcb.setEnabled(visible);
            transparencyFunctionComboBox.setEnabled(visible);
        }
    }

    public void setTransparencyValue(double value) {
        staticTransparencyTextBox.setValue(value + "");
    }

    public void setIdentityField(FieldDef fieldDef) {
        identityFieldList.setValue(fieldDef);
    }

    public void setIdentityText(String text) {
        identityTextBox.setValue(text);
    }

    @UiHandler("labelCheckBox")
    public void changeHideLabels(ValueChangeEvent<Boolean> event) {
        presenter.updateLabelSettings(event.getValue());
    }

    @UiHandler("hideShapeCheckBox")
    public void changeHideShape(ValueChangeEvent<Boolean> event) {
        presenter.updateShapeSettings(event.getValue(), true);
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

    public void enableAllLabelSettings(boolean value) {
        labelFixed.setEnabled(value);
        labelDynamic.setEnabled(value);
        labelTextBox.setEnabled(value);
        fieldList.setEnabled(value);
    }

    public void enableAllScaleOptions(boolean value) {
        hideScaleCheckBox.setValue(value);
        sizeDynamic.setEnabled(value);
        sizeFixed.setEnabled(value);
        sizeComputed.setEnabled(value);
        if (sizeDynamic.getValue() || value) {
            sizeByAttribute.setEnabled(value);
        }
        if (sizeFixed.getValue() || value) {
            staticSizeTextBox.setEnabled(value);
            staticSizeHelp.setVisible(value);
        }
        if (sizeComputed.getValue() || value) {
            sizeFunctionFdcb.setEnabled(value);
            if(sizeFunctionFdcb.getValue() != null)
                sizeFunctionComboBox.setEnabled(value);
            else
                sizeFunctionComboBox.setEnabled(false);
            
        }
    }

    public void enableAllShapeOptions(boolean value) {
        enableShapePicker(value);
    }

    public void enableColorPicker(boolean value) {
        colorButton.setEnabled(value);
        colorTextBox.setEnabled(value);
    }

    public void enableHideIconCheckBox(boolean b) { iconCheckBox.setEnabled(b); }
    public void enableColorCheckBox(boolean b) {
        colorCheckBox.setEnabled(b);
    }
    
    public void enableHideShapeCheckBox(boolean b) {
        hideShapeCheckBox.setEnabled(b);
    }
    
    public void setShapeCheckBox(boolean b){
        hideShapeCheckBox.setValue(b);
    }

    public void enableIconCategoryListBox(boolean value) {
        //iconCategoryListBox.setEnabled(value);
    }

    public void enableIconListBox(boolean value) {
        enableIconPanel(value);
    }

    public void enableLabelSettings(boolean hideLabels) {
        labelCheckBox.setValue(hideLabels);
    }

    public void enableShapePicker(boolean value) {
        if (value) {
            shapePanel.removeStyleName("overlay");
            shapePanel.getElement().getStyle().setProperty("opacity", "1");
        } else {
            shapePanel.addStyleName("overlay");
            shapePanel.getElement().getStyle().setProperty("opacity", ".5");
        }
    }

    public Tab getTab() {
        return tab;
    }

//    @UiHandler("iconCategoryListBox")
//    void iconCategoryChange(SelectionEvent<String> event) {
//        presenter.setIconCategory(event.getSelectedItem());
//    }
    
    @UiHandler("colorTextBox")
    void onColorTextInput(ValueChangeEvent<String> event) {
        checkNotNull(event);
        String value = checkNotNull(event.getValue());
        if (value.charAt(0) == '#') {
            value = value.substring(1);
        }
        presenter.setColor(ClientColorHelper.get().makeFromHex(value));
        colorPalette.setVisible(false);
    }

    @UiHandler({ "iconDynamic", "iconFixed" })
    void onIconDynamic(ClickEvent event) {
        presenter.setIconFixed(iconFixed.getValue());
        if(!iconFixed.getValue())
            presenter.setIconField(iconFieldList.getValue());
    }

//    @UiHandler("imagePreview")
//    void onIconChange(SelectionEvent<String> event) {
//        presenter.setIcon(event.getSelectedItem());
//    }

    @UiHandler("labelTextBox")
    void onLabelTextBoxChange(ValueChangeEvent<String> event) {
        presenter.setLabelText(event.getValue());
    }

    @UiHandler("typeTextBox")
    void onTypeTextBoxChange(ValueChangeEvent<String> event) {
        presenter.setTypeText(event.getValue());
    }

    @UiHandler("nameTextBox")
    public void onNameChange(ValueChangeEvent<String> event) {
        presenter.updateName(event.getValue());
    }

    @UiHandler("sizeByAttribute")
    void onScaleByAttributeChange(SelectionEvent<SizingAttribute> event) {
        presenter.setScaleMeasure(event.getSelectedItem());
    }

    @UiHandler("transparencyByAttribute")
    void onTransparencyByAttributeChange(SelectionEvent<SizingAttribute> event) {
        presenter.setTransparencyMeasure(event.getSelectedItem());
    }

    @UiHandler("colorPalette")
    public void onSelection(SelectionEvent<String> event) {
        presenter.setColor(ClientColorHelper.get().makeFromHex(event.getSelectedItem()));
        colorPalette.setVisible(false);
    }

    @UiHandler("staticSizeTextBox")
    void onStaticSizeTextBoxChange(ValueChangeEvent<String> event) {
        checkNotNull(event);
        double newValue = 1D;
        try {
            newValue = Double.parseDouble(event.getValue());
        } catch (Exception e) {
        }
        presenter.setStaticScaleValue(newValue);
    }

    @UiHandler("staticTransparencyTextBox")
    void onStaticTransparencyTextBoxChange(ValueChangeEvent<String> event) {
        checkNotNull(event);
        double newValue = 100D;
        try {
            newValue = Double.parseDouble(staticTransparencyTextBox.getValue());
        } catch (Exception e) {
        }
        presenter.setStaticTransparencyValue(newValue);
    }

    public void scaleValue(double scaleValue) {
        staticSizeTextBox.setValue(scaleValue + "");
    }

    @UiHandler("labelDynamic")
    public void selectLabelDynamic(ClickEvent event) {
        presenter.setLabelFixed(labelFixed.getValue());
        presenter.setLabelField(fieldList.getValue());
    }

    @UiHandler("labelFixed")
    public void selectLabelFixed(ClickEvent event) {
        presenter.setLabelFixed(labelFixed.getValue());
    }

    @UiHandler("typeDynamic")
    public void selectTypeDynamic(ClickEvent event) {
        presenter.setTypeFixed(typeFixed.getValue());
        presenter.setTypeField(typeFieldList.getValue());
    }

    @UiHandler("identityDynamic")
    public void selectIdentityDynamic(ClickEvent event) {
        presenter.setIdentityFixed(identityFixed.getValue());
        presenter.setIdentityField(identityFieldList.getValue());
    }

    @UiHandler("typeFixed")
    public void selectTypeFixed(ClickEvent event) {
        presenter.setTypeFixed(typeFixed.getValue());
    }

    @UiHandler("identityFixed")
    public void selectIdentityFixed(ClickEvent event) {
        presenter.setIdentityFixed(identityFixed.getValue());
    }

    @UiHandler("sizeDynamic")
    void selectSizeDynamic(ClickEvent event) {
        if (sizeDynamic.getValue()) {
            presenter.setSizeByDynamic();
        }
    }

    @UiHandler("sizeFixed")
    void selectSizeFixed(ClickEvent event) {
        if (sizeFixed.getValue()) {
            presenter.setSizeByFixed();
        }
    }

    @UiHandler("sizeComputed")
    void selectSizeComputed(ClickEvent event) {
        if (sizeComputed.getValue()) {
            presenter.setSizeByComputed();
        }
    }

    @UiHandler("transparencyDynamic")
    void selectTransparencyDynamic(ClickEvent event) {
        if (transparencyDynamic.getValue()) {
            presenter.setTransparencyByDynamic();
        }
    }

    @UiHandler("transparencyFixed")
    void selectTransparencyFixed(ClickEvent event) {
        if (transparencyFixed.getValue()) {
            presenter.setTransparencyByFixed();
        }
    }

    @UiHandler("transparencyComputed")
    void selectTransparencyComputed(ClickEvent event) {
        if (transparencyComputed.getValue()) {
            presenter.setTransparencyByComputed();
        }
    }

    public void setColor(Color color) {
        colorPalette.setValue(color.toString().substring(1).toUpperCase());
        colorTextBox.setValue(color.toString().toUpperCase());
        colorButton.getElement().getStyle().setColor(color.toString());
    }

    public void setIcon(String url) {
        if(url == null){
            url = "";
        }
        imagePreview.setUrl(url);
    }

    public void setIconCheckBox(boolean b) {
        iconCheckBox.setValue(b);
        enableIconCategoryListBox(b);
        enableIconListBox(b);
        iconFieldList.setEnabled(b);
        iconFixed.setEnabled(b);
        iconDynamic.setEnabled(b);
    }

    public void setColorCheckBox(boolean b){
        colorCheckBox.setValue(b);
        enableColorPicker(b);
    }

    public void setLabelField(FieldDef fieldDef) {
        fieldList.setValue(fieldDef);
    }

    public void setlabelFixed(Boolean value) {
        labelFixed.setValue(value);
        labelDynamic.setValue(value == false);
        labelTextBox.setVisible(value);
        fieldList.setVisible(value == false);
    }

    // Presenter sets static label text
    // Presenter could setLable(NodeLabel nl)
    public void setLabelText(String value) {
        labelTextBox.setValue(value);
    }

    // Presenter sets name
    public void setName(String name) {
        nameTextBox.setText(name);
    }

    // Add a print line
    public void setOriginalRenderedNode(final Image image) {
        originalRenderedNode = new Image();
        originalRenderedNode.addStyleName("node-edit-rendered-node");
        previewImagePanel.add(originalRenderedNode);
        image.addLoadHandler(new LoadHandler() {

            @Override
            public void onLoad(LoadEvent event) {
                originalRenderedNode.setUrl(image.getUrl());
            }
        });
    }

    // Bind to presenter
    public void setPresenter(ShowNodeSettings presenter) {
        this.presenter = presenter;
    }

    // Presenter sets preview of current Node appearance.
    public void setPreviewRenderedNode(final Image image) {
        checkNotNull(image);
        image.addStyleName("node-edit-rendered-node");
        image.addLoadHandler(new LoadHandler() {

            @Override
            public void onLoad(LoadEvent event) {
                // Remove the old preview if there is one.
                if (previewRenderedNode != null) {
                    previewRenderedNode.removeFromParent();
                }
                previewRenderedNode = image;
                previewImagePanel.add(image);
            }
        });
        if (originalRenderedNode == null) {
            setOriginalRenderedNode(image);
        }
        RootPanel.get().add(image);
    }

    // Presenter sets the field to size by.
    public void setSizeField(FieldDef sizeByFieldName) {
        sizeFunctionFdcb.setValue(sizeByFieldName);
    }

    public void setSizeMetric(SizingAttribute sizingAttribute) {
        sizeByAttribute.setValue(sizingAttribute, true);
    }

    // Presenter tells the UI if the size is a fixed value.
    public void setSizeFixed() {
        sizeFixed.setValue(true);
        sizeDynamic.setValue(false);
        staticSizeTextBox.setVisible(true);
        staticSizeHelp.setVisible(true);
        sizeByAttribute.setVisible(false);
        sizeFunctionFdcb.setVisible(false);
        sizeFunctionComboBox.setVisible(false);
    }

    // Presenter tells UI the node's shape
    public void setShape(ShapeType shape) {
        //hideShapeCheckBox.setValue(shape);
        // Noop, this implementation of the UI does not reflect the current shape.
    }

    // Presenter tell UI which shapes are available to the user.
    public void setShapes(List<ShapeType> shapes) {
        // TODO: not safe to call twice. need to clear shapePanel?
        for (final ShapeType shape : shapes) {
            ImageResource imageResource = NodeShape.imageFromShapeType(shape);
            if (imageResource != null) {
                Image image = new Image(imageResource);
                image.addClickHandler(new ClickHandler() {

                    @Override
                    public void onClick(ClickEvent event) {
                        presenter.setShape(shape);
                    }
                });
                shapePanel.add(image);
                image.setVisibleRect(-4, 0, 16, 16);
                image.getElement().getStyle().setMarginBottom(2, Unit.PX);
            }
        }
    }

    // User indicates if node should have an icon.
    @UiHandler("iconCheckBox")
    public void toggleIconSettings(ValueChangeEvent<Boolean> event) {
        enableIconPanel(event.getValue());
        if (event.getValue()) {
            iconDialogButton.setEnabled(true);
        } else {
            iconDialogButton.setEnabled(false);
        }
        presenter.updateIconSettings(event.getValue(), true);
    }
    
    @UiHandler("colorCheckBox")
    public void toggleColorSettings(ValueChangeEvent<Boolean> event) {
        enableColorPicker(event.getValue());
        presenter.updateColorSettings(event.getValue(), true);
    }

    public void enableIconPanel(Boolean enable) {
        if(enable){
            imagePreview.getElement().getStyle().setBackgroundColor("#efefef");
            imagePreview.getElement().getStyle().setOpacity(1);
        } else {
            imagePreview.getElement().getStyle().setBackgroundColor("#cccccc");
            imagePreview.getElement().getStyle().setOpacity(.5);
        }
        //presenter.updateIconSettings(enable);
    }

    // User indicates if node should have fixed scale with size of 1
    @UiHandler("hideScaleCheckBox")
    public void toggleSizeSettings(ValueChangeEvent<Boolean> event) {
        presenter.updateSizeSettings(event.getValue());
    }

    @UiHandler("hideTransparencyCheckBox")
    public void toggleTransparencySettings(ValueChangeEvent<Boolean> event) {
        presenter.updateTransparencySettings(event.getValue());
    }

    @UiHandler("identityTextBox")
    void onIdentityTextBoxChange(ValueChangeEvent<String> event) {
        presenter.setIdentityText(event.getValue());
    }

    public void setIdentityFixed(boolean value) {
        identityFixed.setValue(value);
        identityDynamic.setValue(value == false);
        identityTextBox.setVisible(value);
        identityFieldList.setVisible(value == false);
    }
    
    @UiHandler("clearImage")
    public void clearImage(ClickEvent e){
        presenter.setIcon("");
        imagePreview.setUrl("");
        enableImageBox(true);
    }

    public void enableImageBox(boolean b) {
        enableIconCategoryListBox(b);
        enableIconListBox(b);
        if(iconCheckBox.getValue()) {
            iconDialogButton.setEnabled(b);
        }
    }

    interface MyUiBinder extends UiBinder<Tab, AppearanceTab> {
    }

}
