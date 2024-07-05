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
package csi.client.gwt.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.common.primitives.Ints;
import com.google.gwt.resources.client.ImageResource;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.resources.FieldDefResource;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.model.DataModelDef;
import csi.server.common.model.FieldDef;
import csi.server.common.model.FieldType;
import csi.server.common.model.column.ColumnDef;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class FieldDefUtils {

    public enum SortOrder {
        ALPHABETIC, ORDINAL;
    }

    public static Comparator<FieldDef> SORT_ALPHABETIC = new Comparator<FieldDef>() {
        @Override
        public int compare(FieldDef o1, FieldDef o2) {
            if (o1.getFieldName() != null && o2.getFieldName() != null) {
                return o1.getFieldName().compareTo(o2.getFieldName());
            } else if (o2.getFieldName() != null) {
                return 1;
            } else {
                return -1;
            }
        }
    };

    public static Comparator<FieldDef> SORT_ORDINAL = new Comparator<FieldDef>() {
        @Override
        public int compare(FieldDef o1, FieldDef o2) {
            return Ints.compare(o1.getOrdinal(), o2.getOrdinal());
        }
    };

    public static List<FieldDef> getSortedReferenceFields(DataModelDef dataModel, final SortOrder sortOrderIn) {

        return sortFields(dataModel.getFieldListAccess().getReferenceFieldDefs(), sortOrderIn);
    }

    public static List<FieldDef> getSortedNonStaticFields(DataModelDef dataModel, final SortOrder sortOrderIn) {

        return sortFields(dataModel.getFieldListAccess().getNonStaticFieldDefs(), sortOrderIn);
    }

    public static List<FieldDef> getAllSortedFields(DataModelDef dataModel, final SortOrder sortOrderIn) {

        return sortFields(dataModel.getFieldListAccess().getSafeFieldList(), sortOrderIn);
    }

    public static List<FieldDef> getSortedStaticFields(DataModelDef dataModel, final SortOrder sortOrderIn) {

        return sortFields(dataModel.getFieldListAccess().getStaticFieldDefs(), sortOrderIn);
    }
    

    public static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();

    public static List<FieldDef> sortFields(List<FieldDef> listIn, final SortOrder order) {

        Collections.sort(listIn, new Comparator<FieldDef>() {

            @Override
            public int compare(FieldDef o1, FieldDef o2) {
                switch (order) {
                    case ALPHABETIC:
                        if (o1.getFieldName() != null && o2.getFieldName() != null) {
                            return o1.getFieldName().compareTo(o2.getFieldName());
                        } else if (o2.getFieldName() != null) {
                            return 1;
                        } else {
                            return -1;
                        }
                    case ORDINAL:
                        return Ints.compare(o1.getOrdinal(), o2.getOrdinal());
                    default:
                        throw new RuntimeException(i18n.fieldDefOrderException()); //$NON-NLS-1$
                }
            }
        });

        return listIn;
    }

    public static ImageResource getFieldTypeImage(FieldType type) {
        switch (type) {
            case COLUMN_REF:
            case LINKUP_REF:
                return FieldDefResource.IMPL.fieldColumnRef();
            case DERIVED:
            case SCRIPTED:
                return FieldDefResource.IMPL.fieldScripted();
            case STATIC:
                return FieldDefResource.IMPL.fieldStatic();
            default:
                return FieldDefResource.IMPL.valueUnknown();
        }
    }

    public static ImageResource getColumnDataTypeImage(ColumnDef columnIn) {

		return getDataTypeImage(columnIn.getCsiType());
    }

    public static ImageResource getFieldDataTypeImage(FieldDef fieldIn) {

        return getDataTypeImage(fieldIn.getValueType());
    }

    public static ImageResource getMenuChevron() {
        return FieldDefResource.IMPL.contextMenu();
    }

    public static ImageResource getDataTypeImage(CsiDataType dataType) {
        switch (dataType) {
            case Boolean:
                return FieldDefResource.IMPL.valueBoolean();
            case Date:
                return FieldDefResource.IMPL.valueDate();
            case DateTime:
                return FieldDefResource.IMPL.valueDateTime();
            case Integer:
                return FieldDefResource.IMPL.valueInteger();
            case Number:
                return FieldDefResource.IMPL.valueNumber();
            case String:
                return FieldDefResource.IMPL.valueString();
            case Time:
                return FieldDefResource.IMPL.valueTime();
            case Unsupported:
                return FieldDefResource.IMPL.valueUnknown();
            default:
                return FieldDefResource.IMPL.valueUnknown();
        }
    }
}
