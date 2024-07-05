package csi.client.gwt.widget.ui.uploader.wizards.support;

import java.util.List;

/**
 * Created by centrifuge on 9/11/2015.
 */
public class QuotingTest {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static final Integer AsciiReturn = 13;
    private static final Integer AsciiNewLine = 10;

    private List<Integer> _characterBlock;
    private int _limit;
    private int _delimeter;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public QuotingTest(List<Integer> characterBlockIn, int delimeterIn) {

        _characterBlock = characterBlockIn;
        _limit = characterBlockIn.size();
        _delimeter = delimeterIn;
    }

    public int[] identifyQuote(int[] listIn, int lineCountIn) {

        int myLimit = listIn.length;
        int[] myList = new int[myLimit];

        if (0 < myLimit) {

            for (int i = 0, j = 0; (lineCountIn > i) && (_limit > j); i++) {

                j = processNextLine(listIn, myList, j);
            }
        }
        return myList;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private int processNextLine(final int[] testListIn, int[] resultsListIn, int offsetIn) {

        int myOffset = offsetIn;
        int myLastCharacter = -1;
        int myQuote = -1;
        boolean myDelimeterFound = true;

        for (myOffset = offsetIn; _limit > myOffset; myOffset++) {

            int myCharacter = _characterBlock.get(myOffset);

            if ((_delimeter == myCharacter) || (AsciiReturn == myCharacter) || (AsciiNewLine == myCharacter)) {

                if (0 <= myQuote) {

                    if (myLastCharacter == myQuote) {

                        if (0 <= resultsListIn[myQuote]) {

                            resultsListIn[myQuote]++;
                        }

                    } else {

                        resultsListIn[myQuote] = -1;

                        if (0 <= myLastCharacter) {

                            resultsListIn[myLastCharacter] = -1;
                        }
                    }

                } else if (0 <= myLastCharacter) {

                    resultsListIn[myLastCharacter] = -1;
                }

                myDelimeterFound = true;
                myLastCharacter = -1;
                myQuote = -1;

                if (AsciiNewLine == myCharacter) {

                    break;
                }

            } else {

                int mySavedValue = myLastCharacter;
                myLastCharacter = -1;

                for (int i = 0; testListIn.length > i; i++) {

                    if (testListIn[i] == myCharacter) {

                        if(i == mySavedValue) {

                            mySavedValue = -1;

                        } else if (0 < mySavedValue) {

                            if (myQuote == mySavedValue) {

                                myQuote = -1;
                            }
                            resultsListIn[mySavedValue] = -1;
                            mySavedValue = -1;
                            myLastCharacter = i;

                        } else {

                            if (myDelimeterFound) {

                                myQuote = i;

                            } else {

                                myLastCharacter = i;
                            }
                        }
                    }
                }
                if(0 <= mySavedValue) {

                    resultsListIn[mySavedValue] = -1;
                }
                myDelimeterFound = false;
            }
        }

        return myOffset;
    }
}
