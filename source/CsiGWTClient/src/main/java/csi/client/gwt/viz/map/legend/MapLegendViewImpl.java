package csi.client.gwt.viz.map.legend;

import com.github.gwtbootstrap.client.ui.FluidContainer;
import com.google.common.collect.Lists;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Composite;
import com.sencha.gxt.core.client.dom.XElement;
import csi.client.gwt.WebMain;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.graph.window.legend.DropCompleteEvent;
import csi.client.gwt.viz.graph.window.legend.DropCompleteEventHandler;
import csi.client.gwt.viz.graph.window.legend.LegendItemProxy;
import csi.client.gwt.viz.map.settings.MapConfigProxy;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.Dialog;
import csi.server.common.model.map.PlaceidTypenameDuple;
import csi.server.common.model.map.TrackidTracknameDuple;
import csi.server.common.service.api.MapActionsServiceProtocol;
import csi.shared.core.visualization.map.MapConstants;

import java.util.ArrayList;
import java.util.List;

public class MapLegendViewImpl extends Composite implements MapLegend.View {
    private FluidContainer fluidContainer;
    private AssociationOverLimitLegendItemProxy messageLegendItem;

    private MapLegendImpl mapLegend;
    private boolean isReadOnly;

    MapLegendViewImpl(final MapLegendImpl mapLegend, boolean isReadOnly) {
        this.mapLegend = mapLegend;
        this.isReadOnly = isReadOnly;

        if (isReadOnly) {
            fluidContainer = new FluidContainer();
            fluidContainer.addStyleName("maplegend-container");// NON-NLS
            initWidget(fluidContainer);
        } else {
            fluidContainer = new MapDndFluidContainer();
            fluidContainer.addStyleName("maplegend-container");// NON-NLS
            initWidget(fluidContainer);

            ((MapDndFluidContainer) fluidContainer).addDropHandler(new DropCompleteEventHandler() {
                @Override
                public void onDropComplete(DropCompleteEvent event) {
                    int index = 0;
                    List<PlaceidTypenameDuple> placeNames = new ArrayList<>();
                    List<TrackidTracknameDuple> trackNames = new ArrayList<>();
                    List<String> associationNames = new ArrayList<>();
                    List<Integer> redundants = new ArrayList<>();
                    while (index < fluidContainer.getWidgetCount()) {
                        LegendItemProxy proxy = (LegendItemProxy) fluidContainer.getWidget(index);
                        if (proxy instanceof PlaceLegendItemProxy) {
                            PlaceLegendItemProxy placeLegendItemProxy = (PlaceLegendItemProxy) proxy;
                            int placeId = placeLegendItemProxy.getPlaceId();
                            String typeName = placeLegendItemProxy.getType();
                            PlaceidTypenameDuple placeidTypenameDuple = new PlaceidTypenameDuple(placeId, typeName);
                            if (placeNames.contains(placeidTypenameDuple))
                                redundants.add(index);
                            else
                                placeNames.add(placeidTypenameDuple);
                        } else if (proxy instanceof TrackLegendItemProxy) {
                            TrackLegendItemProxy trackLegendItemProxy = (TrackLegendItemProxy) proxy;
                            int trackId = trackLegendItemProxy.getTrackId();
                            String trackName = trackLegendItemProxy.getType();
                            TrackidTracknameDuple trackidTracknameDuple = new TrackidTracknameDuple(trackId, trackName);
                            if (trackNames.contains(trackidTracknameDuple))
                                redundants.add(index);
                            else
                                trackNames.add(trackidTracknameDuple);
                        } else if (proxy instanceof AssociationLegendItemProxy) {
                            String typeName = proxy.getType();
                            if (associationNames.contains(typeName))
                                redundants.add(index);
                            else
                                associationNames.add(typeName);
                        }
                        index++;
                    }

                    if (!redundants.isEmpty()) {
                        redundants = Lists.reverse(redundants);
                        for (Integer d : redundants)
                            fluidContainer.remove(d);
                    }

                    VortexFuture<Void> future = WebMain.injector.getVortex().createFuture();
                    future.addEventHandler(new AbstractVortexEventHandler<Void>() {
                        @Override
                        public void onSuccess(Void value) {
                            try {
                                mapLegend.getMapPresenter().legendUpdated();
                            } catch (Exception ignored) {
                            }
                        }

                        @Override
                        public boolean onError(Throwable t) {
                            return false;
                        }
                    });
                    String dvUuid = mapLegend.getMapPresenter().getDataViewUuid();
                    String vizUuid = mapLegend.getMapPresenter().getVisualizationDef().getUuid();
                    future.execute(MapActionsServiceProtocol.class).updateLegend(dvUuid, vizUuid, placeNames, trackNames, associationNames);
                }
            });
        }
    }

