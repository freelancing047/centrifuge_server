package csi.server.business.visualization.legend;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class MapLegendInfo implements IsSerializable {
   private boolean linkLimitReached = false;
   private List<PlaceLegendItem> placeLegendItems;
   private CombinedPlaceLegendItem combinedPlaceLegendItem;
   private List<AssociationLegendItem> associationLegendItems;
   private List<TrackLegendItem> trackLegendItems;
   private NewPlaceLegendItem newPlaceLegendItem;
   private UpdatedPlaceLegendItem updatedPlaceLegendItem;

   public MapLegendInfo() {
   }

   public boolean isLinkLimitReached() {
      return linkLimitReached;
   }

   public void setLinkLimitReached(boolean linkLimitReached) {
      this.linkLimitReached = linkLimitReached;
   }

   public List<PlaceLegendItem> getPlaceLegendItems() {
      return placeLegendItems;
   }

   public void setPlaceLegendItems(List<PlaceLegendItem> placeLegendItems) {
      this.placeLegendItems = placeLegendItems;
   }

   public CombinedPlaceLegendItem getCombinedPlaceLegendItem() {
      return combinedPlaceLegendItem;
   }

   public void setCombinedPlaceLegendItem(CombinedPlaceLegendItem combinedPlaceLegendItem) {
      this.combinedPlaceLegendItem = combinedPlaceLegendItem;
   }

   public List<AssociationLegendItem> getAssociationLegendItems() {
      return associationLegendItems;
   }

   public void setAssociationLegendItems(List<AssociationLegendItem> associationLegendItems) {
      this.associationLegendItems = associationLegendItems;
   }

   public List<TrackLegendItem> getTrackLegendItems() {
      return trackLegendItems;
   }

   public void setTrackLegendItems(List<TrackLegendItem> trackLegendItems) {
      this.trackLegendItems = trackLegendItems;
   }

   public NewPlaceLegendItem getNewPlaceLegendItem() {
      return newPlaceLegendItem;
   }

   public void setNewPlaceLegendItem(NewPlaceLegendItem newPlaceLegendItem) {
      this.newPlaceLegendItem = newPlaceLegendItem;
   }

   public UpdatedPlaceLegendItem getUpdatedPlaceLegendItem() {
      return updatedPlaceLegendItem;
   }

   public void setUpdatedPlaceLegendItem(UpdatedPlaceLegendItem updatedPlaceLegendItem) {
      this.updatedPlaceLegendItem = updatedPlaceLegendItem;
   }
}
