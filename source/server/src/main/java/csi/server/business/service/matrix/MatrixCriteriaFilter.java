package csi.server.business.service.matrix;

import csi.server.common.model.visualization.chart.ChartCriterion;
import csi.server.common.model.visualization.chart.SingleDoubleTypeCriterion;
import csi.server.common.model.visualization.chart.TwoDoubleTypeCriterion;
import csi.shared.core.visualization.matrix.Cell;

import java.util.List;

public class MatrixCriteriaFilter {
    private static double EPSILON = 0.000000001;


    public MatrixData applyFilter(MatrixData all, List<ChartCriterion> criterionList){
        MatrixData filtered = new MatrixData();

        for(Cell c : all.getAllCells()){
            double cellValue = (double) c.getValue();
            boolean flag = true;
            for(ChartCriterion criteria : criterionList){
                if(!testCriteria(cellValue, criteria)){
                    flag = false;
                }
            }
            if (flag) {
                filtered.addCell(c);
            }
        }
        return filtered;
    }



    public boolean testCriteria(double measureValue, ChartCriterion criterion){
        String operatorString = criterion.getOperatorString();

        switch (operatorString) {
            case "<":
            case "<=":
            case "==":
            case ">=":
            case ">":
            case "!=":
                if (!testSingleDoubleTypeCriterion(
                        measureValue,
                        operatorString,
                        (SingleDoubleTypeCriterion) criterion)
                )
                    return Boolean.FALSE;
                break;
            case "<<":
                if (!testTwoDoubleTypeCriterion(
                        measureValue,
                        operatorString,
                        (TwoDoubleTypeCriterion) criterion)
                )
                    return Boolean.FALSE;
                break;
            case "Top":
            case "Top%":

                break;
            case "Bottom":
            case "Bottom%":

                break;
            default:
                return Boolean.FALSE;
        }

        return Boolean.TRUE;
    }

    private static boolean testTwoDoubleTypeCriterion(Double doubleValue, String operatorString, TwoDoubleTypeCriterion criterion) {
        switch (operatorString) {
            case "<<":
                return criterion.getMinValue() < doubleValue && doubleValue < criterion.getMaxValue();
            default:
                return false;
        }
    }

    private static boolean testSingleDoubleTypeCriterion(Double doubleValue, String operatorString, SingleDoubleTypeCriterion criterion) {
        switch (operatorString) {
            case "<":
                return doubleValue <    criterion.getTestValue();
            case "<=":
                return doubleValue <=   criterion.getTestValue();
            case "==":
                double x = Math.abs(doubleValue - criterion.getTestValue());
                return x < EPSILON;
            case ">=":
                return doubleValue >=   criterion.getTestValue();
            case ">":
                return doubleValue >    criterion.getTestValue();
            case "!=":
                double y = Math.abs(doubleValue - criterion.getTestValue());
                return y > EPSILON;
            default:
                return false;
        }
    }

}
