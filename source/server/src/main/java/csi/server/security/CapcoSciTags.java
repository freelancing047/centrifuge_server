package csi.server.security;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import csi.server.common.enumerations.CapcoSection;
import csi.server.common.util.ValuePair;

/**
 * Created by centrifuge on 4/20/2015.
 *
 * "initialTags" contains markings in the order they appear in the register.
 * "_portionToBannerMapping" contains markings in alphabetical order.
 * Banner text should be obtained from _portionToBannerMapping not initialTags
 * since user changes only appear in _portionToBannerMapping.
 */
public class CapcoSciTags extends AbstractCapcoTagProcessing {

    private static List<CapcoTag> initialTags = new ArrayList<CapcoTag>();

    private static final String[] _delimeter = new String[] {"/", "-", " "};

    private static ValuePair<List<Map<String, String>>, List<Map<String, String>>> portionToBannerMapping;

    static {

        initialTags.add(new CapcoTag("HCS", "HCS", "HCS", 0));
        initialTags.add(new CapcoTag("KDK", "KLONDIKE", "KDK", 0));
        initialTags.add(new CapcoTag("RSV", "RESERVE", "RSV", 0));
        initialTags.add(new CapcoTag("SI", "SI", "SI", 0));
        initialTags.add(new CapcoTag("TK", "TALENT KEYHOLE", "TK", 0));

        portionToBannerMapping = initializeMap(initialTags, _delimeter.length);
    }

    public static void updateMapping(List<CapcoTag> updatesIn) {

        portionToBannerMapping = updateMap(initialTags, _delimeter.length, updatesIn);
    }

    public CapcoSciTags() {

        super(_delimeter, initialTags, portionToBannerMapping.getValue1(), portionToBannerMapping.getValue2());
    }

    public CapcoSection getEnum() {

        return CapcoSection.SCI;
    }

    public int getOrdinal() {

        return CapcoSection.SCI.ordinal();
    }

    @Override
    protected String[] genLabelText() {

        return processDescendingAlphaTags();
    }
}
