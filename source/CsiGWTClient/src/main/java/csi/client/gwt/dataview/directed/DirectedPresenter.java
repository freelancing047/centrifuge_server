package csi.client.gwt.dataview.directed;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.Window;
import csi.client.gwt.WebMain;
import csi.client.gwt.dataview.AbstractDataViewPresenter;
import csi.client.gwt.dataview.DataViewLoadingCallback;
import csi.client.gwt.dataview.DataViewRegistry;
import csi.client.gwt.events.LayoutType;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.mainapp.ApplicationToolbar;
import csi.client.gwt.mainapp.ApplicationToolbarLocator;
import csi.client.gwt.util.Display;
import csi.client.gwt.util.ResponseHandler;
import csi.client.gwt.viz.Visualization;
import csi.client.gwt.viz.VisualizationFactory;
import csi.client.gwt.viz.viewer.Viewer;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.boot.MaskDialog;
import csi.server.common.dto.Response;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.visualization.VisualizationDef;
import csi.server.common.service.api.DataViewActionServiceProtocol;

import java.util.*;
import java.util.logging.Logger;

public class DirectedPresenter extends AbstractDataViewPresenter {

	//TODO: populate these on start
	private List<Visualization> visualizations = new ArrayList<Visualization>();
	private DirectedLayout 	layout;
	private VizLayout	view;
    private MaskDialog mask;
    boolean maskAlreadyDisplayed = false;

    private RefreshDirectedDialog refresh;


    public DirectedPresenter(String uuidIn, LayoutType layoutType, Map<String, Integer> params) {
		super();
		this.dataViewUuid = uuidIn;
		layout = new DirectedLayout();
		layout.setLayoutType(layoutType);
		layout.setParams(params);
	}

	/**
	 * Creates a different view based on layout type
	 *
	 */
	private void createView() {
		if(view == null){
			if(layout.getLayoutType().equals(LayoutType.FIT))
				view = new FitView(this);
			else{
				view = new SelectView(this);
			}
		}

	}

	/**
	 * Loads the data view for this and calls the callback when it is ready.
	 * @param callbackIn Callback to invoke onLoad
	 */
	public void onLoad(final DataViewLoadingCallback callbackIn) {
		setCallback(callbackIn);

		VortexFuture<Response<String, DataView>> myVortexFuture = WebMain.injector.getVortex().createFuture();

		try {

			myVortexFuture.execute(DataViewActionServiceProtocol.class, dataViewUuid).openDataView(dataViewUuid);
			myVortexFuture.addEventHandler(handlerOpenDataViewResponse);

		} catch (Exception myException) {

			Dialog.showException(myException);
			WebMain.injector.getMainPresenter().hideMask();
			setCallback(null);
            ApplicationToolbarLocator.getInstance().abortDataView();
		}
	}

	public void setup(){
	    try{

            //WebMain.injector.getMainPresenter().hideMask();
            closeMask();
	    	if(mask == null) {
				this.mask = new MaskDialog(CentrifugeConstantsLocator.get().directedPresenterLoading());
				if(!maskAlreadyDisplayed) {
                    mask.show();
                    maskAlreadyDisplayed = true;
                }
			}
    		DataViewRegistry dataViewRegistry = DataViewRegistry.getInstance();
    		dataViewRegistry.associatePresenterWithDataView(dataViewUuid, this);

            createView();
	    } catch(Exception e){
	        closeMask();
	    }

	}

    public void closeMask() {
        if(this.mask != null){
	        this.mask.hide();
	        this.mask.removeFromParent();
	        this.mask = null;
	    }
    }



	private void attachVisualizations(HashMap<Integer, Visualization> unorderedViz) {

		int count = 0;
		int foundItems = 0;
		//Since my map doesn't guarantee order, we enforce it here.
		if(unorderedViz.size() > 0){
			while(foundItems < unorderedViz.size()){

				if(unorderedViz.containsKey(count)){
					visualizations.add(unorderedViz.get(count));
					foundItems++;
				}

				count++;
			}
		}
		if(this.layout.getLayoutType() == LayoutType.FIT){
			Collections.reverse(visualizations);
		}

		final int vizCount = visualizations.size();
		for(final Visualization visualization: visualizations){
		    Scheduler.get().scheduleDeferred(new ScheduledCommand(){
	            @Override
	            public void execute() {
	                view.addVisualization(visualization);
					if(view.getVizCount() >= vizCount){
						view.onResize();

						Scheduler.get().scheduleFixedDelay(new Scheduler.RepeatingCommand() {
							@Override
							public boolean execute() {
								closeMask();
								return false;
							}
						},50);
					}
	            }});
		}

	}

