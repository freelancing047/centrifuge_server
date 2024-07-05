/**
 *  Copyright (c) 2008 Centrifuge Systems, Inc.
 *  All rights reserved.
 *
 *  This software is the confidential and proprietary information of
 *  Centrifuge Systems, Inc. ("Confidential Information").  You shall
 *  not disclose such Confidential Information and shall use it only
 *  in accordance with the terms of the license agreement you entered
 *  into with Centrifuge Systems.
 *
 **/
package csi.server.business.service;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.sencha.gxt.data.shared.loader.FilterConfig;

import csi.server.common.dto.graph.gwt.NodeListDTO;
import csi.server.common.dto.graph.gwt.NodeListDTO.NodeListFieldNames;
import csi.server.gwt.gxt.FilterComparison;
import csi.server.util.NumberHelper;

/**
 * Used for filtering GXT node-list grid
 *
 * @author Centrifuge Systems, Inc.
 *
 */
public class NodeListDTOFilteringPredicate implements Predicate<NodeListDTO> {
   private List<FilterConfig> filterConfigs;

   public List<FilterConfig> getFilterConfigs() {
      return filterConfigs;
   }

   public void setFilterConfigs(List<FilterConfig> filterConfigs) {
      this.filterConfigs = filterConfigs;
   }

   @SuppressWarnings("incomplete-switch")
   @Override
   public boolean test(@Nullable NodeListDTO input) {
      boolean success = true;

      if ((filterConfigs != null) && !filterConfigs.isEmpty()) {
         for (FilterConfig filterConfig : getFilterConfigs()) {
            NodeListFieldNames nodeListFieldName = NodeListFieldNames.get(filterConfig.getField());

            if (nodeListFieldName != null) {
               switch (nodeListFieldName) {
                  case ANCHORED:
                     success = Boolean.valueOf(filterConfig.getValue()).equals(Boolean.valueOf(input.isAnchored()));
                     break;
                  case BETWEENNESS:
                     if (NumberHelper.isNumeric(filterConfig.getValue())) {
                        int value = Integer.parseInt(filterConfig.getValue());

                        switch (FilterComparison.forCode(filterConfig.getComparison())) {
                           case LESS_THAN:
                              success = (input.getBetweenness() < value);
                              break;
                           case GREATER_THAN:
                              success = (input.getBetweenness() > value);
                              break;
                           case EQUALS:
                              success = (BigDecimal.valueOf(value)
                                           .compareTo(BigDecimal.valueOf(input.getBetweenness())) == 0);
                              break;
                        }
                     } else {
                        success = false;
                     }
                     break;
                  case BUNDLED:
                     success = Boolean.valueOf(filterConfig.getValue()).equals(Boolean.valueOf(input.isBundled()));
                     break;
                  case BUNDLE_NODE_LABEL:
                     if (!Strings.isNullOrEmpty(filterConfig.getValue())) {
                        success = input.getBundleNodeLabel().contains(filterConfig.getValue());
                     }
                     break;
                  case CLOSENESS:
                     if (NumberHelper.isNumeric(filterConfig.getValue())) {
                        int value = Integer.parseInt(filterConfig.getValue());

                        switch (FilterComparison.forCode(filterConfig.getComparison())) {
                           case LESS_THAN:
                              success = (input.getCloseness() < value);
                              break;
                           case GREATER_THAN:
                              success = (input.getCloseness() > value);
                              break;
                           case EQUALS:
                              success = (BigDecimal.valueOf(value)
                                           .compareTo(BigDecimal.valueOf(input.getCloseness())) == 0);
                              break;
                        }
                     } else {
                        success = false;
                     }
                     break;
                  case COMPONENT:
                     success = Integer.valueOf(filterConfig.getValue()).equals(Integer.valueOf(input.getComponent()));
                     break;
                  case DEGREES:
                     if (NumberHelper.isNumeric(filterConfig.getValue())) {
                        int value = Integer.parseInt(filterConfig.getValue());

                        switch (FilterComparison.forCode(filterConfig.getComparison())) {
                           case LESS_THAN:
                              success = (input.getDegrees() < value);
                              break;
                           case GREATER_THAN:
                              success = (input.getDegrees() > value);
                              break;
                           case EQUALS:
                              success = (BigDecimal.valueOf(value)
                                           .compareTo(BigDecimal.valueOf(input.getDegrees())) == 0);
                              break;
                        }
                     } else {
                        success = false;
                     }
                     break;
                  case DISPLAY_X:
                     success = (BigDecimal.valueOf(Double.parseDouble(filterConfig.getValue()))
                                  .compareTo(BigDecimal.valueOf(input.getDisplayX())) == 0);
                     break;
                  case DISPLAY_Y:
                     success = (BigDecimal.valueOf(Double.parseDouble(filterConfig.getValue()))
                                  .compareTo(BigDecimal.valueOf(input.getDisplayY())) == 0);
                     break;
                  case EIGENVECTOR:
                     if (NumberHelper.isNumeric(filterConfig.getValue())) {
                        int value = Integer.parseInt(filterConfig.getValue());

                        switch (FilterComparison.forCode(filterConfig.getComparison())) {
                           case LESS_THAN:
                              success = (input.getEigenvector() < value);
                              break;
                           case GREATER_THAN:
                              success = (input.getEigenvector() > value);
                              break;
                           case EQUALS:
                              success = (BigDecimal.valueOf(value)
                                           .compareTo(BigDecimal.valueOf(input.getEigenvector())) == 0);
                              break;
                        }
                     } else {
                        success = false;
                     }
                     break;
                  case HIDDEN:
                     success = Boolean.valueOf(filterConfig.getValue()).equals(Boolean.valueOf(input.isHidden()));
                     break;
                  case HIDE_LABELS:
                     success = Boolean.valueOf(filterConfig.getValue()).equals(input.getHideLabels());
                     break;
                  case ID:
                     // Internal
                     break;
                  case KEY:
                     // Internal
                     break;
                  case LABEL:
                     if (!Strings.isNullOrEmpty(filterConfig.getValue())) {
                        success = input.getLabel().toLowerCase().contains(filterConfig.getValue().toLowerCase());
                     }
                     break;
                  case NESTED_LEVEL:
                     if (NumberHelper.isNumeric(filterConfig.getValue())) {
                        int value = Integer.parseInt(filterConfig.getValue());

                        switch (FilterComparison.forCode(filterConfig.getComparison())) {
                           case LESS_THAN:
                              success = (input.getNestedLevel() < value);
                              break;
                           case GREATER_THAN:
                              success = (input.getNestedLevel() > value);
                              break;
                           case EQUALS:
                              success = (value == input.getNestedLevel());
                              break;
                        }
                     } else {
                        success = false;
                     }
                     break;
                  case SELECTED:
                     success = Boolean.valueOf(filterConfig.getValue()).equals(Boolean.valueOf(input.isSelected()));
                     break;
                  case PLUNKED:
                     success = Boolean.valueOf(filterConfig.getValue()).equals(Boolean.valueOf(input.isPlunked()));
                     break;
                  case IS_BUNDLE:
                     success = Boolean.valueOf(filterConfig.getValue()).equals(Boolean.valueOf(input.isBundle()));
                     break;
                  case ANNOTATION:
                     success = Boolean.valueOf(filterConfig.getValue()).equals(Boolean.valueOf(input.hasAnnotation()));
                     break;
                  case TYPE:
                     Collection<String> includedTypes = Lists.newArrayList(Splitter.on("::").split(filterConfig.getValue()));
                     success = includedTypes.contains(input.getType());
                     break;
                  case VISIBLE_NEIGHBORS:
                     if (NumberHelper.isNumeric(filterConfig.getValue())) {
                        int value = Integer.parseInt(filterConfig.getValue());

                        switch (FilterComparison.forCode(filterConfig.getComparison())) {
                           case LESS_THAN:
                              success = (input.getVisibleNeighbors() < value);
                              break;
                           case GREATER_THAN:
                              success = (input.getVisibleNeighbors() > value);
                              break;
                           case EQUALS:
                              success = (value == input.getVisibleNeighbors());
                              break;
                        }
                     } else {
                        success = false;
                     }
                     break;
                  case VISUALIZED:
                     success = Boolean.valueOf(filterConfig.getValue()).equals(input.getVisualized());
                     break;
                  case X:
                     success = (BigDecimal.valueOf(Double.parseDouble(filterConfig.getValue()))
                                  .compareTo(BigDecimal.valueOf(input.getX())) == 0);
                     break;
                  case Y:
                     success = (BigDecimal.valueOf(Double.parseDouble(filterConfig.getValue()))
                                  .compareTo(BigDecimal.valueOf(input.getY())) == 0);
                     break;
                  default:
                     break;
               }
            }
            if (!success) {
               break;
            }
         }
      }
      return success;
   }
}
