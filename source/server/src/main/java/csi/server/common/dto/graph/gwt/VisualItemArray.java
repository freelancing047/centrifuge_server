package csi.server.common.dto.graph.gwt;

import java.util.List;

import csi.server.common.exception.CentrifugeException;

public class VisualItemArray extends AbstractVisualItemTypeBase {
   public List<String> visualItemType;

   public VisualItemArray(List<String> visualItemType) {
      this.visualItemType = visualItemType;
   }

   public VisualItemArray() {
   }

   @Override
   public String getString() throws CentrifugeException {
      throw new CentrifugeException("can not convert Array<String> to String");
   }

   @Override
   public List<String> getArray() throws CentrifugeException {
      return visualItemType;
   }

   public List<String> getVisualItemType() {
      return visualItemType;
   }

   public void setVisualItemType(List<String> visualItemType) {
      this.visualItemType = visualItemType;
   }
}
