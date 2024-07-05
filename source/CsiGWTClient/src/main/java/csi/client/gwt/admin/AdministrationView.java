package csi.client.gwt.admin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import csi.client.gwt.WebMain;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.mainapp.CsiDisplay;
import csi.client.gwt.mainapp.MainView.Mode;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.CsiTabPanel;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.buttons.SimpleButton;
import csi.server.common.dto.ClientStartupInfo;
import csi.server.common.dto.Response;
import csi.server.common.dto.user.UserSecurityInfo;
import csi.server.common.enumerations.GroupType;
import csi.server.common.service.api.UserAdministrationServiceProtocol;

public class AdministrationView extends Composite implements CsiDisplay {

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                  Embedded Interfaces                                   //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    interface MyUiBinder extends UiBinder<Widget, AdministrationView> {
    }

    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                      GUI Objects                                       //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    @UiField
    SimpleButton exitButton;
    @UiField
    CsiTabPanel tabPanel;

    UserTab userTab;
    GroupTab sharingTab;
    ReportsTab reportsTab;
    GroupTab securityTab;
// TODO:    CapcoTab capcoTab;

    Widget widget = null;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static final CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    private SharedItems _shared;
    
    private UserAdmin _userAdmin;
    private SharingAdmin _sharingAdmin;
    private ReportsAdmin _reportsAdmin;
    private SecurityAdmin _securityAdmin;

    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    ClickHandler handleExitButton = new ClickHandler() {
        public void onClick(ClickEvent eventIn) {
            widget.removeFromParent();
            widget = null;
            WebMain.injector.getEventBus().fireEvent(new csi.client.gwt.events.ExitAdminModeEvent());
        }
    };
    
    ClickHandler handleUserRetrievalButton = new ClickHandler() {
        public void onClick(ClickEvent eventIn) {
            _userAdmin.requestData(userTab.getSearchString());
        }
    };

    ClickHandler handleSharingRetrievalButton = new ClickHandler() {
        public void onClick(ClickEvent eventIn) { _sharingAdmin.requestData(sharingTab.getSearchString()); }
    };

    ClickHandler handleReportsRetrieveButton = new ClickHandler() {
        @Override
        public void onClick(ClickEvent clickEvent) { _reportsAdmin.requestData(); }
    };

    ClickHandler handleSecurityRetrievalButton = new ClickHandler() {
        public void onClick(ClickEvent eventIn) {
            _securityAdmin.requestData(securityTab.getSearchString());
        }
    };
/* TODO:
    ClickHandler handleCapcoRetrievalButton = new ClickHandler() {
        public void onClick(ClickEvent eventIn) {
            _capcoAdmin.requestData(capcoTab.getSearchString());
        }
    };
*/
    private VortexEventHandler<Response<String, List<List<String>>>> handleRestrictiveInformationResponse
    =new AbstractVortexEventHandler<Response<String, List<List<String>>>>() {
        @Override
        public boolean onError(Throwable myException) {
            
            Dialog.showException(myException);
            return true;
        }
        @Override
        public void onSuccess(Response<String, List<List<String>>> responseIn) {
            
            List<List<String>> listIn =  responseIn.getResult();
            
            if ((null != listIn) && (0 < listIn.size())) {

                List<String> myRoleNames = listIn.get(0);
                Map<String, Boolean> myPortionMap = new HashMap<String, Boolean>();

                if (1 < listIn.size()) {

                    List<String> myPortionStrings = listIn.get(1);

                    for (String myPortionText : myPortionStrings) {

                        if ((null != myPortionText) && (0 < myPortionText.length())){

                            myPortionMap.put(myPortionText.toLowerCase(),  true);
                        }
                    }
                }
                _shared._portionMap = myPortionMap;
                _shared.buildRoleMap(myRoleNames);

                _userAdmin.enableSelectionControls(responseIn.getCount());
                
            } else {
                
                requestRestrictiveInformation();
            }
        }
    };
    
    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public AdministrationView() {
        
        super();
    }

    public void saveState() {}

    public void restoreState() {}

    public void forceExit() {}

    @Override
    public Widget asWidget() {

        if (null == widget) {

            widget = uiBinder.createAndBindUi(this);
            UserSecurityInfo myUserInfo = WebMain.injector.getMainPresenter().getUserInfo();

            requestRestrictiveInformation();

            _shared = new SharedItems(myUserInfo);

            if (_shared.provideSharing()) {
                if(WebMain.getClientStartupInfo().getFeatureConfigGWT().isShowReportsTab()) {
                    reportsTab = new ReportsTab(myUserInfo, handleReportsRetrieveButton);
                    tabPanel.add(reportsTab);
                    _reportsAdmin = new ReportsAdmin(reportsTab, _shared);
                }
            }

            userTab = new UserTab(myUserInfo, handleUserRetrievalButton);
            tabPanel.add(userTab);

            if (_shared.provideSharing()) {

                sharingTab = new GroupTab(GroupType.SHARING, handleSharingRetrievalButton);
                tabPanel.add(sharingTab);


            }

            if (_shared.provideSecurity()) {

                securityTab = new GroupTab(GroupType.SECURITY, handleSecurityRetrievalButton);
                tabPanel.add(securityTab);

// TODO:            capcoTab = new CapcoTab(handleCapcoRetrievalButton);
// TODO:           tabPanel.add(capcoTab);
            }

            _userAdmin = new UserAdmin(userTab, _shared);

            if (_shared.provideSharing()) {
                _sharingAdmin = new SharingAdmin(sharingTab, _shared);
            }

            if (_shared.provideSecurity()) {

                _securityAdmin = new SecurityAdmin(securityTab, _shared);
// TODO:            _capcoAdmin = new CapcoAdmin(capcoTab, _shared);
            }

            _shared.refreshGroups();

            addHandlers();
        }
        return widget;
    }
    
    private void addHandlers() {
        
        exitButton.addClickHandler(handleExitButton);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    private void requestRestrictiveInformation() {

        try {
            VortexFuture<Response<String, List<List<String>>>> vortexFuture = WebMain.injector.getVortex().createFuture();
            vortexFuture.execute(UserAdministrationServiceProtocol.class).identifyRestrictedStrings();
            
            vortexFuture.addEventHandler(handleRestrictiveInformationResponse);

        } catch (Exception myException) {
            
            Dialog.showException(myException);
        }
    }
}
