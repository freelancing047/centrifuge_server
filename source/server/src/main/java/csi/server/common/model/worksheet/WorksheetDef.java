package csi.server.common.model.worksheet;

import csi.server.common.model.ModelObject;
import csi.server.common.model.visualization.VisualizationDef;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class WorksheetDef extends ModelObject {

    /*
     * Back reference to visualizations.  The DataModelDef will have the actual VisualizationDefs.
     * Here, we just maintain a reference to the UUIDs.  This approach leaves open the possibility of 
     * having visualizations across multiple worksheets in the future if desired.
     */
    @OneToMany(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
    protected List<VisualizationDef> visualizations = new ArrayList<VisualizationDef>();

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "worksheetscreenlayout_uuid")
    private WorksheetScreenLayout worksheetScreenLayout = new WorksheetScreenLayout(this);

    private Integer color;
    protected String worksheetName = "";

    public String getWorksheetName() {
        return worksheetName;
    }

    public void setWorksheetName(String worksheetName) {
        this.worksheetName = worksheetName;
    }

    public Integer getWorksheetColor() {
        return color;
    }

    public void setWorksheetColor(Integer color) {
        this.color = color;
    }

    public WorksheetDef() {
        super();
    }

    public List<VisualizationDef> getVisualizations() {
        return visualizations;
    }

    public void setVisualizations(List<VisualizationDef> visualizations) {
        this.visualizations = visualizations;
    }

    public void addVisualization(VisualizationDef visualization) {
    	VisualizationDef def = findVisualizationByUuid(visualization.getUuid());
    	if ( def != null) {
    		visualizations.remove(def);
    	}
    	
    	visualizations.add(visualization);
    }

    public void removeVisualization(VisualizationDef visualization) {
        visualizations.remove(visualization);
        worksheetScreenLayout.getLayout().removeVisualizationLayoutState(visualization.getUuid());
    }

    public WorksheetScreenLayout getWorksheetScreenLayout() {
        return worksheetScreenLayout;
    }

    public void setWorksheetScreenLayout(WorksheetScreenLayout worksheetScreenLayout) {
        this.worksheetScreenLayout = worksheetScreenLayout;
    }

    @Override
    public <T extends ModelObject, S extends ModelObject, R extends ModelObject> WorksheetDef clone(Map<String, T> visualizationMapIn, Map<String, S> fieldMapIn, Map<String, R> filterMapIn) {
        
        WorksheetDef myClone = new WorksheetDef();
        
        super.cloneComponents(myClone);

        myClone.setVisualizations(identifyVisualizations(visualizationMapIn, fieldMapIn, filterMapIn));
        myClone.setWorksheetName(getWorksheetName());
        myClone.setWorksheetColor(getWorksheetColor());
        if (null != getWorksheetScreenLayout()) {
            myClone.setWorksheetScreenLayout(getWorksheetScreenLayout().clone(myClone, visualizationMapIn, fieldMapIn, filterMapIn));
        }
        
        return myClone;
    }

    @Override
    protected void debugContents(StringBuilder bufferIn, String indentIn) {

        debugObject(bufferIn, worksheetName, indentIn, "worksheetName");
        debugList(bufferIn, visualizations, indentIn, "visualizations");
    }
    
    @SuppressWarnings("unchecked")
    private <T extends ModelObject, S extends ModelObject, R extends ModelObject> List<VisualizationDef> identifyVisualizations(Map<String, T> visualizationMapIn, Map<String, S> fieldMapIn, Map<String, R> filterMapIn) {
        
        if (null != getVisualizations()) {
            
            List<VisualizationDef>  myList = new ArrayList<VisualizationDef>();
            
            for (VisualizationDef myItem : getVisualizations()) {
                
                myList.add((VisualizationDef)cloneFromOrToMap(visualizationMapIn, (T)myItem, fieldMapIn, filterMapIn));
            }
            
            return myList;
            
        } else {
            
            return null;
        }
    }
    
    public VisualizationDef findVisualizationByUuid(String uuid) {
        for (VisualizationDef def : getVisualizations()) {
            if (def.getUuid().equals(uuid)) {
                return def;
            }
        }

        return null;
    }
}
