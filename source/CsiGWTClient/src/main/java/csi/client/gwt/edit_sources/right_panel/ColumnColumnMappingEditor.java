package csi.client.gwt.edit_sources.right_panel;

import java.util.ArrayList;
import java.util.List;

import com.github.gwtbootstrap.client.ui.CheckBox;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;

import csi.client.gwt.edit_sources.DataSourceEditorModel;
import csi.client.gwt.edit_sources.center_panel.shapes.WienzoComposite;
import csi.client.gwt.mapper.VerticalMappingEditorLayout;
import csi.client.gwt.mapper.data_model.ColumnDisplay;
import csi.client.gwt.mapper.data_model.SelectionPair;
import csi.client.gwt.mapper.grids.ColumnColumnResultGrid;
import csi.client.gwt.mapper.grids.ColumnSelectionGrid;
import csi.client.gwt.mapper.grids.SelectionGrid;
import csi.client.gwt.mapper.menus.SortFilterMenu;
import csi.client.gwt.widget.gxt.drag_n_drop.DragLabelProvider;
import csi.server.common.enumerations.ComparingToken;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.model.DataSetOp;
import csi.server.common.model.DataSourceDef;
import csi.server.common.model.operator.OpMapItem;

/**
 * @author Centrifuge Systems, Inc.
 *
 */

/**
 * Created by centrifuge on 3/28/2016.
 */
public class ColumnColumnMappingEditor extends VerticalMappingEditorLayout<ColumnDisplay, ColumnDisplay> {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected static final String[] _resultGridHeaders = {
            _constants.mapper_GridHeader_TableQuery(),
            _constants.mapper_GridHeader_Column(),
            _constants.mapper_GridHeader_TableQuery(),
            _constants.mapper_GridHeader_Column()};

    protected DataSourceEditorModel _model;
    protected WienzoComposite _parent;
    protected DataSetOp _dso;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private ColumnColumnResultGrid.EditCallBack editCallBack = new ColumnColumnResultGrid.EditCallBack() {

        @Override
        public void onEdit(SelectionPair<ColumnDisplay, ColumnDisplay> selectionIn) {

            onEditMapping(selectionIn);
        }
    };

    ValueChangeHandler handleBooleanChange = new ValueChangeHandler<Boolean>() {

        @Override
        public void onValueChange(ValueChangeEvent<Boolean> eventIn) {

            getDso().setForceLocal(!eventIn.getValue());
            _model.setChanged();
            _parent.updateInfo();
        }
    };


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public ColumnColumnMappingEditor(WienzoComposite parentIn, DataSourceEditorModel modelIn) {

        super();

        selectColumnColumn();
        _parent = parentIn;
        _dso = _parent.getDso();
        _model = modelIn;
    }

    public DataSourceEditorModel getModel() {

        return _model;
    }

