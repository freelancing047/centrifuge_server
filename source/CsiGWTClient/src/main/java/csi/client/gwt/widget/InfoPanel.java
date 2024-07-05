package csi.client.gwt.widget;

import com.github.gwtbootstrap.client.ui.Icon;
import com.github.gwtbootstrap.client.ui.constants.IconSize;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.sencha.gxt.widget.core.client.ContentPanel;

import csi.client.gwt.widget.ui.FullSizeLayoutPanel;
import csi.client.gwt.widget.ui.ResizeableAbsolutePanel;

public class InfoPanel extends ContentPanel{
    private FullSizeLayoutPanel internalContainer;
    public static IconType DEFAULT_ICON_TYPE = IconType.INFO_SIGN;
    public static String MESSAGE = "No Results Found";


    public InfoPanel(String message, IconType iconType) {
        this.MESSAGE = message;
        this.DEFAULT_ICON_TYPE = iconType;

        setupPanel();
        buildContainer();
    }

    public InfoPanel(String message) {
        this.MESSAGE = message;
        setupPanel();
        buildContainer();
    }


    public void allowDrillUpButton(){

    }


    public IconType getDEFAULT_ICON_TYPE() {
        return DEFAULT_ICON_TYPE;
    }

    public void setDEFAULT_ICON_TYPE(IconType DEFAULT_ICON_TYPE) {
        this.DEFAULT_ICON_TYPE = DEFAULT_ICON_TYPE;


        this.clear();
        buildContainer();
    }

    public String getMESSAGE() {
        return MESSAGE;
    }

    public void setMessage(String MESSAGE) {
        this.MESSAGE = MESSAGE;

        this.clear();
        buildContainer();
    }

    public InfoPanel() {
        setupPanel();
        buildContainer();

    }

    private void setupPanel() {
        this.getAppearance().getBodyWrap(this.getElement()).getStyle().setBorderColor("white");
        this.setHeaderVisible(false);

        this.setHeight("100%");
        this.setWidth("100%");
    }

    private void buildContainer() {
        Icon icon = new Icon(DEFAULT_ICON_TYPE);
        icon.setIconSize(IconSize.FOUR_TIMES);
        SimplePanel iconPanel = new SimplePanel();
        iconPanel.add(icon);
        iconPanel.setHeight(50+"px");
        iconPanel.setWidth(50+"px");
        iconPanel.getElement().getStyle().setProperty("margin","auto");

        HTMLPanel panel = new HTMLPanel(MESSAGE);
        panel.getElement().getStyle().setFontSize(15, Style.Unit.PX);
        panel.getElement().getStyle().setWhiteSpace(Style.WhiteSpace.NOWRAP);
        panel.getElement().getStyle().setTextAlign(Style.TextAlign.CENTER);

        Panel p = new ResizeableAbsolutePanel();
        p.getElement().getStyle().setColor("#216893");
        p.getElement().getStyle().setTop(40, Style.Unit.PCT);
        p.add(iconPanel);
        p.add(panel);
        this.add(p);
//        this.add(panel);
//        this.add(internalContainer);
    }
}