	private void displayVisualization(VisualizationDef vizDef, HashMap<Integer, Visualization> unorderedViz) {
		Map<String, Integer> params = layout.getParams();


		DataViewRegistry dataViewRegistry = DataViewRegistry.getInstance();
		if(params.isEmpty()){
			visualizations.add(VisualizationFactory.create(this, vizDef));
			dataViewRegistry.associateVisualizationWithDataView(vizDef.getUuid(), this);
		}

		if(params.containsKey(vizDef.getUuid())){

			Visualization visualization = VisualizationFactory.create(this, vizDef);
			int index = params.get(vizDef.getUuid());
			unorderedViz.put(index, visualization);
			dataViewRegistry.associateVisualizationWithDataView(vizDef.getUuid(), this);

		} else if(params.containsKey(vizDef.getName())){

			Visualization visualization = VisualizationFactory.create(this, vizDef);
			int index = params.get(vizDef.getName());
			unorderedViz.put(index, visualization);
			dataViewRegistry.associateVisualizationWithDataView(vizDef.getUuid(), this);
		}

	}


	@Override
	public List<Visualization> getVisualizations(){
		return this.visualizations;
	}

	@Override
	public Widget getView() {
	    createView();
		return this.view.asWidget();
	}

	@Override
	public Viewer getViewer() {
		return null;
	}

	private VortexEventHandler<Response<String, DataView>> handlerOpenDataViewResponse
	= new AbstractVortexEventHandler<Response<String, DataView>>() {
		@Override
		public boolean onError(Throwable exceptionIn) {

			WebMain.injector.getMainPresenter().hideMask();

			// Display error message.
			Dialog.showException(exceptionIn);
			setCallback(null);
            ApplicationToolbarLocator.getInstance().abortDataView();
			return false;
		}

		@Override
		public void onSuccess(Response<String, DataView> responseIn) {

            closeMask();
            WebMain.injector.getMainPresenter().hideMask();

			if (responseIn.isAuthorizationRequired()) {

				try {

					Dialog.showCredentialDialogs(WebMain.injector.getMainPresenter().getAuthorizationMap(), responseIn.getAuthorizationList(), processLogon());

				} catch (Exception myException) {

					Dialog.showException(myException);
					setCallback(null);
                    ApplicationToolbarLocator.getInstance().abortDataView();
				}

			} else if (ResponseHandler.isSuccess(responseIn)) {

                initDataViewAccess(responseIn.getResult());
                setup();
				if (dataView.getNeedsRefresh()) {

					refresh = new RefreshDirectedDialog(DirectedPresenter.this);
					refresh.show();

				}  else {

                    if (responseIn.getLimitedData()) {

                        long myRowCount = responseIn.getCount();

                        Display.continueDialog("More Data Available", "Number of rows truncated to "
                                                + Long.toString(myRowCount) + " row limit as requested.",
                                                continueOpen, cancelOpen);

                    } else {

                        if (null != getCallback()) {
                            getCallback().onCallback(false);
							Logger.getLogger("csi.server.business.service.DataViewActionsService").info
									("Dataview successfully opened.");
                        }
                        setCallback(null);
                    }
				}
			} else {

				setCallback(null);
                ApplicationToolbarLocator.getInstance().abortDataView();
                setCallback(null);
                WebMain.injector.getMainPresenter().conditionalAbort();
			}
		}
	};


	@Override
	protected VortexEventHandler<Response<String, DataView>> handleRefreshResponse(){
		return new AbstractVortexEventHandler<Response<String, DataView>>() {

			@Override
			public boolean onError(Throwable myException) {

				Dialog.showException(myException);
				return true;
			}

			@Override
			public void onSuccess(Response<String, DataView> responseIn) {

				if (responseIn.isAuthorizationRequired()) {

					try {

						Dialog.showCredentialDialogs(getAuthorizationMap(), responseIn.getAuthorizationList(), processLogon());

					} catch (Exception myException) {

						Dialog.showException(myException);
					}

				} else if (ResponseHandler.isSuccess(responseIn)) {
					WebMain.injector.getMainPresenter().closeExistingDataViewAndOpenNewOne(dataViewUuid, layout.getParams(), layout.getLayoutType());
				}
			}
		};
	}

	public void loadVisualizations() {

	    
        hideWatchBox();
		HashMap<Integer, Visualization> unorderedViz = new HashMap<Integer, Visualization>();
		for(VisualizationDef vizDef: dataModel.getVisualizations()){
			displayVisualization(vizDef, unorderedViz);
		}
		attachVisualizations(unorderedViz);

	}

	public void add(Window floatingTabWindow) {
		view.add(floatingTabWindow);
	}


}
