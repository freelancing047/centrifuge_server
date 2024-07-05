package csi.server.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import csi.server.common.enumerations.CapcoSection;
import csi.server.common.util.ValuePair;

/**
 * Created by centrifuge on 4/20/2015.
 */
public abstract class AbstractCapcoTagProcessing {
    public abstract CapcoSection getEnum();
    public abstract int getOrdinal();
    protected abstract String[] genLabelText();

    protected List<Map<String, String>>[] bannerMapping;
    protected List<Map<String, String>> fullBannerMapping;
    protected List<Map<String, String>> abreviationMapping;
    protected Map<String, String[]> portionMap;
    protected List<CapcoTag> basicList;
    protected String[] prefix = null;
    protected String[] delimeter = new String[0];
    protected int maxLevel = 1;

    protected AbstractCapcoTagProcessing() {
    }

    protected AbstractCapcoTagProcessing(String[] delimeterIn, List<CapcoTag> basicListIn,
                                 List<Map<String, String>> fullBannerMappingIn,
                                 List<Map<String, String>> abreviationMappingIn) {

        portionMap = new TreeMap<String, String[]>();

        delimeter = delimeterIn;
        maxLevel = delimeter.length;
        basicList = basicListIn;
        fullBannerMapping = fullBannerMappingIn;
        abreviationMapping = abreviationMappingIn;
    }

    public boolean parsePortionTag(String[] topSplitIn, boolean classifiedIn, boolean forceIn) {

        boolean myFoundFlag = false;

        if ((null != topSplitIn) && (0 < topSplitIn.length)) {

            if (1 < maxLevel) {

                String myText = topSplitIn[0];
                String[] myList = myText.split(delimeter[1]);

                if (null != prefix) {

                    if (prefix[0].equals(myList[0])) {

                        parseControl(myList, 1);

                        for (int i = 1; topSplitIn.length > i; i++) {

                            myText = topSplitIn[i];
                            myList = myText.split(delimeter[1]);
                            parseControl(myList, 0);
                        }
                        myFoundFlag = true;
                    }

                } else {

                    if (forceIn || fullBannerMapping.get(0).containsKey(myList[0])) {

                        for (int i = 0; topSplitIn.length > i; i++) {

                            myText = topSplitIn[i];
                            myList = myText.split(delimeter[1]);

                            parseControl(myList, 0);
                        }
                        myFoundFlag = true;
                    }
                }

            } else {

                if (forceIn || fullBannerMapping.get(0).containsKey(topSplitIn[0])) {

                    for (int i = 0; topSplitIn.length > i; i++) {

                        portionMap.put(topSplitIn[i], new String[] {topSplitIn[i]});
                    }
                    myFoundFlag = true;
                }
            }
        }

        return myFoundFlag;
    }

    public Collection<String> getPortionList() {

        Collection<String> myPortionList = null;

        if (null != prefix) {

            myPortionList = new ArrayList<String>();

            for (String myString : portionMap.keySet()) {

                myPortionList.add(prefix[0] + delimeter[1] + myString);
            }

        } else {

            myPortionList = portionMap.keySet();
        }
        return myPortionList;
    }

    protected void parseControl(String[] listIn, int indexIn) {

        if (listIn.length > indexIn) {

            String myControl = listIn[indexIn++];

            if (listIn.length > indexIn) {

                for (int i = indexIn; listIn.length > i; i++) {

                    parseCompartment(myControl, (2 < maxLevel) ? listIn[i].split(delimeter[2]) : new String[]{listIn[i]});
                }

            } else {

                portionMap.put(myControl, new String[]{myControl});
            }
        }
    }

    protected void parseCompartment(String controlIn, String[] listIn) {

        if (listIn.length > 0) {

            String myCompartment = listIn[0];

            if (listIn.length > 1) {

                for (int i = 1; listIn.length > i; i++) {

                    portionMap.put(controlIn + delimeter[1] + myCompartment + delimeter[2] + listIn[i], new String[]{controlIn, myCompartment, listIn[i]});
                 }

            } else {

                portionMap.put(controlIn + delimeter[1] + myCompartment, new String[] {controlIn, myCompartment});
            }
        }
    }

    public static ValuePair<List<Map<String, String>>, List<Map<String, String>>> updateMap(List<CapcoTag> tagInfoIn, int maxLevelIn, List<CapcoTag> updatesIn) {

        ValuePair<List<Map<String, String>>, List<Map<String, String>>> myMapping = initializeMap(tagInfoIn, maxLevelIn);





        return myMapping;
    }

