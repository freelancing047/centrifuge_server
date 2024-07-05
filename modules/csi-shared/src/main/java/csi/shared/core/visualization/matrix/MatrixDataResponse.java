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
package csi.shared.core.visualization.matrix;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import csi.shared.core.util.Native;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
@SuppressWarnings("serial")
public class
MatrixDataResponse extends AbstractMatrixData {

    private TreeSet<Cell> cells = Sets.newTreeSet();

    private boolean limitReached = false;
    private boolean summary = false;
    private boolean fullMatrix = false;
    private int totalCount;

    public boolean isSummary() {
        return summary;
    }

    public void setSummary(boolean summary) {
        this.summary = summary;
    }

    public boolean isFullMatrix() {
        return fullMatrix;
    }

    public void setFullMatrix(boolean fullMatrix) {
        this.fullMatrix = fullMatrix;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public MatrixDataResponse() {
        super();
    }
    
    public MatrixDataResponse(int x1, int x2, int y1, int y2) {
        super(x1, x2, y1, y2);
    }

    public MatrixDataResponse(MatrixDataRequest request) {
        super(request.getStartX(), request.getEndX(), request.getStartY(), request.getEndY());
    }

    // the final idea for this is to keep all the data, and merge the one coming in together.
    public void addMoreCells(MatrixDataResponse resp){
        Set<Cell> deDuped = new HashSet<>();
        deDuped.addAll(cells);
        deDuped.addAll(resp.getCells());
        cells.clear();
        cells.addAll(deDuped);
    }

    public void updateWindowInfo(MatrixDataResponse resp){
        this.setStartX(resp.getStartX());
        this.setEndX(resp.getEndX());
        this.setStartY(resp.getStartY());
        this.setEndY(resp.getEndY());
    }

    public Set<Cell> getCells() {
        return cells;
    }

    public void setCells(Collection<Cell> cells) {
        this.cells.clear();
        this.cells.addAll(cells);
    }

    public Cell getCell(int x, int y){
        Cell cell = new Cell();
        cell.setX(x);
        cell.setY(y);
        if (cells.contains(cell)) {
            return cells.floor(cell);
        }
        return null;
    }

    public boolean isLimitReached() {
        return limitReached;
    }

    public void setLimitReached(boolean limitReached) {
        this.limitReached = limitReached;
    }

    public MatrixDataRequest getXYPair(){
        MatrixDataRequest r = new MatrixDataRequest();
        r.setStartX(getStartX());
        r.setStartY(getStartY());
        r.setEndX(getEndX());
        r.setEndY(getEndY());
        r.setMaxX(getMaxX());
        r.setMaxY(getMaxY());

        return r;
    }
}
