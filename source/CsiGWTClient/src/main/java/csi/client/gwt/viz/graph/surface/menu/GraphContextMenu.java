package csi.client.gwt.viz.graph.surface.menu;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import com.github.gwtbootstrap.client.ui.NavLink;
import com.github.gwtbootstrap.client.ui.base.IconAnchor;
import com.google.common.collect.BiMap;
import com.google.common.collect.EnumHashBiMap;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import csi.client.gwt.viz.graph.surface.GraphSurface;

public class GraphContextMenu extends Composite {

    public enum GraphContextActionEnum {
        BUNDLE,
        DESELECT_ALL,
        DESELECT,
        HIDE_SELECTION,
        QUICK_REVEAL_NEIGHBORS,
        QUICK_SELECT_NEIGHBORS,
        QUICK_UNBUNDLE,
        SELECT_ALL,
        SELECT,
        DETAILS,
        SHOW_ONLY,
        PLUNK_NODE,
        PLUNK_LINK,
        EDIT_PLUNKED_ITEM,
        DELETE_PLUNKED_ITEM,
        ADD_ANNOTATION,
        UNHIDE_ALL
    }

    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    @UiField
    NavLink bundleLink;
    @UiField
    NavLink deselectAllLink;
    @UiField
    NavLink deselectLink;
    @UiField
    NavLink hideSelectionLink;
    @UiField
    NavLink quickRevealNeighborsLink;
    @UiField
    NavLink quickSelectNeighborsLink;
    @UiField
    NavLink quickUnbundleLink;
    @UiField
    NavLink selectAllLink;
    @UiField
    NavLink selectLink;
    @UiField
    NavLink showDetailsLink;
    @UiField
    NavLink showOnlyLink;
    @UiField
    NavLink plunkNodeLink;
    @UiField
    NavLink plunkLinkLink;
    @UiField
    NavLink editPlunkedLink;
    @UiField
    NavLink deletePlunkedLink;
    @UiField
    NavLink addAnnotationLink;

    @UiField
    NavLink unhideAll;



    private BiMap<GraphContextActionEnum, NavLink> navLinkMap = EnumHashBiMap.create(GraphContextActionEnum.class);
    private List<NavLink> hasSubmenuList = Lists.newArrayList();

    private GraphSurface graphSurface;
    private boolean readOnly = false;

    interface MyUiBinder extends UiBinder<Widget, GraphContextMenu> {
    }

    public GraphContextMenu(GraphSurface graphSurface) {
        this.graphSurface = graphSurface;
        initWidget(uiBinder.createAndBindUi(this));
        navLinkMap.put(GraphContextActionEnum.SELECT, selectLink);
        navLinkMap.put(GraphContextActionEnum.DETAILS, showDetailsLink);
        navLinkMap.put(GraphContextActionEnum.DESELECT, deselectLink);
        navLinkMap.put(GraphContextActionEnum.SELECT_ALL, selectAllLink);
        navLinkMap.put(GraphContextActionEnum.DESELECT_ALL, deselectAllLink);
        navLinkMap.put(GraphContextActionEnum.UNHIDE_ALL, unhideAll);

        if (!readOnly) {
            navLinkMap.put(GraphContextActionEnum.BUNDLE, bundleLink);
            navLinkMap.put(GraphContextActionEnum.QUICK_UNBUNDLE, quickUnbundleLink);
            navLinkMap.put(GraphContextActionEnum.HIDE_SELECTION, hideSelectionLink);
            navLinkMap.put(GraphContextActionEnum.SHOW_ONLY, showOnlyLink);
            navLinkMap.put(GraphContextActionEnum.QUICK_REVEAL_NEIGHBORS, quickRevealNeighborsLink);
            navLinkMap.put(GraphContextActionEnum.QUICK_SELECT_NEIGHBORS, quickSelectNeighborsLink);
            navLinkMap.put(GraphContextActionEnum.PLUNK_NODE, plunkNodeLink);
            navLinkMap.put(GraphContextActionEnum.PLUNK_LINK, plunkLinkLink);
            navLinkMap.put(GraphContextActionEnum.EDIT_PLUNKED_ITEM, editPlunkedLink);
            navLinkMap.put(GraphContextActionEnum.DELETE_PLUNKED_ITEM, deletePlunkedLink);
            navLinkMap.put(GraphContextActionEnum.ADD_ANNOTATION, addAnnotationLink);
        }
        hasSubmenuList.add(quickRevealNeighborsLink);

        hideAll();
        bindButtons(new ContextMenuClickHandler());
        bindButtons(new ContextMenuMouseEventHandler());
        bindButtons(new ContextMenuDoubleClickHandler());
    }

