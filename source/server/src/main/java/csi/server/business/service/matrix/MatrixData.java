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

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;
import com.google.common.collect.Table;
import com.google.common.collect.TreeMultimap;

import csi.server.business.service.ColorActionsService;
import csi.server.common.model.visualization.chart.ChartCriterion;
import csi.server.common.model.visualization.chart.PositiveIntegerTypeCriterion;
import csi.server.common.model.visualization.chart.ZeroToOneTypeCriterion;
import csi.server.common.model.visualization.matrix.MatrixSettings;
import csi.shared.core.visualization.matrix.AxisLabel;
import csi.shared.core.visualization.matrix.Cell;
import csi.shared.core.visualization.matrix.MatrixMetrics;

/**
 * @author Centrifuge Systems, Inc.
 */

public class MatrixData implements Serializable {
    private List<Cell> cells = new ArrayList<Cell>();
    private List<AxisLabel> xCategories = new ArrayList<AxisLabel>();
    private List<AxisLabel> yCategories = new ArrayList<AxisLabel>();
    int x, y, dx, dy;
    private int minX = 0;
    private int minY = 0;
    private int maxX = 0;
    private int maxY = 0;
    private MatrixMetrics matrixMetrics = new MatrixMetrics();
    private String rangeImage;
    private boolean limitExceeded;

    private boolean isSummary;
    private TreeMultimap<Integer, Integer> cellIdMap;

    public MatrixData() {
    }

    public void addCells(List<Cell> allCells) {
        cells.addAll(allCells);
    }

    public void addCell(Cell cell) {
        cells.add(cell);
    }

    public List<Cell> getAllCells() {
        return cells;
    }

    /**
     * 0-based indices.
     * <p>
     * Trying to rework this.
     *
     * @return List of cells with largest values first.
     */
    public List<Cell> get(int x1, int y1, int x2, int y2) {
        List<Cell> cells = new ArrayList<Cell>(1000);

        for (Cell c : this.cells) {
            if (((x1 <= c.getX()) && (x2 >= c.getX())) &&
                    ((y1 <= c.getY()) && (y2 >= c.getY()))) {
                cells.add(c);
            }
        }

        return cells;
    }


    public void postProcess() {
        this.matrixMetrics = computeMetrics();
    }

    public void doFilter(MatrixSettings sets, List<ChartCriterion> criteria, boolean selectionFilter) {
        List<ChartCriterion> filterCriteria = selectionFilter ? criteria : sets.getFilterCriteria();
        List<Cell> ogCells = getAllCells();
        List<Cell> allcells = new ArrayList<>(ogCells.size() + 1);

        for (Cell c : ogCells) {
            allcells.add(c.getClone());
        }
        allcells.sort(Comparator.comparing((Cell o) -> o.getValue().doubleValue()).reversed().thenComparing(Cell::getX).thenComparing(Cell::getY));

        int cellCountMax;
        HashMap<ChartCriterion, Integer> threshholdIndex = new HashMap<ChartCriterion, Integer>();
        HashMap<ChartCriterion, Cell> criteriaValue = new HashMap<ChartCriterion, Cell>();
        for (ChartCriterion filterCriterion : filterCriteria) {
            String operator = filterCriterion.getOperatorString();
            int cellCount = allcells.size();

            if (operator.equals("Top") || operator.equals("Bottom")) {
                if (filterCriterion instanceof PositiveIntegerTypeCriterion) {
                    PositiveIntegerTypeCriterion crit = (PositiveIntegerTypeCriterion) filterCriterion;
                    Integer testValue = crit.getTestValue();
                    switch (operator) {
                        case "Top":
                            threshholdIndex.put(filterCriterion, Integer.valueOf(Math.min(testValue, cellCount)));
                            criteriaValue.put(filterCriterion, allcells.get(Math.min(testValue, cellCount) - 1));
                            break;
                        case "Bottom":
                            threshholdIndex.put(filterCriterion, Integer.valueOf(Math.max(cellCount - testValue, 0)));
                            criteriaValue.put(filterCriterion, allcells.get(Math.max(cellCount - testValue, 0)));
                            break;
                        default:
                            break;
                    }
                }
            } else if (operator.equals("Top%") || operator.equals("Bottom%")) {
                if (filterCriterion instanceof ZeroToOneTypeCriterion) {
                    ZeroToOneTypeCriterion crit = (ZeroToOneTypeCriterion) filterCriterion;
                    switch (operator) {
                        case "Top%":
                            cellCountMax = (int) Math.floor(cellCount * (crit.getTestValue().doubleValue() / 100));

                            threshholdIndex.put(filterCriterion, Integer.valueOf(cellCountMax));
                            criteriaValue.put(filterCriterion, allcells.get(cellCountMax));
                            break;
                        case "Bottom%":
                            cellCountMax = (int) Math.ceil(cellCount * (crit.getTestValue().doubleValue() / 100));

                            threshholdIndex.put(filterCriterion, Integer.valueOf(cellCount - cellCountMax));
                            criteriaValue.put(filterCriterion, allcells.get(cellCount - cellCountMax));
                            break;
                        default:
                            break;
                    }
                }
            }
        }
        if (!threshholdIndex.isEmpty()) {
           List<Cell> filtered = new ArrayList<Cell>();

           for (Cell c : allcells) {
              boolean isPass = true;

              for (ChartCriterion filterCriterion : filterCriteria) {
                 if (isPass) {
                    String operatorString = filterCriterion.getOperatorString();
                    Cell cutoffCell = criteriaValue.get(filterCriterion);
                    isPass = testValue(operatorString, c, cutoffCell);
                 }
              }
              if (isPass) {
                 filtered.add(c);
              }
           }
           this.cells.clear();

           // this replaces the cells on the matrix data for the time being, i'm not sure if that's the best approach..
           // Need to possibly move this into the loader of the matrix and do it before the all the other stuff is done.
           this.cells = filtered;
        }
    }

