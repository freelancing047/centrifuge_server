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
package csi.client.gwt.viz.map.settings;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.IconCell;
import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.github.gwtbootstrap.client.ui.resources.ButtonSize;
import com.google.common.collect.Lists;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;
import com.sencha.gxt.widget.core.client.form.IntegerField;
import com.sencha.gxt.widget.core.client.form.validator.MaxNumberValidator;
import com.sencha.gxt.widget.core.client.form.validator.MinNumberValidator;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.GridView;
import com.sencha.gxt.widget.core.client.grid.editing.GridInlineEditing;
import csi.client.gwt.WebMain;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.maplayer.editor.MapLayerListHolder;
import csi.client.gwt.maplayer.editor.MapMapLayerEditorPresenter;
import csi.client.gwt.widget.combo_boxes.StringComboBox;
import csi.client.gwt.widget.gxt.grid.GridContainer;
import csi.client.gwt.widget.gxt.grid.GridHelper;
import csi.client.gwt.widget.gxt.grid.ResizeableGrid;
import csi.client.gwt.widget.ui.form.DragCell;
import csi.server.common.dto.user.UserSecurityInfo;
import csi.server.common.model.map.Basemap;
import csi.server.common.model.map.map.MapLayerDTO;
import csi.server.common.model.visualization.map.MapSettings;
import csi.server.common.model.visualization.map.MapTileLayer;
import csi.server.common.model.visualization.map.MapViewDef;
import csi.server.common.service.api.MapActionsServiceProtocol;
import csi.server.common.util.StringUtil;

import java.util.*;

/**
 * @author Centrifuge Systems, Inc.
 */
