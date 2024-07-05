package csi.client.gwt.theme.editor.map.places;

import java.util.List;

import com.google.gwt.user.client.ui.Image;

import csi.client.gwt.icon.IconUploadPresenter;
import csi.client.gwt.theme.editor.map.MapThemeEditorPresenter;
import csi.client.gwt.viz.graph.GraphImpl;
import csi.server.common.graphics.shapes.ShapeType;
import csi.server.common.model.themes.map.AssociationStyle;
import csi.server.common.model.themes.map.MapTheme;
import csi.server.common.model.themes.map.PlaceStyle;

public class PlacePresenter {

    PlaceStyleView view;
    MapThemeEditorPresenter presenter;
    IconUploadPresenter iconPresenter;
    private List<String> fieldNames = null;
    private PlaceStyle currentStyle = null;

    public PlacePresenter(MapThemeEditorPresenter mapThemeEditorPresenter, List<String> list) {
        presenter = mapThemeEditorPresenter;
        iconPresenter = new IconUploadPresenter();
        fieldNames = list;
    }

    public void edit(PlaceStyle placeStyle) {
        currentStyle = placeStyle;
        getView().display(currentStyle);
    }

    public void create(){
        currentStyle = new PlaceStyle();
        getView().display(currentStyle);
    }
    
    public PlaceStyleView getView(){
        if(view == null || view.isClosed()){
            view = new PlaceStyleView(this);
        }
        return view;
    }

    public MapTheme getParentTheme() {

        return (null != presenter) ? presenter.getParentTheme() : null;
    }

    public PlaceStyle getStyle() {

        return currentStyle;
    }

    public PlaceStyle findPlaceConflict(String itemIn) {

        return (null != presenter) ? presenter.findPlaceConflict(itemIn) : null;
    }

    public void updateTheme(PlaceStyle placeStyle) {
        presenter.addPlaceStyle(placeStyle);
    }
    
    public Image getCurrentStyle(PlaceStyle placeStyle){

        return GraphImpl.getRenderedIcon(placeStyle.getIconId(), placeStyle.getShape(), placeStyle.getColor(), 15, 1);
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
