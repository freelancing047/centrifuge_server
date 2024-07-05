package csi.server.business.visualization.map.cacheloader.pointcounter;

public abstract class AbstractHundredTreeNode {
   int childrenPrecision;

   public abstract void addCoordinate(String latitude, String longitude);

   public int getKey(String s) {
      int decimalPlace = s.indexOf(".");
      if (decimalPlace == -1) {
         if (childrenPrecision > 0) {
            return 0;
         }
         int i = (s.length() + childrenPrecision) - 1;
         return getKey(s, i);
      } else {
         if (childrenPrecision > 0) {
            int i = decimalPlace + childrenPrecision;
            if (s.length() > i) {
               char ch = s.charAt(i);
               return Character.getNumericValue(ch);
            }
            return 0;
         } else {
            int i = (decimalPlace + childrenPrecision) - 1;
            return getKey(s, i);
         }
      }
   }

   private int getKey(String s, int i) {
      if (i < 0) {
         return 0;
      } else {
         char ch = s.charAt(i);
         if (ch == '-') {
            return 0;
         }
         return Character.getNumericValue(ch);
      }
   }
}
