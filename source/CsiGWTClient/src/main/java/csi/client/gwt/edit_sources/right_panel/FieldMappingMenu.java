package csi.client.gwt.edit_sources.right_panel;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.sencha.gxt.widget.core.client.menu.MenuItem;

import csi.client.gwt.mapper.data_model.ColumnDisplay;
import csi.client.gwt.mapper.data_model.FieldDisplay;
import csi.client.gwt.mapper.grids.SelectionGrid;
import csi.client.gwt.mapper.menus.AutoMapCallbackHandler;
import csi.client.gwt.mapper.menus.AutoMapMenu;
import csi.client.gwt.mapper.menus.MappingSupport;

/**
 * Created by centrifuge on 4/19/2016.
 */
public class FieldMappingMenu extends AutoMapMenu<FieldDisplay, ColumnDisplay> {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public FieldMappingMenu(SelectionGrid<FieldDisplay> leftGridIn, SelectionGrid<ColumnDisplay> rightGridIn,
                            MappingSupport<FieldDisplay, ColumnDisplay> mappingSupportIn,
                            AutoMapCallbackHandler<FieldDisplay, ColumnDisplay> handlerIn) {
        super(leftGridIn, rightGridIn, mappingSupportIn, handlerIn);

        final MenuItem genNewFieldsItem = new MenuItem();

        genNewFieldsItem.setText(i18n.mapperMenu_FieldMap_genNewFieldsItem()); //$NON-NLS-1$
        genNewFieldsItem.addSelectionHandler(genNewFieldsHandler);
        add(genNewFieldsItem);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private SelectionHandler genNewFieldsHandler = new SelectionHandler() {
        @Override
        public void onSelection(SelectionEvent event) {

            _handler.onMenuSelectionProcessed(_mappingSupport.mapByExactName(_leftGrid.getStore(), _rightGrid.getStore()));
        }
    };
}
