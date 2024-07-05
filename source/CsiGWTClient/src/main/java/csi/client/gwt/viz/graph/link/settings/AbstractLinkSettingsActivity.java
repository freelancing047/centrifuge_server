package csi.client.gwt.viz.graph.link.settings;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

import csi.client.gwt.viz.graph.node.settings.tooltip.TooltipFunction;
import csi.client.gwt.viz.graph.shared.AbstractGraphObjectScale.ScaleMode;
import csi.server.common.model.FieldDef;
import csi.shared.core.color.ClientColorHelper.Color;
import csi.shared.gwt.viz.graph.LinkDirection;

public abstract class AbstractLinkSettingsActivity extends AbstractActivity implements LinkSettingsPresenter {

    public AbstractLinkSettingsActivity() {
        super();
    }

    @Override
    public void cancel() {
        // TODO Auto-generated method stub

    }

    @Override
    public void delete() {
        // TODO Auto-generated method stub

    }

    @Override
    public void save() {
        // TODO Auto-generated method stub

    }

    @Override
    public void setColor(Color color) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setHasSize(boolean value) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setHideLabel(boolean hidden) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setHideType(boolean b) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setLabelField(FieldDef field) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setLabelFixed(boolean fixed) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setLabelText(String text) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setSizeMeasure(String measure) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setSizeMode(ScaleMode scaleMode) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setSizeValue(double value) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setTypeField(FieldDef value) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setTypeFixed(boolean value) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setTypeText(String value) {
        // TODO Auto-generated method stub

    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setDirectionMode(LinkDirection valueOf) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setDirectionField(FieldDef value) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setName(String value) {

    }

    @Override
    public void setSizeFunction(TooltipFunction value) {

    }

    @Override
    public void setTransparencyFunction(TooltipFunction value) {

    }

    @Override
    public void setTransparencyMeasure(String value) {

    }

    @Override
    public void setTransparencyMode(ScaleMode dynamic) {

    }

    @Override
    public void setTransparencyValue(double value) {

    }

    @Override
    public void setSizeField(FieldDef value) {

    }

    @Override
    public void setTransparencyField(FieldDef value) {

    }

    @Override
    public void setHasTransparency(Boolean value) {

    }
}