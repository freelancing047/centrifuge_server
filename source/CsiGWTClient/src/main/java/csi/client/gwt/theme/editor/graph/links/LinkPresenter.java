package csi.client.gwt.theme.editor.graph.links;

import java.util.List;

import com.google.gwt.user.client.ui.Image;

import csi.client.gwt.icon.IconUploadPresenter;
import csi.client.gwt.theme.editor.graph.GraphThemeEditorPresenter;
import csi.client.gwt.viz.graph.GraphImpl;
import csi.server.common.graphics.shapes.ShapeType;
import csi.server.common.model.themes.graph.GraphTheme;
import csi.server.common.model.themes.graph.LinkStyle;
import csi.server.common.model.themes.graph.NodeStyle;
import csi.server.common.model.themes.map.MapTheme;
import csi.server.common.model.themes.map.PlaceStyle;

public class LinkPresenter {

    LinkStyleView view;
    GraphThemeEditorPresenter presenter;
    IconUploadPresenter iconPresenter;
    private List<String> fieldNames = null;
    private LinkStyle currentStyle = null;

    public LinkPresenter(GraphThemeEditorPresenter graphThemeEditorPresenter, List<String> list) {
        presenter = graphThemeEditorPresenter;
        iconPresenter = new IconUploadPresenter();
        fieldNames = list;
    }

    public void edit(LinkStyle linkStyle) {
        currentStyle = linkStyle;
        getView().display(currentStyle);
    }

    public void create(){
        currentStyle = new LinkStyle();
        getView().display(currentStyle);
    }
    
    public LinkStyleView getView(){
        if(view == null || view.isClosed()){
            view = new LinkStyleView(this);
        }
        return view;
    }

    public GraphTheme getParentTheme() {

        return (null != presenter) ? presenter.getParentTheme() : null;
    }

    public LinkStyle getStyle() {

        return currentStyle;
    }

    public LinkStyle findLinkConflict(String itemIn) {

        return (null != presenter) ? presenter.findLinkConflict(itemIn) : null;
    }

    public void updateTheme(LinkStyle linkStyle) {
        presenter.addLinkStyle(linkStyle);
    }
    
    public Image getCurrentStyle(LinkStyle linkStyle){
            return null;
        //return GraphImpl.getRenderedIcon(linkStyle.getIconId(), linkStyle.getShape(), linkStyle.getColor(), 1, 1);
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
