package csi.client.gwt.viz.shared.menu;

import csi.client.gwt.WebMain;
import csi.client.gwt.dataview.AbstractDataViewPresenter;
import csi.client.gwt.dataview.DataViewRegistry;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.Visualization;
import csi.client.gwt.vortex.Callback;
import csi.client.gwt.widget.boot.InfoDialog;
import csi.server.common.model.broadcast.BroadcastRequestType;
import csi.server.common.service.api.VisualizationActionsServiceProtocol;

public class BroadcastMenuManager<V extends Visualization> extends AbstractMenuManager<V> {

    private AbstractDataViewPresenter dataViewPresenter;
    private AbstractMenuEventHandler<V, AbstractMenuManager<V>> broadcastInclusion;
    private AbstractMenuEventHandler<V, AbstractMenuManager<V>> broadcastExclusion;
    private AbstractMenuEventHandler<V, AbstractMenuManager<V>> broadcastReplace;
    private AbstractMenuEventHandler<V, AbstractMenuManager<V>> broadcastAddTo;
    private AbstractMenuEventHandler<V, AbstractMenuManager<V>> broadcastRemove;
    private AbstractMenuEventHandler<V, AbstractMenuManager<V>> broadcastClear;
    private AbstractMenuEventHandler<V, AbstractMenuManager<V>> broadcastClearSelection;
    private AbstractMenuEventHandler<V, AbstractMenuManager<V>> broadcastClearAllSelection;
    private AbstractMenuEventHandler<V, AbstractMenuManager<V>> broadcastClearAll;
    private AbstractMenuEventHandler<V, AbstractMenuManager<V>> broadcastListen;
    private AbstractMenuEventHandler<V, AbstractMenuManager<V>> broadcastDirect;
    private AbstractMenuEventHandler<V, AbstractMenuManager<V>> broadcastClearEverything;

    public BroadcastMenuManager(final V viz) {
        super(viz);
        dataViewPresenter = DataViewRegistry.getInstance().dataViewPresenterForVisualization(viz.getUuid());
        initBroadcastDirect();
        initBroadcastDisplay();
        initBroadcastHide();
        initBroadcastReplace();
        initBroadcastAdd();
        initBroadcastRemove();
        initBroadcastClearSelection();
        initBroadcastClear();
        initBroadcastClearAllSelection();
        initBroadcastClearAll();
        initBroadcastClearEverything();
        initBroadcastListen();
    }

    @Override
    public void registerMenus(boolean limitedMenu) {
        register(MenuKey.BROADCAST_DIRECT, broadcastDirect);
        register(MenuKey.BROADCAST_INCLUSION, broadcastInclusion);
        addSeperatorAboveKey(MenuKey.BROADCAST_INCLUSION);
        register(MenuKey.BROADCAST_EXCLUSION, broadcastExclusion);
        register(MenuKey.BROADCAST_REPLACE, broadcastReplace);
        addSeperatorAboveKey(MenuKey.BROADCAST_REPLACE);
        register(MenuKey.BROADCAST_ADDTO, broadcastAddTo);
        register(MenuKey.BROADCAST_REMOVE, broadcastRemove);
        register(MenuKey.CLEAR_SELECTION, broadcastClearSelection);
        addSeperatorAboveKey(MenuKey.CLEAR_SELECTION);
        register(MenuKey.CLEAR_BROADCAST, broadcastClear);
        register(MenuKey.CLEAR_ALL_SELECTION, broadcastClearAllSelection);
        addSeperatorAboveKey(MenuKey.CLEAR_ALL_SELECTION);
        register(MenuKey.CLEAR_ALL_BROADCASTS, broadcastClearAll);
        register(MenuKey.CLEAR_EVERYTHING, broadcastClearEverything);
        register(MenuKey.LISTEN_FOR_BROADCAST, broadcastListen);
        addSeperatorAboveKey(MenuKey.LISTEN_FOR_BROADCAST);

        initListening();
    }

    private void initBroadcastListen() {
        broadcastListen = new AbstractMenuEventHandler<V, AbstractMenuManager<V>>(getPresenter(), this) {
            @Override
            public void onMenuEvent(CsiMenuEvent event) {
                if (getPresenter().isBroadcastListener()) {
                    getPresenter().setBroadcastListener(false);
                    getPresenter().getChrome().getMenu().unCheckedMenuItem(MenuKey.LISTEN_FOR_BROADCAST);
                    getPresenter().getChrome().disableBroadcastListener();
                } else {
                    getPresenter().setBroadcastListener(true);
                    getPresenter().getChrome().getMenu().checkedMenuItem(MenuKey.LISTEN_FOR_BROADCAST);
                    getPresenter().getChrome().enableBroadcastListener();
                }
            }
        };
    }

    private void initBroadcastClearAll() {
        broadcastClearAll = new AbstractMenuEventHandler<V, AbstractMenuManager<V>>(getPresenter(), this) {
            @Override
            public void onMenuEvent(CsiMenuEvent event) {
                dataViewPresenter.getBroadcastManager().clearAllFilters();
            }
        };
    }

    private void initBroadcastClearAllSelection() {
        broadcastClearAllSelection = new AbstractMenuEventHandler<V, AbstractMenuManager<V>>(getPresenter(), this) {

            @Override
            public void onMenuEvent(CsiMenuEvent event) {
                dataViewPresenter.getBroadcastManager().clearAllSelection(getPresenter());
            }
        };
    }

