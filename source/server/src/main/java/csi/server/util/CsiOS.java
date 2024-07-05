package csi.server.util;

/**
 * Created by centrifuge on 9/6/2018.
 */
public enum CsiOS {
   WINDOWS("Windows", "win"),
   MAC("MacOS", "mac"),
   LINUX("Linux", "nux"),
   UNIX("Unix", "nix"),
   SOLARIS("Solaris", "sunos"),
   AIX("Aix", "aix"),
   OTHER("Other", null);

   private static CsiOS _current = null;
   private static String _name = System.getProperty("os.name").toLowerCase();

   private String _label;
   private String _key;

   private CsiOS(String labelIn, String keyIn) {
      _label = labelIn;
      _key = keyIn;
   }

   public static boolean isWindows() {
      return (WINDOWS == getCurrent());
   }

   public static boolean isLinuxOrUnix() {
      CsiOS current = getCurrent();

      return (LINUX == current) || (UNIX == current) || (SOLARIS == current)
            || (AIX == current) || (MAC == current);
   }

   public static CsiOS getCurrent() {
      if (null == _current) {
         _current = OTHER;

         for (CsiOS myOS : CsiOS.values()) {
            String myKey = myOS.getKey();

            if ((null != myKey) && (_name.indexOf(myKey) >= 0)) {
               _current = myOS;
               break;
            }
         }
      }
      return _current;
   }

   String getKey() {
      return _key;
   }
}
