package csi.client.gwt.viz.map.place.settings;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.emitrom.lienzo.client.core.util.Console;
import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.CheckBox;
import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.HelpInline;
import com.github.gwtbootstrap.client.ui.RadioButton;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.widget.core.client.ColorPalette;
import com.sencha.gxt.widget.core.client.form.ComboBox;

import csi.client.gwt.WebMain;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.icon.IconSelectionEvent;
import csi.client.gwt.icon.IconSelectionHandler;
import csi.client.gwt.icon.ui.IconManager;
import csi.client.gwt.validation.feedback.StringValidationFeedback;
import csi.client.gwt.validation.feedback.ValidationFeedback;
import csi.client.gwt.validation.multi.MultiValidatorCollectingErrors;
import csi.client.gwt.validation.multi.ValidationAndFeedbackPair;
import csi.client.gwt.validation.validator.Validator;
import csi.client.gwt.viz.graph.node.settings.appearance.NodeShape;
import csi.client.gwt.viz.graph.node.settings.tooltip.TooltipFunction;
import csi.client.gwt.viz.graph.shared.AbstractGraphObjectScale.ScaleMode;
import csi.client.gwt.widget.combo_boxes.FieldDefComboBox;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.graphics.shapes.ShapeType;
import csi.server.common.model.FieldDef;
import csi.server.common.model.themes.map.PlaceStyle;
import csi.server.common.model.visualization.map.MapAssociation;
import csi.server.common.model.visualization.map.MapPlace;
import csi.server.common.model.visualization.map.MapTrack;
import csi.server.common.service.api.GraphActionServiceProtocol;
import csi.server.common.service.api.IconActionsServiceProtocol;
import csi.shared.core.color.ClientColorHelper;
import csi.shared.core.color.ClientColorHelper.Color;

public class AppearanceTab extends Composite {
    private static final String[] nodeColors = {"660000", "990000", "CC0000", "CC3333", "EA4C88", "D10553", "823CC8",
            "663399", "333399", "0066CC", "0099CC", "7AD9F9", "66CCCC", "74E618", "77CC33", "336600", "666600",
            "999900", "CCCC33", "EAEA26", "FFFF00", "FFCC33", "FF9900", "CE7C00", "FF6600", "CC6633", "996633",
            "AA6117", "663300", "000000", "999999", "CCCCCC"};

    private static AppearanceTabUiBinder uiBinder = GWT.create(AppearanceTabUiBinder.class);

    interface AppearanceTabUiBinder extends UiBinder<Widget, AppearanceTab> {
    }

    private static String getRandomColor() {
        float satuartion = (float) ((Random.nextDouble() / 5F) + .7F);
        float value = (float) ((Random.nextDouble() / 4F) + .75F);
        return Integer.toHexString(ClientColorHelper.get().randomHueWheel(satuartion, value).getIntColor());
    }

    private CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    @UiField
    TextBox placeNameTextBox;
    @UiField
    FieldDefComboBox latFieldComboBox;
    @UiField
    FieldDefComboBox longFieldComboBox;
    @UiField
    Button clearLat;
    @UiField
    Button clearLong;

    @UiHandler("placeNameTextBox")
    public void handlePlaceNameTextBoxChange(ChangeEvent e) {
        presenter.setCurrentPlaceName(placeNameTextBox.getValue());
        getRenderedNode();
    }

    @UiHandler("clearLat")
    public void handleLatClear(ClickEvent e) {
        latFieldComboBox.setValue(null);
    }

    @UiHandler("clearLong")
    public void handleLongClear(ClickEvent e) {
        longFieldComboBox.setValue(null);
    }

    @UiField
    FieldDefComboBox labelFieldComboBox;
    @UiField
    csi.client.gwt.widget.buttons.Button clearLabel;

    @UiHandler("clearLabel")
    public void handleLabelClear(ClickEvent e) {
        labelFieldComboBox.setValue(null);
    }

    @UiField
    RadioButton sizeFixed;
    @UiField
    RadioButton sizeComputed;
    @UiField
    TextBox staticSizeTextBox;
    @UiField
    HelpInline staticSizeHelp;
    @UiField
    FieldDefComboBox sizeFunctionFdcb;
    @UiField(provided = true)
    ComboBox<TooltipFunction> sizeFunctionComboBox;
    @UiField
    CheckBox useDynamicTypeCheckBox;

