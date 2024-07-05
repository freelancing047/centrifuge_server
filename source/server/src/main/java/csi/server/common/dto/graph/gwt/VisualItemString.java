package csi.server.common.dto.graph.gwt;

import java.util.ArrayList;
import java.util.List;

import csi.server.common.exception.CentrifugeException;

public class VisualItemString extends AbstractVisualItemTypeBase {
   public String visualItemType;

   public VisualItemString(String visualItemType) {
      this.visualItemType = visualItemType;
   }

   public VisualItemString() {
   }

   @Override
   public String getString() throws CentrifugeException {
      return visualItemType;
   }

   @Override
   public List<String> getArray() throws CentrifugeException {
      List<String> array = new ArrayList<String>();
      array.add(visualItemType);
      return array;
   }

   public String getVisualItemType() {
      return visualItemType;
   }

   public void setVisualItemType(String visualItemType) {
      this.visualItemType = visualItemType;
   }
}
