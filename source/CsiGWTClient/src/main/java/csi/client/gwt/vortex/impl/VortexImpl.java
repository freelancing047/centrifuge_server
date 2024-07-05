/**
 * Copyright 2013 Centrifuge Systems, Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package csi.client.gwt.vortex.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

import csi.client.gwt.WebMain;
import csi.client.gwt.csiwizard.WizardInterface;
import csi.client.gwt.csiwizard.dialogs.SourceEditDialog;
import csi.client.gwt.dataview.AbstractDataViewPresenter;
import csi.client.gwt.dataview.DataViewPresenter;
import csi.client.gwt.dataview.directed.DirectedPresenter;
import csi.client.gwt.events.OpenDataViewEvent;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.mainapp.MainPresenter;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.Callback;
import csi.client.gwt.vortex.CsiVortexRequestBuilder;
import csi.client.gwt.vortex.ExceptionHandler;
import csi.client.gwt.vortex.Vortex;
import csi.client.gwt.vortex.VortexClientFilter;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.vortex.VortexServiceProvider;
import csi.client.gwt.widget.boot.CsiModal;
import csi.client.gwt.widget.boot.DataviewConflictDialog;
import csi.client.gwt.widget.boot.WarningDialog;
import csi.server.common.model.CsiUUID;
import csi.shared.core.Constants;
import csi.shared.gwt.vortex.VortexRequest;
import csi.shared.gwt.vortex.VortexResponse;
import csi.shared.gwt.vortex.VortexRpcDispatcher;
import csi.shared.gwt.vortex.VortexRpcDispatcherAsync;
import csi.shared.gwt.vortex.VortexService;
import csi.shared.gwt.vortex.impl.SerializableValueImpl;

/**
 * @author Centrifuge Systems, Inc.
 */
public class VortexImpl implements Vortex, VortexSpi, VortexServiceProvider, VortexDispatchSender {


//    
//    private final String REFRESH = i18n.dialog_RefreshButton();
//    private final String CONFLICT_DIALOG_TITLE = i18n.dialog_WarningTitle();
//    private final String CONFLICT_MSG = i18n.dialogConflict();
//    private final String REOPEN_CONFLICT_MSG = i18n.dialogConflictReopen();
//    private final String REFRESH_CONFLICT_MSG = i18n.dialogConflictRefresh();
    private static HashMap<String, VortexFuture> futures = Maps.newHashMap();
    private final String clientId = CsiUUID.randomUUID();
    private Scheduler.RepeatingCommand messangerOverwatch = new Scheduler.RepeatingCommand() {
        @Override
        public boolean execute() {
            if(new Date().getTime()-lastMessage>180000){
                getMessages.execute();
            }
            return true;
        }
    };
    private long lastMessage = new Date().getTime();
    private DataviewConflictDialog dialog;

    {
        Scheduler.get().scheduleFixedPeriod(messangerOverwatch,5000);
    }

