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

import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;

import com.sencha.gxt.data.shared.loader.FilterConfig;

import csi.server.common.dto.graph.gwt.EdgeListDTO;

/**
 * Used for filtering GXT node-list grid
 *
 * @author Centrifuge Systems, Inc.
 *
 */
public class EdgeListDTOFilteringPredicate implements Predicate<EdgeListDTO> {
   private static final Pattern SEPARATOR_PATTERN = Pattern.compile("::");

   private List<FilterConfig> filterConfigs;

   public List<FilterConfig> getFilterConfigs() {
      return filterConfigs;
   }

   public void setFilterConfigs(List<FilterConfig> filterConfigs) {
      this.filterConfigs = filterConfigs;
   }

   @Override
   public boolean test(@Nullable EdgeListDTO input) {
      boolean result = true;

      if (filterConfigs != null) {
         for (FilterConfig filterConfig : filterConfigs) {
            if (filterConfig.getField().equals(EdgeListDTO.EdgeListFieldNames.FIELD_LABEL)) {
               if (input.getLabel().contains(filterConfig.getValue())) {
                  result = false;
                  break;
               }
            } else if (filterConfig.getField().equals(EdgeListDTO.EdgeListFieldNames.FIELD_SOURCE)) {
               String[] values = SEPARATOR_PATTERN.split(filterConfig.getValue());
               boolean matches = false;

               for (String source : values) {
                  if (input.getSource().equals(source)) {
                     matches = true;
                  }
               }
               if (!matches) {
                  result = false;
                  break;
               }
            } else if (filterConfig.getField().equals(EdgeListDTO.EdgeListFieldNames.FIELD_TARGET)) {
               String[] values = SEPARATOR_PATTERN.split(filterConfig.getValue());
               boolean matches = false;

               for (String target : values) {
                  if (input.getTarget().equals(target)) {
                     matches = true;
                  }
               }
               if (!matches) {
                  result = false;
                  break;
               }
            } else if (filterConfig.getField().equals(EdgeListDTO.EdgeListFieldNames.FIELD_TYPE)) {
               String[] values = SEPARATOR_PATTERN.split(filterConfig.getValue());
               boolean matches = false;

               for (String type : values) {
                  if (input.getType().equals(type)) {
                     matches = true;
                  }
               }
               if (!matches) {
                  result = false;
                  break;
               }
            } else if (filterConfig.getField().equals(EdgeListDTO.EdgeListFieldNames.FIELD_SELECTED)) {
               if (!Boolean.valueOf(filterConfig.getValue()).equals(input.isSelected())) {
                  result = false;
                  break;
               }
            } else if (filterConfig.getField().equals(EdgeListDTO.EdgeListFieldNames.FIELD_HIDDEN)) {
               if (!Boolean.valueOf(filterConfig.getValue()).equals(input.isHidden())) {
                  result = false;
                  break;
               }
            } else if (filterConfig.getField().equals(EdgeListDTO.EdgeListFieldNames.FIELD_PLUNKED)) {
               if (!Boolean.valueOf(filterConfig.getValue()).equals(input.isPlunked())) {
                  result = false;
                  break;
               }
            } else if (filterConfig.getField().equals(EdgeListDTO.EdgeListFieldNames.FIELD_ANNOTATION)) {
               if (!Boolean.valueOf(filterConfig.getValue()).equals(input.hasAnnotation())) {
                  result = false;
                  break;
               }
            } else if (filterConfig.getField().equals(EdgeListDTO.EdgeListFieldNames.FIELD_SIZE)) {
               result = (Double.compare(Double.parseDouble(filterConfig.getValue()), input.getWidth()) >= 0);
               break;
            } else if (filterConfig.getField().equals("source_or_target")) {
               String value = filterConfig.getValue();

               if (StringUtils.isEmpty(value)) {
                  continue;
               }
               String caseFreeFilterString = value.toUpperCase().toLowerCase();

               if (!input.getSource().toUpperCase().toLowerCase().contains(caseFreeFilterString) &&
                   !input.getTarget().toUpperCase().toLowerCase().contains(caseFreeFilterString)) {
                  result = false;
                  break;
               }
            }
         }
      }
      return result;
   }
}