   public boolean testValue(String criteriaString, Cell in, Cell cutoffCell) {
      boolean result = true;

      switch (criteriaString) {
         case "<":
         case "<=":
         case "==":
         case ">=":
         case ">":
         case "!=":
         case "<<":
            break;
         case "Top":
         case "Top%":
            if (in.getValue().doubleValue() < cutoffCell.getValue().doubleValue()) {
               result = false;
            } else if (BigDecimal.valueOf(in.getValue().doubleValue())
                         .compareTo(BigDecimal.valueOf(cutoffCell.getValue().doubleValue())) == 0) {
               if (in.getX() > cutoffCell.getX()) {
                  result = false;
               }
            }
            break;
         case "Bottom":
         case "Bottom%":
            if (in.getValue().doubleValue() > cutoffCell.getValue().doubleValue()) {
               result = false;
            } else if (BigDecimal.valueOf(in.getValue().doubleValue())
                         .compareTo(BigDecimal.valueOf(cutoffCell.getValue().doubleValue())) == 0) {
               if (in.getX() < cutoffCell.getX()) {
                  result = false;
               }
            }
            break;
         default:
            result = false;
            break;
      }
      return result;
   }

   public void createScaleImage(MatrixSettings settings, ColorActionsService colorActionsService) {
      rangeImage = colorActionsService.getColorRangeSample(25, 400, settings.getColorModel(),
                                                           ColorActionsService.RangeDirection.VERTICAL);
   }

   public static Ordering<Table.Cell<AxisLabel, AxisLabel, Cell>> stringComparator =
      new Ordering<Table.Cell<AxisLabel, AxisLabel, Cell>>() {
         @Override
         public int compare(Table.Cell<AxisLabel, AxisLabel, Cell> cell1,
                            Table.Cell<AxisLabel, AxisLabel, Cell> cell2) {
            String cell1Val = cell1.getRowKey().getLabel();
            String cell2Val = cell2.getRowKey().getLabel();
            return ComparisonChain.start().compare(cell1Val, cell2Val).compare(cell1.getValue().getValue().doubleValue(), cell2.getValue().getValue().doubleValue()).result();
         }
   };

    public MatrixMetrics computeMetrics() {
        MatrixMetrics mMetrics = new MatrixMetrics();

        mMetrics.setAxisXCount(getxCategories().size());
        mMetrics.setAxisYCount(getyCategories().size());

        for (Cell cell : cells) {
            mMetrics.processCell(cell);
        }

        return mMetrics;
    }

    public int getTotalCount() {
        return cells.size();
    }

    public boolean isLimitExceeded() {
        return limitExceeded;
    }

    public void setLimitExceeded(boolean limitExceeded) {
        this.limitExceeded = limitExceeded;
    }

    public String getRangeImageAsString() {
        return rangeImage;
    }

    public void setMatrixMetrics(MatrixMetrics m) {
        matrixMetrics = m;
    }

    public MatrixMetrics getMatrixMetrics() {
        return matrixMetrics;
    }

    public int getMinX() {
        return minX;
    }

    public void setMinX(int minX) {
        this.minX = minX;
    }

    public int getMaxX() {
        return maxX;
    }

    public void setMaxX(int maxX) {
        this.maxX = maxX;
    }

    public int getMinY() {
        return minY;
    }

    public void setMinY(int minY) {
        this.minY = minY;
    }

    public int getMaxY() {
        return maxY;
    }

    public void setMaxY(int maxY) {
        this.maxY = maxY;
    }

    public boolean isSummary() {
        return isSummary;
    }

    public void setSummary(boolean summary) {
        isSummary = summary;
    }

    public Cell getCell(Integer integer) {
        return cells.get(integer);
    }

    public List<AxisLabel> getxCategories() {
        return xCategories;
    }

    public void setxCategories(List<AxisLabel> xCategories) {
        this.xCategories = xCategories;
    }

    public List<AxisLabel> getyCategories() {
        return yCategories;
    }

    public void setyCategories(List<AxisLabel> yCategories) {
        this.yCategories = yCategories;
    }

    public TreeMultimap<Integer, Integer> getCellIdMap() {
        return cellIdMap;
    }

    public void setCellIdMap(TreeMultimap<Integer, Integer> cellIdMap) {
        this.cellIdMap = cellIdMap;
    }
}
