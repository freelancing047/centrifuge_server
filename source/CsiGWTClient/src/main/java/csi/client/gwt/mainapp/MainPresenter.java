package csi.client.gwt.mainapp;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.ui.Widget;
import csi.client.gwt.ApplicationStartEvent;
import csi.client.gwt.ApplicationStartEventHandler;
import csi.client.gwt.WebMain;
import csi.client.gwt.admin.AdministrationView;
import csi.client.gwt.csiwizard.AdHocInterface;
import csi.client.gwt.csiwizard.dialogs.SourceEditDialog;
import csi.client.gwt.dataview.*;
import csi.client.gwt.dataview.directed.DirectedPresenter;
import csi.client.gwt.edit_sources.presenters.DataSourceEditorPresenter;
import csi.client.gwt.events.*;
import csi.client.gwt.http.Post;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.mainapp.MainView.Mode;
import csi.client.gwt.sharing.ResourceSharingView;
import csi.client.gwt.util.Display;
import csi.client.gwt.util.ResponseHandler;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.Callback;
import csi.client.gwt.vortex.VortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.WatchBox;
import csi.client.gwt.widget.WatchBoxInterface;
import csi.client.gwt.widget.boot.*;
import csi.client.gwt.widget.buttons.ButtonDef;
import csi.client.gwt.worksheet.WorksheetPresenter;
import csi.config.ClientConfig;
import csi.server.common.dto.*;
import csi.server.common.dto.SelectionListData.DriverBasics;
import csi.server.common.dto.SelectionListData.OptionBasics;
import csi.server.common.dto.SelectionListData.ResourceBasics;
import csi.server.common.dto.system.ReleaseInfo;
import csi.server.common.dto.system.UserFunction;
import csi.server.common.dto.user.UserPreferences;
import csi.server.common.dto.user.UserSecurityInfo;
import csi.server.common.dto.user.preferences.DialogPreference;
import csi.server.common.dto.user.preferences.GeneralPreference;
import csi.server.common.dto.user.preferences.ResourceFilter;
import csi.server.common.enumerations.AclControlType;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.CsiUUID;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.security.CapcoInfo;
import csi.server.common.model.security.SecurityTagsInfo;
import csi.server.common.service.api.DataViewActionServiceProtocol;
import csi.server.common.service.api.TestActionsServiceProtocol;
import csi.server.common.service.api.UserAdministrationServiceProtocol;
import csi.server.common.util.ConnectorSupport;
import csi.shared.core.util.Native;

import java.util.*;

public class MainPresenter {


    private class OpenDataViewParameters {

        public String _name;
        public String _uuid;
        public String _owner;

        public OpenDataViewParameters(String nameIn, String uuidIn, String ownerIn) {

            _name = nameIn;
            _uuid = uuidIn;
            _owner = ownerIn;
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static final String VIZ_PARAM = "vis";

    private WatchBoxInterface watchBox;
	private MainView mainView;
	
	//This is used to track editing of templates so we can block conflicts and semaphore them
	private String editingResource = null;
	
    private DataViewPresenter dataViewPresenter = null;
	private DirectedPresenter directedPresenter = null;
	private boolean _openningDataview = false;
	private Map<String, AuthDO> _authorizationMap = null;
    private UserSecurityInfo _userSecurity = null;
    private UserPreferences _userPreferences = null;
    private ClientConfig _clientConfig = null;
	private MaskDialog mask;
    private boolean _provideSourceName = false;
    private boolean _bracketDefault = false;
    private boolean _incrementImmediately = false;
    private boolean _useCaselessFileNames = false;
    private boolean _restrictedList = false;
    private String _requestDataviewName = null;
    private String _requestLayout = null;
    private String _annotationMode = null;
    private LaunchRequest _launchRequest = null;
    private Map<String, Integer> _requestVizParams = null;
    private VortexEventHandler<ResourceFilter> _resourceFilterCallBack = null;
    private Map<Long, Callback<List<OptionBasics>>> _filterListCallBackMap
            = new TreeMap<Long, Callback<List<OptionBasics>>>();
    private Long _filterCallBackKey = 0L;
    private RefreshDataViewDialog refreshDataViewDialog;
    private ResourceFilter _defaultResourceFilter = null;
    private OpenDataViewEvent myActiveEvent = null;
    private OpenDataViewParameters myActiveParameters = null;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private VortexEventHandler<Response<Long, List<ResourceFilter>>> handleAddReplaceResourceFilterResponse
            = new AbstractVortexEventHandler<Response<Long, List<ResourceFilter>>>() {

        @Override
        public boolean onError(Throwable exceptionIn) {

            try {

                watchBox.hide();
                if (null != _resourceFilterCallBack) {

                    _resourceFilterCallBack.onError(exceptionIn);

                } else {

                    Display.error(exceptionIn);
                }

            } catch (Exception myException) {

                Display.error("MainPresenter", 1, myException);
            }
            return true;
        }

        @Override
        public void onSuccess(Response<Long, List<ResourceFilter>> responseIn) {

            try {

                Long myKey = responseIn.getKey();
                Callback<List<OptionBasics>> myCallBack = _filterListCallBackMap.get(myKey);

                watchBox.hide();

                if (ResponseHandler.isSuccess(responseIn)) {

                    List<ResourceFilter> myFilterList = responseIn.getResult();

                    _userPreferences.setResourceFilterList(myFilterList);
                    _defaultResourceFilter = null;
                    for (ResourceFilter myFilter : myFilterList) {

                        if (myFilter.getDefaultFilter()) {

                            _defaultResourceFilter = myFilter;
                            break;
                        }
                    }

                    if (null != myCallBack) {

                        if (null != _userPreferences) {

                            myCallBack.onSuccess(_userPreferences.getResourceFilterDisplayList());

                        } else {

                            myCallBack.onSuccess(new ArrayList<OptionBasics>());
                        }
                    }
                }
                _filterListCallBackMap.remove(myKey);

            } catch (Exception myException) {

                Display.error("MainPresenter", 2, myException);
            }
        }
    };

    private VortexEventHandler<Response<Long, DialogPreference>> handleAddReplaceDialogPreferenceResponse
            = new AbstractVortexEventHandler<Response<Long, DialogPreference>>() {

        @Override
        public boolean onError(Throwable exceptionIn) {

            try {

                Display.error(exceptionIn);

            } catch (Exception myException) {

                Display.error("MainPresenter", 3, myException);
            }
            return true;
        }

        @Override
        public void onSuccess(Response<Long, DialogPreference> responseIn) {

            try {

                if (ResponseHandler.isSuccess(responseIn)) {

                    _userPreferences.addReplaceDialogPreference(responseIn.getResult());
                }

            } catch (Exception myException) {

                Display.error("MainPresenter", 4, myException);
            }
        }
    };

    private VortexEventHandler<Response<Long, GeneralPreference>> handleAddReplaceGeneralPreferenceResponse
            = new AbstractVortexEventHandler<Response<Long, GeneralPreference>>() {

        @Override
        public boolean onError(Throwable exceptionIn) {

            try {

                Display.error(exceptionIn);

            } catch (Exception myException) {

                Display.error("MainPresenter", 5, myException);
            }
            return true;
        }

        @Override
        public void onSuccess(Response<Long, GeneralPreference> responseIn) {

            try {

                if (ResponseHandler.isSuccess(responseIn)) {

                    _userPreferences.addReplaceGeneralPreference(responseIn.getResult());
                }

            } catch (Exception myException) {

                Display.error("MainPresenter", 6, myException);
            }
        }
    };

    private VortexEventHandler<List<DriverBasics>> _driverListCallback = null;

    //
    // Handle response to request for data source driver list
    //
    private VortexEventHandler<List<DriverBasics>> handleDriverListRequestResponse
            = new AbstractVortexEventHandler<List<DriverBasics>>() {
        @Override
        public boolean onError(Throwable exceptionIn) {

            try {

                if (null != _driverListCallback) {

                    _driverListCallback.onError(exceptionIn);

                } else {

                    Display.error(exceptionIn);
                }
                _driverListCallback = null;
                _restrictedList = false;

            } catch (Exception myException) {

                Display.error("MainPresenter", 7, myException);
            }
            return false;
        }
        @Override
        public void onSuccess(List<DriverBasics> listIn) {

            try {

                ConnectorSupport.setInstance(listIn);
                if (null != _driverListCallback) {

                    _driverListCallback.onSuccess(ConnectorSupport.getInstance().getDriverList(_restrictedList));
                }
                _driverListCallback = null;
                _restrictedList = false;

            } catch (Exception myException) {

                Display.error("MainPresenter", 8, myException);
            }
        }
    };

    private AbstractVortexEventHandler<Response<String, DataView>> launchUrlTemplateResponse
            = new AbstractVortexEventHandler<Response<String, DataView>>() {

        @Override
        public boolean onError(Throwable exceptionIn){

            try {

                hideMask();
                ErrorDialog dialog = new ErrorDialog(exceptionIn.getMessage());
                dialog.show();

            } catch (Exception myException) {

                Display.error("MainPresenter", 9, myException);
            }
            return false;
        }

        @Override
        public void onSuccess(Response<String, DataView> responseIn) {

            try {

                if (ResponseHandler.isSuccess(responseIn)) {

                    DataView myDataView = responseIn.getResult();

                    if (null != myDataView) {

                        long myRowCount = responseIn.getCount();
                        boolean myMissingData = responseIn.getLimitedData();

                        if (Strings.isNullOrEmpty(_requestLayout)) {
                            getView().showApplicationToolbar();
                            WebMain.injector.getEventBus().fireEvent(new OpenDataViewEvent(myDataView, myRowCount, myMissingData));
                        } else {
                            WebMain.injector.getEventBus().fireEvent(new OpenDirectedViewEvent(responseIn,
                                    LayoutType.valueOf(_requestLayout.toUpperCase()), _requestVizParams));
                        }
                    }

                } else {

                    hideMask();
                }

            } catch (Exception myException) {

                Display.error("MainPresenter", 10, myException);
            }
        }
    };

    private AbstractVortexEventHandler<Response<String, Map<String, DataView>>> getDataViewsByNameResponse
            = new AbstractVortexEventHandler<Response<String, Map<String, DataView>>>() {

        @Override
        public boolean onError(Throwable exceptionIn) {

            Display.error("MainPresenter", 11, exceptionIn);
            return false;
        }

        @Override
        public void onSuccess(Response<String, Map<String, DataView>> responseIn) {

            try {

                hideMask();

                if (ResponseHandler.isSuccess(responseIn)) {

                    Map<String, DataView> dataviews = responseIn.getResult();

                    if (dataviews != null && !dataviews.isEmpty()) {

                        if(dataviews.size() >= 1){
                            Date date = new Date();

                            _launchRequest.setTargetDvName(_requestDataviewName + "(" + date.toString() +")");
                        }
                    }

                }

                try {
                    VortexFuture<Response<String, DataView>> myVortexFuture = WebMain.injector.getVortex().createFuture();
                    myVortexFuture.addEventHandler(launchUrlTemplateResponse);
                    myVortexFuture.execute(DataViewActionServiceProtocol.class).launchUrlTemplate(_launchRequest);

                } catch (Exception myException) {

                    Display.error(myException.getMessage());
                }

            } catch (Exception myException) {

                Display.error("MainPresenter", 12, myException);
            }
        }
    };

    private ApplicationStartEventHandler applicationStartHandler = new ApplicationStartEventHandler() {

        @Override
        public void onApplicationStart(ApplicationStartEvent event) {

            try {

                VortexFuture<StartUpDownload> myVortexFuture = WebMain.injector.getVortex().createFuture();

                event.getViewport().add(getView());

                myVortexFuture.addEventHandler(startupCallBack);
                myVortexFuture.execute(UserAdministrationServiceProtocol.class).getStartupInfo();

            } catch (Exception myException) {

                Display.error("MainPresenter", 13, myException);
            }
        }
    };

    private ClickHandler retryLoginHandler = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {

            Post.logout();
        }
    };

