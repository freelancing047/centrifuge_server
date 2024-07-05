package csi.server.common.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import csi.server.common.dto.SelectionListData.StringEntry;

/**
 * Created by centrifuge on 9/21/2016.
 */
public class StringUtil {

    private static final char _zero = '0';
    private static final char _nine = '9';
    private static final char _leftBrace = '{';
    private static final char _rightBrace = '}';
    private static final char _dataQuote = '\'';
    private static final char[] _javaWild = new char[] {'*', '?'};
    private static final char[] _sqlWild = new char[] {'%', '_'};

    public static Comparator<StringEntry> getCaselessStringEntryComparator() {

        return new Comparator<StringEntry>() {

            @Override
            public int compare(StringEntry o1, StringEntry o2) {

                return compareCaselessCharacterString(o1.getKey(), o2.getKey(), true);
            }
        };
    }

    public static Comparator<StringEntry> getStringEntryComparator() {

        return new Comparator<StringEntry>() {

            @Override
            public int compare(StringEntry o1, StringEntry o2) {

                return compareCharacterString(o1.getKey(), o2.getKey(), true);
            }
        };
    }

    public static Comparator<String> getCaselessStringComparator() {

        return new Comparator<String>() {

            @Override
            public int compare(String o1, String o2) {

                return compareCaselessCharacterString(o1, o2, true);
            }
        };
    }

    public static Comparator<String> getFuzzyStringComparator() {

        return new Comparator<String>() {

            @Override
            public int compare(String o1, String o2) {

                return compareFuzzyCharacterString(o1, o2, true);
            }
        };
    }

    public static Comparator<String> getStringComparator() {

        return new Comparator<String>() {

            @Override
            public int compare(String o1, String o2) {

                return compareCharacterString(o1, o2, true);
            }
        };
    }

    public static int compareFuzzyCharacterString(String itemOneIn, String itemTwoIn, boolean checkRemainderIn) {

        int myResult = compareCaselessCharacterString(itemOneIn, itemTwoIn, checkRemainderIn);

        return (0 == myResult) ? compareCharacterString(itemOneIn, itemTwoIn, checkRemainderIn) : myResult;
    }

    public static int compareCaselessCharacterString(String itemOneIn, String itemTwoIn, boolean checkRemainderIn) {

        String myItemOne = (itemOneIn == null) ? "" : itemOneIn.toLowerCase();
        String myItemTwo = (itemTwoIn == null) ? "" : itemTwoIn.toLowerCase();

        return compareCharacterString(myItemOne, myItemTwo, checkRemainderIn);
    }

    public static int compareCharacterString(String itemOneIn, String itemTwoIn, boolean checkRemainderIn) {

        int myResult = 0;
        int myLimit = Math.min(itemOneIn.length(), itemTwoIn.length());

        for (int i = 0; myLimit > i; i++) {

            if (itemOneIn.charAt(i) < itemTwoIn.charAt(i)) {

                myResult = -1;
                break;

            } else  if (itemOneIn.charAt(i) > itemTwoIn.charAt(i)) {

                myResult = 1;
                break;
            }
        }
        if ((0 == myResult) && checkRemainderIn) {

            if (myLimit < itemOneIn.length()) {

                myResult = 1;

            } else if (myLimit < itemTwoIn.length()) {

                myResult = -1;
            }
        }
        return myResult;
    }

    public static Collection<Long> extractValuesInto(Collection<Long> collectionIn, String stringIn) {

        return extractValuesInto(collectionIn, stringIn,'|');
    }

    public static Collection<Long> extractValuesInto(Collection<Long> collectionIn, String stringIn, final char delimiterIn) {

        if ((null != collectionIn) && (null != stringIn) && (0 < stringIn.length())) {

            char[] myBuffer = stringIn.toCharArray();
            int myBase = 0;

            for (int i = myBase; myBuffer.length > i; i++) {

                if (delimiterIn == myBuffer[i]) {

                    String myString = (new String(myBuffer, myBase, i - myBase)).trim();

                    if (0 < myString.length()) {

                        collectionIn.add(Long.decode(myString));
                    }
                    myBase = i + 1;
                }
            }
            if (myBuffer.length > myBase) {

                String myString = (new String(myBuffer, myBase, myBuffer.length - myBase)).trim();

                if (0 < myString.length()) {

                    collectionIn.add(Long.decode(myString));
                }
            }
        }
        return ((null != collectionIn) && !collectionIn.isEmpty()) ? collectionIn : null;
    }

