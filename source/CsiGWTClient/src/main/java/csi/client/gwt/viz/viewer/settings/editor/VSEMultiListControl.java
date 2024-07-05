package csi.client.gwt.viz.viewer.settings.editor;

import com.github.gwtbootstrap.client.ui.CheckBox;
import com.github.gwtbootstrap.client.ui.DropdownButton;
import com.github.gwtbootstrap.client.ui.FluidRow;
import com.github.gwtbootstrap.client.ui.NavLink;
import com.github.gwtbootstrap.client.ui.base.InlineLabel;
import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.google.common.collect.Lists;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import csi.shared.gwt.viz.viewer.settings.editor.LensMultiListSetting;
import csi.shared.gwt.viz.viewer.settings.editor.LensSettingsControl;

import java.util.List;

public class VSEMultiListControl extends Composite implements VSEControl {
    private final LensMultiListSetting lensMultiListSetting;
    private List<CheckBox> checkBoxes = Lists.newArrayList();

    public VSEMultiListControl(LensSettingsControl control) {
        FluidRow fr = new FluidRow();
        fr.getElement().getStyle().setProperty("width", "unset");
        fr.getElement().getStyle().setPaddingLeft(24, Style.Unit.PX);
        fr.getElement().getStyle().setLineHeight(30, Style.Unit.PX);
//            fr.getElement().getStyle().setBackgroundColor("#FCFCFC");
        fr.add(new InlineLabel("Direction"));
        DropdownButton dropdownButton = new DropdownButton("Options");
        if (control instanceof LensMultiListSetting) {
            lensMultiListSetting = (LensMultiListSetting) control;
        } else {
            throw new RuntimeException();//FIXME: better way?
        }
        for (String value : lensMultiListSetting.getValues()) {
            NavLink navLink = new NavLink();
            CheckBox checkBox = new CheckBox(value);
            checkBoxes.add(checkBox);
            checkBox.setValue(lensMultiListSetting.getValues().contains(value));
            navLink.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    event.preventDefault();
                    event.stopPropagation();
                    checkBox.setValue(!checkBox.getValue());
                    if (checkBox.getValue()) {
                        lensMultiListSetting.getSelectedValues().remove(value);
                    }
                    navLink.getAnchor().setFocus(false);
                }
            });
            navLink.add(checkBox);
            dropdownButton.add(navLink);
        }
        dropdownButton.setType(ButtonType.DEFAULT);
        dropdownButton.getTriggerWidget().getElement().getStyle().setColor("rgb(85, 85, 85)");
        dropdownButton.getTriggerWidget().getElement().getStyle().setProperty("background", "white");
        dropdownButton.getTriggerWidget().getElement().getStyle().setProperty("textAlign", "left");
        dropdownButton.getMenuWiget().getElement().getStyle().setProperty("minWidth", "125px");
        dropdownButton.getElement().getStyle().setMargin(4, Style.Unit.PX);
        dropdownButton.getElement().getStyle().setMarginRight(87, Style.Unit.PX);
        dropdownButton.getElement().getStyle().setHeight(25, Style.Unit.PX);
        dropdownButton.getElement().getStyle().setFloat(Style.Float.RIGHT);
        fr.add(dropdownButton);
        initWidget(fr);

        dropdownButton.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                List<String> strings = Lists.newArrayList();
                for (CheckBox checkBox : checkBoxes) {
                    if (checkBox.getValue()) {
                        strings.add(checkBox.getText());
                    }
                }
                lensMultiListSetting.setValues(strings);
            }
        });
    }

}
