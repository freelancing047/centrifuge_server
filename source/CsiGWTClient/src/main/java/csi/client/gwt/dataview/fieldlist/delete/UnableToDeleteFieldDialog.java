package csi.client.gwt.dataview.fieldlist.delete;

import com.github.gwtbootstrap.client.ui.Tab;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import csi.client.gwt.widget.boot.CsiTabPanel;
import csi.client.gwt.widget.boot.Dialog;
import csi.shared.core.field.FieldReferences;

/**
 * @author Centrifuge Systems, Inc.
 * Displayed when attempting to delete a field that has is being used.
 */
public class UnableToDeleteFieldDialog {

    interface UnableToDeleteUIBinder extends UiBinder<Dialog, UnableToDeleteFieldDialog> {
    }

    private static UnableToDeleteUIBinder uiBinder = GWT.create(UnableToDeleteUIBinder.class);

    private final FieldReferences fieldReferences;

    @UiField
    Dialog dialog;
    @UiField
    CsiTabPanel tabPanel;
    @UiField
    Tab visualizationTab;
    @UiField
    Tab fieldTab;
    @UiField
    Tab filterTab;
    @UiField
    Tab linkupTab;

    public UnableToDeleteFieldDialog(FieldReferences fieldReferences) {
        this.fieldReferences = fieldReferences;
        uiBinder.createAndBindUi(this);

        dialog.getActionButton().setVisible(false);
        dialog.getCancelButton().setText(Dialog.txtCloseButton);
        dialog.hideOnCancel();

        dialog.add(tabPanel);

        buildPanels();
        handleVisibilities();
    }

    private void handleVisibilities() {
        if(fieldReferences.getVisualizationNames().size() == 0){
            tabPanel.remove(visualizationTab);
        }
        if(fieldReferences.getFieldNames().size() == 0){
            tabPanel.remove(fieldTab);
        }
        if(fieldReferences.getFilterNames().size() == 0){
            tabPanel.remove(filterTab);
        }
        if(fieldReferences.getLinkupNames().size() == 0){
            tabPanel.remove(linkupTab);
        }
    }

    private void buildPanels() {
        visualizationTab.add(buildVisualizations());
        fieldTab.add(buildFields());
        filterTab.add(buildFilter());
        linkupTab.add(buildLinkup());
    }

    private Widget buildLinkup() {
        VerticalPanel vp = new VerticalPanel();
        for (String name : fieldReferences.getLinkupNames()) {
            vp.add(new Label(name));
        }
        return vp;
    }

    private Widget buildFilter() {
        VerticalPanel vp = new VerticalPanel();
        for (String name : fieldReferences.getFilterNames()) {
            vp.add(new Label(name));
        }
        return vp;
    }

    private Widget buildFields() {
        VerticalPanel vp = new VerticalPanel();
        for (String name : fieldReferences.getFieldNames()) {
            vp.add(new Label(name));
        }
        return vp;
    }

    private Widget buildVisualizations() {
        VerticalPanel vp = new VerticalPanel();
        for (String name : fieldReferences.getVisualizationNames()) {
            vp.add(new Label(name));
        }
        return vp;
    }

    public void show(){
        dialog.show();
    }
}
