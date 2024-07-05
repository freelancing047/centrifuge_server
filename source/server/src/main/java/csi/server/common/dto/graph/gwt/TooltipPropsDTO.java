package csi.server.common.dto.graph.gwt;

import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

import csi.server.common.dto.CsiMap;

public class TooltipPropsDTO implements IsSerializable {
   public CsiMap<String,List<String>> attributeTooltips;
   public List<Map<String,String>> bundleContents;
   public CsiMap<String,String> snaMetrics;
   public String displayLabel;
   public AbstractVisualItemTypeBase visualItemType;
   public List<String> memberTypes;

   public TooltipPropsDTO() {
   }

   public CsiMap<String,List<String>> getAttributeNames() {
      return attributeTooltips;
   }

   public void setAttributeNames(CsiMap<String,List<String>> attributeTooltips) {
      this.attributeTooltips = attributeTooltips;
   }

   public List<Map<String,String>> getContents() {
      return bundleContents;
   }

   public void setContents(List<Map<String,String>> bundleContents) {
      this.bundleContents = bundleContents;
   }

   public CsiMap<String,String> getSnaMetrics() {
      return snaMetrics;
   }

   public void setSnaMetrics(CsiMap<String,String> snaMetrics) {
      this.snaMetrics = snaMetrics;
   }

   public String getDisplayLabel() {
      return displayLabel;
   }

   public void setDisplayLabel(String displayLabel) {
      this.displayLabel = displayLabel;
   }

   public AbstractVisualItemTypeBase getVisualItemType() {
      return visualItemType;
   }

   public void setVisualItemType(AbstractVisualItemTypeBase visualItemType) {
      this.visualItemType = visualItemType;
   }

   public List<String> getMemberTypes() {
      return memberTypes;
   }

   public void setMemberTypes(List<String> memberTypes) {
      this.memberTypes = memberTypes;
   }
}
