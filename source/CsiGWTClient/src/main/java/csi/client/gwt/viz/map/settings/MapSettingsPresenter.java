package csi.client.gwt.viz.map.settings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.gwtbootstrap.client.ui.Tab;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;

import csi.client.gwt.WebMain;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.util.name.UniqueNameUtil;
import csi.client.gwt.validation.feedback.StringValidationFeedback;
import csi.client.gwt.validation.feedback.ValidationFeedback;
import csi.client.gwt.validation.multi.ValidationAndFeedbackPair;
import csi.client.gwt.validation.validator.NotBlankValidator;
import csi.client.gwt.validation.validator.Validator;
import csi.client.gwt.validation.validator.VisualizationUniqueNameValidator;
import csi.client.gwt.viz.map.association.settings.MapAssociationDialog;
import csi.client.gwt.viz.map.menu.MapSettingsHandler;
import csi.client.gwt.viz.map.place.settings.MapPlaceDialog;
import csi.client.gwt.viz.map.track.settings.AppearanceTab;
import csi.client.gwt.viz.map.track.settings.MapTrackDialog;
import csi.client.gwt.viz.shared.settings.AbstractSettingsPresenter;
import csi.client.gwt.viz.shared.settings.SettingsActionCallback;
import csi.client.gwt.viz.shared.settings.VisualizationSettingsModal;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.CsiTabPanel;
import csi.client.gwt.widget.boot.Dialog;
import csi.server.common.dto.SelectionListData.ResourceBasics;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.graphics.shapes.ShapeType;
import csi.server.common.model.ColorWheel;
import csi.server.common.model.themes.map.AssociationStyle;
import csi.server.common.model.themes.map.MapTheme;
import csi.server.common.model.themes.map.PlaceStyle;
import csi.server.common.model.visualization.VisualizationType;
import csi.server.common.model.visualization.map.MapAssociation;
import csi.server.common.model.visualization.map.MapPlace;
import csi.server.common.model.visualization.map.MapSettings;
import csi.server.common.model.visualization.map.MapTrack;
import csi.server.common.model.visualization.map.MapViewDef;
import csi.server.common.service.api.ThemeActionsServiceProtocol;
import csi.shared.core.color.ClientColorHelper;
import csi.shared.core.color.ClientColorHelper.Color;

public class MapSettingsPresenter extends AbstractSettingsPresenter<MapViewDef> {
    private static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
    private Map<String, ResourceBasics> themeIdToResource = null;
    private String defaultThemeUuid;
    private boolean isNewViz = false;

    @UiField
    Tab bootGeneralTab;
    @UiField
    MapGeneralTab generalTab;

    @UiField
    Tab bootLayersTab;
    @UiField
    MapLayersTab layersTab;

    @UiField
    Tab bootPlacesTab;
    @UiField
    MapPlacesTab placesTab;

    @UiField
    Tab bootAssociationsTab;
    @UiField
    MapAssociationsTab associationsTab;

    @UiField
    Tab bootHeatmapTab;
    @UiField
    MapHeatmapTab heatmapTab;

    @UiField
    Tab bootBundleTab;
    @UiField
    MapBundleTab bundleTab;

    @UiField
    Tab bootTracksTab;
    @UiField
    AppearanceTab tracksTab;
    @UiField
    CsiTabPanel tabPanel;
    private List<MapPlace> mapPlaces;

    @UiTemplate("MapSettingsView.ui.xml")
    interface SpecificUiBinder extends UiBinder<VisualizationSettingsModal, MapSettingsPresenter> {
    }

