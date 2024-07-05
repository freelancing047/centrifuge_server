package csi.client.gwt.theme.editor;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import csi.client.gwt.dataview.AbstractDataViewPresenter;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.widget.boot.Dialog;

public class ThemeEditorManager {
    

    private static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
    
    private ThemeEditorPanel panel;
    private Dialog dialog;
    
    
    public ThemeEditorManager(ThemeEditorPresenter themeEditorPresenter, AbstractDataViewPresenter dataViewPresenter){
        panel = themeEditorPresenter.getView();
        if(dataViewPresenter != null && dataViewPresenter.getDataView() != null){
            themeEditorPresenter.setFieldNames(dataViewPresenter.getDataView().getMeta().getModelDef().getFieldListAccess().getFieldDefNames());
        }
        dialog = new Dialog();
        dialog.setTitle(i18n.themeEditor_title());
        dialog.setBodyHeight("330px");
        dialog.setBodyWidth("490px");
        dialog.setHeight("435px");
        dialog.setWidth("535px");
        dialog.add(panel);
        themeEditorPresenter.populateThemeNames();
        dialog.getActionButton().setVisible(false);
        csi.client.gwt.widget.buttons.Button cancelButton = dialog.getCancelButton();
        cancelButton.setText(i18n.kmlExportDialogcloseButton()); //$NON-NLS-1$
        cancelButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                dialog.hide();
                dialog.destroy();
            }
        });
    }


    public void show() {
        dialog.show();
    }

}
