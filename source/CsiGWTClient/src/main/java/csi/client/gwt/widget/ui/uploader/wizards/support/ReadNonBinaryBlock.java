package csi.client.gwt.widget.ui.uploader.wizards.support;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.google.gwt.typedarrays.shared.Int8Array;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.util.Display;
import csi.server.common.enumerations.CsiEncoding;
import csi.server.common.enumerations.CsiFileType;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.util.DocumentEncoder;
import csi.server.common.util.EncodingByteValues;
import csi.server.common.util.Format;

/**
 * Created by centrifuge on 9/11/2015.
 */
public abstract class ReadNonBinaryBlock extends ReadBlock {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected static final CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    private static final Map<CsiEncoding, DocumentEncoder.EncodingMethod> _encodingMapIn
            = new TreeMap<CsiEncoding, DocumentEncoder.EncodingMethod>();

    static {

        _encodingMapIn.put(CsiEncoding.UTF_8, DocumentEncoder.EncodingMethod.utf8);
        _encodingMapIn.put(CsiEncoding.UTF_16LE, DocumentEncoder.EncodingMethod.utf16le);
        _encodingMapIn.put(CsiEncoding.UTF_16BE, DocumentEncoder.EncodingMethod.utf16be);
    }

    private static final Map<DocumentEncoder.EncodingMethod, CsiEncoding> _encodingMapOut
            = new TreeMap<DocumentEncoder.EncodingMethod, CsiEncoding>();

    static {

        _encodingMapOut.put(DocumentEncoder.EncodingMethod.utf8, CsiEncoding.UTF_8);
        _encodingMapOut.put(DocumentEncoder.EncodingMethod.utf16le, CsiEncoding.UTF_16LE);
        _encodingMapOut.put(DocumentEncoder.EncodingMethod.utf16be, CsiEncoding.UTF_16BE);
        _encodingMapOut.put(DocumentEncoder.EncodingMethod.ansi, CsiEncoding.LATIN1);
    }

    int _limit;
    int _delimiter = -2;
    List<Integer> _characterBlock;
    DocumentEncoder.EncodingMethod _encodingMethod = null;
    CsiEncoding _encoding = null;
    boolean _fixNulls = false;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Abstract Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected abstract String getNextColumn() throws CentrifugeException;
    protected abstract String getRawRow() throws CentrifugeException;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Static Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public static ReadNonBinaryBlock createReader(CsiFileType fileTypeIn, Int8Array dataSourceIn, boolean fixNullsIn) {

        switch (fileTypeIn) {

            case CSV :

                return new ReadCsvBlock(dataSourceIn, fixNullsIn);

            case TEXT :

                return new ReadTextBlock(dataSourceIn, fixNullsIn);
        }
        return null;
    }