    @UiHandler("sizeFixed")
    void selectSizeFixed(ClickEvent event) {
        if (sizeFixed.getValue()) {
            selectSizeFixed();
        }
    }

    private void selectSizeFixed() {
        placeScaleMode = ScaleMode.FIXED;
        sizeFixed.setValue(true);
        sizeComputed.setValue(false);
        staticSizeTextBox.setVisible(true);
        staticSizeHelp.setVisible(true);
        sizeFunctionFdcb.setVisible(false);
        sizeFunctionComboBox.setVisible(false);
        useDynamicTypeCheckBox.setVisible(false);
    }

    @UiHandler("sizeComputed")
    void selectSizeComputed(ClickEvent event) {
        if (sizeComputed.getValue()) {
            selectSizeComputed();
        }
    }

    private void selectSizeComputed() {
        placeScaleMode = ScaleMode.COMPUTED;
        sizeFixed.setValue(false);
        sizeComputed.setValue(true);
        staticSizeTextBox.setVisible(false);
        staticSizeHelp.setVisible(false);
        sizeFunctionFdcb.setVisible(true);
        sizeFunctionComboBox.setVisible(true);
        useDynamicTypeCheckBox.setVisible(!typeFixed.getValue());
    }

    @UiField
    ControlGroup typeControlGroup;
    @UiField
    RadioButton typeFixed;
    @UiField
    RadioButton typeDynamic;
    @UiField(provided = true)
    FieldDefComboBox typeFieldList;
    @UiField
    CheckBox includeNullTypeCheckBox;
    @UiField
    TextBox typeTextBox;

    @UiHandler({"typeFixed", "typeDynamic"})
    void onTypeDynamic(ClickEvent event) {
        setTypeFixed(typeFixed.getValue());
    }

    public void setTypeFixed(boolean fixed) {
        typeFixed.setValue(fixed);
        typeDynamic.setValue(!fixed);
        typeFieldList.setVisible(!fixed);
        includeNullTypeCheckBox.setVisible(!fixed);
        typeTextBox.setVisible(fixed);
        if (fixed || sizeFixed.getValue()) {
            useDynamicTypeCheckBox.setVisible(false);
        } else {
            useDynamicTypeCheckBox.setVisible(true);
        }
    }

    @UiHandler("typeTextBox")
    public void onTypeTextBoxChange(ChangeEvent event) {
        String typeName = typeTextBox.getValue().trim();
        typeUsePlacename = typeName.length() == 0;
        if (typeName.length()>250) {
            typeName = typeName.substring(0, 250);
        }
        typeTextBox.setValue(typeName);
        getRenderedNode();
    }

    @UiField
    CheckBox iconCheckBox;
    @UiField
    RadioButton iconFixed;
    @UiField
    RadioButton iconDynamic;
    @UiField
    SimplePanel imagePreviewContainer;
    @UiField
    Image imagePreview;
    @UiField
    SimplePanel uploadContainer;
    @UiField(provided = true)
    FieldDefComboBox iconFieldList;

    @UiHandler({"iconCheckBox"})
    void onIconCheckBox(ClickEvent event) {
        enableIconControls(!iconCheckBox.getValue());
        getRenderedNode();
    }

    private void enableIconControls(boolean useDefault) {
        iconFixed.setEnabled(!useDefault);
        iconDynamic.setEnabled(!useDefault);
        iconDialogButton.setEnabled(!useDefault);
        iconFieldList.setEnabled(!useDefault);
        updateIconControls();
    }

    private void updateIconControls() {
        MapPlace mapPlace = presenter.getMapPlace();
        useOverrideIconSettings(mapPlace);
    }

    private void useOverrideIconSettings(MapPlace mapPlace) {
        if (iconId == null) {
            iconId = mapPlace.getIconId();
            boolean iconFixed = mapPlace.isIconFixed();
            setIconFixed(iconFixed);
            if (iconFixed && iconId != null) {
                try {
                    WebMain.injector.getVortex().execute((String result) -> {
                        if ((null != result) && (0 < result.length())) {
                            final Image image = new Image();
                            image.addStyleName("node-edit-rendered-node");
                            image.setUrl(result);
                            image.addLoadHandler(event -> {
                                imagePreview.setUrl(image.getUrl());
                            });
                            RootPanel.get().add(image);
                        }
                    }, IconActionsServiceProtocol.class).getDataUrlImage(iconId);
                } catch (CentrifugeException e) {
                }
            }
        }
    }

