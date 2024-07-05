package csi.client.gwt.viz.shared.export.view;

import java.util.List;

import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.shared.export.ExportTypeHandler;
import csi.client.gwt.viz.shared.export.PickResolutionHandler;
import csi.client.gwt.viz.shared.export.SelectionOnlyHandler;
import csi.client.gwt.viz.shared.export.model.ExportType;
import csi.client.gwt.viz.shared.export.settings.ExportImageSettings;
import csi.client.gwt.viz.shared.export.settings.ExportSettings;
import csi.client.gwt.viz.shared.export.settings.ExportUseSelectionSettings;
import csi.client.gwt.viz.shared.export.view.widget.ExportTypeDropdownWidget;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.buttons.Button;

/**
 * Dialog shown when Export is selected on a visualization, worksheet, or data view.
 * @author Centrifuge Systems, Inc.
 */
public class ExportViewDialog {

    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    interface MyUiBinder extends UiBinder<Widget, ExportViewDialog> {
    }

    @UiField
    Dialog dialog;

    private final InlineTextWidget exportingTextWidget = new InlineTextWidget(18);
    private final SimplePanel simplePanel = new SimplePanel();

    private PickResolutionHandler pickResolutionHandler;
    private ExportSettings exportSettings;
    private List<ExportType> availableExportTypes;

    private SelectionOnlyHandler selectionOnlyHandler;

    private ExportTypeHandler exportTypeHandler;

    public ExportViewDialog() {
        uiBinder.createAndBindUi(this);

        buildUI();
        setupButtons();
        initializeHandlers();
    }

    private void buildUI() {
        VerticalPanel vp = new VerticalPanel();
        vp.add(exportingTextWidget);
        vp.add(simplePanel);
        vp.setHeight("155px");

        dialog.add(vp);
    }

    private void setupButtons() {
        getActionButton().setType(ButtonType.PRIMARY);
        getActionButton().setText(CentrifugeConstantsLocator.get().dialog_ExportButton());
        dialog.getCancelButton().setText(Dialog.txtCloseButton);
        dialog.hideOnCancel();
        dialog.hideOnAction();
    }

    public Button getActionButton() {
        return dialog.getActionButton();
    }

    private void initializeHandlers() {
        pickResolutionHandler = new PickResolutionHandler() {
            @Override
            public void onResolution(int width, int height) {
                ExportImageSettings is = new ExportImageSettings();
                is.setDesiredWidth(width);
                is.setDesiredHeight(height);
                exportSettings.setImageSettings(is);
                setViewFromSettings(exportSettings, availableExportTypes);
            }
        };

        selectionOnlyHandler = new SelectionOnlyHandler() {
            @Override
            public void onUseSelectionOnly(boolean selectionOnly) {
                ExportUseSelectionSettings useSelectionSettings = (ExportUseSelectionSettings) exportSettings;
                useSelectionSettings.setUseSelectionOnly(selectionOnly);
                setViewFromSettings(useSelectionSettings, availableExportTypes);
            }
        };
    }

    public void setViewFromSettings(ExportSettings exportSettings, List<ExportType> availableExportTypes) {
        this.exportSettings = exportSettings;
        this.availableExportTypes = availableExportTypes;
        initializeWidgetValues();

        ExportingTextWidgetBuilder builder = new ExportingTextWidgetBuilder(exportSettings, exportingTextWidget, this);
        builder.build(availableExportTypes);
    }

    private void initializeWidgetValues() {
        exportingTextWidget.clear();
        simplePanel.clear();
        getActionButton().setEnabled(true);
    }

    public void addExportButtonHandler(ClickHandler clickHandler) {
        getActionButton().addClickHandler(clickHandler);
    }

    public void setExportTypeHandler(ExportTypeHandler exportTypeHandler) {
        this.exportTypeHandler = exportTypeHandler;
    }

    public ExportSettings saveViewIntoModel() {
        return exportSettings;
    }

    public IsWidget createExportTypeWidget() {
        return new ExportTypeDropdownWidget(availableExportTypes, exportSettings.getExportType(), getExportTypeHandler());
    }

    public ExportTypeHandler getExportTypeHandler() {
        return exportTypeHandler;
    }

    public void addParameterizeExportWidget(IsWidget widget) {
        simplePanel.clear();
        simplePanel.add(widget);
    }

    public void show() {
        dialog.show();
    }

    public PickResolutionHandler getPickResolutionHandler() {
        return pickResolutionHandler;
    }

    public SelectionOnlyHandler getSelectionOnlyHandler() {
        return selectionOnlyHandler;
    }

}
