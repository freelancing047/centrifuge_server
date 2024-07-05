package csi.server.common.util.uploader;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by centrifuge on 10/5/2015.
 */
public class CsiSimpleXmlParser extends XmlDataAccess {

    private enum AccessMode {

        CLOSED,
        NAME,
        ATTRIBUTES,
        DATA
    }
    private static final int _defaultBufferSize = 1024 * 1024;

    private static final byte _lowerBit = (byte) 0x20;
    private static final byte _upperMask = (byte) 0xDF;

    private int _level = 0;
    private AccessMode _accessMode = AccessMode.CLOSED;
    private List<byte[][]> _parentPath = null;
    private List<byte[]> _nodePath = null;
    private Integer _propertiesRewindHandle = null;
    private String _encoding = null;

    /*
        Server-side constructor -- stream input
     */
    public CsiSimpleXmlParser(CsiSimpleInputStream streamIn) throws IOException {

        super(streamIn);
    }

    /*
        Server-side constructor -- stream input
     */
    public CsiSimpleXmlParser(CsiSimpleInputStream streamIn, int blockSizeIn) throws IOException {

        super(streamIn, blockSizeIn);
    }

    /*
        Client-side constructor -- pre-allocated block
     */
    public CsiSimpleXmlParser(byte[] blockIn) {

        this(blockIn, blockIn.length);
    }

    /*
        Client-side constructor -- pre-allocated block
     */
    public CsiSimpleXmlParser(byte[] blockIn, int countIn) {

        super(blockIn, countIn);

        initValues();
    }

    public Map<String, List<String>> mapAttributes(String xpathIn, String keyAttributeIn, String dataAttributeIn) {

        byte[] myKeyBuffer = keyAttributeIn.getBytes();
        byte[] myDataBuffer = dataAttributeIn.getBytes();
        byte[][] myTagStrings = extractTags(xpathIn);
        boolean[] myMatch = new boolean[myTagStrings.length];
        Map<String, List<String>> myMap = new TreeMap<String, List<String>>();

        initValues();

        for (int i = 0; myMatch.length > i; i++) {

            myMatch[i] = false;
        }

        while (getNextNode(myTagStrings, myMatch)) {

            String myKey = encodeString(getFirstAttribute(myKeyBuffer));

            if (null != myKey) {

                String myData = encodeString(getFirstAttribute(myDataBuffer));

                List<String> myList = myMap.get(myKey);

                if (null == myList) {

                    myList = new ArrayList<String>();
                }
                myList.add(myData);
                myMap.put(myKey, myList);
            }
        }

        return myMap;
    }

    public void restart() {

        initValues();
    }

    public boolean openFirstNode(String xpathIn) {

        boolean mySuccess = false;

        if (accessFirstNode(xpathIn)) {

            mySuccess = stepIntoNode();
        }
        if (mySuccess) {

            _level = _nodePath.size();

            byte[][] myPath = new byte[_level][];
            for (int i = 0; _level > i; i++) {

                myPath[i] = _nodePath.get(i);
            }
            _parentPath.add(myPath);
        }
        return mySuccess;
    }

    public boolean openNode(String xpathIn) {

        boolean mySuccess = false;

        if (accessNode(xpathIn)) {

            mySuccess = stepIntoNode();
        }
        if (mySuccess) {

            _level = _nodePath.size();

            byte[][] myPath = new byte[_level][];
            for (int i = 0; _level > i; i++) {

                myPath[i] = _nodePath.get(i);
            }
            _parentPath.add(myPath);
        }
        return mySuccess;
    }

    public boolean openNode() {

        boolean mySuccess = false;

        if ((AccessMode.NAME == _accessMode) || (AccessMode.ATTRIBUTES == _accessMode)) {

            mySuccess = stepIntoNode();
        }
        if (mySuccess) {

            _level = _nodePath.size();

            byte[][] myPath = new byte[_level][];
            for (int i = 0; _level > i; i++) {

                myPath[i] = _nodePath.get(i);
            }
            _parentPath.add(myPath);
        }
        return mySuccess;
    }
/*
    public boolean openChild(String parentXpathIn, String childXpathIn) {

        boolean mySuccess = false;

        if (accessChild(parentXpathIn, childXpathIn)) {

            mySuccess = stepIntoNode();
        }
        if (mySuccess) {

            _parentPath.add(_nodePath);
        }
        return mySuccess;
    }

    public boolean accessChild(String parentXpathIn, String childXpathIn) {

        byte[][] myParentTagStrings = extractTags(parentXpathIn);
        byte[][] myChildStrings = extractTags(childXpathIn);

        if (dropToParent(myParentTagStrings)) {

            return accessChildNode(myChildStrings, false);
        }
        return false;
    }
*/
    public boolean accessFirstNode(String xpathIn) {

        return accessNode(xpathIn, true);
    }