    public void getRenderedNode() {
        try {
            PlaceStyle placeStyle = presenter.getPlaceStyle(placeNameTextBox.getText());
            String newIconId = iconId;
            ShapeType newShapeType = shapeType;
            Color newColor = color;
            if (placeStyle != null) {
                if (!iconCheckBox.getValue()) {
                    String styleIconId = placeStyle.getIconId();
                    if (styleIconId != null)
                        newIconId = styleIconId;
                    else
                        newIconId = null;
                }
                if (!shapeCheckBox.getValue())
                    if (placeStyle.getShape() != null)
                        newShapeType = placeStyle.getShape();
                    else {
                        ShapeType defaultShapeType = presenter.getDefaultShape();
                        if (defaultShapeType != null)
                            newShapeType = defaultShapeType;
                    }
                if (!colorCheckBox.getValue())
                    newColor = ClientColorHelper.get().make(placeStyle.getColor());
            } else {
                if (!iconCheckBox.getValue())
                    newIconId = null;
                if (!shapeCheckBox.getValue()) {
                    ShapeType defaultShapeType = presenter.getDefaultShape();
                    if (defaultShapeType != null)
                        newShapeType = defaultShapeType;
                }
            }
            WebMain.injector.getVortex().execute((String result) -> {
                final Image image = new Image();
                image.addStyleName("node-edit-rendered-node");
                image.setUrl(result);
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
            }, GraphActionServiceProtocol.class).getNodeAsImageNew(newIconId, newShapeType, newColor.getIntColor(), 50,
                    1.0);
        } catch (CentrifugeException e) {
        }
    }

    private void setOriginalRenderedNode(final Image image) {
        originalRenderedNode = new Image();
        originalRenderedNode.addStyleName("node-edit-rendered-node");
        previewImagePanel.add(originalRenderedNode);
        image.addLoadHandler(event -> {
            originalRenderedNode.setUrl(image.getUrl());
        });
    }

    @UiHandler({"iconDynamic", "iconFixed"})
    void onIconDynamic(ClickEvent event) {
        setIconFixed(iconFixed.getValue());
    }

    private void setIconFixed(boolean fixed) {
        iconFixed.setValue(fixed);
        imagePreviewContainer.setVisible(fixed);
        uploadContainer.setVisible(fixed);
        iconDynamic.setValue(!fixed);
        iconFieldList.setVisible(!fixed);
    }

    @UiField
    CheckBox shapeCheckBox;

    @UiHandler({"shapeCheckBox"})
    void onShapeCheckBox(ClickEvent event) {
        enableShapeControls(!shapeCheckBox.getValue());
        getRenderedNode();
    }

