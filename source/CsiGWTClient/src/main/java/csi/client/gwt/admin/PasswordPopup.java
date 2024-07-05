package csi.client.gwt.admin;

import com.github.gwtbootstrap.client.ui.Label;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;

import csi.client.gwt.events.DataChangeEvent;
import csi.client.gwt.events.DataChangeEventHandler;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.input_boxes.FilteredPasswordTextBox;

public class PasswordPopup implements HasHandlers {

	private CentrifugeConstants _constants = CentrifugeConstantsLocator.get();
	
    private HandlerManager _handlerManager;
    
    private Dialog dialog;
    private boolean _monitoring = false;

    interface SpecificUiBinder extends UiBinder<Dialog, PasswordPopup> {
    }
 
    private static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);

    private static final int _minPasswordSize = 8;
    
    interface PasswordPopupStyle extends CssResource {
        String alertText();
        String congratulatoryText();
      }
    
    @UiField
    PasswordPopupStyle style;
    @UiField
    FilteredPasswordTextBox passwordTextBox;
    @UiField
    FilteredPasswordTextBox repeatTextBox;
    @UiField
    Label messageLabel;
 
    public PasswordPopup(String titleIn, String passwordIn) {
        
        _handlerManager = new HandlerManager(this);

        dialog = uiBinder.createAndBindUi(this);
        
        if (null != titleIn) {
            dialog.setTitle(titleIn);
        }
        
        if (null != passwordIn) {
            passwordTextBox.setText(passwordIn);
        }
        
        dialog.getCancelButton().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                suspendMonitoring();
                dialog.hide();
            }
        });
        
        dialog.getActionButton().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                suspendMonitoring();
                fireEvent(new DataChangeEvent(passwordTextBox.getPassword()));
                dialog.hide();
            }
        });
        
        passwordTextBox.addKeyUpHandler(new KeyUpHandler() {
            public void onKeyUp(KeyUpEvent eventIn) {
                dialog.getActionButton().setEnabled(passwordIsOK());
            }
        });
        
        repeatTextBox.addKeyUpHandler(new KeyUpHandler() {
            public void onKeyUp(KeyUpEvent eventIn) {
                dialog.getActionButton().setEnabled(passwordIsOK());
            }
        });
    }

    public void show() {
        beginMonitoring();
        dialog.show(60);
        
        DeferredCommand.add(new Command() {
            public void execute() {
                repeatTextBox.setFocus(true);
            }
        });
    }

    @Override
    public void fireEvent(GwtEvent<?> eventIn) {
        _handlerManager.fireEvent(eventIn);
    }

    public HandlerRegistration addDataChangeEventHandler(
            DataChangeEventHandler handler) {
        return _handlerManager.addHandler(DataChangeEvent.type, handler);
    }
    
    private boolean passwordIsOK() {
        
        boolean mySuccess = false;
        String myPassword_1 = passwordTextBox.getPassword();
        String myPassword_2 = repeatTextBox.getPassword();
        
        if ((null != myPassword_1) && (_minPasswordSize <= myPassword_1.length())) {
            
            if ((null != myPassword_2) && myPassword_1.equals(myPassword_2)) {
                
                messageLabel.setText(_constants.passwordPopup_passwordMatch());
                messageLabel.removeStyleName(style.alertText());
                messageLabel.addStyleName(style.congratulatoryText());
                mySuccess = true;
            }
            else {
                
                messageLabel.setText(_constants.passwordPopup_passwordDontMatch());
                messageLabel.removeStyleName(style.congratulatoryText());
                messageLabel.addStyleName(style.alertText());
            }
        }
        else {
            
            messageLabel.setText(_constants.passwordPopup_passwordLengthReq());
            messageLabel.removeStyleName(style.congratulatoryText());
            messageLabel.addStyleName(style.alertText());
        }
        return mySuccess;
    }

    private void suspendMonitoring() {

        _monitoring = false;
    }
    
    private void beginMonitoring() {
        
        if (! _monitoring) {
            
            _monitoring = true;
            checkValidity();
        }
    }

    private void checkValidity() {

        dialog.getActionButton().setEnabled(passwordIsOK());

        if (_monitoring ) {
            
            DeferredCommand.add(new Command() {
                public void execute() {
                    checkValidity();
                }
            });
        }
    }
}
