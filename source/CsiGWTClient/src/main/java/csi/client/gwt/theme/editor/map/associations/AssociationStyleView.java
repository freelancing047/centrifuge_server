package csi.client.gwt.theme.editor.map.associations;

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
import csi.server.common.model.themes.map.AssociationStyle;

public class AssociationStyleView{

    private static AssociationStyleViewUiBinder uiBinder = GWT.create(AssociationStyleViewUiBinder.class);
    private static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
    @UiField
    Dialog dialog;
    
    @UiField
    AssociationStyleFieldTab fieldTab;
    
    @UiField
    AssociationStyleAppearanceTab appearanceTab;
    
    @UiField
    CsiTabPanel tabPanel;

    
    private AssociationPresenter presenter;

    private AssociationStyle associationStyle;
       
    private boolean closed = false;
    
    interface AssociationStyleViewUiBinder extends UiBinder<Widget, AssociationStyleView> {
    }
    
    public AssociationStyleView(AssociationPresenter associationPresenter) {

        
        presenter = associationPresenter;
        
        uiBinder.createAndBindUi(this);
        fieldTab.setPresenter(associationPresenter);
        appearanceTab.setPresenter(associationPresenter);

        dialog.setTitle(i18n.themeEditor_map_association_label());
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
        
        csi.client.gwt.widget.buttons.Button cancelButton = dialog.getCancelButton();
        cancelButton.setText(i18n.kmlExportDialogcloseButton()); //$NON-NLS-1$
        cancelButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                updateAssociationStyle();
                if(associationStyle.getName() == null || associationStyle.getName().equals("")){
                    close();
                }else{
                    saveAndClose();
                }
            }

            
        });
        
    }
    

    public void display(AssociationStyle associationStyle) {
        this.associationStyle = associationStyle;
        clear();
        

        fieldTab.display(associationStyle);
        appearanceTab.display(associationStyle);
                
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
    
    
    private void updateAssociationStyle(){
        associationStyle.getFieldNames().clear();

        fieldTab.save(associationStyle);
        appearanceTab.save(associationStyle);
    }


    private void close(){
        dialog.hide();
        closed = true;
        dialog.destroy();
    }


    private void saveAndClose() {
        presenter.updateTheme(associationStyle);
        close();
    }

    public AssociationStyle getAssociationStyle() {
        return associationStyle;
    }

    public void setAssociationStyle(AssociationStyle associationStyle) {
        this.associationStyle = associationStyle;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public AssociationPresenter getPresenter() {
        return presenter;
    }

    public void setPresenter(AssociationPresenter presenter) {
        this.presenter = presenter;
    }
}
