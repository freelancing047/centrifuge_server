package csi.client.gwt.theme.editor.map.associations;

import java.util.List;

import com.google.gwt.user.client.ui.Image;

import csi.client.gwt.icon.IconUploadPresenter;
import csi.client.gwt.theme.editor.map.MapThemeEditorPresenter;
import csi.client.gwt.viz.graph.GraphImpl;
import csi.server.common.graphics.shapes.ShapeType;
import csi.server.common.model.themes.graph.LinkStyle;
import csi.server.common.model.themes.map.AssociationStyle;
import csi.server.common.model.themes.map.MapTheme;
import csi.server.common.model.themes.map.PlaceStyle;

public class AssociationPresenter {

    AssociationStyleView view;
    MapThemeEditorPresenter presenter;
    IconUploadPresenter iconPresenter;
    private List<String> fieldNames = null;
    private AssociationStyle currentStyle = null;

    public AssociationPresenter(MapThemeEditorPresenter mapThemeEditorPresenter, List<String> list) {
        presenter = mapThemeEditorPresenter;
        iconPresenter = new IconUploadPresenter();
        fieldNames = list;
    }

    public void edit(AssociationStyle associationStyle) {
        currentStyle = associationStyle;
        getView().display(currentStyle);
    }

    public void create(){
        currentStyle = new AssociationStyle();
        getView().display(currentStyle);
    }
    
    public AssociationStyleView getView(){
        if(view == null || view.isClosed()){
            view = new AssociationStyleView(this);
        }
        return view;
    }

    public MapTheme getParentTheme() {

        return (null != presenter) ? presenter.getParentTheme() : null;
    }

    public AssociationStyle getStyle() {

        return currentStyle;
    }

    public AssociationStyle findAssociationConflict(String itemIn) {

        return (null != presenter) ? presenter.findAssociationConflict(itemIn) : null;
    }

    public void updateTheme(AssociationStyle associationStyle) {
        presenter.addAssociationStyle(associationStyle);
    }
    
    public Image getCurrentStyle(AssociationStyle associationStyle){
            return null;
        //return MapImpl.getRenderedIcon(associationStyle.getIconId(), associationStyle.getShape(), associationStyle.getColor(), 1, 1);
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
