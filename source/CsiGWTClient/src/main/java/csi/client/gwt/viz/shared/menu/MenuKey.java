package csi.client.gwt.viz.shared.menu;

import java.util.ArrayList;
import java.util.List;

import com.github.gwtbootstrap.client.ui.constants.IconType;

/**
 * 
 */
public enum MenuKey {
    // Top level menu
    ACTION(MenuKeyConstant.ACTION, IconType.USER), //
    EDIT(MenuKeyConstant.EDIT, IconType.PENCIL), //
    CONFIGURE(MenuKeyConstant.CONFIGURE, IconType.COGS), //
    TOOLS(MenuKeyConstant.TOOLS, IconType.WRENCH), //
    LAYOUT(MenuKeyConstant.LAYOUT, IconType.ASTERISK), //
    LINKUP(MenuKeyConstant.LINKUP, IconType.SHARE), // Linkup menu item added manually by the LinkupMenuManager
    BROADCAST(MenuKeyConstant.BROADCAST, IconType.BULLHORN), //

    LINKUP_DEFNAME(MenuKey.LINKUP, MenuKeyConstant.LINKUP_DEFNAME), // place holder
    
    // Broadcast menu
    BROADCAST_DIRECT(MenuKey.BROADCAST, MenuKeyConstant.BROADCAST_DIRECT),
    BROADCAST_INCLUSION(MenuKey.BROADCAST, MenuKeyConstant.BROADCAST_INCLUSION),
    BROADCAST_EXCLUSION(MenuKey.BROADCAST, MenuKeyConstant.BROADCAST_EXCLUSION),
    BROADCAST_REPLACE(MenuKey.BROADCAST, MenuKeyConstant.BROADCAST_REPLACE),
    BROADCAST_ADDTO(MenuKey.BROADCAST, MenuKeyConstant.BROADCAST_ADDTO),
    BROADCAST_REMOVE(MenuKey.BROADCAST, MenuKeyConstant.BROADCAST_REMOVE),
    CLEAR_SELECTION(MenuKey.BROADCAST, MenuKeyConstant.CLEAR_SELECTION),
    CLEAR_BROADCAST(MenuKey.BROADCAST, MenuKeyConstant.CLEAR_BROADCAST),
    CLEAR_ALL_SELECTION(MenuKey.BROADCAST, MenuKeyConstant.CLEAR_ALL_SELECTION),
    CLEAR_ALL_BROADCASTS(MenuKey.BROADCAST, MenuKeyConstant.CLEAR_ALL_BROADCASTS),
    CLEAR_EVERYTHING(MenuKey.BROADCAST, MenuKeyConstant.CLEAR_EVERYTHING),
    LISTEN_FOR_BROADCAST(MenuKey.BROADCAST, MenuKeyConstant.LISTEN_FOR_BROADCAST),

    // Action menu
    LOAD(MenuKey.ACTION, MenuKeyConstant.LOAD), //
    SAVE(MenuKey.ACTION, MenuKeyConstant.SAVE), //
    SPINOFF(MenuKey.ACTION, MenuKeyConstant.SPINOFF), //
    SPAWN(MenuKey.ACTION, MenuKeyConstant.SPAWN), //
    PRINT(MenuKey.ACTION, MenuKeyConstant.PRINT), //
    PUBLISH(MenuKey.ACTION, MenuKeyConstant.PUBLISH), //
    EXPORT(MenuKey.ACTION, MenuKeyConstant.EXPORT), //
    DOWNLOAD_IMAGE(MenuKey.ACTION, MenuKeyConstant.DOWNLOAD_IMAGE), //
    COPY(MenuKey.ACTION, MenuKeyConstant.COPY), //
    MOVE(MenuKey.ACTION, MenuKeyConstant.MOVE), //
    DELETE(MenuKey.ACTION, MenuKeyConstant.DELETE), //

