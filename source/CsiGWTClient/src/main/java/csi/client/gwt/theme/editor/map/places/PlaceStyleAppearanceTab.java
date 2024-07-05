package csi.client.gwt.theme.editor.map.places;

import static com.google.common.base.Preconditions.checkNotNull;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.CheckBox;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.ColorPalette;

import csi.client.gwt.WebMain;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.icon.IconSelectionEvent;
import csi.client.gwt.icon.IconSelectionHandler;
import csi.client.gwt.icon.IconUtils;
import csi.client.gwt.icon.ui.IconManager;
import csi.client.gwt.viz.graph.node.settings.appearance.AppearanceTab;
import csi.client.gwt.viz.graph.node.settings.appearance.NodeShape;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.ErrorDialog;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.graphics.shapes.ShapeType;
import csi.server.common.model.themes.map.PlaceStyle;
import csi.server.common.service.api.GraphActionServiceProtocol;
import csi.shared.core.color.ClientColorHelper;
import csi.shared.core.color.ClientColorHelper.Color;

public class PlaceStyleAppearanceTab extends Composite {

    private static PlaceStyleFieldTabUiBinder uiBinder = GWT.create(PlaceStyleFieldTabUiBinder.class);

    interface PlaceStyleFieldTabUiBinder extends UiBinder<Widget, PlaceStyleAppearanceTab> {
    }
    

    private static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
    private static NumberFormat numberFormat = NumberFormat.getDecimalFormat();
   
    @UiField
    CheckBox icon;
    
    @UiField
    CheckBox hideShapeCheckBox;
    
        
    @UiField
    SimplePanel uploadContainer;

    @UiField
    Image imagePreview;
    
    @UiField
    Image stylePrevious;
    
    @UiField
    Image styleCurrent;

    
    @UiField
    Button colorButton;
    
    @UiField(provided = true)
    ColorPalette colorPalette;
    
    @UiField
    TextBox colorTextBox;
    
    @UiField
    TextBox iconScale;
    
    @UiField
    HTMLPanel shapePanel;
    
    
    private PlacePresenter presenter;
    private PlaceStyle placeStyle;
    private Integer color;


    public PlaceStyleAppearanceTab() {


        colorPalette = new ColorPalette(AppearanceTab.nodeColors, AppearanceTab.nodeColors);
        colorPalette.setVisible(false);
        Style colorStyle = colorPalette.getElement().getStyle();
        colorStyle.setLeft(37, Unit.PX);
        colorStyle.setBottom(14, Unit.PX);
        colorStyle.setWidth(109, Unit.PX);
        
        initWidget(uiBinder.createAndBindUi(this));
        
        uploadContainer.setHeight("24px");
        uploadContainer.setWidth("56px");
        uploadContainer.getElement().getStyle().setDisplay(Display.BLOCK);
            
        Button iconDialogButton = new Button();
        iconDialogButton.setText(i18n.themeEditor_map_place_style_appearance_icon_browse());
        iconDialogButton.setHeight("22px");
        iconDialogButton.setWidth("56px");

        icon.setText(i18n.themeEditor_map_place_style_appearance_icon_checkbox());

        iconDialogButton.addClickHandler(new ClickHandler(){

            @Override
            public void onClick(ClickEvent event) {
                
                
                IconManager iconManager = new IconManager(new IconSelectionHandler(){

                    @Override
                    public void onSelect(IconSelectionEvent iconSelectionEvent) {
                        imagePreview.setUrl(iconSelectionEvent.getIconUrl());
                        placeStyle.setIconId(iconSelectionEvent.getIconUuid());
                        icon.setValue(true, true);
                        updateCurrent();
                    }});
                
                
                iconManager.show();
            }});
        
        uploadContainer.add(iconDialogButton);

        imagePreview.setHeight("100%");
        imagePreview.setWidth("100%");
        imagePreview.getElement().getStyle().setBorderStyle(BorderStyle.NONE);

            
        icon.addValueChangeHandler(new ValueChangeHandler<Boolean>(){

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                
                if(event.getValue()){
                    imagePreview.getElement().getStyle().setBackgroundColor("#efefef");
                    imagePreview.getElement().getStyle().setOpacity(1);
                } else {
                    imagePreview.getElement().getStyle().setBackgroundColor("#cccccc");
                    imagePreview.getElement().getStyle().setOpacity(.5);
                }
                
                updateCurrent();
            }});
        
        hideShapeCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>(){

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                
                if(event.getValue()){
                    shapePanel.getElement().getStyle().clearBackgroundColor();
                    shapePanel.getElement().getStyle().setOpacity(1);
                } else {
                    shapePanel.getElement().getStyle().setBackgroundColor("#cccccc");
                    shapePanel.getElement().getStyle().setOpacity(.5);
                }
                
                updateCurrent();
            }});

        hideShapeCheckBox.setValue(true);
        icon.setValue(true);
        populateShapes();
        
        iconScale.addKeyUpHandler(new KeyUpHandler(){

            @Override
            public void onKeyUp(KeyUpEvent event) {

                String oldValue = iconScale.getValue();
                try{
                    int count = 0;
                    for(char character :oldValue.toCharArray()){
                        if(character == '.'){
                            count ++;
                        }
                    }
                    if(oldValue.endsWith(".") && count == 1){
                        //No op, allow a period at end
                    } else {
                        double value = Double.parseDouble(iconScale.getValue());
                        placeStyle.setIconScale(value);
                        updateCurrent();
                    }
                    
                }catch(Exception e){
                    event.stopPropagation();
                    iconScale.setValue(oldValue.substring(0, oldValue.length()-1), false);
                }
            }
        });
       
    }
    
    public void populateShapes() {
        for (final ShapeType shape : ShapeType.nodeShapeWheel) {
            ImageResource imageResource = NodeShape.imageFromShapeType(shape);
            if (imageResource != null) {
                Image image = new Image(imageResource);
                image.addClickHandler(new ClickHandler() {

                    @Override
                    public void onClick(ClickEvent event) {
                        if(hideShapeCheckBox.getValue()){
                            getPlaceStyle().setShape(shape);
                            updateCurrent();
                        }
                    }

                });
                shapePanel.add(image);
                image.setVisibleRect(-4, 0, 16, 16);
                image.getElement().getStyle().setMarginBottom(2, Unit.PX);
            }
        }
    }
    
    protected void updateCurrent() {
        final VortexFuture<String> futureTask = WebMain.injector.getVortex().createFuture();
        
        if(color != null){
            try {
                ShapeType shape = placeStyle.getShape();
                String iconId = placeStyle.getIconId();
                
                if(!icon.getValue()){
                    iconId = null;
                }
                
                if(!hideShapeCheckBox.getValue()){
                    shape = ShapeType.NONE;
                }
                
                futureTask.execute(GraphActionServiceProtocol.class).getNodeAsImageNew(iconId, shape, color, 50, placeStyle.getIconScale());
            } catch (CentrifugeException e) {
                
            }
            futureTask.addEventHandler(new AbstractVortexEventHandler<String>() {
    
                @Override
                public void onSuccess(String result) {
                    styleCurrent.setUrl(result);
                }
                });
        }
    }
    
    public void setColor(Color color) {
        colorPalette.setValue(color.toString().substring(1).toUpperCase());
        colorTextBox.setValue(color.toString().toUpperCase());
        colorButton.getElement().getStyle().setColor(color.toString());
    }
    
    public void display(PlaceStyle placeStyle) {
        this.placeStyle = placeStyle;
        
        color = placeStyle.getColor();
        
        iconScale.setValue(numberFormat.format(placeStyle.getIconScale()));
        hideShapeCheckBox.setValue(placeStyle.getShape() == null ? false:true, true);
        icon.setValue(placeStyle.getIconId() == null ? false:true, true);
                
        setColor(ClientColorHelper.get().make(placeStyle.getColor()));
        
        if(placeStyle.getIconId() != null){
            IconUtils.getBase64(placeStyle.getIconId(), new AbstractVortexEventHandler<String>(){

                @Override
                public void onSuccess(String result) {
                    if(result == null || result.isEmpty()){
                        imagePreview.setUrl("");
                    } else {
                        imagePreview.setUrl(result);
                    }
                    
                }});
        }
        
        final VortexFuture<String> futureTask = WebMain.injector.getVortex().createFuture();
        try {
            futureTask.execute(GraphActionServiceProtocol.class).getNodeAsImageNew(placeStyle.getIconId(), placeStyle.getShape(), color, 40, placeStyle.getIconScale());
        } catch (CentrifugeException e) {
            
        }
        futureTask.addEventHandler(new AbstractVortexEventHandler<String>() {

            @Override
            public void onSuccess(String result) {
                stylePrevious.setUrl(result);
                styleCurrent.setUrl(result);
            }
            });
    }
    
    void clear() {

        icon.setValue(false, true);
    }

    public void display() {
        clear();
    }
    
    @UiHandler("colorTextBox")
    void onColorTextInput(ValueChangeEvent<String> event) {
        checkNotNull(event);
        String value = checkNotNull(event.getValue());
        if (value.charAt(0) == '#') {
            value = value.substring(1);
        }

        Color colorName = ClientColorHelper.get().makeFromHex(value);
        color = colorName.getIntColor();
        colorPalette.setVisible(false);
        setColor(colorName);
        updateCurrent();
    }
    
    @UiHandler("colorPalette")
    public void onSelection(SelectionEvent<String> event) {
        Color colorName = ClientColorHelper.get().makeFromHex(event.getSelectedItem());
        color = colorName.getIntColor();
        colorPalette.setVisible(false);
        colorTextBox.setValue(colorName.toString().toUpperCase(), false);
        setColor(colorName);
        updateCurrent();
    }
    
    @UiHandler("colorButton")
    void colorButonClickHandler(ClickEvent event) {
        if (colorPalette.isVisible()) {
            colorPalette.setVisible(false);
        } else {
            colorPalette.setVisible(true);
        }
    }
        
    private VortexEventHandler<String> uploadIconEventHandler = new VortexEventHandler<String>(){

        @Override
        public void onSuccess(String result) {
            placeStyle.setIconId(result);
            imagePreview.setUrl(IconUtils.generateIconDownloadUri(result));
            icon.setValue(true, true);
            updateCurrent();
        }

        @Override
        public boolean onError(Throwable t) {
            new ErrorDialog("Icon could not be uploaded").show();
            return false;
        }

        @Override
        public void onUpdate(int taskProgess, String taskMessage) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void onCancel() {
            // TODO Auto-generated method stub
            
        }

    };

    public void save(PlaceStyle placeStyle) {
        placeStyle.setColor(color);
        String iconScaleString = iconScale.getText();
        if(iconScaleString != null && iconScaleString.endsWith(".")){
            iconScaleString = iconScaleString.split(".")[0];
        }
        if(iconScaleString.isEmpty()){
            placeStyle.setIconScale(1.0);
        } else {
            placeStyle.setIconScale(Double.parseDouble(iconScaleString));
        }
        if(!icon.getValue()){
            placeStyle.setIconId(null);
        }
        
        if(!hideShapeCheckBox.getValue()){
            placeStyle.setShape(null);
        }
    }

    public PlacePresenter getPresenter() {
        return presenter;
    }

    public void setPresenter(PlacePresenter presenter) {
        this.presenter = presenter;
    }

    public PlaceStyle getPlaceStyle() {
        return placeStyle;
    }

    public void setPlaceStyle(PlaceStyle placeStyle) {
        this.placeStyle = placeStyle;
    }

}
