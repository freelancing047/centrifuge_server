package csi.client.gwt.widget.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;

import csi.client.gwt.vortex.Callback;
import csi.client.gwt.widget.boot.CsiModal;
import csi.client.gwt.widget.boot.HelpWindow;


public class DialogHeader extends Composite {

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                  Embedded Interfaces                                   //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    interface DialogHeaderUiBinder extends UiBinder<Widget, DialogHeader> {
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                             GUI Objects from the XML File                              //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    @UiField
    InlineLabel dialogTitle;
    @UiField
    InlineLabel helpButton;
    @UiField
    InlineLabel closeButton;

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static boolean _helpSupported = false;
    private static DialogHeaderUiBinder uiBinder = GWT.create(DialogHeaderUiBinder.class);

    private CsiModal _parent;
    private String _helpTarget = ""; //$NON-NLS-1$
    private Callback<CsiModal> _helpCallback = null;

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private ClickHandler handleHelpButtonClick = new ClickHandler() {
        @Override
        public void onClick(ClickEvent eventIn) {
            
            if (null != _helpCallback) {
                
                _helpCallback.onSuccess(_parent);
                
            } else {
                
                HelpWindow.display(_helpTarget);
            }
        }
    };

    //
    // This handler is to be provided by the parent modal display
    //
    private ClickHandler _closeClickHandler = null;

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public DialogHeader(CsiModal parentIn, String titleIn, Callback<CsiModal> helpCallbackIn, ClickHandler closeHandlerIn) {
        initialize(parentIn, titleIn, null, helpCallbackIn, closeHandlerIn, 7);
    }

    public DialogHeader(CsiModal parentIn, String titleIn, String helpTargetIn, ClickHandler closeHandlerIn) {
        initialize(parentIn, titleIn, helpTargetIn, null, closeHandlerIn, 7);
    }

    public DialogHeader(CsiModal parentIn, String titleIn, Callback<CsiModal> helpCallbackIn, ClickHandler closeHandlerIn, int sizeIn) {
        initialize(parentIn, titleIn, null, helpCallbackIn, closeHandlerIn, sizeIn);
    }

    public DialogHeader(CsiModal parentIn, String titleIn, String helpTargetIn, ClickHandler closeHandlerIn, int sizeIn) {
        initialize(parentIn, titleIn, helpTargetIn, null, closeHandlerIn, sizeIn);
    }

    public String getDialogTitle() {
        return dialogTitle.getText();
    }

    public String getHelpTarget() {
        return _helpTarget;
    }

    public Callback<CsiModal> getHelpCallback() {
        return _helpCallback;
    }
    
    private void initialize(CsiModal parentIn, String titleIn, String helpTargetIn, Callback<CsiModal> helpCallbackIn, ClickHandler closeHandlerIn, int sizeIn) {
        
        _parent = parentIn;
        
        initWidget(uiBinder.createAndBindUi(this));
        
        setDialogTitle(titleIn);
        dialogTitle.getElement().getStyle().setFontSize((7 * sizeIn), Style.Unit.PX);
        setHelpTarget(helpTargetIn, helpCallbackIn);
        setCloseHandler(closeHandlerIn);
        closeButton.setVisible(null != closeHandlerIn);
        
        helpButton.addClickHandler(handleHelpButtonClick);
    }

    private void setDialogTitle(String titleIn) {
        if (null != titleIn) {

            dialogTitle.setText(titleIn);

        } else {

            dialogTitle.setText(""); //$NON-NLS-1$
       }
    }

    private void setHelpTarget(String helpTargetIn, Callback<CsiModal> helpCallbackIn) {
        
        if (null != helpTargetIn) {

            _helpTarget = helpTargetIn;
            _helpCallback = null;
            helpButton.addClickHandler(handleHelpButtonClick);
            helpButton.setVisible(_helpSupported);

            
        } else if (null != helpCallbackIn) {

            _helpCallback = helpCallbackIn;
            _helpTarget = ""; //$NON-NLS-1$
            helpButton.addClickHandler(handleHelpButtonClick);
            helpButton.setVisible(_helpSupported);

        } else {

            _helpTarget = ""; //$NON-NLS-1$
            _helpCallback = null;
            helpButton.setVisible(false);
        }
    }

    private void setCloseHandler(ClickHandler closeClickHandlerIn) {
        
        _closeClickHandler = closeClickHandlerIn;
        
        if (null != _closeClickHandler) {

            closeButton.addClickHandler(_closeClickHandler);
            closeButton.setVisible(true);

        } else {

            closeButton.setVisible(false);
        }
    }
}