    public void hideAll() {
        for (GraphContextActionEnum action : GraphContextActionEnum.values()) {
            hide(action);
        }
    }

    public void setReadOnly() {

        if (!readOnly) {

            navLinkMap.remove(GraphContextActionEnum.BUNDLE);
            navLinkMap.remove(GraphContextActionEnum.QUICK_UNBUNDLE);
            navLinkMap.remove(GraphContextActionEnum.HIDE_SELECTION);
            navLinkMap.remove(GraphContextActionEnum.SHOW_ONLY);
            navLinkMap.remove(GraphContextActionEnum.QUICK_REVEAL_NEIGHBORS);
            navLinkMap.remove(GraphContextActionEnum.QUICK_SELECT_NEIGHBORS);
            navLinkMap.remove(GraphContextActionEnum.PLUNK_NODE);
            navLinkMap.remove(GraphContextActionEnum.PLUNK_LINK);
            navLinkMap.remove(GraphContextActionEnum.EDIT_PLUNKED_ITEM);
            navLinkMap.remove(GraphContextActionEnum.DELETE_PLUNKED_ITEM);
            navLinkMap.remove(GraphContextActionEnum.ADD_ANNOTATION);
        }
        readOnly = true;
    }

    private void hide(GraphContextActionEnum action) {
        checkNotNull(action);
        NavLink navLink = checkNotNull(navLinkMap.get(action));
        navLink.setVisible(false);
    }

    private void bindButtons(ClickHandler handler) {
        for (NavLink navLink : navLinkMap.values()) {
            if (!hasSubmenu(navLink)) {
                navLink.addClickHandler(handler);
            }
        }
    }

    private boolean hasSubmenu(NavLink navLink) {
        return hasSubmenuList.contains(navLink);
    }

    private class ContextMenuClickHandler implements ClickHandler {

        @Override
        public void onClick(ClickEvent event) {
            Object source = event.getSource();
            Widget source2 = null;
            if (source instanceof IconAnchor) {
                IconAnchor iconAnchor = (IconAnchor) source;
                source2 = iconAnchor.getParent();
            }
            if (source2 instanceof NavLink) {
                NavLink navLink = (NavLink) source2;
                GraphContextActionEnum action = navLinkMap.inverse().get(navLink);
                if (action != null) {
                    ContextMenuPresenter contextMenuPresenter = graphSurface.getContextMenuPresenter();
                    if (contextMenuPresenter != null) {
                        contextMenuPresenter.handleSelectedAction(action);
                    }
                }
            }
        }
    }

    private void bindButtons(ContextMenuMouseEventHandler handler) {
        for (NavLink navLink : navLinkMap.values()) {
            navLink.addDomHandler(handler, MouseOverEvent.getType());
        }
    }

    private class ContextMenuMouseEventHandler implements MouseOverHandler {
        @Override
        public void onMouseOver(final MouseOverEvent event) {
            Object source = event.getSource();
            if (source instanceof NavLink) {
                NavLink navLink = (NavLink) source;
                GraphContextActionEnum action = navLinkMap.inverse().get(navLink);
                if (action != null) {
                    ContextMenuPresenter contextMenuPresenter = graphSurface.getContextMenuPresenter();
                    if (contextMenuPresenter != null) {
                        int x = navLink.getAbsoluteLeft() + navLink.getOffsetWidth();
                        int y = navLink.getAbsoluteTop();
                        contextMenuPresenter.handleMouseOverAction(x, y, action);
                    }
                }
            }
        }
    }

    private void bindButtons(DoubleClickHandler handler) {
        for (NavLink navLink : navLinkMap.values()) {
            if (hasSubmenu(navLink)) {
                navLink.getAnchor().addDomHandler(handler, DoubleClickEvent.getType());
            }
        }
    }

    private class ContextMenuDoubleClickHandler implements DoubleClickHandler {
        @Override
        public void onDoubleClick(DoubleClickEvent event) {
            Object source = event.getSource();
            Widget source2 = null;
            if (source instanceof IconAnchor) {
                IconAnchor iconAnchor = (IconAnchor) source;
                source2 = iconAnchor.getParent();
            }
            if (source2 instanceof NavLink) {
                NavLink navLink = (NavLink) source2;
                GraphContextActionEnum action = navLinkMap.inverse().get(navLink);
                if (action != null) {
                    ContextMenuPresenter contextMenuPresenter = graphSurface.getContextMenuPresenter();
                    if (contextMenuPresenter != null) {
                        contextMenuPresenter.handleSelectedAction(action);
                    }
                }
            }
        }
    }

    public void show(GraphContextActionEnum action) {
        checkNotNull(action);
        NavLink navLink = checkNotNull(navLinkMap.get(action));
        navLink.setVisible(true);
    }
}