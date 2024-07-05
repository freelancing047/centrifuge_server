package csi.server.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

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
public class CapcoClassTags extends AbstractCapcoTagProcessing {
   private static final Pattern DOUBLE_SLASH_PATTERN = Pattern.compile("//");

    private static List<CapcoTag> usInitialTags = new ArrayList<CapcoTag>();
//    private static List<CapcoTag> natoInitialTags = new ArrayList<CapcoTag>();
//    private static List<CapcoTag> nonusInitialTags = new ArrayList<CapcoTag>();

    private static final String[] _delimeter = new String[] {""};

    private static ValuePair<List<Map<String, String>>, List<Map<String, String>>> usPortionToBannerMapping;
/*
    private static ValuePair<List<Map<String, String>>, List<Map<String, String>>> natoPortionToBannerMapping;
    private static ValuePair<List<Map<String, String>>, List<Map<String, String>>> nonusPortionToBannerMapping;
*/
    private static Map<String, String> usPortionTestMap;
    private static Map<String, String> natoPortionTestMap;
    private static Map<String, String> nonusPortionTestMap;

    static {

        usInitialTags.add(new CapcoTag("TS", "TOP SECRET", "TOP SECRET", 0));
        usInitialTags.add(new CapcoTag("S", "SECRET", "SECRET", 0));
        usInitialTags.add(new CapcoTag("C", "CONFIDENTIAL", "CONFIDENTIAL", 0));
        usInitialTags.add(new CapcoTag("U", "UNCLASSIFIED", "UNCLASSIFIED", 0));

        usPortionToBannerMapping = initializeMap(usInitialTags, _delimeter.length);
        usPortionTestMap = usPortionToBannerMapping.getValue1().get(0);
    }

    static {
/*
        natoInitialTags.add(new CapcoTag("CTS", "COSMIC TOP SECRET", "COSMIC TOP SECRET", 0));
        natoInitialTags.add(new CapcoTag("CTS-B", "COSMIC TOP SECRET-BOHEMIA", "COSMIC TOP SECRET-BOHEMIA", 0));
        natoInitialTags.add(new CapcoTag("CTS-BALK", "COSMIC TOP SECRET-BALK", "COSMIC TOP SECRET-BALK", 0));
        natoInitialTags.add(new CapcoTag("NS", "NATO SECRET", "NATO SECRET", 0));
        natoInitialTags.add(new CapcoTag("NC", "NATO CONFIDENTIAL", "NATO CONFIDENTIAL", 0));
        natoInitialTags.add(new CapcoTag("NR", "NATO RESTRICTED", "NATO RESTRICTED", 0));
        natoInitialTags.add(new CapcoTag("NU", "NATO UNCLASSIFIED", "NATO UNCLASSIFIED", 0));

        natoInitialTags.add(new CapcoTag("CTSA", "COSMIC TOP SECRET ATOMAL", "COSMIC TOP SECRET ATOMAL", 0));
        natoInitialTags.add(new CapcoTag("NSAT", "SECRET ATOMAL", "SECRET ATOMAL", 0));
        natoInitialTags.add(new CapcoTag("NCA", "CONFIDENTIAL ATOMAL", "CONFIDENTIAL ATOMAL", 0));

        natoPortionToBannerMapping = initializeMap(natoInitialTags, _delimeter.length);
        natoPortionTestMap = natoPortionToBannerMapping.getValue1().get(0);
*/
        natoPortionTestMap = new TreeMap<String, String>();

        natoPortionTestMap.put("CTS", "TS");
        natoPortionTestMap.put("CTS-B", "TS");
        natoPortionTestMap.put("CTS-BALK", "TS");
        natoPortionTestMap.put("NS", "S");
        natoPortionTestMap.put("NC", "C");
        natoPortionTestMap.put("NR", "C");
        natoPortionTestMap.put("NU", "U");

        natoPortionTestMap.put("CTSA", "TS");
        natoPortionTestMap.put("NSAT", "S");
        natoPortionTestMap.put("NCA", "C");
    }

    static {
/*
        nonusInitialTags.add(new CapcoTag("TS", "TOP SECRET", "TOP SECRET", 0));
        nonusInitialTags.add(new CapcoTag("S", "SECRET", "SECRET", 0));
        nonusInitialTags.add(new CapcoTag("C", "CONFIDENTIAL", "CONFIDENTIAL", 0));
        nonusInitialTags.add(new CapcoTag("R", "RESTRICTED", "RESTRICTED", 0));
        nonusInitialTags.add(new CapcoTag("U", "UNCLASSIFIED", "UNCLASSIFIED", 0));

        nonusPortionToBannerMapping = initializeMap(nonusInitialTags, _delimeter.length);
        nonusPortionTestMap = nonusPortionToBannerMapping.getValue1().get(0);
*/
        nonusPortionTestMap = new TreeMap<String, String>();

        nonusPortionTestMap.put("TS", "TS");
        nonusPortionTestMap.put("S", "S");
        nonusPortionTestMap.put("C", "C");
        nonusPortionTestMap.put("R", "C");
        nonusPortionTestMap.put("U", "U");
    }

    private boolean _isClassified = false;
/*
    private Map<String, String[]> usPortionMap;
    private Map<String, String[]> natoPortionMap;
    private Map<String, String[]> nonusPortionMap;
    private Map<String, String[]> bogusPortionMap;
*/
    private CapcoFgiTags fgiProcess;
    private String _bogusTag;

