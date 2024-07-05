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

import java.util.Comparator;

import com.google.common.collect.ComparisonChain;
import com.google.common.primitives.Booleans;
import com.google.common.primitives.Doubles;
import com.sencha.gxt.data.shared.SortDir;

import csi.server.common.dto.graph.gwt.EdgeListDTO;
import csi.server.util.NumberHelper;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class EdgeListDTOComparator implements Comparator<EdgeListDTO> {

    private String fieldToCompare;

    public String getFieldToCompare() {
        return fieldToCompare;
    }

    public void setFieldToCompare(String fieldToCompare) {
        this.fieldToCompare = fieldToCompare;
    }

    private SortDir sortDir;

    public SortDir getSortDir() {
        return sortDir;
    }

    public void setSortDir(SortDir sortDir) {
        this.sortDir = sortDir;
    }

    @Override
    public int compare(EdgeListDTO o1, EdgeListDTO o2) {
        int compare = 0;
        if (EdgeListDTO.EdgeListFieldNames.FIELD_LABEL.equals(fieldToCompare)) {
            String value1 = o1.getSource();
            String value2 = o2.getSource();
            ComparisonChain chain = ComparisonChain.start();
            if (NumberHelper.isNumeric(value1) && NumberHelper.isNumeric(value2)) {
                double d1 = Double.parseDouble(value1);
                double d2 = Double.parseDouble(value2);
                chain.compare(d1, d2);
                // NOTE: This logic collapses equal numeric value types such a '0' & '00'.
                // compare = d1 > d2 ? 1 : d1 == d2 ? 0 : -1;
            }
            compare = chain.compare(value1, value2).result();
        } else if (EdgeListDTO.EdgeListFieldNames.FIELD_TARGET.equals(fieldToCompare)) {
            String value1 = o1.getTarget();
            String value2 = o2.getTarget();
            ComparisonChain chain = ComparisonChain.start();
            if (NumberHelper.isNumeric(value1) && NumberHelper.isNumeric(value2)) {
                double d1 = Double.parseDouble(value1);
                double d2 = Double.parseDouble(value2);
                chain.compare(d1, d2);
                // NOTE: This logic collapses equal numeric value types such a '0' & '00'.
                // compare = d1 > d2 ? 1 : d1 == d2 ? 0 : -1;
            }
            compare = chain.compare(value1, value2).result();
        } else if (EdgeListDTO.EdgeListFieldNames.FIELD_SOURCE.equals(fieldToCompare)) {
            String value1 = o1.getSource();
            String value2 = o2.getSource();
            ComparisonChain chain = ComparisonChain.start();
            if (NumberHelper.isNumeric(value1) && NumberHelper.isNumeric(value2)) {
                double d1 = Double.parseDouble(value1);
                double d2 = Double.parseDouble(value2);
                chain.compare(d1, d2);
                // NOTE: This logic collapses equal numeric value types such a '0' & '00'.
                // compare = d1 > d2 ? 1 : d1 == d2 ? 0 : -1;
            }
            compare = chain.compare(value1, value2).result();
        } else if (EdgeListDTO.EdgeListFieldNames.FIELD_TYPE.equals(fieldToCompare)) {
            String value1 = o1.getSource();
            String value2 = o2.getSource();
            ComparisonChain chain = ComparisonChain.start();
            if (NumberHelper.isNumeric(value1) && NumberHelper.isNumeric(value2)) {
                double d1 = Double.parseDouble(value1);
                double d2 = Double.parseDouble(value2);
                chain.compare(d1, d2);
                // NOTE: This logic collapses equal numeric value types such a '0' & '00'.
                // compare = d1 > d2 ? 1 : d1 == d2 ? 0 : -1;
            }
            compare = chain.compare(value1, value2).result();
        } else if (EdgeListDTO.EdgeListFieldNames.FIELD_SELECTED.equalsIgnoreCase(fieldToCompare)) {
            compare = Booleans.compare(o1.isSelected(), o2.isSelected());
        } else if (EdgeListDTO.EdgeListFieldNames.FIELD_HIDDEN.equals(fieldToCompare)) {
            compare = Booleans.compare(o1.isHidden(), o2.isHidden());
        } else if (EdgeListDTO.EdgeListFieldNames.FIELD_PLUNKED.equals(fieldToCompare)) {
            compare = Booleans.compare(o1.isPlunked(), o2.isPlunked());
        } else if (EdgeListDTO.EdgeListFieldNames.FIELD_ANNOTATION.equals(fieldToCompare)) {
            compare = Booleans.compare(o1.hasAnnotation(), o2.hasAnnotation());
        }  else if (EdgeListDTO.EdgeListFieldNames.FIELD_TYPES.equals(fieldToCompare)) { //strictly a string comparison for now.
            String v1 = o1.getAllTypesAsString();
            String v2 = o2.getAllTypesAsString();
            ComparisonChain chain = ComparisonChain.start();
            compare = chain.compare(v1, v2).result();

        }else if (EdgeListDTO.EdgeListFieldNames.FIELD_SIZE.equals(fieldToCompare)){
            compare = Doubles.compare(o1.getWidth(), o2.getWidth());
        }else if (EdgeListDTO.EdgeListFieldNames.OPACITY.equals(fieldToCompare)){
            compare = Doubles.compare(o1.getOpacity(), o2.getOpacity());
        } else{
            throw new RuntimeException("Unknown field type. Don't know how to sort on " + fieldToCompare);
        }
        compare = SortDir.DESC == getSortDir() ? -compare : compare;
        return compare;
    }

}
