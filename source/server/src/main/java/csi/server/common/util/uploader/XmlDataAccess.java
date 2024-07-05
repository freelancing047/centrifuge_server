package csi.server.common.util.uploader;

import java.io.IOException;

/**
 * Created by centrifuge on 10/8/2015.
 */
public class XmlDataAccess implements BlockLoadedCallBack {


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Embedded Classes                                    //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static final int _defaultBufferSize = 1024 * 1024;

    private DataBlock _block = null;
    private long _pointer = 0L;
    private long _valueBase = -1L;
    private long _highWater = 0L;
    private long[] _rewindBase = new long[] {-1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L};


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    // Server-side instantiation -- synchronous access to the file stream
    public XmlDataAccess(CsiSimpleInputStream fileIn) throws IOException {

        this(fileIn, _defaultBufferSize);
    }

    // Server-side instantiation -- synchronous access to the file stream
    public XmlDataAccess(CsiSimpleInputStream fileIn, int blockSizeIn) throws IOException {

        _block = new DataBlock(fileIn, blockSizeIn);
    }

    // Client-side instantiation -- all required data must be contained within the initial block
    public XmlDataAccess(byte[] blockIn) {

        this(blockIn, blockIn.length);
    }

    // Client-side instantiation -- all required data must be contained within the initial block
    public XmlDataAccess(byte[] blockIn, int countIn) {

        _block = new DataBlock(blockIn, countIn);
    }

    public void onBlockLoaded(byte[] blockIn, int countIn) {

    }

    public void onBlockLoadError(Exception ExceptionIn) {

    }

    public void skipLines(long lineCountIn) {}

    public void restart() {

        resetPointer(0L);
    }

    public int getNextByte() {

        int myByte = _block.getByte(_pointer++);

        if ('<' == myByte) {

            if ('?'== _block.getByte(_pointer)) {

                myByte = removeInfoTag();

            } else if (('!' == _block.getByte(_pointer))
                    && ('-' == _block.getByte(_pointer + 1))
                    && ('-' == _block.getByte(_pointer + 2))) {

                myByte = removeCommentTag();
            }
        }
        return myByte;
    }

    public int peekByte(int offsetIn) {

        int myByte = -1;
        long myOffset = (_pointer + offsetIn) - 1;

        if (0L < myOffset) {

            myByte = _block.getByte(myOffset);
        }

        return myByte;
    }

    public void stepBack() {

        decrementPointer(-1L);
    }

   public void skipBlanks() {
      while (' ' == getNextByte()) {
      }
      decrementPointer(-1L);
   }

    public Integer markRewind() {

        Integer myHandle = getRewindHandle();

        if (null != myHandle){

            _rewindBase[myHandle] = _pointer;
        }
        return myHandle;
    }

    public Integer markRewind(int offsetIn) {

        Integer myHandle = getRewindHandle();

        if (null != myHandle){

            _rewindBase[myHandle] = _pointer + offsetIn;
        }
        return myHandle;
    }

    public Integer replaceRewind(Integer handleIn) {

        Integer myHandle = (null != handleIn) ? handleIn : getRewindHandle();

        if (null != myHandle){

            _rewindBase[myHandle] = _pointer;
        }
        return myHandle;
    }

    public Integer replaceRewind(Integer handleIn, int offsetIn) {

        Integer myHandle = (null != handleIn) ? handleIn : getRewindHandle();

        if (null != myHandle){

            _rewindBase[myHandle] = _pointer + offsetIn;
        }
        return myHandle;
    }

    public Integer cancelRewind(Integer handleIn) {

        if ((null != handleIn) && (0 <= handleIn) && (_rewindBase.length > handleIn)) {

            _rewindBase[handleIn] = -1L;
        }
        return null;
    }

    public void rewind(Integer handleIn) {

        if ((null != handleIn) && (0 <= handleIn) && (_rewindBase.length > handleIn)) {

            if (0L <= _rewindBase[handleIn]) {

                resetPointer(_rewindBase[handleIn]);
            }
        }
    }

    public void markValue() {

        _valueBase = _pointer;
    }

    public void markValue(int offsetIn) {

        _valueBase = _pointer + offsetIn;
    }

    public byte[] getValue() {

        if (0L <= _valueBase) {

            return getValue((int)(_pointer - _valueBase) - 1);
        }
        cancelValue();
        return null;
    }

    public byte[] getValue(int byteCountIn) {

        if (0L <= _valueBase) {

            long myPointer = resetPointer(_valueBase);

            if (myPointer > _valueBase) {

                byte[] myByteArray = new byte[byteCountIn];

                for (int i = 0; byteCountIn > i; i++) {

                    int myByte = getNextByte();

                    myByteArray[i] = (-1 != myByte) ? (byte)myByte : (byte)0;
                }

                cancelValue();
                resetPointer(myPointer);

                return myByteArray;
            }
        }
        cancelValue();
        return null;
    }

    public byte[] cancelValue() {

        _valueBase = -1L;

        return null;
    }

    public int read() throws IOException {

        throw new IOException("The \"read\" operation is not supported!");
    }

    public int read(byte[] bufferIn, int offsetIn, int lengthIn) throws IOException {

        throw new IOException("The \"read\" operation is not supported!");
    }

    public long getByteLocation() {

        return _highWater;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Private Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private int removeInfoTag() {

        int myByte = -1;

        incrementPointer(1L);

        for (myByte = getNextByte(); -1 != myByte; myByte = getNextByte()) {

            if (('>' == myByte) && ('?' == peekByte(-1))) {

                myByte = getNextByte();
                break;
            }
        }
        return myByte;
    }

    private int removeCommentTag() {

        int myByte = -1;

        incrementPointer(3L);

        for (myByte = getNextByte(); -1 != myByte; myByte = getNextByte()) {

            if (('>' == myByte) && ('-' == peekByte(-1)) && ('-' == peekByte(-2))) {

                myByte = getNextByte();
                break;
            }
        }
        return myByte;
    }

    private void incrementPointer(long pointerIn) {

        _pointer += pointerIn;
    }

    private void decrementPointer(long pointerIn) {

        _pointer += pointerIn;
    }

    private long resetPointer(long pointerIn) {

        long myPointer = _pointer;

        _pointer = pointerIn;

        return myPointer;
    }

    private Integer getRewindHandle() {

        Integer myHandle;

        for (myHandle = 0; _rewindBase.length > myHandle; myHandle++) {

            if (-1L == _rewindBase[myHandle]) {

                break;
            }
        }
        return (_rewindBase.length > myHandle) ? myHandle : null;
    }
}
