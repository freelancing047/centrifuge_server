/** 
 *  Copyright (c) 2008 Centrifuge Systems, Inc. 
 *  All rights reserved. 
 *   
 *  This software is the confidential and proprietary information of 
 *  Centrifuge Systems, Inc. ("Confidential Information").  You shall 
 *  not disclose such Confidential Information and shall use it only
 *  in accordance with the terms of the license agreement you entered 
 *  into with Centrifuge Systems.
 *
 **/
package csi.client.gwt.viz.timeline.settings;

import java.util.ArrayList;
import java.util.List;

import com.github.gwtbootstrap.client.ui.IconCell;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.common.collect.Lists;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;
import com.sencha.gxt.widget.core.client.event.CellClickEvent;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.GridView;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.shared.settings.VisualizationSettings;
import csi.client.gwt.viz.timeline.view.TimelineTimeCell;
import csi.client.gwt.widget.cells.readonly.FieldDefNameCell;
import csi.client.gwt.widget.gxt.grid.GridContainer;
import csi.client.gwt.widget.gxt.grid.ResizeableGrid;
import csi.server.common.model.FieldDef;
import csi.server.common.model.visualization.timeline.TimelineEventDefinition;
import csi.server.common.model.visualization.timeline.TimelineTimeSetting;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class TimelineEventsTab extends TimelineSettingsComposite {

    TimelineEventDefinitionModelProperties props = GWT.create(TimelineEventDefinitionModelProperties.class);
    
    private TimelineSettingsPresenter presenter;
    
    @UiField
    GridContainer gridContainer;

    @UiField(provided = true)
    String heading = i18n.timelineSettings_eventsTab_eventDefinitions();
    @UiField(provided = true)
    String newButtonText = i18n.timelineSettings_eventsTab_newDefinition();
    
    ResizeableGrid<TimelineEventDefinition> grid;
    

    protected static final CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
    

    interface SpecificUiBinder extends UiBinder<Widget, TimelineEventsTab> {
    }

    private static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);

    public TimelineEventsTab() {
        super();
        
        initWidget(uiBinder.createAndBindUi(this));

        initGrid();
    }

    private void initGrid() {
        
        Cell<FieldDef> cell = new FieldDefNameCell();
        Cell<TimelineTimeSetting> timeCell = new TimelineTimeCell();
        
        ColumnConfig<TimelineEventDefinition, TimelineTimeSetting> startColumn = new ColumnConfig<TimelineEventDefinition, TimelineTimeSetting>(props.startField());
        {
            startColumn.setHeader(i18n.timelineSettings_eventsTab_start()); //$NON-NLS-1$
            startColumn.setWidth(150);
            startColumn.setCell(timeCell);
        }ColumnConfig<TimelineEventDefinition, TimelineTimeSetting> endColumn = new ColumnConfig<TimelineEventDefinition, TimelineTimeSetting>(props.endField());
        {
            endColumn.setHeader(i18n.timelineSettings_eventsTab_end()); //$NON-NLS-1$
            endColumn.setWidth(150);
            endColumn.setCell(timeCell);
        }ColumnConfig<TimelineEventDefinition, FieldDef> labelColumn = new ColumnConfig<TimelineEventDefinition, FieldDef>(props.labelField());
        {
            labelColumn.setHeader(i18n.timelineSettings_eventsTab_label()); //$NON-NLS-1$
            labelColumn.setWidth(150);
            labelColumn.setCell(cell);
        }
        final ColumnConfig<TimelineEventDefinition, Boolean> includeColumn = new ColumnConfig<TimelineEventDefinition, Boolean>(props.isSelected());
        {
            CheckboxCell checkboxCell = new CheckboxCell();
            includeColumn.setCell(checkboxCell);
            includeColumn.setWidth(20);
        }
        final ColumnConfig<TimelineEventDefinition, Void> deleteColumn = new ColumnConfig<TimelineEventDefinition, Void>(props.voidFn());
        {
            IconCell iconCell = new IconCell(IconType.REMOVE);
            iconCell.setTooltip(i18n.kmlExportDialogdeleteTooltip()); //$NON-NLS-1$
            deleteColumn.setCell(iconCell);
            deleteColumn.setWidth(20);

        } final ColumnConfig<TimelineEventDefinition, Void> editColumn = new ColumnConfig<TimelineEventDefinition, Void>(props.voidFn());
        {
            IconCell iconCell = new IconCell(IconType.PENCIL);
            iconCell.setTooltip(i18n.kmlExportDialogeditCellTooltip()); //$NON-NLS-1$
            editColumn.setCell(iconCell);
            editColumn.setWidth(20);

        }
        List<ColumnConfig<TimelineEventDefinition, ?>> columnConfigs = Lists.newArrayList();
//        columnConfigs.add(includeColumn);
        columnConfigs.add(startColumn);
        columnConfigs.add(endColumn);
        columnConfigs.add(labelColumn);
        columnConfigs.add(editColumn);
        columnConfigs.add(deleteColumn);

        ColumnModel<TimelineEventDefinition> columnModel = new ColumnModel<TimelineEventDefinition>(columnConfigs);
        
        ListStore<TimelineEventDefinition> store = new ListStore<TimelineEventDefinition>(props.uuid());
        
        grid = new ResizeableGrid<TimelineEventDefinition>(store, columnModel);
        GridView<TimelineEventDefinition> view = grid.getView();
        view.setShowDirtyCells(false);
        view.setSortingEnabled(false);
        view.setAdjustForHScroll(true);
        grid.setColumnReordering(false);
        grid.setColumnResize(false);
        grid.setAllowTextSelection(false);
        gridContainer.setGrid(grid);
        
        {
            grid.addCellClickHandler(new CellClickEvent.CellClickHandler() {

                @Override
                public void onCellClick(CellClickEvent event) {
                    int rowIndex = event.getRowIndex();
                    int cellIndex = event.getCellIndex();
                    ListStore<TimelineEventDefinition> store = grid.getStore();
                    TimelineEventDefinition eventDefinition = store.get(rowIndex);
                    int delColIndex = grid.getColumnModel().indexOf(deleteColumn);
                    if (cellIndex == delColIndex) {
                        store.remove(rowIndex);
                        presenter.deleteEvent(eventDefinition);
                    }
                }
            });
        }
        {
            grid.addCellClickHandler(new CellClickEvent.CellClickHandler() {

                @Override
                public void onCellClick(CellClickEvent event) {
                    int rowIndex = event.getRowIndex();
                    int cellIndex = event.getCellIndex();
                    ListStore<TimelineEventDefinition> store = grid.getStore();
                    TimelineEventDefinition eventDefinition = store.get(rowIndex);
                    int delColIndex = grid.getColumnModel().indexOf(editColumn);
                    if (cellIndex == delColIndex) {
                        presenter.editEvent(eventDefinition);
                    }
                }
            });
        }
    }

    @Override
    public void updateViewFromModel() {
        List<TimelineEventDefinition> events = getPresenter().visualizationDef.getTimelineSettings().getEvents();
        if(events == null){
            events = new ArrayList<TimelineEventDefinition>();
        }
        
        List<TimelineEventDefinition> badEvents = new ArrayList<TimelineEventDefinition>();
        for(TimelineEventDefinition eventDefinition: events){
            TimelineTimeSetting start = eventDefinition.getStartField();
            TimelineTimeSetting end = eventDefinition.getEndField();
            
            if(start != null){
                if(start.getFieldDef() == null){
                    badEvents.add(eventDefinition);
                    continue;
                }
            }
            
            if(end != null){
                if(end.getFieldDef() == null){
                    badEvents.add(eventDefinition);
                }
            }
        }
        
        events.removeAll(badEvents);
        getPresenter().visualizationDef.getTimelineSettings().setEvents(events);
        grid.getStore().clear();
        grid.getStore().addAll(events);
    }
    
    @Override
    public void setVisualizationSettings(VisualizationSettings visualizationSettings) {
        super.setVisualizationSettings(visualizationSettings);
        createEditor();
    }
    
    @Override
    public void updateModelWithView() {
    	
    }
    
    @UiHandler("newButton")
    void onNewButtonClick(ClickEvent event) {
        presenter.newDefinition();
    }
    
    private void createEditor() {
        

    }

    public TimelineSettingsPresenter getPresenter() {
        return presenter;
    }

    public void setPresenter(TimelineSettingsPresenter presenter) {
        this.presenter = presenter;
    }
    
    interface TimelineEventDefinitionModelProperties extends PropertyAccess<TimelineEventDefinition> {
        ValueProvider<TimelineEventDefinition, String> name();

        ValueProvider<TimelineEventDefinition, FieldDef> labelField();

        ValueProvider<TimelineEventDefinition, TimelineTimeSetting> endField();

        ValueProvider<TimelineEventDefinition, TimelineTimeSetting> startField();

        ModelKeyProvider<TimelineEventDefinition> uuid();

        ValueProvider<TimelineEventDefinition,Boolean> isSelected();

        ValueProvider<TimelineEventDefinition,Void> voidFn();
    }
    

}
