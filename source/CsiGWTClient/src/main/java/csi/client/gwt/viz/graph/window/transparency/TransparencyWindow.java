package csi.client.gwt.viz.graph.window.transparency;

import com.github.gwtbootstrap.client.ui.Button;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.Slider;
import csi.server.common.dto.KeyValueItem;

public class TransparencyWindow implements IsWidget {

    private TransparencySettings settings;
    @UiField
    Slider nodeSlider;
    @UiField
    Slider linkSlider;
    @UiField
    Slider labelSlider;
    @UiField
    Slider nodeFactorSlider;
    @UiField
    SimpleLayoutPanel outerPanel;

    interface MyUiBinder extends UiBinder<Widget, TransparencyWindow> {
    }

    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    public TransparencyWindow(TransparencySettings settings) {
        this.settings = settings;
        uiBinder.createAndBindUi(this);
        nodeSlider.setValue(settings.getModel().getNodeTransparency());
        linkSlider.setValue(settings.getModel().getLinkTransparency());
        labelSlider.setValue(settings.getModel().getLabelTransparency());
        nodeFactorSlider.setValue(settings.getModel().getMinimumNodeScaleFactor());
    }

    public void show() {
    }

    @Override
    public Widget asWidget() {
        return outerPanel;
    }

    @UiField
    Button applyButton;

    @UiHandler("applyButton")
    void onApplyClick(ClickEvent event) {
        settings.apply();
    }

    @UiHandler("nodeSlider")
    void onNodeSlider(ValueChangeEvent<Integer> event) {
        settings.getModel().setNodeTransparency(event.getValue().intValue());
    }

    @UiHandler("linkSlider")
    void onLinkSlider(ValueChangeEvent<Integer> event) {
        settings.getModel().setLinkTransparency(event.getValue().intValue());
    }

    @UiHandler("labelSlider")
    void onLabelSlider(ValueChangeEvent<Integer> event) {
        settings.getModel().setLabelTransparency(event.getValue().intValue());
    }

    @UiHandler("nodeFactorSlider")
    void onNodeFactorSlider(ValueChangeEvent<Integer> event) {
        settings.getModel().setMinimumNodeScaleFactor(event.getValue().intValue());
    }
}
