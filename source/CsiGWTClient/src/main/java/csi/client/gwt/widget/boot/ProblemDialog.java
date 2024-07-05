package csi.client.gwt.widget.boot;

import com.github.gwtbootstrap.client.ui.Icon;
import com.github.gwtbootstrap.client.ui.constants.IconSize;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.dom.client.Style.Unit;


public class ProblemDialog extends NotificationPopup {

    public ProblemDialog(String titleIn, String messageTextIn) {

        super();

        initialize(titleIn, messageTextIn);
    }

    public ProblemDialog(String messageTextIn) {

        super();

        initialize(null, messageTextIn);
    }

    public ProblemDialog(String titleIn, String messageTextIn, CanBeShownParent parentIn) {

        super(parentIn);

        initialize(titleIn, messageTextIn);
    }

    public ProblemDialog(String messageTextIn, CanBeShownParent parentIn) {

        super(parentIn);

        initialize(null, messageTextIn);
    }

    protected void initialize(String titleIn, String messageTextIn) {
        
        String myTitle = (null != titleIn) ? titleIn : Dialog.txtProblemTitle;
        Icon myIcon = new Icon(IconType.WARNING_SIGN);
        
        myIcon.setSize(IconSize.TWO_TIMES);
        myIcon.getElement().getStyle().setColor(txtProblemColor);
        myIcon.getElement().getStyle().setPaddingRight(10, Unit.PX);
        
        init(myTitle, messageTextIn, myIcon);
    }
}