    private void enableShapeControls(boolean useDefault) {
        enableShapePicker(!useDefault);
        updateShapeControls();
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

    private void updateShapeControls() {
        MapPlace mapPlace = presenter.getMapPlace();
        useOverrideShapeSettings(mapPlace);
    }

    private void useOverrideShapeSettings(MapPlace mapPlace) {
        if (shapeType == null) {
            String shapeTypeString = mapPlace.getShapeTypeString();
            if (shapeTypeString == null || shapeTypeString.equals("")) {
                shapeType = presenter.getNextNodeShape();
                mapPlace.setShapeTypeString(shapeType.toString());
            } else {
                shapeType = ShapeType.getShape(shapeTypeString);
            }
        }
    }

    @UiField
    CheckBox colorCheckBox;

    @UiHandler({"colorCheckBox"})
    void onColorCheckBox(ClickEvent event) {
        enableColorControls(!colorCheckBox.getValue());
        getRenderedNode();
    }

    private void enableColorControls(boolean useDefault) {
        enableColorPicker(!useDefault);
        updateColorControls();
    }

    public void enableColorPicker(boolean value) {
        colorButton.setEnabled(value);
        colorTextBox.setEnabled(value);
    }

    private void updateColorControls() {
        MapPlace mapPlace = presenter.getMapPlace();
        useOverrideColorSettings(mapPlace);
        setColor(color);
    }

    private void useOverrideColorSettings(MapPlace mapPlace) {
        if (color == null) {
            String colorString = mapPlace.getColorString();
            if (colorString == null || colorString.equals("")) {
                colorString = getRandomColor();
                mapPlace.setColorString(colorString);
            }
            if (colorString.charAt(0) == '#')
                colorString = colorString.substring(1);
            color = ClientColorHelper.get().makeFromHex(colorString);
        }
    }

    @UiField
    HTMLPanel shapePanel;

    @UiField
    Button colorButton;
    @UiField
    TextBox colorTextBox;
    @UiField(provided = true)
    ColorPalette colorPalette;

    @UiHandler("colorButton")
    void colorButtonClickHandler(ClickEvent event) {
        if (colorPalette.isVisible()) {
            colorPalette.setVisible(false);
        } else {
            colorPalette.setVisible(true);
            colorPalette.focus();
        }
    }

    @UiHandler("colorTextBox")
    void onColorTextInput(ValueChangeEvent<String> event) {
        checkNotNull(event);
        String value = checkNotNull(event.getValue());
        if (value.charAt(0) == '#') {
            value = value.substring(1);
        }
        try {
            Color testColor = ClientColorHelper.get().makeFromHex(value);
            if (testColor != null) {
                color = testColor;
                setColor(color);
                colorPalette.setVisible(false);
                getRenderedNode();
            } else {
                setColor(color);
            }
        } catch (NumberFormatException nfe) {
            setColor(color);
        }
    }

    public void setColor(Color color) {
        colorPalette.setValue(color.toString().substring(1).toUpperCase());
        colorTextBox.setValue(color.toString().toUpperCase());
        colorButton.getElement().getStyle().setColor(color.toString());
    }

    @UiHandler("colorPalette")
    public void onSelection(SelectionEvent<String> event) {
        String colorString = event.getSelectedItem();
        color = ClientColorHelper.get().makeFromHex(colorString);
        setColor(color);
        colorPalette.setVisible(false);
        getRenderedNode();
    }

    @UiField
    HTMLPanel previewImagePanel;

    private FieldDef sizeField;

    private boolean typeUsePlacename = true;
    private FieldDef typeField;

    private String iconId;
    private FieldDef iconField;
    private Button iconDialogButton;

    private ShapeType shapeType;
    private Color color;

    private Image originalRenderedNode;
    private Image previewRenderedNode;

    private ScaleMode placeScaleMode;
    private TooltipFunction tooltipFunction;

    public AppearanceTab() {
        typeFieldList = new FieldDefComboBox();
        typeFieldList.addSelectionHandler(event -> {
            typeField = event.getSelectedItem();
            typeFieldList.setEnabled(true);
        });
        if (typeFieldList.getValue() == null) {
            FieldDef value;
            if (typeField != null) {
                value = typeField;
            } else {
                value = typeFieldList.getStore().get(0);
            }
            typeField = value;
            typeFieldList.setValue(value, true);
        }
        iconFieldList = new FieldDefComboBox();
        iconFieldList.addSelectionHandler(event -> {
            iconField = event.getSelectedItem();
            iconFieldList.setEnabled(true);
        });

        if (iconFieldList.getValue() == null) {
            FieldDef value;
            if (iconField != null) {
                value = iconField;
            } else {
                value = iconFieldList.getStore().get(0);
            }
            iconField = value;
            iconFieldList.setValue(value, true);
        }

        sizeFunctionComboBox = new ComboBox<>(
                new ComboBoxCell<>(new ListStore<>(new ModelKeyProvider<TooltipFunction>() {
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
        sizeFunctionComboBox.setEditable(false);
        sizeFunctionComboBox.setTriggerAction(ComboBoxCell.TriggerAction.ALL);
        sizeFunctionComboBox.addStyleName("string-combo-style");
        sizeFunctionComboBox.setWidth(200);
        sizeFunctionComboBox.setEnabled(false);
        colorPalette = new ColorPalette(nodeColors, nodeColors);
        colorPalette.setVisible(false);
        initWidget(uiBinder.createAndBindUi(this));
        updateShapeList();
        sizeFunctionFdcb.addSelectionHandler(event -> {
            sizeField = event.getSelectedItem();
            sizeFunctionComboBox.setEnabled(true);
        });
        if (sizeFunctionFdcb.getValue() == null) {
            FieldDef value;
            if (sizeField != null) {
                value = sizeField;
                sizeFunctionComboBox.setEnabled(true);
            } else {
                value = sizeFunctionFdcb.getStore().get(0);
            }
            sizeField = value;
            sizeFunctionFdcb.setValue(value, true);
        }

        uploadContainer.setHeight("24px");
        uploadContainer.setWidth("56px");
        uploadContainer.getElement().getStyle().setDisplay(Display.BLOCK);

        iconDialogButton = new Button();
        iconDialogButton.setText(_constants.iconPicker_browseButton());
        iconDialogButton.setHeight("22px");
        iconDialogButton.setWidth("56px");

        iconDialogButton.addClickHandler(event -> {
            IconManager iconManager = new IconManager(new IconSelectionHandler() {
                @Override
                public void onSelect(IconSelectionEvent iconSelectionEvent) {
                    imagePreview.setUrl(iconSelectionEvent.getIconUrl());
                    iconId = iconSelectionEvent.getIconUuid();
                    getRenderedNode();
                }
            });

            iconManager.show();
        });

        uploadContainer.add(iconDialogButton);

        imagePreview.getElement().getStyle().setOpacity(1.0D);
    }

    private void updateShapeList() {
        List<ShapeType> shapes = new ArrayList<ShapeType>(Arrays.asList(ShapeType.nodeShapeWheel));
        if ((shapes != null) && (shapes.size() > 0)) {
            if ((shapes.size() > 1) && (shapes.indexOf(ShapeType.NONE) < 0)) {
                shapes.add(ShapeType.NONE);
            }
            setShapes(shapes);
        }
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
                        shapeType = shape;
                        getRenderedNode();
                    }
                });
                shapePanel.add(image);
                image.setVisibleRect(-4, 0, 16, 16);
                image.getElement().getStyle().setMarginBottom(2, Unit.PX);
            }
        }
    }

