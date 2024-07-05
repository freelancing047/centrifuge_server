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
package csi.client.gwt.edit_sources.center_panel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.emitrom.lienzo.client.core.event.NodeDragMoveEvent;
import com.emitrom.lienzo.client.core.event.NodeDragMoveHandler;
import com.emitrom.lienzo.client.core.event.NodeMouseClickEvent;
import com.emitrom.lienzo.client.core.event.NodeMouseClickHandler;
import com.emitrom.lienzo.client.core.event.NodeMouseDoubleClickEvent;
import com.emitrom.lienzo.client.core.event.NodeMouseDoubleClickHandler;
import com.emitrom.lienzo.client.core.shape.Layer;
import com.emitrom.lienzo.client.core.shape.Scene;
import com.google.common.collect.Lists;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DragEnterEvent;
import com.google.gwt.event.dom.client.DragEnterHandler;
import com.google.gwt.event.dom.client.DragOverEvent;
import com.google.gwt.event.dom.client.DragOverHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.Panel;

import csi.client.gwt.WebMain;
import csi.client.gwt.edit_sources.DataSourceEditorModel;
import csi.client.gwt.edit_sources.center_panel.shapes.*;
import csi.client.gwt.edit_sources.presenters.DataSourceEditorPresenter;
import csi.client.gwt.edit_sources.DataSourceEditorState;
import csi.client.gwt.edit_sources.DataSourceEditorView;
import csi.client.gwt.edit_sources.dialogs.query.CustomQueryDialog;
import csi.client.gwt.csiwizard.support.ConnectionTreeItem;
import csi.client.gwt.etc.BaseCsiEvent;
import csi.client.gwt.etc.BaseCsiEventHandler;
import csi.client.gwt.events.CsiDropEvent;
import csi.client.gwt.events.CsiDropEventHandler;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.server.common.util.ConnectorSupport;
import csi.client.gwt.util.Display;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.gxt.drag_n_drop.ResizeableLienzoPanel;
import csi.server.common.dto.AuthDO;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.DataSetOp;
import csi.server.common.model.DataSourceDef;
import csi.server.common.model.SqlTableDef;
import csi.server.common.model.UUID;
import csi.server.common.model.column.ColumnDef;
import csi.server.common.model.operator.OpJoinType;
import csi.server.common.model.operator.OpMapItem;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class ConfigurationPresenter {

    private DataSourceEditorPresenter _dataSourceEditorPresenter;
    private DataSourceEditorModel _model;
    private DataSourceEditorView _view;
    private ResizeableLienzoPanel _display;
    private Integer _prefix = null;
    private CustomQueryDialog _queryDialog = null;
    private DataSetOp _activeDso = null;
    private List<WienzoComposite> _objectList = null;
    private String _selectedDso = null;
    private WienzoComposite _selectedItem = null;

    public static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();

    ClickHandler handleUpdateQuerySuccess
    = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {

            SqlTableDef myTable = _queryDialog.getTable();

            if (null != myTable) {

                _model.handleDsoUpdate(_activeDso, myTable, _activeDso.extractPrefix());
                _model.replaceQueryParameters(_queryDialog.getDataSetParameters());
                if (null != _selectedItem) {

                    _selectedItem.replaceTitle(_activeDso.getName());
                }
                destroyList();
                selectDso(_activeDso);
                draw();
                _model.setChanged();
            }
            _queryDialog.destroy();
//            _activeDso = null;
//            _view.clearAnyEditor();
        }
    };

    DragEnterHandler handleDragEnter = new DragEnterHandler() {
        @Override
        public void onDragEnter(DragEnterEvent event) {

        }
    };

    DragOverHandler handleDragOver = new DragOverHandler() {
        @Override
        public void onDragOver(DragOverEvent event) {

        }
    };

    CsiDropEventHandler handleDrop = new CsiDropEventHandler() {
        @Override
        public void onDrop(CsiDropEvent eventIn) {

            Object mySelection = eventIn.getSource();

            if (mySelection instanceof ConnectionTreeItem) {

                _view.dropItem((ConnectionTreeItem)mySelection);
            }
        }
    };

    public static void changeDisplayFormat(boolean isFullIn) {

        WienzoTable.changeDisplayFormat(isFullIn);
        ConnectionPoint.changeDisplayFormat(isFullIn);
    }

    public static void hideConnectors(boolean doHideIn) {

        WienzoDualChildComposite.hideConnectors(doHideIn);
        ConnectionPoint._hideDisplay(doHideIn);
    }

    
    public ConfigurationPresenter(DataSourceEditorPresenter dataSourceEditorPresenterIn, DataSourceEditorView viewIn) {
        _dataSourceEditorPresenter = dataSourceEditorPresenterIn;
        _view =  viewIn;
        _model = _dataSourceEditorPresenter.getModel();
        _display = new ResizeableLienzoPanel(_view);
        _display.addDragEnterHandler(handleDragEnter);
        _display.addDragOverHandler(handleDragOver);
        _display.addCsiDropEventHandler(handleDrop);
        initializePrefix(false);
    }

    public ConfigurationPresenter releaseDisplay() {

        _model = null;
        _view = null;
        return null;
    }

    public DataSourceEditorModel getModel() {

        return _model;
    }

    public void addViewToPanel(Panel panel) {
        panel.add(_display);
        destroyList();
        draw();
    }
    
    public void displayState(DataSourceEditorState stateIn) {

        _view.displayState(stateIn);
    }

    public void displayToolTip(String toolTipIn, String colorIn) {

        _view.displayToolTip(toolTipIn, colorIn);
    }

    public void displayToolTip(String toolTipIn) {

        _view.displayToolTip(toolTipIn);
    }

    public void clearToolTip() {
        
        _view.clearToolTip();
    }
    
    public void editCustomQuery(DataSetOp dsoIn, DataSourceDef dataSourceIn, SqlTableDef tableIn) {

        if (ConnectorSupport.getInstance().canExecuteQuery(dataSourceIn) && ConnectorSupport.getInstance().canEditQuery(dataSourceIn)) {

            Map<String, AuthDO> myAuthorizationMap = WebMain.injector.getMainPresenter().getAuthorizationMap();

            _activeDso = dsoIn;
            _queryDialog = new CustomQueryDialog(myAuthorizationMap, dataSourceIn, tableIn,
                    _model.createParameterPresenter(), handleUpdateQuerySuccess,
                    i18n.configurationPresenterUpdateDialogTitle(), _model.getQueryNameMap()); //$NON-NLS-1$

            _queryDialog.show();

        } else {

            Display.error(i18n.dataSourceEditor_CustomQuery(), i18n.blockingCustomQuery());
        }
    }

    public void selectObject(final WienzoComposite objectIn) {

        DeferredCommand.add(new Command() {
            public void execute() {

                for (WienzoComposite myObject : _objectList) {

                    myObject.setDeselected();
                }
                if (null != objectIn) {

                    objectIn.setSelected();

                } else {

 //                   _view.showParameters();
                }
                _selectedItem = objectIn;
                _display.getScene().draw();
            }
        });
    }

    public void selectDso(DataSetOp sourceIn) {

        if (null != sourceIn) {

            _selectedDso = sourceIn.getLocalId();
        }
    }

    public void recordSelection(WienzoComposite objectIn) {

        _selectedItem = objectIn;
    }

    public boolean itemSelected() {

        return (null != _selectedItem);
    }

    public WienzoComposite getSelectedItem() {

        return _selectedItem;
    }

    public boolean tableSelected() {

        return ((null != _selectedItem) && (_selectedItem instanceof WienzoTable));
    }

    public void deleteSelectedObject() {

        if (null != _selectedItem) {

            WienzoComposite myObject = _selectedItem;

            _selectedItem = null;
            myObject.destroy();
            deleteObject(myObject.getDso());
        }
    }

    public void replaceSelectedObject(SqlTableDef newTableIn) {

        if ((null != newTableIn) && (null != _selectedItem)) {

            if (_selectedItem instanceof WienzoTable) {

                DataSetOp myDso = _selectedItem.getDso();

                if (null != myDso) {

                    List<ColumnDef> myColumnList = newTableIn.getColumns();

                    if (null != myColumnList) {

                        for (ColumnDef myColumn : myColumnList) {

                            myColumn.setSelected(true);
                            myColumn.setTableDef(newTableIn);
                        }
                    }
                    _model.handleDsoUpdate(myDso, newTableIn, getPrefix());
                    destroyList();
                    selectDso(myDso);
                    draw();
                    _model.setChanged();
                }

            } else {

                Display.error("Replace JOIN or UNION", "Not Currently Supported!");
            }
        }
    }

    public void editSelectedObject() {

        if (null != _selectedItem) {

            _selectedItem.launchEditDisplay();
        }
    }

    public void showTableEditor(WienzoTable objectIn) {

        _view.showTableEditor(objectIn);
    }

    public void showJoinEditor(WienzoJoin objectIn) {

        _view.showJoinEditor(objectIn);
    }

    public void showAppendEditor(WienzoUnion objectIn) {

        _view.showAppendEditor(objectIn);
    }

    public void addTable(SqlTableDef tableIn, DataSourceDef dataSourceIn) {

        List<DataSetOp> myList = _model.getDataList();
        List<ColumnDef> myColumnList = tableIn.getColumns();

        tableIn.setLocalId(UUID.randomUUID());
        if (null != myColumnList) {

            for (ColumnDef myColumn : myColumnList) {

                myColumn.setLocalId(UUID.randomUUID());
                myColumn.setSelected(true);
                myColumn.setTableDef(tableIn);
            }
        }

        tableIn.setSource(dataSourceIn);

        DataSetOp dso = new DataSetOp();
        dso.setLocalId(UUID.randomUUID());
        dso.setMapItems(Lists.<OpMapItem>newArrayList());
        dso.setTableDef(tableIn);
        dso.createName(getPrefix());    // Must be last item set within the DSO to be generated correctly
        myList.add(dso);

        _model.handleDsoAdd(dso);
        destroyList();
        selectDso(dso);
        draw();
        _dataSourceEditorPresenter.enableTreeSelection();

        _model.setChanged();
    }

    public void fireEvent(BaseCsiEvent<? extends BaseCsiEventHandler> event) {
        _dataSourceEditorPresenter.fireEvent(event);
    }

    public void detachChild(DataSetOp discardIn, boolean keepIn) {

        List<DataSetOp> myList = _model.getDataList();

        discardIn.remove(myList);

        if (keepIn) {

            myList.add(discardIn);
        }
        clearFlags();
        destroyList();

        DeferredCommand.add(new Command() {
            public void execute() {

                _view.showParameters();
                draw();
                _model.setChanged();
            }
        });
    }

    public void attachSibbling(DataSetOp targetIn, DataSetOp objectIn, PortType requestIn) {

        List<DataSetOp> myList = _model.getDataList();

        DataSetOp myNewParent = new DataSetOp();

        clearFlags();

        myNewParent.setLocalId(UUID.randomUUID());
        myNewParent.setMapType(requestIn.getMapType());
        myNewParent.setJoinType(OpJoinType.EQUI_JOIN);
        myNewParent.createName(getPrefix());    // Must be last item set within the DSO to be generated correctly

        // Break any previous connection
        objectIn.remove(myList);

        try {

            targetIn.addSibbling(myNewParent, objectIn,
                    (requestIn.equals(PortType.APPEND_TOP)
                            || requestIn.equals(PortType.JOIN_LEFT)), myList);

        } catch (CentrifugeException myException) {

            Dialog.showException(myException);
        }

        _model.setChanged();
        destroyList();
        selectDso(myNewParent);
        draw();
    }

    public void refresh() {

        destroyList();
        draw();
    }

    public void destroyList() {

        clearToolTip();

        _selectedDso = (null != _selectedItem) ? _selectedItem.getDso().getLocalId() : null;
        _selectedItem = null;

        if ((null !=_objectList) && (0 < _objectList.size())) {

            for (int i = _objectList.size() - 1; 0 <= i; i--) {

                WienzoComposite myObject = _objectList.get(i);

                _objectList.remove(i);
                myObject.destroy();
            }
        }
    }

    private void draw() {

        List<DataSetOp> myDataList = _model.getDataList();

        _display.clear();
        _objectList = new ArrayList<WienzoComposite>();

        createBackDrop();

        Layer layer = new Layer();
        _display.add(layer);
        int y = 0;

        if (null != myDataList) {

            for (DataSetOp myDataSetOp : myDataList) {

                WienzoComposite composite = WienzoFactory.create(_objectList, _selectedDso, myDataSetOp, this, layer, _display);
                if (null != composite) {
                    composite.setY(y);
                    composite.draw();
                    y += composite.getHeight() + 30; // FIXME: Magic number
                    if (myDataSetOp.getLocalId() == _selectedDso) {

                        _selectedItem = composite;
                    }
                }
            }
        }
        _selectedDso = null;
        selectObject(_selectedItem);
        _display.getScene().draw();
        if (null != _view) {

            _view.clearSourceTreeSelection();
        }

        DeferredCommand.add(new Command() {
            public void execute() {
                Scene myScene = _display.getScene();

                if (null != myScene) {

                    myScene.draw();
                }
            }
        });
        if (null != _selectedItem) {

            _selectedItem.setSelected();
        }
    }

    private void createBackDrop() {

        Layer layer = new Layer();
        _display.add(layer);

        layer.addNodeMouseDoubleClickHandler(new NodeMouseDoubleClickHandler() {

            @Override
            public void onNodeMouseDoubleClick(NodeMouseDoubleClickEvent event) {

                selectObject(null);
            }
        });

        layer.addNodeMouseClickHandler(new NodeMouseClickHandler() {

            @Override
            public void onNodeMouseClick(NodeMouseClickEvent event) {

                selectObject(null);
            }
        });

        layer.addNodeDragMoveHandler(new NodeDragMoveHandler() {
            @Override
            public void onNodeDragMove(NodeDragMoveEvent eventIn) {

                selectObject(null);
            }
        });
    }

    private void deleteObject(DataSetOp discardIn) {

        _model.handleDsoDelete(discardIn);

        discardIn.remove(_model.getDataList());

        _view.clearAnyEditor();
        _model.setChanged();
        destroyList();
        draw();
    }

    private void replaceObject(DataSetOp oldDataIn, SqlTableDef newTableIn) {

        _model.handleDsoReplace(oldDataIn);

        _view.clearAnyEditor();
        _model.setChanged();
        destroyList();
        draw();
    }

    private void initializePrefix(boolean forceIn) {

        if (null == _prefix) {

            List<DataSetOp> myDataList = _model.getDataList();

            if ((null != myDataList) && (0 < myDataList.size())) {

                _prefix = 0;
                for (DataSetOp myTop : myDataList) {

                    for (DataSetOp myDso = myTop.getFirstOp(); null != myDso; myDso = myDso.getNextOp()) {

                        Integer myPrefix = myDso.extractPrefix();

                        if ((null != myPrefix) && (myPrefix > _prefix)) {

                            _prefix = myPrefix;
                        }
                    }
                }

            } else if (forceIn) {

                _prefix = 0;
            }
        }
    }

    private int getPrefix() {

        initializePrefix(true);
        return ++_prefix;
    }

    private void clearFlags() {

        _activeDso = null;
        _objectList = null;
        _selectedDso = null;
        _selectedItem = null;
    }
}