    protected static ValuePair<List<Map<String, String>>, List<Map<String, String>>> initializeMap(List<CapcoTag> tagInfoIn, int maxLevelIn) {

        List<Map<String, String>> myNewBannerMap = new ArrayList<Map<String, String>>();
        List<Map<String, String>> myNewAbreviationMap = new ArrayList<Map<String, String>>();

        myNewBannerMap.clear();
        myNewAbreviationMap.clear();

        for (int i = 0; maxLevelIn >= i; i++) {

            myNewBannerMap.add(new TreeMap<String, String>());
            myNewAbreviationMap.add(new TreeMap<String, String>());
        }

        for (CapcoTag myTag : tagInfoIn) {

            int myLevel = myTag.level;

            if ((0 <= myLevel) && (maxLevelIn >= myLevel)) {

                myNewBannerMap.get(myLevel).put(myTag.portionText, myTag.bannerText);
                myNewAbreviationMap.get(myLevel).put(myTag.portionText, myTag.abreviation);
            }
        }
        return new ValuePair<List<Map<String, String>>, List<Map<String, String>>>(myNewBannerMap, myNewAbreviationMap);
    }

    public static void finalizeParsing(List<AbstractCapcoTagProcessing> listIn, int lastIndexIn, boolean isClassified) {

        if (isClassified && (CapcoSection.DISM.ordinal() > lastIndexIn)) {

            listIn.get(CapcoSection.DISM.ordinal()).parsePortionTag(null, isClassified, false);
        }
    }

    public static List<AbstractCapcoTagProcessing> getProcessingList() {

        List<AbstractCapcoTagProcessing> myList = new ArrayList<AbstractCapcoTagProcessing>();

        myList.add(new CapcoClassTags());
        myList.add(new CapcoSciTags());
        myList.add(new CapcoSapTags());
        myList.add(new CapcoAeaTags());
        myList.add(new CapcoFgiTags());
        myList.add(new CapcoDismTags());
        myList.add(new CapcoNonicTags());

        return myList;
    }

    public static String[] genFullLabelSet(List<AbstractCapcoTagProcessing> listIn, Set<String> unrecognizedTagsIn) {

        StringBuilder[] myBuffer = new StringBuilder[] {new StringBuilder(), new StringBuilder(), new StringBuilder()};
        String myDelimeter = "";
        int howMany = listIn.size();

        for (int i = 0; i < howMany; i++) {

            AbstractCapcoTagProcessing myTag = listIn.get(i);
            String[] mySectionSet = myTag.genLabelText();

            if ((null != mySectionSet) && (3 == mySectionSet.length)) {

                for (int j = 0; 3 > j; j++) {

                    String myString = (null != mySectionSet[j]) ? mySectionSet[j].trim() : null;

                    if ((null != myString) && (0 < myString.length())) {

                        myBuffer[j].append(myDelimeter);
                        myBuffer[j].append(mySectionSet[j]);
                    }
                }
                myDelimeter = "//";
            }
        }

        if ((null != unrecognizedTagsIn) && !unrecognizedTagsIn.isEmpty()) {

            for (int j = 0; 3 > j; j++) {

                myBuffer[j].append(processUnrecognizedTags(unrecognizedTagsIn));
            }
        }

        return new String[] {myBuffer[0].toString(), myBuffer[1].toString(), myBuffer[2].toString()};
    }

    protected static String processUnrecognizedTags(Set<String> unrecognizedTagsIn) {

        StringBuilder myBuffer = new StringBuilder();
        String myDelimeter = "??";

        if ((null != unrecognizedTagsIn) && !unrecognizedTagsIn.isEmpty()) {

            for (String myTag : unrecognizedTagsIn) {

                myBuffer.append(myDelimeter);
                myBuffer.append(myTag);
                myDelimeter = "/";
            }
        }
        return myBuffer.toString();
    }

