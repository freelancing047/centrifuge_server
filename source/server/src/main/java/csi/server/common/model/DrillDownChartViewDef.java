package csi.server.common.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import csi.server.common.model.chart.ChartField;
import csi.server.common.model.chart.ChartMeasure;
import csi.server.common.model.visualization.VisualizationDef;
import csi.server.common.model.visualization.selection.NullSelection;
import csi.server.common.model.visualization.selection.Selection;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Deprecated
public class DrillDownChartViewDef extends VisualizationDef {

    @OneToMany(cascade = CascadeType.ALL)
    protected List<ChartMeasure> metrics;

    @OneToMany(cascade = CascadeType.ALL)
    protected List<ChartField> dimensions;

    protected boolean suppressNulls;
    
    protected Integer renderThreshold;
    
    protected String chartType;

    public DrillDownChartViewDef() {
    	super();
    }

    public DrillDownChartViewDef(String name) {
        super(name);
    }

    public List<ChartMeasure> getMetrics() {
        if (this.metrics == null) {
            this.metrics = new ArrayList<ChartMeasure>();
        }
        return metrics;
    }

    public void setMetrics(List<ChartMeasure> metrics) {

        this.metrics = metrics;
    }

    public List<ChartField> getDimensions() {
        if (dimensions == null) {
            dimensions = new ArrayList<ChartField>();
        }
        return this.dimensions;
    }

    public void setDimensions(List<ChartField> dimensions) {
        this.dimensions = dimensions;
    }

    public boolean isSuppressNulls() {
        return suppressNulls;
    }

    public void setSuppressNulls(boolean suppressNulls) {
        this.suppressNulls = suppressNulls;
    }
    
    public int getRenderThreshold() {
        return renderThreshold == null ? -1 : renderThreshold;
    }

    public void setRenderThreshold(int renderThreshold) {
        this.renderThreshold = renderThreshold;
    }

    public String getChartType()
    {
        return chartType;
    }

    public void setChartType( String chartType )
    {
        this.chartType = chartType;
    }
    
    @Override
    public Selection getSelection() {
        return NullSelection.instance;
    }

    @Override
    public <T extends ModelObject, S extends ModelObject> VisualizationDef clone(Map<String, T> fieldMapIn, Map<String, S> filterMapIn) {

        //log.error("Attempting to clone unsupported visualization type \"DrillDownChartViewDef\"");
        return null;
    }

	@Override
	public <T extends ModelObject, S extends ModelObject> VisualizationDef copy(Map<String, T> fieldMapIn,
			Map<String, S> filterMapIn) {
		// TODO Auto-generated method stub
		return null;
	}
}