    private VortexEventHandler<StartUpDownload> startupCallBack = new AbstractVortexEventHandler<StartUpDownload>() {

        @Override
        public boolean onError(Throwable myExceptionIn) {

            Display.error("Session Initialization Failure", myExceptionIn, retryLoginHandler);
            return false;
        }

        @Override
        public void onSuccess(StartUpDownload startupInfoIn) {

            try {

                List<UserFunction> myFunctionList = startupInfoIn.getUserFunctions();

                ReleaseInfo.initialize(startupInfoIn.getReleaseVersion(), startupInfoIn.getBuildNumber());
                CsiModal.setPopupConstants(startupInfoIn.getSatisfiedCount(), startupInfoIn.getMillisecondWait());

                _provideSourceName = startupInfoIn.getProvideSourceName();
                _bracketDefault = startupInfoIn.getBracketDefault();
                _incrementImmediately = startupInfoIn.getIncrementImmediately();
                _userSecurity = startupInfoIn.getUserSecurity();
                _userPreferences = startupInfoIn.getUserPreferences();
                _clientConfig = startupInfoIn.getClientConfig();
                _useCaselessFileNames = startupInfoIn.getCaselessFileNames();

                if (null != myFunctionList) {

                    UserFunction.loadUserFunctions(myFunctionList);
                }
                getView().initializeDisplay(_userSecurity);
                watchBox = WatchBox.getInstance();
                startCentrifuge();
            } catch (Exception myException) {

                Display.error("MainPresenter", 14, myException);
            }
        }
    };

    private SourceEditDialog sourceEditDialog;

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Public Methods                                      //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public AbstractDataViewPresenter getDataViewPresenter(boolean orDirectedDataviewPresenter) {
        if ((dataViewPresenter == null) && orDirectedDataviewPresenter) {
            return directedPresenter;
        }
        return dataViewPresenter;
    }

    public boolean displayLandingPage() {

        return ((null == _requestLayout) || (0 == _requestLayout.length()));
    }

    public void initialize() {

        try {

            WebMain.injector.getEventBus().addHandler(ApplicationStartEvent.type, applicationStartHandler);
            addHandlers();

            retrieveConnectorList(null, false);

        } catch (Exception myException) {

            Display.error("MainPresenter", 15, myException);
        }
    }

    public boolean canUpdate() {

        AbstractDataViewPresenter myPresenter = getDataViewPresenter(true);

        return (null != myPresenter) ? myPresenter.canUpdate() : false;
    }

    public void updateClassification(String uuidIn, CapcoInfo capcoIn, SecurityTagsInfo securityTagsIn, boolean isAuthorizedIn) {

        AbstractDataViewPresenter myPresenter = WebMain.injector.getMainPresenter().getDataViewPresenter(true);

        if (null != myPresenter) {

            myPresenter.updateClassification(uuidIn, capcoIn, securityTagsIn, isAuthorizedIn);
        }
    }

	public String checkDecode(String paramValue) {

        try {

            if(!Strings.isNullOrEmpty(paramValue)){

                paramValue = URL.decodePathSegment(paramValue);
            }
            return paramValue;

        } catch (Exception myException) {

            Display.error("MainPresenter", 16, myException);
        }
        return null;
    }
	
