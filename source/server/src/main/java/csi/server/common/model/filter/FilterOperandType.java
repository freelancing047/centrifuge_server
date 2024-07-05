package csi.server.common.model.filter;

import csi.shared.core.util.HasLabel;

public enum FilterOperandType implements HasLabel {
   STATIC("one of the static values"),
   COLUMN("column"),
   PARAMETER("input parameter");

   public static final String NULL_LABEL = "N/A";

   private String label;

   private FilterOperandType(String label) {
      this.label = label;
   }

   @Override
   public String getLabel() {
      return label;
   }
}
