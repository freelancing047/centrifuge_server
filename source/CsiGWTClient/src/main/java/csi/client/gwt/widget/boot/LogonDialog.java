package csi.client.gwt.widget.boot;

import com.github.gwtbootstrap.client.ui.Icon;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

import csi.client.gwt.events.UserInputEvent;
import csi.client.gwt.events.UserInputEventHandler;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.server.common.util.AuthorizationObject;
import csi.client.gwt.widget.input_boxes.FilteredPasswordTextBox;
import csi.client.gwt.widget.input_boxes.FilteredStringInput;


public class LogonDialog<T> extends ValidatingDialog {

    protected static final CentrifugeConstants i18n = CentrifugeConstantsLocator.get();

    public final String _defaultResourcePrompt = i18n.authorizationObjectDefaultName();

    InlineLabel _usernamePrompt = new InlineLabel(txtUsernamePrompt);
    InlineLabel _passwordPrompt = new InlineLabel(txtPasswordPrompt);
    FilteredStringInput _usernameInput = new FilteredStringInput();
    FilteredPasswordTextBox _passwordInput = new FilteredPasswordTextBox();

    private AuthorizationObject _authorization = null;
    private T _key = null;
    private UserInputEventHandler<T> _processLogon;

    public LogonDialog(AuthorizationObject authorizationIn, UserInputEventHandler<T> processLogonIn) {

        this(authorizationIn, processLogonIn, (T)null);
    }

    public LogonDialog(AuthorizationObject authorizationIn, UserInputEventHandler<T> processLogonIn, T keyIn) {

        _authorization = authorizationIn;
        _processLogon = processLogonIn;
        _key = keyIn;
        initializeDisplay(authorizationIn.getUsername(), authorizationIn.getPassword(), !authorizationIn.isUsernameLocked());
        establishMonitoring();
        wireInHandlers();
    }

    public LogonDialog(AuthorizationObject authorizationIn,
                       UserInputEventHandler<T> processLogonIn, CanBeShownParent parentIn) {

        this(authorizationIn, processLogonIn, null, parentIn);
    }

    public LogonDialog(AuthorizationObject authorizationIn,
                       UserInputEventHandler<T> processLogonIn, T keyIn, CanBeShownParent parentIn) {

        super(parentIn);
        _authorization = authorizationIn;
        _processLogon = processLogonIn;
        _key = keyIn;
        initializeDisplay(authorizationIn.getUsername(), authorizationIn.getPassword(), !authorizationIn.isUsernameLocked());
        establishMonitoring();
        wireInHandlers();
    }

    public void show() {
        
        super.show();
        setFocus();
    }

    private void setFocus() {

        if (_usernameInput.isEnabled()) {
            
            setFocus(_usernameInput);
        
        } else {
            
            setFocus(_passwordInput);
        }
    }

    protected void initializeDisplay(String userIn, String passwordIn, boolean enabledIn) {
        
        String myTitle = txtLogonTitle;
        Icon myIcon = new Icon(IconType.EXCLAMATION_SIGN);
        CsiHeading myHeading = createHeading(myTitle);
        VerticalPanel myBasePanel = new VerticalPanel();
        HorizontalPanel myUsernamePanel = new HorizontalPanel();
        HorizontalPanel myPasswordPanel = new HorizontalPanel();
        Label myLogonPrompt = new Label(txtLogonPrompt);
        String myResourceName = _authorization.getResourceName();
        Label myResourcePrompt = new Label((null != myResourceName) ? myResourceName : _defaultResourcePrompt);
        Label myBlankLine = new Label(".");
        InlineLabel myBlankSpace1 = new InlineLabel(".");
        InlineLabel myBlankSpace2 = new InlineLabel(".");

        myBlankLine.getElement().getStyle().setColor(txtPanelColor);
        myBlankSpace1.getElement().getStyle().setColor(txtPanelColor);
        myBlankSpace2.getElement().getStyle().setColor(txtPanelColor);

        _usernameInput.setText(userIn);
        _usernameInput.setEnabled(enabledIn);

        _passwordInput.setText(passwordIn);
        _passwordInput.setEnabled(true);

        myUsernamePanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        myUsernamePanel.add(_usernamePrompt);
        myUsernamePanel.add(myBlankSpace1);
        myUsernamePanel.add(_usernameInput);

        myPasswordPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        myPasswordPanel.add(_passwordPrompt);
        myPasswordPanel.add(myBlankSpace2);
        myPasswordPanel.add(_passwordInput);

        myBasePanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        myBasePanel.add(myLogonPrompt);
        myBasePanel.add(myResourcePrompt);
        myBasePanel.add(myBlankLine);
        myBasePanel.add(myUsernamePanel);
        myBasePanel.add(myPasswordPanel);
        
        add(myBasePanel);

        myIcon.getElement().getStyle().setFontSize(20, Unit.PX);
        myIcon.getElement().getStyle().setColor(txtDecisionColor);
        myIcon.getElement().getStyle().setPaddingRight(10, Unit.PX);

        myHeading.getElement().getStyle().setDisplay(Display.INLINE);
        
        hideTitleCloseButton();
        addToHeader(myIcon);
        addToHeader(myHeading);
        
        setWidth("450px");
        myBasePanel.setWidth("420px");
        
        getActionButton().setVisible(true);
        getActionButton().setText(txtLogonButton);
        getActionButton().setWidth("60px");
        getCancelButton().setText(txtCancelButton);
        getCancelButton().setWidth("60px");
    }
    
    private void establishMonitoring() {
        

        _usernameInput.setRequired(true);
        addObject(_usernameInput, true);

        _passwordInput.setRequired(true);
        addObject(_passwordInput, true);
    }
    
    private void wireInHandlers() {
        
        getActionButton().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent eventIn) {
                
                if (null != _authorization) {
                    
                    _authorization.updateCredentials(_usernameInput.getText(), _passwordInput.getPassword());
                }
                _processLogon.onUserInput(new UserInputEvent<T>(_key, false));
                hide();
            }
        });
        
        getCancelButton().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent eventIn) {
                
                _processLogon.onUserInput(new UserInputEvent<T>(_key, true));
                hide();
            }
        });
    }
}
