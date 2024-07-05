package csi.client.gwt.util.credentials;

import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.ui.Label;

import csi.client.gwt.events.UserInputEvent;
import csi.client.gwt.events.UserInputEventHandler;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.widget.boot.CanBeShownParent;
import csi.client.gwt.widget.boot.Dialog;
import csi.server.common.dto.AuthDO;
import csi.server.common.model.DataSourceDef;
import csi.server.common.util.AuthorizationObject;
import csi.server.common.util.ConnectorSupport;


public class CredentialBuilder extends Dialog {

    private static CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    private Map<String, AuthDO> _authorizationMap;
    private List<DataSourceDef> _requiredList;
    private UserInputEventHandler<Integer> _processLogon;
    private int _offset = 0;
    
    private UserInputEventHandler<Integer> processLogon
    = new UserInputEventHandler<Integer>() {

        @Override
        public void onUserInput(UserInputEvent<Integer> eventIn) {
            
            if (eventIn.isCanceled()) {
                
                _processLogon.onUserInput(new UserInputEvent<Integer>(true));
                destroy();
                
            } else {
                
                _offset++;

                getNextCredentialSet();
            }
        }
    };

    public CredentialBuilder(Map<String, AuthDO> authorizationMapIn,
                             List<DataSourceDef> requiredListIn,
                             UserInputEventHandler<Integer> processLogonIn) {

        _authorizationMap = authorizationMapIn;
        _requiredList = requiredListIn;
        _processLogon = processLogonIn;

        add(new Label(i18n.credentialBuilderMessage())); //$NON-NLS-1$

        getActionButton().setVisible(false);
        getCancelButton().setVisible(false);

        super.show();
    }

    public CredentialBuilder(Map<String, AuthDO> authorizationMapIn,
                             List<DataSourceDef> requiredListIn,
                             UserInputEventHandler<Integer> processLogonIn, CanBeShownParent parentIn) {

        super(parentIn);

        _authorizationMap = authorizationMapIn;
        _requiredList = requiredListIn;
        _processLogon = processLogonIn;

        add(new Label(i18n.credentialBuilderMessage())); //$NON-NLS-1$

        getActionButton().setVisible(false);
        getCancelButton().setVisible(false);

        super.show();
    }

    @Override
    public void show() {

        if (areAnyRestricted()) {

            Dialog.showError(_constants.restrictedCredentialsTitle(), _constants.restrictedCredentialsInfo());
            destroy();

        } else {

            getNextCredentialSet();
        }
    }

    private boolean areAnyRestricted() {

        if (null != _requiredList) {

            for (DataSourceDef mySource : _requiredList) {

                if (ConnectorSupport.getInstance().isRestricted(mySource)) {

                    return true;
                }
            }
            return false;
        }
        return true;
    }

    private void getNextCredentialSet() {

        if (_requiredList.size() > _offset) {
            
            DataSourceDef mySource = _requiredList.get(_offset);

            showLogon(new AuthorizationObject(_authorizationMap, mySource), processLogon);

        } else {
            
            _processLogon.onUserInput(new UserInputEvent<Integer>(_offset, false));
            destroy();
        }
    }
}
