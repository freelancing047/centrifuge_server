package csi.client.gwt.viz.map.legend;

import com.google.gwt.user.client.Random;
import com.google.web.bindery.event.shared.HandlerRegistration;
import csi.client.gwt.WebMain;
import csi.client.gwt.events.CsiEvent;
import csi.client.gwt.events.CsiEventCommander;
import csi.client.gwt.events.CsiEventHandler;
import csi.client.gwt.events.CsiEventHeader;
import csi.server.business.visualization.legend.*;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.service.api.MapActionsServiceProtocol;

import java.util.*;

public class MapLegendProxy {
    private boolean linkLimitReached;
    private List<PlaceLegendItem> placeLegendItems;
    private CombinedPlaceLegendItem combinedPlaceLegendItem;
    private List<AssociationLegendItem> associationLegendItems;
    private List<TrackLegendItem> trackLegendItems;
    private NewPlaceLegendItem newPlaceLegendItem;
    private UpdatedPlaceLegendItem updatedPlaceLegendItem;
    private CsiEventHeader myEventHeaders;
    private Map<String, Set<Integer>> typenameToPlaceIds;
    private Map<String, Set<Integer>> typenameToTrackIds;
    private HandlerRegistration handlerRegistration;
    private Integer sequenceNumber;

    MapLegendProxy(String dvUuid, String vizUuid, Integer sequenceNumber) {
        myEventHeaders = new CsiEventHeader();
        myEventHeaders.addHeader("MapLegendProxy", "" + Random.nextDouble());
        try {
            WebMain.injector.getVortex().execute((MapLegendInfo result) -> {
                linkLimitReached = result.isLinkLimitReached();
                placeLegendItems = result.getPlaceLegendItems();
                typenameToPlaceIds = new HashMap<>();
                for (PlaceLegendItem item : placeLegendItems) {
                    Set<Integer> placeIds;
                    if (typenameToPlaceIds.containsKey(item.typeName))
                        placeIds = typenameToPlaceIds.get(item.typeName);
                    else {
                        placeIds = new TreeSet<>();
                        typenameToPlaceIds.put(item.typeName, placeIds);
                    }
                    placeIds.add(item.placeId);
                }
                trackLegendItems = result.getTrackLegendItems();
                typenameToTrackIds = new HashMap<>();
                for (TrackLegendItem item : trackLegendItems) {
                    Set<Integer> trackIds;
                    if (typenameToTrackIds.containsKey(item.typeName))
                        trackIds = typenameToTrackIds.get(item.typeName);
                    else {
                        trackIds = new TreeSet<>();
                        typenameToTrackIds.put(item.typeName, trackIds);
                    }
                    trackIds.add(item.trackId);
                }
                combinedPlaceLegendItem = result.getCombinedPlaceLegendItem();
                associationLegendItems = result.getAssociationLegendItems();
                trackLegendItems = result.getTrackLegendItems();
                newPlaceLegendItem = result.getNewPlaceLegendItem();
                updatedPlaceLegendItem = result.getUpdatedPlaceLegendItem();
                new CsiEvent(myEventHeaders).fire();
            }, MapActionsServiceProtocol.class).legendData(dvUuid, vizUuid, sequenceNumber);
        } catch (CentrifugeException ignored) {
        }
    }

    boolean isLinkLimitReached() {
        return linkLimitReached;
    }

    List<PlaceLegendItem> getPlaceLegendItems() {
        return placeLegendItems;
    }

    CombinedPlaceLegendItem getCombinedPlaceLegendItem() {
        return combinedPlaceLegendItem;
    }

    List<AssociationLegendItem> getAssociationLegendItems() {
        return associationLegendItems;
    }

    List<TrackLegendItem> getTrackLegendItems() {
        return trackLegendItems;
    }

    NewPlaceLegendItem getNewPlaceLegendItem() {
        return newPlaceLegendItem;
    }

    UpdatedPlaceLegendItem getUpdatedPlaceLegendItem() {
        return updatedPlaceLegendItem;
    }

    public void addLoadHandler(CsiEventHandler csiEventHandler) {
        this.handlerRegistration = CsiEventCommander.getInstance().addHandler(myEventHeaders, csiEventHandler);
    }

    void removeLoadHandler() {
        this.handlerRegistration.removeHandler();
    }

    boolean isPlaceTypenameUnique(String typename) {
        Set<Integer> placeIds = typenameToPlaceIds.get(typename);
        return placeIds.size() == 1;
    }

}
