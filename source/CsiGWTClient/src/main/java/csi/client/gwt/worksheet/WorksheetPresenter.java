package csi.client.gwt.worksheet;

import com.google.common.base.Strings;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.ui.HasName;
import com.sencha.gxt.widget.core.client.Window;
import csi.client.gwt.WebMain;
import csi.client.gwt.dataview.DataViewPresenter;
import csi.client.gwt.util.name.UniqueNameUtil;
import csi.client.gwt.viz.Visualization;
import csi.client.gwt.viz.VisualizationFactory;
import csi.client.gwt.viz.map.presenter.MapPresenter;
import csi.client.gwt.viz.shared.chrome.panel.VizPanel;
import csi.client.gwt.viz.shared.publish.SnapshotPublishDialog;
import csi.client.gwt.viz.shared.settings.SettingsActionCallback;
import csi.client.gwt.viz.shared.settings.VisualizationSettingsPresenter;
import csi.client.gwt.viz.viewer.Viewer;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.Callback;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.worksheet.layout.window.VisualizationWindow;
import csi.client.gwt.worksheet.layout.window.WindowLayout;
import csi.server.common.model.visualization.VisualizationDef;
import csi.server.common.model.visualization.VisualizationType;
import csi.server.common.model.worksheet.WorksheetDef;
import csi.server.common.service.api.PublishingActionsServiceProtocol;
import csi.server.common.service.api.VisualizationActionsServiceProtocol;
import csi.shared.core.imaging.ImagingRequest;
import csi.shared.core.publish.SnapshotImagingRequest;
import csi.shared.core.publish.SnapshotImagingResponse;
import csi.shared.core.publish.SnapshotPublishRequest;
import csi.shared.core.publish.SnapshotType;

import java.util.ArrayList;
import java.util.List;

public class WorksheetPresenter implements HasName {

    private DataViewPresenter dataViewPresenter;
    private WorksheetDef worksheet;
    private WorksheetView view;
    private List<Visualization> visualizations = new ArrayList<Visualization>();

    public WorksheetPresenter(DataViewPresenter dataViewPresenter, WorksheetDef worksheetDef) {
        this.dataViewPresenter = dataViewPresenter;
        this.worksheet = worksheetDef;
        view = new WindowLayout(this);
    }

    public WorksheetView getView() {
        return view;
    }

    public String getName() {
        return worksheet.getWorksheetName();
    }

    public Integer getColor() {
        return worksheet.getWorksheetColor();
    }

    public void setName(String name) {
        // same process as name
        rename(name);
    }

    public String getUuid() {
        return worksheet.getUuid();
    }

