package csi.server.security;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import csi.server.common.enumerations.CapcoSection;
import csi.server.common.util.ValuePair;

/**
 * Created by centrifuge on 4/16/2015.
 *
 * "initialTags" contains markings in the order they appear in the register.
 * "portionToBannerMapping" contains markings in alphabetical order.
 * Banner text should be obtained from portionToBannerMapping not initialTags
 * since user changes only appear in portionToBannerMapping.
 */
public class CapcoAeaTags extends AbstractCapcoTagProcessing {

    private static List<CapcoTag> initialTags = new ArrayList<CapcoTag>();

    private static final String[] _delimeter = new String[] {"/", "-", " "};

    private static ValuePair<List<Map<String, String>>, List<Map<String, String>>> portionToBannerMapping;

    static {

        initialTags.add(new CapcoTag("RD", "RESTRICTED DATA", "RD", 0));
        initialTags.add(new CapcoTag("FRD", "FORMERLY RESTRICTED DATA", "FRD", 0));
        initialTags.add(new CapcoTag("DOD UCNI", "DOD UNCLASSIFIED CONTROLLED NUCLEAR INFORMATION", "DOD UCNI", 0));
        initialTags.add(new CapcoTag("DOE UCNI", "DOE UNCLASSIFIED CONTROLLED NUCLEAR INFORMATION", "DOE UCNI", 0));
        initialTags.add(new CapcoTag("TFNI", "TRANSCLASSIFIED FOREIGN NATIONAL INFORMATION", "TFNI", 0));
        initialTags.add(new CapcoTag("CNWDI", "CRITICAL NUCLEAR WEAPON DESIGN INFORMATION", "CNWDI", 1));
        initialTags.add(new CapcoTag("SG", "SIGMA", "SIGMA", 1));
        initialTags.add(new CapcoTag("N", "N", "N", 1));

        portionToBannerMapping = initializeMap(initialTags, _delimeter.length);
    }

    public static void updateMapping(List<CapcoTag> updatesIn) {

        portionToBannerMapping = updateMap(initialTags, _delimeter.length, updatesIn);
    }

    public CapcoAeaTags() {

        super(_delimeter, initialTags, portionToBannerMapping.getValue1(), portionToBannerMapping.getValue2());
    }

    public CapcoSection getEnum() {

        return CapcoSection.AEA;
    }

    public int getOrdinal() {

        return CapcoSection.AEA.ordinal();
    }

    @Override
    protected String[] genLabelText() {

        return processDescendingAlphaTags();
    }
}
