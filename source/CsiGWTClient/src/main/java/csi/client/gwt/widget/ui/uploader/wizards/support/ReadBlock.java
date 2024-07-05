package csi.client.gwt.widget.ui.uploader.wizards.support;

import java.util.LinkedList;
import java.util.List;

import com.google.gwt.typedarrays.shared.Int8Array;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.util.Display;
import csi.server.common.exception.CentrifugeException;

/**
 * Created by centrifuge on 9/11/2015.
 */
public abstract class ReadBlock {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected static final CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    int _offset;
    int _lineCount = 0;
    Int8Array _dataSource = null;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Abstract Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected abstract String getNextColumn() throws CentrifugeException;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public ReadBlock(Int8Array dataSourceIn) {

        _offset = 0;
        _lineCount = 0;
        _dataSource = dataSourceIn;
    }

    public int getNumberedGridData(List<String[]> listIn, int maxLinesIn) throws Exception {

        restart();

        return getGridData(listIn, maxLinesIn, true);
    }

    public int getGridData(List<String[]> listIn, int maxLinesIn) throws Exception {

        restart();

        return getGridData(listIn, maxLinesIn, false);
    }

    public ReadBlock restart() throws Exception {

        _offset = 0;
        _lineCount = 0;

        return this;
    }

    public String[] getColumnHeaders() throws Exception {

        restart();

        return getRow(false);
    }

    public int countColumns() throws CentrifugeException {

        String myValue = null;
        int myLine = _lineCount;
        int myColumnCount = 0;

        try {

            for (myValue = getNextColumn();
                 null != myValue;
                 myValue = getNextColumn()) {

                myColumnCount++;
                if (myLine < _lineCount) {

                    break;
                }
            }

        } catch (Exception myException) {

            if (myException.getMessage().equals(_constants.nullCharacterEncountered())) {

                throw myException;
            }
        }
        return myColumnCount;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected int getGridData(List<String[]> listIn, int maxLinesIn, boolean withNumbersIn) {

        int myColumnCount = 0;
        final int myLimit = (0 < maxLinesIn) ? _lineCount + maxLinesIn : -1;
        int myLineNumber = _lineCount;

        try {

            for (String[] myRow = getRow(withNumbersIn);
                 null != myRow;
                 myRow = getRow(withNumbersIn)) {

                if (myRow.length > myColumnCount) {

                    myColumnCount = myRow.length;
                }
                listIn.add(myRow);

                if ((myLineNumber == _lineCount) || (myLimit == _lineCount)) {

                    break;
                }
                myLineNumber = _lineCount;
            }

        } catch (Exception myException) {

            Display.error("Caught Exception Reading Data", myException);
        }
        return myColumnCount;
    }

    protected String[] getRow(boolean withNumbersIn) throws CentrifugeException {

        List<String> myData = new LinkedList<String>();
        String myValue = null;
        int myLine = _lineCount;

        if (withNumbersIn) {

            myData.add(Integer.toString(myLine + 1));
        }

        try {

            for (myValue = getNextColumn();
             null != myValue;
             myValue = getNextColumn()) {

                myData.add(myValue);

                if (myLine < _lineCount) {

                    break;
                }
            }

        } catch (Exception myException) {

            myValue = "???????";
            myData.add(myValue);
            throw myException;
        }

        return (null != myValue) ? myData.toArray(new String[0]) : null;
    }
}