    public void setupVisualizations() {
        for (VisualizationDef vizDef : worksheet.getVisualizations()) {

            final VisualizationDef vizfin = vizDef;
            Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {

                @Override
                public void execute() {
                    createVisualization(vizfin);
                }

            });
        }
    }

    private Visualization createVisualization(VisualizationDef vizDef) {
        // Must be registered before visualizations are created because rel-graph refers to the data view registry
        // on create!
        dataViewPresenter.register(vizDef);
        Visualization visualization = VisualizationFactory.create(dataViewPresenter, vizDef);
        visualizations.add(visualization);
        return visualization;
    }

    public List<String> getWorksheetNames(){

    	List<String> names = new ArrayList<String>();

    	for(WorksheetPresenter presenter: dataViewPresenter.getWorksheetPresenters()){
    		names.add(presenter.getName());
    	}

    	return names;
    }

    public void move(String vizUuid, String targetWorksheetName){

    	Visualization vizToMove = null;
    	VisualizationDef vizDefToMove = null;

    	for (VisualizationDef vizDef : worksheet.getVisualizations()) {
            if (vizDef.getUuid().equals(vizUuid)) {
                vizDefToMove = vizDef;
                break;
            }
        }

    	if(vizDefToMove != null){
	    	for (Visualization viz : visualizations) {
	            if (viz.getUuid().equals(vizDefToMove.getUuid())) {
	                vizToMove = viz;
	                break;
	            }
	        }

	    	if(vizToMove != null){
	    		for(WorksheetPresenter presenter: dataViewPresenter.getWorksheetPresenters()){

	    			if(presenter.getName().equals(targetWorksheetName)){
	    				vizToMove.saveSettings(false);
	    		    	dataViewPresenter.moveViz(vizDefToMove, presenter, this);
	    		    	dataViewPresenter.changeWorksheet(presenter);
	    		    	break;
	    			}

	    		}

	    	}
    	}
    }



    public void delete(VisualizationDef vizDef) {
        worksheet.getVisualizations().remove(vizDef);
        Visualization toDelete = null;
        for (Visualization viz : visualizations) {
            if (viz.getUuid().equals(vizDef.getUuid())) {
                toDelete = viz;
                break;
            }
        }
        if (toDelete != null) {
            getVisualizations().remove(toDelete);
            getView().remove(toDelete);
        }
    }

    public List<Visualization> getVisualizations() {
        return visualizations;
    }

    public WorksheetDef getWorksheet() {
        return worksheet;
    }

    /**
     * @param type Type of visualization that is being requested.
     */
    public void createNewVisualization(VisualizationType type) {
        {//cancel potential directed broadcast
            WebMain.injector.getMainPresenter().getDataViewPresenter(true).getBroadcastManager().endSendTo();
        }
        VisualizationSettingsPresenter presenter = VisualizationFactory.create(dataViewPresenter.getDataView(), this,
                type, new SettingsActionCallback<VisualizationDef>() {

                    @Override
                    public void onSaveComplete(VisualizationDef vizDef, boolean suppressLoadAfterSave) {
                        dataViewPresenter.addVisualization(vizDef, WorksheetPresenter.this);
                    }

                    ;

                    @Override
                    public void onCancel() {
                        // noop
                    }
                }
        );
        presenter.show();
    }

    public void addVisualization(VisualizationDef vizDef) {
        worksheet.getVisualizations().add(vizDef);
        Visualization visualization = createVisualization(vizDef);
        view.add(visualization);
    }

