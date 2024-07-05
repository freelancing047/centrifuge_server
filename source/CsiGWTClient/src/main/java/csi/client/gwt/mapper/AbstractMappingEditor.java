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
package csi.client.gwt.mapper;

import java.util.List;
import java.util.Map;

import com.google.gwt.cell.client.TextCell;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.sencha.gxt.widget.core.client.event.CellClickEvent;
import com.sencha.gxt.widget.core.client.event.CellClickEvent.CellClickHandler;
import com.sencha.gxt.widget.core.client.event.CellDoubleClickEvent;
import com.sencha.gxt.widget.core.client.event.CellDoubleClickEvent.CellDoubleClickHandler;
import com.sencha.gxt.widget.core.client.menu.Menu;

import csi.client.gwt.events.CsiDropEvent;
import csi.client.gwt.events.CsiDropEventHandler;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.mapper.data_model.BasicDragItem;
import csi.client.gwt.mapper.data_model.EmptyDragItem;
import csi.client.gwt.mapper.data_model.SelectionDataAccess;
import csi.client.gwt.mapper.data_model.SelectionPair;
import csi.client.gwt.mapper.grid_model.BuilderInfo;
import csi.client.gwt.mapper.grid_model.ModelBuilder;
import csi.client.gwt.mapper.grids.MenuGrid;
import csi.client.gwt.mapper.grids.ResultGrid;
import csi.client.gwt.mapper.grids.SelectionGrid;
import csi.client.gwt.mapper.menus.AutoMapCallbackHandler;
import csi.client.gwt.mapper.menus.AutoMapMenu;
import csi.client.gwt.mapper.menus.MappingSupport;
import csi.client.gwt.widget.boot.InfoDialog;
import csi.client.gwt.widget.gxt.drag_n_drop.DragLabelProvider;
import csi.server.common.enumerations.ComparingToken;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.model.CsiUUID;
import csi.server.common.util.ValuePair;

/**
 * @author Centrifuge Systems, Inc.
 */
