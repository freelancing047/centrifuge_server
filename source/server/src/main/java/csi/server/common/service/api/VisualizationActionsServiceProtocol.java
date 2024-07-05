/** 
 *  Copyright (c) 2008-2013 Centrifuge Systems, Inc. 
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

import csi.server.common.dto.FieldConstraints;
import csi.server.common.dto.FilterConstraintsRequest;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.FieldDef;
import csi.server.common.model.visualization.VisualizationDef;
import csi.server.common.model.worksheet.WorksheetDef;
import csi.shared.gwt.vortex.VortexService;

import java.util.List;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public interface VisualizationActionsServiceProtocol extends VortexService {

    public void addVisualization(VisualizationDef viz, String dvUuid, String worksheetUuid) throws CentrifugeException;

    public void addWorksheet(WorksheetDef worksheet, String dvUuid) throws CentrifugeException;

    public void removeWorksheet(WorksheetDef worksheet, String dvUuid) throws CentrifugeException;

    /**
     * Removes all references of the visualization within the given dataview
     * @param dvUuid the uuid of the data view
     * @param vizUuid the uuid of the visualization.
     */
    public void deleteVisualization(String dvUuid, String vizUuid) throws CentrifugeException;

    public void saveSettings(VisualizationDef viz, String dvuuid, Boolean isStructural) throws CentrifugeException;

    public void saveSettings(VisualizationDef viz, String dvuuid, Boolean isStructural, Boolean clearTransient) throws CentrifugeException;

    public List<FieldConstraints> getFilterConstraints(FilterConstraintsRequest request, FieldDef selectedItem) throws CentrifugeException;

    public VisualizationDef getVisualization(String dvUuid, String vizUuid);

    public void setWorksheetName(String dvUuid, String uuid, String newName) throws CentrifugeException;

    public void setWorksheetColor(String dvUuid, String uuid, Integer color) throws CentrifugeException;
    
    //public VisualizationDef moveVisualization(String dvUuid, String vizUuid, String worksheetUuid) throws CentrifugeException;

	public VisualizationDef copyVisualization(String dvUuid, String vizUuid, String worksheetUuid, boolean isMove) throws CentrifugeException;
	
    public Boolean isSelectionAvailable(String dvUuid, VisualizationDef visualizationDef);
}
