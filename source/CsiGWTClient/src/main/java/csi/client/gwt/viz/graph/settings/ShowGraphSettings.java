package csi.client.gwt.viz.graph.settings;

import java.util.Collection;
import java.util.List;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

import csi.client.gwt.WebMain;
import csi.client.gwt.viz.graph.link.LinkProxy;
import csi.client.gwt.viz.graph.node.NodeProxy;
import csi.client.gwt.viz.graph.settings.GraphSettings.View;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.Modal;
import csi.server.common.dto.SelectionListData.ResourceBasics;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.themes.graph.GraphTheme;
import csi.server.common.model.visualization.VisualizationType;
import csi.server.common.service.api.ThemeActionsServiceProtocol;

public class ShowGraphSettings implements GraphSettings.Presenter, Activity {

    private GraphSettings graphSettings;
    private static final int OPTION_SETS_RESOURCES_LENGTH = "/resources/OptionSets/".length();//NON-NLS

    public ShowGraphSettings(GraphSettings graphSettings) {
        this.graphSettings = graphSettings;
    }

    @Override
    public String mayStop() {
        return null;
    }

    @Override
    public void onCancel() {
    }

    @Override
    public void onStop() {
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        // reset the layers, or is that a different activity?
        // populate fields
        populateThemes();
    }

    private void populateNodeLayer() {
        // List<NodeDef> nodeDefs = graphSettings.getGraphModel().getNodeDefs();
        View view = graphSettings.getView();
        view.clear();


        // set load on startup
        view.setLoadOnStartup(graphSettings.getModel().getLoadOnStartup());

        // set don't load after save
        view.setLoadAfterSave(graphSettings.getModel().getLoadAfterSave());

        // set title
        view.setVisualizationTitle(graphSettings.getModel().getTitle());

        // set current background color
        view.setCurrentBackgroundColor(graphSettings.getModel().getBackgroundColorString());

        // set render threshold
        view.setRenderThreshold(graphSettings.getModel().getRenderThreshold());

        
        if(graphSettings.getCurrentTheme() != null){
            updateView(graphSettings.getCurrentTheme());
        } else {
            graphSettings.getTheme().addEventHandler(new AbstractVortexEventHandler<GraphTheme>() {

                @Override
                public void onSuccess(GraphTheme result) {
                    updateView(result);
                }

            });
        }
        Modal modal = view.getModal();
        if (modal.isVisible() == false) {
            modal.show();
        }
        view.redraw();
    }
    
    public void updateView(GraphTheme result) {
        graphSettings.getView().setTheme(result);
        // need to apply theme to nodes before adding them to view
        View view2 = graphSettings.getView();
        Collection<NodeProxy> nodeProxies = graphSettings.getNodeProxies();
        for (NodeProxy nodeProxy : nodeProxies) {
            nodeProxy.apply(graphSettings);
            view2.addNode(nodeProxy);
        }
        

        Collection<LinkProxy> LinkProxies = graphSettings.getLinkProxies();
        for (LinkProxy linkProxy : LinkProxies) {
            linkProxy.apply(graphSettings);
            view2.addLink(linkProxy);
        }
    }
    private void populateThemes() {
        /*this loads the names of the option sets into the theme selection listbox */
//        VortexFuture<List<String>> optionSetNames = GraphTheme.getThemeNames();
//        optionSetNames.addEventHandler(new AbstractVortexEventHandler<List<String>>() {
//
//            @Override
//            public void onSuccess(List<String> result) {
//                graphSettings.getView().clearThemes();
//                for (String name : result) {
//                    /*copied logic from flex app*/
//                    if (name.contains(".xml")) {//NON-NLS
//                        String displayName = name.substring(OPTION_SETS_RESOURCES_LENGTH, name.length() - 4);
//                        graphSettings.getView().addTheme(displayName);
//                    }
//                }
//                populateNodeLayer();
//            }
//
//            @Override
//            public boolean onError(Throwable t) {
//                // TODO: How do I handle no being able to get a theme;
//                return true;
//            }
//        });
        
        VortexFuture<List<ResourceBasics>> future = WebMain.injector.getVortex().createFuture();
        future.addEventHandler(new AbstractVortexEventHandler<List<ResourceBasics>>() {

            @Override
            public void onSuccess(List<ResourceBasics> results) {
                graphSettings.getView().clearThemes();
                for (ResourceBasics result : results) {
                    graphSettings.getView().addTheme(result);
                    if(result.getName() != null && graphSettings.getThemeUuid() != null 
                            && result.getName().equals(graphSettings.getThemeUuid())){
                        graphSettings.setTheme(result);
                        graphSettings.setThemeUuid(result.getUuid());
                    }
                }
                populateNodeLayer();
            }

            @Override
            public boolean onError(Throwable t) {
                // TODO: How do I handle no being able to get a theme;
                return true;
            }
        });
        try {
            future.execute(ThemeActionsServiceProtocol.class).listThemesByType(VisualizationType.RELGRAPH_V2);
        } catch (CentrifugeException e) {
        };
        
        /*this loads the names of the option sets into the theme selection listbox */
//      VortexFuture<List<ResourceBasics>> optionSetNames = 
//      optionSetNames.addEventHandler(new AbstractVortexEventHandler<List<ResourceBasics>>() {
//
//          @Override
//          public void onSuccess(List<ResourceBasics> results) {
//              graphSettings.getView().clearThemes();
//              for (ResourceBasics result : results) {
//                  graphSettings.getView().addTheme(result.getName());
//              }
//              populateNodeLayer();
//          }
//
//          @Override
//          public boolean onError(Throwable t) {
//              // TODO: How do I handle no being able to get a theme;
//              return true;
//          }
//      });
      
    }
}
