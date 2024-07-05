package csi.server.util;

import java.util.StringJoiner;

import csi.VersionData;

/**
 * Created by centrifuge on 11/28/2018.
 */
public class Version extends VersionData {
   private static String versionString;

   static {
      StringJoiner joiner = new StringJoiner(".");

      joiner.add(Integer.toString(MAJOR_VERSION));
      joiner.add(Integer.toString(MINOR_VERSION));

      if ((MAJOR_SUB_VERSION > 0) || (MINOR_SUB_VERSION > 0)) {
         joiner.add(Integer.toString(MAJOR_SUB_VERSION));
      }
      if (MINOR_SUB_VERSION > 0) {
         joiner.add(Integer.toString(MINOR_SUB_VERSION));
      }
      versionString = joiner.toString();
   }

   public static int getMajorVersion() {
      return MAJOR_VERSION;
   }
   public static int getMinorVersion() {
      return MINOR_VERSION;
   }

   public static int getSubMajorVersion() {
      return MAJOR_SUB_VERSION;
   }
   public static int getMinorSubVersion() {
      return MINOR_SUB_VERSION;
   }

   public static String getVersionString() {
      return versionString;
   }

   @Override
   public String toString() {
      return versionString;
   }
}
