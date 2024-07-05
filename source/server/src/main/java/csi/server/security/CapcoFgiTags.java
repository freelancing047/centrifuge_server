package csi.server.security;

import java.util.ArrayList;
import java.util.Collection;
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
public class CapcoFgiTags extends AbstractCapcoTagProcessing {

    private static List<CapcoTag> initialTags = new ArrayList<CapcoTag>();

    private static final String _portion = "FGI";
    private static final String _banner = "FOREIGN GOVERNMENT INFORMATION";
    private static final String _abreviation = "FGI";
    private static final String[] _delimeter = new String[] {"/", " "};
    private static final CapcoTag _prefix = new CapcoTag(_portion, _banner, _abreviation, 0);

    private static ValuePair<List<Map<String, String>>, List<Map<String, String>>> portionToBannerMapping;

    static {

        initialTags.add(_prefix);

        portionToBannerMapping = initializeMap(initialTags, _delimeter.length);
    }

    public static void updateMapping(List<CapcoTag> updatesIn) {

        portionToBannerMapping = updateMap(initialTags, _delimeter.length, updatesIn);
    }

    public CapcoFgiTags() {

        super(_delimeter, initialTags, portionToBannerMapping.getValue1(), portionToBannerMapping.getValue2());
    }

    public CapcoSection getEnum() {

        return CapcoSection.FGI;
    }

    public int getOrdinal() {

        return CapcoSection.FGI.ordinal();
    }

    @Override
    public Collection<String> getPortionList() {

        return new ArrayList<String>();
    }

    @Override
    protected String[] genLabelText() {

        if ((null == portionMap) || (0 < portionMap.size())) {

            StringBuilder[] myBuffer = new StringBuilder[] {new StringBuilder(), new StringBuilder(), new StringBuilder()};

            myBuffer[0].append(_portion);
            myBuffer[1].append(_banner);
            myBuffer[2].append(_abreviation);

            if (null != portionMap) {

                for (String myTag : portionMap.keySet()) {

                    for (int i = 0; 3 > i; i++) {

                        myBuffer[i].append(" ");
                        myBuffer[i].append(myTag.substring(4));
                    }
                }
            }

            return new String[] {myBuffer[0].toString(), myBuffer[1].toString(), myBuffer[2].toString()};

        } else {

            return null;
        }
    }
}
