package csi.client.gwt.maplayer.editor;

import csi.client.gwt.WebMain;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.Dialog;
import csi.server.common.dto.SelectionListData.ResourceBasics;
import csi.server.common.dto.user.UserSecurityInfo;
import csi.server.common.model.map.Basemap;
import csi.server.common.service.api.MapActionsServiceProtocol;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapLayerEditorPresenter implements MapLayerListHolder {
    private Map<String, Map<String, ResourceBasics>> nameToBasemaps;
    private MapLayerEditorPanel view;
    private MapMapLayerEditorPresenter mapBasemapEditorPresenter = new MapMapLayerEditorPresenter(this);

    public MapLayerEditorPresenter() {

    }

    public MapLayerEditorPresenter(boolean createLayerIn) {

        if (createLayerIn) {

            populateBasemapNames(true);
        }
    }

    @Override
    public void populateBasemapNames() {

        populateBasemapNames(false);
    }

    public void populateBasemapNames(final boolean createLayerIn) {
        VortexFuture<List<ResourceBasics>> future = WebMain.injector.getVortex().createFuture();
        future.addEventHandler(new AbstractVortexEventHandler<List<ResourceBasics>>() {
            @Override
            public void onSuccess(List<ResourceBasics> result) {
                nameToBasemaps = new HashMap<String, Map<String, ResourceBasics>>();
                for (ResourceBasics resource : result) {
                    Map<String, ResourceBasics> ownerToBasemap;
                    if (nameToBasemaps.containsKey(resource.getName()))
                        ownerToBasemap = nameToBasemaps.get(resource.getName());
                    else {
                        ownerToBasemap = new HashMap<String, ResourceBasics>();
                        nameToBasemaps.put(resource.getName(), ownerToBasemap);
                    }
                    ownerToBasemap.put(resource.getOwner(), resource);
                }
                if (createLayerIn) {

                    editBasemap(null, new Basemap());
                }
                getView().updateGrid(result);
            }

            @Override
            public boolean onError(Throwable t) {
                return false;
            }
        });
        future.execute(MapActionsServiceProtocol.class).listBasemapResources();
    }

    public MapLayerEditorPanel getView() {
        if (view == null) {
            view = new MapLayerEditorPanel();
            view.setPresenter(this);
            populateBasemapNames();
        }
        return view;
    }

    @Override
    public boolean basemapNameExists(String ownerName, String basemapName) {
        if (nameToBasemaps.keySet().contains(basemapName)) {
            Map<String, ResourceBasics> ownerToBasemap = nameToBasemaps.get(basemapName);
            return ownerToBasemap.containsKey(ownerName);
        }
        return false;
    }

    public void editBasemap(String uuid, Basemap basemap) {
        if (uuid == null) {
            Basemap newBasemap = new Basemap();
            UserSecurityInfo _userInfo = WebMain.injector.getMainPresenter().getUserInfo();
            newBasemap.setName(newBasemapName(_userInfo));
            newBasemap.setOwner(_userInfo.getName());
            openEditorForBasemap(newBasemap);
        } else {
            VortexFuture<Basemap> future = WebMain.injector.getVortex().createFuture();
            future.addEventHandler(new AbstractVortexEventHandler<Basemap>() {
                @Override
                public void onSuccess(Basemap basemap) {
                    openEditorForBasemap(basemap);
                }

                @Override
                public boolean onError(Throwable t) {
                    return false;
                }
            });
            future.execute(MapActionsServiceProtocol.class).findBasemap(uuid);
        }
    }

    private String newBasemapName(UserSecurityInfo _userInfo) {
        String startingBasemapName = "Basemap";
        String basemapName = startingBasemapName;
        int i = 1;
        while (basemapNameExists(_userInfo.getName(), basemapName)) {
            basemapName = startingBasemapName + " <" + i + ">";
            i++;
        }
        return basemapName;
    }

    private void openEditorForBasemap(Basemap basemap) {
        mapBasemapEditorPresenter.edit(basemap);
    }

    public void deleteBasemap(String uuid) {
        VortexFuture<Void> future = WebMain.injector.getVortex().createFuture();
        future.addEventHandler(new AbstractVortexEventHandler<Void>() {
            @Override
            public void onSuccess(Void result) {
                populateBasemapNames();
            }

            @Override
            public boolean onError(Throwable t) {
                Dialog.showException(t);
                return false;
            }
        });
        future.execute(MapActionsServiceProtocol.class).deleteBasemap(uuid);
    }

    @Override
    public void notifyBasemapDeleted(Basemap basemap) {
        if (basemap != null)
            deleteBasemap(basemap.getUuid());
    }

}