    public static Collection<String> extractInto(Collection<String> collectionIn, String stringIn) {

        return extractInto(collectionIn, stringIn,'|');
    }

    public static Collection<String> extractInto(Collection<String> collectionIn, String stringIn, final char delimiterIn) {

        if ((null != collectionIn) && (null != stringIn) && (0 < stringIn.length())) {

            char[] myBuffer = stringIn.toCharArray();
            int myBase = 0;

            for (int i = myBase; myBuffer.length > i; i++) {

                if (delimiterIn == myBuffer[i]) {

                    String myString = (new String(myBuffer, myBase, i - myBase)).trim();

                    if (0 < myString.length()) {

                        collectionIn.add(myString);
                    }
                    myBase = i + 1;
                }
            }
            if (myBuffer.length > myBase) {

                String myString = (new String(myBuffer, myBase, myBuffer.length - myBase)).trim();

                if (0 < myString.length()) {

                    collectionIn.add(myString);
                }
            }
        }
        return ((null != collectionIn) && !collectionIn.isEmpty()) ? collectionIn : null;
    }

    public static Map<String, Integer> genImplicitMap(String stringIn) {

        return genImplicitMap(stringIn, '|');
    }

    public static Map<String, Integer> genImplicitMap(String stringIn, final char delimiterIn) {

        Map<String, Integer> myMap = new TreeMap<String, Integer>();

        if ((null != stringIn) && (0 < stringIn.length())) {

            char[] myBuffer = stringIn.toCharArray();
            int myBase = 0;
            int myIndex = 0;

            for (int i = myBase; myBuffer.length > i; i++) {

                if (delimiterIn == myBuffer[i]) {

                    String myString = (new String(myBuffer, myBase, i - myBase)).trim();

                    if (0 < myString.length()) {

                        myMap.put(myString, Integer.valueOf(myIndex++));
                    }
                    myBase = i + 1;
                }
            }
            if (myBuffer.length > myBase) {

                String myString = (new String(myBuffer, myBase, myBuffer.length - myBase)).trim();

                if (0 < myString.length()) {

                    myMap.put(myString, Integer.valueOf(myIndex));
                }
            }
        }
        return myMap.isEmpty() ? null : myMap;
    }

    public static Map<String, Long> genValueMap(String stringIn) {

        return genValueMap(stringIn, '|');
    }

    public static Map<String, Long> genValueMap(String stringIn, final char delimiterIn) {

        Map<String, Long> myMap = new TreeMap<String, Long>();

        if ((null != stringIn) && (0 < stringIn.length())) {

            char[] myBuffer = stringIn.toCharArray();
            String myKey = null;
            int myBase = 0;

            for (int i = 0; myBuffer.length > i; i++) {

                if (delimiterIn == myBuffer[i]) {

                    if (null == myKey) {

                        myKey = (new String(myBuffer, myBase, i - myBase)).trim();
                        myBase = i + 1;

                    } else {

                        String myString = (new String(myBuffer, myBase, i - myBase)).trim();
                        Long myValue = Long.decode(myString);
                        myMap.put(myKey, myValue);
                        myKey = null;
                        myBase = i + 1;
                    }
                }
            }
            if (null != myKey) {

                String myString = (new String(myBuffer, myBase, myBuffer.length - myBase)).trim();
                Long myValue = Long.decode(myString);
                myMap.put(myKey, myValue);
            }
        }
        return myMap.isEmpty() ? null : myMap;
    }

    public static List<StringEntry> buildDisplayList(Collection<String> inputListIn) {

        return (List<StringEntry>)buildDisplayList(new ArrayList<StringEntry>(), inputListIn);
    }