    public boolean accessNode(String xpathIn) {

        return accessNode(xpathIn, false);
    }

    public boolean accessNode(String xpathIn, boolean isFirst) {

        byte[][] myTagStrings = extractTags(xpathIn);

        if (0 == myTagStrings[0].length) {

            if (0 == myTagStrings[1].length) {

                byte[][] myCustomTags = new byte[(myTagStrings.length - 2)][];

                for (int i = 2; myTagStrings.length > i; i++) {

                    myCustomTags[(i - 2)] = myTagStrings[i];
                }

                return accessAnyNode(myCustomTags, isFirst);

            } else {

                byte[][] myCustomTags = new byte[(myTagStrings.length - 1)][];

                for (int i = 1; myTagStrings.length > i; i++) {

                    myCustomTags[(i - 1)] = myTagStrings[i];
                }

                return accessAbsoluteNode(myCustomTags, isFirst);
            }

        } else {

            return accessChildNode(myTagStrings, isFirst);
        }
    }

    public Integer getFirstAttributeAsInteger(String attributeNameIn) {

        return decodeInteger(getFirstAttribute(attributeNameIn.getBytes()));
    }

    public Integer getNextAttributeAsInteger(String attributeNameIn) {

        return decodeInteger(getNextAttribute(attributeNameIn.getBytes()));
    }

    public String getFirstAttributeAsString(String attributeNameIn) {

        return encodeString(getFirstAttribute(attributeNameIn.getBytes()));
    }

    public String getNextAttributeAsString(String attributeNameIn) {

        return encodeString(getNextAttribute(attributeNameIn.getBytes()));
    }

    public byte[] getFirstAttribute(String attributeNameIn) {

        return getFirstAttribute(attributeNameIn.getBytes());
    }

    public byte[] getNextAttribute(String attributeNameIn) {

        return getNextAttribute(attributeNameIn.getBytes());
    }

   public List<String> getDataStrings(){
      List<String> myResults = null;
      List<byte[]> myData = getData();

      if (myData != null) {
         myResults = new ArrayList<String>();

         for (byte[] mySection : myData) {
            myResults.add(encodeString(mySection));
         }
      }
      return myResults;
   }

    public List<byte[]> getData(){

        List<byte[]> myResults = null;

        if ((AccessMode.DATA == _accessMode) || stepIntoNode()) {

            Integer myHandle = markRewind();

            if (null != myHandle) {

                List<byte[]> myList = new ArrayList<byte[]>();
                markValue();

                for (int myByte = getNextByte(); -1 != myByte; myByte = getNextByte()) {

                    if ('<' == myByte) {

                        byte[] myData = getValue();

                        if ((null != myData) && (0 < myData.length)) {

                            myList.add(myData);
                        }

                        myByte = getNextByte();

                        if ('/' == myByte) {

                            myResults = myList;
                            break;

                        } else {

                            stepOverNode();
                            markValue();
                        }
                    }
                }
                cancelValue();
                rewind(myHandle);
                cancelRewind(myHandle);
            }
        }
        return myResults;
    }

   public List<String> getDataStringsAndClose(){
      List<String> myResults = null;
      List<byte[]> myData = getDataAndClose();

      if (myData != null) {
         myResults = new ArrayList<String>();

         for (byte[] mySection : myData) {
            myResults.add(encodeString(mySection));
         }
      }
      return myResults;
   }

    public List<byte[]> getDataAndClose(){

        List<byte[]> myResults = null;

        if ((AccessMode.DATA == _accessMode) || stepIntoNode()) {

            List<byte[]> myList = new ArrayList<byte[]>();

            markValue();

            for (int myByte = getNextByte(); -1 != myByte; myByte = getNextByte()) {

                if ('<' == myByte) {

                    byte[] myData = getValue();

                    if ((null != myData) && (0 < myData.length)) {

                        myList.add(myData);
                    }

                    myByte = getNextByte();

                    if ('/' == myByte) {

                        myResults = myList;
                        stepIntoNode();
                        break;

                    } else {

                        stepOverNode();
                        markValue();
                    }
                }
            }
            cancelValue();
            dropLevel();
            _parentPath.remove(_parentPath.size() - 1);
        }
        return myResults;
    }

