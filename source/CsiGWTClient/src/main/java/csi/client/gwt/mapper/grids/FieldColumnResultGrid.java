package csi.client.gwt.mapper.grids;

import com.google.gwt.cell.client.TextCell;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.sencha.gxt.cell.core.client.form.TextInputCell;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.editing.GridInlineEditing;
import csi.client.gwt.events.GridClickEvent;
import csi.client.gwt.events.GridClickEventHandler;
import csi.client.gwt.mapper.cells.RightSelectionMapperCell;
import csi.client.gwt.mapper.data_model.ColumnDisplay;
import csi.client.gwt.mapper.data_model.FieldDisplay;
import csi.client.gwt.mapper.data_model.SelectionPair;
import csi.client.gwt.mapper.grid_model.*;
import csi.client.gwt.widget.DataTypeCallback;
import csi.client.gwt.widget.cells.context_menu.DataTypeCell;
import csi.server.common.enumerations.CsiDataType;

interface ChangeCallBack {

    public void onNameChange(int rowIn, int columnIn, String valueIn);
}

/**
 * Created by centrifuge on 3/29/2016.
 */
public class FieldColumnResultGrid extends ResultGrid<FieldDisplay, ColumnDisplay> implements ChangeCallBack {


    public interface EditCallBack {

        public void onEdit(SelectionPair<FieldDisplay, ColumnDisplay> selectionIn);
    }

    private class FieldNameEditing extends GridInlineEditing<SelectionPair<FieldDisplay, ColumnDisplay>> {

        ColumnConfig<SelectionPair<FieldDisplay, ColumnDisplay>, String> _config;
        FieldColumnResultGrid _grid;
        int _row;
        int _column;

        public FieldNameEditing(FieldColumnResultGrid gridIn) {

            super(gridIn);
            _grid = gridIn;
            _config = (ColumnConfig<SelectionPair<FieldDisplay, ColumnDisplay>, String>)_grid.getModelBuilder().getColumnConfig(1);
        }

        @Override
        protected <N, O> void doCompleteEditing() {

            super.doCompleteEditing();
            _grid.onNameChange(_row, _column, (String)super.getEditor(_config).getValue());
        }

        @Override
        protected <N, O> void doStartEditing(GridCell cellIn) {

            _row = cellIn.getRow();
            _column = cellIn.getCol();
            super.doStartEditing(cellIn);
        }
    }


    private GridClickEventHandler castingClickHandler = new GridClickEventHandler() {
        @Override
        public void onGridClick(GridClickEvent eventIn) {

            SelectionPair<FieldDisplay, ColumnDisplay> mySelection = getStore().get(eventIn.getRow());
            FieldDisplay myField = mySelection.getLeftData();
            CsiDataType myCurrentCast = mySelection.getCastToType();

            mySelection.setCastToType(myCurrentCast.getNextValue());
            if (null != myField) {

                myField.setDataType(mySelection.getCastToType());
            }
            getView().refresh(false);
        }
    };

    private DataTypeCallback castingCallback = new DataTypeCallback() {
        @Override
        public void onTypeSelection(CsiDataType dataTypeIn, int rowIn) {

            SelectionPair<FieldDisplay, ColumnDisplay> mySelection = getStore().get(rowIn);
            FieldDisplay myField = mySelection.getLeftData();

            mySelection.setCastToType(dataTypeIn);
            if (null != myField) {

                myField.setDataType(dataTypeIn);
            }
            getView().refresh(false);
        }
    };


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public FieldColumnResultGrid(String idIn, String[] gridHeadersIn) {

        super(new ModelBuilder<SelectionPair<FieldDisplay, ColumnDisplay>>()
                        .addColumn(new BuilderInfo<SelectionPair<FieldDisplay, ColumnDisplay>, CsiDataType>(new CastToTypeProvider<SelectionPair<FieldDisplay, ColumnDisplay>>(),
                                60, "Type", false, true, new DataTypeCell(true, false)))
                        .addColumn(new BuilderInfo<SelectionPair<FieldDisplay, ColumnDisplay>, String>(new FieldNameProvider<SelectionPair<FieldDisplay, ColumnDisplay>>(),
                                100, "Name", false, true, new TextCell()))
                        .addColumn(new BuilderInfo<SelectionPair<FieldDisplay, ColumnDisplay>, SelectionPair<FieldDisplay, ColumnDisplay>>(new ObjectProvider<SelectionPair<FieldDisplay, ColumnDisplay>>(),
                                300, "Source", false, true, new RightSelectionMapperCell<FieldDisplay, ColumnDisplay>(true, true, true)))
        );
        ColumnConfig<SelectionPair<FieldDisplay, ColumnDisplay>, CsiDataType> myDataTypeConfig
                = (ColumnConfig<SelectionPair<FieldDisplay, ColumnDisplay>, CsiDataType>)getModelBuilder().getColumnConfig(0);
        ColumnConfig<SelectionPair<FieldDisplay, ColumnDisplay>, String> myFieldName
                = (ColumnConfig<SelectionPair<FieldDisplay, ColumnDisplay>, String>)getModelBuilder().getColumnConfig(1);

        if (null != myDataTypeConfig) {

            DataTypeCell myDataTypeCell = (DataTypeCell)myDataTypeConfig.getCell();

            if (null != myDataTypeCell) {

//                myDataTypeCell.setClickHandler(castingClickHandler);
                myDataTypeCell.setCallback(castingCallback);
            }
            myDataTypeConfig.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        }
        FieldNameEditing myGridEditor = new FieldNameEditing(this);
        myGridEditor.addEditor(myFieldName, new com.sencha.gxt.widget.core.client.form.TextField(new TextInputCell()));
        setGridId(idIn);
    }

    public void onNameChange(int rowIn, int columnIn, String valueIn) {

        SelectionPair<FieldDisplay, ColumnDisplay> mySelection = getStore().get(rowIn);
        FieldDisplay myField = mySelection.getLeftData();

        if (null != myField) {

            myField.setName(valueIn);
        }
    }
}
