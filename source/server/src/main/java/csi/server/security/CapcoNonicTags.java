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
public class CapcoNonicTags extends AbstractCapcoTagProcessing {

    private static List<CapcoTag> initialTags = new ArrayList<CapcoTag>();

    private static final String[] _delimeter = new String[] {"/"};

    private static ValuePair<List<Map<String, String>>, List<Map<String, String>>> portionToBannerMapping;

    static {

        initialTags.add(new CapcoTag("DS", "LIMITED DISTRIBUTION", "LIMDIS", 0));
        initialTags.add(new CapcoTag("XD", "EXCLUSIVE DISTRIBUTION", "EXDIS", 0));
        initialTags.add(new CapcoTag("ND", "NO DISTRIBUTION", "NODIS", 0));
        initialTags.add(new CapcoTag("SBU", "SENSITIVE BUT UNCLASSIFIED", "SBU", 0));
        initialTags.add(new CapcoTag("SBU-NF", "SENSITIVE BUT UNCLASSIFIED NOFORN", "SBU NOFORN", 0));
        initialTags.add(new CapcoTag("LES", "LAW ENFORCEMENT SENSITIVE", "LES", 0));
        initialTags.add(new CapcoTag("LES-NF", "LAW ENFORCEMENT SENSITIVE NOFORN", "LES NOFORN", 0));
        initialTags.add(new CapcoTag("SSI", "SPECIAL SECURITY INFORMATION", "SSI", 0));

        portionToBannerMapping = initializeMap(initialTags, _delimeter.length);
    }

    public static void updateMapping(List<CapcoTag> updatesIn) {

        portionToBannerMapping = updateMap(initialTags, _delimeter.length, updatesIn);
    }

    public CapcoNonicTags() {

        super(_delimeter, initialTags, portionToBannerMapping.getValue1(), portionToBannerMapping.getValue2());
    }

    public CapcoSection getEnum() {

        return CapcoSection.NONIC;
    }

    public int getOrdinal() {

        return CapcoSection.NONIC.ordinal();
    }

    @Override
    protected String[] genLabelText() {

        if ((null != portionMap) && (0 < portionMap.size())) {

            StringBuilder[] myBuffer = new StringBuilder[] {new StringBuilder(), new StringBuilder(), new StringBuilder()};
            String myDelimeter = "";

            for (CapcoTag myTag : initialTags) {

                if (portionMap.containsKey(myTag.portionText)) {

                    portionMap.remove(myTag.portionText);
                    myBuffer[0].append(myDelimeter);
                    myBuffer[0].append(myTag.portionText);
                    myBuffer[1].append(myDelimeter);
                    myBuffer[1].append(myTag.bannerText);
                    myBuffer[2].append(myDelimeter);
                    myBuffer[2].append(myTag.abreviation);
                    myDelimeter = "/";
                }
            }

            return new String[] {myBuffer[0].toString(), myBuffer[1].toString(), myBuffer[2].toString()};

        } else {

            return null;
        }
    }
}
