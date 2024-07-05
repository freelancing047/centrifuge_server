package csi.client.gwt.viz.viewer.settings.editor;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.ButtonGroup;
import com.github.gwtbootstrap.client.ui.FluidContainer;
import com.github.gwtbootstrap.client.ui.FluidRow;
import com.github.gwtbootstrap.client.ui.base.InlineLabel;
import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.github.gwtbootstrap.client.ui.resources.ButtonSize;
import com.google.common.collect.Lists;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import csi.shared.gwt.viz.viewer.settings.editor.LensDefSettings;

import java.util.ArrayList;

public class LensSettingsContainer extends Composite {

    private final FluidContainer fc;
    private final ArrayList<VSEControl> controls;
    private LensDefSettings lsd;
    private ViewerSettingsEditor vse;
    private boolean controlsHidden = true;

    public LensSettingsContainer(LensDefSettings lensDefSettings, ViewerSettingsEditor vse) {
        this.lsd = lensDefSettings;
        this.vse = vse;
        fc = new FluidContainer();
        FluidRow fr = new FluidRow();
        fr.getElement().getStyle().setPaddingLeft(8, Style.Unit.PX);
        {
            Button btn = new Button();
            btn.setIcon(IconType.PLUS);
            btn.setType(ButtonType.LINK);
            btn.setSize(ButtonSize.MINI);
            btn.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    controlsHidden = !controlsHidden;
                    for (VSEControl control : controls) {
                        control.setVisible(!controlsHidden);
                        btn.setIcon(controlsHidden ? IconType.PLUS : IconType.MINUS);
                    }
                }
            });
            fr.add(btn);
        }
        fr.getElement().getStyle().setBackgroundColor("#EEE");
        {
            fr.add(new InlineLabel(lensDefSettings.getName()));
        }
        {
            ButtonGroup buttons = new ButtonGroup();
            buttons.getElement().getStyle().setFloat(Style.Float.RIGHT);
            fr.add(buttons);
            {
                Button btn = new Button();
                btn.setIcon(IconType.ARROW_UP);
                btn.setType(ButtonType.LINK);
                btn.setSize(ButtonSize.MINI);
                btn.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        controlsHidden = !controlsHidden;
                        for (VSEControl control : controls) {
                            control.setVisible(!controlsHidden);
                            btn.setIcon(controlsHidden ? IconType.PLUS : IconType.MINUS);
                        }
                    }
                });
                buttons.add(btn);
            }
            {
                Button btn = new Button();
                btn.setIcon(IconType.ARROW_DOWN);
                btn.setType(ButtonType.LINK);
                btn.setSize(ButtonSize.MINI);
                btn.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        controlsHidden = !controlsHidden;
                        for (VSEControl control : controls) {
                            control.setVisible(!controlsHidden);
                            btn.setIcon(controlsHidden ? IconType.PLUS : IconType.MINUS);
                        }
                    }
                });
                buttons.add(btn);
            }
            {
                Button btn = new Button();
                btn.setIcon(IconType.REMOVE);
                btn.setType(ButtonType.LINK);
                btn.setSize(ButtonSize.MINI);
                btn.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        vse.removeLens(lsd);
                        removeFromParent();
                    }
                });
                buttons.add(btn);
            }
        }
        fc.add(fr);
        initWidget(fc);
        controls = Lists.newArrayList();
    }


    public void add(VSEControl control) {
        controls.add(control);
        control.setVisible(false);
        fc.add(control);
    }
}
