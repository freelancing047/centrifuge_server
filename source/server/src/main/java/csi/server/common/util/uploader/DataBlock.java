package csi.server.common.util.uploader;

/**
 * Created by centrifuge on 11/18/2015.
 */

public class DataBlock {

    private static final int _defaultBufferSize = 1024 * 1024;

    private CsiSimpleInputStream _input = null;
    long _fileSize = 0L;
    long[] _blockBasePair = {0L, 0L};
    long _blockBase = 0L;
    byte[][] _dataBlockPair = null;
    byte[] _dataBlock = null;
    int _activeBlock = 0;
    int _blockSize = 0;
    int[] _byteCountPair = {0, 0};
    int _byteCount = 0;

    public DataBlock(CsiSimpleInputStream streamIn) {

        this(streamIn, _defaultBufferSize);
    }

    public DataBlock(CsiSimpleInputStream streamIn, int blockSizeIn) {

        _input = streamIn;
        _blockSize = blockSizeIn;

        _dataBlockPair = new byte[][] {new byte[_blockSize], new byte[_blockSize]};
    }

    public DataBlock(byte[] dataBlockIn) {

        this(dataBlockIn, dataBlockIn.length);
    }

    public DataBlock(byte[] dataBlockIn, int byteCountIn) {

        _dataBlock = dataBlockIn;
        _byteCount = byteCountIn;
        _blockBase = 0L;
    }

    public int getByte(final long pointerIn) {

        int myByte = -1;
        long myOffset = pointerIn - _blockBase;

        if ((null != _dataBlock) && ((0L <= myOffset) && ((long) _byteCount > myOffset))) {

            myByte = ((int) _dataBlock[(int) myOffset]) & 0xff;

        } else if (0 < _blockSize) {

            myOffset = pointerIn - _blockBasePair[1 - _activeBlock];

            if ((0L <= myOffset) && ((long) _byteCountPair[1 - _activeBlock] > myOffset)) {

                _activeBlock = 1 - _activeBlock;

                _dataBlock = _dataBlockPair[_activeBlock];
                _blockBase = _blockBasePair[_activeBlock];
                _byteCount = _byteCountPair[_activeBlock];

                myOffset = pointerIn - _blockBase;
                myByte = ((int) _dataBlock[(int) myOffset]) & 0xff;

            } else if ((long) _byteCount <= myOffset) {

                try {

                    for (_activeBlock = 1 - _activeBlock, _blockBasePair[_activeBlock]
                            = _blockBasePair[1 - _activeBlock] + _byteCountPair[1 - _activeBlock],
                                 _byteCountPair[_activeBlock] = _input.read(_dataBlockPair[_activeBlock]);
                         (0 < _byteCountPair[_activeBlock])
                                 && ((_blockBasePair[_activeBlock] + _byteCountPair[_activeBlock]) <= pointerIn);
                         _activeBlock = 1 - _activeBlock, _blockBasePair[_activeBlock]
                                 = _blockBasePair[1 - _activeBlock] + _byteCountPair[1 - _activeBlock],
                                 _byteCountPair[_activeBlock] = _input.read(_dataBlockPair[_activeBlock]))

                        ; // Keep looping until satisfied or EOF reached

                } catch (Exception myException) {

                    _byteCountPair[_activeBlock] = 0;
                }

                _byteCountPair[_activeBlock] = Math.max(0, _byteCountPair[_activeBlock]);

                _dataBlock = _dataBlockPair[_activeBlock];
                _blockBase = _blockBasePair[_activeBlock];
                _byteCount = _byteCountPair[_activeBlock];

                myOffset = pointerIn - _blockBase;

                if ((null != _dataBlock) && ((0L <= myOffset) && ((long) _byteCount > myOffset))) {

                    myByte = ((int) _dataBlock[(int) myOffset]) & 0xff;
                }
            }
        }

        return myByte;
    }

    public CsiSimpleInputStream getStream() {

        return _input;
    }
}