    public List<String> getAllData(){

        return null; // NOT Supported !
    }

    public List<String> getAllDataAndClose(){

        return null; // NOT Supported !
    }

    public void closeNode() {

        stepOverNode();
        _parentPath.remove(_parentPath.size() - 1);
    }

    private void initValues() {

        super.restart();

        _level = 0;
        _accessMode = AccessMode.CLOSED;
        _nodePath = new ArrayList<byte[]>();
        _parentPath = new ArrayList<byte[][]>();
    }

    // Locate next node matching absolute
    private boolean accessAbsoluteNode(byte[][] tagStringsIn, boolean isFirstIn) {

        if (isFirstIn) {

            restart();
        }

        if ((AccessMode.ATTRIBUTES == _accessMode) || (AccessMode.NAME == _accessMode)) {

            stepOverNode();
        }

        boolean[] myMatch = establishContext(tagStringsIn);

        return getNextNode(tagStringsIn, myMatch);
    }

    // Locate next node matching path relative to current context
    private boolean accessChildNode(byte[][] tagStringsIn, boolean isFirstIn) {

//TODO:     if ()
        if ((AccessMode.ATTRIBUTES == _accessMode) || (AccessMode.NAME == _accessMode)) {

            stepOverNode();
        }

        return getNextChildNode(tagStringsIn);
    }

    // TODO:

    // TODO:

    // TODO:

    // TODO:

    // TODO:


    // Locate next node matching path at any level
    private boolean accessAnyNode(byte[][] tagStringsIn, boolean isFirstIn) {

//TODO:     if ()
        if ((AccessMode.ATTRIBUTES == _accessMode) || (AccessMode.NAME == _accessMode)) {

            stepOverNode();
        }

        return getNextDescendentNode(tagStringsIn[0]);
    }

    private boolean dropToParent(byte[][] parentTagStringsIn) {

        boolean[] myMatch = establishContext(parentTagStringsIn);
        boolean myOK = false;

        if (myMatch[(myMatch.length - 1)]) {

            int myMatchingLevel;

            for (myMatchingLevel = 0; myMatch.length > myMatchingLevel; myMatchingLevel++) {

                if (!myMatch[myMatchingLevel]) {

                    break;
                }
            }

            while (myMatchingLevel < _level) {

                stepOverNode();
                dropLevel();
            }
            myOK = true;

        } else {

            if (getNextNode(parentTagStringsIn, myMatch)) {

                myOK = stepIntoNode();
            }
        }
        return myOK;
    }
/*
    private List<String[]> locateAndOpenNode(String[] nameIn) {

    }
*/
    private boolean getNextNode(byte[][] tagStringsIn, boolean[] matchIn) {

        int myLimit = matchIn.length;
        int myLevel = myLimit;

        while (enterNextTag(0)) {

            if (myLevel >= _level) {

                for (int i = _level - 1; myLevel > i; i++) {

                    matchIn[i] = false;
                }
                myLevel = Math.min(_level, myLimit);
            }

            if ((matchIn.length >= _level) && ((1 == _level) || matchIn[(_level - 2)])) {

                if (matchName(tagStringsIn[(_level - 1)])) {

                    recordMatch(tagStringsIn, matchIn);

                    if (matchIn[(matchIn.length - 1)]) {

                        break;

                    } else {

                        stepIntoNode();
                    }

                } else {

                    stepIntoNode();
                }

            } else {

                stepIntoNode();
            }
        }

        return (AccessMode.ATTRIBUTES == _accessMode);
    }

