package csi.client.gwt.viz.viewer.settings.editor;

import com.github.gwtbootstrap.client.ui.*;
import com.github.gwtbootstrap.client.ui.constants.FormType;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.ListBox;
import csi.client.gwt.widget.boot.Dialog;

public class ViewerSettingsEditorViewImpl implements ViewerSettingsEditorView {
    private ViewerSettingsEditor viewerSettingsEditor;
    private FluidContainer fc1;
    private FluidContainer fc2;
    private Dialog configDialog;

    public ViewerSettingsEditorViewImpl(ViewerSettingsEditorImpl viewerSettingsEditor) {
        this.viewerSettingsEditor = viewerSettingsEditor;
        initialize();
    }

    private void initialize() {
        configDialog = new Dialog();
        configDialog.setTitle("Configure");
        FluidContainer fc = new FluidContainer();
        fc.setHeight("400px");
        fc.setWidth("600px");
        FluidRow row1 = new FluidRow();
        row1.setHeight("100%");
        {
            Column c1 = new Column(5);
            addAvailableControls(c1);
            Column c3 = new Column(7);
            if (addCurrentLenses(c3)) return;
            row1.add(c1);
            row1.add(c3);
        }


        fc.add(row1);
        configDialog.add(fc);
        configDialog.getActionButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                viewerSettingsEditor.updateModel();

            }
        });
        configDialog.getCancelButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                configDialog.hide();
            }
        });
        configDialog.show();

    }

    private boolean addCurrentLenses(Column c3) {
        FluidRow rowAvailableLens = new FluidRow();
        {
            fc1 = new FluidContainer();
            fc1.setHeight("390px");
            fc1.getElement().getStyle().setOverflowY(Style.Overflow.AUTO);
            fc1.getElement().getStyle().setBorderColor("#AAA");
            fc1.getElement().getStyle().setBorderStyle(Style.BorderStyle.SOLID);
            fc1.getElement().getStyle().setBorderWidth(1, Style.Unit.PX);
            fc1.getElement().getStyle().setPadding(3, Style.Unit.PX);
            fc1.getElement().getStyle().setOverflowX(Style.Overflow.HIDDEN);
            rowAvailableLens.add(fc1);
            c3.add(rowAvailableLens);
        }
        return false;
    }

    private void addOrderControl(Column c4) {
        c4.setHeight("100%");
        c4.getElement().getStyle().setPaddingTop(180, Style.Unit.PX);
        {
            Button w = new Button();
            w.setIcon(IconType.LONG_ARROW_UP);
            c4.add(w);
        }
        {
            Button w = new Button();
            w.setIcon(IconType.LONG_ARROW_DOWN);
            c4.add(w);
        }
    }

    private void addAddRemoveControl(Column c2) {
        c2.setHeight("100%");
        c2.getElement().getStyle().setPaddingTop(180, Style.Unit.PX);
        {
            Button w = new Button();
            w.setIcon(IconType.LONG_ARROW_LEFT);
            c2.add(w);
        }
        {
            Button w = new Button();
            w.setIcon(IconType.LONG_ARROW_RIGHT);
            c2.add(w);
        }
    }

    private void addAvailableControls(Column c1) {
        FluidRow rowAvailableLensGroup = new FluidRow();
        addAvailableControlsGroupControl(rowAvailableLensGroup);
        c1.add(rowAvailableLensGroup);
        FluidRow rowAvailableLens = new FluidRow();
        {
            fc2 = new FluidContainer();
            fc2.getElement().getStyle().setOverflowX(Style.Overflow.HIDDEN);
            fc2.getElement().getStyle().setOverflowY(Style.Overflow.AUTO);
            fc2.setHeight("300px");
            fc2.getElement().getStyle().setBorderColor("#AAA");
            fc2.getElement().getStyle().setBorderStyle(Style.BorderStyle.SOLID);
            fc2.getElement().getStyle().setBorderWidth(1, Style.Unit.PX);
            fc2.getElement().getStyle().setPadding(3, Style.Unit.PX);

            rowAvailableLens.add(fc2);
            c1.add(rowAvailableLens);
        }
    }

    private void addAvailableControlsGroupControl(FluidRow rowAvailableLensGroup) {
        Form form = new Form();
        form.setType(FormType.INLINE);
        Controls controls = new Controls();
        ControlGroup cg = new ControlGroup();
        {
            ControlLabel controlLabel = new ControlLabel("Available Lens");
            cg.add(controlLabel);
            {
                ListBox lb = new ListBox();
                lb.setWidth("125px");
                lb.getElement().getStyle().setMarginLeft(12, Style.Unit.PX);
                {
                    lb.addItem("Graph");
                    lb.addItem("Matrix");
                    lb.addItem("Map");
                    lb.addItem("Table");
                    lb.addItem("Timeline");
                    lb.addItem("General");
                    lb.addItem("Other");
                }
                cg.add(lb);
            }
        }
    controls.add(cg);
        form.add(controls);
    rowAvailableLensGroup.add(form);
    }

    @Override
    public void addSettingsContainer(LensSettingsContainer lsc) {

        fc1.add(lsc);
    }

    @Override
    public void addAvailableLens(AvailableLensControl control) {
        fc2.add(control);

    }

    @Override
    public void showDialog() {
configDialog.show();
    }

    @Override
    public void hide() {
        configDialog.hide();
    }
}
