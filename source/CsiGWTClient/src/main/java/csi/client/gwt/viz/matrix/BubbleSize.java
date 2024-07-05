package csi.client.gwt.viz.matrix;

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
import com.google.inject.spi.LinkedKeyBinding;
import com.sencha.gxt.widget.core.client.Slider;
import csi.client.gwt.viz.matrix.*;
import csi.shared.core.util.Native;

public class BubbleSize implements IsWidget {

    @UiField
    Slider nodeSlider;
    @UiField
    Slider linkSlider;
    @UiField
    SimpleLayoutPanel outerPanel;

    MatrixPresenter presenter;

    interface MyUiBinder extends UiBinder<Widget, BubbleSize> {
    }

    private static BubbleSize.MyUiBinder uiBinder = GWT.create(BubbleSize.MyUiBinder.class);

    public BubbleSize(MatrixPresenter presenter) {
//        this.settings = settings;
        this.presenter = presenter;
        uiBinder.createAndBindUi(this);

        presenter.getModel().getSettings().getMatrixMeasureDefinition().getMeasureScaleMin();
        presenter.getModel().getSettings().getMatrixMeasureDefinition().getMeasureScaleMax();


        outerPanel.setSize("100px", "100px");

        nodeSlider.setValue(presenter.getModel().getSettings().getMatrixMeasureDefinition().getMeasureScaleMin());
        nodeSlider.setIncrement(1);
        nodeSlider.setMaxValue(100);
        linkSlider.setIncrement(1);
        linkSlider.setMaxValue(100);
        linkSlider.setValue(presenter.getModel().getSettings().getMatrixMeasureDefinition().getMeasureScaleMax());
//        labelSlider.setValue(0);
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
        presenter.getView().refresh();
//        outerPanel.getParent().removeFromParent();
    }

    @UiHandler("nodeSlider")
    void onNodeSlider(ValueChangeEvent<Integer> event) {
        presenter.getModel().getSettings().getMatrixMeasureDefinition().setMeasureScaleMax(event.getValue().intValue());

    }

    @UiHandler("linkSlider")
    void onLinkSlider(ValueChangeEvent<Integer> event) {
        presenter.getModel().getSettings().getMatrixMeasureDefinition().setMeasureScaleMin(event.getValue().intValue());

    }


}
