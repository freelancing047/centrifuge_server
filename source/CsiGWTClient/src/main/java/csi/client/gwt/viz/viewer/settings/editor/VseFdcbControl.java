package csi.client.gwt.viz.viewer.settings.editor;

import com.github.gwtbootstrap.client.ui.FluidRow;
import com.github.gwtbootstrap.client.ui.base.InlineLabel;
import com.google.common.collect.Lists;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Composite;
import csi.client.gwt.WebMain;
import csi.client.gwt.dataview.AbstractDataViewPresenter;
import csi.client.gwt.widget.combo_boxes.FieldDefComboBox;
import csi.server.common.model.FieldDef;
import csi.shared.gwt.viz.viewer.settings.editor.LensFieldDefSetting;
import csi.shared.gwt.viz.viewer.settings.editor.LensSettingsControl;

public class VseFdcbControl extends Composite implements VSEControl {

    private LensSettingsControl control;

    public VseFdcbControl(LensSettingsControl control) {

        this.control = control;
        FluidRow fr = new FluidRow();
        fr.getElement().getStyle().setProperty("width", "unset");
        fr.getElement().getStyle().setPaddingLeft(24, Style.Unit.PX);
        fr.getElement().getStyle().setLineHeight(30, Style.Unit.PX);
        FieldDefComboBox fdcb = new FieldDefComboBox();
        AbstractDataViewPresenter dataViewPresenter = WebMain.injector.getMainPresenter().getDataViewPresenter(true);
        fdcb.getStore().addAll(Lists.newArrayList(dataViewPresenter.getDataView().getMeta().getModelDef().getFieldDefs()));
        fdcb.setWidth("150px");
        if (control instanceof LensFieldDefSetting) {
            LensFieldDefSetting fdcbSetting = (LensFieldDefSetting) control;
            FieldDef fieldDef = dataViewPresenter.getFieldByLocalId(fdcbSetting.getValue());
            if (fieldDef != null) {
                fdcb.setValue(fieldDef);
            }
        }
        fdcb.getElement().getStyle().setMargin(4, Style.Unit.PX);
        fdcb.getElement().getStyle().setHeight(25, Style.Unit.PX);
        fdcb.getElement().getStyle().setFloat(Style.Float.RIGHT);
        fr.add(new InlineLabel(control.getLabel()));
        fr.add(fdcb);
        initWidget(fr);
        fdcb.addValueChangeHandler(new ValueChangeHandler<FieldDef>() {
            @Override
            public void onValueChange(ValueChangeEvent<FieldDef> event) {
                if (control instanceof LensFieldDefSetting) {
                    LensFieldDefSetting fdcbSetting = (LensFieldDefSetting) control;
                    fdcbSetting.setValue(fdcb.getValue().getLocalId());
                }
            }
        });
    }
}
