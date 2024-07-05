package csi.client.gwt.viz.chart.overview.view.content;

import java.util.List;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * @author Centrifuge Systems, Inc.
 */
public interface OverviewContent extends IsWidget {

    public void setCategoryData(List<Integer> categoryData);
    public void resize(int width);

}
