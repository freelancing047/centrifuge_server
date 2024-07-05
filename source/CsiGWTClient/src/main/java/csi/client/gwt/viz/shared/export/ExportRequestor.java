package csi.client.gwt.viz.shared.export;

import com.google.gwt.junit.client.impl.JUnitHost;
import csi.client.gwt.WebMain;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.mainapp.SecurityBanner;
import csi.client.gwt.util.DownloadHelper;
import csi.client.gwt.viz.graph.Graph;
import csi.client.gwt.viz.graph.surface.GraphSurface;
import csi.client.gwt.viz.shared.export.model.CompoundVisualizationExportData;
import csi.client.gwt.viz.shared.export.model.ExportType;
import csi.client.gwt.viz.shared.export.model.VisualizationExportData;
import csi.client.gwt.viz.shared.export.model.VisualizationExportable;
import csi.client.gwt.viz.shared.export.settings.ExportImageSettings;
import csi.client.gwt.viz.shared.export.settings.ExportSettings;
import csi.client.gwt.vortex.Callback;
import csi.client.gwt.widget.boot.ErrorDialog;
import csi.client.gwt.widget.boot.MaskDialog;
import csi.config.SecurityPolicyConfig;
import csi.server.common.service.api.ExportActionsServiceProtocol;
import csi.server.common.util.ValuePair;
import csi.shared.core.imaging.ImageComponent;
import csi.shared.core.imaging.ImagingRequest;
import csi.shared.core.imaging.PNGImageComponent;
import csi.shared.core.imaging.SVGImageComponent;

import java.util.Map;

//import com.github.gwtbootstrap.client.ui.Image;


/**
 * Makes the appropriate export request based on the exportable and exportSettings.
 * Invokes the callback which will download the file.
 * @author Centrifuge Systems, Inc.
 */
public class ExportRequestor {

    private final VisualizationExportable exportable;
    private final ExportSettings exportSettings;
    private final Callback<String> downloadCallback;

    private MaskDialog mask;

    public ExportRequestor(VisualizationExportable exportable, ExportSettings settings){
        this.exportable =  exportable;
        this.exportSettings = settings;
        this.downloadCallback = createDownloadCallback(settings.getExportType());
    }




    private Callback<String> createDownloadCallback(final ExportType exportType) {

        return (String fileToken) -> {
            mask.hide();
            DownloadHelper.download(exportSettings.getName(),
                    exportType.getFileSuffix(),
                    fileToken);
        };
    }

    private Callback<String> createZipCallback(){
        return fileToken -> {
            mask.hide();
            DownloadHelper.download(exportSettings.getName(),
                    ".zip",
                    fileToken);
        };
    }

    public void downloadExport() {

        mask = new MaskDialog("Exporting " + exportable.getName());
        mask.setTitle(CentrifugeConstantsLocator.get().exportInProgress());
        mask.show();

        switch (exportSettings.getExportType()) {
            case CSV:
                exportCSV();
                break;
            case PNG:
                exportPNGBundle();
                break;
            case ANX:
                exportANX();
                break;
            case ZIP:
                exportPNGBundle();
//            case PDF:
//                exportPDF();
//                break;
        }
    }
//
//    /**
//     * @deprecated
//     */
//    private void exportXML() {
//        WebMain.injector.getVortex().execute(downloadCallback, ExportActionsServiceProtocol.class).createPrettyXML(exportable.getDataViewUuid());
//    }
//
//    /**
//     * @deprecated
//     */
//    private void exportPDF() {
//        WebMain.injector.getVortex().execute(downloadCallback, ExportActionsServiceProtocol.class).createPDF(exportable.getDataViewUuid(), exportable.getData());
//    }


    /**
     * Exports given VisualizationExportData to a png. Graph follows different logic.
     */
    private void exportPNG() {

        if(exportable.getVisualization() instanceof Graph){
            exportGraphAsPNG(exportable);
            return;
        }
        exportAsPNG(exportable);
    }


    private void exportANX(){
        if(exportable.getVisualization() instanceof Graph) {
            Graph graph = (Graph) exportable.getVisualization();
            GraphSurface graphSurface = graph.getGraphSurface();

            WebMain.injector.getVortex().execute(downloadCallback, ExportActionsServiceProtocol.class).createANX(graphSurface.getVizUuid());
        }
    }


