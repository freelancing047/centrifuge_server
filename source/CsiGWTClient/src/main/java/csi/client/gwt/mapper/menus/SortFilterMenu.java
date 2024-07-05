package csi.client.gwt.mapper.menus;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.sencha.gxt.widget.core.client.menu.MenuItem;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.mapper.grids.SelectionGrid;

/**
 * Created by centrifuge on 4/1/2016.
 */
public class SortFilterMenu<T extends SelectionGrid<?>> extends GridMenu<T> {

    protected final CentrifugeConstants i18n = CentrifugeConstantsLocator.get();

    public SortFilterMenu() {

        initialize();
    }

    public SortFilterMenu(T gridIn) {

        super(gridIn);

        initialize();
    }

    private void initialize() {

        final MenuItem ordinalAscending = new MenuItem();
        final MenuItem alphaAscending = new MenuItem();
        final MenuItem alphaDescending = new MenuItem();

        ordinalAscending.setText(i18n.mapperMenu_Sort_ByOrdinalAscending()); //$NON-NLS-1$
        ordinalAscending.addSelectionHandler(sortOrdinalAscending);
        add(ordinalAscending);

        alphaAscending.setText(i18n.mapperMenu_Sort_ByAlphaAscending()); //$NON-NLS-1$
        alphaAscending.addSelectionHandler(sortAlphaAscending);
        add(alphaAscending);

        alphaDescending.setText(i18n.mapperMenu_Sort_ByAlphaDescending()); //$NON-NLS-1$
        alphaDescending.addSelectionHandler(sortAlphaDescending);
        add(alphaDescending);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private SelectionHandler sortOrdinalAscending = new SelectionHandler() {
        @Override
        public void onSelection(SelectionEvent event) {

            _grid.sortByOrdinal();
        }
    };

    private SelectionHandler sortAlphaAscending = new SelectionHandler() {
        @Override
        public void onSelection(SelectionEvent event) {

            _grid.sortAlphaAscending();
        }
    };

    private SelectionHandler sortAlphaDescending = new SelectionHandler() {
        @Override
        public void onSelection(SelectionEvent event) {

            _grid.sortAlphaDescending();
        }
    };
}