	public String[] checkDecodeArray(String[] paramValue) {

        try {

            if(paramValue == null || paramValue.length == 0){
                return null;
            }

            String[] decoded = new String[paramValue.length];

            int count = 0;
            for(String param: paramValue){
                if(!Strings.isNullOrEmpty(param)){

                    decoded[count] = URL.decodePathSegment(param);
                    count++;
                }
            }
            return decoded;

        } catch (Exception myException) {

            Display.error("MainPresenter", 17, myException);
        }
        return null;
    }
    //
    // Request the list of connectors from the server which the user can use
    //
    public void retrieveConnectorList(VortexEventHandler<List<DriverBasics>> handlerIn, boolean restrictedIn) {

        try {

            if (null == handlerIn) {

                _driverListCallback = handlerIn;
                _restrictedList = true;

                VortexFuture<List<DriverBasics>> myVortexFuture = WebMain.injector.getVortex().createFuture();

                try {
                    myVortexFuture.addEventHandler(handleDriverListRequestResponse);
                    myVortexFuture.execute(TestActionsServiceProtocol.class).listConnectionDescriptors();

                } catch (Exception myException) {

                    Display.error(myException.getMessage());
                }

            } else {

                handlerIn.onSuccess(ConnectorSupport.getInstance().getDriverList(restrictedIn));
            }

        } catch (Exception myException) {

            Display.error("MainPresenter", 18, myException);
        }
    }

    public MainView.Mode getMode() {

        try {

            return getView().getMode();

        } catch (Exception myException) {

            Display.error("MainPresenter", 19, myException);
        }
        return null;
	}

	public Widget getWidget() {

        try {

            return getView().getWidget();

        } catch (Exception myException) {

            Display.error("MainPresenter", 20, myException);
        }
        return null;
	}

    public boolean provideSourceName() {
        return _provideSourceName;
    }

    public boolean bracketDefault() {
        return _bracketDefault;
    }

    public boolean incrementImmediately() {
        return _incrementImmediately;
    }

    public boolean isDataViewReadOnly() {

        return (null != dataViewPresenter)
                ? dataViewPresenter.isReadOnly()
                : (null != directedPresenter) ? directedPresenter.isReadOnly() : true;
    }

    public boolean canEditDataView() {

        return !isDataViewReadOnly();
    }

	public void cancelOpenDataViewProcess() {

		_openningDataview = false;
	}

    public void conditionalAbort() {

        try {

            if (_openningDataview) {

                ApplicationToolbarLocator.getInstance().abortDataView();
            }

        } catch (Exception myException) {

            Display.error("MainPresenter", 21, myException);
        }
    }

    public ClientConfig getClientConfig() {

        return _clientConfig;
    }

    public void getResourceFilterDisplayList(Callback<List<OptionBasics>> callBackIn) {

        try {

            VortexFuture<Response<Long, List<ResourceFilter>>> myVortexFuture = WebMain.injector.getVortex().createFuture();
            Long myKey = _filterCallBackKey++;

            watchBox.show();
            _filterListCallBackMap.put(myKey, callBackIn);
            myVortexFuture.addEventHandler(handleAddReplaceResourceFilterResponse);
            myVortexFuture.execute(UserAdministrationServiceProtocol.class).getResourceFilterList(myKey);

        } catch (Exception myException) {

            watchBox.hide();
            Display.error("MainPresenter", 22, myException);
        }
    }

    public ResourceFilter getDefaultResourceFilter() {

        return _defaultResourceFilter;
    }

    public ResourceFilter getResourceFilter(final OptionBasics selectionIn) {

        try {

            return _userPreferences.getResourceFilter(selectionIn);

        } catch (Exception myException) {

            Display.error("MainPresenter", 26, myException);
        }
        return null;
    }

    public double getDialogPreference(String dialogKeyIn, String dataKeyIn, double defaultIn) {

        return Double.parseDouble(getDialogPreference(dialogKeyIn, dataKeyIn, Double.toString(defaultIn)));
    }

    public long getDialogPreference(String dialogKeyIn, String dataKeyIn, long defaultIn) {

        return Long.decode(getDialogPreference(dialogKeyIn, dataKeyIn, Long.toString(defaultIn)));
    }

    public boolean getDialogPreference(String dialogKeyIn, String dataKeyIn, boolean defaultIn) {

        String myDefault = defaultIn ? "T" : "F";
        String myResult = getDialogPreference(dialogKeyIn, dataKeyIn, myDefault);

        return ("T".equals(myResult));
    }

    public String getDialogPreference(String dialogKeyIn, String dataKeyIn, String defaultIn) {

        DialogPreference myPreference = getDialogPreference(dialogKeyIn, dataKeyIn);

        return (null != myPreference) ? myPreference.getDataValue() : defaultIn;
    }

    public DialogPreference getDialogPreference(String dialogKeyIn, String dataKeyIn) {

        DialogPreference myPreference = null;

        try {

            myPreference = _userPreferences.getDialogPreference(dialogKeyIn, dataKeyIn);

        } catch (Exception myException) {

            Display.error("MainPresenter", 27, myException);
        }

        return myPreference;
    }

    public void setDialogPreference(String dialogKeyIn, String dataKeyIn, double valueIn) {

        setDialogPreference(dialogKeyIn, dataKeyIn, Double.toString(valueIn));
    }

    public void setDialogPreference(String dialogKeyIn, String dataKeyIn, long valueIn) {

        setDialogPreference(dialogKeyIn, dataKeyIn, Long.toString(valueIn));
    }

    public void setDialogPreference(String dialogKeyIn, String dataKeyIn, boolean valueIn) {

        setDialogPreference(dialogKeyIn, dataKeyIn, valueIn ? "T" : "F");
    }

    public void setDialogPreference(String dialogKeyIn, String dataKeyIn, String valueIn) {

        try {

            DialogPreference myPreference
                    = _userPreferences.addReplaceDialogPreference(new DialogPreference(getUserName(),
                                                                                dialogKeyIn, dataKeyIn, valueIn));

            if (null != myPreference) {

                try {

                    Long myKey = _filterCallBackKey++;
                    VortexFuture<Response<Long, DialogPreference>> myVortexFuture
                            = WebMain.injector.getVortex().createFuture();

                    myVortexFuture.addEventHandler(handleAddReplaceDialogPreferenceResponse);
                    myVortexFuture.execute(UserAdministrationServiceProtocol.class).addReplaceDialogPreference(myKey,
                                                                                                        myPreference);

                } catch (Exception myException) {

                    Display.error("MainPresenter", 30, myException);
                }
            }

        } catch (Exception myException) {

            Display.error("MainPresenter", 27, myException);
        }
    }

    public String getGeneralPreference(String dataKeyIn, String defaultIn) {

        GeneralPreference myPreference = getGeneralPreference(dataKeyIn);

        return (null != myPreference) ? myPreference.getDataValue() : null;
    }

    public GeneralPreference getGeneralPreference(String dataKeyIn) {

        GeneralPreference myPreference = null;

        try {

            myPreference = _userPreferences.getGeneralPreference(dataKeyIn);

        } catch (Exception myException) {

            Display.error("MainPresenter", 28, myException);
        }

        return myPreference;
    }

    public GeneralPreference setGeneralPreference(String dataKeyIn, String valueIn) {

        GeneralPreference myPreference = null;

        try {

            myPreference = _userPreferences.addReplaceGeneralPreference(new GeneralPreference(getUserName(),
                                                                                                dataKeyIn, valueIn));

            if (null != myPreference) {

                try {

                    Long myKey = _filterCallBackKey++;
                    VortexFuture<Response<Long, GeneralPreference>> myVortexFuture
                            = WebMain.injector.getVortex().createFuture();

                    myVortexFuture.addEventHandler(handleAddReplaceGeneralPreferenceResponse);
                    myVortexFuture.execute(UserAdministrationServiceProtocol.class).addReplaceGeneralPreference(myKey,
                            myPreference);

                } catch (Exception myException) {

                    Display.error("MainPresenter", 30, myException);
                }
            }
        } catch (Exception myException) {

            Display.error("MainPresenter", 28, myException);
        }

        return myPreference;
    }