    private boolean getNextChildNode(byte[][] tagStringsIn) {

        int myBase = _level;
        int myLimit = tagStringsIn.length;
        int myLevel = myBase + myLimit;
        boolean[] myMatch = new boolean[myLimit];

        while (enterNextTag(myBase)) {

            int myIndex = _level - myBase - 1;

            if (myLevel >= _level) {

                for (int i = Math.max(_level, myBase) - 1; Math.min(myLevel, myLimit) > i; i++) {

                    myMatch[i] = false;
                }
                myLevel = Math.min(_level, myLimit);
            }

            if ((0 <= myIndex) && (myLimit >= myIndex) && ((0 == myIndex) || myMatch[myIndex -1])) {

                if (matchName(tagStringsIn[myIndex])) {

                    recordMatch(tagStringsIn, myMatch, myBase);

                    if (myMatch[(myLimit - 1)]) {

                        break;

                    } else {

                        stepIntoNode();
                    }

                } else {

                    stepIntoNode();
                }

            } else {

                stepIntoNode();
            }
        }

        return (AccessMode.ATTRIBUTES == _accessMode);
    }

    private boolean getNextDescendentNode(byte[] tagNameIn) {

        boolean mySuccess = false;
        int myParentLevel = _parentPath.size();

        while (enterNextTag(myParentLevel)) {

            byte[] myName = extractName();

            _nodePath.add(myName);

            if (matchName(tagNameIn, myName)) {

                mySuccess = true;
                break;

            } else {

                stepIntoNode();
            }
        }

        return mySuccess;
    }

    private boolean matchName(byte[] patternIn, byte[] tagNameIn) {

        boolean mySuccess = (patternIn.length == tagNameIn.length);

        if (mySuccess) {

            for (int i = 0; patternIn.length > i; i++) {

                if (patternIn[i] != tagNameIn[i]) {

                    mySuccess = false;
                    break;
                }
            }
        }
        return mySuccess;
    }

    private boolean enterNextTag(int minLevelIn) {

        if ((AccessMode.CLOSED != _accessMode) && (AccessMode.DATA != _accessMode)) {

            stepIntoNode();
        }

        for (int myByte = getNextByte(); -1 != myByte; myByte = getNextByte()) {

            if ('<' == myByte) {

                myByte = peekByte(1);

                if ('/' == myByte) {

                    if (minLevelIn == _level) {

                        stepBack();
                        return false;

                    } else {

                        dropLevel();

                        if (0 == _level) {

                            _accessMode = AccessMode.CLOSED;

                        } else {

                            _accessMode = AccessMode.DATA;
                        }
                    }

                } else {

                    _propertiesRewindHandle = replaceRewind(_propertiesRewindHandle);
                    _accessMode = AccessMode.NAME;
                    return (0 < ++_level);
                }
            }
        }

        return false;
    }

    private byte[][] extractTags(String xpathIn) {

        byte[] myBuffer = xpathIn.getBytes();
        List<Integer> mySplit = new ArrayList<Integer>();
        byte[][] myResult = null;
        int myBase = 0;
        int mySize = 0;

        for (int i = 0; myBuffer.length > i; i++) {

            if ('/' == myBuffer[i]) {

                mySplit.add(i);
            }
        }
        mySplit.add(myBuffer.length);
        mySize = mySplit.size();

        myResult = new byte[mySize][];

        for (int i = 0; mySize > i; i++) {

            int myTop = mySplit.get(i);
            int myCount = myTop - myBase;
            byte[] myBytes = new byte[myCount];

            for (int j = 0; myCount > j; j++) {

                myBytes[j] = myBuffer[(myBase + j)];
            }
            myBase = myTop + 1;
            myResult[i] = myBytes;
        }

        return myResult;
    }

    private byte[] extractName() {

        if (AccessMode.NAME == _accessMode) {

            markValue();

            for (int myByte = getNextByte(); -1 != myByte; myByte = getNextByte()) {

                if ((' ' == myByte) || ('>' == myByte) || ('/' == myByte)) {

                    break;
                }
            }
        }
        return getValue();
    }

    private byte[] getFirstAttribute(final byte[] attributeNameIn) {

        rewind(_propertiesRewindHandle);
        skipBlanks();

        return getNextAttribute(attributeNameIn);
    }

    private  byte[] getNextAttribute(final byte[] attributeNameIn) {

        byte[] myValue = null;
        int myIndex = 0;
        final int myLimit = attributeNameIn.length;

        for (int myByte = getNextByte(); -1 != myByte; myByte = getNextByte()) {

            if (myLimit == myIndex) {

                if (((byte)' ' == myByte) || ('=' == myByte) || ('>' == myByte) || ('/' == myByte)) {

                    myValue = loadValue();
                    break;

                } else {

                    myIndex = 0;
                    discardAttribute();
                }

            } else if (('>' == myByte) || ('/' == myByte)) {

                break;

            } else {

                byte myNameByte = attributeNameIn[myIndex++];

                if (myNameByte !=  myByte) {

                    myIndex = 0;
                    discardAttribute();
                }
            }
            if (('>' == myByte) || ('/' == myByte)) {

                break;
            }
        }
        return myValue;
    }

