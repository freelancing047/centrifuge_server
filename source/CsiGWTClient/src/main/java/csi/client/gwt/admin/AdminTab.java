package csi.client.gwt.admin;

import com.github.gwtbootstrap.client.ui.RadioButton;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DropEvent;
import com.google.gwt.event.dom.client.DropHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.TextBox;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.widget.boot.AbstractCsiTab;
import csi.client.gwt.widget.buttons.BlueButton;
import csi.client.gwt.widget.buttons.CyanButton;


public abstract class AdminTab extends AbstractCsiTab {

    public abstract TextBox getSearchTextBox();
    public abstract RadioButton getAllRadioButton();
    public abstract RadioButton getSearchRadioButton();
    public abstract CyanButton getRetrievalButton();
    public abstract BlueButton getNewButton();
    protected abstract void wireInHandlers();

    private static final CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    private static final IconType _iconType[] = new IconType[] {IconType.USER, IconType.GROUP,
                                                                IconType.GROUP, IconType.GROUP, IconType.BAR_CHART};
    private static final String[] _tabLabelChoices = new String[] {_constants.administrationDialogs_individualUsers(),
                                                                    _constants.administrationDialogs_sharingGroups(),
                                                                    _constants.administrationDialogs_securityGroups(),
                                                                    _constants.administrationDialogs_CapcoComponents(),
                                                                    _constants.administrationDialogs_reports()};
    private static final String[] _onlyMatchLabel = new String[] {_constants.userTab_returnUsersMatch(),
                                                                    _constants.groupTab_returnGroupsMatch(),
                                                                    _constants.groupTab_returnRolesMatch(),
                                                                    _constants.capcoTab_returnTokensMatch(),
                                                                    _constants.reportsTab_returnTokensMatch()};
    private static final String[] _returnAllLabel = new String[] {_constants.userTab_returnAllUsers(),
                                                                    _constants.groupTab_returnAllGroups(),
                                                                    _constants.groupTab_returnAllRoles(),
                                                                    _constants.capcoTab_returnAllTokens(),
                                                                    _constants.reportsTab_returnAllTokens()};
    private static final String[] _newButtonText = new String[] {_constants.userTab_newUser(),
                                                                    _constants.groupTab_newGroup(),
                                                                    _constants.groupTab_newRole(),
                                                                    _constants.capcoTab_newToken(),
                                                                    _constants.reportsTab_newToken()};

    private boolean _enableSearch = false;
    private String _searchString = null;
    private int _tabIndex;
    private IconType _tabIcon;
    private String _tabLabel;



    protected ClickHandler radioClickHandler = new ClickHandler() {
        public void onClick(ClickEvent eventIn) {
            searchEnableDisable();
        }
    };

    protected KeyUpHandler keyboardHandler = new KeyUpHandler() {
        public void onKeyUp(KeyUpEvent eventIn) {
            getEnableDisable();
        }
    };

    protected DropHandler dropHandler = new DropHandler() {
        public void onDrop(DropEvent eventIn) {
            getRetrievalButton().setEnabled(true);
        }
    };

    

    public AdminTab(int tabIndexIn) {
        
        super();

        _tabIndex = tabIndexIn;
        _tabIcon = _iconType[_tabIndex];
        _tabLabel = _tabLabelChoices[_tabIndex];
    }
    
    public String getSearchString() {
        
        _searchString = null;
        
        if (getSearchTextBox().isEnabled()) {
            
            _searchString = getSearchTextBox().getText();
        }
        return _searchString;
    }
    
    public String getRefreshString() {

        return _searchString;
    }

    public void disableControls() {

        getSearchTextBox().setEnabled(false);
        getAllRadioButton().setEnabled(false);
        getSearchRadioButton().setEnabled(false);
        getRetrievalButton().setEnabled(false);
        getNewButton().setEnabled(false);
    }
    
    public void enableControls() {

        getSearchTextBox().setEnabled(_enableSearch);
        getAllRadioButton().setEnabled(true);
        getSearchRadioButton().setEnabled(true);
        getNewButton().setEnabled(true);
        getEnableDisable();
    }

    protected void initialize() {

        getSearchRadioButton().setText(_onlyMatchLabel[_tabIndex]);
        getAllRadioButton().setText(_returnAllLabel[_tabIndex]);
        getNewButton().setText(_newButtonText[_tabIndex]);

        wireInHandlers();
    }
    
    private void searchEnableDisable() {

        _enableSearch = getSearchRadioButton().getValue();
        enableControls();
        if (_enableSearch) {
            getSearchTextBox().setFocus(true);
        }
        getEnableDisable();
    }
    
    private void getEnableDisable() {
        boolean myActive = false;

        if ((!_enableSearch) || (0 < getSearchTextBox().getValue().length())) {
            myActive = true;
        }
        getRetrievalButton().setEnabled(myActive);
    }

    @Override
    public void onShow() {

        if (_enableSearch) {
            
            getSearchRadioButton().setValue(true,  true);
            
        } else {
            
            getAllRadioButton().setValue(true, true);
        }
        searchEnableDisable();
    }

    @Override
    public void onHide() {

        searchEnableDisable();
    }

    @Override
    public String getHeadingText() {

        return _tabLabel;
    }

    @Override
    public IconType getIconType() {

        return _tabIcon;
    }
}
