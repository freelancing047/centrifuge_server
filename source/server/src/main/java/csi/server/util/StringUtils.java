package csi.server.util;

import csi.server.common.enumerations.CsiDataType;

public class StringUtils {
   public static String asString(Object o) {
      return (String) CsiTypeUtil.coerceType(o, CsiDataType.String, null);
   }
}
