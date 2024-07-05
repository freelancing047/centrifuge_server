package csi.client.gwt.csiwizard.widgets;

        import csi.client.gwt.csiwizard.support.ParameterValidator;
        import csi.server.common.exception.CentrifugeException;


public class FilteredPasswordTextBox extends StringInputWidget {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static final String _txtDefaultPrompt = _constants.connectorParameterPrompt_Password();


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public FilteredPasswordTextBox(String promptIn, ParameterValidator validatorIn, boolean requiredIn) {

        super((null != promptIn) ? promptIn : _txtDefaultPrompt, validatorIn, requiredIn);
    }

    public FilteredPasswordTextBox(String promptIn, boolean requiredIn) {

        super((null != promptIn) ? promptIn : _txtDefaultPrompt, requiredIn);
    }

    public FilteredPasswordTextBox(String promptIn, ParameterValidator validatorIn) {

        super((null != promptIn) ? promptIn : _txtDefaultPrompt, validatorIn);
    }

    public FilteredPasswordTextBox(String promptIn) {

        super((null != promptIn) ? promptIn : _txtDefaultPrompt);
    }

    public String getPassword() throws CentrifugeException {

        return super.getText();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected boolean hideValue() {

        return false;
    }
}
