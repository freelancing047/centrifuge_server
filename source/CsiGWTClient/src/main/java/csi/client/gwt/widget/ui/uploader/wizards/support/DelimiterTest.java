package csi.client.gwt.widget.ui.uploader.wizards.support;

import java.util.ArrayList;
import java.util.List;

import csi.server.common.enumerations.CsiColumnDelimiter;

/**
 * Created by centrifuge on 9/11/2015.
 */
public class DelimiterTest {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static final byte[] _characterMap = new byte[] {

            //  00  01  02  03  04  05  06  07  08  09  0A  0B  0C  0D  0E  0F

            -1, -1, -1, -1, -1, -1, -1, -1, -1,  1, -1, -1, -1, -1, -1, -1, //0F
            -1, -1, -1, 18, -1, -1, -1, -1, -1, -1, -1, -1, -1, 16, 17, 15, //1F
            14, 11, -1,  5,  6,  9, -1, -1, -1, -1, 10, 13,  0,  8, -1, 12, //2F
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, //3F
            7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, //4F
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, //5F
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, //6F
            -1, -1,  3,  4, -1, -1, -1, -1, -1, -1, -1, -1,  2, -1, -1, -1  //7F
    };

    private static final int _characterMapSize = _characterMap.length;
    private static final int _delimiterTallySize = CsiColumnDelimiter.values().length;
    private static final Integer AsciiNewLine = 10;

    private List<Integer> _characterBlock;
    private int _offset;
    private int _limit;
    private int _rowLimit;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public DelimiterTest(List<Integer> characterBlockIn, int rowLimitIn) {

        _characterBlock = characterBlockIn;
        _limit = characterBlockIn.size();
        _rowLimit = rowLimitIn;
    }

    public CsiColumnDelimiter identifySingleCsvDelimiter(int quoteIn) {

        List<int[]> myList = new ArrayList<int[]>();

        _offset = 0;

        if (0 != quoteIn) {

            for (int[] myTally = tallySingleCsvDelimiters(quoteIn);
                 null != myTally;
                 myTally = tallySingleCsvDelimiters(quoteIn)) {

                myList.add(myTally);
                if (_rowLimit <= myList.size()) {

                    break;
                }
            }

        } else {

            for (int[] myTally = tallySingleDelimiters();
                 null != myTally;
                 myTally = tallySingleDelimiters()) {

                myList.add(myTally);
                if (_rowLimit <= myList.size()) {

                    break;
                }
            }
        }
        return identifySingleDelimiter(myList);
    }

    public CsiColumnDelimiter identifySingleTxtDelimiter(int escapeIn) {

        List<int[]> myList = new ArrayList<int[]>();

        _offset = 0;

        if (0 != escapeIn) {

            for (int[] myTally = tallySingleTextDelimiters(escapeIn);
                 null != myTally;
                 myTally = tallySingleTextDelimiters(escapeIn)) {

                myList.add(myTally);
                if (_rowLimit <= myList.size()) {

                    break;
                }
            }

        } else {

            for (int[] myTally = tallySingleDelimiters();
                 null != myTally;
                 myTally = tallySingleDelimiters()) {

                myList.add(myTally);
                if (_rowLimit <= myList.size()) {

                    break;
                }
            }
        }
        return identifySingleDelimiter(myList);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private CsiColumnDelimiter identifySingleDelimiter(List<int[]> listIn) {

        int[] myCount = new int[_delimiterTallySize];
        int myFinalChoice = 0;

        for (int i = 0; listIn.size() > i; i++) {

            int myChoice = 0;
            int[] mySubCount = listIn.get(i);

            for (int j = 0; _delimiterTallySize > j; j++) {

                if (mySubCount[j] > mySubCount[myChoice]) {

                    myChoice = j;
                }
            }
            myCount[myChoice] += 1;
        }

        for (int i = 0; _delimiterTallySize > i; i++) {

            if (myCount[i] > myCount[myFinalChoice]) {

                myFinalChoice = i;
            }
        }
        return CsiColumnDelimiter.values()[myFinalChoice];
    }

    private int[] tallySingleCsvDelimiters(int quoteIn) {

        int[] myTally = new int[_delimiterTallySize];
        Integer myTest = null;
        boolean myQuoting = false;

        while (_limit > _offset) {

            myTest = _characterBlock.get(_offset++);

            if (!myQuoting) {

                if (AsciiNewLine == myTest) {

                    break;

                } else if (quoteIn == myTest) {

                    myQuoting = true;

                } else {

                    if (_characterMap.length > myTest) {

                        int myValue = _characterMap[myTest];

                        if ((0 <= myValue) && (_delimiterTallySize > myValue)) {

                            (myTally[myValue])++;
                        }
                    }
                }

            } else if (quoteIn == myTest) {

                myQuoting = false;
            }
        }
        return (AsciiNewLine == myTest) ? myTally : null;
    }

    private int[] tallySingleTextDelimiters(final int escapeIn) {

        int[] myTally = new int[_delimiterTallySize];
        Integer myTest = null;
        boolean myDoEscape = false;

        while (_limit > _offset) {

            myTest = _characterBlock.get(_offset++);

            if (!myDoEscape) {

                if (AsciiNewLine == myTest) {

                    break;

                } else if (escapeIn == myTest) {

                    myDoEscape = true;

                } else {

                    if (_characterMap.length > myTest) {

                        int myValue = _characterMap[myTest];

                        if ((0 <= myValue) && (_delimiterTallySize > myValue)) {

                            (myTally[myValue])++;
                        }
                    }
                }

            } else {

                myDoEscape = false;
            }
        }
        return (AsciiNewLine == myTest) ? myTally : null;
    }

    //
    // Perform a tally of possible single delimiter characters without any quoting or escaping.
    //
    private int[] tallySingleDelimiters() {

        int[] myTally = new int[_delimiterTallySize];
        Integer myTest = null;

        while (_limit > _offset) {

            myTest = _characterBlock.get(_offset++);

            if (AsciiNewLine == myTest) {

                break;

            } else {

                if (_characterMap.length > myTest) {

                    int myValue = _characterMap[myTest];

                    if ((0 <= myValue) && (_delimiterTallySize > myValue)) {

                        (myTally[myValue])++;
                    }
                }
            }
        }
        return (AsciiNewLine == myTest) ? myTally : null;
    }
}