    public static Collection<StringEntry> buildDisplayList(Collection<StringEntry> outputListIn,
                                                           Collection<String> inputListIn) {

        Collection<StringEntry> myList = (null != outputListIn) ? outputListIn : new ArrayList<StringEntry>();

        if (null != inputListIn) {

            for (String myItem : inputListIn) {

                myList.add(new StringEntry(myItem));
            }
        }
        return myList;
    }

    public static List<String> buildOrderedList(String stringIn) {

        return buildOrderedList(stringIn,'|');
    }

    public static List<String> buildOrderedList(String stringIn, final char delimiterIn) {

        List<String> myList = new ArrayList<String>();

        if ((null != stringIn) && (0 < stringIn.length())) {

            char[] myBuffer = stringIn.toCharArray();
            int myBase = 0;

            for (int i = myBase; myBuffer.length > i; i++) {

                if (delimiterIn == myBuffer[i]) {

                    String myString = (new String(myBuffer, myBase, i - myBase)).trim();

                    if (0 < myString.length()) {

                        myList.add(myString);
                    }
                    myBase = i + 1;
                }
            }
            if (myBuffer.length > myBase) {

                String myString = (new String(myBuffer, myBase, myBuffer.length - myBase)).trim();

                if (0 < myString.length()) {

                    myList.add(myString);
                }
            }
        }
        return myList.isEmpty() ? null : myList;
    }

    public static Collection<StringEntry> extractForDisplay(Collection<StringEntry> collectionIn, String stringIn) {

        return extractForDisplay(collectionIn, stringIn,'|');
    }

    public static Collection<StringEntry> extractForDisplay(Collection<StringEntry> collectionIn, String stringIn, final char delimiterIn) {

        if ((null != collectionIn) && (null != stringIn) && (0 < stringIn.length())) {

            char[] myBuffer = stringIn.toCharArray();
            int myBase = 0;

            for (int i = myBase; myBuffer.length > i; i++) {

                if (delimiterIn == myBuffer[i]) {

                    String myString = (new String(myBuffer, myBase, i - myBase)).trim();

                    if (0 < myString.length()) {

                        collectionIn.add(new StringEntry(myString));
                    }
                    myBase = i + 1;
                }
            }
            if (myBuffer.length > myBase) {

                String myString = (new String(myBuffer, myBase, myBuffer.length - myBase)).trim();

                if (0 < myString.length()) {

                    collectionIn.add(new StringEntry(myString));
                }
            }
        }
        return ((null != collectionIn) && !collectionIn.isEmpty()) ? collectionIn : null;
    }

    public static Collection<String> extractForAcl(Collection<String> collectionIn, String stringIn) {

        return extractForAcl(collectionIn, stringIn,'|');
    }

    public static Collection<String> extractForAcl(Collection<String> collectionIn, String stringIn, final char delimiterIn) {

        if ((null != collectionIn) && (null != stringIn) && (0 < stringIn.length())) {

            char[] myBuffer = stringIn.toLowerCase().toCharArray();
            int myBase = 0;

            for (int i = myBase; myBuffer.length > i; i++) {

                if (delimiterIn == myBuffer[i]) {

                    String myString = (new String(myBuffer, myBase, i - myBase)).trim();

                    if (0 < myString.length()) {

                        collectionIn.add(myString);
                    }
                    myBase = i + 1;
                }
            }
            if (myBuffer.length > myBase) {

                String myString = (new String(myBuffer, myBase, myBuffer.length - myBase)).trim();

                if (0 < myString.length()) {

                    collectionIn.add(myString);
                }
            }
        }
        return ((null != collectionIn) && !collectionIn.isEmpty()) ? collectionIn : null;
    }

    public static String concatUniqueIntegers(Collection<Integer> collectionIn) {

        Set<Integer> mySet = new TreeSet<Integer>();

        if ((null != collectionIn) && !collectionIn.isEmpty()) {

            mySet.addAll(collectionIn);
        }

        return concatIntegers(mySet);
    }

