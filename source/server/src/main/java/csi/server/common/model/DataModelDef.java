package csi.server.common.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.OrderColumn;
import javax.persistence.PreRemove;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import csi.server.common.dto.FieldListAccess;
import csi.server.common.enumerations.AclResourceType;
import csi.server.common.interfaces.FieldDefSource;
import csi.server.common.model.dataview.AnnotationCardDef;
import csi.server.common.model.filter.Filter;
import csi.server.common.model.visualization.VisualizationDef;
import csi.server.common.model.worksheet.WorksheetDef;
import csi.server.common.util.ReadOnlyList;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class DataModelDef extends Resource implements FieldDefSource {

    public static final String DEFAULT_SKETCH_VIEW = "DefaultSketch";

    public static final String DEFAULT_GOOGLE_MAPS = "DefaultGoogleMaps";

    private static final Map<String, Class<? extends VisualizationDef>> DefaultVisualizations;

    static {
        DefaultVisualizations = new HashMap<String, Class<? extends VisualizationDef>>();
        DefaultVisualizations.put(DEFAULT_SKETCH_VIEW, SketchViewDef.class);
        DefaultVisualizations.put(DEFAULT_GOOGLE_MAPS, GoogleMapsViewDef.class);
    }

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
    @OrderBy("ordinal")
    @JoinColumn(name = "parent_uuid")
    protected List<FieldDef> fieldDefs;

    @OneToMany(cascade = { CascadeType.ALL })
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
    @Basic(fetch = FetchType.EAGER)
    protected List<VisualizationDef> visualizations;

    @OrderColumn(name="worksheets_order")
    @OneToMany(cascade = CascadeType.ALL)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
    protected List<WorksheetDef> worksheets;

    @OneToMany(cascade = CascadeType.ALL)
    protected List<AnnotationCardDef> annotationCardDefs = new ArrayList<AnnotationCardDef>();

    protected boolean sorted = false;

    @Transient
    FieldListAccess _fieldAccess = null;
    @Transient
    Map<String, VisualizationDef> _cloneVisualizationMap;
    @Transient
    Map<String, FieldDef> _cloneFieldMap;
    @Transient
    Map<String, Filter> _cloneFilterMap;

    public List<WorksheetDef> getWorksheets() {
        if (this.worksheets == null) {
            this.worksheets = new ArrayList<WorksheetDef>();
        }
        return worksheets;
    }

    public void setWorksheets(List<WorksheetDef> worksheets) {
        this.worksheets = worksheets;
    }

    // TODO: won't need this once we go to new model
    protected int initialViewIndex;

    public DataModelDef() {
    	super(AclResourceType.DATA_MODEL);
        worksheets = new ArrayList<WorksheetDef>();
        visualizations = new ArrayList<VisualizationDef>();
    }

    @Override
    public void resetTransients() {

        _fieldAccess = null;
        _cloneVisualizationMap = null;
        _cloneFieldMap = null;
        _cloneFilterMap = null;
    }

    public FieldListAccess getFieldListAccess() {

        if (null == _fieldAccess) {

            _fieldAccess = new FieldListAccess(this, fieldDefs);
        }
        return _fieldAccess;
    }

    public FieldListAccess refreshFieldListAccess() {

        getFieldListAccess().resetMaps();
        return _fieldAccess;
    }

    public List<FieldDef> getFieldList() {

        return new ReadOnlyList<FieldDef>(getFieldListAccess().getFieldDefList());
    }

    public boolean isSorted() {

        return sorted;
    }

    public void setSorted(boolean sortedIn) {

        sorted = sortedIn;
    }

    public List<FieldDef> getFieldDefs() {

        return fieldDefs;
    }

    public void setFieldDefs(List<FieldDef> listIn) {

        fieldDefs = new ArrayList<FieldDef>(listIn);
        if (null != _fieldAccess) {

            _fieldAccess.resetList(this, fieldDefs);
        }
    }

    public void setInitialViewIndex(int initialViewIndex) {
        this.initialViewIndex = initialViewIndex;

    }

    public int getInitialViewIndex() {
        return initialViewIndex;
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
    }

    public void addWorksheet(WorksheetDef worksheet) {
        worksheets.add(worksheet);
    }

    public void removeWorksheet(WorksheetDef worksheet) {
        worksheets.remove(worksheet);
    }

    public List<VisualizationDef> getVisualizations() {
        if (this.visualizations == null) {
            this.visualizations = new ArrayList<VisualizationDef>();
        }
        return visualizations;
    }

    public void setVisualizations(List<VisualizationDef> visualizations) {
        this.visualizations = visualizations;
    }
//
//    @SuppressWarnings("unchecked")
//    public <T extends VisualizationDef> T getVisualizationDef(String name, Class<T> clazz) {
//        for (VisualizationDef v : getVisualizations()) {
//            if (clazz.isAssignableFrom(v.getClass()) && v.getName().equalsIgnoreCase(name)) {
//                return (T) v;
//            }
//        }
//
//        return null;
//    }

//    public SketchViewDef getSketchViewDef() {
//        return DataModelDefHelper.getVisualizationDef(this, DEFAULT_SKETCH_VIEW, SketchViewDef.class);
//    }
//
//    public GoogleMapsViewDef getGoogleMapsViewDef() {
//        return DataModelDefHelper.getVisualizationDef(this, DEFAULT_GOOGLE_MAPS, GoogleMapsViewDef.class);
//    }
//
    public VisualizationDef findVisualizationByUuid(String uuid) {
        for (VisualizationDef def : getVisualizations()) {
            if (def.getUuid().equals(uuid)) {
                return def;
            }
        }

        return null;
    }

    public VisualizationDef findVisualizationByName(String name) {
        for (VisualizationDef def : getVisualizations()) {
            if (def.getName().equalsIgnoreCase(name)) {
                return def;
            }
        }

        return null;
    }

    @PreRemove
    public void cleanupRefs() {
        resetTransients();
        // HACK: clear the list of fields causing the fields to become orphaned.
        // This is to avoid constraint violations due to orphaned objects.
        // The orphaned objects occur during merges of lists.

        // TODO: Fix this hack. Using the hibernate DELETE_ORPHAN annotation
        // could fix this but we would to change the way the model uses lists.
        this.getFieldDefs().clear();
    }

    //
    // Upon entry, "fieldMapIn" is an empty hashmap, filterMapIn is complete
    //
    @Override
    public DataModelDef clone() {

        _cloneFieldMap = new HashMap<String, FieldDef>();
        _cloneFilterMap = new HashMap<String, Filter>();

        return clone(_cloneFieldMap, _cloneFilterMap);
    }

    //
    // Upon entry, "fieldMapIn" is an empty hashmap, filterMapIn is complete
    //
    @Override
    public <T extends ModelObject, S extends ModelObject> DataModelDef clone(Map<String, T> fieldMapIn, Map<String, S> filterMapIn) {

        DataModelDef myClone = new DataModelDef();

        super.cloneComponents(myClone);

        _cloneVisualizationMap = new HashMap<String, VisualizationDef>();

        myClone.setFieldDefs(cloneFieldDefs(fieldMapIn)); // loads values into "fieldMapIn"
        myClone.setVisualizations(cloneVisualizations(_cloneVisualizationMap, fieldMapIn, filterMapIn)); // loads values into "_cloneVisualizationMap"
        myClone.setWorksheets(cloneWorksheets(_cloneVisualizationMap, fieldMapIn, filterMapIn));
        myClone.setSorted(isSorted());

        return myClone;
    }

    @Override
    protected void debugContents(StringBuilder bufferIn, String indentIn) {

        super.debugContents(bufferIn, indentIn);
        debugObject(bufferIn, initialViewIndex, indentIn, "initialViewIndex");
        debugList(bufferIn, fieldDefs, indentIn, "fieldDefs");
        debugList(bufferIn, visualizations, indentIn, "visualizations");
        debugList(bufferIn, worksheets, indentIn, "worksheets");
    }

    public List<AnnotationCardDef> getAnnotationCardDefs() {
        return annotationCardDefs;
    }

    public void setAnnotationCardDefs(List<AnnotationCardDef> annotationCardDefs) {
        this.annotationCardDefs = annotationCardDefs;
    }

    @SuppressWarnings("unchecked")
    private <T extends ModelObject> List<FieldDef> cloneFieldDefs(Map<String, T> fieldMapIn) {

        if (null != getFieldDefs()) {

            List<FieldDef>  myList = new ArrayList<FieldDef>();

            for (FieldDef myItem : getFieldDefs()) {

                myList.add((FieldDef)cloneFromOrToMap(fieldMapIn, (T)myItem, fieldMapIn));
            }

            return myList;

        } else {

            return null;
        }
    }

    private <T extends ModelObject, S extends ModelObject> List<VisualizationDef> cloneVisualizations(Map<String, VisualizationDef> visualizationMapIn, Map<String, T> fieldMapIn, Map<String, S> filterMapIn) {



        if (null != getVisualizations()) {

            List<VisualizationDef>  myList = new ArrayList<VisualizationDef>();

            for (VisualizationDef myItem : getVisualizations()) {

                VisualizationDef myClone = myItem.clone(fieldMapIn, filterMapIn);
                visualizationMapIn.put(myItem.getUuid(), myClone);

                myList.add(myClone);
            }

            return myList;

        } else {

            return null;
        }
    }


    private <T extends ModelObject, S extends ModelObject, R extends ModelObject> List<WorksheetDef> cloneWorksheets(Map<String, S>  visualizationMapIn, Map<String, T> fieldMapIn, Map<String, R> filterMapIn) {

        if (null != getWorksheets()) {

            List<WorksheetDef>  myList = new ArrayList<WorksheetDef>();

            for (WorksheetDef myItem : getWorksheets()) {

                myList.add(myItem.clone(visualizationMapIn, fieldMapIn, filterMapIn));
            }

            return myList;

        } else {

            return null;
        }
    }
}
