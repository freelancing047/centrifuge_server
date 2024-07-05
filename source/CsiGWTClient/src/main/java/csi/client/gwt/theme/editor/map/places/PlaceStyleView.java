package csi.client.gwt.theme.editor.map.places;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.widget.boot.CsiTabPanel;
import csi.client.gwt.widget.boot.Dialog;
import csi.server.common.model.themes.map.PlaceStyle;

public class PlaceStyleView{

    private static PlaceStyleViewUiBinder uiBinder = GWT.create(PlaceStyleViewUiBinder.class);
    private static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
    @UiField
    Dialog dialog;
    
    @UiField
    PlaceStyleFieldTab fieldTab;
    
    @UiField
    PlaceStyleAppearanceTab appearanceTab;
    
    @UiField
    CsiTabPanel tabPanel;

    
    private PlacePresenter presenter;

    private PlaceStyle placeStyle;
       
    private boolean closed = false;
    
    interface PlaceStyleViewUiBinder extends UiBinder<Widget, PlaceStyleView> {
    }
    
    public PlaceStyleView(PlacePresenter placePresenter) {

        
        presenter = placePresenter;
        
        uiBinder.createAndBindUi(this);

        fieldTab.setPresenter(placePresenter);
        appearanceTab.setPresenter(placePresenter);

        dialog.setTitle(i18n.themeEditor_map_place_styles_label());
        tabPanel.selectTab(0);
                
        
        dialog.setBodyHeight("310px");
        dialog.setBodyWidth("590px");
        dialog.setWidth("600px");
        dialog.setHeight("433px");  
               
        
        
        csi.client.gwt.widget.buttons.Button actionButton = dialog.getActionButton();
        actionButton.setText(i18n.kmlExportDialogcloseButton()); //$NON-NLS-1$
        actionButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                saveAndClose();
            }
        });
        
        actionButton.setVisible(false);


        // right now - delete if name is empty.. hopefully that doesnt' mess people up.
        csi.client.gwt.widget.buttons.Button cancelButton = dialog.getCancelButton();
        cancelButton.setText(i18n.kmlExportDialogcloseButton()); //$NON-NLS-1$
        cancelButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                updatePlaceStyle();

                if(placeStyle.getName() == null || placeStyle.getName().equals("")){
                    close();
                }else {
                    saveAndClose();
                }
            }

            
        });
        
    }
    

    public void display(PlaceStyle placeStyle) {
        this.placeStyle = placeStyle;
        clear();
        

        fieldTab.display(placeStyle);
        appearanceTab.display(placeStyle);
                
        dialog.show();
    }

    private void clear() {
        
        fieldTab.clear();
        appearanceTab.clear();
    }

    public void display() {
        clear();
        
        dialog.show();
    }


    private void updatePlaceStyle(){
        placeStyle.getFieldNames().clear();

        fieldTab.save(placeStyle);
        appearanceTab.save(placeStyle);
    }


    private void close(){
        dialog.hide();
        closed = true;
        dialog.destroy();
    }
    
    
    private void saveAndClose() {
        presenter.updateTheme(placeStyle);
        close();
    }

    public PlaceStyle getPlaceStyle() {
        return placeStyle;
    }

    public void setPlaceStyle(PlaceStyle placeStyle) {
        this.placeStyle = placeStyle;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public PlacePresenter getPresenter() {
        return presenter;
    }

    public void setPresenter(PlacePresenter presenter) {
        this.presenter = presenter;
    }
}
