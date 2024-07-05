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
package csi.client.gwt.viz.matrix;

import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;
import com.google.gwt.i18n.client.NumberFormat;
import csi.client.gwt.WebMain;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.model.visualization.matrix.MatrixCategoryDefinition;
import csi.server.common.model.visualization.matrix.MatrixMeasureDefinition;
import csi.server.common.model.visualization.matrix.MatrixSettings;
import csi.server.common.model.visualization.selection.MatrixCellSelection;
import csi.shared.core.visualization.matrix.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class MatrixModel {

    private static final int MIN_VIEW_WIDTH = 1;
    private String visualizationUuid;

    public void setVisualizationUuid(String visualizationUuid) {
        this.visualizationUuid = visualizationUuid;
    }

    public String getVisualizationUuid() {
        return visualizationUuid;
    }


    public enum Axis{
        X, Y;
    }
    //    private int x,y,height, width;
    private MatrixSettings settings;
    private MatrixMetrics metrics;
    private MatrixCategoryResponse categoryResponse;
    private MatrixDataResponse matrixDataResponse;
    private Set<Cell> selectedCells = new HashSet<>();
    private Table<Integer,Integer,Boolean> summaryMatrixCells;

    private boolean summary = false;

    private double x;
    private double y;
    private double height;
    private double width;


    public List<AxisLabel> getCategoryX() {
        return categoryResponse.getCategoryX();
    }
    public List<AxisLabel> getCategoryY() {
        return categoryResponse.getCategoryY();
    }

    public MatrixMetrics getMetrics() {
        return metrics;
    }

    public void setMetrics(MatrixMetrics result) {
        this.metrics = result;
    }


    public boolean isSummary() {
        return summary;
    }

    public void setSummary(boolean summary) {
        this.summary = summary;
    }

    /**
    *              (sMax-sMin)(val - vMin)
    *        f(x) = ----------------------  + sMin
    *                    vMax - vMin
    * @param cellValue double
    * @return double
    */
    public double getScaledBubbleRadius(double cellValue) {
        double min = metrics.getMinValue();
        double max = metrics.getMaxValue();
        int scaleMax = settings.getMatrixMeasureDefinition().getMeasureScaleMax();
        int scaleMin = settings.getMatrixMeasureDefinition().getMeasureScaleMin();

        if (Math.abs(min - max) < 0.001) {
            return scaleMin;
        } else {
            double radius = (((scaleMax - scaleMin)*(cellValue - min))/(max - min)) + scaleMin;
            return radius;
        }
    }

    /**
     * this will be missing the dvuuid because i can't get it from the model
     * @return
     */
    MatrixDataRequest getViewMetricsRequest(){
        double x = Math.ceil(getX());
        double y = Math.ceil(getY());
        double height = Math.floor(getY() + getHeight());
        double width =  Math.floor(getX() + getWidth());

        MatrixDataRequest req = new MatrixDataRequest();
        req.setVizUuid(getVisualizationUuid());
        req.setExtent((int)x, (int)width, (int)y, (int)height);

        return req;
    }


    public void clearSelectedCells(){
        selectedCells.clear();
    }


    /**
     *
     * Applies MatrixRowsSelection(a list of all selected ids) to
     * the currently available cells on the client.
     *
     * @param selection
     */
    public void updateVisualSelection(MatrixCellSelection selection) {
        selectedCells.clear();

        for (MatrixCellSelection.CellPosition cellPosition : selection.getSelectedCells()) {
            Cell c = new Cell();
            c.setX(cellPosition.getX());
            c.setY(cellPosition.getY());
            c.setValue(-1);

            selectedCells.add(c);
        }
    }

    public void setResult(MatrixCategoryResponse response, MatrixSettings matrixSettings) {
        categoryResponse = response;
        this.settings = matrixSettings;
    }

    public void setCategoryResponseAndSettings(MatrixCategoryResponse response, MatrixSettings matrixSettings) {
        categoryResponse = response;
        this.settings = matrixSettings;
    }

    /**
     * Adds/Removes a cell from a list of selected cells
     * @param cell
     * @param selected
     */
    public void modifySelection(Cell cell, boolean selected) {
        if (selected) {
            selectedCells.add(cell);
        } else {
            selectedCells.remove(cell);
        }
    }

    /**
     * WARNING: This collection can contain "fabricated" cells (only category values populated) and therefore should not
     * be used for anything other than selection state analysis.
     */
    public Set<Cell> getSelectedCells() {
        return selectedCells;
    }

    public void selectCell(Cell cell){
        this.getSelectedCells().add(cell);
    }

    public void deselectCell(Cell cell){
        this.getSelectedCells().remove(cell);
    }

    public String getXAxisTitle(){

        try {
            for (MatrixCategoryDefinition matrixCategoryDefinition : getSettings().getAxisCategories()) {
                if (matrixCategoryDefinition.getAxis() == csi.server.common.model.visualization.matrix.Axis.X) {
                    return matrixCategoryDefinition.getComposedName();
                }
            }
        }catch(Exception e){

        }
        return "";
    }

    public String getYAxisTitle(){
        try{
            for (MatrixCategoryDefinition matrixCategoryDefinition : getSettings().getAxisCategories()) {
                if (matrixCategoryDefinition.getAxis() == csi.server.common.model.visualization.matrix.Axis.Y) {
                    return matrixCategoryDefinition.getComposedName();
                }
            }
        }catch(Exception e){

        }
        return "";
    }

    public MatrixDataRequest getAxisSelectionRequest(int index, Axis axis){
        MatrixDataRequest dr = new MatrixDataRequest();
        dr.setVizUuid(this.visualizationUuid);
        dr.setDvUuid(WebMain.injector.getMainPresenter().getDataViewPresenter(true).getUuid());
        dr.setSummarizationPolicy(MatrixDataRequest.REQUEST_SUMMARIZATION_POLICY.DISALLOW_SUMMARY);

        switch (axis) {
            case X:
                int binCount  = getBinCountForAxis((int) this.getWidth());
                int width = index + binCount - 1;

                dr.setExtent(index, width, 0, metrics.getAxisYCount());
                break;
            case Y:
                int height = index + getBinCountForAxis((int) this.getHeight()) - 1;
                dr.setExtent(0, metrics.getAxisXCount(), index, height);
                break;
        }

        return dr;
    }

    public void setupSummarySelection(){
        summaryMatrixCells = null;
        int minX = (int) Math.floor(getX());
        int minY = (int) Math.floor(getY());

        int xBucketSize = getBinCountForAxis((int) getWidth());
        int yBucketSize = getBinCountForAxis((int) getHeight());

        int xSize = (int) (getWidth() / xBucketSize + 1);
        int ySize = (int) (getHeight()/ yBucketSize + 1);


//        summaryMatrixCells = new boolean[xSize][ySize];
        summaryMatrixCells = TreeBasedTable.create();

        for (Cell c : getSelectedCells()) {
            int x = (c.getX() - minX) / xBucketSize;
            int y = (c.getY() - minY) / yBucketSize;

            if( x >= 0 &&  x < xSize && y >= 0 &&  y < ySize){
                summaryMatrixCells.put(x,y, Boolean.TRUE);
            }
        }
    }

    public boolean isSelected(Cell cell) {
        if(cell==null){
            return false;
        }
        if (isSummary()) {
            int minX = (int) Math.floor(getX());
            int minY = (int) Math.floor(getY());

            int xBucketSize = getBinCountForAxis((int) getWidth());
            int yBucketSize = getBinCountForAxis((int) getHeight());

            int yy = (cell.getY() - minY) / yBucketSize;
            int xx = (cell.getX() - minX) / xBucketSize;

            return summaryMatrixCells.get(xx,yy)==Boolean.TRUE;
        }else {
            return selectedCells.contains(cell);
        }
    }

    public String getOverviewImage() {
        return categoryResponse.getOverviewImage();
    }

    public String getColorScale() {
        return categoryResponse.getRangeImage();
    }

    public String getColor(double doubleValue) {
        return settings.getColorModel().getColor(doubleValue, metrics.getMinValue(), metrics.getMaxValue());
    }

    public MatrixSettings getSettings() {
        return settings;
    }

    public int getColorDivision() {
        return settings.getColorModel().getDivisions(true);
    }

    public MatrixDataResponse getMatrixDataResponse() {
        return matrixDataResponse;
    }

    public void setMatrixDataResponse(MatrixDataResponse matrixDataResponse) {
        this.matrixDataResponse = matrixDataResponse;
        if(first) {
            setX(matrixDataResponse.getStartX());
            setY(matrixDataResponse.getStartY());
            setWidth(matrixDataResponse.getEndX() - matrixDataResponse.getStartX());
            setHeight(matrixDataResponse.getEndY() - matrixDataResponse.getStartY());
            first = false;
        }
    }

    private boolean first = true;

    public MatrixCategoryResponse getCategoryResponse() {
        return categoryResponse;
    }

    public void setCategoryResponse(MatrixCategoryResponse categoryResponse) {
        this.categoryResponse = categoryResponse;
    }

    public void setSettings(MatrixSettings settings) {
        this.settings = settings;
    }

    public boolean isShowValue() {
        return settings.isShowLabel();
    }

    /**
     * @return inset on the main canvas. Its half max measure scale
     */
    public double getViewportPad() {
        if(getSettings() == null){
            return -1.0;
        }

        MatrixMeasureDefinition matrixMeasureDefinition = getSettings().getMatrixMeasureDefinition();
        double maxSize = Math.max(matrixMeasureDefinition.getMeasureScaleMax(), matrixMeasureDefinition.getMeasureScaleMin());
        return maxSize*.5;
    }

    public void setCurrentView(double x, double y, double width, double height) {
        // x is within our displayable range
        int maxX = this.getMetrics().getAxisXCount();
        int maxY = this.getMetrics().getAxisYCount();

        if (x < 0) {
            x = 0;
        }

        if (width > maxX) {
            width = maxX;
        }

        if (x + width > maxX) {
            x = maxX - width;
        }

        this.setX(x);

        this.setWidth(width);

        if (y < 0) {
            y = 0;
        }

        if (height > maxY) {
            height = maxY;
        }

        if (y + height > maxY) {
            y = maxY - height;
        }

        this.setY(y);
        this.setHeight(height);

        if (width < 2.9) {
            double dw = 2.9 - width;
            double w = 2.9;
            double xh = x - dw / 2.0;

            this.setWidth(w);
            this.setX(xh);
        }

        if (height < 2.9) {
            double dh = 2.9 - height;
            double h = 2.9;
            double yh = y - dh / 2.0;

            this.setHeight(h);
            this.setY(yh);
        }
    }

    public double getWidth() {
        return width;
    }

    private void setWidth(double width) {

        this.width = Math.max(width, 1.1);
    }

    public double getHeight() {
        return height;
    }

    private void setHeight(double height) {

        this.height = Math.max(height, 1.1);
    }

    public double getY() {
        return y;
    }

    private void setY(double y) {
        this.y = y;
    }

    public double getX() {
        return x;
    }

    private void setX(double x) {
        this.x = x;
    }

    // this is a clone of the width.
    public int getBinCountForAxis(int catLength){
        if(!isSummary()){
            return 1;
        }

        int matrixCellLimit = WebMain.getClientStartupInfo().getMatrixMaxCells();
        if(catLength <= (int) Math.sqrt(matrixCellLimit * 1.2)){
            return 1;
        }else{
            int binSize = catLength/(int) Math.sqrt(matrixCellLimit * 1.2);
            return binSize;
        }
    }

    public String formatAxisLabel(String value, MatrixModel.Axis axis) {
        CsiDataType dataType = getDataTypeForAxis(axis);
        String formattedLabel = "";
        value =  value.replace("\uE000", "");

        if(dataType == CsiDataType.Number && !this.isSummary()){
            Double v = null;
            try{
                v = new Double(value);
            } catch(Exception e){
                return null;
            }

            if (v  != null) {
                if (v % 1 == 0) {
                    NumberFormat format = NumberFormat.getFormat("0.0#");
                    formattedLabel = format.format(v);
                } else {
                    // should we add custom formatters to every field?
                    formattedLabel = value.toString();
                }
            }else{
                return "";
            }
        }else{
            // can add more specific configs..
            formattedLabel = value;
        }

        if(formattedLabel == null){
            formattedLabel = value;
        }

        return formattedLabel;
    }

    public CsiDataType getDataTypeForAxis(Axis axis){
        CsiDataType x = null, y = null;
        if(axis == null){
            return null;
        }

        for (MatrixCategoryDefinition cat : this.getSettings().getAxisCategories()) {
            if(cat.getAxis() == csi.server.common.model.visualization.matrix.Axis.X){
                x = cat.getDerivedType();
            }
            if(cat.getAxis() == csi.server.common.model.visualization.matrix.Axis.Y){
                y = cat.getDerivedType();
            }
        }

        if(axis == Axis.X){
            return x;
        }else{
            return y;
        }
    }


    List<MatrixMetrics> calculateViewMetrics() {

        MatrixMetrics metrics = new MatrixMetrics();
        double x = getX();
        double y = getY();
        double height = y + getHeight() - 1;
        double width = x + getWidth() - 1;

        boolean noCellsInRegion = true;

        for (Cell c : getMatrixDataResponse().getCells()) {
            if (c.getX() >= x && c.getX() <= width && c.getY() >= y && c.getY() <= height) {
                noCellsInRegion = false;
                metrics.setMaxValue(Math.max(metrics.getMaxValue(), c.getValue().doubleValue()));
                metrics.setMinValue(Math.min(metrics.getMinValue(), c.getValue().doubleValue()));
                metrics.setTotalCells(metrics.getTotalCells() + 1);
            }
        }

        // if there are no cells in the view
        if (noCellsInRegion) {
            metrics.setMaxValue(0);
            metrics.setMinValue(0);
        }

        int xCount = 0, yCount = 0;
        for (AxisLabel axisLabel : getCategoryResponse().getCategoryX()) {
            if (axisLabel.getOrdinalPosition() >= x && axisLabel.getOrdinalPosition() <= width) {
                xCount++;
            }
        }
        for (AxisLabel axisLabel : getCategoryResponse().getCategoryY()) {
            if (axisLabel.getOrdinalPosition() >= y && axisLabel.getOrdinalPosition() <= height) {
                yCount++;
            }
        }


        metrics.setAxisXCount(xCount);
        metrics.setAxisYCount(yCount);

        ArrayList<MatrixMetrics> matrixMetrics = new ArrayList<>();
        matrixMetrics.add(metrics);
        return matrixMetrics;
    }

}
