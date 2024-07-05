package csi.server.business.cachedb;

import java.io.OutputStreamWriter;

import csi.server.common.enumerations.CsiEncoding;
import csi.server.util.uploader.CsiFileInputStream;
import csi.server.util.uploader.CsiInputStreamReader;

/**
 * Created by centrifuge on 9/24/2015.
 */
public class TranslatingProducer extends AbstractInlineProducer<CsiFileInputStream> {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static final int _charBufferSize = 16 * 1024;

    private CsiEncoding _encoding;

    protected CsiInputStreamReader _reader = null;
    protected OutputStreamWriter _writer = null;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public TranslatingProducer(String taskIdIn, CsiFileInputStream streamIn,
                               Integer rowLimitIn, CsiEncoding encodingIn) {

        super(taskIdIn, streamIn, rowLimitIn);

        _encoding = encodingIn;
   }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected Exception execute() {

        Exception myException = null;

        try {

            int myCount = 0;
            char[] myBuffer = new char[_charBufferSize];

            initialize();

            while (0 <= myCount) {

                myCount = read(myBuffer, 0, _charBufferSize);

                if (0 <= myCount) {

                    write(myBuffer, 0, myCount);
                }
            }

        } catch (Exception myCaughtException) {

            myException = myCaughtException;
        }
        return myException;
    }

    protected void write(char[] bufferIn, int offsetIn, int lengthIn) throws Exception {

        _writer.write(bufferIn, offsetIn, lengthIn);
        _writer.flush();
        recordProgress();
    }

    protected int read(char[] bufferIn, int offsetIn, int lengthIn) throws Exception {

        return _reader.read(bufferIn, offsetIn, lengthIn);
    }

    protected void initialize() throws Exception {

        if (null != _encoding) {

            _reader = new CsiInputStreamReader(_receivingStream, _encoding);
            _writer = new OutputStreamWriter(_intermediateStream, CsiEncoding.UTF_8.getJavaName());
        }
    }
}
