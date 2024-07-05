package csi.client.gwt.dataview.fieldlist.editor;

import com.github.gwtbootstrap.client.ui.Column;
import com.github.gwtbootstrap.client.ui.FluidRow;
import com.github.gwtbootstrap.client.ui.TextArea;
import com.google.gwt.dom.client.Style;
import csi.client.gwt.widget.boot.NotificationPopup;

public class UsedVisualizationDialog extends NotificationPopup {
    FluidRow _fullMessageRow = null;

    public UsedVisualizationDialog(String titleIn, String messageTextIn) {

        super();

        initialize(titleIn, messageTextIn, false);
    }



    protected void initialize(String titleIn, String messageTextIn, boolean stripFirstLineIn) {

        String myTitle = (null != titleIn) ? titleIn : i18n.fieldList_fieldEditor_inUseViz_dialogTitle();
        String myMessage = (null != messageTextIn) ? messageTextIn : txtUnknownError;


        Column myColumn = new Column(12);
        TextArea myTextArea = new TextArea();
        _fullMessageRow = new FluidRow();
        myTextArea.getElement().setAttribute("wrap", "on");
        myTextArea.setSize("350px", "100px");
        myTextArea.setText(myMessage);
        myColumn.getElement().getStyle().setTextAlign(Style.TextAlign.LEFT);
        myColumn.add(myTextArea);
        _fullMessageRow.add(myColumn);
        _fullMessageRow.setVisible(true);

        myMessage = "";
        init(myTitle, myMessage, null);

        if (null != _fullMessageRow) {

            add(_fullMessageRow);
        }
    }
}

