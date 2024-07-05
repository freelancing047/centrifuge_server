package csi.server.business.cachedb;

import java.io.FileOutputStream;

import csi.server.common.dto.installed_tables.ColumnParameters;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.util.ByteBuffer;
import csi.server.common.util.EncodingByteValues;
import csi.server.common.util.uploader.zip.CsiZipEntry;
import csi.server.util.uploader.XlsxReader;

/**
 * Created by centrifuge on 11/18/2015.
 */
public class NewExcelProducer extends AbstractInlineProducer<XlsxReader> {

   ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private XlsxReader _reader;
    private ColumnParameters[] _columns;
    private CsiZipEntry _entry;
    private int _skipCount;
    private FileOutputStream _debugFile;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public NewExcelProducer(String taskIdIn, XlsxReader streamIn, CsiZipEntry entryIn, Integer rowLimitIn,
                            ColumnParameters[] columnsIn, int skipCountIn, FileOutputStream debugFileIn) {

        super(taskIdIn, streamIn, rowLimitIn);

        _reader = streamIn;
        _columns = columnsIn;
        _entry = entryIn;
        _skipCount = skipCountIn;
        _debugFile = debugFileIn;
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

            initialize();

            if (null != _debugFile) {

                for (ByteBuffer myBuffer = _reader.getNextRow(); null != myBuffer; myBuffer = _reader.getNextRow()) {

                    if (_rowLimit <= _rowCount) {

                        break;
                    }
                    myBuffer.append(EncodingByteValues.asciiNewLine);
                    _debugFile.write(myBuffer.getBytes(), myBuffer.start(), myBuffer.length());
                    write(myBuffer.getBytes(), myBuffer.start(), myBuffer.length());
                    _rowCount++;
                }
                _debugFile.close();

            } else {

                for (ByteBuffer myBuffer = _reader.getNextRow(); null != myBuffer; myBuffer = _reader.getNextRow()) {

                    myBuffer.append(EncodingByteValues.asciiNewLine);
                    write(myBuffer.getBytes(), myBuffer.start(), myBuffer.length());
                    _rowCount++;
                }
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

        if (null == _reader) {

            throw new CentrifugeException("Bad Input stream to table loader.");

        } else if (null == _entry) {

            throw new CentrifugeException("No data parameters supplied for table input.");


        } else if ((null == _columns) || (0 >= _columns.length)) {

            throw new CentrifugeException("No columns identified for table input.");

        } else if (_reader.openWorksheet(_entry, _columns)) {

            if (0 < _skipCount) {

                _reader.skipLines(_skipCount);
            }

        } else {

            throw new CentrifugeException("Unable to open spreadsheet.");
        }
    }
}