    private void exportPNGBundle(){
        // if timeline
        if(exportable instanceof  CompoundVisualizationExportData) {
            CompoundVisualizationExportData exportData = (CompoundVisualizationExportData) exportable;
            WebMain.injector.getVortex().execute(createZipCallback(), ExportActionsServiceProtocol.class).exportZipPNG(exportData.getImagingRequests());
        }else{
            exportPNG();
        }
    }

    private void exportAsPNG(VisualizationExportable visualizationExportData) {
        ImagingRequest request = null;
        if(visualizationExportData instanceof VisualizationExportData){
            VisualizationExportData exp = (VisualizationExportData) visualizationExportData;
            request = exp.getImagingRequest();
        }else if(visualizationExportData instanceof CompoundVisualizationExportData){
            CompoundVisualizationExportData exp = (CompoundVisualizationExportData) visualizationExportData;
            request = exp.getImagingRequests().get(0);
        }

        setImagingRequestSize(request);
        if(request == null) {
            showErrorForPNGExport();
            return;
        }
        WebMain.injector.getVortex().execute(downloadCallback, ExportActionsServiceProtocol.class).createPNG(request);

    }

    private void showErrorForPNGExport(){
        new ErrorDialog(
                CentrifugeConstantsLocator.get().exportRequestor_exportAsPNG_ErrorTitle(),
                CentrifugeConstantsLocator.get().exportRequestor_exportAsPNG_ErrorMessage()
        ).show();
    }

    public String RgbToHex(int r, int g, int b){
        StringBuilder sb = new StringBuilder();
        sb.append('#')
                .append(Integer.toHexString(r))
                .append(Integer.toHexString(g))
                .append(Integer.toHexString(b));
        return sb.toString();
    }

    private void exportGraphAsPNG(VisualizationExportable visualizationExportData) {
        String securityText = SecurityBanner.getBannerText();

        Graph graph = (Graph)visualizationExportData.getVisualization();
        GraphSurface graphSurface = graph.getGraphSurface();

        ImagingRequest legendReq = new ImagingRequest();
        Map<String, String> visItems = graph.getVisItems();
        int[] legendSize = LegendExporter.getLegendSize(visItems);

        PNGImageComponent leg = new PNGImageComponent();
        legendReq.setWidth(legendSize[0]);
        legendReq.setHeight(legendSize[1]);
        leg.setData(LegendExporter.getLegendAsBase64(visItems,"Graph Legend:",  legendSize));
        legendReq.addComponent(leg);

        int modelWidth = graphSurface.asWidget().getOffsetWidth();
        int modelHeight = graphSurface.asWidget().getOffsetHeight();

        WebMain.injector.getVortex().execute(createZipCallback(), ExportActionsServiceProtocol.class).createGraphPNGWithLegend(
                                            graphSurface.getVizUuid(),
                                            exportable.getName(),
                                            legendReq,
                                            modelWidth,
                                            modelHeight,
                                            securityText);
    }

    /**
     *  Consider swapping the ExportImageSettings to ImageRequest to begin with..
     * @param request
     */
    private void setImagingRequestSize(ImagingRequest request) {
        if(exportSettings.getImageSettings() == null)
            return;

        ExportImageSettings exportImageSettings = exportSettings.getImageSettings();
        if(exportImageSettings.getDesiredHeight() <= 0 || exportImageSettings.getDesiredWidth() <= 0)
            return;

        request.setHeight(exportImageSettings.getDesiredHeight());
        request.setWidth(exportImageSettings.getDesiredWidth());

        for (ImageComponent imageComponent : request.getComponents()) {
            if(imageComponent instanceof SVGImageComponent) {
                imageComponent.setWidth(exportImageSettings.getDesiredWidth());
                imageComponent.setHeight(exportImageSettings.getDesiredHeight());
            }
        }
    }

    private void exportCSV() {
        boolean useSelectionOnly = exportSettings.isUseSelectionOnly();

//        if(exportable.getVisualization() instanceof MapPresenter){
//            WebMain.injector.getVortex().execute(downloadCallback, ExportActionsServiceProtocol.class).exportMapCSV(exportable.getVisualization().getUuid(), useSelectionOnly);
//        }else {
            WebMain.injector.getVortex().execute(downloadCallback, ExportActionsServiceProtocol.class).createCSV(exportable.getDataViewUuid(), exportable.getData(), useSelectionOnly);
//        }
    }
}