    protected String[] processDescendingAlphaTags() {

        Map<String, Object> myBaseTree = new TreeMap<String, Object>();

        if ((null != portionMap) && !portionMap.isEmpty()) {

            StringBuilder[] myBuffer = new StringBuilder[] {new StringBuilder(), new StringBuilder(), new StringBuilder()};

            if (null != prefix) {

                myBuffer[0].append(prefix[0]);
                myBuffer[0].append(delimeter[1]);
                myBuffer[1].append(prefix[1]);
                myBuffer[1].append(delimeter[1]);
                myBuffer[2].append(prefix[2]);
                myBuffer[2].append(delimeter[1]);
            }

            // Build the tree from a mixture of 3 possible formats
            // <program>
            // <program><delimeter-1><compartment>
            // <program><delimeter-1><compartment><delimeter-2><sub-compartment>
            for (String myBase : portionMap.keySet()) {

                Map<String, Object> myTree = myBaseTree;

                for (int i = 1; (null != myTree) && (maxLevel >= i); i++) {

                    String[] myTest = (maxLevel > i) ? myBase.split(delimeter[i]) : new String[] {myBase};
                    Map<String, Object> myNewTree = (Map<String, Object>)myTree.get(myTest[0]);

                    if (null == myNewTree) {

                        myNewTree = new TreeMap<String, Object>();
                        myTree.put(myTest[0], null);
                    }
                    if (1 < myTest.length) {

                        myTree.put(myTest[0], myNewTree);
                        myTree = myNewTree;
                        myBase = myTest[1];
                    }
                }
            }

            // Process the tree
            String myDelimeter = "";
            for (Map.Entry<String,Object> entry : myBaseTree.entrySet()) {
               String myString = entry.getKey();

                for (int i = 0; 3 > i; i++) {

                    myBuffer[i].append(myDelimeter);
                    myBuffer[i].append(getTag(myString, i));
                }
                myDelimeter = delimeter[0];

                processTree(myBuffer, (Map<String,Object>) entry.getValue(), 0);
            }

            return new String[] {myBuffer[0].toString(), myBuffer[1].toString(), myBuffer[2].toString()};

        } else {

            return null;
        }
    }

   private void processTree(StringBuilder[] bufferIn, Map<String, Object> treeIn, int levelIn) {
      if (treeIn != null) {
         int myLevel = levelIn + 1;
         String myDelimeter = (maxLevel > myLevel) ? delimeter[myLevel] : " ";

         for (Map.Entry<String, Object> entry : treeIn.entrySet()) {
            Map<String, Object> myTree = (Map<String, Object>) entry.getValue();

            for (int i = 0; i < 3; i++) {
               bufferIn[i].append(myDelimeter);
               bufferIn[i].append(getTag(entry.getKey(), i, myLevel));
            }
            processTree(bufferIn, myTree, myLevel);
         }
      }
   }
/*
    protected String getFullBanner(String portionIn, int levelIn) {

        String myBanner = null;

        if ((null != fullBannerMapping) && (fullBannerMapping.size() > levelIn)) {

            Map<String, String> myMap = fullBannerMapping.get(levelIn);

            if (null != myMap) {

                myBanner = myMap.get(portionIn);
            }
        }
        return (null != myBanner) ? myBanner : portionIn;
    }

    protected String getAbreviation(String portionIn, int levelIn) {

        String myBanner = null;

        if ((null != abreviationMapping) && (abreviationMapping.size() > levelIn)) {

            Map<String, String> myMap = abreviationMapping.get(levelIn);

            if (null != myMap) {

                myBanner = myMap.get(portionIn);
            }
        }
        return (null != myBanner) ? myBanner : portionIn;
    }
*/
    protected String getPrefix(int modeIn) {

        if ((null != prefix) && (0 <= modeIn) && (prefix.length > modeIn)) {

            return prefix[modeIn];
        }
        return "";
    }

    protected String getTag(String keyIn, int modeIn) {

        String myResult = null;

        if (1 == modeIn) {

            myResult = fullBannerMapping.get(0).get(keyIn);

        } else if (2 == modeIn) {

            myResult = abreviationMapping.get(0).get(keyIn);
        }

        return (null != myResult) ? myResult : keyIn;
    }

    protected String getTag(String keyIn, int modeIn, int levelIn) {

        String myResult = null;

        if (1 == modeIn) {

            if (fullBannerMapping.size() > levelIn) {

                myResult = fullBannerMapping.get(0).get(keyIn);
            }

        } else if (2 == modeIn) {

            if (abreviationMapping.size() > levelIn) {

                myResult = abreviationMapping.get(0).get(keyIn);
            }
        }

        return (null != myResult) ? myResult : keyIn;
    }
}
