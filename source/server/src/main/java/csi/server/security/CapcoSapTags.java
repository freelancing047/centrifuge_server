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
 * "portionToBannerMapping" contains markings in alphabetical order.
 * Banner text should be obtained from portionToBannerMapping not initialTags
 * since user changes only appear in portionToBannerMapping.
 */
public class CapcoSapTags extends AbstractCapcoTagProcessing {

    private static List<CapcoTag> initialTags = new ArrayList<CapcoTag>();

    private static final String[] _delimeter = new String[] {"/", "-", " "};
    private static final CapcoTag _prefix = new CapcoTag("SAR", "SPECIAL ACCESS REQUIRED", "SAR", 0);

    private static ValuePair<List<Map<String, String>>, List<Map<String, String>>> portionToBannerMapping;

    static {

        initialTags.add(_prefix);

        portionToBannerMapping = initializeMap(initialTags, _delimeter.length);
    }

    public static void updateMapping(List<CapcoTag> updatesIn) {

        portionToBannerMapping = updateMap(initialTags, _delimeter.length, updatesIn);
    }

    public CapcoSapTags() {

        super(_delimeter, initialTags, portionToBannerMapping.getValue1(), portionToBannerMapping.getValue2());
        prefix = new String[]{_prefix.portionText, _prefix.bannerText, _prefix.abreviation};
    }

    public CapcoSection getEnum() {

        return CapcoSection.SAP;
    }

    public int getOrdinal() {

        return CapcoSection.SAP.ordinal();
    }

    @Override
    protected String[] genLabelText() {

        return processDescendingAlphaTags();
    }
}
