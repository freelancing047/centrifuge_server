/** 
 *  Copyright (c) 2008 Centrifuge Systems, Inc. 
 *  All rights reserved. 
 *   
 *  This software is the confidential and proprietary information of 
 *  Centrifuge Systems, Inc. ("Confidential Information").  You shall 
 *  not disclose such Confidential Information and shall use it only
 *  in accordance with the terms of the license agreement you entered 
 *  into with Centrifuge Systems.
 *
 **/
package csi.server.common.service.api;

import java.util.List;
import java.util.Map;

import csi.server.common.dto.Response;
import csi.server.common.dto.graph.gwt.EdgeListDTO;
import csi.server.common.dto.graph.gwt.NodeListDTO;
import csi.server.common.enumerations.AclResourceType;
import csi.server.common.model.visualization.VisualizationDef;
import csi.shared.core.imaging.ImagingRequest;
import csi.shared.gwt.vortex.VortexService;
import csi.server.common.model.Resource;


/**
 * Creates a file for downloading and returns a token, which is used in a subsequent request
 * to download the file.
 *
 * @author Centrifuge Systems, Inc.
 *
 */
public interface ExportActionsServiceProtocol extends VortexService {

    public String createGraphPNGWithLegend(String vizuuid, String prefixFileName,
                                    ImagingRequest legend, int viewWdith, int viewHeight,
                                           String securityText);

    public String createANX(String vizuuid);

    public String createPNG(ImagingRequest request);

    public String createCSV(String dvUuid, VisualizationDef visualizationDef, boolean useSelectionOnly);

    public String createExportName(AclResourceType dataType, String resourceId, List<String> order);

    public String createChartTableCSV(List<String>headers, List<List<String>> data);

    public String createPDF(String dataViewUuid, VisualizationDef visualizationDef);

    public String createGraphPNG(String vizuuid, int desiredWidth, int desiredHeight);

    public void destroyDownload(String tokenIn);

    public Response<String, String> exportTheme(String themeId);

    public Response<String, String> exportDataView(String uuidIn);

    public Response<String, String> exportTemplate(String uuidIn);

    public Response<String, String> exportSupportingResources(boolean includeThemesIn, boolean includeIconsIn,
                                                              boolean includeMapsIn);

    public Response<String, String> exportThemes(List<String> uuidListIn, boolean includeIconsIn, boolean includeMapsIn);

    public Response<String, String> exportDataViews(List<String> uuidListIn, boolean includeThemesIn,
                                                    boolean includeIconsIn, boolean includeMapsIn, boolean includeDataIn);

    public Response<String, String> exportTemplates(List<String> uuidListIn, boolean includeThemesIn,
                                                    boolean includeIconsIn, boolean includeMapsIn);

    public String exportNodesList(List<NodeListDTO> list,  List<String> visibleColumns);

    public String exportLinksList(List<EdgeListDTO> list,  List<String> visibleColumns);

//    String exportMapCSV(String uuidIn, boolean selectionOnly);

    public String exportZipPNG(List<ImagingRequest> req);
}