    public static String concatIntegers(Collection<Integer> collectionIn) {

        String myResult = null;

        if ((null != collectionIn) && !collectionIn.isEmpty()) {

            StringBuilder myBuffer = new StringBuilder();

            for (Integer myValue : collectionIn) {

                try {

                    if (null != myValue) {

                        myBuffer.append(myValue.toString());
                    }
                    myBuffer.append('|');

                } catch (Exception myException) {

                }
            }
            myResult = myBuffer.substring(0, myBuffer.length() - 1);
        }
        return myResult;
    }

    public static String concatUniqueValues(Collection<Long> collectionIn) {

        Set<Long> mySet = new TreeSet<Long>();

        if ((null != collectionIn) && !collectionIn.isEmpty()) {

            mySet.addAll(collectionIn);
        }

        return concatValues(mySet);
    }

   public static String concatValues(Collection<Long> collectionIn) {
      return (collectionIn == null) ? null : collectionIn.stream().map(i -> i.toString()).collect(Collectors.joining("|"));
   }

   public static String concatValueMap(Map<String, Long> mapIn) {
      return (mapIn == null) ? null : mapIn.values().stream().map(i -> i.toString()).collect(Collectors.joining("|"));
   }

    public static String concatUniqueInput(String[] arrayIn) {

        return concatUniqueInput(((null != arrayIn) ? Arrays.asList(arrayIn) : null), '|');
    }

    public static String concatUniqueInput(String[] arrayIn, char delimiterIn) {

        return concatUniqueInput(((null != arrayIn) ? Arrays.asList(arrayIn) : null), delimiterIn);
    }

    public static String concatUniqueInput(Collection<String> collectionIn) {

        return concatUniqueInput(collectionIn, '|');
    }

    public static String concatUniqueInput(Collection<String> collectionIn, char delimiterIn) {

        Set<String> mySet = new TreeSet<String>();

        if ((null != collectionIn) && !collectionIn.isEmpty()) {

            mySet.addAll(collectionIn);
        }

        return concatInput(mySet, delimiterIn);
    }

    public static String concatInput(String[] arrayIn) {

        return concatInput((null != arrayIn) ? Arrays.asList(arrayIn) : null);
    }

    public static String concatInput(String[] arrayIn, char delimiterIn) {

        return concatInput(((null != arrayIn) ? Arrays.asList(arrayIn) : null), delimiterIn);
    }

    public static String concatInput(Collection<String> collectionIn) {

        return concatInput(collectionIn, '|');
    }

    public static String concatInput(Collection<String> collectionIn, char delimiterIn) {

        String myResult = null;

        if ((null != collectionIn) && !collectionIn.isEmpty()) {

            StringBuilder myBuffer = new StringBuilder();

            for (String myValue : collectionIn) {

                if (null != myValue) {

                    myBuffer.append(myValue.trim());
                }
                myBuffer.append(delimiterIn);
            }
            myResult = myBuffer.substring(0, myBuffer.length() - 1);
        }
        return myResult;
    }

    public static String[] split(String stringIn, char delimiterIn) {

        Collection<String> myResults = extractInto(new ArrayList<String>(), stringIn, delimiterIn);

        return ((null != myResults) && !myResults.isEmpty()) ? myResults.toArray(new String[0]) : null;
    }

    public static String[] split(String stringIn) {

        Collection<String> myResults = extractInto(new ArrayList<String>(), stringIn);

        return ((null != myResults) && !myResults.isEmpty()) ? myResults.toArray(new String[0]) : null;
    }

    public static String concatDisplay(StringEntry[] arrayIn) {

        return concatDisplay((null != arrayIn) ? Arrays.asList(arrayIn) : null);
    }

    public static String concatDisplay(StringEntry[] arrayIn, boolean caselessIn) {

        return concatDisplay((null != arrayIn) ? Arrays.asList(arrayIn) : null, caselessIn);
    }

    public static String concatDisplay(Collection<StringEntry> collectionIn) {

        return concatDisplay(collectionIn, true);
    }