    // Edit menu
    SELECT_ALL(MenuKey.EDIT, MenuKeyConstant.SELECT_ALL), //
    SELECT_NEIGHBORS(MenuKey.EDIT, MenuKeyConstant.SELECT_NEIGHBORS), //
    INVERT_SELECTION(MenuKey.EDIT, MenuKeyConstant.INVERT_SELECTION), //
    DESELECT_ALL(MenuKey.EDIT, MenuKeyConstant.DESELECT_ALL), //
    SELECT_DDD(MenuKey.EDIT, MenuKeyConstant.SELECT_DDD), //
    HIDE_SELECTION(MenuKey.EDIT, MenuKeyConstant.HIDE_SELECTION), //
    UNHIDE_SELECTION(MenuKey.EDIT, MenuKeyConstant.UNHIDE_SELECTION), //
    UNHIDE_ALL(MenuKey.EDIT, MenuKeyConstant.UNHIDE_ALL), //
    REMOVE_SELECTED_NODES(MenuKey.EDIT, MenuKeyConstant.REMOVE_SELECTED_NODES), //
    DELETE_PLUNKED(MenuKey.EDIT, MenuKeyConstant.DELETE_PLUNKED),
    COLLAPSE_ALL(MenuKey.EDIT, MenuKeyConstant.COLLAPSE_ALL),
    EXPAND_ALL(MenuKey.EDIT, MenuKeyConstant.EXPAND_ALL),
    SORT_ASC(MenuKey.EDIT, MenuKeyConstant.SORT_ASC),
    SORT_DSC(MenuKey.EDIT, MenuKeyConstant.SORT_DSC),
    QUICK_SORT_MENU_LBL(MenuKey.EDIT, MenuKeyConstant.QUICK_SORT_MENU_LBL),
    TOGGLE_SORT_MEASURE(MenuKey.EDIT, MenuKeyConstant.TOGGLE_SORT_MEASURE),
    TOGGLE_SORT_CATEGORY(MenuKey.EDIT, MenuKeyConstant.TOGGLE_SORT_CATEGORY),
    TOGGLE_SORT_X(MenuKey.EDIT, MenuKeyConstant.TOGGLE_SORT_X),
    TOGGLE_SORT_Y(MenuKey.EDIT, MenuKeyConstant.TOGGLE_SORT_Y),


    // Configure
    SETTINGS(MenuKey.CONFIGURE, MenuKeyConstant.SETTINGS), //
    FILTERS(MenuKey.CONFIGURE, MenuKeyConstant.FILTERS), //
    CREATE_SELECTION_FILTER(MenuKey.CONFIGURE, MenuKeyConstant.CREATE_SELECTION_FILTER),
    LOAD_ON_STARTUP(MenuKey.CONFIGURE, MenuKeyConstant.LOAD_ON_STARTUP), //
    TOGGLE_TOOLTIP_ANCHORS_HOVER(MenuKey.CONFIGURE, MenuKeyConstant.TOGGLE_TOOLTIP_ANCHORS_HOVER),//
    TOGGLE_TOOLTIP_ANCHORS_ALWAYS(MenuKey.CONFIGURE, MenuKeyConstant.TOGGLE_TOOLTIP_ANCHORS_ALWAYS),//

    // Tools
    NODES_LIST(MenuKey.TOOLS, MenuKeyConstant.NODES_LIST), //
    LINKS_LIST(MenuKey.TOOLS, MenuKeyConstant.LINKS_LIST), //
    TIME_PLAYER(MenuKey.TOOLS, MenuKeyConstant.TIME_PLAYER), //
    GRAPH_SEARCH(MenuKey.TOOLS, MenuKeyConstant.GRAPH_SEARCH), //
    BUNDLE(MenuKey.TOOLS, MenuKeyConstant.BUNDLE), //
    UNBUNDLE(MenuKey.TOOLS, MenuKeyConstant.UNBUNDLE), //
    QUICK_UNBUNDLE(MenuKey.TOOLS, MenuKeyConstant.QUICK_UNBUNDLE), //
    REVEAL_NEIGHBORS(MenuKey.TOOLS, MenuKeyConstant.REVEAL_NEIGHBORS), //
    FIND_PATHS(MenuKey.TOOLS, MenuKeyConstant.FIND_PATHS), //
    COMPUTE_SNA_METRICS(MenuKey.TOOLS, MenuKeyConstant.COMPUTE_SNA_METRICS), //
    APPEARANCE_EDITOR(MenuKey.TOOLS, MenuKeyConstant.APPEARANCE_EDITOR), //
    HIDE_LEGEND(MenuKey.TOOLS, MenuKeyConstant.HIDE_LEGEND), //
    SHOW_LEGEND(MenuKey.TOOLS, MenuKeyConstant.SHOW_LEGEND), //
    RESET_LEGEND(MenuKey.TOOLS, MenuKeyConstant.RESET_LEGEND), // ,
    SHOW_GROUPS(MenuKey.TOOLS, MenuKeyConstant.SHOW_GROUPS), //
    HIDE_GROUPS(MenuKey.TOOLS, MenuKeyConstant.HIDE_GROUPS), //
    RESET_GROUPS(MenuKey.TOOLS, MenuKeyConstant.RESET_GROUPS), // ,
    SHOW_SEARCH(MenuKey.TOOLS, MenuKeyConstant.SHOW_SEARCH), //
    HIDE_SEARCH(MenuKey.TOOLS, MenuKeyConstant.HIDE_SEARCH), //
    HIDE_ANNOTATION(MenuKey.TOOLS, MenuKeyConstant.HIDE_ANNOTATION), //
    SHOW_ANNOTATION(MenuKey.TOOLS, MenuKeyConstant.SHOW_ANNOTATION), //
    ZOOM_OUT(MenuKey.TOOLS, MenuKeyConstant.ZOOM_OUT), //
    ZOOM_IN(MenuKey.TOOLS, MenuKeyConstant.ZOOM_IN), //
    HIDE_MULTITYPE_DECORATOR(MenuKey.TOOLS, MenuKeyConstant.HIDE_MULTITYPE_DECORATOR), //
    SHOW_MULTITYPE_DECORATOR(MenuKey.TOOLS, MenuKeyConstant.SHOW_MULTITYPE_DECORATOR), //
    SHOW_LINKUP_HIGLIGHTS(MenuKey.TOOLS, MenuKeyConstant.SHOW_LINKUP_HIGLIGHTS), //
    CLEAR_MERGE_HIGHLIGHTS(MenuKey.TOOLS, MenuKeyConstant.CLEAR_MERGE_HIGHLIGHTS), //
    COPY_CELLS(MenuKey.TOOLS, MenuKeyConstant.COPY_CELLS), //
    //HIDE_LINKUP_HIGLIGHTS(MenuKey.TOOLS, MenuKeyConstant.HIDE_LINKUP_HIGLIGHTS), //