    private static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);

    public MapSettingsPresenter(SettingsActionCallback<MapViewDef> settingsActionCallback) {
        super(settingsActionCallback);
        if (settingsActionCallback instanceof MapSettingsHandler)
            isNewViz = false;
        else
            isNewViz = true;
    }

    public void populateThemeIdToResource(final PopulateThemeIdToResourceCallback callback) {
        themeIdToResource = Maps.newHashMap();
        try {
            WebMain.injector.getVortex().execute((List<ResourceBasics> result) -> {
                String defaultThemeName = MapConfigProxy.instance().getDefaultThemeName();
                // Only will be a name on new maps with default names
                for (ResourceBasics resourceBasics : result) {
                    String displayName = resourceBasics.getDisplayName();
                    String uuid = resourceBasics.getUuid();
                    themeIdToResource.put(uuid, resourceBasics);
                    if (defaultThemeName != null && defaultThemeName.equals(displayName)) {
                        defaultThemeUuid = uuid;
                    }
                }
                if (createMode) {
                    getVisualizationDef().getMapSettings().setThemeUuid(defaultThemeUuid);
                }
                placesTab.populateThemes();
                callback.actionComplete();
            }, ThemeActionsServiceProtocol.class).listThemesByType(VisualizationType.GEOSPATIAL_V2);
        } catch (CentrifugeException e) {
        }
    }

    public Map<String, ResourceBasics> getThemeIdToResource() {
        return themeIdToResource;
    }

    @Override
    protected void bindUI() {
        mapPlaces = Lists.newArrayList(getVisualizationDef().getMapSettings().getMapPlaces());
        uiBinder.createAndBindUi(this);
        generalTab.setPresenter(this);
        placesTab.setPresenter(this);
        associationsTab.setPresenter(this);
        tracksTab.setPresenter(this);
    }

    @Override
    protected MapViewDef createNewVisualizationDef() {
        MapViewDef def = new MapViewDef();
        def.setBroadcastListener(WebMain.getClientStartupInfo().isListeningByDefault());
        MapSettings mapSettings = new MapSettings();
        mapSettings.setMinPlaceSize(MapConfigProxy.instance().getMinPlaceSize());
        mapSettings.setMaxPlaceSize(MapConfigProxy.instance().getMaxPlaceSize());
        def.setMapSettings(mapSettings);
        String name = UniqueNameUtil.getDistinctName(UniqueNameUtil.getVisualizationNames(dataViewPresenter),
                i18n.mapSettingsPresenter_name());
        def.setName(name);
        return def;
    }

    @Override
    protected void saveVisualizationToServer() {
        VortexFuture<Void> future = getVisualization().saveSettings(false, true);
        future.addEventHandler(new AbstractVortexEventHandler<Void>() {

            @Override
            public void onSuccess(Void result) {
                vizSettings.hide();
                settingsActionCallback.onSaveComplete(getVisualizationDef(), vizSettings.isSuppressLoadOnSave());
            }
        });
    }

    private Integer minIconSizeValue;
    private Integer maxIconSizeValue;

    @Override
    protected void initiateValidator() {
        NotBlankValidator notBlankValidator = new NotBlankValidator(generalTab.mapName);

        VisualizationUniqueNameValidator visualizationUniqueNameValidator = new VisualizationUniqueNameValidator(
                getDataViewDef().getModelDef().getVisualizations(), generalTab.mapName,
                getVisualizationDef().getUuid());

        Validator minIconSizeValidator = new Validator() {
            @Override
            public boolean isValid() {
                boolean isValid = false;
                String s = generalTab.minSizeTextBox.getValue();
                try {
                    minIconSizeValue = Integer.parseInt(s);
                    isValid = true;
                } catch (NumberFormatException nfe) {
                }
                return isValid;
            }
        };
        ValidationFeedback minIconSizeFeedback = new StringValidationFeedback(
                CentrifugeConstantsLocator.get().mapSettingsValidatingMinIconSize());

        Validator maxIconSizeValidator = new Validator() {
            @Override
            public boolean isValid() {
                boolean isValid = false;
                String s = generalTab.maxSizeTextBox.getValue();
                try {
                    maxIconSizeValue = Integer.parseInt(s);
                    isValid = true;
                } catch (NumberFormatException nfe) {
                }
                return isValid;
            }
        };
        ValidationFeedback maxIconSizeFeedback = new StringValidationFeedback(
                CentrifugeConstantsLocator.get().mapSettingsValidatingMaxIconSize());

        Validator minIconSizeValueTooSmallValidator = new Validator() {
            @Override
            public boolean isValid() {
                if (minIconSizeValue != null)
                    return minIconSizeValue >= 4;
                return true;
            }
        };
        ValidationFeedback minIconSizeValueTooSmallFeedback = new StringValidationFeedback(
                CentrifugeConstantsLocator.get().mapSettingsValidatingMinIconSizeTooSmall());

        Validator maxIconSizeValueTooLargeValidator = new Validator() {
            @Override
            public boolean isValid() {
                if (maxIconSizeValue != null)
                    return maxIconSizeValue <= 500;
                return true;
            }
        };
        ValidationFeedback maxIconSizeValueTooLargeFeedback = new StringValidationFeedback(
                CentrifugeConstantsLocator.get().mapSettingsValidatingMaxIconSizeTooLargeString());

        Validator minmaxIconSizeValueValidator = new Validator() {
            @Override
            public boolean isValid() {
                if (minIconSizeValue != null && maxIconSizeValue != null)
                    return maxIconSizeValue >= minIconSizeValue;
                return true;
            }
        };
        ValidationFeedback minmaxIconSizeValueFeedback = new StringValidationFeedback(
                CentrifugeConstantsLocator.get().mapSettingsValidatingMinMaxIconSizeValue());

        Validator placesNotEmptyValidator = new Validator() {
            @Override
            public boolean isValid() {
                List<MapPlace> mapPlaces = getMapPlaces();
                return !mapPlaces.isEmpty();
            }
        };
        ValidationFeedback placesNotEmptyFeedback = new StringValidationFeedback(
                CentrifugeConstantsLocator.get().mapSettingsValidatingPlacesNotEmpty());

        validator.addValidationAndFeedback(new ValidationAndFeedbackPair(notBlankValidator,
                StringValidationFeedback.getEmptyVisualizationFeedback()));
        validator.addValidationAndFeedback(new ValidationAndFeedbackPair(visualizationUniqueNameValidator,
                StringValidationFeedback.getDuplicateVisualizationFeedback()));
        validator.addValidationAndFeedback(new ValidationAndFeedbackPair(minIconSizeValidator, minIconSizeFeedback));
        validator.addValidationAndFeedback(new ValidationAndFeedbackPair(maxIconSizeValidator, maxIconSizeFeedback));
        validator.addValidationAndFeedback(
                new ValidationAndFeedbackPair(minIconSizeValueTooSmallValidator, minIconSizeValueTooSmallFeedback));
        validator.addValidationAndFeedback(
                new ValidationAndFeedbackPair(maxIconSizeValueTooLargeValidator, maxIconSizeValueTooLargeFeedback));
        validator.addValidationAndFeedback(
                new ValidationAndFeedbackPair(minmaxIconSizeValueValidator, minmaxIconSizeValueFeedback));
        validator.addValidationAndFeedback(
                new ValidationAndFeedbackPair(placesNotEmptyValidator, placesNotEmptyFeedback));
    }

    private MapPlaceDialog placeDialog;
    private MapAssociationDialog associationDialog;
    private MapTrackDialog trackDialog;

    protected void newPlace() {
        if (placeDialog == null) {
            placeDialog = new MapPlaceDialog(this);
        }
        MapPlace mapPlace = new MapPlace();
        List<String> currentNames = getPlaceCurrentNames();
        String distinctName = UniqueNameUtil.getDistinctName(currentNames, i18n.map_place_name());
        mapPlace.setName(distinctName);
        if (mapTheme != null) {
            if (mapTheme.getDefaultShape() != null) {
                mapPlace.setShapeTypeString(mapTheme.getDefaultShape().toString());
            }
        }
        placeDialog.setSelection(mapPlace);
        placeDialog.show();
    }

    private List<String> getPlaceCurrentNames() {
        List<String> currentNames = new ArrayList<String>();
        List<MapPlace> mapPlaces = getMapPlaces();
        if (mapPlaces != null) {
            for (MapPlace mapPlace : mapPlaces) {
                currentNames.add(mapPlace.getName());
            }
        }
        return currentNames;
    }

    public void saveMapPlace(MapPlace mapPlace) {
        if (mapPlaces == null || mapPlaces.size() == 0) {
            mapPlaces = new ArrayList<MapPlace>();
        }
        if (!mapPlaces.contains(mapPlace)) {
            mapPlaces.add(mapPlace);
        }
        tracksTab.updatePlaces(mapPlaces);
        placesTab.updateViewFromModel();
        placeDialog.hide();
    }

    public List<MapPlace> getMapPlaces() {
        return mapPlaces;
    }

    protected void editPlace(MapPlace mapPlace) {
        if (placeDialog == null) {
            placeDialog = new MapPlaceDialog(this);
        }
        List<MapPlace> mapPlaces = getMapPlaces();
        if (mapPlaces == null) {
            mapPlaces = new ArrayList<MapPlace>();
        }

        placeDialog.setSelection(mapPlace);
        placeDialog.show();
    }

    public void removePlace(MapPlace mapPlace) {
        String mapPlaceName = mapPlace.getName();
        boolean mapPlaceUsed = false;
        for (MapAssociation mapAssociation : getVisualizationDef().getMapSettings().getMapAssociations()) {
            if (mapAssociation.getSource().equals(mapPlaceName)
                    || mapAssociation.getDestination().equals(mapPlaceName)) {
                mapPlaceUsed = true;
                break;
            }
        }
        if (!mapPlaceUsed) {
            for (MapTrack mapTrack : getVisualizationDef().getMapSettings().getMapTracks()) {
                if (mapTrack.getPlace().equals(mapPlaceName)) {
                    mapPlaceUsed = true;
                    break;
                }
            }
        }
        if (mapPlaceUsed) {
            Dialog.showContinueDialog(i18n.mapSettingsView_placesTab_MapInUseTitle(),
                    i18n.mapSettingsView_placesTab_MapInUseMsg(), new ClickHandler() {
                        @Override
                        public void onClick(ClickEvent clickEvent) {
                            removePlaceNow(mapPlace);

                        }
                    });
        } else {
         removePlaceNow(mapPlace);
        }
    }

    private void removePlaceNow(MapPlace mapPlace) {
        getMapPlaces().remove(mapPlace);
        placesTab.updateViewFromModel();
        tracksTab.updatePlaces(mapPlaces);
        List<MapAssociation> collect = getVisualizationDef().getMapSettings().getMapAssociations().stream().filter(mapAssociation -> !mapAssociation.getSource().equals(mapPlace.getName()) && !mapAssociation.getDestination().equals(mapPlace.getName())).collect(Collectors.toList());
        getVisualizationDef().getMapSettings().setMapAssociations(collect);
        associationsTab.updateViewFromModel();
    }

    protected void newAssociation() {
        if (associationDialog == null) {
            associationDialog = new MapAssociationDialog(this);
        }
        MapAssociation mapAssociation = new MapAssociation();
        List<String> currentNames = getAssociationCurrentNames();
        String distinctName = UniqueNameUtil.getDistinctName(currentNames, i18n.map_association_name());
        mapAssociation.setName(distinctName);
        associationDialog.setSelection(mapAssociation);
        associationDialog.show();
    }

    private List<String> getAssociationCurrentNames() {
        List<String> currentNames = new ArrayList<String>();
        List<MapAssociation> mapAssociations = getVisualizationDef().getMapSettings().getMapAssociations();
        if (mapAssociations != null) {
            for (MapAssociation mapAssociation : mapAssociations) {
                currentNames.add(mapAssociation.getName());
            }
        }
        return currentNames;
    }

    public void saveMapAssociation(MapAssociation mapAssociation) {
        List<MapAssociation> mapAssociations = getVisualizationDef().getMapSettings().getMapAssociations();
        if (mapAssociations == null || mapAssociations.size() == 0) {
            mapAssociations = new ArrayList<MapAssociation>();
            getVisualizationDef().getMapSettings().setMapAssociations(mapAssociations);
        }
        if (!mapAssociations.contains(mapAssociation)) {
            mapAssociations.add(mapAssociation);
        }
        associationsTab.updateViewFromModel();
        associationDialog.hide();
    }

    protected void editAssociation(MapAssociation mapAssociation) {
        if (associationDialog == null) {
            associationDialog = new MapAssociationDialog(this);
        }
        List<MapAssociation> mapAssociations = getVisualizationDef().getMapSettings().getMapAssociations();
        if (mapAssociations == null) {
            mapAssociations = new ArrayList<MapAssociation>();
        }

        getVisualizationDef().getMapSettings().setMapAssociations(mapAssociations);
        associationDialog.setSelection(mapAssociation);
        associationDialog.show();
    }

    public void removeAssociation(MapAssociation mapAssociation) {
        getVisualizationDef().getMapSettings().getMapAssociations().remove(mapAssociation);
        associationsTab.updateViewFromModel();
    }

    protected void newTrack() {
        if (trackDialog == null) {
            trackDialog = new MapTrackDialog(this);
        }
        MapTrack mapTrack = new MapTrack();
        List<String> currentNames = getTrackCurrentNames();
        String distinctName = UniqueNameUtil.getDistinctName(currentNames, i18n.map_track_name());
//        mapTrack.setName(distinctName);
        trackDialog.setSelection(mapTrack);
        trackDialog.show();
    }

    private List<String> getTrackCurrentNames() {
        List<String> currentNames = new ArrayList<String>();
        /*List<MapTrack> mapTracks = getVisualizationDef().getMapSettings().getMapTracks();
        if (mapTracks != null) {
            for (MapTrack mapTrack : mapTracks) {
                currentNames.add(mapTrack.getName());
            }
        }*/
        return currentNames;
    }

    public void saveMapTrack(MapTrack mapTrack) {
        List<MapTrack> mapTracks = getVisualizationDef().getMapSettings().getMapTracks();
        if (mapTracks == null || mapTracks.size() == 0) {
            mapTracks = new ArrayList<>();
            getVisualizationDef().getMapSettings().setMapTracks(mapTracks);
        }
        if (!mapTracks.contains(mapTrack)) {
            mapTracks.add(mapTrack);
        }
        tracksTab.updateViewFromModel();
        trackDialog.hide();
    }

    void editTrack(MapTrack mapTrack) {
        if (trackDialog == null) {
            trackDialog = new MapTrackDialog(this);
        }
        List<MapTrack> mapTracks = getVisualizationDef().getMapSettings().getMapTracks();
        if (mapTracks == null) {
            mapTracks = new ArrayList<>();
        }

        getVisualizationDef().getMapSettings().setMapTracks(mapTracks);
        trackDialog.setSelection(mapTrack);
        trackDialog.show();
    }

    public void removeTrack(MapTrack mapTrack) {
        getVisualizationDef().getMapSettings().getMapTracks().remove(mapTrack);
        tracksTab.updateViewFromModel();
    }

    public void cancelMapPlaceDefinition() {
        placeDialog.hide();
    }

    public void cancelMapAssociationDefinition() {
        associationDialog.hide();
    }

    public void cancelMapTrackDefinition() {
        trackDialog.hide();
    }

    private Map<String, PlaceStyle> placeNameToStyleMap = null;

    public PlaceStyle getPlaceStyle(String placeName) {
        if (placeNameToStyleMap != null) {
            return placeNameToStyleMap.get(placeName);
        } else {
            return null;
        }
    }

    public ShapeType getDefaultShape() {
        return defaultShape;
    }

    private Map<String, AssociationStyle> associationNameToStyleMap = null;

    public AssociationStyle getAssociationStyle(String associationName) {
        if (associationNameToStyleMap != null) {
            return associationNameToStyleMap.get(associationName);
        } else {
            return null;
        }
    }

    private MapTheme mapTheme;
    private ShapeType defaultShape;

    public void applyTheme(MapTheme mapTheme) {
        if (mapTheme == null) {
            defaultShape = null;
            placeNameToStyleMap = null;
            associationNameToStyleMap = null;
            for (MapPlace mapPlace : getMapPlaces()) {
                if (mapPlace.isUseDefaultIconSetting()) {
                    mapPlace.setIconId(null);
                }

                if (this.mapTheme != null && mapPlace.isUseDefaultShapeSetting()) {
                    mapPlace.setShapeTypeString(ShapeType.getNextNodeShape().toString());
                    Color color = ClientColorHelper.get().make(ColorWheel.next());
                    mapPlace.setColorString(color.toString());
                }
            }
            return;
        }

        this.mapTheme = mapTheme;
        placeNameToStyleMap = new HashMap<String, PlaceStyle>();
        for (PlaceStyle placeStyle : mapTheme.getPlaceStyles()) {
            for (String placeName : placeStyle.getFieldNames()) {
                if (!placeNameToStyleMap.keySet().contains(placeName)) {
                    placeNameToStyleMap.put(placeName, placeStyle);
                }
            }
        }
        defaultShape = mapTheme.getDefaultShape();
        associationNameToStyleMap = new HashMap<String, AssociationStyle>();
        for (AssociationStyle associationStyle : mapTheme.getAssociationStyles()) {
            for (String associationName : associationStyle.getFieldNames()) {
                if (!associationNameToStyleMap.keySet().contains(associationName)) {
                    associationNameToStyleMap.put(associationName, associationStyle);
                }
            }
        }
        for (MapPlace mapPlace : getMapPlaces()) {
            String typeName = mapPlace.getTypeName();
            if (typeName == null || typeName.trim().length() == 0) {
                typeName = mapPlace.getName();
            }
            PlaceStyle placeStyle = getPlaceStyle(typeName);
            if (placeStyle != null) {
                if (mapPlace.isUseDefaultIconSetting()) {
                    mapPlace.setIconId(placeStyle.getIconId());
                }
                if (mapPlace.isUseDefaultShapeSetting()) {
                    Color color = ClientColorHelper.get().make(placeStyle.getColor());
                    mapPlace.setColorString(color.toString());
                    ShapeType shapeType = placeStyle.getShape();
                    if (shapeType == null) {
                        shapeType = mapTheme.getDefaultShape();
                    }
                    if (shapeType != null) {
                        mapPlace.setShapeTypeString(shapeType.toString());
                    }
                }
            } else {
                if (mapPlace.isUseDefaultIconSetting()) {
                    mapPlace.setIconId(null);
                }
                if (mapPlace.isUseDefaultShapeSetting()) {
                    if (mapTheme.getDefaultShape() != null) {
                        mapPlace.setShapeTypeString(mapTheme.getDefaultShape().toString());
                    }
                }
            }
        }
        for (MapAssociation mapAssociation : getVisualizationDef().getMapSettings().getMapAssociations()) {
            AssociationStyle associationStyle = getAssociationStyle(mapAssociation.getName());
            if (associationStyle != null) {
                if (mapAssociation.isUseDefaultColorSetting()) {
                    Color color = ClientColorHelper.get().make(associationStyle.getColor());
                    mapAssociation.setColorString(color.toString());
                }
                if (mapAssociation.isUseDefaultWidthSetting()) {
                    mapAssociation.setWidth(associationStyle.getWidth().intValue());
                }
            }
        }
    }

    public MapTheme getTheme() {
        return mapTheme;
    }


    public void showBundleTab(boolean show) {
        bootBundleTab.asWidget().setVisible(show);
    }

    public void showTrackTab(boolean show) {
        bootTracksTab.asWidget().setVisible(show);
    }

    public void showHeatmapTab(boolean show) {
        bootHeatmapTab.asWidget().setVisible(show);
    }

    public void showAssociationsTab(boolean show) {
        bootAssociationsTab.asWidget().setVisible(show);
    }

    public void setMapPlaces(List<MapPlace> places) {
        mapPlaces = Lists.newArrayList(places);
    }

}
