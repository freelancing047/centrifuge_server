package csi.client.gwt.widget.gxt.grid;

import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.sencha.gxt.widget.core.client.grid.GridView;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;

public class CustomGridView<M> extends GridView<M> {

    private Menu menu;

    private MenuItem byNameItem = new MenuItem();
    private MenuItem byTypeItem = new MenuItem();
    private MenuItem exactMatchItem = new MenuItem();
    private MenuItem byPositionItem = new MenuItem();
    private MenuItem byRelativePositionItem = new MenuItem();

    private Integer column = null;

    private CentrifugeConstants i18n = CentrifugeConstantsLocator.get();

    public void initializeMenu(int columnIn) {

        column = columnIn;
        createContextMenu(columnIn);
    }

    @Override
    protected Menu createContextMenu(final int colIndex) {

        //final Menu menu = super.createContextMenu(colIndex);

        if (( null != column) && column.equals(colIndex) && (menu == null)){

            menu = new Menu();

//            final MenuItem mapItem = new MenuItem();
//            mapItem.setHideOnClick(false);
//            mapItem.setText(i18n.linkupGridMenuTopMenuItem()); //$NON-NLS-1$

            getSubMenu(menu);

        }

        return menu;
    }

    private Menu getSubMenu(Menu menu) {

        if(menu == null){
            menu = new Menu();
        }
        byNameItem.setText(i18n.linkupGridMenuByNameItem()); //$NON-NLS-1$
        menu.add(byNameItem);

        byTypeItem.setText(i18n.linkupGridMenuByTypeItem()); //$NON-NLS-1$
        menu.add(byTypeItem);

        exactMatchItem.setText(i18n.linkupGridMenuByExactMatchItem()); //$NON-NLS-1$
        menu.add(exactMatchItem);

        byPositionItem.setText(i18n.linkupGridMenuByPositionItem()); //$NON-NLS-1$
        menu.add(byPositionItem);

        byRelativePositionItem.setText(i18n.linkupGridMenuByRelativePositionItem()); //$NON-NLS-1$
        menu.add(byRelativePositionItem);

        return menu;
    }


    public HandlerRegistration addByNameHandler(SelectionHandler byNameHandler) {
        return byNameItem.addSelectionHandler(byNameHandler);
    }

    public HandlerRegistration addExactMatchHandler(SelectionHandler exactMatchHandler) {
        return exactMatchItem.addSelectionHandler(exactMatchHandler);
    }

    public HandlerRegistration addByPositionHandler(SelectionHandler byPositionHandler) {
        return byPositionItem.addSelectionHandler(byPositionHandler);
    }

    public HandlerRegistration addByRelativePositionHandler(SelectionHandler byRelativePositionHandler) {
        return byRelativePositionItem.addSelectionHandler(byRelativePositionHandler);
    }

    public HandlerRegistration addByTypeHandler(SelectionHandler byTypeHandler) {
        return byTypeItem.addSelectionHandler(byTypeHandler);
    }
}
