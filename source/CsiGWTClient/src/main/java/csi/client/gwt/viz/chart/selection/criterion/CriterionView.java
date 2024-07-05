package csi.client.gwt.viz.chart.selection.criterion;

import com.github.gwtbootstrap.client.ui.FluidRow;

public interface CriterionView {
	void setup(FluidRow row);

	boolean isValid();
}
