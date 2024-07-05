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
import com.google.common.primitives.Ints;
import com.sencha.gxt.data.shared.SortDir;

import csi.server.common.dto.graph.gwt.NodeListDTO;
import csi.server.common.dto.graph.gwt.NodeListDTO.NodeListFieldNames;
import csi.server.util.NumberHelper;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class NodeListDTOComparator implements Comparator<NodeListDTO> {

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
    public int compare(NodeListDTO o1, NodeListDTO o2) {
        int compare = 0;
        // String formattedString = CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, fieldToCompare);
        NodeListFieldNames nodeListFieldName = NodeListFieldNames.get(fieldToCompare);
        switch (nodeListFieldName) {
            case ANCHORED:
                compare = Booleans.compare(o1.isAnchored(), o2.isAnchored());
                break;
            case BETWEENNESS:
                compare = Doubles.compare(o1.getBetweenness(), o2.getBetweenness());
                break;
            case BUNDLE_NODE_LABEL:
                compare = o1.getBundleNodeLabel().compareTo(o2.getBundleNodeLabel());
                break;
            case BUNDLED:
                compare = Booleans.compare(o1.isBundled(), o2.isBundled());
                break;
            case CLOSENESS:
                compare = Doubles.compare(o1.getCloseness(), o2.getCloseness());
                break;
            case COMPONENT:
                compare = Ints.compare(o1.getComponent(), o2.getComponent());
                break;
            case DEGREES:
                compare = Doubles.compare(o1.getDegrees(), o2.getDegrees());
                break;
            case DISPLAY_X:
                compare = Doubles.compare(o1.getDisplayX(), o2.getDisplayX());
                break;
            case DISPLAY_Y:
                compare = Doubles.compare(o1.getDisplayY(), o2.getDisplayY());
                break;
            case EIGENVECTOR:
                compare = Doubles.compare(o1.getEigenvector(), o2.getEigenvector());
                break;
            case HIDDEN:
                compare = Booleans.compare(o1.isHidden(), o2.isHidden());
                break;
            case PLUNKED:
                compare = Booleans.compare(o1.isPlunked(), o2.isPlunked());
                break;
            case IS_BUNDLE:
                compare = Booleans.compare(o1.isBundle(), o2.isBundle());
                break;
            case ANNOTATION:
                compare = Booleans.compare(o1.hasAnnotation(), o2.hasAnnotation());
                break;
            case HIDE_LABELS:
                compare = Booleans.compare(o1.getHideLabels(), o2.getHideLabels());
                break;
            case ID:
                break;
            case KEY:
                break;
            case LABEL:
                String value1 = o1.getLabel();
                String value2 = o2.getLabel();
                ComparisonChain chain = ComparisonChain.start();
                if (NumberHelper.isNumeric(value1) && NumberHelper.isNumeric(value2)) {
                    double d1 = Double.parseDouble(value1);
                    double d2 = Double.parseDouble(value2);
                    chain.compare(d1, d2);
                    // NOTE: This logic collapses equal numeric value types such a '0' & '00'.
                    // compare = d1 > d2 ? 1 : d1 == d2 ? 0 : -1;
                }
                compare = chain.compare(value1, value2).result();
                break;
            case NESTED_LEVEL:
                compare = Ints.compare(o1.getNestedLevel(), o2.getNestedLevel());
                break;
            case SELECTED:
                compare = Booleans.compare(o1.isSelected(), o2.isSelected());
                break;
            case TYPE:
                compare = o1.getType().compareTo(o2.getType());
                break;
            case VISIBLE_NEIGHBORS:
                compare = Ints.compare(o1.getVisibleNeighbors(), o2.getVisibleNeighbors());
                break;
            case VISUALIZED:
                compare = Booleans.compare(o1.getVisualized(), o2.getVisualized());
                break;
            case SIZE:
                compare = Double.compare(o1.getSize(), o2.getSize());
                break;
            case TRANSPARENCY:
                compare = Double.compare(o1.getTransparency(), o2.getTransparency());
                break;
            default:
                throw new RuntimeException("Unknown field type. Don't know how to sort on " + fieldToCompare);
        }
        compare = SortDir.DESC == getSortDir() ? -compare : compare;
        return compare;
    }
}