    public static void updateMapping(List<CapcoTag> updatesIn) {

    }

    public CapcoClassTags() {

        super(_delimeter, usInitialTags, usPortionToBannerMapping.getValue1(), usPortionToBannerMapping.getValue2());
//        super();
//        usPortionToBannerMapping = initializeMap(usInitialTags, _delimeter.length);
//        usPortionTestMap = usPortionToBannerMapping.getValue1().get(0);
//        setAll(_delimeter, usInitialTags, usPortionToBannerMapping.getValue1(), usPortionToBannerMapping.getValue2());
    }

    public CapcoSection getEnum() {

        return CapcoSection.CLASS;
    }

    public int getOrdinal() {

        return CapcoSection.CLASS.ordinal();
    }

    @Override
    public Collection<String> getPortionList() {

        Collection<String> myResult = new ArrayList<String>();

        if ((null != portionMap) && !portionMap.isEmpty()) {

            for (CapcoTag myTag : usInitialTags) {

                if (portionMap.containsKey(myTag.portionText)) {

                    myResult.add(myTag.portionText);
                    break;
                }
            }
        }
        if (myResult.isEmpty()) {

            myResult.add("U");
        }
        return myResult;
    }

    @Override
    protected String[] genLabelText() {

        String[] myResult = null;

        if ((null != portionMap) && !portionMap.isEmpty()) {

            for (CapcoTag myTag : usInitialTags) {

                if (portionMap.containsKey(myTag.portionText)) {

                    myResult = new String[] {myTag.portionText, myTag.bannerText, myTag.abreviation};
                    break;
                }
            }
        }
        return (myResult == null) ? new String[] {"U", "UNCLASSIFIED", "UNCLASSIFIED"} : myResult;
    }

    @Override
    public boolean parsePortionTag(String[] topSplitIn, boolean classifiedIn, boolean forceIn) {

        return false;
    }

    public String[] extractClassificationTag(String portionTextIn, CapcoFgiTags fgiProcessIn) {

        String[] myResult = null;

        fgiProcess = fgiProcessIn;
        _isClassified = false;
        _bogusTag = null;

        if ((null != portionTextIn) && (0 < portionTextIn.length())) {

            String[] mySections = DOUBLE_SLASH_PATTERN.split(portionTextIn);
            int myLimit = mySections.length;
            int myIndex = 0;

            if ((0 < myLimit) && (0 < mySections[0].length())) {

                parseUsTag(mySections[0]);
                myIndex = 1;

            } else if ((1 < myLimit) && (0 < mySections[1].length())) {

                parseOtherTag(mySections[1]);
                myIndex = 2;
            }
            if (0 < myIndex) {

                myResult = new String[myLimit - myIndex];

                for (int i = 0, j = myIndex; myLimit > j; i++, j++) {

                    myResult[i] = mySections[j];
                }
            }
        }

        return myResult;
    }

    public boolean isClassified() {

        return _isClassified;
    }

    public String getBadClassification() {

        return _bogusTag;
    }

    private boolean parseUsTag(String portionTagIn) {

        boolean mySuccess = false;

        if (usPortionTestMap.containsKey(portionTagIn)) {

            portionMap.put(portionTagIn, new String[] {portionTagIn});
            _isClassified = !"U".equals(portionTagIn);
            mySuccess = true;

        } else {

            _bogusTag = portionTagIn;
        }

        return mySuccess;
    }

    private boolean parseOtherTag(String portionTagIn) {

        boolean mySuccess = false;
        String myUsNatoTag = natoPortionTestMap.get(portionTagIn);

        if (null != myUsNatoTag) {

            if (fgiProcess.parsePortionTag(new String[] {"FGI NATO"}, false, false)) {

                portionMap.put(myUsNatoTag, new String[]{myUsNatoTag});
                _isClassified = !"U".equals(portionTagIn);
                mySuccess = true;
            }

        } else {

            String[] myKeys = portionTagIn.split(" ");
            String myUsForeignTag = nonusPortionTestMap.get(myKeys[1]);

            if (null != myUsForeignTag) {

                if (parseJointTag(myKeys) || parseForeignTag(myKeys)) {

                    portionMap.put(myUsNatoTag, new String[]{myUsForeignTag});
                    _isClassified = !"U".equals(myUsForeignTag);
                    mySuccess = true;
                }
            }
        }

        if (!mySuccess) {

            _bogusTag = portionTagIn;
        }

        return mySuccess;
    }

    private boolean parseJointTag(String[] portionTagsIn) {

        boolean mySuccess = false;

        if ("JOINT".equals(portionTagsIn[0])) {

            if (3 < portionTagsIn.length) {

                StringBuilder myBuffer = new StringBuilder("FGI");

                for (int i = 2; portionTagsIn.length > i; i++) {

                    if (!"USA".equals(portionTagsIn[i])) {

                        myBuffer.append(" ");
                        myBuffer.append(portionTagsIn[i]);
                    }
                }

                mySuccess = fgiProcess.parsePortionTag(new String[] {myBuffer.toString()}, false, false);
            }
        }

        return mySuccess;
    }

    private boolean parseForeignTag(String[] portionTagsIn) {

        boolean mySuccess = false;
        String myTag = portionTagsIn[0];

        if ((3 == myTag.length()) || (4 == myTag.length())) {

            mySuccess = fgiProcess.parsePortionTag(new String[] {"FGI " + myTag}, false, false);
        }

        return mySuccess;
    }
}
