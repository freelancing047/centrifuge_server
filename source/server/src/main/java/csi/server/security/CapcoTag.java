package csi.server.security;

/**
 * Created by centrifuge on 4/20/2015.
 */
public class CapcoTag {

    String portionText;
    String bannerText;
    String abreviation;
    int level;

    public CapcoTag(String portionTextIn, String bannerTextIn, String abreviationIn, int levelIn) {

        portionText = portionTextIn;
        bannerText = bannerTextIn;
        abreviation = abreviationIn;
        level = levelIn;
    }
}
