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

import csi.server.common.model.visualization.chart.ChartCriterion;
import csi.server.common.model.visualization.matrix.Axis;
import csi.server.common.model.visualization.matrix.MatrixViewDef;
import csi.server.common.model.visualization.selection.Selection;
import csi.shared.core.util.IntCollection;
import csi.shared.core.visualization.matrix.*;
import csi.shared.gwt.vortex.VortexService;

import java.util.List;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public interface MatrixActionsServiceProtocol extends VortexService {


     MatrixMetrics getFullMatrixMetrics(String visUuid);

     MatrixWrapper getCellsInRegionForSelection(MatrixDataRequest req);

    /**
     * @param request
     * @return Category x and y values.
     */
     MatrixCategoryResponse getCategories(MatrixCategoryRequest request);

    /**
     * @param viewDef
     * @param dataViewUuid
     * @return Metrics (count of ordinals in x and y axis and number of cells).
     */
     MatrixMetrics getMetrics(MatrixViewDef viewDef, String dataViewUuid);


    /**
     * Retuns the matrix wrapper that contains everything you will need to load the matrix.
     * @param viewDef
     * @param dataViewUuid
     * @return
     */
     MatrixWrapper getMatrix(MatrixViewDef viewDef, String dataViewUuid);


    /**
     * Will return an array of ids for the category requested on a given axis.
     * @param request
     * @return
     */
    public IntCollection getSelectionIdsFromCache(MatrixSelectionRequest request);


     MatrixCategoryResponse getAxisCategoriesForSearch(MatrixSearchRequest request, Axis axis);
     MatrixWrapper search(MatrixSearchRequest request);

     Selection doQuickSortFilter(MatrixViewDef viewDef, String dvUuid);

    /**
     * send in the viewport and get the metrics for that view.
     * @param req
     * @return
     */
     MatrixMetrics getViewMetrics(MatrixDataRequest req);

    /**
     * wrapped version ofg get summarycell which gets cells fro region
     * @param req
     * @return
     */
     MatrixWrapper getRegion(MatrixDataRequest req);

     MatrixWrapper getCellsInRegion(MatrixDataRequest req);


     MatrixWrapper getMatrixRegion(MatrixViewDef viewDef, String dvUuid, int x1, int y1, int x2, int y2);

     MatrixWrapper selectCells(List<ChartCriterion> criteria, String vizUuid, String dvUuid);



     void invalidateCache(String visUUid);

}