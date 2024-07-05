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

import java.io.Serializable;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
@SuppressWarnings("serial")
public class FilterPickEntry implements Serializable {
   public static final String LABEL_VALUE = "Value";
   public static final String LABEL_FREQUENCY = "Frequency";

   private String value;
   private int frequency;

   public String getValue() {
      return value;
   }

   public void setValue(String value) {
      this.value = value;
   }

   public int getFrequency() {
      return frequency;
   }

   public void setFrequency(int frequency) {
      this.frequency = frequency;
   }
}