    private Scheduler.ScheduledCommand getMessages = new Scheduler.ScheduledCommand() {
        int[] retry_times = { 100, 500, 2000, 5000 };
        int retry_index = 0;

        @Override
        public void execute() {
            lastMessage = new Date().getTime();
            dispatcher.getMessages(clientId, new AsyncCallback<ArrayList<VortexResponse>>() {
                @Override
                public void onFailure(Throwable caught) {
                    int retry_time = retry_times[retry_index];
                    Scheduler.get().scheduleFixedDelay(new Scheduler.RepeatingCommand() {
                        @Override
                        public boolean execute() {
                            getMessages.execute();
                            return false;
                        }
                    }, retry_time);
                    if (retry_index < retry_times.length - 1) {
                        retry_index++;
                    }
                    //                    throw new RuntimeException(caught);
                }

                @Override
                public void onSuccess(ArrayList<VortexResponse> result) {
                    //                    logger.severe(String.valueOf(result.size()));

                    try{
                        for (VortexResponse response : result) {

                            try{
                                //                        logger.severe(String.valueOf(response.getTaskId()));
                                //The future should not be removed if the response is only an update.
                                VortexFuture future = futures.get(response.getTaskId());
                                if (future != null) {
                                    switch (response.getTaskStatus()) {
                                    case TASK_STATUS_NEW:
                                        //Should not occur;
                                        break;
                                    case TASK_STATUS_RUNNING:
                                        //should not occur;
                                        break;
                                    case TASK_STATUS_UPDATE:
                                        //want to fire update if future is an updateablefuture;
                                        future.fireUpdate(response);
                                        break;
                                    case TASK_STATUS_COMPLETE:
                                        futures.remove(response.getTaskId());
                                        //FIXME: Error should only come through the TASK_STATUS_ERROR case.
                                        if (response.hasException()) {
                                            future.fireFailure(response.getException());
                                        } else {
                                            future.fireSuccess(response.getResponse().getValue());
                                        }
                                        break;
                                    case TASK_STATUS_ERROR:
                                        futures.remove(response.getTaskId());
                                        Throwable exception;
                                        if (response.hasException()) {
                                            exception = response.getException();
                                        } else {
                                            exception = null;
                                        }
                                        future.fireFailure(exception);
                                        break;
                                    case TASK_STATUS_CANCELED:
                                        futures.remove(response.getTaskId());
                                        future.fireCancel();
                                        break;
                                    case TASK_STATUS_CONFLICT:
                                        futures.remove(response.getTaskId());
                                        String conflictUuid = response.getTaskMessage();
                                        future.fireCancel();

                                        MainPresenter mainPresenter = WebMain.injector.getMainPresenter();
                                        AbstractDataViewPresenter dataViewPresenter = null;
                                        String uuid = response.getTaskMessage();
                                        
                                        boolean isEditResource = false;
                                        boolean isDirected = false;
                                        if (mainPresenter != null) {
                                            isEditResource = mainPresenter.getEditingResource() != null && mainPresenter.getEditingResource().equals(conflictUuid);
                                            if(isEditResource) {
                                                
                                                SourceEditDialog sourceEditDialog = WebMain.injector.getMainPresenter().getSourceEditDialog();
                                                sourceEditDialog.abort();
                                                CsiModal.clearAll();

                                            }
                                            dataViewPresenter = mainPresenter.getDataViewPresenter(true);
                                        }

                                        if (dataViewPresenter != null) {

                                            isDirected = dataViewPresenter instanceof DirectedPresenter;
                                            uuid = dataViewPresenter.getUuid();

                                            if(conflictUuid.equals(uuid)) {
                                                //Can't close directed
                                                if(!isDirected) {
                                                    closeDataview();
                                                    closeFullScreenViz();
                                                }
                                            }
                                            

                                        }
                                        

                                        showConflictDialog(uuid, isDirected, isEditResource);
                                        
                                        break;
                                    default:
                                        break;
                                    }
                                }
                            } catch(Exception e){
                                retry_index = 1;
                                GWT.getUncaughtExceptionHandler().onUncaughtException(e);
                            }
                        }
                    } finally{
                        retry_index = 0;
                        //NOTE: Run it again.
                        getMessages.execute();
                    }
                }

                /**
                 * 
                 *  Bit of a hack, but not a lot of good ways to retrieve FullScreenViz panel
                 * 
                 */
                private void closeFullScreenViz() {
                    int count = RootPanel.get().getWidgetCount();
                    for(int ii=count-1; ii>=0; ii--) {
                        Widget widget = RootPanel.get().getWidget(ii);
                        if(widget != null && widget instanceof FlowPanel) {
                            String styleName = widget.getStyleName();
                            if(styleName.equals(Constants.UIConstants.Styles.WINDOW_FULL_SCREEN_CONTAINER)) {
                                widget.removeFromParent();
                            }
                        }
                    }
                    
                }

                private void showConflictDialog(String uuid, boolean isDirected, boolean isSourceEdit) {
                    CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
                    CsiModal.clearAll();
                    if(dialog != null) {
                        dialog.hide();
                    }
                    if(isDirected) {
                        dialog = new DataviewConflictDialog(i18n.dialog_WarningTitle(), i18n.dialogConflict() +" "+ i18n.dialogConflictRefresh());
                        dialog.setActionText(i18n.dialog_RefreshButton());
                        dialog.addClickHandler(new ClickHandler() {

                            @Override
                            public void onClick(ClickEvent event) {
                                Window.Location.reload();
                            }});
                    } else {
                        if(uuid == null || isSourceEdit) {
                            dialog = new DataviewConflictDialog(i18n.dialog_WarningTitle(), i18n.dialogConflict());
                            dialog.removeActionButton();
                        } else {
                            dialog = new DataviewConflictDialog(i18n.dialog_WarningTitle(), i18n.dialogConflict() + " " + i18n.dialogConflictReopen());
                            dialog.addClickHandler(new ClickHandler() {
                                
                                @Override
                                public void onClick(ClickEvent event) {
                                    WebMain.injector.getEventBus().fireEvent(new OpenDataViewEvent(uuid));
                                }});
                        }
                    }

                    dialog.show();
                }
                
                private void closeDataview() {
                    CsiModal.clearAll();
                    WebMain.injector.getMainPresenter().clearDataViewDisplay();
                    //ApplicationToolbarLocator.getInstance().abortDataView();
                }
                
                
            });
        }
    };



