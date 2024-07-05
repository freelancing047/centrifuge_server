package csi.client.gwt.csiwizard.widgets;

import com.github.gwtbootstrap.datetimepicker.client.ui.base.HasViewMode;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.input_boxes.FilteredDateTimeBox;
import csi.server.common.exception.CentrifugeException;

/**
 * Created by centrifuge on 11/14/2016.
 */
public class DateTimeRangeWidget extends AbstractInputWidget {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected FilteredDateTimeBox datePickerOne = null;
    protected FilteredDateTimeBox datePickerTwo = null;
    protected VerticalPanel formatPanelOne = null;
    protected VerticalPanel formatPanelTwo = null;
    protected Label formatOne = null;
    protected Label formatTwo = null;
    protected Label onOrAfterPrompt = null;
    protected Label beforePrompt = null;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected static final CentrifugeConstants i18n = CentrifugeConstantsLocator.get();

    //    protected Date _defaultDate = null;
    protected String _defaultDate = null;

    protected boolean _initialized = false;
    protected boolean _needDate = true;
    protected boolean _needTime = true;
    protected HasViewMode.ViewMode _startView = null;
    protected HasViewMode.ViewMode _minView = null;
    protected HasViewMode.ViewMode _maxView = null;

    private boolean _monitoring = false;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public DateTimeRangeWidget() throws CentrifugeException {

        this(true, true);
    }

    public DateTimeRangeWidget(boolean needDateIn, boolean needTimeIn) throws CentrifugeException {

        super();

        try {

            _needDate = needDateIn;
            _needTime = needTimeIn;

            //
            // Initialize the display objects
            //
            if (_needDate) {

                if (_needTime) {

                    _startView = null;
                    _minView = null;
                    _maxView = null;
                    initializeObject("Select date and time");

                } else {

                    _startView = HasViewMode.ViewMode.MONTH;
                    _minView = HasViewMode.ViewMode.MONTH;
                    _maxView = null;
                    initializeObject("Select date");
                }

            } else {

                if (_needTime) {

                    _startView = HasViewMode.ViewMode.DAY;
                    _minView = null;
                    _maxView = HasViewMode.ViewMode.DAY;
                    initializeObject("Select time");

                } else {

                    initializeObject("? ? ?");
                }
            }

        } catch (Exception myException) {

            Dialog.showException("DateTimeRangeWidget", myException);
        }
    }

    @Override
    public String getText() throws CentrifugeException {

        return null;
    }

    @Override
    public void resetValue() {

    }

    @Override
    public boolean isValid() {
        return false;
    }

    @Override
    public boolean atReset() {
        return false;
    }

    @Override
    public void grabFocus() {

    }

