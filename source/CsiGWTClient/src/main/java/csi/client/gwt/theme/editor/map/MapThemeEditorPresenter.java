package csi.client.gwt.theme.editor.map;

import java.util.ArrayList;
import java.util.List;

import csi.client.gwt.WebMain;
import csi.client.gwt.theme.editor.ThemeEditor;
import csi.client.gwt.theme.editor.ThemeEditorPresenter;
import csi.client.gwt.theme.editor.map.associations.AssociationPresenter;
import csi.client.gwt.theme.editor.map.places.PlacePresenter;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.Dialog;
import csi.server.common.graphics.shapes.ShapeType;
import csi.server.common.model.themes.Theme;
import csi.server.common.model.themes.VisualItemStyle;
import csi.server.common.model.themes.map.AssociationStyle;
import csi.server.common.model.themes.map.MapTheme;
import csi.server.common.model.themes.map.PlaceStyle;
import csi.server.common.service.api.ThemeActionsServiceProtocol;
import csi.server.common.util.ValuePair;

public class MapThemeEditorPresenter implements ThemeEditor {

    private MapTheme mapTheme;
    private MapThemeEditorView view;
    private PlacePresenter placePresenter = null;
    private AssociationPresenter associationPresenter = null;
    private ThemeEditorPresenter presenter;
    private List<ValuePair<String, VisualItemStyle>> conflictList = null;

    public MapThemeEditorPresenter(ThemeEditorPresenter themeEditorPresenter) {
        this.presenter = themeEditorPresenter;
    }

    @Override
    public void edit(Theme theme) {

        mapTheme = (MapTheme) theme;
        mapTheme.removeConflicts(mapTheme.getPlaceOverFlow());
        mapTheme.removeConflicts(mapTheme.getAssociationOverFlow());
        setPlacePresenter(new PlacePresenter(this, presenter.getFieldNames()));
        setAssociationPresenter(new AssociationPresenter(this, presenter.getFieldNames()));
        
        if(view == null){
            view = new MapThemeEditorView(this);
            
        }
        
        view.display(mapTheme);
        view.show();

    }

    public MapTheme getParentTheme() {

        return mapTheme;
    }

    public PlaceStyle findPlaceConflict(String itemIn) {

        return (null != mapTheme) ? mapTheme.findPlaceConflict(itemIn) : null;
    }

    public AssociationStyle findAssociationConflict(String itemIn) {

        return (null != mapTheme) ? mapTheme.findAssociationConflict(itemIn) : null;
    }

    public void saveModel(List<PlaceStyle> placeStyles, List<AssociationStyle> associationStyles, String name) {
        mapTheme.getPlaceStyles().clear();
        mapTheme.getPlaceStyles().addAll(placeStyles);
        mapTheme.setName(name);
        mapTheme.removeConflicts(conflictList);
        mapTheme.resetMaps();

        //mapTheme.setAssociationStyles(associationStyles);
        
        VortexFuture<Void> vortexFuture = WebMain.injector.getVortex().createFuture();
        try {

            vortexFuture.execute(ThemeActionsServiceProtocol.class).saveTheme(mapTheme);

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

        mapTheme.resetMaps();
    }

    public void deletePlaceStyle(PlaceStyle placeStyle) {
        if(mapTheme.getBundleStyle() != null && mapTheme.getBundleStyle().equals(placeStyle)){
            mapTheme.setBundleStyle(null);
        }

        mapTheme.getPlaceStyles().remove(placeStyle);
}

    public void editPlaceStyle(PlaceStyle placeStyle) {
        if(placeStyle == null){
            placePresenter.create();
        } else {
            placePresenter.edit(placeStyle);
        }
    } 
    
    public void addPlaceStyle(PlaceStyle placeStyle) {
        boolean update = false;
        int index = 0;
        for(int ii=0; ii<mapTheme.getPlaceStyles().size(); ii++){
            PlaceStyle placeStyles = mapTheme.getPlaceStyles().get(ii);
            if(placeStyles.getUuid().equals(placeStyle.getUuid())){
                index = ii;
                update = true;
                break;
            }
        }
        
        if(update){
            mapTheme.getPlaceStyles().remove(index);
        }
            mapTheme.getPlaceStyles().add(placeStyle);
            
        view.display(mapTheme);
    }

    public void deleteAssociationStyle(AssociationStyle associationStyle) {
        mapTheme.getAssociationStyles().remove(associationStyle);
    }

    public void editAssociationStyle(AssociationStyle associationStyle) {
        if(associationStyle == null){
            associationPresenter.create();
        } else {
            associationPresenter.edit(associationStyle);
        }
    }

    public PlacePresenter getPlacePresenter() {
        return placePresenter;
    }

    public void setPlacePresenter(PlacePresenter placePresenter) {
        this.placePresenter = placePresenter;
    }

    public AssociationPresenter getAssociationPresenter() {
        return associationPresenter;
    }

    public void setAssociationPresenter(AssociationPresenter associationPresenter) {
        this.associationPresenter = associationPresenter;
    }

    public void addAssociationStyle(AssociationStyle associationStyle) {
        boolean update = false;
        int index = 0;
        for(int ii=0; ii<mapTheme.getAssociationStyles().size(); ii++){
            AssociationStyle associationStyles = mapTheme.getAssociationStyles().get(ii);
            if(associationStyles.getUuid().equals(associationStyle.getUuid())){
                index = ii;
                update = true;
                break;
            }
        }
        
        if(update){
            mapTheme.getAssociationStyles().remove(index);
        }
            mapTheme.getAssociationStyles().add(associationStyle);
            
        view.display(mapTheme);
    }

    public void setBundleStyle(PlaceStyle bundleStyle) {
        mapTheme.setBundleStyle(bundleStyle);
    }

    public void setDefaultShape(ShapeType selectedItem) {
        mapTheme.setDefaultShape(selectedItem);
    }

    public void setName(String text){ mapTheme.setName(text);}

}
