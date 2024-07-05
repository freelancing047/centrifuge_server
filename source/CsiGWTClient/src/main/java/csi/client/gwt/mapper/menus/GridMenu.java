package csi.client.gwt.mapper.menus;

import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.menu.Menu;

/**
 * Created by centrifuge on 4/1/2016.
 */
public abstract class GridMenu<T extends Grid<?>> extends Menu {

    T _grid;

    public GridMenu() {

        super();
    }

    public GridMenu(T gridIn) {

        super();

        _grid = gridIn;
    }

    public void setGrid(T gridIn) {

        _grid = gridIn;
    }
}
