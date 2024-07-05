package csi.client.gwt.mapper.grids;

import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.widget.core.client.menu.Menu;

import csi.client.gwt.events.CsiDropEventHandler;
import csi.client.gwt.mapper.data_model.EmptyDragItem;
import csi.client.gwt.mapper.grid_model.MenuGridView;
import csi.client.gwt.mapper.grid_model.ModelBuilder;
import csi.client.gwt.widget.gxt.drag_n_drop.IntegratedGrid;

/**
 * Created by centrifuge on 4/1/2016.
 */
public class MenuGrid extends IntegratedGrid<EmptyDragItem> {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    Menu _menu;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public MenuGrid(ModelBuilder<EmptyDragItem> modelBuilderIn, Menu menuIn, CsiDropEventHandler handlerIn) {

        super(modelBuilderIn.genStore(), modelBuilderIn.genModel(), new MenuGridView(menuIn));

        _menu = menuIn;
        addCsiDropEventHandler(handlerIn);
    }

    public Menu getMenu() {

        return _menu;
    }

    public static ValueProvider<EmptyDragItem, String> getValueProvider() {

        return new ValueProvider<EmptyDragItem, String>() {
            @Override
            public String getValue(EmptyDragItem object) {
                return "";
            }

            @Override
            public void setValue(EmptyDragItem object, String value) {

            }

            @Override
            public String getPath() {
                return null;
            }
        };
    }
}