    private MapPlaceSettingsPresenter presenter;

    public void setPresenter(MapPlaceSettingsPresenter presenter) {
        this.presenter = presenter;
        populate();
        hookupListener();
    }

    private void populate() {
        List<FieldDef> fieldDefs = presenter.getFieldDefs();
        for (FieldDef fieldDef : fieldDefs) {
            CsiDataType type = fieldDef.getValueType();
            if (type == CsiDataType.Number) {
                latFieldComboBox.getStore().add(fieldDef);
                longFieldComboBox.getStore().add(fieldDef);
            }
        }
        labelFieldComboBox.getStore().addAll(fieldDefs);
        sizeFunctionFdcb.getStore().addAll(fieldDefs);
        typeFieldList.getStore().addAll(fieldDefs);
        iconFieldList.getStore().addAll(fieldDefs);
    }

    private void hookupListener() {
        presenter.registerCurrentNameListener((String currentPlaceName) -> {
            String typeName;
            if (typeUsePlacename) {
                typeName = currentPlaceName;
            } else {
                typeName = presenter.getMapPlace().getTypeName();
                if (typeName == null || typeName.trim().length() == 0) {

                    typeName = currentPlaceName;
                }
            }
            typeName = typeName.trim();
            if (typeName.length()>250) {
                typeName = typeName.substring(0, 250);
            }
            placeNameTextBox.setValue(typeName);
            typeTextBox.setValue(typeName);

            getRenderedNode();
        });
    }

    protected void clearSelection() {
        placeNameTextBox.setText("");
        latFieldComboBox.clear();
        longFieldComboBox.clear();
        labelFieldComboBox.clear();
        sizeFunctionFdcb.clear();
        typeFieldList.clear();
        iconFieldList.clear();
        iconId = null;
        imagePreview.setUrl("");
        shapeType = null;
        color = null;
    }