    @Override
    public int getRequiredHeight() {
        return 70;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    //
    //
    //
    protected void initializeObject(String promptIn) {

        //
        // Create the widgets which are part of this selection widget
        //
        createWidgets();

        _initialized = true;
    }

    @Override
    protected void layoutDisplay() {

        int myWidth = getWidth();
        int myHalfWidth = (myWidth - Dialog.intMargin) / 2;
        int myInputTop = Dialog.intLabelHeight;
        int myInputWidth = myHalfWidth - (Dialog.intMargin * 2);
        int myLabelWidth = (myHalfWidth * 3) / 5;
        int myFormatWidth = (myHalfWidth * 2) / 5;

        if (null != formatPanelOne) {
            formatPanelOne.setPixelSize(myFormatWidth, Dialog.intLabelHeight);
            setWidgetTopHeight(formatPanelOne, 0, Style.Unit.PX, Dialog.intLabelHeight, Style.Unit.PX);
            setWidgetLeftWidth(formatPanelOne, myLabelWidth - Dialog.intMargin, Style.Unit.PX, myFormatWidth, Style.Unit.PX);
        }
        if (null != formatPanelTwo) {
            formatPanelTwo.setPixelSize(myFormatWidth, Dialog.intLabelHeight);
            setWidgetTopHeight(formatPanelTwo, 0, Style.Unit.PX, Dialog.intLabelHeight, Style.Unit.PX);
            setWidgetRightWidth(formatPanelTwo, Dialog.intMargin, Style.Unit.PX, myFormatWidth, Style.Unit.PX);
        }

        if (null != onOrAfterPrompt) {
            setWidgetTopHeight(onOrAfterPrompt, 0, Style.Unit.PX, Dialog.intLabelHeight, Style.Unit.PX);
            setWidgetLeftWidth(onOrAfterPrompt, 0, Style.Unit.PX, myLabelWidth, Style.Unit.PX);
        }
        if (null != beforePrompt) {
            setWidgetTopHeight(beforePrompt, 0, Style.Unit.PX, Dialog.intLabelHeight, Style.Unit.PX);
            setWidgetLeftWidth(beforePrompt, myHalfWidth + Dialog.intMargin, Style.Unit.PX, myLabelWidth, Style.Unit.PX);
        }

        if (null != datePickerOne) {
            datePickerOne.setBoxWidth(Integer.toString(myInputWidth) + "px");
            setWidgetTopHeight(datePickerOne, myInputTop, Style.Unit.PX, Dialog.intTextBoxHeight, Style.Unit.PX);
            setWidgetLeftWidth(datePickerOne, 0, Style.Unit.PX, myHalfWidth, Style.Unit.PX);
        }
        if (null != datePickerTwo) {
            datePickerTwo.setBoxWidth(Integer.toString(myInputWidth) + "px");
            setWidgetTopHeight(datePickerTwo, myInputTop, Style.Unit.PX, Dialog.intTextBoxHeight, Style.Unit.PX);
            setWidgetRightWidth(datePickerTwo, 0, Style.Unit.PX, myHalfWidth, Style.Unit.PX);
        }
    }

    protected HasViewMode.ViewMode getStartView() {
        return _startView;
    }

    protected HasViewMode.ViewMode getMinimumView() {
        return _minView;
    }

    protected HasViewMode.ViewMode getMaximumView() {
        return _maxView;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private void createWidgets() {

        try {

            HasViewMode.ViewMode myStartView = getStartView();
            HasViewMode.ViewMode myMinView = getMinimumView();
            HasViewMode.ViewMode myMaxView = getMaximumView();

            clear();

            datePickerOne = new FilteredDateTimeBox(_needDate, _needTime, _required);
            datePickerOne.setAutoClose(true);
            datePickerTwo = new FilteredDateTimeBox(_needDate, _needTime, _required);
            datePickerTwo.setAutoClose(true);
            //datePicker.setReadOnly(true);

            if (null != myStartView) {

                datePickerOne.setStartView(myStartView);
                datePickerTwo.setStartView(myStartView);
            }

            if (null != myMinView) {

                datePickerOne.setMinView(myMinView);
                datePickerTwo.setMinView(myMinView);
            }

            if (null != myMaxView) {

                datePickerOne.setMaxView(myMaxView);
                datePickerTwo.setMaxView(myMaxView);
            }

            datePickerOne.setText(null);
            add(datePickerOne);
            datePickerTwo.setText(null);
            add(datePickerTwo);

            onOrAfterPrompt = new Label(_constants.simpleOnOrAfter());
            add(onOrAfterPrompt);
            beforePrompt = new Label(_constants.simpleBefore());
            add(beforePrompt);

            formatPanelOne = new VerticalPanel();
            formatPanelOne.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
            formatOne = new Label();
            formatOne.asWidget().getElement().getStyle().setColor(Dialog.txtPatternColor);
            formatOne.setText(datePickerOne.getFormat());
            formatPanelOne.add(formatOne);
            add(formatPanelOne);
            formatPanelTwo = new VerticalPanel();
            formatPanelTwo.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
            formatTwo = new Label();
            formatTwo.asWidget().getElement().getStyle().setColor(Dialog.txtPatternColor);
            formatTwo.setText(datePickerOne.getFormat());
            formatPanelTwo.add(formatTwo);
            add(formatPanelTwo);

        } catch (Exception myException) {

            Dialog.showException(myException);
        }
    }

    private void checkValidity() {

        if ((null != datePickerOne) && (null != datePickerTwo)) {

            reportValidity(datePickerOne.isValid() & datePickerTwo.isValid(),
                            datePickerOne.atReset() & datePickerTwo.atReset());
        }
        if (_monitoring ) {

            DeferredCommand.add(new Command() {
                public void execute() {
                    checkValidity();
                }
            });
        }
    }
}
