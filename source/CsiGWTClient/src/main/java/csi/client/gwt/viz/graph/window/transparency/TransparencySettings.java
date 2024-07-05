package csi.client.gwt.viz.graph.window.transparency;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.gwt.activity.shared.ActivityManager;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.place.shared.PlaceController;
import com.google.web.bindery.event.shared.EventBus;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.button.ToolButton;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.util.BasicPlace;
import csi.client.gwt.viz.graph.Graph;

public class TransparencySettings {

    private Graph graph;
    private TransparencyWindow window;
    private ContentPanel contentPanel;
    private TransparencySettingsProxy model;

    public TransparencySettings(Graph graph) {
        this.graph = checkNotNull(graph);
        EventBus eventBus = new SimpleEventBus();
        PlaceController placeController = new PlaceController(eventBus, new PlaceController.DefaultDelegate());
        TransparencySettingsActivityMapper activityMapper = new TransparencySettingsActivityMapper(this);
        new ActivityManager(activityMapper, eventBus);
        placeController.goTo(BasicPlace.DEFAULT_PLACE);
        model = new TransparencySettingsProxy(this);
        window = new TransparencyWindow(this);
        contentPanel = GWT.create(ContentPanel.class);
        contentPanel.setVisible(false);
        CentrifugeConstants constants = CentrifugeConstantsLocator.get();
        contentPanel.setHeading(constants.appearanceTab_heading());
        contentPanel.getHeader().setBorders(false);
        contentPanel.getElement().getStyle().setProperty("boxShadow", "rgba(0, 0, 0, 0.2) 0px 5px 10px 0px"); //NON-NLS
        contentPanel.getElement().getStyle().setProperty("MozBoxSizing", "border-box");//NON-NLS
        contentPanel.setBodyBorder(false);
        ToolButton closeButton = new ToolButton(ToolButton.CLOSE);
        ClickHandler closeHandler = new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                contentPanel.setVisible(false);
            }
        };
        closeButton.addDomHandler(closeHandler, ClickEvent.getType());
        contentPanel.addTool(closeButton);
        contentPanel.setAllowTextSelection(false);
    }

    public void show() {
        contentPanel.setVisible(true);
        contentPanel.add(window.asWidget());
        contentPanel.setWidth(300);
        contentPanel.setHeight(210);
    }

    public ContentPanel asWindow() {
        return contentPanel;
    }

    public void apply() {
        graph.getGraphSurface().refresh(graph.getModel().saveSettings());
    }

    public Graph getGraph() {
        return graph;
    }

    public TransparencySettingsProxy getModel() {
        return model;
    }
}
