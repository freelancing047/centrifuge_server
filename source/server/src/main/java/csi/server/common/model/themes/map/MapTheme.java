package csi.server.common.model.themes.map;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import csi.server.common.enumerations.AclResourceType;
import csi.server.common.graphics.shapes.ShapeType;
import csi.server.common.model.themes.HasDefaultShape;
import csi.server.common.model.themes.Theme;
import csi.server.common.model.visualization.VisualizationType;
import csi.server.common.util.ValuePair;

@SuppressWarnings("serial")
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class MapTheme extends Theme implements HasDefaultShape{

    public MapTheme() {
        super(AclResourceType.MAP_THEME, VisualizationType.MAP_CHART);
    }

    @OneToMany(cascade = CascadeType.ALL, fetch=FetchType.EAGER)
    @Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
    @Fetch(value = FetchMode.SUBSELECT)
    @Cascade(value = { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    private List<PlaceStyle> placeStyles = new ArrayList<PlaceStyle>();


    @OneToMany(cascade = CascadeType.ALL, fetch=FetchType.EAGER)
    @Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
    @Fetch(value = FetchMode.SUBSELECT)
    @Cascade(value = { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    private List<AssociationStyle> associationStyles = new ArrayList<AssociationStyle>();

    @OneToOne(cascade = CascadeType.ALL, fetch=FetchType.EAGER)
    @Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
    @Cascade(value = { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    private PlaceStyle bundleStyle;

    @Enumerated(EnumType.STRING)
    private ShapeType defaultShape;

    @Transient
    private Map<String, PlaceStyle> _placeStyleMap = null;
    @Transient
    private List<ValuePair<String, PlaceStyle>> _placeOverflow = null;
    @Transient
    private Map<String, AssociationStyle> _associationStyleMap = null;
    @Transient
    private List<ValuePair<String, AssociationStyle>> _associationOverflow = null;

    public MapTheme(String uuidIn, String nameIn, String remarksIn, String ownerIn, ShapeType shapeTypeIn,
                    PlaceStyle bundleStyleIn, List<PlaceStyle> placeStylesIn,
                    List<AssociationStyle> associationStylesIn) {

        this();

        name = nameIn;
        remarks = remarksIn;
        owner = ownerIn;
        defaultShape = shapeTypeIn;
        bundleStyle = bundleStyleIn;

        if ((placeStylesIn != null) && !placeStylesIn.isEmpty()) {
            placeStyles.addAll(placeStylesIn);
        }
        if ((associationStylesIn != null) && !associationStylesIn.isEmpty()) {
            associationStyles.addAll(associationStylesIn);
        }
        setUuid(uuidIn);
    }

    public List<PlaceStyle> getPlaceStyles() {
        return placeStyles;
    }


    public void setPlaceStyles(List<PlaceStyle> placeStyles) {
        this.placeStyles = placeStyles;
    }


    public List<AssociationStyle> getAssociationStyles() {
        return associationStyles;
    }


    public void setAssociationStyles(List<AssociationStyle> associationStyles) {
        this.associationStyles = associationStyles;
    }


    public PlaceStyle getBundleStyle() {
        return bundleStyle;
    }


    public void setBundleStyle(PlaceStyle bundleStyle) {
        this.bundleStyle = bundleStyle;
    }


    public ShapeType getDefaultShape() {
        return defaultShape;
    }


    public void setDefaultShape(ShapeType defaultShape) {
        this.defaultShape = defaultShape;
    }

    public void addPlaceStyle(PlaceStyle styleIn, String itemIn) {

        addStyle(styleIn, itemIn, getPlaceStyleMap());
    }

    public void removePlaceStyle(PlaceStyle styleIn, String itemIn) {

        removeStyle(styleIn, itemIn, getPlaceStyleMap());
    }

    public void addAssociationStyle(AssociationStyle styleIn, String itemIn) {

        addStyle(styleIn, itemIn, getAssociationStyleMap());
    }

    public void removeAssociationStyle(AssociationStyle styleIn, String itemIn) {

        removeStyle(styleIn, itemIn, getAssociationStyleMap());
    }

    public void resetMaps() {

        resetPlaceMaps();
        resetAssociationMaps();
    }

    public PlaceStyle findPlaceConflict(String itemIdIn) {

        return getPlaceStyleMap().get(itemIdIn);
    }

    public List<ValuePair<String, PlaceStyle>> getPlaceOverFlow() {

        List<ValuePair<String, PlaceStyle>> myList = _placeOverflow;

        if (null == myList) {

            initilizePlaceMaps();
            myList = _placeOverflow;
        }
        _placeOverflow = null;
        return myList;
    }

    public Map<String, PlaceStyle> getPlaceStyleMap() {

        if (null == _placeStyleMap) {

            initilizePlaceMaps();
        }
        return _placeStyleMap;
    }

    protected List<ValuePair<String, PlaceStyle>> getPlaceOverflow() {

        if (null == _placeOverflow) {

            initilizeAssociationMaps();
        }
        return _placeOverflow;
    }

    public void resetPlaceMaps() {

        _placeOverflow = null;
        _placeStyleMap = null;
    }

    protected void initilizePlaceMaps() {

        _placeOverflow = new ArrayList<ValuePair<String, PlaceStyle>>();
        _placeStyleMap = new TreeMap<String, PlaceStyle>();

        buildStyleMaps(placeStyles, _placeStyleMap, _placeOverflow);
    }

    public AssociationStyle findAssociationConflict(String itemIdIn) {

        return getAssociationStyleMap().get(itemIdIn);
    }

    public List<ValuePair<String, AssociationStyle>> getAssociationOverFlow() {

        List<ValuePair<String, AssociationStyle>> myList = _associationOverflow;

        if (null == myList) {

            initilizePlaceMaps();
            myList = _associationOverflow;
        }
        _associationOverflow = null;
        return myList;
    }

    public Map<String, AssociationStyle> getAssociationStyleMap() {

        if (null == _associationStyleMap) {

            initilizeAssociationMaps();
        }
        return _associationStyleMap;
    }

    protected List<ValuePair<String, AssociationStyle>> getAssociationOverflow() {

        if (null == _associationOverflow) {

            initilizeAssociationMaps();
        }
        return _associationOverflow;
    }

    public void resetAssociationMaps() {

        _associationOverflow = null;
        _associationStyleMap = null;
    }

    protected void initilizeAssociationMaps() {

        _associationOverflow = new ArrayList<ValuePair<String, AssociationStyle>>();
        _associationStyleMap = new TreeMap<String, AssociationStyle>();

        buildStyleMaps(associationStyles, _associationStyleMap, _associationOverflow);
    }

    @Override
    public MapTheme clone() {

        MapTheme myClone = new MapTheme();

        super.cloneComponents(myClone);
        return cloneValues(myClone);
    }

    private MapTheme cloneValues(MapTheme cloneIn) {

        cloneIn.setDefaultShape(defaultShape);
        cloneIn.setBundleStyle(bundleStyle);
        cloneIn.setPlaceStyles(clonePlaceStyles());
        cloneIn.setAssociationStyles(cloneAssociationStyles());
        return cloneIn;
    }

    private List<PlaceStyle> clonePlaceStyles() {

        List<PlaceStyle> myList = new ArrayList<PlaceStyle>();

        for (PlaceStyle myStyle : placeStyles) {

            myList.add(myStyle.clone());
        }
        return myList;
    }

    private List<AssociationStyle> cloneAssociationStyles() {

        List<AssociationStyle> myList = new ArrayList<AssociationStyle>();

        for (AssociationStyle myStyle : associationStyles) {

            myList.add(myStyle.clone());
        }
        return myList;
    }
}
