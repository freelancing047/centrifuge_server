package csi.client.gwt.widget.boot;

import com.github.gwtbootstrap.client.ui.Icon;
import com.github.gwtbootstrap.client.ui.TextArea;
import com.github.gwtbootstrap.client.ui.constants.IconSize;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Created by centrifuge on 5/25/2016.
 */
public class ScrollingDialog extends Dialog {

    private static final String STRIPED_BACKGROUND = "#E0FFFF";
    private static final String WHITE_BACKGROUND = "#FFFFFF";

    private ScrollPanel scrollPanel;
    private VerticalPanel dataPanel = null;

    private Integer _verticalScroll = null;
    private boolean _doStriping = false;
    private boolean _isDark = false;

    public ScrollingDialog(String titleIn, boolean doStripingIn) {

        super();

        initialize(titleIn, null);
        _doStriping = doStripingIn;
    }

    public ScrollingDialog(String titleIn, String messageTextIn) {

        super();

        initialize(titleIn, messageTextIn);
    }

    public ScrollingDialog(String messageTextIn) {

        super();

        initialize(null, messageTextIn);
    }

    public ScrollingDialog(String titleIn, String messageTextIn, CanBeShownParent parentIn) {

        super(parentIn);

        initialize(titleIn, messageTextIn);
    }

    public ScrollingDialog(String messageTextIn, CanBeShownParent parentIn) {

        super(parentIn);

        initialize(null, messageTextIn);
    }

    public void recordVerticalPosition() {

        _verticalScroll = scrollPanel.getVerticalScrollPosition();
    }

    public void restoreVerticalPosition() {

        if (null != _verticalScroll) {

            scrollPanel.setVerticalScrollPosition(_verticalScroll);
        }
    }

    public void clearPanel() {

        if (null != dataPanel) {

            dataPanel.clear();
        }
    }

    public void addWidget(Widget widgetIn) {

        if (null != dataPanel) {

            VerticalPanel myPanel = new VerticalPanel();

            myPanel.setWidth("400px");
            myPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);

            if (_doStriping) {

                if (_isDark) {

                    _isDark = false;
                    myPanel.getElement().getStyle().setBackgroundColor(STRIPED_BACKGROUND);

                } else {

                    _isDark = true;
                    myPanel.getElement().getStyle().setBackgroundColor(WHITE_BACKGROUND);
                }
            }
            widgetIn.getElement().getStyle().setMarginTop(10, Style.Unit.PX);
            widgetIn.getElement().getStyle().setMarginBottom(10, Style.Unit.PX);
            myPanel.add(widgetIn);
            dataPanel.add(myPanel);
        }
    }

    protected void initialize(String titleIn, String messageTextIn) {

        String myTitle = (null != titleIn) ? titleIn : Dialog.txtInfoTitle;
        Icon myIcon = new Icon(IconType.INFO_SIGN);
        scrollPanel = new ScrollPanel();
        TextArea myTextArea = ((null != messageTextIn) && (0 < messageTextIn.length())) ? new TextArea() : null;
        CsiHeading myHeading = createHeading(myTitle);

        hideTitleCloseButton();
        myIcon.setSize(IconSize.TWO_TIMES);
        myIcon.getElement().getStyle().setColor(txtInfoColor);
        addToHeader(myIcon);

        myHeading.getElement().getStyle().setDisplay(Style.Display.INLINE);
        addToHeader(myHeading);

        if (null != myTextArea) {

            myTextArea.getElement().setAttribute("wrap", "on");
            myTextArea.setSize("400px", "390px");
            myTextArea.setText(messageTextIn);
            myTextArea.getElement().getStyle().setProperty("resize", "none");
            scrollPanel.add(myTextArea);

        } else {

            dataPanel = new VerticalPanel();
            dataPanel.setWidth("400px");
            scrollPanel.add(dataPanel);
        }
        scrollPanel.setHeight("400px");
        add(scrollPanel);

        getActionButton().setVisible(false);
        getCancelButton().setText(txtCloseButton);
        hideOnCancel();
    }
}
