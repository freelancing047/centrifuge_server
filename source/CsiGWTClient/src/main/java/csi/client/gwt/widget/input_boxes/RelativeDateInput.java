package csi.client.gwt.widget.input_boxes;

import com.github.gwtbootstrap.client.ui.base.InlineLabel;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;

import csi.client.gwt.csiwizard.widgets.AbstractInputWidget;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.widget.boot.Dialog;
import csi.server.common.exception.CentrifugeException;

/**
 * Created by centrifuge on 11/18/2016.
 */
public class RelativeDateInput extends AbstractInputWidget {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected HorizontalPanel basePanel = null;
    protected InlineLabel baseValueLabel = null;
    protected InlineLabel daysLabel = null;
    protected FilteredIntegerInput dayCount = null;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static final CentrifugeConstants _constants = CentrifugeConstantsLocator.get();
    private static final String todayLabel = _constants.resourceFilter_FilterDialog_TodayLabel();
    private static final String tomorrowLabel = _constants.resourceFilter_FilterDialog_TomorrowLabel();
    private static final String timeUnit = _constants.resourceFilter_FilterDialog_TimeUnit();

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public RelativeDateInput(boolean futureLimitFlag) {

        basePanel = new HorizontalPanel();
        basePanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        basePanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
        if (futureLimitFlag) {

            baseValueLabel = new InlineLabel(tomorrowLabel);

        } else {

            baseValueLabel = new InlineLabel(todayLabel);
        }
        baseValueLabel.getElement().getStyle().setPaddingRight(5, Style.Unit.PX);
        basePanel.add(baseValueLabel);
        dayCount = new FilteredIntegerInput();
        dayCount.setWidth("30px");
        basePanel.add(dayCount);
        daysLabel = new InlineLabel(timeUnit);
        daysLabel.getElement().getStyle().setPaddingLeft(5, Style.Unit.PX);
        basePanel.add(daysLabel);
        add(basePanel);
        layoutDisplay();
    }

    public void setValue(Integer valueIn) {

        dayCount.setValue(valueIn);
    }

    public Integer getValue() {

        return dayCount.getNumericValue();
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
        return Dialog.intTextBoxHeight;
    }

    @Override
    protected void layoutDisplay() {
/*
        int myWidth = getWidth();

        basePanel.setWidth(Integer.toString(myWidth) + "px");
        setWidgetTopHeight(basePanel, 0, Style.Unit.PX, Dialog.intTextBoxHeight, Style.Unit.PX);
        setWidgetRightWidth(basePanel, 0, Style.Unit.PX, myWidth, Style.Unit.PX);
*/
    }
}
