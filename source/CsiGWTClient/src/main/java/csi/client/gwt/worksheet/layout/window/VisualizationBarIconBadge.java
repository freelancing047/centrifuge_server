package csi.client.gwt.worksheet.layout.window;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.SimplePanel;

public class VisualizationBarIconBadge extends SimplePanel {
//    private Button countBadge = new Button();

    public VisualizationBarIconBadge(String visCount) {
        super(DOM.createSpan());
        this.getElement().setClassName("badge badge-light");
        Style countBadgeStyle = this.getElement().getStyle();
        countBadgeStyle.setBorderStyle(Style.BorderStyle.SOLID);
        countBadgeStyle.setColor("white");

        countBadgeStyle.setBorderWidth(1, Style.Unit.PX);
        countBadgeStyle.setPosition(Style.Position.ABSOLUTE);
        countBadgeStyle.setBorderColor("#206995");
        countBadgeStyle.setBackgroundColor("#206995");

        countBadgeStyle.setProperty("borderRadius", "8px");
        countBadgeStyle.setProperty("padding", "0px 3.65px");
        countBadgeStyle.setCursor(Style.Cursor.DEFAULT);
        countBadgeStyle.setFontSize(11, Style.Unit.PX);
        countBadgeStyle.setRight(5, Style.Unit.PX);

        setCount(visCount);
    }

    public String getCount() {
        return this.getElement().getInnerText();
}

    public void setCount(String count) {
        if (Integer.parseInt(count) > 9) {
            this.getElement().setInnerText(count + "+");
        } else {
            this.getElement().setInnerText(count);
        }
    }
}
