package csi.client.gwt.widget;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.github.gwtbootstrap.client.ui.NavLink;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.UListElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.event.dom.client.ContextMenuHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;

import csi.client.gwt.mainapp.SecurityBanner;
import csi.client.gwt.viz.graph.surface.ContextMenuInfo;

/**
 * Created by centrifuge on 8/23/2017.
 */
public class ContextMenuDisplay extends Composite {

    interface BinderInterface extends UiBinder<Widget, ContextMenuDisplay> {}

    class PopupMenuHandler implements ContextMenuHandler {

        int _returnValue;

        public PopupMenuHandler(int returnValueIn) {

            _returnValue = returnValueIn;
        }

        @Override
        public void onContextMenu(ContextMenuEvent event) {

            event.preventDefault();
            event.stopPropagation();
            _callback.onSelectionMade(_returnValue);
            popupPanel.hide();
        }
    }

    private static BinderInterface _uiBinder = GWT.create(BinderInterface.class);
    private static int NAV_HEIGHT = 22;
    private static int BANNER_HEIGHT = SecurityBanner.getHeight();
    private static int MARGIN_HEIGHT = 20;

    @UiField
    HTMLPanel menuPanel;
    @UiField
    UListElement menuList;
    private PopupPanel popupPanel;

    private int _width;
    private int _height;
    MenuCallback _callback = null;
    private Map<String, Integer> _map = new TreeMap<>();

    private ContextMenuHandler ignoreRightClick = event -> {
        event.preventDefault();
        event.stopPropagation();
    };

    private ClickHandler menuSelectionHandler = new ClickHandler() {
        @Override
        public void onClick(ClickEvent eventIn) {

            if (null != _callback) {

                Element mySelection = eventIn.getRelativeElement();
                String myLabel = mySelection.getInnerText().trim();
                Integer myChoice = _map.get(myLabel);

                if (null != myChoice) {

                    _callback.onSelectionMade(myChoice);
                    popupPanel.hide();
                }
            }
        }
    };

    public ContextMenuDisplay(List<String> menuItemsIn) {

        initializeDisplay(menuItemsIn.toArray(new String[0]));
    }

    public ContextMenuDisplay(String[] menuItemsIn) {

        initializeDisplay(menuItemsIn);
    }

    public void showMenuAt(int xIn, int yIn, MenuCallback callbackIn) {

        int myX = ensureContextMenuWithinWindowWidth(xIn);
        int myY = ensureContextMenuWithinWindowHeight(yIn);

        _callback = callbackIn;
        popupPanel.setPopupPosition(myX, myY);
        popupPanel.show();
    }

    private void initializeDisplay(String[] menuItemsIn) {

        initWidget(_uiBinder.createAndBindUi(this));

        _width = ContextMenuInfo.CONTEXT_MENU_WIDTH;
        _height = (NAV_HEIGHT * menuItemsIn.length) + MARGIN_HEIGHT + BANNER_HEIGHT;

        popupPanel = new PopupPanel(true, true);
        popupPanel.setGlassEnabled(true);
        menuPanel.sinkEvents(Event.ONCONTEXTMENU);
        menuPanel.addHandler(ignoreRightClick, ContextMenuEvent.getType());

        for (int i= 0; menuItemsIn.length > i; i++) {

            String myLabel = menuItemsIn[i].trim();
            NavLink myNavLink = new NavLink(myLabel);

            _map.put(myLabel, i);
            myNavLink.sinkEvents(Event.ONCONTEXTMENU);
            myNavLink.addHandler(new PopupMenuHandler(i), ContextMenuEvent.getType());
            myNavLink.addClickHandler(menuSelectionHandler);
            menuPanel.add(myNavLink, menuList);
        }
        popupPanel.add(this);
    }

    private int ensureContextMenuWithinWindowWidth(int xIn) {

        return ((xIn +_width) > Window.getClientWidth())
                ? Window.getClientWidth() - _width
                : xIn;
    }

    private int ensureContextMenuWithinWindowHeight(int yIn) {

        return ((yIn + _height) > Window.getClientHeight())
                ? Window.getClientHeight() - _height
                : yIn;
    }
}
