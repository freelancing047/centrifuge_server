package csi.client.gwt.viz.graph.surface.menu;

import java.util.ArrayList;

import com.github.gwtbootstrap.client.ui.NavLink;
import com.github.gwtbootstrap.client.ui.base.UnorderedList;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import csi.client.gwt.i18n.CentrifugeConstantsLocator;

public class RevealNeighborsSubmenu extends Composite {

    private static RevealNeighborsSubmenuUiBinder uiBinder = GWT.create(RevealNeighborsSubmenuUiBinder.class);

    interface RevealNeighborsSubmenuUiBinder extends UiBinder<Widget, RevealNeighborsSubmenu> {
    }

    public RevealNeighborsSubmenu() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    @UiField
    UnorderedList navList;

    private ArrayList<String> typesWithNodes;
    private GraphContextSubmenuCallback callback;
    private int rowsPerPage = 10;
    private int currentPage = 0;
    private int index;

    public RevealNeighborsSubmenu(ArrayList<String> typesWithNodes, GraphContextSubmenuCallback callback) {
        this.typesWithNodes = typesWithNodes;
        this.callback = callback;
        initializeWidget();
        populateWidget();
    }

    private void initializeWidget() {
        initWidget(uiBinder.createAndBindUi(this));
        navList.getElement().addClassName("dropdown-menu");//NON-NLS
    }

    private void populateWidget() {
        navList.clear();
        //        createAllNavLink();
        createTypeNavLinks();
        createMoreNavLink();
    }

    //    private void createAllNavLink() {
    //        createNavLink("All", callback);
    //    }

    private void createNavLink(String type, GraphContextSubmenuCallback callback) {
        NavLink menuItem = new NavLink(type);
        menuItem.addClickHandler(new GraphContextSubmenuClickHandler(type, callback));
        navList.add(menuItem);
    }

    private void createTypeNavLinks() {
        int beginRow = currentPage * rowsPerPage;
        int endRow = beginRow + rowsPerPage;
        index = beginRow;
        while ((index < endRow) && (index < typesWithNodes.size())) {
            createNavLink(typesWithNodes.get(index), callback);
            index++;
        }
    }

    private void createMoreNavLink() {
        if (index < typesWithNodes.size()) {
            createNavLink(CentrifugeConstantsLocator.get().graphContextMenu_moreRevealNeighborsByTypePrompt(), new GraphContextSubmenuCallback() {

                @Override
                public void execute(String type) {
                    currentPage++;
                    populateWidget();
                }
            });
        }
    }
}
