package csi.client.gwt.theme.editor.graph.nodes;

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
import csi.server.common.model.themes.graph.NodeStyle;

public class NodeStyleView{

    private static NodeStyleViewUiBinder uiBinder = GWT.create(NodeStyleViewUiBinder.class);
    private static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
    @UiField
    Dialog dialog;
    
    @UiField
    NodeStyleFieldTab fieldTab;
    
    @UiField
    NodeStyleAppearanceTab appearanceTab;
    
    @UiField
    CsiTabPanel tabPanel;

    
    private NodePresenter presenter;

    private NodeStyle nodeStyle;
       
    private boolean closed = false;
    
    interface NodeStyleViewUiBinder extends UiBinder<Widget, NodeStyleView> {
    }
    
    public NodeStyleView(NodePresenter nodePresenter) {

        
        presenter = nodePresenter;
        
        uiBinder.createAndBindUi(this);
        fieldTab.setPresenter(nodePresenter);
        appearanceTab.setPresenter(nodePresenter);

        dialog.setTitle(i18n.nodeStyleDialog_title());
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
                updateNodeStyle();
                if(nodeStyle.getName() == null || nodeStyle.getName().equals("")){
                    close();
                }else {
                    saveAndClose();
                }
            }
        });
        
    }

    public void display(NodeStyle nodeStyle) {
        this.nodeStyle = nodeStyle;
        clear();
        

        fieldTab.display(nodeStyle);
        appearanceTab.display(nodeStyle);
                
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
    
    
    private void updateNodeStyle(){
        nodeStyle.getFieldNames().clear();

        fieldTab.save(nodeStyle);
        appearanceTab.save(nodeStyle);

    }

    private void close(){
        dialog.hide();

        closed = true;
        dialog.destroy();
    }



    private void saveAndClose() {
        presenter.updateTheme(nodeStyle);
        close();
    }

    public NodeStyle getNodeStyle() {
        return nodeStyle;
    }

    public void setNodeStyle(NodeStyle nodeStyle) {
        this.nodeStyle = nodeStyle;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public NodePresenter getPresenter() {
        return presenter;
    }

    public void setPresenter(NodePresenter presenter) {
        this.presenter = presenter;
    }
}