    @Override
    public void clear() {
        fluidContainer.clear();
    }

    public void showLinkLimitReachedMessage(boolean isLinkLimitReached) {
        if (isLinkLimitReached) {
            ((MapDndFluidContainer) fluidContainer).setLinkMessageUp(true);
            if (messageLegendItem == null) {
                messageLegendItem = new AssociationOverLimitLegendItemProxy();
                messageLegendItem.asWidget().addStyleName("maplegend-item");
                messageLegendItem.asWidget().addDomHandler(event -> Dialog.showInfo(CentrifugeConstantsLocator.get().tooManyAssociationsTitle(), CentrifugeConstantsLocator.get().tooManyAssociationsMessage(MapConfigProxy.instance().getLinkLimit())), ClickEvent.getType());
            } else {
                int index = fluidContainer.getWidgetIndex(messageLegendItem);
                if (index != -1)
                    fluidContainer.remove(index);
            }
            fluidContainer.insert(messageLegendItem, 0);
        } else {
            ((MapDndFluidContainer) fluidContainer).setLinkMessageUp(false);
            if (messageLegendItem != null) {
                int index = fluidContainer.getWidgetIndex(messageLegendItem);
                if (index != -1)
                    fluidContainer.remove(index);
            }
        }
    }

    @Override
    public void addLegendItem(LegendItemProxy item, boolean draggable) {
        if ((item instanceof PlaceLegendItemProxy)) {
            // tooltip
            if (item.getType().equals(MapConstants.NULL_TYPE_NAME)) {
                item.asWidget().setTitle(CentrifugeConstantsLocator.get().null_label());
            } else {
                item.asWidget().setTitle(item.getType());
            }
            item.asWidget().addStyleName("maplegend-item");
            item.asWidget().addDomHandler(new PlaceLegendItemClickHandler(mapLegend.getMapPresenter(), ((PlaceLegendItemProxy) item).getPlaceId(), item.getType()), ClickEvent.getType());
        }

        if ((item instanceof CombinedPlaceLegendItemProxy)) {
            item.asWidget().addStyleName("maplegend-item");
            item.asWidget().addDomHandler(new CombinedPlaceLegendItemClickHandler(mapLegend.getMapPresenter()), ClickEvent.getType());
        }

        if (item instanceof AssociationLegendItemProxy) {
            item.asWidget().addStyleName("maplegend-item");
            item.asWidget().addDomHandler(new AssociationLegendItemClickHandler(mapLegend.getMapPresenter(), item.getKey()), ClickEvent.getType());
        }

        if (item instanceof TrackLegendItemProxy) {
            item.asWidget().addStyleName("maplegend-item");
            item.asWidget().addDomHandler(new TrackLegendItemClickHandler(mapLegend.getMapPresenter(), ((TrackLegendItemProxy) item).getTrackId(), item.getType()), ClickEvent.getType());
        }

        if ((item instanceof NewPlaceLegendItemProxy)) {
            item.asWidget().addStyleName("maplegend-item");
            item.asWidget().addDomHandler(new NewPlaceLegendItemClickHandler(mapLegend.getMapPresenter()), ClickEvent.getType());
        }

        if ((item instanceof UpdatedPlaceLegendItemProxy)) {
            item.asWidget().addStyleName("maplegend-item");
            item.asWidget().addDomHandler(new UpdatedPlaceLegendItemClickHandler(mapLegend.getMapPresenter()), ClickEvent.getType());
        }

        if (draggable)
            fluidContainer.add(item.asWidget());
        else
            ((MapDndFluidContainer) fluidContainer).addNoHandler(item.asWidget());
    }

    @Override
    public FluidContainer getFluidContainer() {
        return this.fluidContainer;
    }

    @Override
    public void setScrollParent(XElement body) {
        if (fluidContainer instanceof MapDndFluidContainer)
            ((MapDndFluidContainer) fluidContainer).setScrollParent(body);
    }
}