    public void addReplaceResourceFilter(ResourceFilter resourceFilterIn, Callback<List<OptionBasics>> callBackIn) {

        try {

            VortexFuture<Response<Long, List<ResourceFilter>>> myVortexFuture = WebMain.injector.getVortex().createFuture();
            Long myKey = _filterCallBackKey++;

            _filterListCallBackMap.put(myKey, callBackIn);
            watchBox.show();
            myVortexFuture.addEventHandler(handleAddReplaceResourceFilterResponse);
            myVortexFuture.execute(UserAdministrationServiceProtocol.class).addReplaceResourceFilter(myKey, resourceFilterIn);

        } catch (Exception myException) {

            watchBox.hide();
            Display.error("MainPresenter", 29, myException);
        }
    }

    public void deleteResourceFilters(List<Long> idListIn, Callback<List<OptionBasics>> callBackIn) {

        if ((null != idListIn) && (0 < idListIn.size())) {

            try {

                VortexFuture<Response<Long, List<ResourceFilter>>> myVortexFuture = WebMain.injector.getVortex().createFuture();
                Long myKey = _filterCallBackKey++;

                watchBox.show();
                _filterListCallBackMap.put(myKey, callBackIn);
                myVortexFuture.addEventHandler(handleAddReplaceResourceFilterResponse);
                myVortexFuture.execute(UserAdministrationServiceProtocol.class).deleteResourceFilters(myKey, idListIn);

            } catch (Exception myException) {

                watchBox.hide();
                Display.error("MainPresenter", 32, myException);
            }
        }
    }

    public void deleteDialogPreference(String dialogKeyIn, String dataKeyIn) {

        try {

            Long myId = _userPreferences.deleteDialogPreference(dialogKeyIn, dataKeyIn);

            if (null != myId) {

            }

        } catch (Exception myException) {

            Display.error("MainPresenter", 33, myException);
        }
    }

    public void deleteGeneralPreference(String dataKeyIn) {

        try {

            Long myId = _userPreferences.deleteGeneralPreference(dataKeyIn);

            if (null != myId) {

            }

        } catch (Exception myException) {

            Display.error("MainPresenter", 34, myException);
        }
    }

    public long getMaxBufferSize() {

        return (null != _clientConfig) ? _clientConfig.getMaxClientBufferSize() : 0L;
    }

    public boolean isSecurity() {

        return getUserInfo().isSecurity();
    }

    public boolean isAdmin() {

        return getUserInfo().isAdmin();
    }

    public boolean isIconAdmin() {

        return getUserInfo().getIconAdmin();
    }

    public boolean isFieldListAdmin() {
        return getUserInfo().getFieldListAdmin();
    }

    public String getUserName() {

        return getUserInfo().getName();
    }

    public UserSecurityInfo getUserInfo() {

        return _userSecurity;
    }

    public boolean useCaselessFilenames() {

        return _useCaselessFileNames;
    }

	public Map<String, AuthDO> getAuthorizationMap() {

		if (null == _authorizationMap) {

			_authorizationMap = new HashMap<String, AuthDO>();
		}
		return _authorizationMap;
	}

	public List<AuthDO> getAuthorizationList() {

        try {

            return new ArrayList<AuthDO>(getAuthorizationMap().values());

        } catch (Exception myException) {

            Display.error("MainPresenter", 35, myException);
        }
        return null;
	}

	public void closeExistingDataViewAndOpenNewOne(String newUuid){

        try {
            WorksheetPresenter myActiveWorkSheet = (newUuid == dataViewPresenter.getUuid()) ? dataViewPresenter.getActiveWorksheet() : null;
            String myWorkSheetName = (null != myActiveWorkSheet) ? myActiveWorkSheet.getName() : null;

            _openningDataview = false;
            getView().removeDataViewDisplay();
            closeDataView(dataViewPresenter.getUuid(), false);

            if (null != myWorkSheetName) {

                Native.log("Reloading.");
                openDataViewToWorksheet(newUuid, myWorkSheetName);

            } else {

                openDataView(newUuid);
            }

        } catch (Exception myException) {

            Display.error("MainPresenter", 36, myException);
        }
	}

    public void closeExistingDataViewAndOpenNewOne(DataView dataViewIn, long rowCountIn, boolean moreDataIn){

        try {
            WorksheetPresenter myActiveWorkSheet = (dataViewIn.getUuid() == dataViewPresenter.getUuid())
                                                    ? dataViewPresenter.getActiveWorksheet() : null;
            String myWorkSheetName = (null != myActiveWorkSheet) ? myActiveWorkSheet.getName() : null;

            _openningDataview = false;
            getView().removeDataViewDisplay();
            closeDataView(dataViewPresenter.getUuid(), false);

            if (null != myWorkSheetName) {

                Native.log("Reloading.");
                openDataViewToWorksheet(dataViewIn, rowCountIn, moreDataIn, myWorkSheetName);

            } else {

                openDataView(dataViewIn, rowCountIn, moreDataIn);
            }

        } catch (Exception myException) {

            Display.error("MainPresenter", 36, myException);
        }
    }

    public void closeExistingDataViewAndOpenNewOne(String newUuid, Map<String, Integer> params, LayoutType layout){

        try {

            _openningDataview = false;
            if(dataViewPresenter != null){
                closeDataView(dataViewPresenter.getUuid(), false);
            }
            openDirectedView(newUuid, params, layout);

        } catch (Exception myException) {

            Display.error("MainPresenter", 37, myException);
        }
    }

    public MaskDialog getMask() {
        return mask;
    }

    public void hideMask() {

        try {

            if(mask != null){
                mask.hide();
                mask.removeFromParent();
                mask = null;
            }

        } catch (Exception myException) {

            Display.error("MainPresenter", 38, myException);
        }
    }

    public static String decode(String s) {

        try {

            boolean needToChange = false;
            int numChars = s.length();
            StringBuffer sb = new StringBuffer(numChars > 500 ? numChars / 2 : numChars);
            int i = 0;

            char c;
            StringBuffer bytes = null;
            while (i < numChars) {
                c = s.charAt(i);
                switch (c) {
                    case '+':
                        sb.append(' ');
                        i++;
                        needToChange = true;
                        break;
                    case '%':
                /*
                 * Starting with this instance of %, process all
                 * consecutive substrings of the form %xy. Each
                 * substring %xy will yield a byte. Convert all
                 * consecutive  bytes obtained this way to whatever
                 * character(s) they represent in the provided
                 * encoding.
                 */

                        try {

                            // (numChars-i)/3 is an upper bound for the number
                            // of remaining bytes
                            bytes = new StringBuffer() ;
                            int pos = 0;

                            while ( ((i+2) < numChars) &&
                                    (c=='%')) {
                                bytes.append((char)Integer.parseInt(s.substring(i+1,i+3),16)) ;
                                pos++ ;
                                i+= 3;
                                if (i < numChars)
                                    c = s.charAt(i);
                            }

                            // A trailing, incomplete byte encoding such as
                            // "%x" will cause an exception to be thrown

                            if ((i < numChars) && (c=='%'))
                                throw new IllegalArgumentException(
                                        "URLDecoder: Incomplete trailing escape (%) pattern");

                            sb.append(bytes);
                        } catch (NumberFormatException e) {
                            throw new IllegalArgumentException(
                                    "URLDecoder: Illegal hex characters in escape (%) pattern - "
                                            + e.getMessage());
                        }
                        needToChange = true;
                        break;
                    default:
                        sb.append(c);
                        i++;
                        break;
                }
            }
            return (needToChange? sb.toString() : s);

        } catch (Exception myException) {

            Display.error("MainPresenter", 39, myException);
        }
        return null;
    }

