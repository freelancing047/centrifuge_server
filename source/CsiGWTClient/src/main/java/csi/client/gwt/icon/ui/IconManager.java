package csi.client.gwt.icon.ui;

import com.google.gwt.event.dom.client.ClickHandler;
import csi.client.gwt.csi_resource.filters.ResourceFilterDialog;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.icon.IconSelectionHandler;
import csi.client.gwt.icon.IconUploadPresenter;
import csi.client.gwt.widget.boot.Dialog;

public class IconManager {
    
    private Dialog dialog;
    private IconPanel iconPanel;

    public IconManager(){
        dialog = new  Dialog();

        dialog.setTitle(CentrifugeConstantsLocator.get().iconPanelTitle());
        dialog.setBodyHeight("480px");
        dialog.setBodyWidth("650px");
        dialog.setWidth("682px");
        dialog.setHeight("586px");

        dialog.getActionButton().setVisible(true);
        dialog.getActionButton().setText(CentrifugeConstantsLocator.get().iconPanelDoneButton());
        dialog.getCancelButton().setVisible(false);


        iconPanel = new IconPanel(new IconUploadPresenter(), dialog.getActionButton());

        iconPanel.getTagFilter();
        dialog.add(iconPanel.getIconFilter());
        dialog.add(iconPanel.getWidget());


        ClickHandler closeHandler = event -> {
            hide();
            iconPanel.clear();
        };

        dialog.getCancelButton().addClickHandler(closeHandler);
        dialog.getActionButton().addClickHandler(closeHandler);

    }
    
    public IconManager(IconSelectionHandler handler){
        this();
        iconPanel.addIconSelectionHandler(handler);
        dialog.getActionButton().setVisible(true);
        dialog.getActionButton().setVisible(true);
    }

    public void hide() {
        if(iconPanel != null)
            iconPanel.clear();
        
        if(dialog != null){
            dialog.hide();
            dialog.destroy();
        }
    }
    
    public void show() {
        dialog.show();
    }
    
    
    
}
