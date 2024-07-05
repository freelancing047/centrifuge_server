package csi.client.gwt.mapper.grids;

import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import csi.client.gwt.events.GridClickEvent;
import csi.client.gwt.events.GridClickEventHandler;
import csi.client.gwt.mapper.cells.LeftSelectionMapperCell;
import csi.client.gwt.widget.cells.context_menu.OperatorCell;
import csi.client.gwt.mapper.cells.RightSelectionMapperCell;
import csi.client.gwt.mapper.data_model.ColumnDisplay;
import csi.client.gwt.mapper.data_model.SelectionPair;
import csi.client.gwt.mapper.grid_model.*;
import csi.client.gwt.widget.DataTypeCallback;
import csi.client.gwt.widget.OperatorCallback;
import csi.client.gwt.widget.cells.readonly.ConstantCell;
import csi.client.gwt.widget.cells.context_menu.DataTypeCell;
import csi.server.common.enumerations.ComparingToken;
import csi.server.common.enumerations.CsiDataType;

/**
 * Created by centrifuge on 3/28/2016.
 */
public class ColumnColumnResultGrid extends ResultGrid<ColumnDisplay, ColumnDisplay> {

    public interface EditCallBack {

        public void onEdit(SelectionPair<ColumnDisplay, ColumnDisplay> selectionIn);
    }


    private EditCallBack editCallBack = null;

    private GridClickEventHandler castingClickHandler = new GridClickEventHandler() {
        @Override
        public void onGridClick(GridClickEvent eventIn) {

            SelectionPair<ColumnDisplay, ColumnDisplay> myPair = getStore().get(eventIn.getRow());
            CsiDataType myCurrentCast = myPair.getCastToType();

            myPair.setCastToType(myCurrentCast.getNextValue());
            editCallBack.onEdit(myPair);
            getView().refresh(false);
        }
    };

    private GridClickEventHandler operatorClickHandler = new GridClickEventHandler() {
        @Override
        public void onGridClick(GridClickEvent eventIn) {

            SelectionPair<ColumnDisplay, ColumnDisplay> myPair = getStore().get(eventIn.getRow());
            ComparingToken myCurrentToken = myPair.getComparingToken();

            myPair.setComparingToken(myCurrentToken.getNextValue());
            editCallBack.onEdit(myPair);
            getView().refresh(false);
        }
    };

    private DataTypeCallback castingCallback = new DataTypeCallback() {
        @Override
        public void onTypeSelection(CsiDataType dataTypeIn, int rowIn) {

            SelectionPair<ColumnDisplay, ColumnDisplay> myPair = getStore().get(rowIn);

            myPair.setCastToType(dataTypeIn);
            editCallBack.onEdit(myPair);
            getView().refresh(false);
        }
    };

    private OperatorCallback operatorCallback = new OperatorCallback() {
        @Override
        public void onOperatorSelection(ComparingToken operatorIn, int rowIn) {

            SelectionPair<ColumnDisplay, ColumnDisplay> myPair = getStore().get(rowIn);

            myPair.setComparingToken(operatorIn);
            editCallBack.onEdit(myPair);
            getView().refresh(false);
        }
    };


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public ColumnColumnResultGrid(String idIn, String[] gridHeadersIn, String symbolIn, String labelIn, EditCallBack callBackIn) {

        super(new ModelBuilder<SelectionPair<ColumnDisplay, ColumnDisplay>>()
                        .addColumn(new BuilderInfo<SelectionPair<ColumnDisplay, ColumnDisplay>, CsiDataType>(new CastToTypeProvider<SelectionPair<ColumnDisplay, ColumnDisplay>>(),
                                50, _constants.type(), false, true, new DataTypeCell(true, false)))
                        .addColumn(new BuilderInfo<SelectionPair<ColumnDisplay, ColumnDisplay>, SelectionPair<ColumnDisplay, ColumnDisplay>>(new ObjectProvider<SelectionPair<ColumnDisplay, ColumnDisplay>>(),
                                140, gridHeadersIn[1], false, true, new LeftSelectionMapperCell<ColumnDisplay, ColumnDisplay>(true)))
                        .addColumn(new BuilderInfo<SelectionPair<ColumnDisplay, ColumnDisplay>, ComparingToken>(new OperatorProvider<SelectionPair<ColumnDisplay, ColumnDisplay>>(),
                                50, ((null != symbolIn) ? "" : _constants.operator()), false, true, ((null != symbolIn) ? new ConstantCell<ComparingToken>(labelIn, symbolIn) : new OperatorCell())))
                        .addColumn(new BuilderInfo<SelectionPair<ColumnDisplay, ColumnDisplay>, SelectionPair<ColumnDisplay, ColumnDisplay>>(new ObjectProvider<SelectionPair<ColumnDisplay, ColumnDisplay>>(),
                                140, gridHeadersIn[1], false, true, new RightSelectionMapperCell<ColumnDisplay, ColumnDisplay>(true)))
        );
        ColumnConfig<SelectionPair<ColumnDisplay, ColumnDisplay>, CsiDataType> myDataTypeConfig
                = (ColumnConfig<SelectionPair<ColumnDisplay, ColumnDisplay>, CsiDataType>)getModelBuilder().getColumnConfig(0);
        ColumnConfig<SelectionPair<ColumnDisplay, ColumnDisplay>, ComparingToken> myOperatorConfig
                = (ColumnConfig<SelectionPair<ColumnDisplay, ColumnDisplay>, ComparingToken>)getModelBuilder().getColumnConfig(2);

        if (null != myDataTypeConfig) {

            DataTypeCell myDataTypeCell = (DataTypeCell)myDataTypeConfig.getCell();

            if (null != myDataTypeCell) {

//                myDataTypeCell.setClickHandler(castingClickHandler);
                myDataTypeCell.setCallback(castingCallback);
            }
            myDataTypeConfig.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        }
        if (null != myOperatorConfig) {

            OperatorCell myOperatorCell = (null != symbolIn) ? null : (OperatorCell)myOperatorConfig.getCell();

            if (null != myOperatorCell) {

//                myOperatorCell.setClickHandler(operatorClickHandler);
                myOperatorCell.setCallback(operatorCallback);
            }
            myOperatorConfig.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        }
        editCallBack = callBackIn;
        setGridId(idIn);
    }
}
