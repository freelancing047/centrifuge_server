package csi.client.gwt.viz.graph.link.settings;

import java.util.ArrayList;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

import csi.client.gwt.viz.graph.node.settings.SizingAttribute;
import csi.client.gwt.viz.graph.node.settings.tooltip.TooltipFunction;
import csi.client.gwt.viz.graph.shared.AbstractGraphObjectScale.ScaleMode;
import csi.server.common.model.FieldDef;
import csi.shared.core.color.ClientColorHelper.Color;
import csi.shared.gwt.viz.graph.LinkDirection;

public class ShowLinkSettings extends AbstractActivity implements LinkSettingsPresenter {

    private LinkSettings linkSettings;

    public ShowLinkSettings(LinkSettings linkSettings) {
        this.linkSettings = linkSettings;
    }

    @Override
    public void cancel() {
        // should move to different activity.
        LinkSettingsView view = linkSettings.getView();
        view.close();
        linkSettings.close();
    }

    @Override
    public void delete() {
        // should move to different activity.
        linkSettings.delete();
        LinkSettingsView view = linkSettings.getView();
        view.close();
        linkSettings.close();
    }

    @Override
    public void save() {
        linkSettings.save();
    }

    @Override
    public void setColor(Color color) {
        LinkSettingsView view = linkSettings.getView();
        LinkSettingsModel model = linkSettings.getModel();
        LinkColor linkColor = model.getLinkColor();
        if (color != null) {
            linkColor.setColor(color);
        }
        view.setColor(linkColor);
    }

    @Override
    public void setHasSize(boolean value) {
        LinkSettingsView view = linkSettings.getView();
        LinkSettingsModel model = linkSettings.getModel();
        LinkSize scale = model.getLinkSize();
        scale.setEnabled(value);
        view.setSize(scale);
    }

    @Override
    public void setHideLabel(boolean hidden) {
        LinkSettingsView view = linkSettings.getView();
        LinkSettingsModel model = linkSettings.getModel();
        LinkLabel label = model.getLinkLabel();
        label.setHidden(hidden);
        view.setLabel(label);
    }

    @Override
    public void setHideType(boolean hidden) {
        LinkSettingsView view = linkSettings.getView();
        LinkSettingsModel model = linkSettings.getModel();
        LinkType type = model.getLinkType();
        type.setHidden(hidden);
        view.setType(type);
    }

    @Override
    public void setLabelField(FieldDef field) {
        LinkSettingsView view = linkSettings.getView();
        LinkSettingsModel model = linkSettings.getModel();
        LinkLabel label = model.getLinkLabel();
        label.setFieldDef(field);
        view.setLabel(label);
    }

    @Override
    public void setLabelFixed(boolean fixed) {
        LinkSettingsView view = linkSettings.getView();
        LinkSettingsModel model = linkSettings.getModel();
        LinkLabel label = model.getLinkLabel();
        label.setFixed(fixed);
        view.setLabel(label);
    }

    @Override
    public void setLabelText(String text) {
        LinkSettingsView view = linkSettings.getView();
        LinkSettingsModel model = linkSettings.getModel();
        LinkLabel label = model.getLinkLabel();
        label.setText(text);
        view.setLabel(label);
    }

    @Override
    public void setSizeMeasure(String measure) {
        LinkSettingsView view = linkSettings.getView();
        LinkSettingsModel model = linkSettings.getModel();
        LinkSize scale = model.getLinkSize();
        scale.setMeasure(SizingAttribute.valueOf(measure));
        view.setSize(scale);
    }

    @Override
    public void setSizeMode(ScaleMode scaleMode) {
        LinkSettingsView view = linkSettings.getView();
        LinkSettingsModel model = linkSettings.getModel();
        LinkSize scale = model.getLinkSize();
        scale.setMode(scaleMode);
        view.setSize(scale);
    }

    @Override
    public void setSizeValue(double value) {
        LinkSettingsView view = linkSettings.getView();
        LinkSettingsModel model = linkSettings.getModel();
        LinkSize scale = model.getLinkSize();
        scale.setValue(value);
        view.setSize(scale);
    }

    @Override
    public void setTypeField(FieldDef field) {
        LinkSettingsView view = linkSettings.getView();
        LinkSettingsModel model = linkSettings.getModel();
        LinkType type = model.getLinkType();
        type.setFieldDef(field);
        view.setType(type);
    }

    @Override
    public void setTypeFixed(boolean fixed) {
        LinkSettingsView view = linkSettings.getView();
        LinkSettingsModel model = linkSettings.getModel();
        LinkType type = model.getLinkType();
        type.setFixed(fixed);
        view.setType(type);
    }

