package csi.client.gwt.theme.editor.graph.links;

import static com.google.common.base.Preconditions.checkNotNull;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.ColorPalette;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.graph.node.settings.appearance.AppearanceTab;
import csi.client.gwt.vortex.VortexEventHandler;
import csi.client.gwt.widget.boot.ErrorDialog;
import csi.server.common.model.themes.graph.LinkStyle;
import csi.shared.core.color.ClientColorHelper;
import csi.shared.core.color.ClientColorHelper.Color;

public class LinkStyleAppearanceTab extends Composite {

    private static LinkStyleFieldTabUiBinder uiBinder = GWT.create(LinkStyleFieldTabUiBinder.class);

    interface LinkStyleFieldTabUiBinder extends UiBinder<Widget, LinkStyleAppearanceTab> {
    }
    

    private static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
    private static NumberFormat numberFormat = NumberFormat.getDecimalFormat();
   
        
//    @UiField
//    Image stylePrevious;
//    
//    @UiField
//    Image styleCurrent;

    
    @UiField
    Button colorButton;
    
    @UiField(provided = true)
    ColorPalette colorPalette;
    
    @UiField
    TextBox colorTextBox;
    
    @UiField
    TextBox width;
    
    
    private LinkPresenter presenter;
    private LinkStyle linkStyle;
    private Integer color;


    public LinkStyleAppearanceTab() {

        colorPalette = new ColorPalette(AppearanceTab.nodeColors, AppearanceTab.nodeColors);
        colorPalette.setVisible(false);
        Style colorStyle = colorPalette.getElement().getStyle();
        colorStyle.setLeft(37, Unit.PX);
        colorStyle.setBottom(123, Unit.PX);
        colorStyle.setWidth(109, Unit.PX);
        
        initWidget(uiBinder.createAndBindUi(this));
        
        width.addKeyUpHandler(new KeyUpHandler(){

            @Override
            public void onKeyUp(KeyUpEvent event) {

                String oldValue = width.getValue();
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
                        double value = Double.parseDouble(width.getValue());
                    }
                    
                }catch(Exception e){
                    event.stopPropagation();
                    width.setValue(oldValue.substring(0, oldValue.length()-1), false);
                }
            }
        });
       
    }
    
    
    
   
    public void setColor(Color color) {
        colorPalette.setValue(color.toString().substring(1).toUpperCase());
        colorTextBox.setValue(color.toString().toUpperCase());
        colorButton.getElement().getStyle().setColor(color.toString());
    }
    
    public void display(LinkStyle linkStyle) {
        this.linkStyle = linkStyle;
        color = linkStyle.getColor();
        setColor(ClientColorHelper.get().make(linkStyle.getColor()));
        width.setValue(""+linkStyle.getWidth()); 
      
    }
    
    void clear() {
        color = null;
        width.setValue("");
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
    }
    
    @UiHandler("colorPalette")
    public void onSelection(SelectionEvent<String> event) {
        Color colorName = ClientColorHelper.get().makeFromHex(event.getSelectedItem());
        color = colorName.getIntColor();
        colorPalette.setVisible(false);
        colorTextBox.setValue(colorName.toString().toUpperCase(), false);
        setColor(colorName);
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

    public void save(LinkStyle linkStyle) {
        linkStyle.setColor(color);
        String iconScaleString = width.getText();
        if(iconScaleString != null && iconScaleString.endsWith(".")){
            iconScaleString = iconScaleString.split(".")[0];
        }
        if(iconScaleString.isEmpty()){
            linkStyle.setWidth(1.0);
        } else {
            linkStyle.setWidth(Double.parseDouble(iconScaleString));
        }
    }

    public LinkPresenter getPresenter() {
        return presenter;
    }

    public void setPresenter(LinkPresenter presenter) {
        this.presenter = presenter;
    }

    public LinkStyle getLinkStyle() {
        return linkStyle;
    }

    public void setLinkStyle(LinkStyle linkStyle) {
        this.linkStyle = linkStyle;
    }

}
