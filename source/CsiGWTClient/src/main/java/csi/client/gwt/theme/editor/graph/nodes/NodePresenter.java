package csi.client.gwt.theme.editor.graph.nodes;

import java.util.List;

import com.google.gwt.user.client.ui.Image;

import csi.client.gwt.theme.editor.graph.GraphThemeEditorPresenter;
import csi.client.gwt.viz.graph.GraphImpl;
import csi.server.common.graphics.shapes.ShapeType;
import csi.server.common.model.themes.graph.GraphTheme;
import csi.server.common.model.themes.graph.NodeStyle;

public class NodePresenter {

    NodeStyleView view;
    GraphThemeEditorPresenter presenter;
    private List<String> fieldNames = null;
    private NodeStyle currentStyle = null;

    public NodePresenter(GraphThemeEditorPresenter graphThemeEditorPresenter, List<String> list) {
        presenter = graphThemeEditorPresenter;
        fieldNames = list;
    }

    public void edit(NodeStyle nodeStyle) {
        currentStyle = nodeStyle;
        getView().display(currentStyle);
    }
    
    public void create(){
        currentStyle = new NodeStyle();
        getView().display(currentStyle);
    }
    
    public NodeStyleView getView(){
        if(view == null || view.isClosed()){
            view = new NodeStyleView(this);
        }
        return view;
    }

    public GraphTheme getParentTheme() {

        return (null != presenter) ? presenter.getParentTheme() : null;
    }

    public NodeStyle getStyle() {

        return currentStyle;
    }

    public NodeStyle findNodeConflict(String itemIn) {

        return (null != presenter) ? presenter.findNodeConflict(itemIn) : null;
    }

    public void updateTheme(NodeStyle nodeStyle) {
        presenter.addNodeStyle(nodeStyle);
    }
    
    public Image getCurrentStyle(NodeStyle nodeStyle){

        return GraphImpl.getRenderedIcon(nodeStyle.getIconId(), nodeStyle.getShape(), nodeStyle.getColor(), 1, 1);
    }
    

    public Image getRenderedStyle(String iconURI, ShapeType shape, int color, int size, double iconScale){
    
        return GraphImpl.getRenderedIcon(iconURI, shape, color, size, iconScale);
    }

    public List<String> getFieldNames() {
        return fieldNames;
    }

    public void setFieldNames(List<String> fieldNames) {
        this.fieldNames = fieldNames;
    }
    

}