    protected void updateMapPlace() {
        MapPlace mapPlace = presenter.getMapPlace();
        String oldName = mapPlace.getName();
        String newName = placeNameTextBox.getText();
        for (MapAssociation mapAssociation : presenter.getMapSettings().getMapAssociations()) {
            if (mapAssociation.getSource().equals(oldName))
                mapAssociation.setSource(newName);
            if (mapAssociation.getDestination().equals(oldName))
                mapAssociation.setDestination(newName);
        }
        for (MapTrack mapTrack : presenter.getMapSettings().getMapTracks()) {
            if (mapTrack.getPlace().equals(oldName))
                mapTrack.setPlace(newName);
        }
        mapPlace.setName(newName);
        mapPlace.setLatField(latFieldComboBox.getCurrentValue());
        mapPlace.setLongField(longFieldComboBox.getCurrentValue());
        mapPlace.setLabelField(labelFieldComboBox.getCurrentValue());

        if (placeScaleMode == ScaleMode.FIXED)
            mapPlace.setFixedSize();
        else
            mapPlace.setComputedSize();

        String sizeString = staticSizeTextBox.getValue();
        try {
            int sizeValue = Integer.parseInt(sizeString);
            mapPlace.setSize(sizeValue);
        } catch (NumberFormatException nfe) {
            staticSizeTextBox.setValue("1");
            mapPlace.setSize(1);
        }
        mapPlace.setSizeField(sizeFunctionFdcb.getCurrentValue());
        if (sizeFunctionComboBox.getCurrentValue() != null)
            mapPlace.setAggregateFunction(sizeFunctionComboBox.getCurrentValue().getAggregateType());
        mapPlace.setPlaceSizeByDynamicType(useDynamicTypeCheckBox.getValue());

        mapPlace.setTypeFixed(typeFixed.getValue());
        String typeName = typeTextBox.getValue();
        if (typeName.equals(mapPlace.getName()))
            mapPlace.setTypeName(null);
        else if (!typeName.equals(mapPlace.getTypeName()))
            mapPlace.setTypeName(typeName);
        mapPlace.setTypeField(typeFieldList.getCurrentValue());
        mapPlace.setIncludeNullType(includeNullTypeCheckBox.getValue());

        mapPlace.setUseDefaultIconSetting(!iconCheckBox.getValue());
        mapPlace.setIconFixed(iconFixed.getValue());
        mapPlace.setIconId(iconId);
        mapPlace.setIconField(iconFieldList.getCurrentValue());
        mapPlace.setUseDefaultShapeSetting(!shapeCheckBox.getValue());
        mapPlace.setShapeTypeString(shapeType.toString());
        mapPlace.setUseDefaultColorSetting(!colorCheckBox.getValue());
        mapPlace.setColorString(colorPalette.getValue());
    }

    protected void setSelection() {
        MapPlace mapPlace = presenter.getMapPlace();
        String placeName = mapPlace.getName();
        FieldDef latField = null;
        FieldDef longField = null;

        if (mapPlace.getLatField() != null)
            latField = mapPlace.getLatField();

        if (mapPlace.getLongField() != null)
            longField = mapPlace.getLongField();

        setSelection(placeName, latField, longField);

        if (previewRenderedNode != null) {
            previewRenderedNode.removeFromParent();
            previewRenderedNode = null;
        }
        if (originalRenderedNode != null) {
            originalRenderedNode.removeFromParent();
            originalRenderedNode = null;
        }

        FieldDef labelField = null;

        if (mapPlace.getLabelField() != null)
            labelField = mapPlace.getLabelField();

        labelFieldComboBox.setValue(labelField);

        if (mapPlace.isFixedSize()) {
            placeScaleMode = ScaleMode.FIXED;
            selectSizeFixed();
        } else {
            placeScaleMode = ScaleMode.COMPUTED;
            selectSizeComputed();
        }

        Integer size = mapPlace.getSize();
        if (size != null)
            staticSizeTextBox.setValue(size.toString());

        sizeField = null;

        if (mapPlace.getSizeField() != null) {
            sizeField = mapPlace.getSizeField();
            sizeFunctionComboBox.setEnabled(true);
        }

        sizeFunctionFdcb.setValue(sizeField);

        useDynamicTypeCheckBox.setValue(mapPlace.isPlaceSizeByDynamicType());

        typeField = null;

        if (mapPlace.getTypeField() != null)
            typeField = mapPlace.getTypeField();

        typeFieldList.setValue(typeField);

        includeNullTypeCheckBox.setValue(mapPlace.isIncludeNullType());

        String typeName = mapPlace.getTypeName();
        if (typeName == null || typeName.trim().length() == 0) {
            typeName = mapPlace.getName();
            typeUsePlacename = true;
        } else
            typeUsePlacename = false;
        typeTextBox.setValue(typeName);

        setTypeFixed(mapPlace.isTypeFixed());

        tooltipFunction = null;

        if (mapPlace.getAggregateFunction() != null)
            tooltipFunction = TooltipFunction.getByAggregateFunction(mapPlace.getAggregateFunction());

        sizeFunctionComboBox.setValue(tooltipFunction, true);

        iconField = null;

        if (mapPlace.getIconField() != null)
            iconField = mapPlace.getIconField();

        iconFieldList.setValue(iconField);

        setShapeCheckBox(mapPlace.isUseDefaultShapeSetting());
        setColorCheckBox(mapPlace.isUseDefaultColorSetting());
        setIconCheckBox(mapPlace.isUseDefaultIconSetting());
        getRenderedNode();
    }