    public void abortDataView() {

        _openningDataview = false;
        clearDataViewDisplay();
    }

    public void clearDataViewDisplay() {

        try {

            if (null != dataViewPresenter) {
                dataViewPresenter.close();
            }
            dataViewPresenter = null;
            directedPresenter = null;

            ApplicationToolbarLocator.getInstance().abortDataView();
            getView().exitMode(Mode.ANALYSIS);
            SecurityBanner.displayBanner(null);

        } catch (Exception myException) {

            Display.error("MainPresenter", 40, myException);
        }
    }

    public boolean checkDataView(String nameIn, String ownerIn) {

        AbstractDataViewPresenter myPresenter = getDataViewPresenter(true);

        return (null != myPresenter) && myPresenter.checkDataView(nameIn, ownerIn);
    }

    public void guaranteeDataView(String nameIn, String ownerIn) {

        try {

            AbstractDataViewPresenter myPresenter = getDataViewPresenter(true);

            if ((null != myPresenter) && myPresenter.checkDataView(nameIn, ownerIn)) {

                getView().exitMode(Mode.ANALYSIS);
                dataViewPresenter = null;
            }

        } catch (Exception myException) {

            Display.error("MainPresenter", 41, myException);
        }
    }

    public void refreshDataView() {

        try {

            AbstractDataViewPresenter myPresenter = getDataViewPresenter(true);

            if (null != myPresenter) {

                String myUuid = myPresenter.getUuid();
                clearDataViewDisplay();
                openDataView(myUuid);
            }

        } catch (Exception myException) {

            Display.error("MainPresenter", 41, myException);
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    //
    // Handle choice being made between current and new browser window
    //
    private ChoiceMadeEventHandler handleChoiceMadeEvent
            = new ChoiceMadeEventHandler() {
        @Override
        public void onChoiceMade(ChoiceMadeEvent eventIn) {

            try {

                Object myObject = eventIn.getData();

                if ((null != myObject) && (myObject instanceof OpenDataViewEvent)) {

                    OpenDataViewEvent myEvent = (OpenDataViewEvent)eventIn.getData();

                    switch (eventIn.getChoice()) {

                        case 1:

                            DataView myDataView = myEvent.getDataView();

                            DataViewInNewTab.open(myEvent.getDataViewUuid(), myEvent.getDataViewUuid(), myEvent.getDataViewOwner());
                            break;

                        case 2:

                            WebMain.injector.getEventBus().fireEvent(myEvent);
                            break;
                    }
                }

            } catch (Exception myException) {

                Dialog.showException("OpenDataviewDialog", myException);
            }
        }
    };

    private void chooseTab(OpenDataViewEvent eventIn) {

        CentrifugeConstants myConstants = CentrifugeConstantsLocator.get();

        String myChoiceDialogNewButtonString = myConstants.openDataviewDialog_Choice_NewButtonString();
        String myChoiceDialogCurrentButtonString = myConstants.openDataviewDialog_Choice_CurrentButtonString();
        String myChoiceDialogTitle = myConstants.openDataviewDialog_Choice_DialogTitle();
        String myChoiceInfoString = myConstants.openDataviewDialog_Choice_InfoString(myChoiceDialogNewButtonString, myChoiceDialogCurrentButtonString);

        List<ButtonDef> myButtonList = Arrays.asList(

                new ButtonDef(myChoiceDialogCurrentButtonString),
                new ButtonDef(myChoiceDialogNewButtonString)
        );

        if (null != dataViewPresenter) {

            (new DecisionDialog(myChoiceDialogTitle, myChoiceInfoString, myButtonList, handleChoiceMadeEvent, eventIn, 80)).show();

        } else {

            WebMain.injector.getEventBus().fireEvent(eventIn);
        }
    }

    public void beginOpenDataView(DataView dataViewIn) {

        if ((!_openningDataview) && (null != dataViewIn)) {

            chooseTab(new OpenDataViewEvent(dataViewIn));
        }
    }

    public void beginOpenDataView(ResourceBasics infoIn) {

        if ((!_openningDataview) && (null != infoIn) && (null != infoIn.getUuid())) {

            chooseTab(new OpenDataViewEvent(infoIn.getName(), infoIn.getUuid(), infoIn.getOwner()));
        }
    }

    public void beginOpenDataView(SharingDisplay infoIn) {

        if ((!_openningDataview) && (null != infoIn) && (null != infoIn.getUuid())) {

            chooseTab(new OpenDataViewEvent(infoIn.getName(), infoIn.getUuid(), infoIn.getOwner()));
        }
    }

    public void beginOpenDataView(String uuidIn) {

        if ((!_openningDataview) && (null != uuidIn)) {

            chooseTab(new OpenDataViewEvent(uuidIn));
        }
    }

    public void beginOpenDataView(String uuidIn, long rowCount, boolean moreData) {

        if ((!_openningDataview) && (null != uuidIn)) {

            chooseTab(new OpenDataViewEvent(uuidIn, rowCount, moreData));
        }
    }

    public void beginOpenDataView(String nameIn, String uuidIn, long rowCount, boolean moreData) {

        if ((!_openningDataview) && (null != uuidIn)) {

            chooseTab(new OpenDataViewEvent(nameIn, uuidIn, rowCount, moreData));
        }
    }

    private void openOrCancelDataView(final DataView dataViewIn, final String uuidIn) {

        String myUuid = (null != dataViewIn) ? dataViewIn.getUuid() : uuidIn;

        if ((!_openningDataview) && (null != myUuid)) {

            if (null != dataViewPresenter) {

                CentrifugeConstants centrifugeConstants = CentrifugeConstantsLocator.get();
                // TODO: to be shown only after new or current tab prompt
                Dialog.showContinueDialog(
                        centrifugeConstants.mainPresenter_CloseDataview_DialogTitle(),
                        centrifugeConstants.mainPresenter_CloseDataview_InfoString(centrifugeConstants.dialog_ContinueButton(),
                                centrifugeConstants.dialog_CancelButton()),
                        new ClickHandler() {

                            public void onClick(ClickEvent eventIn) {

                                try {

                                    closeExistingDataViewAndOpenNewOne(myUuid);

                                } catch (Exception myException) {

                                    Display.error("MainPresenter", 43, myException);
                                }
                            }
                        },
                        new ClickHandler() {

                            public void onClick(ClickEvent eventIn) {
                                _openningDataview = false;
                            }
                        }
                );

            } else {

                if (null != dataViewIn) {

                    openDataView(dataViewIn);

                } else {

                    openDataView(myUuid);
                }
            }
        }
    }

    private void openDataViewByName(final String dvName, String userName,
                                    final Map<String, Integer> params, final String layout, final String annotation) {
        VortexFuture<Response<String, Map<String, DataView>>> myVortexFuture = WebMain.injector.getVortex().createFuture();
        DataviewRequest request = new DataviewRequest();
        request.setUserName(userName);
        request.setDataviewName(dvName);
        request.setAccessMode(AclControlType.READ);
        //This breaks opening a template via url
        CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
        myVortexFuture.addEventHandler(new AbstractVortexEventHandler<Response<String, Map<String, DataView>>>() {

            @Override
            public void onSuccess(Response<String, Map<String, DataView>> responseIn) {

                try {

                    hideMask();

                    if (ResponseHandler.isSuccess(responseIn)) {

                        Map<String, DataView> dataviews = responseIn.getResult();

                        if (dataviews != null && !dataviews.isEmpty()) {

                            if(dataviews.size() == 1){
                                for(String key: dataviews.keySet()){
                                    if(Strings.isNullOrEmpty(layout)){
                                        getView().showApplicationToolbar();
                                        WebMain.injector.getEventBus().fireEvent(new OpenDataViewEvent(dataviews.get(key).getUuid()));
                                    } else {
                                        WebMain.injector.getEventBus().fireEvent(new OpenDirectedViewEvent(dataviews.get(key).getUuid(), LayoutType.valueOf(layout.toUpperCase()), params));
                                    }
                                }
                            } else {
                                SelectDataviewDialog dialog = new SelectDataviewDialog(dataviews, params, layout, getView());
                                dialog.show();

                            }
                        } else {
                            getView().hideApplicationToolbar();
                            ErrorDialog dialog = new ErrorDialog(CentrifugeConstantsLocator.get().mainPresenter_noDataviewFound() + " " + dvName);
                            dialog.show();
                        }
                    }

                } catch (Exception myException) {

                    Display.error("MainPresenter", 44, myException);
                }
            }

            @Override
            public boolean onError(Throwable t){

                try {

                    hideMask();
                    getView().hideApplicationToolbar();
                    ErrorDialog dialog = new ErrorDialog(t.getMessage());
                    dialog.show();

                } catch (Exception myException) {

                    Display.error("MainPresenter", 45, myException);
                }
                return false;
            }
        });
        try {
            myVortexFuture.execute(DataViewActionServiceProtocol.class).getDataviewsByName(request);
            hideMask();
            mask = new MaskDialog(i18n.mainPresenter_locatingDataview());
            mask.show();
        } catch (CentrifugeException e) {
            getView().hideApplicationToolbar();
            ErrorDialog dialog = new ErrorDialog(e.getMessage());
            dialog.show();
        }
    }

    private Map<String, Integer> parseVizParams() {

        Map<String, Integer> vizParams = new HashMap<String, Integer>();
        String parameter = null;
        int count = 0;
        String name = VIZ_PARAM + count;

        parameter = checkDecode(Location.getParameter(name));
        while(parameter != null){

            vizParams.put(parameter, count);

            count++;
            name = VIZ_PARAM + count;
            parameter = checkDecode(Location.getParameter(name));
        }

        return vizParams;
    }

    private void openDirectedView(final String uuidIn, Map<String, Integer> params, LayoutType layoutType) {

        if (!_openningDataview) {

            openDataView(uuidIn, layoutType, params);
        }
    }

    private void openDirectedView(Response<String, DataView> dataView, Map<String, Integer> params, LayoutType layoutType) {

        if (!_openningDataview) {

            openDataView(dataView, layoutType, params);
        }
    }

    private void addHandlers() {
        WebMain.injector.getEventBus().addHandler(OpenDataViewEvent.type,
                new OpenDataViewEventHandler() {

                    @Override
                    public void onDataViewOpen(final OpenDataViewEvent eventIn) {

                        try {

                            CsiModal.clearAll();
                            getView().showApplicationToolbar();
                            myActiveEvent = eventIn;
                            if (eventIn.hasMoreData()) {

                                long myRowCount = eventIn.getRowCount();

                                Display.continueDialog("More Data Available", "Number of rows truncated to "
                                                + Long.toString(myRowCount) + " row limit as requested.",
                                                continueOpen, cancelOpen);

                            } else {

                                continueOpen.onClick(null);
                            }

                        } catch (Exception myException) {

                            Display.error("MainPresenter", 46, myException);
                        }
                    }
                });
        WebMain.injector.getEventBus().addHandler(OpenDirectedViewEvent.type,
                new OpenDirectedViewEventHandler() {

                    @Override
                    public void onDataViewOpen(OpenDirectedViewEvent event) {

                        try {
                            getView().showApplicationToolbar();
                            if (event.getResponse() == null) {
                                openDirectedView(event.getDataViewUUID(), event.getVizParams(), event.getLayoutType());
                            }else{
                                openDirectedView(event.getResponse(), event.getVizParams(), event.getLayoutType());
                            }

                        } catch (Exception myException) {

                            Display.error("MainPresenter", 47, myException);
                        }
                    }
                });

        WebMain.injector.getEventBus().addHandler(CloseDataViewEvent.type, new CloseDataViewEventHandler() {

            @Override
            public void onCloseDataView(CloseDataViewEvent event) {

                try {

                    closeDataView(event.getDataViewUuid(), event.isAbort());
                    getView().exitMode(Mode.ANALYSIS);
                    SecurityBanner.displayBanner(null);

                } catch (Exception myException) {

                    Display.error("MainPresenter", 48, myException);
                }
            }
        });

        WebMain.injector.getEventBus().addHandler(EnterAdminModeEvent.type, new EnterAdminModeEventHandler() {

            @Override
            public void onEnterAdminMode(EnterAdminModeEvent eventIn) {

                try {

                    AdministrationView sysAdminWidget = new AdministrationView();
                    getView().enterMode(Mode.ADMIN, sysAdminWidget);
                    getView().replaceMenuBar(CentrifugeConstantsLocator.get().applicationToolbar_systemAdministration());

                } catch (Exception myException) {

                    Display.error("MainPresenter", 49, myException);
                }
            }
        });

        WebMain.injector.getEventBus().addHandler(ExitAdminModeEvent.type, new ExitAdminModeEventHandler() {

            @Override
            public void onExitAdminMode(ExitAdminModeEvent eventIn) {

                try {

                    getView().exitMode(Mode.ADMIN);
                    getView().showApplicationToolbar();
                } catch (Exception myException) {

                    Display.error("MainPresenter", 50, myException);
                }
            }
        });

        WebMain.injector.getEventBus().addHandler(EnterSharingModeEvent.type, new EnterSharingModeEventHandler() {

            @Override
            public void onEnterSharingMode(EnterSharingModeEvent eventIn) {

                try {

                    ResourceSharingView resourceSharingWidget = new ResourceSharingView();
                    if (MainView.Status.NEED_DISPLAY.equals(getView().enterMode(Mode.SHARING, null))) {

                        getView().enterMode(Mode.SHARING, resourceSharingWidget);
                    }
                    getView().replaceMenuBar(CentrifugeConstantsLocator.get().applicationToolbar_manageResources());

                } catch (Exception myException) {

                    Display.error("MainPresenter", 51, myException);
                }
            }
        });

        WebMain.injector.getEventBus().addHandler(ExitSharingModeEvent.type, new ExitSharingModeEventHandler() {

            @Override
            public void onExitSharingMode(ExitSharingModeEvent eventIn) {

                try {

                    getView().exitMode(Mode.SHARING);
                    getView().showApplicationToolbar();
                } catch (Exception myException) {

                    Display.error("MainPresenter", 52, myException);
                }
            }
        });

        WebMain.injector.getEventBus().addHandler(EnterSourceEditModeEvent.type, new EnterSourceEditModeEventHandler() {

            @Override
            public void onEnterSourceEditMode(EnterSourceEditModeEvent eventIn) {

                SourceEditDialog myDialog = eventIn.getParentDialog();
                try {

                    DataSourceEditorPresenter mySourceEditPresenter = eventIn.getPresenter();

                    if (null != mySourceEditPresenter) {

                        _openningDataview = false;
                        mySourceEditPresenter.setParent(myDialog);
                        getView().enterMode(Mode.DATA_SOURCE_EDITOR, mySourceEditPresenter);
                        getView().replaceMenuBar(mySourceEditPresenter.getName());
                    }

                } catch (Exception myException) {

                    if (null != myDialog) {

                        myDialog.abort(myException);

                    } else {

                        Display.error("MainPresenter", 53, myException);
                    }
                }
            }
        });

        WebMain.injector.getEventBus().addHandler(ExitSourceEditModeEvent.type, new ExitSourceEditModeEventHandler() {

            @Override
            public void onExitSourceEditMode(ExitSourceEditModeEvent eventIn) {

                try {
                    WebMain.injector.getMainPresenter().setEditingResource(null);
                    editingResource = null;

                    String myTitleBar = eventIn.getTitleBar();
                    boolean myRefresh = eventIn.getRefresh();

                    getView().exitMode(Mode.DATA_SOURCE_EDITOR);
                    if (null != myTitleBar) {

                        getView().replaceMenuBar(myTitleBar);

                    } else {
                        getView().showApplicationToolbar();
                    }
                    if (myRefresh) {

                        returnToRefresh();
                    }

                } catch (Exception myException) {

                    Display.error("MainPresenter", 54, myException);
                }
            }
        });

        WebMain.injector.getEventBus().addHandler(ReopenDataViewEvent.type, new ReopenDataViewEventHandler() {

            @Override
            public void onDataViewReopen(ReopenDataViewEvent eventIn) {

                try {

                    AbstractDataViewPresenter myAnyPresenter = getDataViewPresenter(true);
//                    DataViewPresenter myPresenter = (DataViewPresenter)getDataViewPresenter(false);
                    OpenDataViewEvent myOpenEvent = /*(null != myPresenter)
                                                        ? eventIn.getOpenEvent(myPresenter.getUuid(),
                                                                                myPresenter.getActiveWorksheetId())
                                                        : */eventIn.getOpenEvent();
                    if (null != myAnyPresenter) {

                        closeDataView(myAnyPresenter.getUuid(), false);
                        getView().exitMode(Mode.ANALYSIS);
                    }
                    WebMain.injector.getEventBus().fireEvent(myOpenEvent);

                } catch (Exception myException) {

                    Display.error("MainPresenter", 55, myException);
                }
            }
        });

        WebMain.injector.getEventBus().addHandler(DataViewNameChangeEvent.type, new DataViewNameChangeEventHandler() {

            @Override
            public void onDataViewNameChange(DataViewNameChangeEvent eventIn) {


                try {

                    if (null != dataViewPresenter) {

                        dataViewPresenter.renameDataView(eventIn.getUuid(), eventIn.getName(), eventIn.getRemarks());
                    }

                } catch (Exception myException) {

                    Display.error("MainPresenter", 56, myException);
                }
            }

        });

    }

    private void openDataViewToWorksheet(String dvUuid, String wsUuid){

        dataViewPresenter = new DataViewPresenter(dvUuid);
        GWT.log("Opening dataview with UUID: " + dvUuid.toString());
        hideMask();
        _openningDataview = true;
        mask = new MaskDialog(CentrifugeConstantsLocator.get().mainPresenter_loadingDataview());
        mask.show();

        dataViewPresenter.onLoad(new DataViewLoadingCallback() {

            @Override
            public void onCallback(long countIn, boolean moreDataIn) {
                getView().enterMode(Mode.ANALYSIS, dataViewPresenter);
                if (null != wsUuid) {

                    List<WorksheetPresenter> worksheetPresenters = dataViewPresenter.getWorksheetPresenters();
                    worksheetPresenters.forEach(
                            worksheetPresenter -> {
                                if (worksheetPresenter.getName().equals(wsUuid)) {
                                    dataViewPresenter.changeWorksheet(worksheetPresenter);
                                }
                            }
                    );
                }
                _openningDataview = false;
                hideMask();
            }

            @Override
            public void onCallback(boolean moreDataIn) {

                onCallback(-1, moreDataIn);
            }
        });
    }

    private void openDataView(DataView dataViewIn, long countIn, boolean moreDataIn){

        openDataViewToWorksheet(dataViewIn, countIn, moreDataIn, null);
    }

    private void openDataViewToWorksheet(DataView dataViewIn, long countIn, boolean moreDataIn, String wsUuid){

        String myUuid = dataViewIn.getUuid();
        openDataViewToWorksheet(myUuid, wsUuid);
    }

	private void openDataView(String uuidIn) {
		dataViewPresenter = new DataViewPresenter(uuidIn);
		GWT.log("Opening dataview with UUID: " + uuidIn.toString());
		hideMask();
		_openningDataview = true;
		mask = new MaskDialog(CentrifugeConstantsLocator.get().mainPresenter_loadingDataview());
		mask.show();
		dataViewPresenter.onLoad(new DataViewLoadingCallback() {

            @Override
            public void onCallback(long countIn, boolean moreDataIn) {
                getView().enterMode(Mode.ANALYSIS, dataViewPresenter);
                _openningDataview = false;
                hideMask();
            }

            @Override
            public void onCallback(boolean moreDataIn) {

                onCallback(-1, moreDataIn);
            }
        });
	}

	private void openDataView(DataView dataView) {
		dataViewPresenter = new DataViewPresenter(dataView.getUuid());
		GWT.log("Opening dataview with UUID: " + dataView.getUuid().toString());
		hideMask();
		_openningDataview = true;
		mask = new MaskDialog(CentrifugeConstantsLocator.get().mainPresenter_loadingDataview());
		mask.show();
        _openningDataview = true;
        //                            mask = new MaskDialog(CentrifugeConstantsLocator.get().mainPresenter_loadingDataview());
        //                            mask.show();
        Scheduler.get().scheduleFixedDelay(new Scheduler.RepeatingCommand() {
            @Override
            public boolean execute() {
                getView().enterMode(Mode.ANALYSIS, dataViewPresenter);
                _openningDataview = false;
                hideMask();
                return false;
            }
        }, 120);
        
        //We already have dataview so don't call onLoad
        dataViewPresenter.openDataview(dataView);
    }


    private void openDataView(String uuidIn, LayoutType layoutType, Map<String, Integer> params) {
        getView().hideApplicationToolbar();
        directedPresenter = new DirectedPresenter(uuidIn, layoutType, params);
        GWT.log("Opening dataview with UUID: " + uuidIn.toString());

        hideMask();

        _openningDataview = true;
        mask = new MaskDialog(CentrifugeConstantsLocator.get().mainPresenter_loading());
        mask.show();
        directedPresenter.onLoad(new DataViewLoadingCallback() {

            @Override
            public void onCallback(long countIn, boolean moreDataIn) {

                try {

                    getView().enterMode(Mode.VIZ, directedPresenter);
                    hideMask();
                    _openningDataview = false;

                } catch (Exception myException) {

                    hideMask();
                    Display.error("MainPresenter", 57, myException);
                }
            }

            @Override
            public void onCallback(boolean moreDataIn) {

                onCallback(-1, moreDataIn);
            }
        });
    }


    private void openDataView(Response<String, DataView> response, LayoutType layoutType, Map<String, Integer> params) {
        getView().hideApplicationToolbar();
		directedPresenter = new DirectedPresenter(response.getResult().getUuid(), layoutType, params);
		GWT.log("Opening dataview with UUID: " + response.getResult().getUuid());

		
		_openningDataview = true;
        directedPresenter.getHandlerOpenDataViewResponse().onSuccess(response);
        getView().enterMode(Mode.VIZ, directedPresenter);
        _openningDataview = false;
        hideMask();
	}

	private void closePresenters() {

		_openningDataview = false;
		if (null != dataViewPresenter) {

			dataViewPresenter.close();
		}
		dataViewPresenter = null;
	}

    public void returnToRefresh() {

        if (null != dataViewPresenter) {
            if(refreshDataViewDialog == null || refreshDataViewDialog.isHidden()
                    || !dataViewPresenter.getUuid().equals(refreshDataViewDialog.getPresenter().getUuid()))
            refreshDataViewDialog = new RefreshDataViewDialog(dataViewPresenter);
            refreshDataViewDialog.show();

        } else {

            closePresenters();
        }
    }

    public void refreshDataViewList() {

        getView().refreshDataViewList();
    }

	private void closeDataView(String uuidIn, boolean abortIn) {
		_openningDataview = false;
		if ((null != uuidIn) && !abortIn) {

			//WebMain.injector.getVortex().execute(DataViewActionServiceProtocol.class, UUID.randomUUID().toString()).closeDataview(uuidIn);
	        VortexFuture<Response<String, DataView>> myVortexFuture = WebMain.injector.getVortex().createFuture();

	        //We close and assign a random uuid to get around the DV conflict check
			myVortexFuture.execute(DataViewActionServiceProtocol.class, CsiUUID.randomUUID()).closeDataview(uuidIn);
		}
        if (null != dataViewPresenter) {
            dataViewPresenter.close();
            dataViewPresenter = null;
        }
        SecurityBanner.displayBanner(null);
	}

	public MainView getView() {
		if (mainView == null) {
			mainView = new MainView(this);
		}
		return mainView;
	}

    public void startCentrifuge() {
        // TODO: move somewhere smarter
        String dvUuid = Location.getParameter("dvuuid");
        dvUuid = checkDecode(dvUuid);
        String dvName = checkDecode(Location.getParameter("dvName"));
        String userName = checkDecode(Location.getParameter("userName"));
        String template = checkDecode(Location.getParameter("template"));
        _annotationMode = checkDecode(Location.getParameter("annotation"));

        _requestVizParams = parseVizParams();
        _requestLayout = checkDecode(Location.getParameter("layout"));

        if(!Strings.isNullOrEmpty(_requestLayout) && Strings.isNullOrEmpty(template)){

            if(!Strings.isNullOrEmpty(dvName)){
                if (!Strings.isNullOrEmpty(_annotationMode)) {
                    openDataViewByName(dvName, userName, _requestVizParams, _requestLayout, _annotationMode);
                    return;
                } else {
                    openDataViewByName(dvName, userName, _requestVizParams, _requestLayout, "true");
                    return;
                }
            } else {
                WebMain.injector.getEventBus().fireEvent(new OpenDirectedViewEvent(dvUuid, LayoutType.valueOf(_requestLayout.toUpperCase()), _requestVizParams));
                return;
            }
        }

        if (!Strings.isNullOrEmpty(dvUuid)) {
            getView().showApplicationToolbar();
            WebMain.injector.getEventBus().fireEvent(new OpenDataViewEvent(dvUuid));
            return;
        }

        if (!Strings.isNullOrEmpty(template)) {

            _requestDataviewName = checkDecode(Location.getParameter("dvName"));
            String dvRemarks = checkDecode(Location.getParameter("dvRemarks"));
            String paramCountString = checkDecode(Location.getParameter("paramCount"));
            String replace = checkDecode(Location.getParameter("replace"));
            List<LaunchParam> parameterData = Lists.newArrayList();
            try {
                int paramCount = Integer.valueOf(paramCountString);
                for (int i = 0; i < paramCount; i++) {
                    String paramString = checkDecode(Location.getParameter("param" + (i + 1)));
                    String[] parts = paramString.split("=", 2);
                    LaunchParam launchParam = new LaunchParam(checkDecode(parts[0]), Lists.newArrayList(checkDecodeArray(parts[1].split(","))));
                    parameterData.add(launchParam);
                }

            } catch (NumberFormatException e) {

            }

            boolean forceIn = false;
            try{
                if(!Strings.isNullOrEmpty(replace)){
                    forceIn = Boolean.parseBoolean(replace);
                }
            } catch(Exception exception){
                //TODO: Figure out if I should display error or just ignore.

            }

            
            
            _launchRequest = new LaunchRequest(null, template, _requestDataviewName, dvRemarks, parameterData, null, forceIn, false);
            if(!Strings.isNullOrEmpty(_requestDataviewName) && !forceIn){
                VortexFuture<Response<String, Map<String, DataView>>> dataviewRequestFuture = WebMain.injector.getVortex().createFuture();
                dataviewRequestFuture.addEventHandler(getDataViewsByNameResponse);

                try {
                    DataviewRequest request = new DataviewRequest();
                    request.setUserName(userName);
                    request.setDataviewName(dvName);
                    request.setAccessMode(AclControlType.READ);
                    dataviewRequestFuture.execute(DataViewActionServiceProtocol.class).getDataviewsByName(request);
                    hideMask();
                    mask = new MaskDialog(CentrifugeConstantsLocator.get().mainPresenter_validatingRequest());
                    mask.show();
                } catch (CentrifugeException e) {
                    hideMask();
                    getView().hideApplicationToolbar();
                    ErrorDialog dialog = new ErrorDialog(e.getMessage());
                    dialog.show();
                }

            } else {
                try {
                    mask = new MaskDialog(CentrifugeConstantsLocator.get().mainPresenter_loadingDataview());
                    mask.show();
                    VortexFuture<Response<String, DataView>> myVortexFuture = WebMain.injector.getVortex().createFuture();
                    myVortexFuture.addEventHandler(launchUrlTemplateResponse);
                    myVortexFuture.execute(DataViewActionServiceProtocol.class).launchUrlTemplate(_launchRequest);

                } catch (Exception myException) {
                    hideMask();
                    Display.error("MainPresenter", 58, myException);
                }
            }

            return;
        }

        if(!Strings.isNullOrEmpty(dvName)){
            getView().showApplicationToolbar();
            openDataViewByName(dvName, userName, null, null, null);
            return;
        }
        getView().enterMode(Mode.GET_STARTED, null);
        getView().showApplicationToolbar();
    }
    
    public SourceEditDialog createSourceEditDialog(DataSourceEditorPresenter presenterIn, AdHocInterface priorDialogIn,
            String titleIn, String helpIn, String resourceUuid) throws CentrifugeException {
        sourceEditDialog = new SourceEditDialog(presenterIn, priorDialogIn, titleIn, helpIn);
        this.editingResource = resourceUuid;
        return sourceEditDialog;
    }
    
    public SourceEditDialog getSourceEditDialog() {
        return sourceEditDialog;
    }

    public void setRefreshDialog(RefreshDataViewDialog refresh) {
        this.refreshDataViewDialog = refresh;
    }

    private ClickHandler continueOpen = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {

            if (null != myActiveEvent) {

                openOrCancelDataView(myActiveEvent.getDataView(), myActiveEvent.getDataViewUuid());
                myActiveEvent = null;
            }
        }
    };

    private ClickHandler cancelOpen = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {

            if (null != myActiveEvent) {

                try {

                    VortexFuture<String> myVortexFuture = WebMain.injector.getVortex().createFuture();
                    myVortexFuture.addEventHandler(new VortexEventHandler<String>() {
                        @Override
                        public void onSuccess(String result) {
                            ApplicationToolbarLocator.getInstance().abortDataView();
//                            getStartedView.reloadData();
                        }
                        @Override
                        public boolean onError(Throwable t) {
                            ApplicationToolbarLocator.getInstance().abortDataView();
//                            getStartedView.reloadData();
                            return false;
                        }
                        @Override
                        public void onUpdate(int taskProgess, String taskMessage) { /* IGNORE */ }
                        @Override
                        public void onCancel() { /* IGNORE */ }
                    });
                    myVortexFuture.execute(DataViewActionServiceProtocol.class).deleteDataView(myActiveEvent.getDataViewUuid());

                } catch (Exception myException) {

                    Display.error("MainPresenter", 59, myException);
                }
                myActiveEvent = null;
            }
        }
    };

    public String getEditingResource() {
        return editingResource;
    }

    public void setEditingResource(String editingResource) {
        this.editingResource = editingResource;
    }

    public String getAnnotationMode() { return _annotationMode; }
}