/*    *//**
     * Works similar to addVisualization, but does not register with the dataview
     * This is used for existing visualizations that are already registered.
     *
     * @param vizDef
     *//*
    public void receiveVisualization(VisualizationDef vizDef) {

        worksheet.getVisualizations().add(vizDef);
        Visualization visualization = VisualizationFactory.create(dataViewPresenter, vizDef);
        visualizations.add(visualization);
        view.add(visualization);

    	VisualizationWindow window = ((VisualizationWindow) ((VizPanel) visualization.getChrome()).getFrameProvider());

    	//TODO: would like to make a way so that the viz could keep its previous size.
    	window.setHeight(300);
    	window.setWidth(400);
    }*/

    public void receiveCopyVisualization(VisualizationDef vizDef, int width, int height) {

    	Visualization visualization = createVisualization(vizDef);
        worksheet.getVisualizations().add(vizDef);
        view.add(visualization);

    	final VisualizationWindow window = ((VisualizationWindow) ((VizPanel) visualization.getChrome()).getFrameProvider());
        ((VizPanel) visualization.getChrome()).hideMenu();
        dataViewPresenter.getDataView().getMeta().getModelDef().addVisualization(vizDef);
    	//TODO: would like to make a way so that the viz could keep its previous size.

        final int myWidth = width;
        final int myHeight = height;
        window.setPixelSize(myWidth, myHeight);

        Scheduler.get().scheduleDeferred(() -> window.setPixelSize(myWidth, myHeight));
    }

    public void rename(String name) {
        if (name.equals(worksheet.getWorksheetName())) {
            return;
        }

        try {

            String uniqueName = UniqueNameUtil.getDistinctName(UniqueNameUtil.getWorksheetNames(dataViewPresenter), name);
            WebMain.injector.getVortex().execute(VisualizationActionsServiceProtocol.class)
                    .setWorksheetName(dataViewPresenter.getUuid(), worksheet.getUuid(), uniqueName);
            worksheet.setWorksheetName(uniqueName);
            dataViewPresenter.renameWorksheet(this, uniqueName);

        } catch (Exception myException) {

            Dialog.showException(myException);
        }
    }

    public void setColor(Integer color) {
        if(worksheet.getWorksheetColor() != null) {
            if (Integer.compare(color, worksheet.getWorksheetColor()) == 0) {
                return;
            }
        }
        try {
            WebMain.injector.getVortex().execute(VisualizationActionsServiceProtocol.class)
                    .setWorksheetColor(dataViewPresenter.getUuid(), worksheet.getUuid(), color);
            worksheet.setWorksheetColor(color);
            dataViewPresenter.colorWorksheet(this, color);
        } catch(Exception myException) {
            Dialog.showException(myException);
        }
    }

    public void publish() {
        List<Visualization> eligibleVisualizations = new ArrayList<Visualization>();
        for (Visualization vz : getVisualizations()) {
            if (!isMinimized(vz) && vz.isImagingCapable()) {
                eligibleVisualizations.add(vz);
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
                    publishRequest.setMetaDescription("Snapshot of worksheet " + getName()); //$NON-NLS-1$
                    publishRequest.setSnapshotType(SnapshotType.WORKSHEET);
                    publishRequest.setDataViewUuid(dataViewPresenter.getUuid());

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

    public boolean isMinimized(Visualization visualization) {
        return getView().isMinimized(visualization);
    }

    public void add(Window w) {
        view.add(w);

    }

	public void openMoveDialog(WorksheetPresenter worksheetPresenter, String uuid) {
		WorksheetMoveDialog moveDialog = new WorksheetMoveDialog(worksheetPresenter, uuid);

		moveDialog.show();
	}

	public void copyVisualization(String vizUuid, String targetWorksheetName) {
        if (Strings.isNullOrEmpty(vizUuid)) {
            return;
        }
        if (Strings.isNullOrEmpty(targetWorksheetName)) {
            return;
        }

        final VisualizationDef vizDefToMove = getVisualizationDef(vizUuid);
        if (vizDefToMove == null) {
            return;
        }

        final Visualization vizToMove = getVisualization(vizDefToMove);
        if(vizToMove == null){
            return;
        }

        final WorksheetPresenter targetWorksheetPresenter = getWorksheetPresenter(targetWorksheetName);
        if(targetWorksheetPresenter == null){
            return;
        }
        VortexFuture<Void> future; 
        if (vizToMove instanceof MapPresenter) {
        	future = ((MapPresenter)vizToMove).saveSettingsOnly(false);
        } else {
        	future = vizToMove.saveSettings(false, false, false);
        }
        final WorksheetPresenter sourceWorksheetPresenter = this;
        future.addEventHandler(new AbstractVortexEventHandler<Void>() {
            @Override
            public void onSuccess(Void result) {
                dataViewPresenter.copyViz(vizDefToMove, targetWorksheetPresenter, sourceWorksheetPresenter);
                dataViewPresenter.changeWorksheet(targetWorksheetPresenter);

            }
        });
    }

    protected WorksheetPresenter getWorksheetPresenter(String targetWorksheetName) {
        WorksheetPresenter targetWorksheetPresenter = null;
        for (WorksheetPresenter presenter : dataViewPresenter.getWorksheetPresenters()) {
            if (presenter.getName().equals(targetWorksheetName)) {
                targetWorksheetPresenter = presenter;
            }
        }
        return targetWorksheetPresenter;
    }

    protected Visualization getVisualization(VisualizationDef vizDefToMove) {
        Visualization vizToMove = null;
        for (Visualization viz : visualizations) {
            if (viz.getUuid().equals(vizDefToMove.getUuid())) {
                vizToMove = viz;
                break;
            }
        }
        return vizToMove;
    }

    protected VisualizationDef getVisualizationDef(String vizUuid) {
        VisualizationDef vizDefToMove = null;
        for (VisualizationDef vizDef : worksheet.getVisualizations()) {
            if (vizDef.getUuid().equals(vizUuid)) {
                vizDefToMove = vizDef;
                break;
            }
        }
        return vizDefToMove;
    }

    public void openCopyDialog(WorksheetPresenter worksheetPresenter, String uuid) {

		WorksheetCopyDialog copyDialog = new WorksheetCopyDialog(worksheetPresenter, uuid);

		copyDialog.show();
	}

    public boolean isReadOnly() {

        return (null != dataViewPresenter) ? dataViewPresenter.isReadOnly() : true;
    }

    public Viewer getViewer() {
        return view.getViewer();
    }
}
