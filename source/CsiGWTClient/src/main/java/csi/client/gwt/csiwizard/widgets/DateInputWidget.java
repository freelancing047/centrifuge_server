package csi.client.gwt.csiwizard.widgets;

import com.github.gwtbootstrap.datetimepicker.client.ui.base.HasViewMode;

import csi.server.common.exception.CentrifugeException;


public class DateInputWidget extends DateTimeInputWidget {

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public DateInputWidget(String promptIn, String defaultIn, boolean requiredIn) throws CentrifugeException {

        super(promptIn, defaultIn, true, false, requiredIn);
    }

    public DateInputWidget() throws CentrifugeException {

        super(true, false);
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    @Override
    protected HasViewMode.ViewMode getStartView() {
        return HasViewMode.ViewMode.MONTH;
    }
    
    @Override
    protected HasViewMode.ViewMode getMinimumView() {
        return HasViewMode.ViewMode.MONTH;
    }
}
