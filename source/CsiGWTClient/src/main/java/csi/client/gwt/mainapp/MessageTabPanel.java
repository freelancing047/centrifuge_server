package csi.client.gwt.mainapp;

import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RequiresResize;
import com.sencha.gxt.widget.core.client.TabPanel;
import com.sencha.gxt.widget.core.client.menu.Item;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;


public class MessageTabPanel extends TabPanel implements RequiresResize, HasMouseDownHandlers, HasMouseUpHandlers {
    public static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
    private Menu menu = null;
    private MenuItem editMenuItem = null;
    private MenuItem deleteMenuItem = null;
    private Label actionsTab = null;
    private Label annotationsTab = null;
    private Label tagsTab = null;


    public MessageTabPanel() {
        super();
        init();
    }

    private void init() {
        addMouseDownHandler(createMouseDownHandler());
        addMouseUpHandler(createMouseUpHandler());

//        add(actionsTab);
//        add(annotationsTab);
//        add(tagsTab);



    }

    private MouseUpHandler createMouseUpHandler() {
        return new MouseUpHandler() {
            @Override
            public void onMouseUp(MouseUpEvent event) {

            }
        };
    }

    private MouseDownHandler createMouseDownHandler() {
        return new MouseDownHandler() {
            @Override
            public void onMouseDown(MouseDownEvent event) {

            }
        };
    }

    private void setUpActionsTab() {
        actionsTab.setText("Actions");

    }

    private void setUpAnnotationsTab() {
        annotationsTab.setText("Annotations");
    }

    private void setUpTagsTab() {
        tagsTab.setText("Tags");
    }

    @Override
    public HandlerRegistration addMouseDownHandler(MouseDownHandler handler) {
        return null;
    }

    @Override
    public HandlerRegistration addMouseUpHandler(MouseUpHandler handler) {
        return null;
    }

    @Override
    public void onResize() {

    }
}
