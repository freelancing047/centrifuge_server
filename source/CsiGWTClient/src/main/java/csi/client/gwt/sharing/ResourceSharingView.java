package csi.client.gwt.sharing;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

import csi.client.gwt.WebMain;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.mainapp.CsiDisplay;
import csi.client.gwt.mainapp.MainView.Mode;
import csi.client.gwt.util.Display;
import csi.client.gwt.util.ResponseHandler;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.AbstractCsiTab;
import csi.client.gwt.widget.boot.CsiTabPanel;
import csi.client.gwt.widget.buttons.SimpleButton;
import csi.server.common.dto.Response;
import csi.server.common.dto.user.UserSecurityInfo;
import csi.server.common.service.api.UserAdministrationServiceProtocol;


public class ResourceSharingView extends Composite implements CsiDisplay {

    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    interface MyUiBinder extends UiBinder<Widget, ResourceSharingView> {
    }
    
    @UiField
    SimpleButton exitButton;
    @UiField
    CsiTabPanel tabPanel;

    ResourceTab templateTab;
    ResourceTab dataviewTab;
    ResourceTab themeTab;
    ResourceTab basemapTab;
    ResourceTab datatableTab;
    Widget displayWidget = null;

    AbstractCsiTab activeTab = null;

    private CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    private String _metaViewerGroup;
    private String _adminGroup;
    private String _securityGroup;
    private String _everyoneGroup;
    private String _adminUser;
    private String _securityUser;
    private String _originatorGroup;
    private String _currentUser;

    private UserSecurityInfo _userInfo;

    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private VortexEventHandler<Response<String, List<String>>> handleGroupRefreshResponse
            = new AbstractVortexEventHandler<Response<String, List<String>>>() {

        @Override
        public boolean onError(Throwable myException) {

            Display.error("ResourceSharingView", 1, myException);
            return true;
        }

        @Override
        public void onSuccess(Response<String, List<String>> responseIn) {

            if (ResponseHandler.isSuccess(responseIn)) {

                templateTab.replaceGroupLists(responseIn.getResult());
                dataviewTab.replaceGroupLists(responseIn.getResult());
                themeTab.replaceGroupLists(responseIn.getResult());
                datatableTab.replaceGroupLists(responseIn.getResult());
                basemapTab.replaceGroupLists(responseIn.getResult());
            }
        }
    };

    private VortexEventHandler<Response<String, List<String>>> handleUserRefreshResponse
            = new AbstractVortexEventHandler<Response<String, List<String>>>() {

        @Override
        public boolean onError(Throwable myException) {

            Display.error("ResourceSharingView", 2, myException);
            return true;
        }

        @Override
        public void onSuccess(Response<String, List<String>> responseIn) {

            if (ResponseHandler.isSuccess(responseIn)) {

                templateTab.replaceUserLists(responseIn.getResult());
                dataviewTab.replaceUserLists(responseIn.getResult());
                themeTab.replaceUserLists(responseIn.getResult());
                datatableTab.replaceUserLists(responseIn.getResult());
                basemapTab.replaceUserLists(responseIn.getResult());
            }
        }
    };

    
    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public ResourceSharingView() {

        _userInfo = WebMain.injector.getMainPresenter().getUserInfo();
        _metaViewerGroup = _userInfo.getViewerGroup();
        _adminGroup = _userInfo.getAdminGroup();
        _securityGroup = _userInfo.getCsoGroup();
        _originatorGroup = _userInfo.getOriginatorGroup();
        _everyoneGroup = _userInfo.getEveryoneGroup();
        _adminUser = _userInfo.getAdminUser();
        _securityUser = _userInfo.getCsoUser();
        _currentUser = _userInfo.getName();
   }

    public void saveState() {

        activeTab = tabPanel.getActiveTab();
    }

    public void restoreState() {

        if (null != activeTab) {

            DeferredCommand.add(new Command() {
                public void execute() {
                    tabPanel.selectTab(activeTab);
                }
            });
        }
    }

    public void forceExit() {}

    @Override
    public Widget asWidget() {

        if (null == displayWidget) {

            displayWidget = uiBinder.createAndBindUi(this);

            templateTab = new TemplateTab(this);
            dataviewTab = new DataViewTab(this);
            datatableTab = new DataTableTab(this);
            tabPanel.add(templateTab);
            tabPanel.add(dataviewTab);
            tabPanel.add(datatableTab);
            if(!WebMain.getClientStartupInfo().getFeatureConfigGWT().isUseNewLogoutPage()) {
                themeTab = new ThemeTab(this);
                tabPanel.add(themeTab);
                basemapTab = new MapLayerTab(this);
                tabPanel.add(basemapTab);
            }
            refreshGroups();
            refreshUsers();

            exitButton.addClickHandler(new ClickHandler() {
                public void onClick(ClickEvent eventIn) {
                    WebMain.injector.getEventBus().fireEvent(new csi.client.gwt.events.ExitSharingModeEvent());
                }
            });
        }
        DeferredCommand.add(new Command() {
            public void execute() {
                restoreState();
            }
        });
        return displayWidget;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public void refreshGroups() {

        // Request data from the server

        try {
            VortexFuture<Response<String, List<String>>> vortexFuture = WebMain.injector.getVortex().createFuture();

            vortexFuture.execute(UserAdministrationServiceProtocol.class).getActiveGroupNames();
            vortexFuture.addEventHandler(handleGroupRefreshResponse);

        } catch (Exception myException) {

            Display.error("ResourceSharingView", 3, myException);
        }
    }

    public void refreshUsers() {

        // Request data from the server

        try {
            VortexFuture<Response<String, List<String>>> vortexFuture = WebMain.injector.getVortex().createFuture();

            if (_userInfo.isAdmin() || _userInfo.isSecurity()) {

                vortexFuture.execute(UserAdministrationServiceProtocol.class).getAllUserNames();

            } else {

                vortexFuture.execute(UserAdministrationServiceProtocol.class).getActiveUserNames();
            }
            vortexFuture.addEventHandler(handleUserRefreshResponse);

        } catch (Exception myException) {

            Display.error("ResourceSharingView", 4, myException);
        }
    }
}