    private VortexRpcDispatcherAsync dispatcher = GWT.create(VortexRpcDispatcher.class);
    private VortexServiceStubFactory stubFactory = GWT.create(VortexServiceStubFactory.class);

    private List<VortexClientFilter> filters = new ArrayList<VortexClientFilter>();
    private CsiVortexRequestBuilder requestBuilder = new CsiVortexRequestBuilder(this);

    // This works because of the fundamental single-threaded nature of Javascript execution.
    private String currentMethodName;

    public VortexImpl() {
        ((ServiceDefTarget) dispatcher).setRpcRequestBuilder(requestBuilder);
        Scheduler.get().scheduleDeferred(getMessages);
    }

    @Override
    public void setRPCServiceEntryPointURL(String url) {
        ((ServiceDefTarget) dispatcher).setServiceEntryPoint(url);
    }

    @Override
    public void addRPCServiceFilter(VortexClientFilter filter) {
        filters.add(filter);
    }

    @Override
    public Vortex withMeta(String name, Serializable value) {
        VortexShell shell = new VortexShell(this);
        return shell.withMeta(name, value);
    }

    @Override
    public <E extends VortexService> E execute(Class<E> clz) {
        return execute(new Callback<Object>() {

            @Override
            public void onSuccess(Object returnValue) {
                // Noop
            }
        }, clz);
    }

    @Override
    public <E extends VortexService, R> E execute(Callback<R> callback, Class<E> clz) {
        return execute(new ExceptionHandler() {

            @Override
            public boolean handle(Throwable t) {
                return true;
            }
        }, callback, clz);
    }

    @Override
    public <E extends VortexService, R> E execute(ExceptionHandler handler, Callback<R> callback, Class<E> clz) {
        return execute(new HashMap<String, SerializableValueImpl>(), handler, callback, clz);
    }

    /**
     * All the execute() variants call this and we wrap the parameters passed here in a call to VortexFuture.
     */
    public <E extends VortexService, R> E execute(Map<String, SerializableValueImpl> meta,
            final ExceptionHandler handler, final Callback<R> callback, Class<E> clz) {
        VortexFuture<R> future = create(meta);
        future.addEventHandler(new AbstractVortexEventHandler<R>() {

            @Override
            public void onSuccess(R result) {
                callback.onSuccess((R) result);
            }

            @Override
            public boolean onError(Throwable t) {
                return handler.handle(t);
            }
        });
        return future.execute(clz);
    }

    @Override
    public <R> VortexFuture<R> createFuture() {
        return create(new HashMap<String, SerializableValueImpl>());
    }