public abstract class AbstractMappingEditor<T1 extends SelectionDataAccess<?>, T2 extends SelectionDataAccess<?>>
        extends ResizeComposite {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected abstract List<? extends SelectionPair<T1, T2>> createPreSelectionList();
    protected abstract SelectionPair<T1, T2> createMappingItem(String idIn, T1 leftSelectionIn, T2 rightSelectionIn,
                                                               CsiDataType castToTypeIn, ComparingToken comparingTokenIn);
    protected abstract boolean useQuickClick();

    protected static final CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    protected static final String _txtGroupInUse = _constants.mapper_FieldGroup_InUse();
    protected static final String _txtGroupNotInUse = _constants.mapper_FieldGroup_NotInUse();
    protected static final String _txtHeader_Map = _constants.mapper_GridHeader_Map();
    protected static final String _txtHeader_Column_SortFilter = _constants.mapper_GridHeader_Column_SortFilter();
    protected static final String _txtHeader_Field_SortFilter = _constants.mapper_GridHeader_Field_SortFilter();

    private boolean _removeLeft;
    private boolean _removeRight;

    protected MenuGrid _menuGrid;
    protected SelectionGrid<T1> _leftGrid;
    protected SelectionGrid<T2> _rightGrid;
    protected ResultGrid<T1, T2> _mappingGrid;
    protected String _leftId = CsiUUID.randomUUID();
    protected String _rightId = CsiUUID.randomUUID();
    protected String _mappingId = CsiUUID.randomUUID();

    protected DragLabelProvider leftLabelProvider = null;
    protected DragLabelProvider rightLabelProvider = null;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected CsiDropEventHandler dropHandler =  new CsiDropEventHandler() {
        @Override
        public void onDrop(CsiDropEvent eventIn) {

            Object mySourceObject = (null != eventIn.getSource()) ? ((List<Object>)eventIn.getSource()).get(0) : null;
            Object myTargetObject = eventIn.getTarget();

            if ((null != mySourceObject) && (mySourceObject instanceof BasicDragItem)) {

                if (((BasicDragItem)mySourceObject).getGridId().equals(_mappingId)) {

                    unmapSelectedPairs();

                } else if (null != myTargetObject) {

                    if (((BasicDragItem)mySourceObject).getGridId().equals(_rightId)) {

                        mapSelectedPair((T1) myTargetObject, (T2) mySourceObject);

                    } else if (((BasicDragItem)mySourceObject).getGridId().equals(_leftId)) {

                        mapSelectedPair((T1) mySourceObject, (T2) myTargetObject);
                    }
                }
            }
        }
    };


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public AbstractMappingEditor() {

        _removeRight = true;
        _removeLeft = true;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected void initializeGrids(SelectionGrid<T1> leftGridIn, SelectionGrid<T2> rightGridIn, ResultGrid<T1, T2> mappingGridIn) {

        _leftGrid = leftGridIn;
        _rightGrid = rightGridIn;
        _mappingGrid = mappingGridIn;
        _leftGrid.initializeGrid();
        _rightGrid.initializeGrid();
        _mappingGrid.initializeGrid(leftGridIn.getSelectedItems(), rightGridIn.getSelectedItems(), processInitialSelection());
        _leftGrid.sortByOrdinal();
        _rightGrid.sortByOrdinal();
        _menuGrid = new MenuGrid(new ModelBuilder<EmptyDragItem>().addColumn(
                    new BuilderInfo<EmptyDragItem, String>(MenuGrid.getValueProvider(), 50, _txtHeader_Map, false, false, new TextCell())),
                    new AutoMapMenu<T1, T2>(_leftGrid, _rightGrid, new MappingSupport<T1, T2>(), menuHandler), dropHandler);
        // Link grids together for drag'n'drop
        if (null != leftLabelProvider) {

            _leftGrid.integrate(new String[]{_rightId}, new String[]{_mappingId}, leftLabelProvider);

        } else {

            _leftGrid.integrate(new String[]{_rightId}, new String[]{_mappingId}, "left list item");
        }
        if (null != rightLabelProvider) {

            _rightGrid.integrate(new String[]{_leftId}, new String[]{_mappingId}, rightLabelProvider);

        } else {

            _rightGrid.integrate(new String[]{_leftId}, new String[]{_mappingId}, "right list item");
        }
        _mappingGrid.integrate(new String[0], new String[0], "pair(s) to be unmapped");
        _menuGrid.integrate(new String[0], new String[]{_mappingId}, "");
    }

    protected Menu getMappingMenu() {

        return _menuGrid.getMenu();
    }
/*
ValueProvider<T, S> valueProviderIn, int widthIn, String labelIn, boolean sortableIn, boolean menuDisabledIn, Cell displayCellIn
 */
    protected AutoMapCallbackHandler<T1, T2> menuHandler = new AutoMapCallbackHandler<T1, T2>() {

        @Override
        public void onMenuSelectionProcessed(List<ValuePair<T1, T2>> listIn) {

            if ((null != listIn) && (0 < listIn.size())) {

                for (ValuePair<T1, T2> myPair : listIn) {

                    mapSelectedPair(myPair.getValue1(), myPair.getValue2());
                }
            }
        }
    };

    protected void mapSelectedPair(T1 leftSelectionIn, T2 rightSelectionIn) {

        // if a grid is empty
        if ((null != rightSelectionIn) && (null != leftSelectionIn)) {

            SelectionPair<T1, T2> myItem = createMappingItem(_mappingId, leftSelectionIn, rightSelectionIn,
                                                                leftSelectionIn.getCastToType(), ComparingToken.EQ);

            if (_mappingGrid.getStore().getAll().contains(myItem)) {
                new InfoDialog(_constants.dataSourceEditor_Add(), _constants.dataSourceEditor_MappingExists()).show();

            } else {

                Map<String, T1> myLeftSelected = _leftGrid.getSelectedItems();
                Map<String, T2> myRightSelected = _rightGrid.getSelectedItems();

                _mappingGrid.getStore().add(myItem);

                if (_removeLeft) {

                    myLeftSelected.put(leftSelectionIn.getKey(), leftSelectionIn);
                    _leftGrid.getStore().remove(leftSelectionIn);
                    _leftGrid.refreshKeepState();
                }
                _leftGrid.getSelectionModel().deselectAll();

                if (_removeRight) {

                    myRightSelected.put(rightSelectionIn.getKey(), rightSelectionIn);
                    _rightGrid.getStore().remove(rightSelectionIn);
                    _rightGrid.refreshKeepState();
                }
                _rightGrid.getSelectionModel().deselectAll();

                onAddMapping(myItem);
            }

        } else {

            InfoDialog iDialog = new InfoDialog(_constants.dataSourceEditor_NoSelection(),
                                                _constants.dataSourceEditor_NeedTwoColumns());
            iDialog.show();
            return;
        }
    }

    protected  void unmapSelectedPairs() {

        if (_mappingGrid.getSelectionModel().getSelectedItems().size() == 0) {
            new InfoDialog(_constants.dataSourceEditor_Delete(), _constants.dataSourceEditor_SelectDeselect()).show();
            ;
        } else {

            Map<String, T1> myLeftSelected = _leftGrid.getSelectedItems();
            Map<String, T2> myRightSelected = _rightGrid.getSelectedItems();

            for (SelectionPair<T1, T2> myItem : _mappingGrid.getSelectionModel().getSelectedItems()) {

                if (_removeLeft) {

                    String myKey = myItem.getLeftKey();
                    T1 myDisplayItem = myLeftSelected.get(myKey);

                    if (null != myDisplayItem) {

                        myLeftSelected.remove(myKey);
                        _leftGrid.getStore().add(myDisplayItem);
                    }
                }
                _leftGrid.getSelectionModel().deselectAll();

                if (_removeRight) {

                    String myKey = myItem.getRightKey();
                    T2 myDisplayItem = myRightSelected.get(myKey);

                    if (null != myDisplayItem) {

                        myRightSelected.remove(myKey);
                        _rightGrid.getStore().add(myDisplayItem);
                    }
                }
                _rightGrid.getSelectionModel().deselectAll();

                _mappingGrid.getStore().remove(myItem);
                _mappingGrid.refreshKeepState();
                onDeleteMapping(myItem);
            }
        }
    }

    protected void onAddMapping(SelectionPair<T1, T2> itemIn) {
    }

    protected void onDeleteMapping(SelectionPair<T1, T2> item) {
    }

    protected void onEditMapping(SelectionPair<T1, T2> item) {
    }

    protected void addHandlers() {

        _leftGrid.addCellClickHandler(new CellClickHandler() {

            @Override
            public void onCellClick(CellClickEvent eventIn) {
                sourceSelectionChanged();
            }
        });

        _rightGrid.addCellClickHandler(new CellClickHandler() {

            @Override
            public void onCellClick(CellClickEvent eventIn) {
                sourceSelectionChanged();
            }
        });

        _mappingGrid.addCellDoubleClickHandler(new CellDoubleClickHandler() {
            @Override
            public void onCellClick(CellDoubleClickEvent eventIn) {

                unmapSelectedPairs();
            }
        });
    }

    protected void sourceSelectionChanged() {

        T2 mySource = _rightGrid.getSelectionModel().getSelectedItem();
        T1 myTarget = _leftGrid.getSelectionModel().getSelectedItem();

        if (useQuickClick() && (null != mySource) && (null != myTarget)) {

            mapSelectedPair(myTarget, mySource);
        }
    }

    protected List<? extends SelectionPair<T1, T2>> processInitialSelection() {

        return createPreSelectionList();
    }
}
