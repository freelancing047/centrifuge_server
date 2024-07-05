package csi.client.gwt.mapper.grids;

import csi.client.gwt.events.CsiDropEventHandler;
import csi.client.gwt.mapper.cells.SelectionMapperCell;
import csi.client.gwt.mapper.data_model.FieldDisplay;
import csi.client.gwt.mapper.grid_model.BuilderInfo;
import csi.client.gwt.mapper.grid_model.GroupProvider;
import csi.client.gwt.mapper.grid_model.ModelBuilder;
import csi.client.gwt.mapper.grid_model.ObjectProvider;
import csi.client.gwt.mapper.menus.GridMenu;
import csi.client.gwt.widget.cells.readonly.CsiTitleCell;
import csi.server.common.model.FieldDef;

/**
 * Created by centrifuge on 3/27/2016.
 */
public abstract class FieldSelectionGrid extends SelectionGrid<FieldDisplay> {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected static final String _txtGroupInUse = _constants.mapper_FieldGroup_InUse();
    protected static final String _txtGroupNotInUse = _constants.mapper_FieldGroup_NotInUse();


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public FieldSelectionGrid(String gridHeaderIn, GridMenu<SelectionGrid<FieldDisplay>> gridMenuIn,
                              CsiDropEventHandler handlerIn) {

        super(new ModelBuilder<FieldDisplay>()
                .addColumn(new BuilderInfo<FieldDisplay, String>(new GroupProvider<FieldDisplay>(),
                        150, "", false, false, new CsiTitleCell()))
                .addColumn(new BuilderInfo<FieldDisplay, FieldDisplay>(new ObjectProvider<FieldDisplay>(),
                        150, gridHeaderIn, false, false, new SelectionMapperCell<FieldDef>())), gridMenuIn, handlerIn);
    }

    public FieldSelectionGrid(String idIn, String gridHeaderIn, GridMenu<SelectionGrid<FieldDisplay>> gridMenuIn,
                              CsiDropEventHandler handlerIn) {

        this(gridHeaderIn, gridMenuIn, handlerIn);

        setGridId(idIn);
    }
}