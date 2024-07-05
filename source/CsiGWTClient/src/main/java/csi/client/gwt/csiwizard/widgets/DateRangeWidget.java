package csi.client.gwt.csiwizard.widgets;

import java.util.Date;

import com.github.gwtbootstrap.client.ui.RadioButton;
import com.github.gwtbootstrap.datetimepicker.client.ui.base.HasViewMode;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.util.Display;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.input_boxes.FilteredDateTimeBox;
import csi.client.gwt.widget.input_boxes.RelativeDateInput;
import csi.server.common.exception.CentrifugeException;

/**
 * Created by centrifuge on 11/18/2016.
 */
public class DateRangeWidget extends AbstractInputWidget {


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

    protected RelativeDateInput dateCalculatorOne = null;
    protected RelativeDateInput dateCalculatorTwo = null;
    protected Label onOrAfterCB = null;
    protected Label beforeCB = null;
    protected HorizontalPanel modePanel = null;
    protected Label dateTypeLabel = null;
    protected RadioButton absoluteRB = null;
    protected RadioButton relativeRB = null;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////


    //    protected Date _defaultDate = null;
    protected String _defaultDate = null;
    private static CentrifugeConstants _constants = CentrifugeConstantsLocator.get();
    private static String dateTypeLabelText = _constants.resourceFilter_FilterDialog_DateValuesAre();
    private static String absoluteRBText = _constants.resourceFilter_FilterDialog_Absolute();
    private static String relativeRBText = _constants.resourceFilter_FilterDialog_Relative();

    protected boolean _initialized = false;
    protected HasViewMode.ViewMode _startView = null;
    protected HasViewMode.ViewMode _minView = null;
    protected HasViewMode.ViewMode _maxView = null;
    protected boolean _absoluteFlag;
    protected int _inputTop = Dialog.intLabelHeight;
    protected int _inputWidth = (getWidth() - Dialog.intMargin) / 2;

