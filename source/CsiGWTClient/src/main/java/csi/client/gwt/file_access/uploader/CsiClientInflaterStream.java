package csi.client.gwt.file_access.uploader;

//import com.google.gwt.user.client.Command;
//import com.google.gwt.user.client.DeferredCommand;
import java.io.IOException;

import csi.client.gwt.file_access.uploader.zip.Inflater;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.server.common.exception.InternalError;
import csi.server.common.exception.ZipException;
import csi.server.common.util.uploader.BlockLoadedCallBack;

public class CsiClientInflaterStream implements BlockLoadedCallBack {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static final CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    private BlockLoadedCallBack _callBack = null;

    protected CsiClientRandomAccess input;
    protected Inflater inflater;
    protected byte[][] inputBuffer;
    protected int _activeInput = 0;
    protected boolean isOpen;
    private byte[] _outputBuffer = null;
    private int _outputCount = 0;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                       Callbacks                                        //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public void onBlockLoaded(byte[] blockIn, int countIn) {

        if (0 > countIn) {

            _callBack.onBlockLoadError(new ZipException(_constants.csiClientInflaterStream_StreamEndsEarly()));

        } else {

            inflater.setInput(blockIn, 0, countIn);

            read();
        }
    }

    public void onBlockLoadError(Exception exceptionIn) {

        _callBack.onBlockLoadError(exceptionIn);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public CsiClientInflaterStream(CsiClientRandomAccess inputIn)
    {
        this(inputIn, new Inflater());
    }

    public CsiClientInflaterStream(CsiClientRandomAccess inputIn, Inflater inflaterIn)
    {
        this(inputIn, inflaterIn, 65536);
    }

    public CsiClientInflaterStream(CsiClientRandomAccess inputIn, Inflater inflaterIn, int sizeIn)
    {
        if (null != inputIn) {

            if (null != inflaterIn) {

                if (0 < sizeIn){

                    input = inputIn;
                    input.resetStream();
                    inflater = inflaterIn;
                    inputBuffer = new byte[][] { new byte[sizeIn], new byte[sizeIn] };
                    isOpen = true;

                } else {

                    throw new IllegalArgumentException(_constants.csiClientInflaterStream_BufferSizeIllegal(Integer.toString(sizeIn)));
                }

            } else {

                throw new NullPointerException(_constants.csiClientInflaterStream_InflatorNotIdentified());
            }

        } else {

            throw new NullPointerException(_constants.csiClientInflaterStream_StreamNotIdentified());
        }
    }

    public void close() throws IOException
    {
        if (input != null)
            input.close();
        input = null;
        isOpen = false;
    }

    public void read(final byte[] blockIn, BlockLoadedCallBack callBackIn) throws IOException
    {
        if (null != callBackIn) {

            if ((null != blockIn) && (0 < blockIn.length)) {

                if (isOpen) {

                    _callBack = callBackIn;
                    _outputCount = 0;
                    _outputBuffer = blockIn;
/*
                    DeferredCommand.add(new Command() {

                        public void execute() {

                            read(blockIn);
                        }
                    });
*/
                    read();

                } else {

                    callBackIn.onBlockLoadError(new IOException(_constants.csiClientInflaterStream_StreamClosed()));
                }

            } else {

                throw new InternalError(_constants.csiClientInflaterStream_MissingOutputBuffer());
            }

        } else {

            throw new InternalError(_constants.csiClientInflaterStream_NoCallback());
        }
    }

    public void read()
    {
        int myCount = 0;

        try {

            int myRequest = _outputBuffer.length - _outputCount;
            myCount = inflater.inflate(_outputBuffer, _outputCount, myRequest);

            if (0 < myCount) {

                _outputCount += myCount;
            }

            if (inflater.needsDictionary() | inflater.finished()) {

                _callBack.onBlockLoaded(_outputBuffer, _outputCount);

            } else if (inflater.needsInput()) {

                try {

                    fillBuffer(this);

                } catch(Exception myException) {

                    _callBack.onBlockLoadError(myException);
                }
            }
            else {

                _callBack.onBlockLoaded(_outputBuffer, _outputCount);
            }

        } catch (Exception myException) {

            _callBack.onBlockLoadError(new ZipException(myException.getMessage()));
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected void fillBuffer(BlockLoadedCallBack callbackIn) throws IOException
    {
        if (input == null)
            throw new ZipException(_constants.csiClientInflaterStream_InflaterInputStreamClosed());

        _activeInput = 1 - _activeInput;
        input.read(inputBuffer[_activeInput], callbackIn);
    }
}
