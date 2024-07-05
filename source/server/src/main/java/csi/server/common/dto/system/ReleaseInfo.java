package csi.server.common.dto.system;

/**
 * Created by centrifuge on 8/29/2018.
 */
public class ReleaseInfo {

    public static String version;
    public static String build;

    public static void initialize(String versionIn, String buildIn) {

        version = versionIn;
        build = buildIn;
    }
}
