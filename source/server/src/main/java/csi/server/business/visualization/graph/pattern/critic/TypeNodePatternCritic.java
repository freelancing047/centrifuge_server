package csi.server.business.visualization.graph.pattern.critic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

import prefuse.visual.EdgeItem;
import prefuse.visual.VisualItem;

import csi.server.business.visualization.graph.base.LinkStore;
import csi.server.business.visualization.graph.base.NodeStore;
import csi.shared.gwt.viz.graph.tab.pattern.settings.PatternCriterion;

public class TypeNodePatternCritic implements NodePatternCritic, LinkPatternCritic {
   @Override
   public boolean criticizeNode(String g, VisualItem item, NodeStore details, PatternCriterion criterion,
                                String jobId) {
      boolean result = false;

      for (String s : details.getTypes().keySet()) {
         if (criterion.getValue().equals(s)) {
            result = true;
            break;
         }
      }
      return result;
   }

   @Override
   public SafeHtml getObservedValue(VisualItem item, NodeStore details, PatternCriterion criterion, String dvUuid) {
      List<String> parts = new ArrayList<String>();
      Map<String,Integer> types = details.getTypes();

      for (String type : types.keySet()) {
         if (criterion.getValue().equals(type)) {
            parts.add("<b>" + type + "</b>");
         } else {
            parts.add(type);
         }
      }
      return SafeHtmlUtils.fromSafeConstant(parts.stream().collect(Collectors.joining(", ")));
   }

   @Override
   public boolean criticizeLink(String dvuuid, EdgeItem item, LinkStore details, PatternCriterion criterion) {
      boolean result = false;
      Map<String,Integer> types = details.getTypes();

      for (String type : types.keySet()) {
         if (criterion.getValue().equals(type)) {
            result = true;
            break;
         }
      }
      return result;
   }

   @Override
   public SafeHtml getObservedValue(VisualItem edge, LinkStore details, PatternCriterion patternCriterion,
                                    String dvuuid) {
      List<String> parts = new ArrayList<String>();
      Map<String,Integer> types = details.getTypes();

      for (String type : types.keySet()) {
         if (patternCriterion.getValue().equals(type)) {
            parts.add("<b>" + type + "</b>");
         } else {
            parts.add(type);
         }
      }
      return SafeHtmlUtils.fromSafeConstant(parts.stream().collect(Collectors.joining(", ")));
   }

   @Override
   public boolean criticizeReverseLink(String dvuuid, EdgeItem item, LinkStore details,
                                       PatternCriterion patternCriterion) {
      return criticizeLink(dvuuid, item, details, patternCriterion);
   }
}
