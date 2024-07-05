package csi.client.gwt.viz.viewer;

import com.github.gwtbootstrap.client.ui.*;
import com.github.gwtbootstrap.client.ui.base.InlineLabel;
import com.github.gwtbootstrap.client.ui.constants.BaseIconType;
import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.github.gwtbootstrap.client.ui.resources.ButtonSize;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.ToStringValueProvider;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.widget.core.client.ListView;
import com.sencha.gxt.widget.core.client.form.ListField;
import csi.client.gwt.viz.viewer.lens.LensImageViewer;
import csi.client.gwt.viz.viewer.settings.editor.ViewerSettingsEditorImpl;
import csi.client.gwt.widget.boot.Dialog;
import csi.server.common.model.visualization.viewer.LensDef;
import csi.server.common.model.visualization.viewer.NodeNeighborLabelLensDef;
import csi.server.common.model.visualization.viewer.Objective;

public class ViewerViewImpl implements ViewerImpl.ViewerView {
    private ViewerImpl viewer;
    private final FluidContainer widget;

    public ViewerViewImpl(ViewerImpl viewer) {
        this.viewer = viewer;
        widget = new FluidContainer();
        widget.setHeight("100%");
        widget.setWidth("300px");
        Style style = widget.getElement().getStyle();
        style.setPadding(0, Style.Unit.PX);
        style.setOverflowY(Style.Overflow.AUTO);
        style.setOverflowX(Style.Overflow.HIDDEN);
        widget.getElement().addClassName("insight-viewer");

    }

    @Override
    public Widget asWidget() {
        return widget;
    }

    @Override
    public void setObjective(Objective objective) {
        FluidRow    row = new FluidRow();
        row.getElement().getStyle().setPaddingRight(4, Style.Unit.PX);
//        addVisualizationName(objective, row);
//        row.add(new InlineLabel(objective.getItemName()));
        addHideButton(row);
        addConfigButton(row);
        widget.add(row);
    }

    public void addHideButton(FluidRow row) {
        Button button = new Button();
        button.getElement().getStyle().setFloat(Style.Float.RIGHT);
        button.setType(ButtonType.LINK);
        button.setIcon(IconType.REMOVE);
        button.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                viewer.getContainer().hide();

            }
        });
        row.add(button);
    }

    public void addConfigButton(FluidRow row) {
        Button button = new Button();
        button.getElement().getStyle().setFloat(Style.Float.RIGHT);
        button.setType(ButtonType.LINK);
        button.setIcon(IconType.GEAR);
        button.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                new ViewerSettingsEditorImpl(viewer.getModel().getSettings());

            }
        });//I think I can follow you
        row.add(button);
    }

    public void addVisualizationName(Objective objective, FluidRow row) {
        row.add(new InlineLabel(objective.getVisualizationName()));
    }

    @Override
    public void addLensImageViewer(LensImageViewer lens) {
        FluidRow row = new FluidRow();
        row.add(lens);
        widget.add(row);
    }

    @Override
    public void removeAllLensImageViewers() {
        widget.clear();
    }
}