    public static String concatDisplay(Collection<StringEntry> collectionIn, boolean caselessIn) {

        Comparator<StringEntry> myComparator = caselessIn ? getCaselessStringEntryComparator() : getStringEntryComparator();
        Set<StringEntry> mySet = new TreeSet<StringEntry>(myComparator);

        mySet.addAll(collectionIn);

        return concatDisplay(mySet);
    }

    public static String concatDisplay(Set<StringEntry> setIn) {

        String myResult = null;

        if ((null != setIn) && !setIn.isEmpty()) {

            StringBuilder myBuffer = new StringBuilder();

            for (StringEntry myValue : setIn) {

                if ((null != myValue) && (null != myValue.getValue())) {

                    myBuffer.append(myValue.getValue().trim());
                }
                myBuffer.append('|');
            }
            myResult = myBuffer.substring(0, myBuffer.length() - 1);
        }
        return myResult;
    }

    public static String patternToSql(String stringIn) {

        return ((null != stringIn) && (0 < stringIn.length())) ? translate(stringIn, _javaWild, _sqlWild) : stringIn;
    }

    public static String patternFromSql(String stringIn) {

        return ((null != stringIn) && (0 < stringIn.length())) ? translate(stringIn, _sqlWild, _javaWild) : stringIn;
    }

    private static String translate(String stringIn, char[] inIn, char[] outIn) {

        StringBuilder myBuffer = new StringBuilder(stringIn.length());

        for (int i = 0; stringIn.length() > i; i++) {

            char myCharacter = stringIn.charAt(i);

            if ((outIn[0] == myCharacter) || (outIn[1] == myCharacter)) {

                myBuffer.append('\\');
                myBuffer.append(myCharacter);

            } else if (inIn[0] == myCharacter) {

                myBuffer.append(outIn[0]);

            } else if (inIn[1] == myCharacter) {

                myBuffer.append(outIn[1]);

            } else if ('\\' == myCharacter) {

                i++;
                if (stringIn.length() > i) {

                    myCharacter = stringIn.charAt(i);

                    if ((inIn[0] != myCharacter) && (inIn[1] != myCharacter)) {

                        myBuffer.append('\\');
                    }
                    myBuffer.append(myCharacter);
                }

            } else {

                myBuffer.append(myCharacter);
            }
        }
        return myBuffer.toString();
    }

    public static String escapeStaticSqlText(String valueIn) {

        String myValue = valueIn;

        if ((null != valueIn) && (0 < valueIn.length())) {

            int myFirstQuote = valueIn.indexOf(_dataQuote);

            if (0 <= myFirstQuote) {

                StringBuilder myBuffer = new StringBuilder(valueIn.substring(0, myFirstQuote));

                for (int i = myFirstQuote; valueIn.length() > i; i++) {

                    char myCharacter = valueIn.charAt(i);

                    if (_dataQuote == myCharacter) {

                        myBuffer.append(_dataQuote);
                    }
                    myBuffer.append(myCharacter);
                }
                myValue = myBuffer.toString();
            }
        }
        return myValue;
    }

    public static String replaceArguments(String formatIn, String[] argumentsIn) {

        StringBuilder myBuffer = new StringBuilder(formatIn.length());
        int myActiveFlag = -1;
        int myValue = -1;

        for (int i = 0; formatIn.length() > i; i++) {

            char myCharacter = formatIn.charAt(i);

            if (0 <= myActiveFlag) {

                if (_rightBrace == myCharacter) {

                    if ((myActiveFlag + 1) < i) {

                        if ((0 <= myValue) && (argumentsIn.length > myValue)) {

                            String myArgument = argumentsIn[myValue];

                            if (null != myArgument) {

                                myBuffer.append(myArgument);
                            }
                        }
                    }
                    myActiveFlag = -1;

                } else if ((_zero <= myCharacter) && (_nine >= myCharacter)) {

                    int myDelta = myCharacter - _zero;

                    if (0 < myValue) {

                        myValue = (myValue * 10) + myDelta;

                    } else {

                        myValue = myDelta;
                    }

                } else {

                    for (int j = myActiveFlag; i >= j; j++) {

                        myBuffer.append(formatIn.charAt(j));
                    }
                    myActiveFlag = -1;
                }

            } else if (_leftBrace == myCharacter) {

                myActiveFlag = i;
                myValue = -1;

            } else {

                myBuffer.append(myCharacter);
            }
        }
        return myBuffer.toString();
    }

