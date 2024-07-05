/**
 * Copyright (c) 2008 Centrifuge Systems, Inc.
 * All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of
 * Centrifuge Systems, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered
 * into with Centrifuge Systems.
 **/
package csi.server.common.model.visualization.map;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Type;

import csi.server.common.model.FieldDef;
import csi.server.common.model.ModelObject;
import csi.shared.core.color.ColorModel;
import csi.shared.core.color.ContinuousColorModel;

/**
 * @author Centrifuge Systems, Inc.
 */
@SuppressWarnings("serial")
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class MapSettings extends ModelObject implements Serializable {
    public static final float DEFAULT_PLACE_OPACITY = 55f;
    @OneToMany(cascade = CascadeType.ALL)
    @OrderBy("listPosition")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @Cascade(value = {org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN})
    private List<MapTileLayer> mapTileLayer = new ArrayList<MapTileLayer>();

    private Boolean useHeatMapField = Boolean.FALSE;

    private Boolean useBundleField = Boolean.FALSE;

    private Boolean useTrackField = Boolean.FALSE;

    private float nodeTransparency = DEFAULT_PLACE_OPACITY;

    @OneToMany(cascade = CascadeType.ALL)
    @OrderBy("listPosition")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @Cascade(value = {org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN})
    private List<MapBundleDefinition> mapBundleDefinition = new ArrayList<MapBundleDefinition>();

    @Type(type = "csi.server.dao.jpa.xml.SerializedXMLType")
    @Column(columnDefinition = "TEXT")
    private ColorModel colorModel = new ContinuousColorModel();

    @ManyToOne
    private FieldDef weightField;

    @OneToMany(cascade = CascadeType.ALL)
    @OrderBy("listPosition")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @Cascade(value = {org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN})
    private List<MapPlace> mapPlaces = new ArrayList<MapPlace>();

    @OneToMany(cascade = CascadeType.ALL)
    @OrderBy("listPosition")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @Cascade(value = {org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN})
    private List<MapAssociation> mapAssociations = new ArrayList<MapAssociation>();

    @OneToMany(cascade = CascadeType.ALL)
    @OrderBy("listPosition")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @Cascade(value = {org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN})
    private List<MapTrack> mapTracks = new ArrayList<MapTrack>();

    private String themeUuid;

    private int minPlaceSize = 4;
    private int maxPlaceSize = 20;

    public MapSettings() {
        super();
    }

    public List<MapTileLayer> getTileLayers() {
        return mapTileLayer;
    }

    public void setTileLayers(List<MapTileLayer> mapTileLayer) {
        this.mapTileLayer = mapTileLayer;
    }

    public Boolean isUseHeatMap() {
        return useHeatMapField;
    }

    public void setUseHeatMap(Boolean useHeatMapField) {
        this.useHeatMapField = useHeatMapField;
    }

    public Boolean isUseBundle() {
        return useBundleField;
    }

    public void setUseBundle(Boolean useBundleField) {
        this.useBundleField = useBundleField;
    }

    public Boolean isBundleUsed() {
        return isUseBundle() && !getMapBundleDefinitions().isEmpty();
    }

    public Boolean isUseTrack() {
        return useTrackField && !getMapTracks().isEmpty();
    }

    public void setUseTrack(Boolean useTrackField) {
        this.useTrackField = useTrackField;
    }

    public List<MapBundleDefinition> getMapBundleDefinitions() {
        return mapBundleDefinition;
    }

    public void setMapBundleDefinitions(List<MapBundleDefinition> mapBundleDefinition) {
        this.mapBundleDefinition = mapBundleDefinition;
    }

    public ColorModel getColorModel() {
        return colorModel;
    }

    public void setColorModel(ColorModel colorModel) {
        this.colorModel = colorModel;
    }

    public FieldDef getWeightField() {
        return weightField;
    }

    public void setWeightField(FieldDef weightField) {
        this.weightField = weightField;
    }

    public List<MapPlace> getMapPlaces() {
        if (mapPlaces == null) {
            mapPlaces = new ArrayList<MapPlace>();
        }
        return this.mapPlaces;
    }

    public void setMapPlaces(List<MapPlace> mapPlaces) {
        this.mapPlaces = mapPlaces;
    }

    public List<MapAssociation> getMapAssociations() {
        if (mapAssociations == null) {
            mapAssociations = new ArrayList<>();
        }
        return this.mapAssociations;
    }

    public void setMapAssociations(List<MapAssociation> mapAssociations) {
        this.mapAssociations = mapAssociations;
    }

    public List<MapTrack> getMapTracks() {
        if (mapTracks == null) {
            mapTracks = new ArrayList<>();
        }
        return this.mapTracks;
    }

    public void setMapTracks(List<MapTrack> mapTracks) {
        this.mapTracks = mapTracks;
    }

    public String getThemeUuid() {
        return themeUuid;
    }

    public void setThemeUuid(String themeUuid) {
        this.themeUuid = themeUuid;
    }

    public int getMinPlaceSize() {
        return minPlaceSize;
    }

    public void setMinPlaceSize(int minPlaceSize) {
        this.minPlaceSize = minPlaceSize;
    }

    public int getMaxPlaceSize() {
        return maxPlaceSize;
    }

    public void setMaxPlaceSize(int maxPlaceSize) {
        this.maxPlaceSize = maxPlaceSize;
    }

    public <T extends ModelObject> MapSettings copy(Map<String, T> fieldMapIn) {

        MapSettings myCopy = new MapSettings();

        super.cloneComponents(myCopy);

        myCopy.setTileLayers(copyTileLayers(fieldMapIn));
        myCopy.setUseHeatMap(isUseHeatMap());
        myCopy.setUseBundle(isUseBundle());
        myCopy.setMapBundleDefinitions(copyMapBundleDefinitions(fieldMapIn));
        myCopy.setColorModel(getColorModel());
        myCopy.setWeightField(getWeightField());
        myCopy.setMapPlaces(copyMapPlaces());
        myCopy.setMapAssociations(copyMapAssociations());
        myCopy.setMapTracks(copyMapTracks());
        myCopy.setThemeUuid(getThemeUuid());
        myCopy.setMinPlaceSize(getMinPlaceSize());
        myCopy.setMaxPlaceSize(getMaxPlaceSize());
        myCopy.setUseTrack(isUseTrack());
        myCopy.setUseBundle(isUseBundle());
        myCopy.setUseHeatMap(isUseHeatMap());
        myCopy.setNodeTransparency(getNodeTransparency());
        return myCopy;
    }

    private <T extends ModelObject> List<MapTileLayer> copyTileLayers(Map<String, T> fieldMapIn) {
        List<MapTileLayer> myList = new ArrayList<>();

        for (MapTileLayer myItem : getTileLayers()) {
            myList.add(myItem.copy(fieldMapIn));
        }

        return myList;
    }

    private <T extends ModelObject> List<MapBundleDefinition> copyMapBundleDefinitions(Map<String, T> fieldMapIn) {
        List<MapBundleDefinition> myList = new ArrayList<>();

        for (MapBundleDefinition myItem : getMapBundleDefinitions()) {
            myList.add(myItem.copy(fieldMapIn));
        }

        return myList;
    }

    private List<MapPlace> copyMapPlaces() {
        List<MapPlace> myList = new ArrayList<>();

        for (MapPlace myItem : getMapPlaces()) {
            myList.add(myItem.clone());
        }

        return myList;
    }

    private List<MapAssociation> copyMapAssociations() {
        List<MapAssociation> myList = new ArrayList<>();

        for (MapAssociation myItem : getMapAssociations()) {
            myList.add(myItem.clone());
        }

        return myList;
    }

    private List<MapTrack> copyMapTracks() {
        List<MapTrack> myList = new ArrayList<>();

        for (MapTrack myItem : getMapTracks()) {
            myList.add(myItem.clone());
        }

        return myList;
    }

    @Override
    public <T extends ModelObject> MapSettings clone(Map<String, T> fieldMapIn) {

        MapSettings myClone = new MapSettings();

        super.cloneComponents(myClone);

        myClone.setTileLayers(copyTileLayers(fieldMapIn));
        myClone.setUseHeatMap(isUseHeatMap());
        myClone.setUseBundle(isUseBundle());
        myClone.setMapBundleDefinitions(copyMapBundleDefinitions(fieldMapIn));
        myClone.setColorModel(getColorModel());
        myClone.setWeightField(getWeightField());
        myClone.setMapPlaces(cloneMapPlaces());
        myClone.setMapAssociations(cloneMapAssociations());
        myClone.setMapTracks(cloneMapTracks());
        myClone.setThemeUuid(getThemeUuid());
        myClone.setMinPlaceSize(getMinPlaceSize());
        myClone.setMaxPlaceSize(getMaxPlaceSize());
        myClone.setUseTrack(isUseTrack());
        myClone.setUseBundle(isUseBundle());
        myClone.setUseHeatMap(isUseHeatMap());
        myClone.setNodeTransparency(getNodeTransparency());

        return myClone;
    }

    private List<MapPlace> cloneMapPlaces() {
        List<MapPlace> myList = new ArrayList<>();

        for (MapPlace myItem : getMapPlaces()) {
            myList.add(myItem.clone());
        }

        return myList;
    }

    private List<MapAssociation> cloneMapAssociations() {
        List<MapAssociation> myList = new ArrayList<>();

        for (MapAssociation myItem : getMapAssociations()) {
            myList.add(myItem.clone());
        }

        return myList;
    }

    private List<MapTrack> cloneMapTracks() {
        List<MapTrack> myList = new ArrayList<>();

        for (MapTrack myItem : getMapTracks()) {
            myList.add(myItem.clone());
        }

        return myList;
    }

    public float getNodeTransparency() {
        return nodeTransparency;
    }

    public void setNodeTransparency(float nodeTransparency) {
        this.nodeTransparency = nodeTransparency;
    }
}