    public DataSetOp getDso() {

        return _dso;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void onAddMapping(SelectionPair<ColumnDisplay, ColumnDisplay> selectionIn) {

        selectionIn.setMapped(true);
        if (selectionIn instanceof ColumnMappingSet) {

            getModel().mapColumnPair(getDso(), (ColumnMappingSet) selectionIn);
            _parent.updateInfo();
        }
    }

    @Override
    protected void onDeleteMapping(SelectionPair<ColumnDisplay, ColumnDisplay> selectionIn) {

        selectionIn.setMapped(false);
        if (selectionIn instanceof ColumnMappingSet) {

            getModel().unmapColumnPair(getDso(), (ColumnMappingSet) selectionIn);
            _parent.updateInfo();
        }
    }

    @Override
    protected void onEditMapping(SelectionPair<ColumnDisplay, ColumnDisplay> selectionIn) {

        if (selectionIn instanceof ColumnMappingSet) {

            getModel().updateColumnPair((ColumnMappingSet)selectionIn);
            _parent.updateInfo();
        }
    }

    @Override
    protected List<? extends SelectionPair<ColumnDisplay, ColumnDisplay>> createPreSelectionList() {

        List<ColumnMappingSet> mySelectionList = null;

        if (_dso.hasMapItems()) {

            List<OpMapItem> myOldList = _dso.getMapItems();
            List<OpMapItem> myNewList = new ArrayList<OpMapItem>(myOldList.size());

            mySelectionList = new ArrayList<ColumnMappingSet>(_dso.getMapItems().size());

            for (int i= 0; myOldList.size() > i; i++) {

                OpMapItem myItem = ColumnSelectionGrid.validateMapping(_leftGrid.getDisplayList(),
                        _rightGrid.getDisplayList(), myOldList.get(i));

                if (null != myItem) {

                    ColumnMappingSet mySet = createMappingItem(_mappingId,
                            _leftGrid.getDisplayItem(myItem.getLeftTableLocalId(), myItem.getLeftColumnLocalId()),
                            _rightGrid.getDisplayItem(myItem.getRightTableLocalId(), myItem.getRightColumnLocalId()),
                            myItem.getCastToType(), myItem.getComparingToken());

                    myNewList.add(mySet.getResult());
                    mySelectionList.add(mySet);
                }
            }
            _dso.setMapItems(myNewList);
        }
        return (null != mySelectionList) ? mySelectionList : new ArrayList<ColumnMappingSet>(0);
    }

    protected void initAll() {

        initAll(null, null);
    }

    protected void initAll(String symbolIn, String labelIn) {

        leftLabelProvider = new DragLabelProvider() {
            @Override
            public String getLabel() {

                return ((ColumnSelectionGrid)_leftGrid).getSelectionLabel();
            }
        };

        rightLabelProvider = new DragLabelProvider() {
            @Override
            public String getLabel() {

                return ((ColumnSelectionGrid)_rightGrid).getSelectionLabel();
            }
        };
        initializeGrids(new ColumnSelectionGrid(_leftId, _txtHeader_Column_SortFilter, new SortFilterMenu<SelectionGrid<ColumnDisplay>>(), dropHandler, _dso.getLeftChild()),
                new ColumnSelectionGrid(_rightId, _txtHeader_Column_SortFilter, new SortFilterMenu<SelectionGrid<ColumnDisplay>>(), dropHandler, _dso.getRightChild()),
                new ColumnColumnResultGrid(_mappingId, _resultGridHeaders, symbolIn, labelIn, editCallBack));
        initializeCombinationLogic();
    }

    @Override
    protected ColumnMappingSet createMappingItem(String idIn, ColumnDisplay leftSelectionIn,
                                                 ColumnDisplay rightSelectionIn, CsiDataType castToTypeIn,
                                                 ComparingToken comparingTokenIn) {

        OpMapItem myItem = new OpMapItem(getDso(),
                leftSelectionIn.getParentId(),
                leftSelectionIn.getData().getLocalId(),
                rightSelectionIn.getParentId(),
                rightSelectionIn.getData().getLocalId());

        myItem.setCastToType(castToTypeIn);
        myItem.setComparingToken(comparingTokenIn);

        return new ColumnMappingSet(idIn, leftSelectionIn, rightSelectionIn, myItem);
    }

    @Override
    protected void addHandlers() {
        super.addHandlers();

        getAtSourceCheckBox().addValueChangeHandler(handleBooleanChange);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private void initializeCombinationLogic() {

        CheckBox myCheckBox = getAtSourceCheckBox();
        DataSetOp myLeftOp = _dso.getLeftChild();
        DataSetOp myRightOp = _dso.getRightChild();

        if (isSameSource(myLeftOp, myRightOp)) {

            myCheckBox.setEnabled(true);

        } else {

            myCheckBox.setEnabled(false);
            _dso.setForceLocal(true);
        }
        myCheckBox.setValue(!_dso.getForceLocal());
    }

    private boolean isSameSource(DataSetOp leftOpIn, DataSetOp rightOpIn) {

        DataSourceDef myLeftSource = getDataSource(leftOpIn);
        DataSourceDef myRightSource = getDataSource(rightOpIn);

        return (null != myLeftSource) && (myLeftSource.equals(myRightSource));
    }

    private DataSourceDef getDataSource(DataSetOp opIn) {

        if (opIn.getForceLocal()) {

            return null;

        } else {

            DataSourceDef mySource = (null != opIn.getTableDef()) ? opIn.getTableDef().getSource() : null;

            if (null == mySource) {

                DataSourceDef myLeftSource = getDataSource(opIn.getLeftChild());
                DataSourceDef myRightSource = getDataSource(opIn.getRightChild());

                if ((null != myLeftSource) && (myLeftSource.equals(myRightSource))) {

                    mySource = myLeftSource;
                }
            }
            return ((null != mySource) && (!mySource.isSingleTable())) ? mySource : null;
        }
    }
}
