package csi.client.gwt.viz.chart.selection.criterion;

import com.github.gwtbootstrap.client.ui.FluidRow;

import com.sencha.gxt.widget.core.client.info.Info;
import csi.client.gwt.viz.chart.selection.ValidationListener;
import csi.server.common.model.visualization.chart.*;

public class ChartCriterionPresenter {
    private ValidationListener validationListener;
    private int columnIndex;
    private String columnHeader;
    private CriterionView view = null;
    private ChartCriterion criterion = null;

    public ChartCriterionPresenter(ValidationListener validationListener) {
        this.validationListener = validationListener;
    }

    public int getColumnIndex() {
        return columnIndex;
    }

    public void setColumnIndex(int columnIndex) {
        this.columnIndex = columnIndex;
        if (criterion != null) {
            criterion.setColumnIndex(columnIndex - 1);
        }
    }

    public String getColumnHeader() {
        return columnHeader;
    }

    public void setColumnHeader(String columnHeader) {
        this.columnHeader = columnHeader;
        if (criterion != null) {
            criterion.setColumnHeader(columnHeader);
        }
    }

    public void assignOperator(String operatorString) {
        switch (operatorString) {
            case "<":
            case "<=":
            case "==":
            case ">=":
            case ">":
            case "!=":
                criterion = new SingleDoubleTypeCriterion(columnIndex, columnHeader, operatorString);
                break;
            case "<<":
                criterion = new TwoDoubleTypeCriterion(columnIndex, columnHeader, operatorString);
                break;
            case "Top":
            case "Bottom":
                criterion = new PositiveIntegerTypeCriterion(columnIndex, columnHeader, operatorString);
                break;
            case "Top%":
            case "Bottom%":
                criterion = new ZeroToOneTypeCriterion(columnIndex, columnHeader, operatorString);
                break;
            default:
                criterion = null;
                break;
        }

        createView();
    }

    private void createView() {
        if (criterion == null) {
            view = null;
        } else if (criterion instanceof SingleDoubleTypeCriterion) {
            view = new SingleDoubleTypeCriterionView(validationListener, (SingleDoubleTypeCriterion) criterion);
        } else if (criterion instanceof TwoDoubleTypeCriterion) {
            view = new TwoDoubleTypeCriterionView(validationListener, (TwoDoubleTypeCriterion) criterion);
        } else if (criterion instanceof SingleIntegerTypeCriterion) {
            view = new SingleIntegerTypeCriterionView(validationListener, (SingleIntegerTypeCriterion) criterion);
        } else if (criterion instanceof ZeroToOneTypeCriterion) {
            view = new ZeroToOneTypeCriterionView(validationListener, (ZeroToOneTypeCriterion) criterion);
        } else if (criterion instanceof PositiveIntegerTypeCriterion) {
            view = new PositiveIntegerTypeCriterionView(validationListener, (PositiveIntegerTypeCriterion) criterion);
        }
    }

    public void setup(FluidRow row) {
        if (view == null) {
//			row.clear();
        } else {
            view.setup(row);
        }
    }

    public ChartCriterion getCriterion() {
        return criterion;
    }

    public boolean isValid() {
        if (view != null) {
            return view.isValid();
        }
        return false;
    }

    public void setCriterion(ChartCriterion criterion) {
        this.criterion = criterion;

        createView();
    }
}