    public static ReadNonBinaryBlock createReader(CsiFileType fileTypeIn, ReadNonBinaryBlock priorBlockIn, boolean fixNullsIn) {

        switch (fileTypeIn) {

            case CSV :

                return new ReadCsvBlock(priorBlockIn._dataSource, priorBlockIn._characterBlock, priorBlockIn._encoding, fixNullsIn);

            case TEXT :

                return new ReadTextBlock(priorBlockIn._dataSource, priorBlockIn._characterBlock, priorBlockIn._encoding, fixNullsIn);
        }
        return null;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public ReadNonBinaryBlock(Int8Array dataSourceIn, boolean fixNullsIn) {

        this(dataSourceIn, null, null, fixNullsIn);
    }

    public ReadNonBinaryBlock(Int8Array dataSourceIn, List<Integer> characterBlockIn,
                              CsiEncoding encodingIn, boolean fixNullsIn) {

        super(dataSourceIn);

        if ((null != characterBlockIn) && (null != encodingIn)) {

            setEncodingMethod(encodingIn);
            _encoding = encodingIn;
            _characterBlock = characterBlockIn;
            _limit = _characterBlock.size();

        } else {

            _encoding = null;
            _encodingMethod = null;
            _characterBlock = null;
            _limit = 0;
        }
        _offset = 0;
        _lineCount = 0;
        _fixNulls = fixNullsIn;
    }

    public List<Integer> getCharacterBlock() throws Exception {

        if (null == _characterBlock) {

            determineEncoding();
        }

        return _characterBlock;
    }

    public void setDelimiter(int delimiterIn) {

        _delimiter = delimiterIn;
    }

    public void clearDelimiter() {

        _delimiter = -2;
    }

    public CsiEncoding determineEncoding() throws Exception {

        return determineEncoding(false);
    }

    public CsiEncoding determineEncoding(boolean ignoreNullsIn) throws Exception {

        CsiEncoding myEncoding = ignoreNullsIn ? null : _encoding;
        DocumentEncoder.EncodingMethod myEncodingMethod = _encodingMethod;
        List<DocumentEncoder.EncodingMethod> myRejectionList = new ArrayList<DocumentEncoder.EncodingMethod>();

        while (null == myEncoding) {

            if (null != _dataSource) {

                myEncodingMethod = DocumentEncoder.identifyEncoding(new DataSource(_dataSource), myRejectionList, ignoreNullsIn);

                if (null != myEncodingMethod) {

                    myEncoding = _encodingMapOut.get(myEncodingMethod);
                }
            }
            if (null == myEncoding) {

                myEncoding = CsiEncoding.LATIN1;
            }
            if (!changeEncoding(myEncoding)) {

                myRejectionList.add(myEncodingMethod);
//                Display.error("Failed processing file as "
//                                + ((null != myEncoding) ? myEncoding.getLabel() : "null")
//                                + ".\n Check encoding value.");
                myEncoding = null;
            }
        }
        return _encoding;
    }

    public boolean changeEncoding(final CsiEncoding encodingIn) throws Exception {

        boolean myChange = false;
        DocumentEncoder.EncodingMethod myMethod = setEncodingMethod(encodingIn);

        if (_encodingMethod != myMethod) {

            List<Integer> myCharacterBlock = DocumentEncoder.decodeDataBlock(new DataSource(_dataSource),
                                                                                myMethod, _dataSource.length());

            if (null != myCharacterBlock) {

                _characterBlock = myCharacterBlock;
                _limit = _characterBlock.size();
                _encodingMethod = myMethod;
                _encoding = encodingIn;
                myChange = true;
            }

        } else {

            _encoding = encodingIn;
        }
        return myChange;
    }

    @Override
    public ReadBlock restart() throws Exception {

        super.restart();

        getCharacterBlock();

        return this;
    }

    public boolean isAtEOD() {

        return (_offset >= _limit);
    }

    private DocumentEncoder.EncodingMethod setEncodingMethod(CsiEncoding encodingIn) {

        DocumentEncoder.EncodingMethod myMethod = _encodingMapIn.get(encodingIn);

        if (null == myMethod) {

            myMethod = DocumentEncoder.EncodingMethod.ansi;
        }
        return myMethod;
    }

    public void getNumberedListData(List<String[]> listIn, int maxLinesIn, int maxSizeIn) throws Exception {

        restart();

        getListData(listIn, maxLinesIn, maxSizeIn, true);
    }

    public void getListData(List<String[]> listIn, int maxLinesIn, int maxSizeIn) throws Exception {

        restart();

        getListData(listIn, maxLinesIn, maxSizeIn, false);
    }

    public void getListData(List<String[]> listIn, int maxLinesIn, int maxSizeIn, boolean withNumbersIn) {

        int myColumnCount = 0;
        int myDataColumn = withNumbersIn ? 1 : 0;
        int myClipSize = Math.max(maxSizeIn - 4, 0);
        final int myLimit = (0 < maxLinesIn) ? _lineCount + maxLinesIn : -1;
        int myLineNumber = _lineCount;

        try {

            for (String[] myRow = getRawRow(withNumbersIn);
             null != myRow;
             myRow = getRawRow(withNumbersIn)) {

                if (myRow.length > myColumnCount) {

                    myColumnCount = myRow.length;
                }
                if ((0 < maxSizeIn) && (myDataColumn < myRow.length)) {

                    String myData = myRow[myDataColumn];

                    if ((null != myData) && (myData.length() > maxSizeIn)) {

                        myRow[myDataColumn] = myData.substring(0, myClipSize) + " ...";
                    }
                }
                listIn.add(myRow);

                if ((myLineNumber == _lineCount) || (myLimit == _lineCount)) {

                    break;
                }
                myLineNumber = _lineCount;
            }

        } catch (CentrifugeException myException) {

            Display.error("Caught exception reading line "
                    + Format.value(myLineNumber), myException);
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected String[] getRawRow(boolean withNumbersIn) throws CentrifugeException {

        List<String> myData = new LinkedList<String>();
        String myValue = null;
        int myLine = _lineCount;

        if (withNumbersIn) {

            myData.add(Integer.toString(myLine + 1));
        }
        myValue = getRawRow();
        myData.add(myValue);

        return (null != myValue) ? myData.toArray(new String[0]) : null;
    }

    protected void checkIntegrity() throws CentrifugeException {

        int myValue = getChar();

        if ((_delimiter != myValue) && (EncodingByteValues.cpEOS != myValue)) {

            if (EncodingByteValues.cpAsciiReturn == myValue) {

                myValue = getChar();

                if (EncodingByteValues.cpAsciiNewLine != myValue) {

                    _offset--;
                }
                _lineCount++;

            } else if (EncodingByteValues.cpAsciiNewLine == myValue) {

                _lineCount++;

            } else {

                throw new CentrifugeException(_constants.installFileWizard_CellDataFormatError(_constants.installFileWizard_CellDataErrorReason5(Integer.toString(myValue))));
            }
        }
    }

    protected int peekChar() throws CentrifugeException {

        int myOffset = _offset;
        int myCharacter = getChar();
        _offset = myOffset;
        return myCharacter;
    }

    protected int getChar() throws CentrifugeException {

        int myValue = EncodingByteValues.cpEOS;

        if (_fixNulls) {

            while (_limit > _offset) {

                myValue = _characterBlock.get(_offset++);

                if (EncodingByteValues.cpAsciiNull != myValue) {

                    break;

                } else if (_limit <= _offset) {

                    myValue = EncodingByteValues.cpEOS;
                }
            }

        } else if (_limit > _offset) {

            myValue = _characterBlock.get(_offset++);
            if (EncodingByteValues.cpAsciiNull == myValue) {

                throw new CentrifugeException(_constants.nullCharacterEncountered());
            }
        }
        return myValue;
    }

    protected int getChar(int offsetIn) {

        if (_limit > offsetIn) {

            return _characterBlock.get(offsetIn);
        }
        return EncodingByteValues.cpEOS;
    }
}
