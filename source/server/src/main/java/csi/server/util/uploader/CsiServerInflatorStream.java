package csi.server.util.uploader;

import java.io.IOException;
import java.util.zip.Inflater;

/**
 * Created by centrifuge on 11/16/2015.
 */
public class CsiServerInflatorStream {
   private static final int DEFAULT_BUFFER_SIZE = 1024 * 1024;

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected CsiFileInputStream input;
    protected Inflater inflater;
    protected byte[][] inputBuffer;
    protected int _activeInput = 0;
    protected boolean isOpen;
    private byte[] _outputBuffer = null;
    private int _outputCount = 0;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public CsiServerInflatorStream(CsiFileInputStream inputIn)
    {
        this(inputIn, new Inflater());
    }

    public CsiServerInflatorStream(CsiFileInputStream inputIn, Inflater inflaterIn)
    {
        this(inputIn, inflaterIn, DEFAULT_BUFFER_SIZE);
    }

    public CsiServerInflatorStream(CsiFileInputStream inputIn, Inflater inflaterIn, int sizeIn)
    {
        if (null != inputIn) {

            if (null != inflaterIn) {

                if (0 < sizeIn){

                    input = inputIn;
                    inflater = inflaterIn;
                    inputBuffer = new byte[][] { new byte[sizeIn], new byte[sizeIn] };
                    isOpen = true;

                } else {

                    throw new IllegalArgumentException("Buffer size of " + Integer.toString(sizeIn) + " bytes is illegal!");
                }

            } else {

                throw new NullPointerException("Inflator module not identified!");
            }

        } else {

            throw new NullPointerException("Input stream not identified!");
        }
    }

    public void close() throws IOException
    {
        if (input != null)
            input.close();
        input = null;
        isOpen = false;
    }

    public int read(final byte[] blockIn) throws IOException
    {
        int myCount = -1;

        try {

            if (isOpen && (null != blockIn) && (0 < blockIn.length)) {

                _outputCount = 0;
                _outputBuffer = blockIn;

                readBlock();
                myCount = _outputCount;
            }

        } catch (Exception myException) {

        }
        return myCount;
    }

    private void readBlock() throws Exception
    {
        int myRequest = _outputBuffer.length - _outputCount;
        int myCount = inflater.inflate(_outputBuffer, _outputCount, myRequest);

        if (0 < myCount) {

            _outputCount += myCount;
        }

        if (inflater.needsDictionary() | inflater.finished()) {

            return;

        } else if (inflater.needsInput()) {

            fillBuffer();
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                   Protected Methods                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    protected void fillBuffer() throws Exception
    {
        _activeInput = 1 - _activeInput;

        int myCount = input.read(inputBuffer[_activeInput]);

        if (0 < myCount) {

            inflater.setInput(inputBuffer[_activeInput], 0, myCount);

            readBlock();
        }
    }
}
