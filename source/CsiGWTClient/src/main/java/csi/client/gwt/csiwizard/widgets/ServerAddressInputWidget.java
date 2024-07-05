package csi.client.gwt.csiwizard.widgets;

import com.google.gwt.user.client.ui.Label;


public class ServerAddressInputWidget extends TextInputWidget {
    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    Label parameterPrompt = null;
    

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    public ServerAddressInputWidget(String promptIn, String defaultIn) {
        
        super(promptIn, null, defaultIn);
        
        //
        // Initialize the display objects
        //
        initializeObject(promptIn, defaultIn);
    }

    @Override
    public String getText() {
        // TODO Auto-generated method stub
        return null;
    }

    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    //
    //
    //
    protected void initializeObject(String promptIn, String defaultIn) {
        
        //
        // Create the widgets which are part of this selection widget
        //
        createWidgets(promptIn, defaultIn);
        
        //
        // Wire in the handlers
        //
        wireInHandlers();
    }
    
    protected void wireInHandlers() {
        
    }
    
    protected void layoutDisplay() {
        
    }
    

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    private void createWidgets(String promptIn, String defaultIn) {

    }
}
