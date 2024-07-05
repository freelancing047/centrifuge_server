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
package csi.server.common.model;

import java.io.Serializable;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * Holds a reference to a field by field type and ordinal. This is safe across "Save as template" and "DV from template"
 * operations.
 * @author Centrifuge Systems, Inc.
 *
 */
@SuppressWarnings("serial")
@XStreamAlias("FieldReference")
public class FieldReference implements Serializable {

    @XStreamAsAttribute
    private String fieldDefLocalId;
    @XStreamAsAttribute
    private FieldType fieldType;
    private FieldDef field;

    public FieldReference() {
        super();
    }

    public FieldReference(FieldDef field) {
        setField(field);
        setFieldDefLocalId(field.getLocalId());
        setFieldType(field.getFieldType());
    }

    public FieldDef getField() {
        return field;
    }

    public void setField(FieldDef field) {
        this.field = field;
    }

    public String getFieldDefLocalId() {
        return fieldDefLocalId;
    }

    public void setFieldDefLocalId(String localId) {
        this.fieldDefLocalId = localId;
    }

    public FieldType getFieldType() {
        return fieldType;
    }

    public void setFieldType(FieldType fieldType) {
        this.fieldType = fieldType;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getFieldDefLocalId(), getFieldType());
    }

   @Override
   public boolean equals(Object obj) {
      return (this == obj) ||
             ((obj != null) &&
              (obj instanceof FieldReference) &&
              Objects.equal(getFieldDefLocalId(), ((FieldReference) obj).getFieldDefLocalId()) &&
              Objects.equal(getFieldType(), ((FieldReference) obj).getFieldType()));
   }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)//
                .add("fieldDefLocalId", getFieldDefLocalId()) //
                .add("fieldType", getFieldType()).toString();
    }
}
