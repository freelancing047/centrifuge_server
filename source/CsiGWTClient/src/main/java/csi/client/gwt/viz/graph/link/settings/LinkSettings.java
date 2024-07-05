package csi.client.gwt.viz.graph.link.settings;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.place.shared.Place;
import com.google.web.bindery.event.shared.EventBus;

import csi.client.gwt.util.CSIActivityManager;
import csi.client.gwt.viz.graph.link.LinkProxy;
import csi.client.gwt.viz.graph.settings.GraphSettings;
import csi.server.common.model.themes.graph.GraphTheme;

public class LinkSettings {

    public class LinkSettingsActivityMapper implements ActivityMapper {

        @Override
        public Activity getActivity(Place place) {
            return null;
        }

    }

    private class ShowSoon implements RepeatingCommand {

        private LinkSettings linkSettings;

        public ShowSoon(LinkSettings linkSettings) {
            this.linkSettings = linkSettings;
        }

        @Override
        public boolean execute() {
            activityManager.setActivity(new ShowLinkSettings(linkSettings));
            return false;
        }
    }

    private CSIActivityManager activityManager;

    private GraphSettings graphSettings;
    private LinkProxy linkProxy;
    private EventBus eventBus;
    private LinkSettingsModel model;
    private LinkSettingsView view;
    private GraphTheme theme;

    public LinkSettings(GraphSettings graphSettings, LinkProxy linkProxy) {
        this.graphSettings = graphSettings;
        this.linkProxy = linkProxy;
        model = new LinkSettingsModel(this, linkProxy);
        view = new LinkSettingsDialog(this);
        eventBus = new SimpleEventBus();
        activityManager = new CSIActivityManager(new LinkSettingsActivityMapper(), eventBus);
        activityManager.setActivity(new PrepareLinkSettings(this));
    }

    public void close() {
        graphSettings.show();
    }

    public void delete() {
        graphSettings.deleteLink(linkProxy);
    }

//    public DataViewPresenter getDataviewPresenter() {
//        return graphSettings.getDataview();
//    }

    public GraphSettings getGraphSettings() {
        return graphSettings;
    }

    public LinkSettingsModel getModel() {
        return model;
    }

    public GraphTheme getTheme() {
        return theme;
    }

    public LinkSettingsView getView() {
        return view;
    }

    public void save() {
        activityManager.setActivity(new SaveLinkSettings(this));
    }

    public void setTheme(GraphTheme theme) {
        this.theme = theme;
    }

    public void show() {
        // I really need a GraphTheme
        Scheduler.get().scheduleFixedDelay(new ShowSoon(this), 25);
    }
}
