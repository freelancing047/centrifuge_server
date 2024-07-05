package csi.server.business.helper;

import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.server.common.exception.CentrifugeException;

public class Argument {
   private static final Logger LOG = LogManager.getLogger(Argument.class);

    public enum DecodeMask {
        DECODE_NONE(0), DECODE_KEY(1), DECODE_VALUE(2), DECODE_BOTH(3);

        private int value;

        private DecodeMask(int valueIn) {
            this.value = valueIn;
        }

        public int Mask() {
            return this.value;
        }
    }

    public static String decodeString(String stringIn) throws CentrifugeException {
        String myString = null;

        if (null != stringIn) {
            try {
                myString = URLDecoder.decode(stringIn, "UTF-8");
            } catch (Exception myException) {
                try {
                    throw new CentrifugeException("Caught an exception decoding argument string \"" + stringIn + "\".",
                            myException);
                } catch (Exception e) {
                    throw new CentrifugeException("Caught an exception decoding argument string.", myException);
                }
            }
        }
        return myString;
    }

    private boolean doDebug = false;

    private String base = null;
    private Map<String, Argument> childMap = null;
    private String[][] childList = null;

    public Argument(String stringIn, boolean doDebugIn) throws CentrifugeException {
        this.doDebug = doDebugIn;

        if (this.doDebug)
           LOG.trace("-- -- --  Argument::Argument(\"" + stringIn + "\")");

        this.base = decodeString(stringIn);

        if (this.doDebug)
           LOG.trace("           - base = \"" + this.toString() + "\"");
    }

    public Argument(String stringIn, boolean decodeIn, boolean doDebugIn) throws CentrifugeException {
        this.doDebug = doDebugIn;

        if (this.doDebug)
           LOG.trace("-- -- --  Argument::Argument(\"" + stringIn + "\", " + ((decodeIn) ? "true" : "false") + ")");

        if (decodeIn) {
            this.base = decodeString(stringIn);
        } else {
            this.base = stringIn;
        }

        if (this.doDebug)
           LOG.trace("           - base = \"" + this.toString() + "\"");
    }

    public int length() throws CentrifugeException {
        int myCount = 0;

        if (null == this.childList) {
            buildList();
        }

        if (null != this.childList) {
            myCount = this.childList.length;
        }
        return myCount;
    }

    public String get() {
        return this.base;
    }

    public String getKey(int indexIn) throws CentrifugeException {
        String myResult = null;

        if (null == this.childList) {
            buildList();
        }

        if (null != this.childList) {
            if (this.childList.length > indexIn) {
                myResult = this.childList[indexIn][0];
            }
        }

        return myResult;
    }

    public Argument getArgument(int indexIn) throws CentrifugeException {
        Argument myResult = null;

        if (null == this.childList) {
            buildList();
        }

        if (null != this.childList) {
            if (this.childList.length > indexIn) {
                if (null != this.childMap) {
                    myResult = getChildArgument(this.childList[indexIn][0]);
                } else {
                    myResult = new Argument(this.childList[indexIn][1], this.doDebug);
                }
            }
        }

        return myResult;
    }

    public String get(int indexIn) throws CentrifugeException {
        String myResult = null;

        if (null == this.childList) {
            buildList();
        }

        if (null != this.childList) {
            if (this.childList.length > indexIn) {
                myResult = decodeString(this.childList[indexIn][1]);
            }
        }

        return myResult;
    }

    public int getCount() throws CentrifugeException {
        int myCount = 0;

        if (null == this.childList) {
            buildList();
        }

        if (null != this.childList) {
            myCount = this.childList.length;
        }

        return myCount;
    }

    public String[] getPair(int indexIn) throws CentrifugeException {
        return getPair(indexIn, DecodeMask.DECODE_NONE);
    }

