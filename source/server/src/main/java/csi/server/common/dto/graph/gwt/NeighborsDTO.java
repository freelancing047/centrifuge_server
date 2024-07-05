package csi.server.common.dto.graph.gwt;

import com.google.gwt.user.client.rpc.IsSerializable;

import csi.server.common.dto.CsiMap;

public class NeighborsDTO implements IsSerializable {
   public Integer ID;
   public String objectType;
   public Double X;
   public Double Y;
   public Double displayX;
   public Double displayY;
   public String label;
   public CsiMap<String,AbstractItemTypeBase> columns;

   public NeighborsDTO() {
   }

   public Integer getID() {
      return ID;
   }

   public void setID(Integer iD) {
      ID = iD;
   }

   public String getObjectType() {
      return objectType;
   }

   public void setObjectType(String objectType) {
      this.objectType = objectType;
   }

   public Double getX() {
      return X;
   }

   public void setX(Double x) {
      X = x;
   }

   public Double getY() {
      return Y;
   }

   public void setY(Double y) {
      Y = y;
   }

   public Double getDisplayX() {
      return displayX;
   }

   public void setDisplayX(Double displayX) {
      this.displayX = displayX;
   }

   public Double getDisplayY() {
      return displayY;
   }

   public void setDisplayY(Double displayY) {
      this.displayY = displayY;
   }

   public CsiMap<String,AbstractItemTypeBase> getColumns() {
      return columns;
   }

   public void setColumns(CsiMap<String,AbstractItemTypeBase> columns) {
      this.columns = columns;
   }

   public String getLabel() {
      return label;
   }

   public void setLabel(String label) {
      this.label = label;
   }
}