    private byte[] getNextAttribute() {

        if (AccessMode.ATTRIBUTES == _accessMode) {

            if (('>' != peekByte(0)) && ('/' != peekByte(0))) {

                skipBlanks();

                markValue();

                for (int myByte = getNextByte(); -1 != myByte; myByte = getNextByte()) {

                    if ((' ' == myByte) || ('=' == myByte) || ('>' == myByte) || ('/' == myByte)) {

                        break;
                    }
                }

                return getValue();
            }
        }
        return null;
    }

    // Advance past the attributes to the closing bracket
    // and return true if not a self-contained node
    private boolean stepIntoNode() {

        boolean mySuccess = false;
        int myByte = peekByte(0);

        while ((-1 != myByte) && ('>' != myByte)) {

            myByte = getNextByte();
        }

        if ('>' == myByte) {

            if ('/' == peekByte(-1)) {

                dropLevel();
                if (0 == _level) {

                    _accessMode = AccessMode.CLOSED;

                } else {

                    _accessMode = AccessMode.DATA;
                }

            } else {

                _accessMode = AccessMode.DATA;
                mySuccess = true;
            }
        }
        _propertiesRewindHandle = cancelRewind(_propertiesRewindHandle);

        return mySuccess;
    }

    // Advance past all children to the first byte after the node
    // terminating tag or the end of the node tag if a self-contained node
    private boolean stepOverNode() {

        int myLevel = 0;

        if ((0 < _level) && (AccessMode.CLOSED != _accessMode)) {

            myLevel = 1;

            if ((AccessMode.DATA != _accessMode) && !stepIntoNode()) {
               myLevel = 0;
            }

            if (0 < myLevel) {

                for (int myByte = getNextByte(); -1 != myByte; myByte = getNextByte()) {

                    if ('<' == myByte) {

                        myByte = getNextByte();

                        if ('/' == myByte) {

                            myLevel--;
                            stepIntoNode();
                            if (0 == myLevel) {

                                break;
                            }

                        } else if (stepIntoNode()) {

                            myLevel++;
                        }
                    }
                }
            }
            if (0 == myLevel) {

                dropLevel();

                if (0 == _level) {

                    _accessMode = AccessMode.CLOSED;

                } else {

                    _accessMode = AccessMode.DATA;
                }
            }
        }
        _propertiesRewindHandle = cancelRewind(_propertiesRewindHandle);

        return (0 == myLevel);
    }

    private boolean matchName(byte[] nameIn) {

        int myOffset = 0;
        int myTestByte = -1;
        boolean mySuccess = false;

        if (AccessMode.NAME == _accessMode) {

            for (myOffset = 0; nameIn.length > myOffset; myOffset++) {

                myTestByte = getNextByte();

                if (nameIn[myOffset] != myTestByte) {

                    break;
                }
            }
            myTestByte = peekByte(1);

            if ((nameIn.length == myOffset)
                    && (((byte) ' ' == myTestByte) || ('>' == myTestByte) || ('/' == myTestByte))) {

                mySuccess = true;
            }
        }

        if (mySuccess) {

            _accessMode = AccessMode.ATTRIBUTES;
            _propertiesRewindHandle = replaceRewind(_propertiesRewindHandle);

        } else {

            rewind(_propertiesRewindHandle);
        }

        return mySuccess;
    }

    private byte[] loadValue() {

        int myTop = 0;

        // Position ourselves over the last character of the attribute name.
        stepBack();

        // Skip over all leading blanks
        skipBlanks();
        if ('=' == peekByte(1)) {

            // Skip over the equal sign plus any following blanks
            getNextByte();
            skipBlanks();



            if ('"' == peekByte(1)) {

                // Skip over value quote
                getNextByte();

                // Identify the start of the value
                markValue();

                // Count the number of value bytes while stepping over them and the trailing quote
                for (myTop = 0; '"' != getNextByte(); myTop++) {

               }
            }
        }
        return (0 < myTop) ? getValue(myTop) : cancelValue();
    }

