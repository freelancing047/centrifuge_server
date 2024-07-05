package csi.client.gwt.theme.editor.graph;

import java.util.ArrayList;
import java.util.List;

import csi.client.gwt.WebMain;
import csi.client.gwt.theme.editor.ThemeEditor;
import csi.client.gwt.theme.editor.ThemeEditorPresenter;
import csi.client.gwt.theme.editor.graph.links.LinkPresenter;
import csi.client.gwt.theme.editor.graph.nodes.NodePresenter;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.Dialog;
import csi.server.common.graphics.shapes.ShapeType;
import csi.server.common.model.themes.Theme;
import csi.server.common.model.themes.VisualItemStyle;
import csi.server.common.model.themes.graph.GraphTheme;
import csi.server.common.model.themes.graph.LinkStyle;
import csi.server.common.model.themes.graph.NodeStyle;
import csi.server.common.model.themes.map.AssociationStyle;
import csi.server.common.model.themes.map.MapTheme;
import csi.server.common.model.themes.map.PlaceStyle;
import csi.server.common.service.api.ThemeActionsServiceProtocol;
import csi.server.common.util.ValuePair;

public class GraphThemeEditorPresenter implements ThemeEditor{

    private GraphTheme graphTheme;
    private GraphThemeEditorView view;
    private NodePresenter nodePresenter = null;
    private LinkPresenter linkPresenter = null;
    private ThemeEditorPresenter presenter;
    private List<ValuePair<String, VisualItemStyle>> conflictList = null;
    
    public GraphThemeEditorPresenter(ThemeEditorPresenter presenter){
        this.presenter = presenter;
    }
    
    @Override
    public void edit(Theme theme) {
        graphTheme = (GraphTheme) theme;
        graphTheme.removeConflicts(graphTheme.getNodeOverFlow());
        graphTheme.removeConflicts(graphTheme.getLinkOverFlow());
        setNodePresenter(new NodePresenter(this, presenter.getFieldNames()));
        setLinkPresenter(new LinkPresenter(this, presenter.getFieldNames()));
        initialize(graphTheme);
    }


    private void initialize(GraphTheme graphTheme) {
        if(view == null){
            view = new GraphThemeEditorView(this);
            
        }
        
        view.display(graphTheme);
        view.show();
    }

    public GraphTheme getParentTheme() {

        return graphTheme;
    }

    public NodeStyle findNodeConflict(String itemIn) {

        return (null != graphTheme) ? graphTheme.findNodeConflict(itemIn) : null;
    }

    public LinkStyle findLinkConflict(String itemIn) {

        return (null != graphTheme) ? graphTheme.findLinkConflict(itemIn) : null;
    }

    public NodePresenter getNodePresenter() {
        return nodePresenter;
    }

    public void setNodePresenter(NodePresenter nodePresenter) {
        this.nodePresenter = nodePresenter;
    }

    public LinkPresenter getLinkPresenter() {
        return linkPresenter;
    }

    public void setLinkPresenter(LinkPresenter linkPresenter) {
        this.linkPresenter = linkPresenter;
    }

    public void deleteNodeStyle(NodeStyle nodeStyle) {
        if(graphTheme.getBundleStyle() != null && graphTheme.getBundleStyle().equals(nodeStyle)){
            graphTheme.setBundleStyle(null);
        }
        graphTheme.getNodeStyles().remove(nodeStyle);
    }

    public void editNodeStyle(NodeStyle nodeStyle) {
        if(nodeStyle == null){
            nodePresenter.create();
        } else {
            nodePresenter.edit(nodeStyle);
        }
    }

    public void saveModel(List<NodeStyle> nodeStyles, List<LinkStyle> linkStyles, String name) {

        graphTheme.getNodeStyles().clear();
        graphTheme.getNodeStyles().addAll(nodeStyles);
        graphTheme.setName(name);
        graphTheme.removeConflicts(conflictList);
        graphTheme.resetMaps();
        
        //graphTheme.setLinkStyles(linkStyles);
        
        VortexFuture<Void> vortexFuture = WebMain.injector.getVortex().createFuture();
        try {

            vortexFuture.execute(ThemeActionsServiceProtocol.class).saveTheme(graphTheme);

        } catch (Exception myException) {

            Dialog.showException(myException);
        }
        VortexEventHandler<Void> handler = new AbstractVortexEventHandler<Void>() {

            @Override
            public boolean onError(Throwable t) {
                Dialog.showException(t);
                return true;
            }

            @Override
            public void onSuccess(Void v) {
                presenter.populateThemeNames();
            }};
        vortexFuture.addEventHandler(handler);
    }

    public void discardChanges() {

        graphTheme.resetMaps();
    }

    public void addNodeStyle(NodeStyle nodeStyle) {
        
        boolean update = false;
        int index = 0;
        for(int ii=0; ii<graphTheme.getNodeStyles().size(); ii++){
            NodeStyle nodeStyles = graphTheme.getNodeStyles().get(ii);
            if(nodeStyles.getUuid().equals(nodeStyle.getUuid())){
                index = ii;
                update = true;
                break;
            }
        }
        
        if(update){
            graphTheme.getNodeStyles().remove(index);
        }
            graphTheme.getNodeStyles().add(nodeStyle);
            
        view.display(graphTheme);
        //view.display(graphTheme);
    }

    public void setBundleStyle(NodeStyle bundleStyle) {
        graphTheme.setBundleStyle(bundleStyle);
    }

    public void deleteLinkStyle(LinkStyle linkStyle) {
        graphTheme.getLinkStyles().remove(linkStyle);
    }

    public void editLinkStyle(LinkStyle linkStyle) {
        if(linkStyle == null){
            linkPresenter.create();
        } else {
            linkPresenter.edit(linkStyle);
        }
    }

    public void addLinkStyle(LinkStyle linkStyle) {

        boolean update = false;
        int index = 0;
        for(int ii=0; ii<graphTheme.getLinkStyles().size(); ii++){
            LinkStyle linkStyles = graphTheme.getLinkStyles().get(ii);
            if(linkStyles.getUuid().equals(linkStyle.getUuid())){
                index = ii;
                update = true;
                break;
            }
        }
        
        if(update){
            graphTheme.getLinkStyles().remove(index);
        }
            graphTheme.getLinkStyles().add(linkStyle);
            
        view.display(graphTheme);
    }

    public void setDefaultShape(ShapeType selectedItem) {
        graphTheme.setDefaultShape(selectedItem);
    }

    public void setName(String text) {
        graphTheme.setName(text);
    }


}