    private void setSelection(String placeName, FieldDef latField, FieldDef longField) {
        placeNameTextBox.setText(placeName);
        latFieldComboBox.setValue(latField);
        longFieldComboBox.setValue(longField);
    }

    private void setShapeCheckBox(boolean useDefault) {
        shapeCheckBox.setValue(!useDefault);
        enableShapeControls(useDefault);
    }

    private void setColorCheckBox(boolean useDefault) {
        colorCheckBox.setValue(!useDefault);
        enableColorControls(useDefault);
    }

    private void setIconCheckBox(boolean useDefault) {
        iconCheckBox.setValue(!useDefault);
        enableIconControls(useDefault);
    }

    protected void initializeValidation(MultiValidatorCollectingErrors validator) {
        Validator placeNameNullValidator = new Validator() {
            @Override
            public boolean isValid() {
                String placeNameText = placeNameTextBox.getText();
                return placeNameText != null && placeNameText.length() != 0;
            }
        };
        ValidationFeedback placeNameNullFeedback = new StringValidationFeedback(
                _constants.mapSettingsView_placeNameNullFeedbackText()); // $NON-NLS-1$
        validator
                .addValidationAndFeedback(new ValidationAndFeedbackPair(placeNameNullValidator, placeNameNullFeedback));

        Validator placeNameAlreadyExistingValidator = new Validator() {
            @Override
            public boolean isValid() {
                String placeNameText = placeNameTextBox.getText();

                for (MapPlace thisMapPlace : presenter.getMapSettings().getMapPlaces()) {
                    if (thisMapPlace.getName().equals(placeNameText)
                            && !thisMapPlace.getUuid().equals(presenter.getMapPlace().getUuid())) {
                        return false;
                    }
                }

                return true;
            }
        };
        ValidationFeedback placeNameAlreadyExistingFeedback = new StringValidationFeedback(
                _constants.mapSettingsView_placeNameAlreadyExistingFeedbackText()); // $NON-NLS-1$
        validator.addValidationAndFeedback(
                new ValidationAndFeedbackPair(placeNameAlreadyExistingValidator, placeNameAlreadyExistingFeedback));

        Validator latFieldNullValidator = new Validator() {
            @Override
            public boolean isValid() {
                return latFieldComboBox.getValue() != null;
            }
        };
        ValidationFeedback latFieldNullFeedback = new StringValidationFeedback(
                _constants.mapSettingsView_latFieldNullFeedbackText()); // $NON-NLS-1$
        validator.addValidationAndFeedback(new ValidationAndFeedbackPair(latFieldNullValidator, latFieldNullFeedback));

        Validator longFieldNullValidator = new Validator() {
            @Override
            public boolean isValid() {
                return longFieldComboBox.getValue() != null;
            }
        };
        ValidationFeedback longFieldNullFeedback = new StringValidationFeedback(
                _constants.mapSettingsView_longFieldNullFeedbackText()); // $NON-NLS-1$
        validator
                .addValidationAndFeedback(new ValidationAndFeedbackPair(longFieldNullValidator, longFieldNullFeedback));

        Validator latFieldNumberValidator = new Validator() {
            @Override
            public boolean isValid() {
                FieldDef latField = latFieldComboBox.getValue();
                return latField != null && latField.getValueType() == CsiDataType.Number;
            }
        };
        ValidationFeedback latFieldValueNotNumberFeedback = new StringValidationFeedback(
                _constants.mapSettingsView_latFieldValueNotNumberFeedbackText()); // $NON-NLS-1$
        validator.addValidationAndFeedback(
                new ValidationAndFeedbackPair(latFieldNumberValidator, latFieldValueNotNumberFeedback));

        Validator longFieldNumberValidator = new Validator() {
            @Override
            public boolean isValid() {
                FieldDef longField = latFieldComboBox.getValue();
                return longField != null && longField.getValueType() == CsiDataType.Number;
            }
        };
        ValidationFeedback longFieldValueNotNumberFeedback = new StringValidationFeedback(
                _constants.mapSettingsView_longFieldValueNotNumberFeedback()); // $NON-NLS-1$
        validator.addValidationAndFeedback(
                new ValidationAndFeedbackPair(longFieldNumberValidator, longFieldValueNotNumberFeedback));
        Validator sizeFieldNullValidator = new Validator() {
            @Override
            public boolean isValid() {
                if (placeScaleMode == ScaleMode.FIXED) {
                    String staticSizeTextBoxValue = staticSizeTextBox.getValue();
                    return staticSizeTextBoxValue != null && !staticSizeTextBoxValue.isEmpty();
                }
                return true;
            }
        };
        ValidationFeedback sizeFieldNullFeedback = new StringValidationFeedback(
                _constants.mapSettingsView_sizeFieldNullFeedbackText()); // $NON-NLS-1$
        validator
                .addValidationAndFeedback(new ValidationAndFeedbackPair(sizeFieldNullValidator, sizeFieldNullFeedback));

        Validator sizeFieldNumberValidator = new Validator() {
            @Override
            public boolean isValid() {
                if (placeScaleMode == ScaleMode.FIXED) {
                    String staticSizeTextBoxValue = staticSizeTextBox.getValue();
                    if (staticSizeTextBoxValue == null || staticSizeTextBoxValue.isEmpty())
                        return false;

                    boolean retVal = false;
                    try {
                        int intVal = Integer.parseInt(staticSizeTextBoxValue);
                        if (1 <= intVal && intVal <= 5) {
                            retVal = true;
                        }
                    } catch (NumberFormatException e) {
                    }
                    return retVal;
                }
                return true;
            }
        };
        ValidationFeedback sizeFieldValueNotNumberFeedback = new StringValidationFeedback(
                _constants.mapSettingsView_sizeFieldValueNumberNotRightFeedbackText()); // $NON-NLS-1$
        validator.addValidationAndFeedback(
                new ValidationAndFeedbackPair(sizeFieldNumberValidator, sizeFieldValueNotNumberFeedback));

        Validator sizeColumnNullValidator = new Validator() {
            @Override
            public boolean isValid() {
                if (placeScaleMode == ScaleMode.COMPUTED) {
                    return sizeFunctionFdcb.getCurrentValue() != null;
                }
                return true;
            }
        };
        ValidationFeedback sizeColumnNullFeedback = new StringValidationFeedback(
                _constants.mapSettingsView_sizeColumnNullFeedbackText()); // $NON-NLS-1$
        validator.addValidationAndFeedback(
                new ValidationAndFeedbackPair(sizeColumnNullValidator, sizeColumnNullFeedback));

        Validator sizeFunctionNullValidator = new Validator() {
            @Override
            public boolean isValid() {
                if (placeScaleMode == ScaleMode.COMPUTED) {
                    return sizeFunctionComboBox.getCurrentValue() != null;
                }
                return true;
            }
        };
        ValidationFeedback sizeFunctionNullFeedback = new StringValidationFeedback(
                _constants.mapSettingsView_sizeFunctionNullFeedbackText()); // $NON-NLS-1$
        validator.addValidationAndFeedback(
                new ValidationAndFeedbackPair(sizeFunctionNullValidator, sizeFunctionNullFeedback));

        Validator typeColumnNullValidator = new Validator() {
            @Override
            public boolean isValid() {
                if (typeFixed.getValue()) {
                    return true;
                } else {
                    return typeFieldList.getCurrentValue() != null;
                }
            }
        };
        ValidationFeedback typeColumnNullFeedback = new StringValidationFeedback(
                _constants.mapSettingsView_typeColumnNullFeedbackText()); // $NON-NLS-1$
        validator.addValidationAndFeedback(
                new ValidationAndFeedbackPair(typeColumnNullValidator, typeColumnNullFeedback));

        Validator iconColumnNullValidator = new Validator() {
            @Override
            public boolean isValid() {
                if (iconFixed.getValue()) {
                    return true;
                } else {
                    return iconFieldList.getCurrentValue() != null;
                }
            }
        };
        ValidationFeedback iconColumnNullFeedback = new StringValidationFeedback(
                _constants.mapSettingsView_iconColumnNullFeedbackText()); // $NON-NLS-1$
        validator.addValidationAndFeedback(
                new ValidationAndFeedbackPair(iconColumnNullValidator, iconColumnNullFeedback));
    }

    @UiHandler("clearImage")
    public void clearImage(ClickEvent e) {
        MapPlace mapPlace = presenter.getMapPlace();
        iconId = null;
        mapPlace.setIconId(null);
        imagePreview.setUrl("");
        getRenderedNode();
    }

}
