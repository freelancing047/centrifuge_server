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
package csi.server.util;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class NumberHelper {
   private NumberHelper() {
   }

   public static boolean isNumeric(final String str) {
      boolean success = false;

      if (str != null) {
         boolean periodFound = false;
         boolean signFound = false;

         for (char ch : str.trim().toCharArray()) {
            if (Character.isDigit(ch)) {
               success = true;
            } else {
               if (ch == '.') {
                  if (periodFound) {
                     success = false;
                     break;
                  }
                  periodFound = true;
               } else if ((ch == '+') || (ch == '-')) {
                  if (success || periodFound || signFound) {
                     success = false;
                     break;
                  }
                  signFound = true;
               } else {
                  success = false;
                  break;
               }
            }
         }
      }
      return success;
   }
}
