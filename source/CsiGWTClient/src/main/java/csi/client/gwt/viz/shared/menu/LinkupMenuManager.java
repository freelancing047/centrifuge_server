package csi.client.gwt.viz.shared.menu;

import java.util.List;

import com.github.gwtbootstrap.client.ui.NavLink;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;

import csi.client.gwt.dataview.AbstractDataViewPresenter;
import csi.client.gwt.dataview.DataViewRegistry;
import csi.client.gwt.dataview.linkup.LinkupSelectionDialog;
import csi.client.gwt.events.DataChangeEvent;
import csi.client.gwt.events.DataChangeEventHandler;
import csi.client.gwt.viz.Visualization;
import csi.server.common.model.linkup.LinkupExtender;
import csi.server.common.model.linkup.LinkupMapDef;
import csi.server.common.model.visualization.VisualizationDef;
import csi.server.common.model.visualization.graph.RelGraphViewDef;

public class LinkupMenuManager<V extends Visualization> extends AbstractMenuManager<V> {
    
    class CsiNavLink extends NavLink {
        
    }
    
    class CsiDynamicMenuClickHandler implements ClickHandler {

        private AbstractDataViewPresenter _dataViewPresenter;
        private V _visualization;
        private String _selectionKey;
        
        public CsiDynamicMenuClickHandler(AbstractDataViewPresenter _dataViewPresenter2, V visualizationIn, String selectionKeyIn) {
            
            _dataViewPresenter = _dataViewPresenter2;
            _visualization = visualizationIn;
            _selectionKey = selectionKeyIn;
        }
        
        public void onClick(ClickEvent eventIn) {
            
            if (null != _selectionKey) {
                
                new LinkupSelectionDialog(_dataViewPresenter, _visualization, _selectionKey);
            } else {
                
                
            }
        }
    }
    private DataChangeEventHandler handleLinkupResponse
    = new DataChangeEventHandler() {

        @Override
        public void onDataChange(DataChangeEvent eventIn) {
            if (eventIn.isSuccess()) {
                if (eventIn.isDelete()) {
                    registerMenus(false);
                } else if (eventIn.isNew()) {
                    registerMenus(false);
                } else if (eventIn.isChange()) {
                    registerMenus(false);
                }
            }
        }
    };

    private AbstractDataViewPresenter _dataViewPresenter;
    private V _visualization;
    private HandlerRegistration _linkupCallbackHandler = null;
    
    public LinkupMenuManager(V vizualizationIn) {
        super(vizualizationIn);
        _visualization = vizualizationIn;
        _dataViewPresenter = DataViewRegistry.getInstance().dataViewPresenterForVisualization(vizualizationIn.getUuid());
        
        // Identify callback handler for server requests involving linkups (add, update, delete)
        _linkupCallbackHandler = _dataViewPresenter.addHandler(handleLinkupResponse, DataChangeEvent.type);
    }

    @Override
    public void registerMenus(boolean limitedMenu) {

        List<LinkupMapDef> myMappings = _dataViewPresenter.getLinkupMappings();
        int myLinkupCount = 0;
        if(myMappings != null){
            myLinkupCount = myMappings.size();
        }
        CsiMenuNav myMenu =  getPresenter().getChrome().getMenu();
        
        myMenu.removeAll(MenuKey.LINKUP);
        myMenu.cancelScrolling(MenuKey.LINKUP);

        if (0 < myLinkupCount) {

            if (12 < myLinkupCount) {

                myMenu.setScrolling(MenuKey.LINKUP, 240);
            }

            for (LinkupMapDef myMapping : myMappings) {

                if (isRelevant(myMapping)) {

                    String myLinkupKey = myMapping.getLinkupName();
                    NavLink myMenuItem = new NavLink(myLinkupKey);

                    myMenuItem.addClickHandler(new CsiDynamicMenuClickHandler(_dataViewPresenter, _visualization, myLinkupKey));
                    myMenu.addMenuItem(MenuKey.LINKUP, myMenuItem);
               }
            }
        }
    }
    
    protected void finalize( ) {
        
        if (null != _linkupCallbackHandler) {
            _linkupCallbackHandler.removeHandler();
        }
    }

	@Override
	public void registerPreloadMenus(boolean limitedMenu) {
		// TODO Auto-generated method stub
		
	}

    private boolean isRelevant(LinkupMapDef mappingIn) {

        if (null != mappingIn) {

            List<LinkupExtender> myExtenders = mappingIn.getLinkupExtenders();

            if ((null != myExtenders) && (0 < myExtenders.size())) {

                VisualizationDef myVizDef = (null != _visualization) ? _visualization.getVisualizationDef() : null;
                String myVizId = ((null != myVizDef) &&(myVizDef instanceof RelGraphViewDef)) ? myVizDef.getLocalId() : null;

                if (null != myVizId) {

                    for (LinkupExtender myExtender : myExtenders) {

                        if ((!myExtender.getIsDisabled()) && myVizId.equals(myExtender.getVizDefId())) {

                            return true;
                        }
                    }
                }

            } else {

                return true;
            }
        }
        return false;
    }
}
