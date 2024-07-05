package csi.client.gwt.csiwizard.widgets;

import com.github.gwtbootstrap.datetimepicker.client.ui.base.HasViewMode;

import csi.server.common.exception.CentrifugeException;


public class TimeInputWidget extends DateTimeInputWidget {

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    public TimeInputWidget(String promptIn, String defaultIn, boolean requiredIn) throws CentrifugeException {

        super(promptIn, defaultIn, false, true, requiredIn);
    }

    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    @Override
    protected HasViewMode.ViewMode getStartView() {
        return HasViewMode.ViewMode.DAY;
    }
    
    @Override
    protected HasViewMode.ViewMode getMaximumView() {
        return HasViewMode.ViewMode.DAY;
    }
}