    public static String htmlEncode(String stringIn) {

        StringBuilder myBuffer = new StringBuilder();
        char[] myData = stringIn.toCharArray();

        for (int i = 0; myData.length > i; i++) {

            char myChar = myData[i];

            switch (myChar) {

                case '<':

                    myBuffer.append("&lt;");
                    break;

                case '>':

                    myBuffer.append("&gt;");
                    break;

                case '"':

                    myBuffer.append("&quot;");
                    break;

                case '&':

                    myBuffer.append("&amp;");
                    break;

                default:

                    myBuffer.append(myChar);
                    break;
            }
        }
        return myBuffer.toString();
    }

    private static String htmlDecode(String stringIn) {

        StringBuilder myBuffer = new StringBuilder();
        char[] myData = stringIn.toCharArray();

        for (int i = 0; myData.length > i; i++) {

            char myChar = myData[i];

            if ('&' == myChar) {

                if ((3 < (myData.length - i)) && ('t' == myData[i + 2]) && (';' == myData[i + 3])) {

                    if ('l' == myData[i + 1]) {

                        i += 3;
                        myBuffer.append('<');

                    } else if ('g' == myData[i + 1]) {

                        i += 3;
                        myBuffer.append('>');

                    } else {

                        myBuffer.append(myChar);
                    }

                } else if ((4 < (myData.length - i)) && ('a' == myData[i + 1]) && ('m' == myData[i + 2])
                            && ('p' == myData[i + 3]) && (';' == myData[i + 4])) {
                    i += 4;
                    myBuffer.append('&');

                } else if ((5 < (myData.length - i)) && ('q' == myData[i + 1]) && ('u' == myData[i + 2])
                        && ('o' == myData[i + 3]) && ('t' == myData[i + 4]) && (';' == myData[i + 5])) {
                    i += 5;
                    myBuffer.append('"');

                } else {

                    myBuffer.append(myChar);
                }

            } else {

                myBuffer.append(myChar);
            }
        }
        return myBuffer.toString();
    }

    public static int countChar(String stringIn, final char testCharIn) {

        int myCount = 0;
        char[] myChars = stringIn.toCharArray();

        for (int i = 0; stringIn.length() > i; i++) {

            if (testCharIn == myChars[i]) {

                myCount++;
            }
        }
        return myCount;
    }

    public static String removePath(String fileNameIn) {

        int myBase1 = fileNameIn.lastIndexOf('\\');
        int myBase2 = fileNameIn.lastIndexOf('/');
        int myBase = Math.max(myBase1, myBase2);

        if (-1 < myBase) {

            return fileNameIn.substring(myBase + 1);
        }
        return fileNameIn;
    }

    public static String quoteAndEscape(String stringIn, char quoteIn) {

        StringBuilder myBuffer = new StringBuilder();
        char[] myChars = stringIn.toCharArray();

        myBuffer.append(quoteIn);
        for (int i = 0; stringIn.length() > i; i++) {

            if (quoteIn == myChars[i]) {

                myBuffer.append(quoteIn);
            }
            myBuffer.append(myChars[i]);
        }
        myBuffer.append(quoteIn);
        return myBuffer.toString();
    }

    public static String trimAnyQuote(String stringIn ) {

        String myResult = stringIn;
        int myLastIndex = stringIn.length() - 1;

        if (0 < myLastIndex) {

            char myFirst = stringIn.charAt(0);
            char myLast = stringIn.charAt(myLastIndex);

            if ((('"' == myFirst) && ('"' == myLast))
                || (('\'' == myFirst) && ('\'' == myLast))) {

                myResult = stringIn.substring(1, myLastIndex);
            }
        }
        return myResult;
    }
}
