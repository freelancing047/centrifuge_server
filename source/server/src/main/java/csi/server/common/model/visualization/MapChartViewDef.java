package csi.server.common.model.visualization;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import csi.server.common.model.CsiUUID;
import csi.server.common.model.FieldDef;
import csi.server.common.model.ModelObject;
import csi.server.common.model.chart.ChartDimension;
import csi.server.common.model.visualization.selection.NullSelection;
import csi.server.common.model.visualization.selection.Selection;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Deprecated
public class MapChartViewDef extends VisualizationDef {

    @OneToOne(cascade = CascadeType.ALL)
    protected ChartDimension cell;

    protected String chartFunction;

    @OneToMany(cascade = CascadeType.ALL)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
    protected List<ChartDimension> dimensions;

    protected String chartType;

    protected boolean suppressNulls;

    protected String mapName;
    
    protected String icon;

    @ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
    protected FieldDef latitudeField;

    @ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
    protected FieldDef longitudeField;

    @ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
    protected FieldDef typeField;

    @OneToMany(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
    protected List<FieldDef> toolTipFields;

    @OneToOne(cascade = CascadeType.ALL)
    protected ChartDimension bubbleCell;

    protected String bubbleFunction;

    public MapChartViewDef() {
    	super();
    }

    public MapChartViewDef(CsiUUID uuid, String name) {
        super(name);
    }

    public ChartDimension getCell() {
        return cell;
    }

    public void setCell(ChartDimension value) {
        this.cell = value;
    }

    public String getChartFunction() {
        return chartFunction;
    }

    public void setChartFunction(String value) {
        this.chartFunction = value;
    }

    public ChartDimension getBubbleCell() {
        return bubbleCell;
    }

    public void setBubbleCell(ChartDimension bubbleCell) {
        this.bubbleCell = bubbleCell;
    }

    public String getBubbleFunction() {
        return bubbleFunction;
    }

    public void setBubbleFunction(String bubbleFunction) {
        this.bubbleFunction = bubbleFunction;
    }

    public List<ChartDimension> getDimensions() {
        if (dimensions == null) {
            dimensions = new ArrayList<ChartDimension>();
        }
        return this.dimensions;
    }

    public void setDimensions(List<ChartDimension> dimensions) {
        this.dimensions = dimensions;
    }

    public String getChartType() {
        return chartType;
    }

    public void setChartType(String chartType) {
        this.chartType = chartType;
    }

    public boolean isSuppressNulls() {
        return suppressNulls;
    }

    public void setSuppressNulls(boolean suppressNulls) {
        this.suppressNulls = suppressNulls;
    }

    public String getMapName() {
        return mapName;
    }

    public void setMapName(String mapName) {
        this.mapName = mapName;
    }

    public FieldDef getLatitudeField() {
        return latitudeField;
    }

    public void setLatitudeField(FieldDef latitudeField) {
        this.latitudeField = latitudeField;
    }

    public FieldDef getLongitudeField() {
        return longitudeField;
    }

    public void setLongitudeField(FieldDef longitudeField) {
        this.longitudeField = longitudeField;
    }

    public FieldDef getTypeField() {
        return typeField;
    }

    public void setTypeField(FieldDef typeField) {
        this.typeField = typeField;
    }

    public List<FieldDef> getToolTipFields() {
        if (toolTipFields == null) {
            toolTipFields = new ArrayList<FieldDef>();
        }
        return toolTipFields;
    }

    public void setToolTipFields(List<FieldDef> fields) {
        this.toolTipFields = fields;
    }

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}
    
    @Override
    public Selection getSelection() {
        return NullSelection.instance;
    }

    @Override
    public <T extends ModelObject, S extends ModelObject> VisualizationDef clone(Map<String, T> fieldMapIn, Map<String, S> filterMapIn) {
        
        //log.error("Attempting to clone unsupported visualization type \"MapChartViewDef\"");
        return null;
    }

	@Override
	public <T extends ModelObject, S extends ModelObject> VisualizationDef copy(Map<String, T> fieldMapIn,
			Map<String, S> filterMapIn) {
		// TODO Auto-generated method stub
		return null;
	}
}
