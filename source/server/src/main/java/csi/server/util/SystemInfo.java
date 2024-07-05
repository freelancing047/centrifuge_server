package csi.server.util;

// Class to return System information regarding the runtime environment

public class SystemInfo {

    public static String getJavaVersion() {
        return System.getProperty("java.version");
    }

    public static String getOSName() {
        return System.getProperty("os.name");
    }

    public static String getOSVersion() {
        return System.getProperty("os.version");
    }

    public static String getBuildNumber() {
        return BuildNumber.getBuildNumber();
    }

    public static String getReleaseVersion() {
        return Version.getVersionString();
    }

    public static String getFreeMemory() {
        return Long.toString(Runtime.getRuntime().freeMemory());
    }

    public static String getTotalMemory() {
        return Long.toString(Runtime.getRuntime().totalMemory());
    }
}