    CHART_METRICS(MenuKey.TOOLS, MenuKeyConstant.CHART_METRICS),
    TOGGLE_SHOW_BREADCRUMBS(MenuKey.TOOLS, MenuKeyConstant.TOGGLE_BREAD),
    MATRIX_METRICS(MenuKey.TOOLS, MenuKeyConstant.MATRIX_METRICS),
    TIMELINE_METRICS(MenuKey.TOOLS, MenuKeyConstant.TIMELINE_METRICS),
    MAP_METRICS(MenuKey.TOOLS, MenuKeyConstant.MAP_METRICS),
    HIDE_OVERVIEW(MenuKey.TOOLS, MenuKeyConstant.HIDE_OVERVIEW), //
    SHOW_OVERVIEW(MenuKey.TOOLS, MenuKeyConstant.SHOW_OVERVIEW), //

    // Layout
    CENTRIFUGE(MenuKey.LAYOUT, MenuKeyConstant.CENTRIFUGE), //
    CIRCULAR(MenuKey.LAYOUT, MenuKeyConstant.CIRCULAR), //
    FORCE_DIRECTED(MenuKey.LAYOUT, MenuKeyConstant.FORCE_DIRECTED), //
    LINEAR_HIERARCHY(MenuKey.LAYOUT, MenuKeyConstant.LINEAR_HIERARCHY), //
    RADIAL(MenuKey.LAYOUT, MenuKeyConstant.RADIAL), //
    SCRAMBLE_AND_PLACE(MenuKey.LAYOUT, MenuKeyConstant.SCRAMBLE_AND_PLACE), //
    GRID(MenuKey.LAYOUT, MenuKeyConstant.GRID), //
    APPLY_FORCE(MenuKey.LAYOUT, MenuKeyConstant.APPLY_FORCE);

    private boolean topLevel;
    private MenuKeyConstant label;
    private IconType icon;
    private MenuKey parent;
    private List<MenuKey> children = new ArrayList<MenuKey>();

    private static List<MenuKey> topLevelMenus = new ArrayList<MenuKey>();

    static {
        // Compute top level items
        for (MenuKey menuItem : values()) {
            if (menuItem.topLevel) {
                topLevelMenus.add(menuItem);
            }
        }
    }

    /**
     * For top level menus.
     */
    private MenuKey(MenuKeyConstant label, IconType icon) {
        this.label = label;
        this.icon = icon;
        topLevel = true;
    }

    /**
     * For child menus.
     * @param parent
     * @param label
     */
    private MenuKey(MenuKey parent, MenuKeyConstant label) {
        this.label = label;
        this.parent = parent;
        parent.addChild(this);
    }

    public String getLabel() {
        return label.toString();
    }

    public IconType getIcon() {
        return icon;
    }

    private void addChild(MenuKey child) {
        children.add(child);
    }

    public MenuKey getParent() {
        return parent;
    }

    public List<MenuKey> getChildren() {
        return children;
    }

    public static List<MenuKey> getTopLevelMenus() {
        return topLevelMenus;
    }

}
