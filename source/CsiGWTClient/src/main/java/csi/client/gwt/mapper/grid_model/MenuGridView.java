package csi.client.gwt.mapper.grid_model;

import com.sencha.gxt.widget.core.client.grid.GridView;
import com.sencha.gxt.widget.core.client.menu.Menu;

import csi.client.gwt.mapper.data_model.EmptyDragItem;

/**
 * Created by centrifuge on 4/1/2016.
 */
public class MenuGridView extends GridView<EmptyDragItem> {

    private Menu _menu = null;
    private boolean _hideMenu = false;

    public MenuGridView(Menu menuIn) {

        super();

        _menu = menuIn;
        _hideMenu = (null == _menu);
    }

    @Override
    protected Menu createContextMenu(final int colIndex) {

        return _hideMenu ? null : (null != _menu) ? _menu : super.createContextMenu(colIndex);
    }
}