    private void discardAttribute() {

        int myByte = -1;

        for (myByte = peekByte(0); -1 != myByte; myByte = getNextByte()) {

            if ((' ' == myByte) || ('=' == myByte) || ('>' == myByte) || ('/' == myByte)) {

                break;
            }
        }
        if ('>' != myByte) {

            skipValue();
        }
    }

    private void skipValue() {

        // Position ourselves over the last character of the attribute name.
        stepBack();

        // Skip over all leading blanks
        skipBlanks();

        if ('=' == peekByte(1)) {

            // Skip over the equal sign plus any following blanks
            getNextByte();
            skipBlanks();

            if ('"' == peekByte(1)) {

                // Skip over value bytes plus both quotes
                getNextByte();
                for (int myByte = getNextByte(); -1 != myByte; myByte = getNextByte()) {

                    if ('"' == myByte) {

                        break;
                    }
                }

                // Skip over any blanks following the value
                skipBlanks();
            }
        }
    }

    private boolean[] establishContext(byte[][] tagStringsIn) {

        boolean[] myMatch = new boolean[tagStringsIn.length];
        int myLimit = _nodePath.size();

        for (int i = 0; myMatch.length > i; i++) {

            myMatch[i] = false;

            if (myLimit > i) {

                byte[] mySource = _nodePath.get(i);
                byte[] myTest = tagStringsIn[i];

                if (mySource.length == myTest.length) {

                    myMatch[i] = true;

                    for (int j = 0; mySource.length > j; j++) {

                        if (mySource[j] != myTest[j]) {

                            myMatch[i] = false;
                            break;
                        }
                    }
                    if (!myMatch[i]) {

                        myLimit = i;
                    }
                }
            }
        }
        return myMatch;
    }

    private void dropLevel() {

        _level = Math.max(0, (_level - 1));
        for (int i = _nodePath.size() - 1; _level <= i; i--) {

            _nodePath.remove(i);
        }
    }

    private void recordMatch(byte[][] tagStringsIn, boolean[] matchIn) {

        if ((0 < _level) && (tagStringsIn.length >= _level)) {

            for (int myIndex = _nodePath.size(); _level > myIndex; myIndex++) {

                byte[] mySource = tagStringsIn[myIndex];
                int mySize = mySource.length;
                byte[] myTarget = new byte[mySize];

                for (int i = 0; mySize > i; i++) {

                    myTarget[i] = mySource[i];
                }
                matchIn[myIndex] = true;

                _nodePath.add(myTarget);
            }
        }
    }

    private void recordMatch(byte[][] tagStringsIn, boolean[] matchIn, int baseIn) {

        int myLevel = _level - baseIn;

        if ((0 < myLevel) && (tagStringsIn.length >= myLevel)) {

            for (int myIndex = _nodePath.size() - baseIn; myLevel > myIndex; myIndex++) {

                byte[] mySource = tagStringsIn[myIndex];
                int mySize = mySource.length;
                byte[] myTarget = new byte[mySize];

                for (int i = 0; mySize > i; i++) {

                    myTarget[i] = mySource[i];
                }
                matchIn[myIndex] = true;

                _nodePath.add(myTarget);
            }
        }
    }

    private String encodeString(byte[] valueIn) {

        if ((null != valueIn) && (0 < valueIn.length)) {

            if (null != _encoding) {

                try {

                    return new String(valueIn, _encoding);

                } catch(UnsupportedEncodingException myException) {
                }
            }

            return new String(valueIn);

        } else {

            return null;
        }
    }

    private Integer decodeInteger(byte[] valueIn) {

        Integer myResult = null;

        if ((null != valueIn) && (0 < valueIn.length)) {

            int myValue = 0;
            int myBase = 0;
            int myCount;

            if ('-' == valueIn[0]) {

                myBase++;
            }

            for (myCount = myBase; valueIn.length > myCount; myCount++) {

                int myDigit = valueIn[myCount] - '0';

                if ((0 > myDigit) || (9 < myDigit)) {

                    break;
                }
                myValue = (myValue * 10) + myDigit;
            }
            if (myCount > myBase) {

                if (0 < myBase) {

                    myValue *= -1;
                }
                myResult = myValue;
            }
        }
        return myResult;
    }
}
