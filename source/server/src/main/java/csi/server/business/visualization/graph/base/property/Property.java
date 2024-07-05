package csi.server.business.visualization.graph.base.property;

import java.util.ArrayList;
import java.util.List;

public class Property {
   // protected QName name;
   protected String name;
   protected boolean includeInTooltip;
   protected boolean hideEmptyInTooltip;

   protected List<Object> values;
   private int tooltipOrdinal;

   // public Property(QName name) {
   // this.name = name.getLocalPart();
   // values = null;
   // includeInTooltip = true;
   // }

   public Property(String name) {
      this.name = name;
      includeInTooltip = true;
   }

   public String getName() {
      // return name.getLocalPart();
      return name;
   }

   public String getFullName() {
      return name;
   }

   // public QName getQName() {
   // // return name;
   // return new QName(name);
   // }
   //
   public boolean hasValues() {
      return ((values != null) && !values.isEmpty());
   }

   public List<Object> getValues() {
      if (values == null) {
         values = new ArrayList<Object>();
      }
      return values;
   }

   public void setValues(List<Object> values) {
      this.values = values;
   }

   public boolean isIncludeInTooltip() {
      return includeInTooltip;
   }

   public void setIncludeInTooltip(boolean includeInTooltip) {
      this.includeInTooltip = includeInTooltip;
   }

   public boolean isHideEmptyInTooltip() {
      return hideEmptyInTooltip;
   }

   public void setHideEmptyInTooltip(boolean hideEmptyInTooltip) {
      this.hideEmptyInTooltip = hideEmptyInTooltip;
   }

   public void setTooltipOrdinal(int tooltipOrdinal) {
      this.tooltipOrdinal = tooltipOrdinal;
   }

   public int getTooltipOrdinal() {
      return tooltipOrdinal;
   }
}