    @Override
    public void setTypeText(String text) {
        LinkSettingsView view = linkSettings.getView();
        LinkSettingsModel model = linkSettings.getModel();
        LinkType type = model.getLinkType();
        type.setText(text);
        view.setType(type);
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        LinkSettingsView view = linkSettings.getView();
        LinkSettingsModel model = linkSettings.getModel();
        view.bind(this);
        // name
        view.setName(model.getName());
        // size
        view.setSize(model.getLinkSize());
        // transparency
        view.setTransparency(model.getTransparency());
        // label
        view.setLabel(model.getLinkLabel());
        // type
        view.setType(model.getType());
        // color
        view.setColor(model.getLinkColor());
        
        view.getAppearanceTab().setColorEnabled(model.getLinkColor().isColorEnabled());
        view.getAppearanceTab().setWidthEnabled(model.getLinkSize().isEnabled());

        // direction
        view.setDirection(model.getLinkDirection());
        // tooltips
        view.setTooltips(model.getLinkTooltips());
        // and finally show.
        view.show();
    }

    @Override
    public void setDirectionMode(LinkDirection linkDirection) {
        LinkSettingsView view = linkSettings.getView();
        LinkSettingsModel model = linkSettings.getModel();
        LinkDirectionDef linkDirectionDef = model.getLinkDirection();
        linkDirectionDef.setLinkDirection(linkDirection);
        view.setDirection(linkDirectionDef);
    }

    @Override
    public void setDirectionField(FieldDef value) {
        LinkSettingsView view = linkSettings.getView();
        LinkSettingsModel model = linkSettings.getModel();
        LinkDirectionDef linkDirectionDef = model.getLinkDirection();
        if (value != linkDirectionDef.getFieldDef()) {
            linkDirectionDef.setFieldDef(value);
            linkDirectionDef.setForwardValues(new ArrayList<String>());
            linkDirectionDef.setReverseValues(new ArrayList<String>());
        }
        view.setDirection(linkDirectionDef);

    }

    @Override
    public void setName(String value) {
        LinkSettingsView view = linkSettings.getView();
        LinkSettingsModel model = linkSettings.getModel();
        model.setName(value);
        view.setName(value);
    }

    @Override
    public void setSizeFunction(TooltipFunction value) {
        LinkSettingsModel model = linkSettings.getModel();
        LinkSettingsView view = linkSettings.getView();
        LinkSize linkSize = model.getLinkSize();
        linkSize.setFunction(value);
        view.setSize(linkSize);
    }

    @Override
    public void setTransparencyFunction(TooltipFunction value) {
        LinkSettingsModel model = linkSettings.getModel();
        LinkSettingsView view = linkSettings.getView();
        LinkTransparency transparency = model.getTransparency();
        transparency.setFunction(value);
        view.setTransparency(transparency);
    }

    @Override
    public void setTransparencyMeasure(String value) {
        LinkSettingsModel model = linkSettings.getModel();
        LinkSettingsView view = linkSettings.getView();
        LinkTransparency transparency = model.getTransparency();
        transparency.setMeasure(SizingAttribute.valueOf(value));
        view.setTransparency(transparency);
    }

    @Override
    public void setTransparencyMode(ScaleMode mode) {
        LinkSettingsModel model = linkSettings.getModel();
        LinkSettingsView view = linkSettings.getView();
        LinkTransparency transparency = model.getTransparency();
        transparency.setMode(mode);
        view.setTransparency(transparency);
    }

    @Override
    public void setTransparencyValue(double value) {
        LinkSettingsModel model = linkSettings.getModel();
        LinkSettingsView view = linkSettings.getView();
        LinkTransparency transparency = model.getTransparency();
        transparency.setValue(value);
        view.setTransparency(transparency);
    }

    @Override
    public void setSizeField(FieldDef value) {
        LinkSettingsModel model = linkSettings.getModel();
        LinkSettingsView view = linkSettings.getView();
        LinkSize linkSize = model.getLinkSize();
        linkSize.setField(value);
        view.setSize(linkSize);
    }

    @Override
    public void setTransparencyField(FieldDef value) {
        LinkSettingsModel model = linkSettings.getModel();
        LinkSettingsView view = linkSettings.getView();
        LinkTransparency transparency = model.getTransparency();
        transparency.setField(value);
        view.setTransparency(transparency);
    }

    @Override
    public void setHasTransparency(Boolean value) {
        LinkSettingsView view = linkSettings.getView();
        LinkSettingsModel model = linkSettings.getModel();
        LinkTransparency transparency = model.getTransparency();
        transparency.setEnabled(value);
        view.setTransparency(transparency);
    }
    
    public void updateColorSettings(Boolean value, boolean render) {
        LinkSettingsView view = linkSettings.getView();
        LinkSettingsModel model = linkSettings.getModel();
        LinkColor color = model.getLinkColor();
        color.setColorEnabled(value);
        LinkAppearanceTab appearanceTab = view.getAppearanceTab();
        appearanceTab.setColorCheckBox(value);
    }
}
