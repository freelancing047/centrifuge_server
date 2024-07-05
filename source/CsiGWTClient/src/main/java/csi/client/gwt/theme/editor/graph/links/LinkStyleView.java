package csi.client.gwt.theme.editor.graph.links;

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
import csi.server.common.model.themes.graph.LinkStyle;

public class LinkStyleView{

    private static LinkStyleViewUiBinder uiBinder = GWT.create(LinkStyleViewUiBinder.class);
    private static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
    @UiField
    Dialog dialog;
    
    @UiField
    LinkStyleFieldTab fieldTab;
    
    @UiField
    LinkStyleAppearanceTab appearanceTab;
    
    @UiField
    CsiTabPanel tabPanel;

    
    private LinkPresenter presenter;

    private LinkStyle linkStyle;
       
    private boolean closed = false;
    
    interface LinkStyleViewUiBinder extends UiBinder<Widget, LinkStyleView> {
    }
    
    public LinkStyleView(LinkPresenter linkPresenter) {

        
        presenter = linkPresenter;
        
        uiBinder.createAndBindUi(this);

        fieldTab.setPresenter(linkPresenter);
        appearanceTab.setPresenter(linkPresenter);

        dialog.setTitle(i18n.themeEditor_graph_add_link_style_label());
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
                updateLinkStyle();
                if (linkStyle.getName() == null || linkStyle.getName().equals("")) {
                    close();
                } else {
                    saveAndClose();
                }
            }
        });
    }


    public void display(LinkStyle linkStyle) {
        this.linkStyle = linkStyle;
        clear();
        

        fieldTab.display(linkStyle);
        appearanceTab.display(linkStyle);
                
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
    
    private void updateLinkStyle(){
        linkStyle.getFieldNames().clear();

        fieldTab.save(linkStyle);
        appearanceTab.save(linkStyle);
    }

    private void close(){
        dialog.hide();
        closed = true;
        dialog.destroy();
    }
    
    private void saveAndClose() {
        presenter.updateTheme(linkStyle);
        close();
    }

    public LinkStyle getLinkStyle() {
        return linkStyle;
    }

    public void setLinkStyle(LinkStyle linkStyle) {
        this.linkStyle = linkStyle;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public LinkPresenter getPresenter() {
        return presenter;
    }

    public void setPresenter(LinkPresenter presenter) {
        this.presenter = presenter;
    }
}
