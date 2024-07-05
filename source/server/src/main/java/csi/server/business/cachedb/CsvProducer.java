package csi.server.business.cachedb;

import csi.server.common.dto.installed_tables.ColumnParameters;
import csi.server.common.enumerations.CsiDataType;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.util.ByteBuffer;
import csi.server.common.util.EncodingByteValues;
import csi.server.util.uploader.CsiFileInputStream;
import csi.server.util.uploader.CsvReader;

/**
 * Created by centrifuge on 9/24/2015.
 */
public class CsvProducer extends AbstractInlineProducer<CsiFileInputStream> {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private CsiFileInputStream _stream;
    private CsvReader _reader;
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

    public CsvProducer(String taskIdIn, CsiFileInputStream streamIn, Integer delimiterIn, Integer quoteIn,
                       Integer escapeIn, String nullIndicatorIn, ColumnParameters[] columnsIn, int skipCountIn,
                       Integer rowLimitIn, boolean handleBackSlashIn, boolean doOptimizeIn) {

        super(taskIdIn, streamIn, rowLimitIn);

        _stream = streamIn;
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

                for (ByteBuffer myBuffer = (ByteBuffer)_reader.getNextRow();
                        null != myBuffer; myBuffer = (ByteBuffer)_reader.getNextRow()) {

                    if (_rowLimit <= _rowCount) {

                        break;
                    }
                    myBuffer.appendElement(EncodingByteValues.asciiNewLine);
                    write(myBuffer.getBytes(), myBuffer.start(), myBuffer.length());
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


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private void initialize() throws Exception {

        _reader = new CsvReader(_stream, _delimiter, _quote, _escape, _nullIndicator, _handleBackSlash);

        if (null == _reader) {

            throw new CentrifugeException("Bad Input stream to table loader.");

        } else if ((null == _columns) || (0 >= _columns.length)) {

            throw new CentrifugeException("No columns identified for table input.");

        } else if (_reader.openWorksheet(_columns)) {

            if (0 < _skipCount) {

                _reader.skipLines(_skipCount);
            }

        } else {

            throw new CentrifugeException("Unable to open spreadsheet.");
        }
    }
}
