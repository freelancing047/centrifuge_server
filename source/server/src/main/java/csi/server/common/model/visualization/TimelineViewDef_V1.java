package csi.server.common.model.visualization;

import java.util.Map;

import javax.persistence.Entity;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import csi.server.common.model.ModelObject;
import csi.server.common.model.visualization.selection.NullSelection;
import csi.server.common.model.visualization.selection.Selection;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Deprecated
public class TimelineViewDef_V1 extends VisualizationDef {

//    @OneToMany(cascade = CascadeType.ALL)
//	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
//    protected List<EventDef> eventDefs;

    public TimelineViewDef_V1() {
    	super();
        setType(VisualizationType.CHRONOS);
    }
    public TimelineViewDef_V1(String name) {
        super(name);
    }

//    public List<EventDef> getEventDefs() {
//        if (eventDefs == null) {
//            eventDefs = new ArrayList<EventDef>();
//        }
//        return eventDefs;
//    }
//
//    public void setEventDefs(List<EventDef> eventDefs) {
//        this.eventDefs = eventDefs;
//    }
    
    @Override
    public Selection getSelection() {
        return NullSelection.instance;
    }

    @Override
    public <T extends ModelObject, S extends ModelObject> VisualizationDef clone(Map<String, T> fieldMapIn, Map<String, S> filterMapIn) {
        
        //log.error("Attempting to clone unsupported visualization type \"TimelineViewDef\"");
        return null;
    }
	@Override
	public <T extends ModelObject, S extends ModelObject> VisualizationDef copy(Map<String, T> fieldMapIn,
			Map<String, S> filterMapIn) {
		// TODO Auto-generated method stub
		return null;
	}
}
