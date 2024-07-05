/**
 *  Copyright (c) 2008 Centrifuge Systems, Inc.
 *  All rights reserved.
 *
 *  This software is the confidential and proprietary information of
 *  Centrifuge Systems, Inc. ("Confidential Information").  You shall
 *  not disclose such Confidential Information and shall use it only
 *  in accordance with the terms of the license agreement you entered
 *  into with Centrifuge Systems.
 *
 **/
package csi.client.gwt.dataview;

import com.google.common.collect.Lists;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.ResettableEventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.ui.Widget;
import csi.client.gwt.WebMain;
import csi.client.gwt.events.PublishDataViewEvent;
import csi.client.gwt.events.PublishDataViewEventHandler;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.mainapp.ApplicationToolbar;
import csi.client.gwt.mainapp.ApplicationToolbarLocator;
import csi.client.gwt.util.Display;
import csi.client.gwt.viz.Visualization;
import csi.client.gwt.viz.shared.DeleteVisualizationEvent;
import csi.client.gwt.viz.shared.DeleteVisualizationEventHandler;
import csi.client.gwt.viz.shared.chrome.panel.VizPanel;
import csi.client.gwt.viz.shared.publish.SnapshotPublishDialog;
import csi.client.gwt.viz.viewer.Viewer;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.Callback;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.WarningDialog;
import csi.client.gwt.worksheet.WorksheetColorDialog;
import csi.client.gwt.worksheet.WorksheetNameDialog;
import csi.client.gwt.worksheet.WorksheetPresenter;
import csi.client.gwt.worksheet.layout.window.VisualizationWindow;
import csi.server.common.model.DataModelDef;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.visualization.VisualizationDef;
import csi.server.common.model.worksheet.WorksheetDef;
import csi.server.common.service.api.DataViewActionServiceProtocol;
import csi.server.common.service.api.PublishingActionsServiceProtocol;
import csi.server.common.service.api.VisualizationActionsServiceProtocol;
import csi.shared.core.imaging.ImagingRequest;
import csi.shared.core.publish.SnapshotImagingRequest;
import csi.shared.core.publish.SnapshotImagingResponse;
import csi.shared.core.publish.SnapshotPublishRequest;
import csi.shared.core.publish.SnapshotType;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class DataViewPresenter extends AbstractDataViewPresenter {

    private static final CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
    private final String DEFAULT_WORKSHEET_NAME = i18n.dataViewPresenterDefaultWorksheetName(); //$NON-NLS-1$

    private DataViewWidget view;
    private List<WorksheetPresenter> worksheetPresenters = Lists.newArrayList();
    private EventBus eventBus = new ResettableEventBus(new SimpleEventBus());
    private WorksheetPresenter activeWorksheet;

    private List<HandlerRegistration> handlerRegistrations = new ArrayList<HandlerRegistration>();


    public DataViewPresenter(String dataViewUUID) {
        super();
        this.dataViewUuid = dataViewUUID;
    }

    private ClickHandler moreDataOk = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {

            openDataview(dataView);
        }
    };

    public DataViewPresenter(DataView dataViewIn, Long countIn, Boolean moreDataIn) {
        super();
        if ((null != countIn) && (null != moreDataIn) && moreDataIn) {

            dataView = dataViewIn;
            dataViewUuid = dataView.getUuid();

            Display.warning("More Data Available", "Number of rows truncated to "
                    + Long.toString(countIn) + " row limit as requested.", moreDataOk);
        } else {

            openDataview(dataViewIn);
        }
    }

    @Override
    protected void setup() {
        setupHandlers();
        DataViewRegistry dataViewRegistry = DataViewRegistry.getInstance();
        dataViewRegistry.associatePresenterWithDataView(dataViewUuid, this);

        //FIXME: originally done because open dataview was called twice... probably should remove...
        if(worksheetPresenters.size() == 0){

            // Create worksheets
            for (WorksheetDef worksheet : dataModel.getWorksheets()) {
                createAndAddWorksheetPresenter(worksheet);
            }
        }

        if (worksheetPresenters.size() > 0) {
            if (getActiveWorksheet() == null) {
                createOrGetView().setActiveWorksheet(worksheetPresenters.get(0));
            }
            //Bit of a hack, rename first worksheet of a new dataview to default.
            if(worksheetPresenters.size() == 1){
            	if(worksheetPresenters.get(0).getName().equals(""))
            		worksheetPresenters.get(0).setName(DEFAULT_WORKSHEET_NAME);
            }
        }
        // FIXME: Extreme coupling here. Need to fix this.
        ApplicationToolbarLocator.getInstance().setDataView(this);
    }


    private void setupHandlers() {
        handlerRegistrations.add(WebMain.injector.getEventBus().addHandler(DeleteVisualizationEvent.type,
                new DeleteVisualizationEventHandler() {

                    @Override
                    public void deleteVisualization(final String visualizationUuid) {
                        VortexFuture<Void> vortexFuture = WebMain.injector.getVortex().createFuture();
                        try {

                            vortexFuture.execute(VisualizationActionsServiceProtocol.class).deleteVisualization(getResource().getUuid(), visualizationUuid);
                            vortexFuture.addEventHandler(new AbstractVortexEventHandler<Void>() {
                                @Override
                                public void onSuccess(Void v) {
                                    VisualizationDef vizDef = getResource().getMeta().getModelDef().findVisualizationByUuid(visualizationUuid);
                                    if(vizDef != null) {
                                        getResource().getMeta().getModelDef().removeVisualization(vizDef);
                                        for (WorksheetPresenter presenter : worksheetPresenters) {
                                            presenter.delete(vizDef);
                                        }
                                    }
                                }
                            });

                        } catch (Exception myException) {

                            Display.error("DataViewPresenter", 1, myException);
                        }
                    }
                }));

        handlerRegistrations.add(WebMain.injector.getEventBus().addHandler(PublishDataViewEvent.type,
                new PublishDataViewEventHandler() {

                    @Override
                    public void onPublish(PublishDataViewEvent event) {
                        if (event.getDataViewUuid().equals(dataViewUuid)) {
                            publish();
                        }
                    }
                }));
    }

    protected void publish() {
        List<Visualization> eligibleVisualizations = new ArrayList<Visualization>();
        for (WorksheetPresenter worksheetPresenter : worksheetPresenters) {
            for (Visualization visualization : worksheetPresenter.getVisualizations()) {
                if (visualization.isImagingCapable() && !worksheetPresenter.isMinimized(visualization)) {
                    eligibleVisualizations.add(visualization);
                }
            }
        }
        if (eligibleVisualizations.size() > 0) {
            SnapshotImagingRequest imagingRequest = new SnapshotImagingRequest();
            for (Visualization visualization : eligibleVisualizations) {
                ImagingRequest ir = visualization.getImagingRequest();
                imagingRequest.getImagingRequests().add(ir);
            }

            WebMain.injector.getVortex().execute(new Callback<SnapshotImagingResponse>() {

                @Override
                public void onSuccess(SnapshotImagingResponse result) {
                    SnapshotPublishRequest publishRequest = new SnapshotPublishRequest();
                    publishRequest.setMetaDescription(i18n.dataViewPresenterMetaDescription() + getName()); //$NON-NLS-1$
                    publishRequest.setSnapshotType(SnapshotType.DATAVIEW);
                    publishRequest.setDataViewUuid(dataViewUuid);

                    SnapshotPublishDialog dialog = new SnapshotPublishDialog();
                    dialog.setImagingResponse(result);
                    dialog.setPublishRequest(publishRequest);
                    dialog.show();
                }
            }, PublishingActionsServiceProtocol.class).getImages(imagingRequest);

        } else {
            // FIXME: Feedback - no visualizations eligible.
        }
    }

    private WorksheetPresenter createAndAddWorksheetPresenter(WorksheetDef worksheet) {
        WorksheetPresenter presenter = new WorksheetPresenter(this, worksheet);
        worksheetPresenters.add(presenter);
        presenter.setupVisualizations();
        createOrGetView().addWorksheet(presenter);
        return presenter;
    }


    public void addVisualization(VisualizationDef vizDef, WorksheetPresenter worksheetPresenter) {
        getResource().getMeta().getModelDef().addVisualization(vizDef);
        worksheetPresenter.addVisualization(vizDef);
    }

    public void register(VisualizationDef visualization) {
        DataViewRegistry dataViewRegistry = DataViewRegistry.getInstance();
        dataViewRegistry.associateVisualizationWithDataView(visualization.getUuid(), this);
    }

    private DataViewWidget createOrGetView() {
        if (view == null) {
            view = new DataViewWidget(this);
        }
        return view;
    }

    public Widget getView() {
        return createOrGetView();
    }

    @Override
    public Viewer getViewer() {
        return getActiveWorksheet().getViewer();
    }

    public void createWorksheet() {

        createNewWorksheet(DEFAULT_WORKSHEET_NAME);

//        final WorksheetNameDialog dialog = new WorksheetNameDialog(null);
//        dialog.addClickHandler(new ClickHandler() {
//
//            @Override
//            public void onClick(ClickEvent event) {
//                if (dialog.getName() != null && dialog.getName().trim().length() > 0) {
//                    dialog.hide();
//                } else {
//
//                    Display.error("Worksheet requires a name");
//                }
//            }
//        });
//        dialog.show();

    }

    protected void createNewWorksheet(final String name) {
        WorksheetDef worksheet = new WorksheetDef();
        try {
            WebMain.injector.getVortex().execute(new Callback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    final WorksheetPresenter worksheetPresenter = createAndAddWorksheetPresenter(worksheet);
                    dataModel.getWorksheets().add(worksheet);
                    worksheetPresenter.setName(name);
                    view.setActiveWorksheet(worksheetPresenter);
                }
            }, VisualizationActionsServiceProtocol.class).addWorksheet(worksheet, getUuid());

        } catch (Exception myException) {

            Display.error("DataViewPresenter", 2, myException);
        }
    }

    public void renameWorksheet(final WorksheetPresenter worksheetPresenter) {
        final WorksheetNameDialog dialog = new WorksheetNameDialog(worksheetPresenter.getName());
        dialog.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                renameWorksheet(worksheetPresenter, dialog.getName());
                dialog.hide();
            }
        });
        dialog.show();
    }

    public void renameWorksheet(WorksheetPresenter worksheetPresenter, String name) {
        worksheetPresenter.rename(name);
        view.renameWorksheet(worksheetPresenter);
    }

    public void colorWorksheet(final WorksheetPresenter worksheetPresenter, double bottom, double left, double windowRight) {
        final WorksheetColorDialog dialog = new WorksheetColorDialog(worksheetPresenter);
        dialog.setPosition(bottom, left, windowRight);
        dialog.show();
    }

    public void colorWorksheet(WorksheetPresenter worksheetPresenter, Integer color) {
        worksheetPresenter.setColor(color);
        view.colorWorksheet(worksheetPresenter.getView().asWidget(), color);
    }

    public void deleteWorksheet(final WorksheetPresenter worksheetPresenter) {
        int vizCount = worksheetPresenter.getVisualizations().size();
        String message;
        if (vizCount == 0) {
            message = i18n.dataViewPresenterDeleteConfirmation() + worksheetPresenter.getName() + "?"; //$NON-NLS-1$ //$NON-NLS-2$
        } else if (vizCount == 1) {
            message = i18n.dataViewPresenterWorksheetSingleDeleteConfirmation(worksheetPresenter.getName()); //$NON-NLS-1$
        } else {
            message = i18n.dataViewPresenterWorksheetManyDeleteMessage( worksheetPresenter.getName(), vizCount); //$NON-NLS-1$
        }
        WarningDialog dialog = new WarningDialog(i18n.dataViewPresenterDeleteWorksheetTItle(), message); //$NON-NLS-1$
        dialog.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                try {

                    dataModel.getVisualizations().removeAll(worksheetPresenter.getWorksheet().getVisualizations());
                    dataModel.removeWorksheet(worksheetPresenter.getWorksheet());
                    view.deleteWorksheet(worksheetPresenter);
                    worksheetPresenters.remove(worksheetPresenter);
                    
                    WebMain.injector.getVortex().execute(VisualizationActionsServiceProtocol.class)
                        .removeWorksheet(worksheetPresenter.getWorksheet(), getUuid());
                    

                } catch (Exception myException) {

                    Display.error("DataViewPresenter", 3, myException);
                }
            }
        });
        dialog.show();
    }

    public List<Visualization> getVisualizations() {
        List<Visualization> list = new ArrayList<Visualization>();

        if ((null != worksheetPresenters) && (0 < worksheetPresenters.size())) {

            for (WorksheetPresenter presenter : worksheetPresenters) {
                list.addAll(presenter.getVisualizations());
            }
        }
        return list;
    }

    public void setActiveWorksheet(WorksheetPresenter worksheetPresenter) {
        activeWorksheet = worksheetPresenter;
    }

    public WorksheetPresenter getActiveWorksheet() {
        return activeWorksheet;
    }

    public String getActiveWorksheetId() {
        return (null != activeWorksheet) ? activeWorksheet.getName() : null;
    }

    public void publishWorksheet() {
        if (getActiveWorksheet() != null) {
            getActiveWorksheet().publish();
        }
    }

    public void close() {
        for (HandlerRegistration hr : handlerRegistrations) {
            hr.removeHandler();
        }
        //Found some errors where the view would be removed before handlers
        //Causing mad chaos when closing and opening dataviews.
        Scheduler.get().scheduleDeferred(new ScheduledCommand(){

            @Override
            public void execute() {

                getView().removeFromParent();
            }});

    }

    public List<WorksheetPresenter> getWorksheetPresenters() {
        return worksheetPresenters;
    }

    public void moveViz(final VisualizationDef vizDefToMove, final WorksheetPresenter presenter, final WorksheetPresenter oldPresenter) {

        VortexFuture<VisualizationDef> vortexFuture = WebMain.injector.getVortex().createFuture();
        try {

            vortexFuture.execute(VisualizationActionsServiceProtocol.class).copyVisualization(getResource().getUuid(), vizDefToMove.getUuid(), presenter.getUuid(), true);
            vortexFuture.addEventHandler(new AbstractVortexEventHandler<VisualizationDef>() {
                @Override
                public void onSuccess(VisualizationDef newVizDef) {
                    Visualization oldViz = null;
                    for(Visualization viz: oldPresenter.getVisualizations()){
                        if(viz.getUuid().equals(vizDefToMove.getUuid())){
                            oldViz = viz;
                        }
                    }
//                    newVizDef.getSelection().setFromSelection(oldViz.getVisualizationDef().getSelection());

                    VisualizationWindow oldVizWindow = ((VisualizationWindow) ((VizPanel) oldViz.getChrome()).getFrameProvider());

                    if(oldVizWindow.isFullScreen()){
                        oldVizWindow.restoreFullScreen();
                    }

                    presenter.receiveCopyVisualization(newVizDef, oldVizWindow.getWidth(), oldVizWindow.getHeight());

                    if(oldViz != null){
                        oldViz.delete();
                    }
                }
            });

        } catch (Exception myException) {

            Display.error("DataViewPresenter", 6, myException);
        }

    }

    public void copyViz(final VisualizationDef vizDefToMove, final WorksheetPresenter presenter, final WorksheetPresenter oldPresenter){

        VortexFuture<VisualizationDef> vortexFuture = WebMain.injector.getVortex().createFuture();
        try {

            vortexFuture.execute(VisualizationActionsServiceProtocol.class).copyVisualization(getResource().getUuid(), vizDefToMove.getUuid(), presenter.getUuid(), false);
            vortexFuture.addEventHandler(new AbstractVortexEventHandler<VisualizationDef>() {
                @Override
                public void onSuccess(VisualizationDef vizDef) {

                    Visualization oldViz = null;
                    for(Visualization viz: oldPresenter.getVisualizations()){
                        if(viz.getUuid().equals(vizDefToMove.getUuid())){
                            oldViz = viz;
                            break;
                        }
                    }

                    // Transient stuff that we should retain in the new viz
                    vizDef.getSelection().setFromSelection(oldViz.getVisualizationDef().getSelection());
                    vizDef.setBroadcastListener(oldViz.isBroadcastListener());

//                    vizDef.getSelection().setFromSelection(oldViz.getVisualizationDef().getSelection());

                    VisualizationWindow window = ((VisualizationWindow) ((VizPanel) oldViz.getChrome()).getFrameProvider());
                    ((VizPanel) oldViz.getChrome()).hideMenu();
                    if(window.isFullScreen()){
                    	window.restoreFullScreen();
                    }
                    presenter.receiveCopyVisualization(vizDef, window.getWidth(), window.getHeight());

                }
            });

        } catch (Exception myException) {

            Display.error("DataViewPresenter", 7, myException);
        }

    }

    public void changeWorksheet(WorksheetPresenter presenter){
        this.setActiveWorksheet(presenter);
        this.view.setActiveWorksheet(presenter);
    }

    public void reOrderWorksheet(int newIndex, int oldIndex) {

    	VortexFuture<DataModelDef> vortexFuture = WebMain.injector.getVortex().createFuture();
    	try {

    		vortexFuture.execute(DataViewActionServiceProtocol.class).reorderWorksheet(newIndex, oldIndex, this.getUuid());
    		vortexFuture.addEventHandler(new AbstractVortexEventHandler<DataModelDef>() {
    			@Override
    			public void onSuccess(DataModelDef modelDef) {
    				getDataView().getMeta().setModelDef(modelDef);
    				initDataViewAccess(getDataView());
    			}

    			@Override
    		    public boolean onError(Throwable myException){

    	    		Display.error("DataViewPresenter", 8, myException);
    	    		return false;
    			}
    		});

    	} catch (Exception myException) {

    		Display.error("DataViewPresenter", 9, myException);
    	}
    }
}
