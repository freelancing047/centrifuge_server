package csi.client.gwt.widget.boot;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

import csi.client.gwt.widget.ui.FullSizeLayoutPanel;

/**
 * Created by centrifuge on 11/30/2017.
 */
public class TitleBar extends FullSizeLayoutPanel {

    private static TitleBar _instance;

    private VerticalPanel vPanel;
    private HorizontalPanel hPanel;
    private Label label;

    private int _limit = 0;
    private String _tail = " . . .";

    private TitleBar(int limitIn, String tailIn) {

        super();

        _limit = limitIn;
        if (null != tailIn) {

            _tail = tailIn;
        }

        vPanel = new VerticalPanel();
        hPanel = new HorizontalPanel();
        label = new Label();
        label.getElement().getStyle().setBackgroundColor("#2677a8");
        label.getElement().getStyle().setColor("#ffffff");
        label.getElement().getStyle().setFontSize(20, Style.Unit.PX);
//        label.getElement().getStyle().setFontWeight(Style.FontWeight.BOLD);
        label.setWordWrap(false);

        vPanel.setWidth("100%");
        vPanel.setHeight("100%");
        hPanel.setWidth("100%");
        hPanel.setHeight("100%");
        hPanel.getElement().getStyle().setBackgroundColor("#2677a8");
        hPanel.getElement().getStyle().setBorderWidth(1, Style.Unit.PX);
        hPanel.getElement().getStyle().setBorderStyle(Style.BorderStyle.SOLID);
        this.add(vPanel);
        this.setWidgetLeftRight(vPanel, 0, Style.Unit.PX, 0, Style.Unit.PX);
        this.setWidgetTopBottom(vPanel, 0, Style.Unit.PX, 0, Style.Unit.PX);
        vPanel.setWidth("100%");
        vPanel.setHeight("100%");
        vPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        vPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        vPanel.add(hPanel);
        hPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        hPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        hPanel.add(label);
    }

    public static TitleBar getInstance() {

        if (null == _instance) {
            _instance = new TitleBar(80, " . . .\"");
        }
        return _instance;
    }

    public String getText() {

        if (isVisible()) {

            return label.getTitle();
        }
        return null;
    }

    public void setText(String textIn) {

        String myDisplay = ((0 < _limit) && (textIn.length() > _limit)) ? textIn.substring(0, _limit) + _tail : textIn;

        label.setText(myDisplay);
        label.setTitle(textIn);
    }
}
