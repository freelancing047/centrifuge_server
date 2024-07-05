package csi.client.gwt.csiwizard.widgets;


public class UrlInputWidget extends TextInputWidget {

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    public UrlInputWidget(String promptIn, String defaultIn) {
        
        super(promptIn, null, defaultIn);
    }
    
    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    //
    // Replace this code with the proper validity check
    //
    protected Integrity checkInput(String userInputIn) {
        return super.checkInput(userInputIn);
    }
}