    private void initBroadcastClearEverything() {
        broadcastClearEverything = new AbstractMenuEventHandler<V, AbstractMenuManager<V>>(getPresenter(), this) {

            @Override
            public void onMenuEvent(CsiMenuEvent event) {
                dataViewPresenter.getBroadcastManager().clearEverything(getPresenter());
            }
        };
    }

    private void initBroadcastClear() {
        broadcastClear = new AbstractMenuEventHandler<V, AbstractMenuManager<V>>(getPresenter(), this) {

            @Override
            public void onMenuEvent(CsiMenuEvent event) {

                dataViewPresenter.getBroadcastManager().clearFilter(getPresenter());
            }
        };
    }

    private void initBroadcastClearSelection() {
        broadcastClearSelection = new AbstractMenuEventHandler<V, AbstractMenuManager<V>>(getPresenter(), this) {

            @Override
            public void onMenuEvent(CsiMenuEvent event) {

                dataViewPresenter.getBroadcastManager().clearSelection(getPresenter());
            }
        };
    }
    
    private class BroadcastMenuEventHandler extends AbstractMenuEventHandler<V, AbstractMenuManager<V>> {
    	Visualization visualization;
    	Callback<Void> callback;

		public BroadcastMenuEventHandler(V presenter, AbstractMenuManager<V> menuManager, Callback<Void> callback) {
			super(presenter, menuManager);
			this.visualization = (Visualization) presenter; 
			this.callback = callback;
		}
		
		@Override
        public void onMenuEvent(CsiMenuEvent event) {
        	visualization.saveViewStateToVisualizationDef();
        	if (visualization instanceof SelectionOnlyOnServer) {
        		WebMain.injector.getVortex().execute(new Callback<Boolean>() {
        			@Override
        			public void onSuccess(Boolean hasSelection) {
        				if (hasSelection) {
        					proceed();
        				} else {
        					displayError();
        				}
        			}

        		}, VisualizationActionsServiceProtocol.class).isSelectionAvailable(visualization.getDataViewUuid(), visualization.getVisualizationDef());
        		
        	} else {
                if(getPresenter().hasSelection()){
                    proceed();
                } else {
                	displayError();
                }
        	}
        }

		private void proceed() {
			callback.onSuccess(null);
		}
	
		private void displayError() {
			new InfoDialog(CentrifugeConstantsLocator.get().broadcastMenuManager_title(), 
					CentrifugeConstantsLocator.get().broadcastMenuManager_message()).show();
		}

    }

    private void initBroadcastRemove() {
    	broadcastRemove = new BroadcastMenuEventHandler(getPresenter(), this, new Callback<Void>() {

			@Override
			public void onSuccess(Void result) {
				dataViewPresenter.getBroadcastManager().broadcastSelection(BroadcastRequestType.SELECTION_REMOVE, getPresenter());
			}
    		
    	}); 
    }

    private void initBroadcastAdd() {
    	broadcastAddTo = new BroadcastMenuEventHandler(getPresenter(), this, new Callback<Void>() {

			@Override
			public void onSuccess(Void result) {
				dataViewPresenter.getBroadcastManager().broadcastSelection(BroadcastRequestType.SELECTION_ADD, getPresenter());
			}
    		
    	}); 
    }

    private void initBroadcastReplace() {
    	broadcastReplace = new BroadcastMenuEventHandler(getPresenter(), this, new Callback<Void>() {

			@Override
			public void onSuccess(Void result) {
				dataViewPresenter.getBroadcastManager().broadcastSelection(BroadcastRequestType.SELECTION_REPLACE, getPresenter());
			}
    		
    	}); 
    }

    private void initBroadcastHide() {
    	broadcastExclusion = new BroadcastMenuEventHandler(getPresenter(), this, new Callback<Void>() {

			@Override
			public void onSuccess(Void result) {
				dataViewPresenter.getBroadcastManager().broadcastFilter(BroadcastRequestType.FILTER_HIDE, getPresenter());
			}
    		
    	}); 
    }

    private void initBroadcastDisplay() {
    	broadcastInclusion = new BroadcastMenuEventHandler(getPresenter(), this, new Callback<Void>() {

			@Override
			public void onSuccess(Void result) {
				dataViewPresenter.getBroadcastManager().broadcastFilter(BroadcastRequestType.FILTER_DISPLAY, getPresenter());
			}
    		
    	}); 
    }

    private void initListening() {
        if (getPresenter().isBroadcastListener()) {
            getPresenter().getChrome().getMenu().checkedMenuItem(MenuKey.LISTEN_FOR_BROADCAST);
            getPresenter().getChrome().enableBroadcastListener();
        } else {
            getPresenter().getChrome().getMenu().unCheckedMenuItem(MenuKey.LISTEN_FOR_BROADCAST);
            getPresenter().getChrome().disableBroadcastListener();
        }
    }

	@Override
	public void registerPreloadMenus(boolean limitedMenu) {
		// TODO Auto-generated method stub
		
	}

    private void initBroadcastDirect() {
        broadcastDirect = new BroadcastMenuEventHandler(getPresenter(), this, new Callback<Void>() {

            @Override
            public void onSuccess(Void result) {
                dataViewPresenter.getBroadcastManager().sendTo(getPresenter());
            }

        });
    }
}
