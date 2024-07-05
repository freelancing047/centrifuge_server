package csi.client.gwt.viz.shared.export.view;

import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.shared.export.model.ExportType;
import csi.client.gwt.viz.shared.export.settings.ExportImageSettings;
import csi.client.gwt.viz.shared.export.settings.ExportSettings;
import csi.client.gwt.viz.shared.export.settings.ExportUseSelectionSettings;
import csi.client.gwt.viz.shared.export.view.widget.ResolutionWidget;
import csi.client.gwt.viz.shared.export.view.widget.SelectionRadioWidget;

/**
 *
 *  TODO: REMOVE THIS
 * Builds the appropriate InlineTextWidget.
 * @author Centrifuge Systems, Inc.
 */
public class ExportingTextWidgetBuilder {

    private final ExportSettings exportSettings;
    private final InlineTextWidget exportingTextWidget;
    private final ExportViewDialog exportDialog;

	private CentrifugeConstants _constants = CentrifugeConstantsLocator.get();
	
    public ExportingTextWidgetBuilder(ExportSettings exportSettings, InlineTextWidget widget, ExportViewDialog exportDialog) {
        this.exportSettings = exportSettings;
        this.exportingTextWidget = widget;
        this.exportDialog = exportDialog;
    }

    public void build(List<ExportType> availableExportTypes) {
        exportingTextWidget.clear();

        if (availableExportTypes.size() < 1) {
            setExportingTextToErrorCase();
        }
        else {
            setExportingText(availableExportTypes);
        }
    }

    private void setExportingText(final List<ExportType> availableExportTypes) {
        exportingTextWidget.add(_constants.dialog_ExportButton());
        if (exportSettings instanceof ExportUseSelectionSettings) {
//            addSelectionLink((ExportUseSelectionSettings) exportSettings);
        }

        exportingTextWidget.addBold(exportSettings.getName()).add(_constants.exportingTextWidgetBuilder_asA());
        addExportTypeControl(availableExportTypes);

//        if (exportSettings instanceof ExportImageSettings) {
//            addResolutionLink((ExportImageSettings) exportSettings);
//        }
    }

    private void addExportTypeControl(List<ExportType> availableExportTypes) {
        if(availableExportTypes.size() == 1){
            exportingTextWidget.add(exportSettings.getExportType().name());
        }else {
            ClickHandler clickOnExportHandler = createClickOnExportHandler(availableExportTypes);
            exportingTextWidget.addLink(exportSettings.getExportType().name(), clickOnExportHandler);
        }
    }

    private void addResolutionLink(final ExportImageSettings exportImageSettings) {
        ClickHandler clickOnResolutionHandler = createClickOnResolutionHandler(exportImageSettings);

        String linkText = _constants.exportingTextWidgetBuilder_visualizationSize();
        if(exportImageSettings.getDesiredWidth() > 0 && exportImageSettings.getDesiredHeight() > 0) {
            exportingTextWidget.add(_constants.exportingTextWidgetBuilder_withSize());
            linkText = exportImageSettings.getDesiredWidth() + "x" + exportImageSettings.getDesiredHeight();
        }
        else{
            exportingTextWidget.add(_constants.exportingTextWidgetBuilder_withThe());

        }
        exportingTextWidget.addLink(linkText, clickOnResolutionHandler);
    }

    private void addSelectionLink(final ExportUseSelectionSettings exportUseSelectionSettings) {
        ClickHandler clickOnSelectionHandler = createClickOnSelectionHandler(exportUseSelectionSettings);

        if (exportUseSelectionSettings.isUseSelectionOnly()) {
            exportingTextWidget.addLink(_constants.exportingTextWidgetBuilder_selectionOf(), clickOnSelectionHandler);
        } else {
            exportingTextWidget.addLink(_constants.exportingTextWidgetBuilder_all(), clickOnSelectionHandler);
        }
    }


    private ClickHandler createClickOnResolutionHandler(final ExportImageSettings exportImageSettings) {
        return new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                exportDialog.addParameterizeExportWidget(new ResolutionWidget(exportImageSettings.getDesiredWidth(), exportImageSettings.getDesiredHeight(), exportDialog.getPickResolutionHandler()));
            }
        };
    }

    private ClickHandler createClickOnExportHandler(final List<ExportType> availableExportTypes) {
        return new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                exportDialog.addParameterizeExportWidget(exportDialog.createExportTypeWidget());
            }
        };
    }

    private ClickHandler createClickOnSelectionHandler(final ExportUseSelectionSettings exportUseSelectionSettings) {
        return new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                exportDialog.addParameterizeExportWidget(new SelectionRadioWidget(exportUseSelectionSettings.isUseSelectionOnly(), exportDialog.getSelectionOnlyHandler()));
            }
        };
    }

    private void setExportingTextToErrorCase() {
        exportingTextWidget.add(_constants.exportingTextWidgetBuilder_error());
        exportDialog.getActionButton().setEnabled(false);
    }

}
