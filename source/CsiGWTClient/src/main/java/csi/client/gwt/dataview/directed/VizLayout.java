package csi.client.gwt.dataview.directed;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.sencha.gxt.widget.core.client.Window;

import csi.client.gwt.mainapp.CsiDisplay;
import csi.client.gwt.viz.Visualization;

public abstract class VizLayout extends ResizeComposite implements CsiDisplay {
	
    int vizCount = 1;
    
	void addVisualization(Visualization visualization) {
	}

	void add(Window floatingTabWindow) {
	}

    public void saveState() {}

    public void restoreState() {}

    public void forceExit() {}

    public int getVizCount() {
        return vizCount;
    }

    public void setVizCount(int vizCount) {
        this.vizCount = vizCount;
    }
}