    public <R> VortexFuture<R> create(Map<String, SerializableValueImpl> headers) {
        VortextFutureImpl<R> future = new VortextFutureImpl<R>();
        future.setVortex(this);
        return future;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <V extends VortexService, R> V executeForFuture(Map<String, SerializableValueImpl> meta,
            VortexFutureSpi<R> vortexFuture, Class<V> clz) {
        VortexService stub = stubFactory.create(clz);
        ((AbstractVortexEnabledStub<R>) stub).setDispatchParameters(vortexFuture, meta, this);
        return (V) stub;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <V extends VortexService, R> V executeForFuture(Map<String, SerializableValueImpl> meta,
            VortexFutureSpi<R> vortexFuture, Class<V> clz, String resourceUuid) {
        VortexService stub = stubFactory.create(clz);
        ((AbstractVortexEnabledStub<R>) stub).setDispatchParameters(vortexFuture, meta, this, resourceUuid);
        return (V) stub;
    }

    @Override
    public String getCurrentMethodName() {
        return currentMethodName;
    }

    @Override
    public <R> void dispatch(final VortexRequest request, final VortexFutureSpi<R> vortexFuture) {
        final List<VortexClientFilter> executedFilters = new ArrayList<VortexClientFilter>();
                
        boolean filtersAborted = false;
        // Call client side RPC filters.
        for (VortexClientFilter filter : filters) {
            if (!filter.onStart(request)) {
                filtersAborted = true;
                break;
            }
            executedFilters.add(filter);
        }

        if (!filtersAborted) {
            //NOTE: this is set so that the request method signature is included in the headers.
            currentMethodName = request.getMethodSignature();
            request.setClientId(clientId);
            try {
                MainPresenter mainPresenter = WebMain.injector.getMainPresenter();
                AbstractDataViewPresenter dataViewPresenter = null;
                String uuid=null;
                String editResourceUuid = null;
                if (mainPresenter != null) {
                    dataViewPresenter = mainPresenter.getDataViewPresenter(true);
                    editResourceUuid = mainPresenter.getEditingResource();
                }
                if (dataViewPresenter != null) {
                    uuid = dataViewPresenter.getUuid();
                }
                
                //Decides whether this request goes on template/dataview semaphore
                if(editResourceUuid == null) {
                    request.setExecutionPoolId(uuid);
                } else {
                    request.setExecutionPoolId(editResourceUuid);
                }
                
                //We check if this was set by the vortex call(openDataview, getTemplate), if not, we use the semaphored one
                if(request.getResourceUuid() == null) {
                    //This is where the request is considered in regards to template/dataview
                    request.setResourceUuid(request.getExecutionPoolId());
                }
            }catch (Exception ignored){
                //TODO: error handling? this will be the case for calls before dataview is open.
                //at the moment this will gracefully degrade to single execution pool behavior.
            }
            String taskId = CsiUUID.randomUUID();
            vortexFuture.setTaskId(taskId);
            request.setTaskId(taskId);
            //keep track of the futures so that we can fire on success when we get back the response.
            futures.put(taskId, vortexFuture);
            dispatcher.dispatch(request, new AsyncCallback<VortexResponse>() {

                @SuppressWarnings("unchecked")
                public void onSuccess(VortexResponse response) {
                    //The expectation is that we get back null, because all vortex calls are now asynchronous.
                }

                public void onFailure(Throwable throwable) {
                    //                    if (vortexFuture.fireFailure(throwable)) {
                    //                        logger.log(Level.WARNING, throwable.getMessage());
                    //                    }
                    //                    onRpcEnd(false, request, null, executedFilters);
                    throw new RuntimeException(throwable);
                }
            });
        } else {
            // One of the filters asked the RPC service to be aborted.
            onRpcEnd(false, request, null, executedFilters);
        }


    }

    /**
     * Note: only those filters that were called during onStart without the
     * chain aborting will be called in onEnd.
     *
     */
    private void onRpcEnd(boolean success, VortexRequest request, VortexResponse response,
            List<VortexClientFilter> filters) {
        for (VortexClientFilter filter : filters) {
            filter.onEnd(success, request, response.getResponse());
        }
    }

    public void cancel(String taskId, AsyncCallback<Void> callback) {
    	dispatcher.cancelTask(taskId, callback);
    }

}
