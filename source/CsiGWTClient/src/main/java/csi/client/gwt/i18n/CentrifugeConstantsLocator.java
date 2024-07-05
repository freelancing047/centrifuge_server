package csi.client.gwt.i18n;

public class CentrifugeConstantsLocator {

    private static CentrifugeConstants instance;
//    final private static CentrifugeConstants centrifugeConstants = WebMain.injector.getCentrifugeConstants();

    public static void set(CentrifugeConstants centrifugeConstants) {
    	instance = centrifugeConstants;
    }

    public static CentrifugeConstants get() {
        return instance;
    }

}
