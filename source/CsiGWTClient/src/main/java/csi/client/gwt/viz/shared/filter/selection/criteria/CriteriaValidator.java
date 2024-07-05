package csi.client.gwt.viz.shared.filter.selection.criteria;

import csi.client.gwt.viz.chart.selection.ValidationListener;
import csi.client.gwt.viz.shared.filter.selection.SelectionFilterView;

public class CriteriaValidator implements ValidationListener {
    protected SelectionFilterView view;


    public CriteriaValidator(SelectionFilterView view) {
        this.view = view;
    }

    // TODO
    @Override
    public void checkCriteriaValidity() {
        view.setButtonsEnabled(true);
    }
}
