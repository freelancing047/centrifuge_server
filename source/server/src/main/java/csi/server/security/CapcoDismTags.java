package csi.server.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
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
public class CapcoDismTags extends AbstractCapcoTagProcessing {

    private static List<CapcoTag> initialTags = new ArrayList<CapcoTag>();

    private static final String[] _delimeter = new String[] {"/"};
    private static final Pattern CONJUNCTION_PATTERN = Pattern.compile(", ");

    private static final String noRelToKey = "NREL";
    private static final String noDisplayOnlyKey = "NDISP";
    private static final String noFornKey = "NF";
    private static final String relidoKey = "RELIDO";
    private static final String relToKey = "REL";
    private static final String relToTag = "REL TO USA, ";
    private static final String displayOnlyKey = "DISPLAY ONLY";
    private static final String displayOnlyTag = "DISPLAY ONLY ";
    private static final String eyesOnlyKey = "EYES";
    private static final String eyesOnlyTag = " EYES ONLY";
    private static final Map<String, String[]> tetraGraphMap = new HashMap<String, String[]>();
    private static final int relToBase = relToTag.length();
    private static final int displayOnlyBase = displayOnlyTag.length();

    private static boolean _doStrictRelTo = false;
    private static boolean _doStrictDisplay = false;

    private static ValuePair<List<Map<String, String>>, List<Map<String, String>>> portionToBannerMapping;

    static {

        tetraGraphMap.put("TEYE", new String[] {"AUS", "GBR"});
        tetraGraphMap.put("ACGU", new String[] {"AUS", "CAN", "GBR"});
        tetraGraphMap.put("FVEY", new String[] {"AUS", "CAN", "GBR", "NZL"});

        initialTags.add(new CapcoTag("RS", "RISK SENSITIVE", "RSEN", 0));
        initialTags.add(new CapcoTag("FOUO", "FOR OFFICIAL USE ONLY", "FOUO", 0));
        initialTags.add(new CapcoTag("OC", "ORIGINATOR CONTROLLED", "ORCON", 0));
        initialTags.add(new CapcoTag("IMC", "CONTROLLED IMAGERY", "IMCON", 0));
        initialTags.add(new CapcoTag("NF", "NOT RELEASABLE TO FOREIGN NATIONALS", "NOFORN", 0));
        initialTags.add(new CapcoTag("PR", "CAUTION-PROPRIETARY INFORMATION INVOLVED", "PROPIN", 0));
/***/   initialTags.add(new CapcoTag("REL", "AUTHORIZED FOR RELEASE", "REL", 0));
        initialTags.add(new CapcoTag("RELIDO", "RELEASABLE BY INFORMATION DISCLOSURE OFFICIAL", "RELIDO", 0));
/***/   initialTags.add(new CapcoTag("EYES", "EYES ONLY", "EYES ONLY", 0));
        initialTags.add(new CapcoTag("DSEN", "DEA SENSITIVE", "DEA SENSITIVE", 0));
        initialTags.add(new CapcoTag("FISA", "FOREIGN INTELLIGENCE SURVEILLANCE ACT", "FISA", 0));
/***/   initialTags.add(new CapcoTag("DISPLAY ONLY", "DISPLAY ONLY", "DISPLAY ONLY", 0));

        portionToBannerMapping = initializeMap(initialTags, _delimeter.length);
    }

    private boolean relToFound = false;
    private boolean displayOnlyFound = false;
    private Map<String, String> _scratchMap = null;
    private List<String> _relTo = null;

    public static void updateMapping(List<CapcoTag> updatesIn) {

        portionToBannerMapping = updateMap(initialTags, _delimeter.length, updatesIn);
    }

    public CapcoDismTags() {

        this(false);
    }

    public CapcoDismTags(boolean useAbreviationsIn) {

        super(_delimeter, initialTags, portionToBannerMapping.getValue1(), portionToBannerMapping.getValue2());
    }

    public CapcoSection getEnum() {

        return CapcoSection.DISM;
    }

    public int getOrdinal() {

        return CapcoSection.DISM.ordinal();
    }

   @Override
   public Collection<String> getPortionList() {
      return (_relTo == null) ? new ArrayList<String>() : _relTo;
   }