public class MapLayersTab extends MapSettingsComposite implements MapLayerListHolder {
    private static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
    private static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);
    private static MapTileLayerPropertyAccess properties = GWT.create(MapTileLayerPropertyAccess.class);
    @UiField
    StringComboBox layersField;
    @UiField
    Button buttonAddToGrid;
    @UiField
    Button buttonNew;
    @UiField
    GridContainer gridContainer;
    private UserSecurityInfo _userInfo;
    private MapMapLayerEditorPresenter mapBasemapEditorPresenter = new MapMapLayerEditorPresenter(this);
    private Map<String, Map<String, Basemap>> nameToBasemaps = null;
    private Map<String, Basemap> idToBasemap = null;
    private Map<String, Boolean> idToEditPermission = null;
    private Set<String> layerUuids;
    private Grid<MapTileLayerDTO> grid;

    public MapLayersTab() {
        super();
        initWidget(uiBinder.createAndBindUi(this));
        _userInfo = WebMain.injector.getMainPresenter().getUserInfo();
        initButton();
        initGrid();
    }

    private void initButton() {
        buttonAddToGrid.setIcon(IconType.CIRCLE_ARROW_DOWN);
        buttonAddToGrid.setType(ButtonType.LINK);
        buttonAddToGrid.setSize(ButtonSize.LARGE);
        Style buttonStyle = buttonAddToGrid.getElement().getStyle();
        buttonStyle.setFontSize(26.0D, Unit.PX);
        buttonStyle.setTextDecoration(Style.TextDecoration.NONE);
        buttonStyle.setPaddingLeft(0, Unit.PX);
        buttonStyle.setPaddingTop(0, Unit.PX);
        buttonStyle.setMarginBottom(0, Unit.PX);
        buttonStyle.setMarginTop(0, Unit.PX);
        layersField.addSelectionHandler(event -> addLayerFromList());
        buttonAddToGrid.addClickHandler(event -> addLayerFromList());
        if(!WebMain.getClientStartupInfo().getFeatureConfigGWT().isUseNewLogoutPage()) {
            buttonNew.addClickHandler(event -> {
                Basemap newBasemap = new Basemap();
                newBasemap.setOwner(_userInfo.getName());
                newBasemap.setName(newBasemapName());
                mapBasemapEditorPresenter.edit(newBasemap);
            });
        } else {
            buttonNew.setVisible(false);
        }
    }

    private void addLayerFromList() {
        addLayer(layersField.getCurrentValue());
    }

    private void addLayer(String layerName) {
        if (layerName == null)
            return;
        String basemapName = layerName;
        String ownerName = null;
        if (layerName.endsWith("]")) {
            String substr = layerName.substring(0, layerName.length() - 1);
            String[] strArr = StringUtil.split(substr, '[');
            basemapName = strArr[0].trim();
            ownerName = strArr[1];
        }

        Map<String, Basemap> basemaps = nameToBasemaps.get(basemapName);
        Basemap basemap = null;
        if (ownerName == null)
            for (Basemap b : basemaps.values())
                basemap = b;
        else
            basemap = basemaps.get(ownerName);
        if (basemap != null) {
            String layerUuid = basemap.getUuid();
            MapTileLayerDTO mapTileLayerDTO = new MapTileLayerDTO();
            mapTileLayerDTO.setLayerUuid(layerUuid);
            mapTileLayerDTO.setLayerName(getBasemapName(basemap));
            mapTileLayerDTO.setVisible(true);
            mapTileLayerDTO.setOpacity(100);
            mapTileLayerDTO.setCanEdit(idToEditPermission.getOrDefault(basemap.getUuid(), false));
            grid.getStore().add(mapTileLayerDTO);
            layerUuids.add(layerUuid);
        }

        populateBasemapField();
    }

    private String getBasemapName(Basemap basemap) {
        Map<String, Basemap> basemaps = nameToBasemaps.get(basemap.getName());
        if (basemaps.size() == 1)
            return basemap.getName();
        else
            return getNameWithOwnerString(basemap.getOwner(), basemap.getName());
    }

    private String getNameWithOwnerString(String ownerName, String basemapName) {
        return basemapName + "[" + ownerName + "]";
    }

    private void populateBasemapField() {
        layersField.getStore().clear();
        int index = 0;
        int defaultIndex = 0;
        for (Map.Entry<String, Map<String, Basemap>> entry : nameToBasemaps.entrySet()) {
            String baseMapName = entry.getKey();
            Map<String, Basemap> baseMaps = entry.getValue();
            boolean includeUserName = baseMaps.size() != 1;
            for (Map.Entry<String, Basemap> e : baseMaps.entrySet()) {
                String ownerName = e.getKey();
                Basemap basemap = e.getValue();
                if (!layerUuids.contains(basemap.getUuid())) {
                    if (includeUserName) {
                        layersField.getStore().add(getNameWithOwnerString(ownerName, baseMapName));
                    } else {
                        layersField.getStore().add(baseMapName);
                    }
                    if (baseMapName.equals(MapConfigProxy.instance().getDefaultBasemapId()) && ownerName.equals(MapConfigProxy.instance().getDefaultBasemapOwner()))
                        defaultIndex = index;
                    index++;
                }
            }
        }
        if (layersField.getStore().size() > 0) {
            layersField.setSelectedIndex(defaultIndex);
            String currentCellText = layersField.getStore().getAll().get(defaultIndex);
            layersField.setText(currentCellText);
        } else {
            layersField.setText("");
        }
    }

    private String newBasemapName() {
        String startingBasemapName = "Layer";
        String basemapName = startingBasemapName;
        int i = 1;
        while (basemapNameExists(_userInfo.getName(), basemapName)) {
            basemapName = startingBasemapName + " <" + i + ">";
            i++;
        }
        return basemapName;
    }

    @SuppressWarnings("unchecked")
    private void initGrid() {
        ColumnConfig<MapTileLayerDTO, Void> dragColumn = new ColumnConfig<>(properties.voidFn(), 20, "");
        final ColumnConfig<MapTileLayerDTO, String> nameColumn = new ColumnConfig<>(properties.layerName(), 150, i18n.mapSettingsView_layersTab_nameCol());
        final ColumnConfig<MapTileLayerDTO, Boolean> visibleColumn = new ColumnConfig<>(properties.visible(), 40, i18n.mapSettingsView_layersTab_visibleCol());
        final ColumnConfig<MapTileLayerDTO, Integer> opacityColumn = new ColumnConfig<>(properties.opacity(), 60, i18n.mapSettingsView_layersTab_opacityCol());
        final ColumnConfig<MapTileLayerDTO, Boolean> editColumn = new ColumnConfig<>(properties.canEdit(), 20, "");
        final ColumnConfig<MapTileLayerDTO, Void> deleteColumn = new ColumnConfig<>(properties.voidFn(), 20, "");

        dragColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        dragColumn.setCell(DragCell.create());
        dragColumn.setResizable(false);

        visibleColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        CheckboxCell checkboxCell = new CheckboxCell();
        visibleColumn.setCell(checkboxCell);

        editColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        {
            ValueBasedIconCell iconCell = new ValueBasedIconCell(IconType.PENCIL);
            iconCell.setTooltip(i18n.mapSettingsView_layersTab_editCellTooltip()); // $NON-NLS-1$
            editColumn.setCell(iconCell);
        }

        deleteColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        {
            IconCell iconCell = new IconCell(IconType.REMOVE);
            iconCell.setTooltip(i18n.mapSettingsView_layersTab_deleteTooltip()); // $NON-NLS-1$
            deleteColumn.setCell(iconCell);
        }

        List<ColumnConfig<MapTileLayerDTO, ?>> columnConfigs = Lists.newArrayList();
        columnConfigs.add(dragColumn);
        columnConfigs.add(nameColumn);
        columnConfigs.add(visibleColumn);
        columnConfigs.add(opacityColumn);
        columnConfigs.add(editColumn);
        columnConfigs.add(deleteColumn);

        ColumnModel<MapTileLayerDTO> columnModel = new ColumnModel<>(columnConfigs);

        final ListStore<MapTileLayerDTO> store = new ListStore<>(properties.layerUuid());

        grid = new ResizeableGrid<>(store, columnModel);
        grid.setColumnReordering(false);
        grid.setColumnResize(false);
        grid.setAllowTextSelection(false);

        GridView<MapTileLayerDTO> view = grid.getView();
        view.setAutoExpandColumn(nameColumn);
        view.setShowDirtyCells(false);
        view.setSortingEnabled(false);
        view.setAdjustForHScroll(true);

        GridHelper.setDraggableRowsDefaults(grid);
        gridContainer.setGrid(grid);

        grid.addCellClickHandler(event -> {
            int rowIndex = event.getRowIndex();
            int cellIndex = event.getCellIndex();
            MapTileLayerDTO mapTileLayerDTO = store.get(rowIndex);
            if (cellIndex == grid.getColumnModel().indexOf(editColumn)) {
                Basemap basemap = idToBasemap.get(mapTileLayerDTO.getLayerUuid());
                if (idToEditPermission.get(basemap.getUuid()))
                    mapBasemapEditorPresenter.edit(basemap);
            } else if (cellIndex == grid.getColumnModel().indexOf(deleteColumn))
                deleteFromList(mapTileLayerDTO);
        });

        // EDITING //
        final GridInlineEditing<MapTileLayerDTO> editing = new GridInlineEditing<>(grid);
        editing.setRevertInvalid(true);
        IntegerField integerField = new IntegerField();
        integerField.addValidator(new MinNumberValidator<>(0));
        integerField.addValidator(new MaxNumberValidator<>(100));
        integerField.setAllowBlank(false);
        integerField.setAllowDecimals(false);
        integerField.setAllowNegative(false);
        editing.addEditor(opacityColumn, integerField);
    }

    private void deleteFromList(MapTileLayerDTO mapTileLayerDTO) {
        grid.getStore().remove(mapTileLayerDTO);
        layerUuids.remove(mapTileLayerDTO.getLayerUuid());
        populateBasemapField();
    }

    @Override
    public void updateViewFromModel() {
        if (nameToBasemaps == null)
            populateState(() -> populateControls());
        else
            populateControls();
    }

    private void populateControls() {
        populateGrid();
        populateBasemapField();
    }

    private void populateGrid() {
        grid.getStore().clear();

        layerUuids = new TreeSet<>();
        MapViewDef mapViewDef = getVisualizationSettings().getVisualizationDefinition();
        MapSettings settings = mapViewDef.getMapSettings();
        if (settings.getTileLayers().size() > 0) {
            for (MapTileLayer mapTileLayer : settings.getTileLayers()) {
                String layerUuid = mapTileLayer.getLayerId();
                Basemap basemap = idToBasemap.get(layerUuid);
                if (basemap != null)
                    addToGrid(basemap, mapTileLayer.isVisible(), mapTileLayer.getOpacity());
            }
        } else {
            Map<String, Basemap> ownerToBasemap = nameToBasemaps.get(MapConfigProxy.instance().getDefaultBasemapId());
            if (ownerToBasemap != null) {
                Basemap basemap = ownerToBasemap.get(MapConfigProxy.instance().getDefaultBasemapOwner());
                if (basemap != null)
                    addToGrid(basemap, true, 100);
            }
        }
    }

    private void addToGrid(Basemap basemap, boolean visible, int opacity) {
        MapTileLayerDTO mapTileLayerDTO = new MapTileLayerDTO();
        mapTileLayerDTO.setLayerUuid(basemap.getUuid());
        mapTileLayerDTO.setLayerName(getBasemapName(basemap));
        mapTileLayerDTO.setVisible(visible);
        mapTileLayerDTO.setOpacity(opacity);
        mapTileLayerDTO.setCanEdit(idToEditPermission.getOrDefault(basemap.getUuid(), false));
        grid.getStore().add(mapTileLayerDTO);
        layerUuids.add(basemap.getUuid());
    }

    @Override
    public void updateModelWithView() {
        grid.getStore().commitChanges();
        MapViewDef mapViewDef = getVisualizationSettings().getVisualizationDefinition();
        MapSettings settings = mapViewDef.getMapSettings();
        settings.getTileLayers().clear();
        int index = 0;
        while (index < grid.getStore().size()) {
            MapTileLayerDTO mapTileLayerDTO = grid.getStore().get(index);
            MapTileLayer mapTileLayer = new MapTileLayer();
            mapTileLayer.setLayerId(mapTileLayerDTO.getLayerUuid());
            mapTileLayer.setVisible(mapTileLayerDTO.isVisible());
            mapTileLayer.setOpacity(mapTileLayerDTO.getOpacity());
            settings.getTileLayers().add(mapTileLayer);
            index++;
        }
    }

    private void populateState(PopulateStateCallback callback) {
        WebMain.injector.getVortex().execute((List<MapLayerDTO> basemapDTOs) -> {
            nameToBasemaps = new HashMap<>();
            idToBasemap = new HashMap<>();
            idToEditPermission = new HashMap<>();
            for (MapLayerDTO basemapDTO : basemapDTOs) {
                Basemap basemap = basemapDTO.getBasemap();
                Map<String, Basemap> ownerToBasemap;
                if (nameToBasemaps.containsKey(basemap.getName()))
                    ownerToBasemap = nameToBasemaps.get(basemap.getName());
                else {
                    ownerToBasemap = new HashMap<>();
                    nameToBasemaps.put(basemap.getName(), ownerToBasemap);
                }
                ownerToBasemap.put(basemap.getOwner(), basemap);
                idToBasemap.put(basemap.getUuid(), basemap);
                String userName = _userInfo.getName();
                String ownerName = basemap.getOwner();
                String basemapUuid = basemap.getUuid();
                if (userName.equals(ownerName))
                    idToEditPermission.put(basemapUuid, true);
                else
                    idToEditPermission.put(basemapUuid, basemapDTO.isCanEdit());
            }
            if (callback != null)
                callback.execute();
        }, MapActionsServiceProtocol.class).listBasemaps();
    }

    @Override
    public void populateBasemapNames() {
        populateState(() -> populateBasemapField());
    }

    @Override
    public boolean basemapNameExists(String ownerName, String basemapName) {
        if (nameToBasemaps.keySet().contains(basemapName)) {
            Map<String, Basemap> ownerToBasemap = nameToBasemaps.get(basemapName);
            return ownerToBasemap.containsKey(ownerName);
        }
        return false;
    }

    @Override
    public void notifyBasemapDeleted(Basemap basemap) {
        if (basemap != null) {
            String layerUuid = basemap.getUuid();
            String name = basemap.getName();
            String owner = basemap.getOwner();
            MapTileLayerDTO mapTileLayerDTO = new MapTileLayerDTO();
            mapTileLayerDTO.setLayerUuid(layerUuid);
            int index = grid.getStore().indexOf(mapTileLayerDTO);
            grid.getStore().remove(index);
            layerUuids.remove(mapTileLayerDTO.getLayerUuid());
            Map<String, Basemap> ownerToBasemaps = nameToBasemaps.get(name);
            if (ownerToBasemaps.size() == 1)
                nameToBasemaps.remove(name);
            else
                ownerToBasemaps.remove(owner);
            idToBasemap.remove(layerUuid);
            idToEditPermission.remove(layerUuid);
            WebMain.injector.getVortex().execute(MapActionsServiceProtocol.class).deleteBasemap(layerUuid);
        }
    }

    interface SpecificUiBinder extends UiBinder<Widget, MapLayersTab> {
    }

    interface MapTileLayerPropertyAccess extends PropertyAccess<MapTileLayerDTO> {
        ModelKeyProvider<MapTileLayerDTO> layerUuid();

        ValueProvider<MapTileLayerDTO, String> layerName();

        ValueProvider<MapTileLayerDTO, Boolean> asBasemap();

        ValueProvider<MapTileLayerDTO, Boolean> visible();

        ValueProvider<MapTileLayerDTO, Integer> opacity();

        ValueProvider<MapTileLayerDTO, Boolean> canEdit();

        ValueProvider<MapTileLayerDTO, Void> voidFn();
    }

    public interface PopulateStateCallback {
        void execute();
    }

    static class MapTileLayerDTO {
        private String layerUuid;
        private String layerName;
        private boolean visible;
        private int opacity;
        private boolean canEdit;

        MapTileLayerDTO() {
        }

        String getLayerUuid() {
            return layerUuid;
        }

        void setLayerUuid(String layerUuid) {
            this.layerUuid = layerUuid;
        }

        public String getLayerName() {
            return layerName;
        }

        public void setLayerName(String layerName) {
            this.layerName = layerName;
        }

        public boolean isVisible() {
            return visible;
        }

        public void setVisible(boolean visible) {
            this.visible = visible;
        }

        public int getOpacity() {
            return opacity;
        }

        public void setOpacity(int opacity) {
            this.opacity = opacity;
        }

        public boolean isCanEdit() {
            return canEdit;
        }

        public void setCanEdit(boolean canEdit) {
            this.canEdit = canEdit;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((layerUuid == null) ? 0 : layerUuid.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            MapTileLayerDTO other = (MapTileLayerDTO) obj;
            if (layerUuid == null) {
                return other.layerUuid == null;
            } else return layerUuid.equals(other.layerUuid);
        }
    }
}