    public String[] getPair(int indexIn, DecodeMask maskIn) throws CentrifugeException {
        String[] myResult = null;

        if (null == this.childList) {
            buildList();
        }

        if (null != this.childList) {
            if (this.childList.length > indexIn) {
                if (DecodeMask.DECODE_NONE == maskIn) {
                    myResult = this.childList[indexIn];
                } else {
                    String[] myPair = this.childList[indexIn];

                    myResult = new String[2];

                    if (0 != (DecodeMask.DECODE_KEY.Mask() & maskIn.Mask())) {
                        myResult[0] = decodeString(myPair[0]);
                    } else {
                        myResult[0] = myPair[0];
                    }

                    if (0 != (DecodeMask.DECODE_VALUE.Mask() & maskIn.Mask())) {
                        myResult[1] = decodeString(myPair[1]);
                    } else {
                        myResult[1] = myPair[1];
                    }
                }
            }
        }

        return myResult;
    }

    public Argument getArgument(String requestIn) throws CentrifugeException {
        return getArgument(requestIn, -1);
    }

    public String get(String requestIn) throws CentrifugeException {
        return get(requestIn, -1);
    }

    private Argument getArgument(String requestIn, int indexIn) throws CentrifugeException {
        Argument myResult = this;

        if ((null != requestIn) && (0 < requestIn.length())) {
            String[] myRequest = peelKey(requestIn);

            myResult = null;

            if ((null != myRequest) && (0 < myRequest.length)) {
                myResult = getChildArgument(myRequest[0]);

                if (1 < myRequest.length) {
                    myResult = getArgument(myRequest[1], -1);
                }
            }
        }

        if ((null != myResult) && (0 <= indexIn)) {
            myResult = myResult.getArgument(indexIn);
        }

        return myResult;
    }

    @Override
    public String toString() {
        if (null != this.base) {
            return this.base;
        } else {
            return "<null>";
        }
    }

    public String get(String requestIn, int indexIn) throws CentrifugeException {
        String myResult = null;
        Argument myArg = getArgument(requestIn, indexIn);

        if (null != myArg) {
            myResult = myArg.get();
        }

        return myResult;
    }

    private Argument getChildArgument(String keyIn) throws CentrifugeException {
        Argument myResult = null;
        String myKey = keyIn.toLowerCase();

        if (null == this.childMap) {
            buildMap();
        }

        if (null != this.childMap) {
            if (this.childMap.containsKey(myKey)) {
                myResult = this.childMap.get(myKey);
            }
        }

        return myResult;
    }

    private void buildList() throws CentrifugeException {
        String[][] myList = null;

        if (null != this.base) {
            String[] myArgs = this.base.split("&");

            if ((null != myArgs) && (0 < myArgs.length)) {
                myList = new String[myArgs.length][];

                for (int i = 0; myArgs.length > i; i++) {
                    String myArg = myArgs[i];
                    myList[i] = myArg.split("=");
                }
            }
        }
        this.childList = myList;
    }

    private void buildMap() throws CentrifugeException {
        Map<String, Argument> myMap = null;

        if (null != this.base) {
            String[] myArgs = this.base.split("&");

            if ((null != myArgs) && (0 < myArgs.length)) {
                myMap = new HashMap<String, Argument>();

                for (int i = 0; myArgs.length > i; i++) {
                    String myArg = myArgs[i];
                    String[] myPair = myArg.split("=");
                    String myKey = myPair[0].toLowerCase();

                    if (2 == myPair.length) {
                        if (myMap.containsKey(myKey)) {
                           LOG.error("Encountered multiple values for key \"" + myKey
                                    + "\". Disregarding all but the first value.");
                        } else {
                            myMap.put(myKey, new Argument(myPair[1], this.doDebug));
                        }
                    } else {
                       LOG.error("Encountered bad argument \"" + myArg + "\". Disregarding it.");
                    }
                }
            }
        }
        this.childMap = myMap;
    }

    private String[] peelKey(String requestIn) {
        String[] myResult = null;

        if ((null != requestIn) && (0 < requestIn.length())) {
            int mySplit = requestIn.indexOf(":");

            if (-1 != mySplit) {
                if ((requestIn.length() - 1) > mySplit) {
                    myResult = new String[2];
                    myResult[0] = requestIn.substring(0, (mySplit - 1));
                    myResult[1] = requestIn.substring(mySplit + 1);
                }
            }

            if (null == myResult) {
                myResult = new String[1];
                myResult[0] = requestIn;
            }
        }

        return myResult;
    }
}