    @Override
    protected String[] genLabelText() {

        if ((null != portionMap) && !portionMap.isEmpty()) {

            StringBuilder[] myBuffer = new StringBuilder[] {new StringBuilder(), new StringBuilder(), new StringBuilder()};
            Map<String, String> myFullMap = fullBannerMapping.get(0);
            Map<String, String> myAbrvMap = abreviationMapping.get(0);
            String myDelimeter = "";

            _scratchMap = new TreeMap<String, String>();

            processReleaseLogic();

            for (CapcoTag myTag : initialTags) {

                if (portionMap.containsKey(myTag.portionText)) {

                    String myAugment = _scratchMap.get(myTag.portionText);

                    portionMap.remove(myTag.portionText);
                    myBuffer[0].append(myDelimeter);
                    myBuffer[0].append(myTag.portionText);
                    myBuffer[1].append(myDelimeter);
                    myBuffer[1].append(myFullMap.get(myTag.portionText));
                    myBuffer[2].append(myDelimeter);
                    myBuffer[2].append(myAbrvMap.get(myTag.portionText));
                    if (null != myAugment) {

                        myBuffer[0].append(myAugment);
                        myBuffer[1].append(myAugment);
                        myBuffer[2].append(myAugment);
                    }
                    myDelimeter = "/";
                }
            }

            return new String[] {myBuffer[0].toString(), myBuffer[1].toString(), myBuffer[2].toString()};

        } else {

            return null;
        }
    }

    public boolean parsePortionTag(String[] topSplitIn, boolean classifiedIn, boolean forceIn) {

        boolean myFoundFlag = false;

        relToFound = false;
        displayOnlyFound = false;

        if ((null != topSplitIn) && (0 < topSplitIn.length)) {

            if (forceIn || isMatch(topSplitIn[0])) {

                for (int i = 0; topSplitIn.length > i; i++) {

                    addPortionTag(topSplitIn[i]);
                }
                myFoundFlag = true;
                if (relToFound) {

                    portionMap.put(relToKey, null);

                } else {

                    portionMap.put(noRelToKey, null);
                }
                if (displayOnlyFound) {

                    portionMap.put(displayOnlyKey, null);

                } else {

                    portionMap.put(noDisplayOnlyKey, null);
                }
            }
        } else if (classifiedIn) {

            portionMap.put(noRelToKey, null);
            portionMap.put(noDisplayOnlyKey, null);
        }

        return myFoundFlag;
    }

    private boolean isMatch(String tokenIn) {

        boolean myFoundFlag = portionToBannerMapping.getValue1().get(0).containsKey(tokenIn);

        if (!myFoundFlag) {

            int mySize = tokenIn.length();

            if (10 <= mySize) {

                if (relToTag.equals(tokenIn.substring(0, relToTag.length()))
                        || displayOnlyTag.equals(tokenIn.substring(0, displayOnlyTag.length()))
                        || eyesOnlyTag.equals(tokenIn.substring(mySize - eyesOnlyTag.length()))) {

                    myFoundFlag = true;
                }
            }
        }
        return myFoundFlag;
    }

    private void processReleaseLogic() {

        _relTo = new ArrayList<String>();

        if (portionMap.containsKey(noFornKey)) {

            if (portionMap.containsKey(relidoKey)) {

                portionMap.remove(relidoKey);
            }

            if (portionMap.containsKey(relToKey)) {

                portionMap.remove(relToKey);
            }

            if (portionMap.containsKey(displayOnlyKey)) {

                portionMap.remove(displayOnlyKey);
            }

        } else if (portionMap.containsKey(relidoKey)) {

            if (portionMap.containsKey(relToKey)) {

                portionMap.remove(relToKey);
                portionMap.remove(relidoKey);
                portionMap.put(noFornKey, new String[] {noFornKey});
            }

            if (portionMap.containsKey(displayOnlyKey)) {

                portionMap.remove(displayOnlyKey);
                portionMap.remove(relidoKey);
                portionMap.put(noFornKey, new String[]{noFornKey});
            }

        } else {

            mergeRestrictions();
        }
        if (_relTo.isEmpty() && portionMap.containsKey(noFornKey)) {

            _relTo.add(noFornKey);
        }
    }

