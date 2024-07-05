package csi.server.business.cachedb;

import csi.server.common.dto.installed_tables.ColumnParameters;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.enumerations.CsiEncoding;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.util.CharacterBuffer;
import csi.server.common.util.EncodingByteValues;
import csi.server.util.uploader.CsiFileInputStream;
import csi.server.util.uploader.CsvReader;

/**
 * Created by centrifuge on 9/24/2015.
 */
public class CsvEncodingProducer extends TranslatingProducer {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private CsvReader _rowReader;
    private ColumnParameters[] _columns;
    private int _skipCount;
    private Integer _delimiter;
    private Integer _quote;
    private Integer _escape;
    private String _nullIndicator;
    private boolean _needsSpecialProcessing = false;
    private boolean _handleBackSlash;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public CsvEncodingProducer(String taskIdIn, CsiFileInputStream streamIn, CsiEncoding encodingIn,
                               Integer delimiterIn, Integer quoteIn, Integer escapeIn,
                               String nullIndicatorIn, ColumnParameters[] columnsIn, int skipCountIn,
                               Integer rowLimitIn, boolean handleBackSlashIn, boolean doOptimizeIn) {

        super(taskIdIn, streamIn, rowLimitIn, encodingIn);

        _columns = columnsIn;
        _skipCount = skipCountIn;
        _delimiter = delimiterIn;
        _quote = quoteIn;
        _escape = escapeIn;
        _nullIndicator = nullIndicatorIn;
        _needsSpecialProcessing = (null != rowLimitIn) || (!doOptimizeIn);
        _handleBackSlash = handleBackSlashIn;

        if (doOptimizeIn) {

            for (ColumnParameters myColumn : columnsIn) {

                if ((CsiDataType.String != myColumn.getDataType()) || !myColumn.isIncluded()) {

                    _needsSpecialProcessing = true;
                    break;
                }
            }
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected Exception execute() {

        Exception myException = null;

        try {

            if (_needsSpecialProcessing || (0 < _skipCount)) {

                initialize();
            }

            if (_needsSpecialProcessing) {

                for (CharacterBuffer myBuffer = (CharacterBuffer)_rowReader.getNextRow();
                    null != myBuffer; myBuffer = (CharacterBuffer)_rowReader.getNextRow()) {

                    if (_rowLimit <= _rowCount) {

                        break;
                    }
                    myBuffer.appendElement(EncodingByteValues.cpAsciiNewLine);
                    write(myBuffer.getCharacters(), myBuffer.start(), myBuffer.length());
                    _rowCount++;
                }

            } else {

                super.execute();
            }

        } catch (Exception myCaughtException) {

            myException = myCaughtException;
        }
        return myException;
    }

    @Override
    protected void initialize() throws Exception {

        super.initialize();

        _rowReader = new CsvReader(_reader, _delimiter, _quote, _escape, _nullIndicator, _handleBackSlash);

        if (null == _rowReader) {

            throw new CentrifugeException("Bad Input stream to table loader.");

        } else if ((null == _columns) || (0 >= _columns.length)) {

            throw new CentrifugeException("No columns identified for table input.");

        } else if (_rowReader.openWorksheet(_columns)) {

            if (0 < _skipCount) {

                _rowReader.skipLines(_skipCount);
            }

        } else {

            throw new CentrifugeException("Unable to open spreadsheet.");
        }
    }
}
