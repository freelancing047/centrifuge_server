package csi.client.gwt.viz.graph.link.settings;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class SaveLinkSettings extends AbstractLinkSettingsActivity {

    private LinkSettings linkSettings;

    SaveLinkSettings(LinkSettings linkSettings) {
        this.linkSettings = linkSettings;
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        super.start(panel, eventBus);
        LinkSettingsModel model = linkSettings.getModel();
        LinkSettingsView view = linkSettings.getView();

        model.getLinkDirection().setForwardValues(view.getDirectionTab().getForwardValues());
        model.getLinkDirection().setReverseValues(view.getDirectionTab().getReverseValues());

        model.save();
        view.close();
        linkSettings.close();
    }

    @Override
    public void updateColorSettings(Boolean value, boolean render) {
        LinkSettingsView view = linkSettings.getView();
        LinkSettingsModel model = linkSettings.getModel();
        LinkColor color = model.getLinkColor();
        color.setColorEnabled(value);
        LinkAppearanceTab appearanceTab = view.getAppearanceTab();
        appearanceTab.setColorCheckBox(value);
    }

}
