/**
 * Copyright (c) 2008 Centrifuge Systems, Inc.
 * All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of
 * Centrifuge Systems, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered
 * into with Centrifuge Systems.
 **/
package csi.server.business.service.matrix;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.server.business.selection.torows.MatrixSelectionToRowsConverter;
import csi.server.business.selection.toselection.RowsToSelectionConverter;
import csi.server.business.selection.toselection.RowsToSelectionConverterFactory;
import csi.server.business.service.ColorActionsService;
import csi.server.business.service.FilterActionsService;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.visualization.VisualizationDef;
import csi.server.common.model.visualization.chart.ChartCriterion;
import csi.server.common.model.visualization.matrix.Axis;
import csi.server.common.model.visualization.matrix.MatrixSettings;
import csi.server.common.model.visualization.matrix.MatrixViewDef;
import csi.server.common.model.visualization.selection.NullSelection;
import csi.server.common.model.visualization.selection.Selection;
import csi.server.common.service.api.MatrixActionsServiceProtocol;
import csi.server.dao.CsiPersistenceManager;
import csi.server.util.sql.SQLFactory;
import csi.shared.core.util.IntCollection;
import csi.shared.core.visualization.matrix.AxisLabel;
import csi.shared.core.visualization.matrix.Cell;
import csi.shared.core.visualization.matrix.MatrixCategoryRequest;
import csi.shared.core.visualization.matrix.MatrixCategoryResponse;
import csi.shared.core.visualization.matrix.MatrixDataRequest;
import csi.shared.core.visualization.matrix.MatrixDataResponse;
import csi.shared.core.visualization.matrix.MatrixMetrics;
import csi.shared.core.visualization.matrix.MatrixSearchRequest;
import csi.shared.core.visualization.matrix.MatrixSelectionRequest;
import csi.shared.core.visualization.matrix.MatrixWrapper;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class MatrixActionsService implements MatrixActionsServiceProtocol {
   private static final Logger LOG = LogManager.getLogger(MatrixActionsService.class);

   private final String COUNT_STAR_MEASURE_LABEL = "COUNT(*)";

    @Inject
    private SQLFactory sqlFactory;

    @Inject
    private FilterActionsService filterActionsService;

    @Inject
    private ColorActionsService colorActionsService;

    public MatrixWrapper getCellsInRegionForSelection(MatrixDataRequest req){
        MatrixDataResponse dataResponse = new MatrixDataResponse(Math.min(req.getStartX(), req.getEndX()), Math.max(req.getStartX(), req.getEndX()), Math.min(req.getStartY(), req.getEndY()), Math.max(req.getStartY(), req.getEndY()));
        MatrixViewDef viewDef = CsiPersistenceManager.findObject(MatrixViewDef.class, req.getVizUuid());
        // this will follow the policy
        MatrixData data = MatrixDataService.getDataForSelection(req, viewDef);
        dataResponse.setCells(data.getAllCells());
        dataResponse.setSummary(data.isSummary());
        dataResponse.setFullMatrix(false);

        return new MatrixWrapper(null, dataResponse, null);
    }


    /**
     * wrapped getMatrixRegion with MDReq
     * @param req
     * @return
     */
    public MatrixWrapper getRegion(MatrixDataRequest req){
       LOG.trace("(" + req.getStartX() + ", " + req.getStartY() + ") (" + req.getEndX() + ", " + req.getEndY() + ")");
        MatrixViewDef viewDef = CsiPersistenceManager.findObject(MatrixViewDef.class, req.getVizUuid());
        // this will follow the policy
        MatrixData data = MatrixDataService.getData(req, viewDef);

        MatrixCategoryResponse response = this.buildCategoryResponse(viewDef.getName(), data, viewDef.getMatrixSettings());
        MatrixDataResponse dataResponse = new MatrixDataResponse(Math.min(req.getStartX(), req.getEndX()), Math.max(req.getStartX(), req.getEndX()), Math.min(req.getStartY(), req.getEndY()), Math.max(req.getStartY(), req.getEndY()));
        dataResponse.setCells(data.getAllCells());
        dataResponse.setSummary(data.isSummary());
        dataResponse.setFullMatrix(false);

        LOG.trace("returning " + dataResponse.getCells().size() + " cells for the region req");
        return new MatrixWrapper(data.getMatrixMetrics(), dataResponse, response);
    }

    @Override
    public MatrixWrapper getCellsInRegion(MatrixDataRequest req) {
        MatrixWrapper region = getRegion(req);
        return new MatrixWrapper(null, region.getData(), null);
    }

    @Override
    public MatrixMetrics getViewMetrics(MatrixDataRequest req) {
        MatrixViewDef viewDef = CsiPersistenceManager.findObject(MatrixViewDef.class, req.getVizUuid());
        // force no summary.
        req.setSummarizationPolicy(MatrixDataRequest.REQUEST_SUMMARIZATION_POLICY.DISALLOW_SUMMARY);
        // this will follow the policy
        MatrixData data = MatrixDataService.getData(req, viewDef);

        if((data.getMatrixMetrics().getAxisXCount() == req.getEndX()) && (data.getMatrixMetrics().getAxisYCount() == req.getEndY()) && (req.getStartY() == 0) && (req.getStartX() == 0)){

            return data.getMatrixMetrics();
        }

        // process...
        double x = req.getStartX();
        double y = req.getStartY();
        double height = req.getEndY();
        double width =  req.getEndX();

        MatrixMetrics metrics = new MatrixMetrics();

        boolean noCellsInRegion = true;

        for(Cell c : data.getAllCells()){
            if((c.getX() >= x) && (c.getX() <= width) && (c.getY() >= y) && (c.getY() <= height)){
                noCellsInRegion = false;
                metrics.setMaxValue(Math.max(metrics.getMaxValue(), c.getValue().doubleValue()));
                metrics.setMinValue(Math.min(metrics.getMinValue(), c.getValue().doubleValue()));
                metrics.setTotalCells(metrics.getTotalCells() + 1);
            }
        }

        // if there are no cells in the view
        if(noCellsInRegion) {
            metrics.setMaxValue(0);
            metrics.setMinValue(0);
        }

        int xCount = 0, yCount = 0;
        for (AxisLabel axisLabel : data.getxCategories()) {
            if((axisLabel.getOrdinalPosition() >= x) && (axisLabel.getOrdinalPosition() <= width)){
                xCount++;
            }
        }
        for (AxisLabel axisLabel : data.getyCategories()) {
            if((axisLabel.getOrdinalPosition() >= y) && (axisLabel.getOrdinalPosition() <= height)){
                yCount++;
            }
        }


        metrics.setAxisXCount(xCount);
        metrics.setAxisYCount(yCount);

        return metrics;
    }

    /**
     * @param viewDef
     * @param dvUuid
     * @param x1 x start
     * @param y1 y start
     * @param x2 x end
     * @param y2 y end
     * @return
     */
    public MatrixWrapper getMatrixRegion(MatrixViewDef viewDef, String dvUuid, int x1, int y1, int x2, int y2){
        //TODO: remove timing.
        long startTime = System.nanoTime();
        MatrixData data = MatrixDataService.getData(viewDef, dvUuid, x1,y1,x2,y2);

        if(data.getTotalCount() == 0){
            return new MatrixWrapper();
        }

        // no need its set in getData;
        MatrixSettings settings = viewDef.getMatrixSettings();
        data.createScaleImage(settings, MatrixDataService.getColorActionsService());
        data.setLimitExceeded(false);

        // this should set the extents correctly... meaning that we'll b able to merge on the clientside..
        MatrixCategoryResponse response = this.buildCategoryResponse(viewDef.getName(), data, settings);
        MatrixDataResponse dataResponse = new MatrixDataResponse(Math.min(x1, x2), Math.max(x1,x2), Math.min(y1,y2), Math.max(y1,y2));
        dataResponse.setCells(data.getAllCells());
        dataResponse.setSummary(data.isSummary());
        dataResponse.setFullMatrix(false);

        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1000000 ;  //divide by 1000000 to get milliseconds.
        LOG.trace("getPartial complete. Duration: " + duration);

        return new MatrixWrapper(data.getMatrixMetrics(), dataResponse, response);
    }

    @Override
    public MatrixWrapper selectCells(List<ChartCriterion> criteria, String vizUuid,  String dvUuid) {
        MatrixViewDef viewDef = CsiPersistenceManager.findObject(MatrixViewDef.class, vizUuid);
        MatrixData data = MatrixDataService.getMatrixData(viewDef, dvUuid, false);

        // this will do top/bottom#, top/bottom %
        data.doFilter(viewDef.getMatrixSettings(), criteria, true);
        MatrixCriteriaFilter sieve = new MatrixCriteriaFilter();
        MatrixData filteredData = sieve.applyFilter(data, criteria);
        MatrixWrapper wrapper = new MatrixWrapper();
        MatrixDataResponse dataResponse = this.buildMatrixDataResponse(filteredData, true);
        wrapper.setData(dataResponse);
        return wrapper;
    }

    /**
     *
     * @param viewDef
     * @param dvUuid
     * @return
     */
    public MatrixWrapper getMatrix(MatrixViewDef viewDef, String dvUuid) {
        MatrixWrapper mw;
        long startTime = System.nanoTime();
        MatrixData data = getMatrixData(viewDef, dvUuid);
        MatrixSettings settings = viewDef.getMatrixSettings();

        if(data.getTotalCount() == 0){
            return new MatrixWrapper();
        }

        MatrixMetrics metrics = data.getMatrixMetrics();
        MatrixCategoryResponse categoryResp = this.buildCategoryResponse(viewDef.getName(), data, settings);
        MatrixDataResponse dataResponse = this.buildMatrixDataResponse(data, true);
        dataResponse.setFullMatrix(true);

        mw = new MatrixWrapper(metrics, dataResponse, categoryResp);
        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1000000 ;  //divide by 1000000 to get milliseconds.
        LOG.debug("Get full matrix complete. Duration: " + duration);

        return mw;
    }

    @Override
    public IntCollection getSelectionIdsFromCache(MatrixSelectionRequest request) {
        int[] ids = MatrixDataService.getIdsForCategory(request);
        IntCollection idCollection = new IntCollection();
        idCollection.addAll(ids);
        return idCollection;
    }

    public MatrixCategoryResponse getAxisCategoriesForSearch(MatrixSearchRequest request, Axis axis){
        MatrixCategoryResponse resp = new MatrixCategoryResponse();
        MatrixViewDef viewDef = CsiPersistenceManager.findObject(MatrixViewDef.class, request.getVizUuid());
        MatrixData data = MatrixDataService.getMatrixData(viewDef, request.getDvUuid(), false);

        List<AxisLabel> xLabels = new ArrayList<AxisLabel>();
        List<AxisLabel> yLabels = new ArrayList<AxisLabel>();

        String xPrefix = "";
        String yPrefix = "";

        if(!request.getxQuery().isEmpty()) {
            xPrefix = request.getxQuery().toLowerCase();
        }
        if(!request.getyQuery().isEmpty()) {
            yPrefix = request.getyQuery().toLowerCase();
        }

        switch (axis){
            case X:
                for (AxisLabel axisLabel : data.getxCategories()) {
                    if(axisLabel.getLabel().toLowerCase().startsWith(xPrefix)){
                        xLabels.add(axisLabel);
                    }
                }
                break;
            case Y:
                for (AxisLabel axisLabel : data.getyCategories()) {
                    if(axisLabel.getLabel().toLowerCase().startsWith(yPrefix)){
                        yLabels.add(axisLabel);
                    }
                }

                break;
        }

        resp.setCategoryX(xLabels);
        resp.setCategoryY(yLabels);

        return resp;

    }

    /**
     * this about how are going to be doing by value search
     * @param request
     * @return
     */
    @Override
    public MatrixWrapper search(MatrixSearchRequest request) {
        MatrixViewDef viewDef = CsiPersistenceManager.findObject(MatrixViewDef.class, request.getVizUuid());
        MatrixData data = MatrixDataService.getMatrixData(viewDef, request.getDvUuid(), false);

        MatrixWrapper wrap;

        String x = request.getxQuery().toLowerCase();
        String y = request.getyQuery().toLowerCase();

        int foundXIndex = -1;
        int foundYindex = -1;

        boolean X_Y = !x.isEmpty() && !y.isEmpty();
        boolean ONLY_X = !x.isEmpty();
        boolean ONLY_Y = !y.isEmpty();

        if(X_Y){
            for (AxisLabel axisLabel : data.getxCategories()) {
                if(axisLabel.getLabel().equalsIgnoreCase(x)){
                    foundXIndex = axisLabel.getOrdinalPosition();
                }
            }
            for (AxisLabel axisLabel : data.getyCategories()) {
                if(axisLabel.getLabel().equalsIgnoreCase(y)){
                    foundYindex = axisLabel.getOrdinalPosition();
                    break;
                }
            }

            wrap =  new MatrixWrapper(null, new MatrixDataResponse(foundXIndex - request.getCONTEXT_WIDTH(), foundXIndex + request.getCONTEXT_WIDTH(), foundYindex - request.getCONTEXT_WIDTH(), foundYindex + request.getCONTEXT_WIDTH()), new MatrixCategoryResponse());

        }else if(ONLY_X){
            for (AxisLabel axisLabel : data.getxCategories()) {
                if(axisLabel.getLabel().equalsIgnoreCase(x)){
                    foundXIndex = axisLabel.getOrdinalPosition();
                    break;
                }
            }

            wrap = new MatrixWrapper(null, new MatrixDataResponse(foundXIndex - request.getCONTEXT_WIDTH(), foundXIndex + request.getCONTEXT_WIDTH(), 0, data.getMaxY()), new MatrixCategoryResponse());

        }else if(ONLY_Y){
            for (AxisLabel axisLabel : data.getyCategories()) {
                if(axisLabel.getLabel().equalsIgnoreCase(y)){
                    foundYindex = axisLabel.getOrdinalPosition();
                    break;
                }
            }
            wrap = new MatrixWrapper(null, new MatrixDataResponse(0, data.getMaxX(), foundYindex - request.getCONTEXT_WIDTH(), foundYindex + request.getCONTEXT_WIDTH()), new MatrixCategoryResponse());

        }else{
            wrap = new MatrixWrapper();
        }

        if((foundXIndex == -1) && (foundYindex == -1)){
            wrap.setEmpty(true);
        }

        return wrap;
    }

    @Override
    public Selection doQuickSortFilter(MatrixViewDef viewDef, String dvUuid) {
        DataView dv = CsiPersistenceManager.findObject(DataView.class, dvUuid);
        // convert selection to rows to preserve it
        MatrixSelectionToRowsConverter selToRow = new MatrixSelectionToRowsConverter(dv, viewDef);
        Set<Integer> oldSelection = selToRow.convertToRows(viewDef.getSelection(), false);

        invalidateCache(viewDef.getUuid());
        createCache(viewDef, dv);

        // convert rows back into cells
        RowsToSelectionConverter rowsToSel= new RowsToSelectionConverterFactory(dv, viewDef).create();
        Selection newSelection = oldSelection.isEmpty() ? NullSelection.instance : rowsToSel.toSelection(oldSelection);

        //update viewDef so client picks up the latest sort on reload.
        CsiPersistenceManager.merge(viewDef);

        return newSelection;
    }


    private MatrixMetrics buildMatrixMetrics(MatrixData data){
        MatrixMetrics metrics = data.getMatrixMetrics();
        metrics.setAxisXCount(data.getxCategories().size());
        metrics.setAxisYCount(data.getyCategories().size());

        return metrics;

    }

    private MatrixDataResponse buildPartialMatrixDataResponse(MatrixDataResponse resp, MatrixData data){
            MatrixDataResponse ret = buildMatrixDataResponse(data, false);
            ret.updateWindowInfo(resp);

            return ret;
    }



    private MatrixDataResponse buildMatrixDataResponse(MatrixData data, boolean isFullMatrix){
        MatrixDataResponse dataResponse = new MatrixDataResponse(data.getMinX(), data.getMaxX(), data.getMinY(), data.getMaxY());
        dataResponse.setMaxX(data.getMaxX());
        dataResponse.setMaxY(data.getMaxY());

        dataResponse.setCells(data.getAllCells());
        dataResponse.setSummary(data.isSummary());

        dataResponse.setFullMatrix(isFullMatrix);

        return dataResponse;

    }


    private MatrixCategoryResponse buildCategoryResponse(String name, MatrixData data, MatrixSettings settings){
        MatrixCategoryResponse categoryResp = new MatrixCategoryResponse();


        categoryResp.setMainTitle(name);
        categoryResp.setCategoryX(data.getxCategories());
        categoryResp.setCategoryY(data.getyCategories());
        categoryResp.setAxisXLabel(settings.getAxisX().get(0).getComposedName());
        categoryResp.setAxisYLabel(settings.getAxisY().get(0).getComposedName());
        categoryResp.setMeasureLabel(settings.isUseCountForMeasure() ? COUNT_STAR_MEASURE_LABEL : settings.getMatrixMeasureDefinition().getComposedName());
        categoryResp.setRangeImage(data.getRangeImageAsString());


        categoryResp.setSummaryX(data.isSummary());
        categoryResp.setSummaryY(data.isSummary());



        return categoryResp;

    }


    public void invalidateCache(String visUUid){
        MatrixDataService.invalidateMatrixData(visUUid);
    }


    /**
     * Will query the matrix data, build the MatrixData object, and return either the full object or the summary view of it.
     *
     *
     * @param viewDef Instance of @{{@link MatrixViewDef}} for settings of viz
     * @param dataViewUuid - DV id
     * @return
     */
    private MatrixData  getMatrixData(MatrixViewDef viewDef, String dataViewUuid) {
        if (LOG.isDebugEnabled()) {
         LOG.debug("init: " + Objects.isNull(viewDef) + " || " + dataViewUuid.isEmpty());
      }
        if ((viewDef == null) || ((dataViewUuid != null) && dataViewUuid.isEmpty())) {
            // honestly we should error and this is terrible.
            return new MatrixData();
        }

        return MatrixDataService.getMatrixData(viewDef, dataViewUuid, true);
    }


    @PostConstruct
    public void postConstruct() {
        MatrixDataService.postConstruct(sqlFactory, filterActionsService, colorActionsService);
    }

    public void onFailure(Throwable caught) {
       LOG.error("Failed with exception: " + caught.getMessage(), caught);
        if (LOG.isDebugEnabled()) {
            caught.printStackTrace();
        }
    }

    @Override
    public MatrixMetrics getFullMatrixMetrics(String visUuid) {
        if(visUuid.isEmpty()){
            return new MatrixMetrics();
        }
        return MatrixDataService.getMetricsForMatrix(visUuid);
    }

    /**
     *  Used to get categories for the matrix,
     * @param request ids of viz
     * @return X/Y category labels and stats for a matrix instance
     */
    @Deprecated
    @Override
    public MatrixCategoryResponse getCategories(MatrixCategoryRequest request) {
        final MatrixViewDef viewDef = CsiPersistenceManager.findObject(MatrixViewDef.class, request.getVizUuid());
        MatrixCategoryResponse response = new MatrixCategoryResponse();
        MatrixData data = getMatrixData(viewDef, request.getDvUuid());

        response.setCategoryX(data.getxCategories());
        response.setCategoryY(data.getyCategories());
//        response.setOverviewImage(data.getOverviewImageAsString());
        response.setRangeImage(data.getRangeImageAsString());

        // if the limit is exceeded, return with the flags.
        if (data.isLimitExceeded()) {
            // clean out the lists so we don't waste time sending.
            response.setCategoryX(new ArrayList<AxisLabel>());
            response.setCategoryY(new ArrayList<AxisLabel>());

            response.setLimitExceeded(data.isLimitExceeded());
            response.setCount(data.getTotalCount());
            return response;
        }

        MatrixSettings settings = viewDef.getMatrixSettings();
        response.setAxisXLabel(settings.getAxisX().get(0).getComposedName());
        response.setAxisYLabel(settings.getAxisY().get(0).getComposedName());
        if (settings.isUseCountForMeasure()) {
            response.setMeasureLabel("COUNT(*)");
        } else {
            response.setMeasureLabel(settings.getMatrixMeasureDefinition().getComposedName());
        }

        response.setMainTitle(viewDef.getName());

        return response;
    }

    @Deprecated
    @Override
    public MatrixMetrics getMetrics(MatrixViewDef viewDef, String dataViewUuid) {
        // This is the first method to be called on a visualization reload. If the visualization is listening to
        // broadcasts, we don't want to hit the cache. So we invalidate the cache here.
        //if (viewDef.isBroadcastListener()) {
        //FIXME: CEN-1578: Old cache was being returned when not listening for broadcasts.
        //TODO: CEN-1682: re-implement cache correctly.
        MatrixDataService.invalidateMatrixData(viewDef.getCacheKey());
        //}

        MatrixData data = getMatrixData(viewDef, dataViewUuid);

        data.getMatrixMetrics().setAxisXCount(data.getxCategories().size());
        data.getMatrixMetrics().setAxisYCount(data.getyCategories().size());


        return data.getMatrixMetrics();
    }

    public void createCache(VisualizationDef viz, DataView dataView) {
        if(viz instanceof MatrixViewDef){
            MatrixViewDef viewDef = (MatrixViewDef) viz;
            MatrixDataService.getMatrixData(viewDef, dataView.getUuid(), false);
        }
    }


    //    @Deprecated
//    @Override
//    public MatrixDataResponse getData(MatrixDataRequest request) {
//        MatrixViewDef viewDef = CsiPersistenceManager.findObject(MatrixViewDef.class, request.getVizUuid());
//        MatrixDataResponse response = new MatrixDataResponse(request);
//        MatrixData data = MatrixDataService.getMatrixData(viewDef, request.getDvUuid(), true);
//
//        // If matrix data is too large - we don't bother with the rest.
//        if (data.isLimitExceeded()) {
//            response.setLimitReached(true);
//            response.setTotalCount(data.getTotalCount());
//            return response;
//        }
//
//        response.setCells(data.get(request.getStartX(), request.getStartY(), request.getEndX(), request.getEndY()));
//        response.setSummary(data.isSummary());
//
//
//        List<AxisLabel> xAxis = data.getxCategories();
//        if (xAxis.size() == 1) {
//            xAxis.add(new AxisLabel(""));
//            xAxis.add(0, new AxisLabel(""));
//
//            for (Cell cell : response.getCells()) {
//                cell.setX(1);
//            }
//        }
//        List<AxisLabel> yAxis = data.getyCategories();
//        if (yAxis.size() == 1) {
//            yAxis.add(new AxisLabel(""));
//            yAxis.add(0, new AxisLabel(""));
//
//            for (Cell cell : response.getCells()) {
//                cell.setY(1);
//            }
//        }
//
//        return response;
//    }


}