    private boolean _monitoring = false;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public ClickHandler handleAbsoluteClick = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {

            try {

                if (!_absoluteFlag) {

                    activateAbsolute();
                }

            } catch (Exception myException) {

                Display.error("DateRangeWidget", myException);
            }
        }
    };

    public ClickHandler handleRelativeClick = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {

            try {

                if (_absoluteFlag) {

                    activateRelative();
                }

            } catch (Exception myException) {

                Display.error("DateRangeWidget", myException);
            }
        }
    };


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public DateRangeWidget(boolean absoluteFlagIn, Date leftAbsoluteValueIn, Date rightAbsoluteValueIn,
                           Integer leftDeltaIn, Integer rightDeltaIn) throws CentrifugeException {

        this(absoluteFlagIn, leftAbsoluteValueIn, rightAbsoluteValueIn, leftDeltaIn, rightDeltaIn, "mode");
    }

    public DateRangeWidget(boolean absoluteFlagIn, Date leftAbsoluteValueIn, Date rightAbsoluteValueIn,
                           Integer leftDeltaIn, Integer rightDeltaIn, String radioGroupIn)
            throws CentrifugeException {

        super();

        try {

            //
            // Initialize the display objects
            //
            _startView = HasViewMode.ViewMode.MONTH;
            _minView = HasViewMode.ViewMode.MONTH;
            _maxView = null;
            initializeObject(radioGroupIn, "Select date");
            wireInHandlers();
            setAbsolute(absoluteFlagIn);
            setLeftAbsolute(leftAbsoluteValueIn);
            setRightAbsolute(rightAbsoluteValueIn);
            setLeftDelta(leftDeltaIn);
            setRightDelta(rightDeltaIn);

        } catch (Exception myException) {

            Display.error("DateRangeWidget", 1, myException);
        }
    }

    public void setAbsolute(boolean absoluteIn) {

        try {

            absoluteRB.setValue(absoluteIn);

        } catch (Exception myException) {

            Display.error("DateRangeWidget", 2, myException);
        }
    }

    public boolean isAbsolute() {

        try {

            return absoluteRB.getValue();

        } catch (Exception myException) {

            Display.error("DateRangeWidget", 3, myException);
        }
        return false;
    }

    public void setLeftAbsolute(Date dateIn) {

        try {

            datePickerOne.setValue(dateIn);

        } catch (Exception myException) {

            Display.error("DateRangeWidget", 4, myException);
        }
    }

    public Date getLeftAbsolute() {

        try {

            return datePickerOne.getValue();

        } catch (Exception myException) {

            Display.error("DateRangeWidget", 5, myException);
        }
        return null;
    }

    public void setRightAbsolute(Date dateIn) {

        try {

            datePickerTwo.setValue(dateIn);

        } catch (Exception myException) {

            Display.error("DateRangeWidget", 6, myException);
        }
    }

    public Date getRightAbsolute() {

        try {

            return datePickerTwo.getValue();

        } catch (Exception myException) {

            Display.error("DateRangeWidget", 7, myException);
        }
        return null;
    }

    public void setLeftDelta(Integer valueIn) {

        try {

            dateCalculatorOne.setValue(valueIn);

        } catch (Exception myException) {

            Display.error("DateRangeWidget", 8, myException);
        }
    }

    public Integer getLeftDelta() {

        try {

            return dateCalculatorOne.getValue();

        } catch (Exception myException) {

            Display.error("DateRangeWidget", 9, myException);
        }
        return null;
    }

    public void setRightDelta(Integer valueIn) {

        try {

            dateCalculatorTwo.setValue(valueIn);

        } catch (Exception myException) {

            Display.error("DateRangeWidget", 10, myException);
        }
    }

    public Integer getRightDelta() {

        try {

            return dateCalculatorTwo.getValue();

        } catch (Exception myException) {

            Display.error("DateRangeWidget", 11, myException);
        }
        return null;
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
    protected void initializeObject(String radioGroupIn, String promptIn) {

        //
        // Create the widgets which are part of this selection widget
        //
        createWidgets(radioGroupIn);
    }

    @Override
    protected void layoutDisplay() {

        int myWidth = getWidth();
        int myHalfWidth = (myWidth - Dialog.intMargin) / 2;
        int myInputWidth = myHalfWidth - (Dialog.intMargin * 2);
        int myLabelWidth = (myHalfWidth * 3) / 5;
        int myFormatWidth = (myHalfWidth * 2) / 5;
        int myRadioTop =  _inputTop + Dialog.intTextBoxHeight + 5;

        _inputWidth = myHalfWidth;

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
            setWidgetLeftWidth(beforePrompt, _inputWidth + Dialog.intMargin, Style.Unit.PX, myLabelWidth, Style.Unit.PX);
        }

        if (null != datePickerOne) {
            datePickerOne.setBoxWidth(Integer.toString(myInputWidth) + "px");
            setWidgetTopHeight(datePickerOne, _inputTop, Style.Unit.PX, Dialog.intTextBoxHeight, Style.Unit.PX);
            setWidgetLeftWidth(datePickerOne, 0, Style.Unit.PX, _inputWidth, Style.Unit.PX);
        }
        if (null != datePickerTwo) {
            datePickerTwo.setBoxWidth(Integer.toString(myInputWidth) + "px");
            setWidgetTopHeight(datePickerTwo, _inputTop, Style.Unit.PX, Dialog.intTextBoxHeight, Style.Unit.PX);
            setWidgetRightWidth(datePickerTwo, 0, Style.Unit.PX, _inputWidth, Style.Unit.PX);
        }

        if (null != onOrAfterCB) {
            setWidgetTopHeight(onOrAfterCB, 0, Style.Unit.PX, Dialog.intLabelHeight, Style.Unit.PX);
            setWidgetLeftWidth(onOrAfterCB, 0, Style.Unit.PX, myLabelWidth, Style.Unit.PX);
        }
        if (null != beforeCB) {
            setWidgetTopHeight(beforeCB, 0, Style.Unit.PX, Dialog.intLabelHeight, Style.Unit.PX);
            setWidgetLeftWidth(beforeCB, _inputWidth + Dialog.intMargin, Style.Unit.PX, myLabelWidth, Style.Unit.PX);
        }

        if (null != dateCalculatorOne) {
            dateCalculatorOne.setPixelSize(myInputWidth, dateCalculatorOne.getRequiredHeight());
        }
        if (null != dateCalculatorTwo) {
            dateCalculatorTwo.setPixelSize(myInputWidth, dateCalculatorTwo.getRequiredHeight());
        }

        if (null != modePanel) {
            setWidgetTopHeight(modePanel, myRadioTop, Style.Unit.PX, Dialog.intLabelHeight, Style.Unit.PX);
            setWidgetLeftRight(modePanel, 0, Style.Unit.PX, 0, Style.Unit.PX);
        }

        if (_absoluteFlag) {

            absoluteRB.setValue(true);
            activateAbsolute();

        } else {

            relativeRB.setValue(true);
            activateRelative();
        }
        _initialized = true;
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

    private void createWidgets(String radioGroupIn) {

        try {

            HasViewMode.ViewMode myStartView = getStartView();
            HasViewMode.ViewMode myMinView = getMinimumView();
            HasViewMode.ViewMode myMaxView = getMaximumView();

            clear();

            datePickerOne = new FilteredDateTimeBox(true, false, _required);
            datePickerOne.setAutoClose(true);
            datePickerTwo = new FilteredDateTimeBox(true, false, _required);
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
            datePickerTwo.setText(null);

            onOrAfterPrompt = new Label(_constants.simpleOnOrAfter());
            beforePrompt = new Label(_constants.simpleBefore());

            formatPanelOne = new VerticalPanel();
            formatPanelOne.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
            formatOne = new Label();
            formatOne.asWidget().getElement().getStyle().setColor(Dialog.txtPatternColor);
            formatOne.setText(datePickerOne.getFormat());
            formatPanelOne.add(formatOne);
            formatPanelTwo = new VerticalPanel();
            formatPanelTwo.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
            formatTwo = new Label();
            formatTwo.asWidget().getElement().getStyle().setColor(Dialog.txtPatternColor);
            formatTwo.setText(datePickerOne.getFormat());
            formatPanelTwo.add(formatTwo);

            dateCalculatorOne = new RelativeDateInput(false);
            dateCalculatorTwo = new RelativeDateInput(true);

            onOrAfterCB = new Label(_constants.simpleOnOrAfter());
            beforeCB = new Label(_constants.simpleBefore());

            modePanel = new HorizontalPanel();
            dateTypeLabel = new Label(dateTypeLabelText);
            modePanel.add(dateTypeLabel);
            absoluteRB = new RadioButton(radioGroupIn, absoluteRBText);
            absoluteRB.getElement().getStyle().setPaddingLeft(40, Style.Unit.PX);
            absoluteRB.setValue(_absoluteFlag);
            modePanel.add(absoluteRB);
            relativeRB = new RadioButton(radioGroupIn, relativeRBText);
            relativeRB.getElement().getStyle().setPaddingLeft(40, Style.Unit.PX);
            relativeRB.setValue(!_absoluteFlag);
            modePanel.add(relativeRB);
            add(formatPanelOne);
            add(formatPanelTwo);
            add(onOrAfterCB);
            add(beforeCB);
            add(onOrAfterPrompt);
            add(beforePrompt);
            add(modePanel);
            add(datePickerOne);
            add(datePickerTwo);

        } catch (Exception myException) {

            Display.error(myException);
        }
    }

    private void activateAbsolute() {

        _absoluteFlag = true;

        if (_initialized) {

            remove(dateCalculatorOne);
            remove(dateCalculatorTwo);
        }
        onOrAfterCB.setVisible(false);
        beforeCB.setVisible(false);

        onOrAfterPrompt.setVisible(true);
        beforePrompt.setVisible(true);
        formatOne.setVisible(true);
        formatTwo.setVisible(true);

        datePickerOne.setVisible(true);
        datePickerTwo.setVisible(true);
    }

    private void activateRelative() {

        _absoluteFlag = false;

        if (_initialized) {

            datePickerOne.setVisible(false);
            datePickerTwo.setVisible(false);
        }
        onOrAfterPrompt.setVisible(false);
        beforePrompt.setVisible(false);
        formatOne.setVisible(false);
        formatTwo.setVisible(false);
        datePickerOne.setVisible(false);
        datePickerTwo.setVisible(false);

        onOrAfterCB.setVisible(true);
        beforeCB.setVisible(true);
        dateCalculatorOne.setVisible(true);
        dateCalculatorTwo.setVisible(true);

        add(dateCalculatorOne);
        add(dateCalculatorTwo);
        setWidgetTopHeight(dateCalculatorOne, _inputTop, Style.Unit.PX, Dialog.intTextBoxHeight, Style.Unit.PX);
        setWidgetLeftWidth(dateCalculatorOne, 0, Style.Unit.PX, _inputWidth, Style.Unit.PX);
        setWidgetTopHeight(dateCalculatorTwo, _inputTop, Style.Unit.PX, Dialog.intTextBoxHeight, Style.Unit.PX);
        setWidgetRightWidth(dateCalculatorTwo, 0, Style.Unit.PX, _inputWidth, Style.Unit.PX);
    }

    private void wireInHandlers() {

        absoluteRB.addClickHandler(handleAbsoluteClick);
        relativeRB.addClickHandler(handleRelativeClick);
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
