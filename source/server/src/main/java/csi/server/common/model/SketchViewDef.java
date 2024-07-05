package csi.server.common.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import csi.server.common.model.visualization.VisualizationDef;
import csi.server.common.model.visualization.selection.NullSelection;
import csi.server.common.model.visualization.selection.Selection;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class SketchViewDef extends VisualizationDef {

    protected String heatMapFunction;

    @ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
    protected FieldDef area;

    @ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
    protected FieldDef heat;

    @OneToMany(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
    protected List<FieldDef> toolTipFields;

    public SketchViewDef() {
    	super();
    }

    public SketchViewDef(String name) {
        super(name);
    }

    public String getHeatMapFunction() {
        return heatMapFunction;
    }

    public void setHeatMapFunction(String heatMapFunction) {
        this.heatMapFunction = heatMapFunction;
    }

    public FieldDef getArea() {
        return area;
    }

    public void setArea(FieldDef area) {
        this.area = area;
    }

    public FieldDef getHeat() {
        return heat;
    }

    public void setHeat(FieldDef heat) {
        this.heat = heat;
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
    
    @Override
    public Selection getSelection() {
        return NullSelection.instance;
    }

    @Override
    public <T extends ModelObject, S extends ModelObject> VisualizationDef clone(Map<String, T> fieldMapIn, Map<String, S> filterMapIn) {
        
        //log.error("Attempting to clone unsupported visualization type \"SketchViewDef\"");
        return null;
    }

	@Override
	public <T extends ModelObject, S extends ModelObject> VisualizationDef copy(Map<String, T> fieldMapIn,
			Map<String, S> filterMapIn) {
		// TODO Auto-generated method stub
		return null;
	}
}