    private void mergeRestrictions() {

        if (portionMap.containsKey(relToKey)) {

            if (portionMap.containsKey(noRelToKey)) {

                portionMap.remove(relToKey);
                portionMap.put(noFornKey, new String[] {noFornKey});

            } else {

                Set<String> myTagSet = mergeValues(relToTag, relToBase);

                if ((null != myTagSet) && !myTagSet.isEmpty()) {

                    for (String myTag : myTagSet) {

                        _relTo.add(myTag);
                    }
                    _scratchMap.put(relToKey, buildOutputList(myTagSet, " TO USA, "));

                } else {

                    portionMap.remove(relToKey);

                    if (_doStrictRelTo) {

                        portionMap.put(noFornKey, new String[]{noFornKey});
                        _relTo.add(noFornKey);
                    }
                }
            }
        }

        if (portionMap.containsKey(displayOnlyKey)) {

            if (portionMap.containsKey(noDisplayOnlyKey)) {

                portionMap.remove(displayOnlyKey);
                portionMap.put(noFornKey, new String[]{noFornKey});

            } else {

                Set<String> myTagSet = mergeValues(displayOnlyTag, displayOnlyBase);

                if ((null != myTagSet) && !myTagSet.isEmpty()) {

                    _scratchMap.put(displayOnlyKey, buildOutputList(myTagSet, " "));

                } else {

                    portionMap.remove(displayOnlyKey);

                    if (_doStrictDisplay) {

                        portionMap.put(noFornKey, new String[] {noFornKey});
                    }
                }
            }
        }
    }

    private String buildOutputList(Set<String> tagSetIn, String delimeterIn) {

        StringBuilder myBuffer = new StringBuilder();
        String myDelimeter = delimeterIn;

        for (String myTag : tagSetIn) {

            if (3 == myTag.length()) {

                myBuffer.append(myDelimeter);
                myBuffer.append(myTag);
            }
            myDelimeter = ", ";
        }

        for (String myTag : tagSetIn) {

            if (4 == myTag.length()) {

                myBuffer.append(myDelimeter);
                myBuffer.append(myTag);
            }
            myDelimeter = ", ";
        }
        return myBuffer.toString();
    }

    private Set<String> mergeValues(String tagIn, int baseIn) {

        List<String> myGroupList = new ArrayList<String>();
        Set<String> myTagSet = null;

        for (String myKey : portionMap.keySet()) {

            if (myKey.startsWith(tagIn)) {

                myGroupList.add(myKey);
            }
        }
        if (!myGroupList.isEmpty()) {
            for (String myGroup : myGroupList) {
                myTagSet = adjustTags(myTagSet, CONJUNCTION_PATTERN.split(myGroup.substring(baseIn)));
                portionMap.remove(myGroup);

                if (myTagSet.isEmpty()) {
                    break;
                }
            }
        }
        return myTagSet;
    }

    private Set<String> adjustTags(Set<String> tagSetIn, String[] tagListIn) {

        Set<String> myTagSet = new TreeSet<String>();

        for (int i = 0; tagListIn.length > i; i++) {

            String myTag = tagListIn[i];
            String[] myMembers = null;

            // Build initial set (if first entry) or set to search
            // -- unfound items will be removed from the initial set
            if (4 == myTag.length()) {

                // Break-out members from tetragraph when authorized to do so.
                myMembers = tetraGraphMap.get(myTag);
            }
            if (null != myMembers) {

                for (int j = 0; myMembers.length > j; j++) {

                    myTagSet.add(myMembers[j]);
                }

            } else {

                myTagSet.add(myTag);
            }
            if (null != tagSetIn) {

                // Remove any tag from the original set which is not in the current set
                List<String> myRemovalList = new ArrayList<String>();

                for (String myOriginalTag : tagSetIn) {

                    if (!myTagSet.contains(myTag)) {

                        myRemovalList.add(myTag);
                    }
                }

                for (String myOriginalTag : myRemovalList) {

                    tagSetIn.remove(myOriginalTag);
                }

            } else {

                // Use this set to define the original set
                tagSetIn = myTagSet;
            }
        }

        return myTagSet;
    }

    private void addPortionTag(String tagIn) {

        if (null != tagIn) {

            if (tagIn.startsWith(relToKey)) {

                relToFound = true;
            }

            if (tagIn.startsWith(displayOnlyKey)) {

                displayOnlyFound = true;
            }
            portionMap.put(tagIn, new String[] {tagIn});
        }
    }
}
