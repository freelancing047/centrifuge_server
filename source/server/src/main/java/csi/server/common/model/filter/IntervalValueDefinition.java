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
package csi.server.common.model.filter;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import csi.server.common.enumerations.CsiDataType;
import csi.server.common.enumerations.OperandCardinality;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
@SuppressWarnings("serial")
@XStreamAlias("IntervalValueDefinition")
public class IntervalValueDefinition<T> implements ValueDefinition {
   @XStreamAsAttribute
   private CsiDataType dataType;
   @XStreamAsAttribute
   private OperandTypeAndValue<T> start;
   @XStreamAsAttribute
   private OperandTypeAndValue<T> end;
   @XStreamAsAttribute
   private boolean startInclusive = true;
   @XStreamAsAttribute
   private boolean endInclusive = true;

   public CsiDataType getDataType() {
      return dataType;
   }

   public OperandTypeAndValue<T> getStart() {
      return start;
   }

   public OperandTypeAndValue<T> getEnd() {
      return end;
   }

   public boolean isStartInclusive() {
      return startInclusive;
   }

   public boolean isEndInclusive() {
      return endInclusive;
   }

   public void setDataType(CsiDataType dataType) {
      this.dataType = dataType;
   }

   public void setStart(OperandTypeAndValue<T> start) {
      this.start = start;
   }

   public void setEnd(OperandTypeAndValue<T> end) {
      this.end = end;
   }

   public void setStartInclusive(boolean startInclusive) {
      this.startInclusive = startInclusive;
   }

   public void setEndInclusive(boolean endInclusive) {
      this.endInclusive = endInclusive;
   }

   @Override
   public OperandCardinality getCardinality() {
      return OperandCardinality.RANGE;
   }

   @Override
   public String getShortValueDescription() {
      return new StringBuilder(startInclusive ? "[" : "(")
                       .append(start)
                       .append(" ... ")
                       .append(end)
                       .append(endInclusive ? "]" : ")")
                       .toString();
   }

   @Override
   public String getValueDescription() {
      return getShortValueDescription();
   }

   @Override
   public ValueDefinition cloneThis() {
      IntervalValueDefinition<T> vd = new IntervalValueDefinition<T>();

      vd.setDataType(dataType);
      vd.setStart(start);
      vd.setEnd(end);
      vd.setStartInclusive(startInclusive);
      vd.setEndInclusive(endInclusive);
      return vd;
   }
}
