package csi.client.gwt.viz.viewer.settings.editor;

import com.github.gwtbootstrap.client.ui.FluidRow;
import com.github.gwtbootstrap.client.ui.base.InlineLabel;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;
import csi.shared.gwt.viz.viewer.settings.editor.LensListSetting;
import csi.shared.gwt.viz.viewer.settings.editor.LensSettingsControl;

public class VSEListControl extends Composite implements VSEControl {

    private LensListSetting lensListSetting;

    public VSEListControl(LensSettingsControl control) {
        FluidRow fr = new FluidRow();
        fr.getElement().getStyle().setProperty("width", "unset");
        fr.getElement().getStyle().setPaddingLeft(24, Style.Unit.PX);
        fr.getElement().getStyle().setLineHeight(30, Style.Unit.PX);
        ListBox listBox = new ListBox();
        listBox.setWidth("150px");
        if (control instanceof LensListSetting) {
            lensListSetting = (LensListSetting) control;
            for (String value : lensListSetting.getValues()) {
                listBox.addItem(value);
                if (value.equals(lensListSetting.getSelectedValue())) {
                    listBox.setSelectedIndex(listBox.getItemCount() - 1);
                }
            }
        }
        listBox.getElement().getStyle().setMargin(4, Style.Unit.PX);
        listBox.getElement().getStyle().setHeight(25, Style.Unit.PX);
        listBox.getElement().getStyle().setFloat(Style.Float.RIGHT);
        fr.add(new InlineLabel(control.getLabel()));
        fr.add(listBox);
        initWidget(fr);
        listBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                if (lensListSetting == null) {
                    return;
                }
                lensListSetting.setSelectedValue(listBox.getSelectedValue());
            }
        });
    }
}