package csi.server.common.util;

/**
 * Created by centrifuge on 7/1/2015.
 */
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;

/*
 * Inteface for supporting reading the next byte from the input source.
 */
interface ByteInput {

    public int getByte();
}

/*
 * Inteface for supporting writing the next byte to the output target.
 */
interface ByteOutput {

    public void putByte(int valueIn);
}

/*
 * Inteface for supporting reading characters from a stream.
 */
interface CharacterInput {

    public void stepOverBOM();
    public Integer inputCharacter();
    public Integer getLastByteLocation();
    public int getBufferSize();
    public Long bytesRead();
    public void setTesting(boolean isTestingIn);
}

/*
 * Inteface for supporting writing characters to a stream.
 */
interface CharacterOutput {

    public void generateBOM();
    public boolean outputCharacter(int valueIn);
}

class BufferObject<T> {

    T _buffer;
    int _trueCount;

    public BufferObject(T bufferIn, int trueCountIn) {

        _buffer = bufferIn;
        _trueCount = trueCountIn;
    }

    public T getBuffer() {

        return _buffer;
    }

    public int getTrueCount() {

        return _trueCount;
    }
}

public class DocumentEncoder {

    /*
     * Enumeration of encoding standards supported
     */
    public enum EncodingMethod {

        utf8,
        utf16le,
        utf16be,
        extunix,
        ansi
    }

    public interface DataSource {

        int read(byte[] bufferIn, int startIn, int sizeIn);
    }

    public interface DataTarget {

        int putByte(byte byteOut);
    }

    private CharacterInput _input = null;
    private CharacterOutput _output = null;
    private String _encodingStringIn = null;
    private String _encodingStringOut = null;
    private EncodingMethod _encodingIn = null;
    private EncodingMethod _encodingOut = null;
    private DataSource _streamIn = null;
    private DataTarget _streamOut = null;
    private EncodingSearch _encodingTester = null;

    //
    // ENTRY POINT FROM SYSTEM
    //
    //		arg[0] : [required] output encoding method (utf8, unicode, unicodebe, or ansi)
    //		arg[1] : [optional] input encoding method (utf8, unicode, unicodebe, or ansi)
    //
    /*
    public static void main(String[] args) {

        if (0 < args.length) {

            if (1 < args.length) {

                new DocumentEncoder(System.in, System.err, args[0], args[1]).run();

            } else {

                new DocumentEncoder(System.in, System.err, args[0]).run();
            }

        } else {

        }
    }
*/
    public static EncodingMethod identifyEncoding(DataSource inputStreamIn,
                                                  List<EncodingMethod> negativeListIn, boolean ignoreNullsIn) {

        return (new EncodingSearch(inputStreamIn)).determineEncoding(negativeListIn, ignoreNullsIn);
    }

    public static EncodingMethod identifyEncoding(DataSource inputStreamIn, boolean ignoreNullsIn) {

        return (new EncodingSearch(inputStreamIn)).determineEncoding(null, ignoreNullsIn);
    }

    public static List<Integer> decodeDataBlock(DataSource inputStreamIn, EncodingMethod inputEncodingIn, int limitIn) throws Exception {

        return extractData(CharacterProcessing.open(inputStreamIn, inputEncodingIn), limitIn);
    }

    public static Long countBytes(DataSource inputStreamIn, EncodingMethod inputEncodingIn, int countIn) throws Exception {

        return countBytes(CharacterProcessing.open(inputStreamIn, inputEncodingIn), countIn);
    }

    //
    // CONSTRUCTOR -- one of four
    //
    public DocumentEncoder(DocumentEncoder.DataSource inputIn) {

        this(inputIn, null, null, null);
    }

    //
    // CONSTRUCTOR -- two of four
    //
    /*
    public DocumentEncoder(DocumentEncoder.DataSource inputIn, DataTarget outputIn, Map<String, String> encodingMapIn) {

        this(inputIn, outputIn, outputEncodingIn, null);
    }
*/
    //
    // CONSTRUCTOR -- three of four
    //
    public DocumentEncoder(DocumentEncoder.DataSource inputIn, DataTarget outputIn, String outputEncodingIn) {

        this(inputIn, outputIn, outputEncodingIn, null);
    }

    //
    // CONSTRUCTOR -- four of four
    //
    public DocumentEncoder(DocumentEncoder.DataSource inputIn, DataTarget outputIn, String outputEncodingIn, String inputEncodingIn) {

        try {

            // Record the values for the input and output streams
            _streamIn = inputIn;
            _streamOut = outputIn;

            // Identify the input encoding method -- 4 are supported
            // UTF-8, UTF-16 (Little Endian), UTF-16 (Big Endian), and Windows-1252.
            _encodingStringOut = outputEncodingIn;
            if (null != _encodingStringOut) {

                _encodingOut = EncodingMethod.valueOf(_encodingStringOut.toLowerCase());
            }

            // Argument #2 -- optional -- default = unknown
            // Identify the input encoding method -- 4 are supported
            // UTF-8, UTF-16 (Little Endian), UTF-16 (Big Endian), and Windows-1252.
            _encodingStringIn = inputEncodingIn;
            if (null != _encodingStringIn) {

                _encodingIn = EncodingMethod.valueOf(_encodingStringIn.toLowerCase());
            }

        } catch (Exception myException) {

            // !! OOPS !! Something broke !!

            // Display the error message.
            System.err.println("Caught exception initializing the system: "
                    + myException.toString());
        }
    }

    //
    // This is a static entry point, used to create the proper character processing
    // class for the document based upon the document's character encoding.
    //
    String determineEncoding() {

        // Use the EncodingSearch class to determine
        // the character encoding since it is the most involved
        // encoding method and cannot be recognized without that knowledge.
        _encodingTester = new EncodingSearch(_streamIn);
        _encodingIn = _encodingTester.determineEncoding(null, false);

        if (null != _encodingIn) {

            _encodingStringIn = _encodingIn.name();

        } else {

            _encodingStringIn = null;
        }

        return _encodingStringIn;
    }
/*

    public int[] decodeBuffer() {

        int[] myBuffer = new int[_bufferSize];
        _testing = true;

        rewindInput();
        stepOverBOM();
//        for (Integer myCodePoint = _input.inputCharacter(); null != myCodePoint; myCodePoint = _input.inputCharacter()) {

        for (int i = 0; _bufferSize > i; i++) {

            Integer myValue = inputCharacter();
        }
    }

    public int[] decodeBuffer(String encodingIn) {

        int[] myBuffer = null;
        CharacterInput myInput = null;

        if (null != encodingIn) {

            _encodingIn = EncodingMethod.valueOf(encodingIn.toLowerCase());

            myInput = _encodingTester.spawnDecoder(_encodingIn);
        }

        if (null != myInput) {

            int myBufferSize = myInput.getBufferSize();
            myBuffer = new int[myBufferSize];

            myInput.setTesting(true);

            for (int i = 0; myBufferSize > i; i++) {

                Integer myCodePoint = myInput.inputCharacter();

                if (null != myCodePoint) {

                    myBuffer[i] = myCodePoint;

                } else {

                    myBuffer[i] = CharacterProcessing.noCharacterByte;
                    break;
                }
            }
        }
        return myBuffer;
    }

 */

    //
    // This routine is the high level control for parsing a single document.
    //
    public void run() {

        try {

            // Identify and initialize the character processing
            // class based unpon the document encoding.
            initializeCharacterProcessing();

            if (null != _output) {

                if (null != _input) {

                    // Scan the document and store the individual words
                    // and phrases along with the occurrence count.
                    reformatData();

                } else {

                    if (null != _encodingStringIn) {

                        // Display the error message.
                        System.err.println("Could not recognize the encoding method \"" + _encodingStringIn + "\" for input.");

                    }

                    // Display the error message.
                    System.err.println("Unable to determine the encoding method from the input data.");
                }

            } else {

                if (null != _encodingStringOut) {

                    // Display the error message.
                    System.err.println("Could not recognize the encoding method \"" + _encodingStringOut + "\" for output.");

                } else {

                    // Display the error message.
                    System.err.println("No encoding method assigned for the output.");
                }
                System.err.println(".");
            }

        } catch (Exception myException) {

            // !! OOPS !! Something broke !!

            // If we have established the desired character processing class,
            // use it to determine which byte within the document we broke on.
            if (null != _input) {

                System.err.println("Caught exception at byte "
                        + _input.getLastByteLocation().toString() + ":");
            }
            // Display the error message.
            System.err.println(myException.toString());
        }
    }

    //
    // This routine is responsible for determining the encoding method
    // for the document and initiali8zing the character processing object
    // that will be used to extract the characters from the input byte stream.
    //
    private void initializeCharacterProcessing() {

        // Select the output processing class based upon
        // the encoding value passed in on the command line.
        _output = CharacterProcessing.open(_streamOut, _encodingOut);

        // Select the input processing class based upon the encoding
        // value passed in on the command line, or the encoding
        //value determined by a partial scan of the document.
        _input = CharacterProcessing.open(_streamIn, _encodingIn);
    }

    //
    // This routine is responsible for the extraction and processing of words
    // found in the document as well as the recognition and processing of phrases.
    //
    private void reformatData() throws Exception {

        // Process each character as it is found
        for (Integer myCodePoint = _input.inputCharacter(); null != myCodePoint; myCodePoint = _input.inputCharacter()) {

            if (!_output.outputCharacter(myCodePoint)) {

                throw new Exception("Failed encoding Codepoint " + myCodePoint.toString() + " using " + _encodingOut.name() + " encoding.");
            }
        }
    }

    //
    // This routine is responsible for the extraction and processing of words
    // found in the document as well as the recognition and processing of phrases.
    //
    private static List<Integer> extractData(CharacterInput inputIn, int limitIn) throws Exception {

        List<Integer> myData = new ArrayList<Integer>();

        // Process each character as it is found
        for (Integer myCodePoint = inputIn.inputCharacter(); null != myCodePoint; myCodePoint = inputIn.inputCharacter()) {

            myData.add(myCodePoint);

            if (myData.size() >= limitIn) {

                break;
            }
        }
        return myData.isEmpty() ? null : myData;
    }

    //
    // This routine is responsible for counting the number of bytes corresponding
    // to the desired number of characters at the beginning of a block.
    //
    private static Long countBytes(CharacterInput inputIn, int countIn) throws Exception {

        // Skip over each character as it is found
        for (int i = 0; countIn > i; i++) {

            Integer myCodePoint = inputIn.inputCharacter();

            if (null == myCodePoint) {

                break;
            }

        }
        return inputIn.bytesRead();
    }
}

/*
 * This is the base class for all of the character processing objects.
 * Currently this code supports only four:
 *
 * 	UTF-8 character set processing,
 *  ANSI (as Windows-1252) character set processing
 *  Little Endian UTF-16 (Windows Unicode) character set processing
 *  Big Endian UTF-16 character set processing
 */
abstract class CharacterProcessing implements ByteInput, ByteOutput  {

    static final int noCharacterByte = -1;
    static final int eof = -1;

    protected static final int _bufferSize = 4096;

    protected static final int _byteMask = 0xFF;
    protected static final int _asciiMask = 0x7F;

    protected static final int _bomUtf81 = 0xEF;
    protected static final int _bomUtf82 = 0xBB;
    protected static final int _bomUtf83 = 0xBF;
    protected static final int _nullByte = 0x00;

    protected static final int _bomUnicode1A = 0xC3;
    protected static final int _bomUnicode1B = 0xBE;
    protected static final int _bomUnicode2A = 0xC3;
    protected static final int _bomUnicode2B = 0xBF;

    protected static final int _bomUnicode1 = 0xFE;
    protected static final int _bomUnicode2 = 0xFF;
    protected static final int _bomUnicode = 0xFEFF;

    protected static final int _range1top = 0xD7FF;
    protected static final int _range2base = 0xE000;
    protected static final int _range2top = 0xFFFF;

    protected static final int _characterBits = 0xFFFF;
    protected static final int _supplementaryBase = 0x010000;
    protected static final int _supplementaryTop = 0x10FFFF;
    protected static final int _surrogateBase1 = 0xD800;
    protected static final int _surrogateBase2 = 0xDC00;
    protected static final int _surrogateTop1 = 0xDBFF;
    protected static final int _surrogateTop2 = 0xDFFF;

    protected static final int _tenBitMask = 0x03FF;
    protected static final int _tenBitShift = 10;

    protected static final int _escapeChar = 0x1B;
    protected static final int _shiftOut = 0x0E;
    protected static final int _shiftIn = 0x0F;
    protected static final int _illegalExtRangeLo = 0x80;
    protected static final int _illegalExtRangeHi = 0x8D;

    protected DocumentEncoder.EncodingMethod _inputEncoding = null;
    protected boolean _atEof = false;
    protected boolean _testing = false;

    private Queue<BufferObject<byte[]>> _bufferQueue = new LinkedList<BufferObject<byte[]>>();
    private DocumentEncoder.DataSource _input = null;
    private DocumentEncoder.DataTarget _output = null;
    private byte[] _byteBuffer = null;
    private int _bufferTop = 0;
    private int _bufferPointer = 0;
    private int _byteCount = 0;
    private long _bytesRead = 0L;
    private Short _surrogate = null;
    private int _lastByte = noCharacterByte;
    private int _currentByte = noCharacterByte;
    private int _nextByte = noCharacterByte;
    private int _queueLimit = 0;
    private int _queueIndex = 0;
    private boolean _retryByte = false;

    //
    // This is a static entry point, used to create the proper character processing
    // class for the document based upon the document's character encoding.
    //
    static CharacterInput open(DocumentEncoder.DataSource streamIn, DocumentEncoder.EncodingMethod encodingIn) {

        CharacterInput myProcessing = null;

        // If user has identified the encoding method
        // use it without further verification.
        if (null != encodingIn) {

            switch (encodingIn) {

                case utf8:

                    // Process document assuming UTF-8 encoding
                    myProcessing = new Utf8InputProcessing(streamIn);
                    break;

                case utf16le:

                    // Process document assuming UTF-16 Little Endian encoding
                    myProcessing = new Utf16InputProcessing(streamIn, new LittleEndianProcessing());
                    break;

                case utf16be:

                    // Process document assuming UTF-16 Big Endian encoding
                    myProcessing = new Utf16InputProcessing(streamIn, new BigEndianProcessing());
                    break;

                case ansi:

                    // Process document assuming Windows-1252 encoding
                    myProcessing = new AnsiInputProcessing(streamIn);
                    break;

                case extunix:

                    // Process document assuming Windows-1252 encoding
                    myProcessing = new ExtUnixInputProcessing(streamIn);
                    break;
            }
        }

        // If the encoding has not been identified, attempt to identify it
        if (null == myProcessing) {

            // Use the EncodingSearch class to determine
            // the character encoding since it is the most involved
            // encoding method and cannot be recognized without that knowledge.
            myProcessing = new EncodingSearch(streamIn).testEncodings();
        }

        return myProcessing;
    }

    //
    // This is a static entry point, used to create the proper character processing
    // class for the document based upon the document's character encoding.
    //
    static CharacterOutput open(DocumentEncoder.DataTarget streamIn, DocumentEncoder.EncodingMethod encodingIn) {

        CharacterOutput myProcessing = null;

        // If user has identified the encoding method
        // use it without further verification.
        if (null != encodingIn) {

            switch (encodingIn) {

                case utf8:

                    // Generate document using UTF-8 encoding
                    myProcessing = new Utf8OutputProcessing(streamIn);
                    break;

                case utf16le:

                    // Generate document using UTF-16 Little Endian encoding
                    myProcessing = new Utf16OutputProcessing(streamIn, new LittleEndianProcessing());
                    break;

                case utf16be:

                    // Generate document using UTF-16 Big Endian encoding
                    myProcessing = new Utf16OutputProcessing(streamIn, new BigEndianProcessing());
                    break;

                case ansi:

                    // Generate document using Windows-1252 encoding
                    myProcessing = new AnsiOutputProcessing(streamIn);
                    break;
            }
        }

        return myProcessing;
    }

    //
    // CONSTRUCTOR -- one of two
    //
    // This is the first of the two constructors for the CharacterProcessing base class.
    // It is used to support creation of a encoding based character processing class
    // when the user has identified the the character encoding for the document.
    //
    CharacterProcessing(DocumentEncoder.DataSource DataSourceIn, DocumentEncoder.DataTarget DataTargetIn) {

        _input = DataSourceIn;
        _output = DataTargetIn;

        // Step over the Byte Order Marker if one exists.
        // This action will be carried out by the subclass.
        stepOverBOM();
    }

    //
    // CONSTRUCTOR -- two of two
    //
    // This is the second of the two constructors for the CharacterProcessing base class.
    // It is used to support creation of a encoding based character processing class
    // when the user has not identified the the character encoding for the document,
    // but the encoding has been determined by scanning a portion of the document.
    //
    CharacterProcessing(DocumentEncoder.DataSource DataSourceIn, DocumentEncoder.DataTarget DataTargetIn, Queue<BufferObject<byte[]>> bufferQueueIn) {

        // Retain the buffer used during the character encoding pre-scan.
        _bufferQueue = bufferQueueIn;

        _input = DataSourceIn;
        _output = DataTargetIn;

        // Step over the Byte Order Marker if one exists.
        // This action will be carried out by the subclass.
        stepOverBOM();
    }

    public int getBufferSize() {

        return _bufferSize;
    }

    public void setTesting(boolean isTestingIn) {

        _testing = isTestingIn;
    }

    //
    // Method for stepping over the Byte Order Mark
    // found at the beginning of the document.
    //
    public void stepOverBOM() {

        // ANSI encoded documents such as ASCII
        // or Windows-1252 do not have a Byte Ordwr Mark.
    }

    //
    // Get the next byte from the stream.
    // Used by the processing algorithms for the supported
    // character encoding methods.
    //
    public int getByte() {

        if (!_retryByte) {

            _lastByte = _currentByte;
            _currentByte = _nextByte;
            _nextByte = noCharacterByte;

            if (noCharacterByte == _currentByte) {

                // If at the end of the buffer, get more data from the stream.
                if (_bufferTop <= _bufferPointer) {

                    refreshBuffer();
                }

                // If there is data within the buffer, get
                // the next byte and advance the buffer pointer.
                if (_bufferTop > _bufferPointer) {

                    _byteCount++;
                    _bytesRead++;

                    // Ensure nothing roles negative accidentally.
                    _currentByte = _byteMask & _byteBuffer[_bufferPointer++];
                }
            }

        } else {

            _retryByte = false;
        }

        return _currentByte;
    }

    public void retryByte() {

        _retryByte = true;
    }

    public void putByte(int valueIn) {

        _output.putByte((byte)(0xFF & valueIn));
    }

    //
    // Return the location of the last byte processed within the stream.
    // This is helpful when reporting an error to the user.
    //
   public Integer getLastByteLocation() {
      return Integer.valueOf(_byteCount);
   }

    //
    // Return the location of the last byte processed within the stream.
    // This is helpful when reporting an error to the user.
    //
   public Long bytesRead() {
      return Long.valueOf(_bytesRead);
   }

    //
    // Returns true if this is the first byte of a pair of bytes.
    // This is important for processing or recognizing 16 bit
    // encodings, ie: Unicode.
    //
    boolean isFirstByte() {

        return (1 == (_byteCount % 2));
    }

    //
    // Return the second last byte read from the stream.
    //
    protected int getLastByte() {

        return _lastByte;
    }

    //
    // Return the next byte to be read from the stream.
    //
    protected int getNextByte() {

        // Save current class variables so they may be
        // returned to their original state after the
        // preemptive read from the stream.
        int myLastByte = _lastByte;
        int myCurrentByte = _currentByte;

        // Perfornm the read using established method.
        _nextByte = getByte();

        // Restore modified class variables to their original state.
        _lastByte = myLastByte;
        _currentByte = myCurrentByte;

        return _nextByte;
    }

    //
    // Return to the beginning of the stream.
    // -- only supports one buffer worth of recovery.
    // -- used after testing encoding type and after
    //    finding no BOM at the beginning of the stream.
    //
    protected void rewindInput() {

        _bufferPointer = 0;
        _byteCount = 0;
        _bytesRead = 0L;
        _atEof = false;
    }

    //
    // Instantiate a new UTF-8 character processing object
    // while retaining the buffer containing the data already
    // read from the input stream by the parent.
    //
    // -- used to instantiate the character processing object
    //    after determining the character encoding for the document.
    //
    protected Utf8InputProcessing spawnUtf8InputProcessing() {

        return new Utf8InputProcessing(_input, _bufferQueue);
    }

    //
    // Instantiate a new UTF-16 character processing object
    // while retaining the buffer containing the data already
    // read from the input stream by the parent.
    //
    // -- used to instantiate the character processing object
    //    after determining the character encoding for the document.
    //
    protected Utf16InputProcessing spawnUtf16InputProcessing(ByteOrderIO ioIn) {

        return new Utf16InputProcessing(_input, _bufferQueue, ioIn);
    }

    //
    // Instantiate a new single byte character processing object
    // while retaining the buffer containing the data already
    // read from the input stream by the parent.
    //
    // -- used to instantiate the character processing object
    //    after determining the character encoding for the document.
    //
    protected AnsiInputProcessing spawnAnsiInputProcessing() {

        return new AnsiInputProcessing(_input, _bufferQueue);
    }

    //
    // Instantiate a new extended unix character processing object
    // while retaining the buffer containing the data already
    // read from the input stream by the parent.
    //
    // -- used to instantiate the character processing object
    //    after determining the character encoding for the document.
    //
    protected ExtUnixInputProcessing spawnExtUnixInputProcessing() {

        return new ExtUnixInputProcessing(_input, _bufferQueue);
    }

    //
    //
    private void refreshBuffer() {

        // Do not allow a refresh if testing for encoding method,
        // We only support rewinding the stream for at most one
        // buffer size after the testing is completed.
        // Instead signify an end of stream condition.
        if (_testing) {

            if (null != _byteBuffer) {

                if (_queueLimit > _queueIndex) {

                    if (null != _bufferQueue) {

                        _bufferQueue.add(new BufferObject(_byteBuffer, _bufferTop));
                    }

                } else {

                    _atEof = true;
                }
            }

            if (!_atEof) {

                readData();
            }

        } else if ((null != _bufferQueue) && (null != _bufferQueue.peek())) {

            BufferObject<byte[]> myNextBuffer = _bufferQueue.remove();

            _byteBuffer = myNextBuffer.getBuffer();
            _bufferTop = myNextBuffer.getTrueCount();

        } else {

            readData();
        }
        // Reset the buffer pointer to the
        // beginning of the buffer in all cases.
        _bufferPointer = 0;
    }

    private void readData() {

        _byteBuffer = new byte[_bufferSize];

        // Get the next buffer full of data
        // (or whatever is left) from the input stream.
        _bufferTop = _input.read(_byteBuffer, 0, _bufferSize);
        // No data indicates an end of stream condition.
        _atEof = (_bufferPointer >= _bufferTop);
    }
}

/*
 * This class is responsible for all UTF-8 character processing
 * as well as testing the document to determine the character encoding.
 */
abstract class Utf8CharacterProcessing extends CharacterProcessing {

    protected static final byte[] _continuationMap = {

            1,  1,  1,  1,  1,  1,  1,  1,  2,  2,  2,  2,  3,  3,  4,  5
    };

    protected static final int _countShift = 2;
    protected static final int _continueShift = 6;

    // Byte processing masks useful in processing
    // the complex encoding algorithm.
    protected static final int _countMask = 0x0F;
    protected static final int _continueTestMask = 0xC0;
    protected static final int _continueDataMask = 0x3F;
    protected static final int _oneDataMask = 0x7F;
    protected static final int _twoDataMask = 0x1F;
    protected static final int _threeDataMask = 0x0F;
    protected static final int _fourDataMask = 0x07;
    protected static final int _fiveDataMask = 0x03;
    protected static final int _sixDataMask = 0x01;
    protected static final int _continueFlag = 0x80;
    protected static final int _multipleFlag = 0x80;
    protected static final int _illegalUtf8 = 0xFF;

    // Masks used to extract the actual data bits from
    // encoding bytes 1-6 used to define a single character.
    protected static final int[] _dataMask = new int[] {

            _oneDataMask, _twoDataMask, _threeDataMask,
            _fourDataMask, _fiveDataMask, _sixDataMask
    };

    //
    // CONSTRUCTOR -- one of two
    //
    // This is the first of the two constructors for the CharacterProcessing base class.
    // It is used to support creation of a encoding based character processing class
    // when the user has identified the the character encoding for the document.
    //
    Utf8CharacterProcessing(DocumentEncoder.DataSource DataSourceIn, DocumentEncoder.DataTarget DataTargetIn) {

        super(DataSourceIn, DataTargetIn);
    }

    //
    // CONSTRUCTOR -- two of two
    //
    // This is the second of the two constructors for the CharacterProcessing base class.
    // It is used to support creation of a encoding based character processing class
    // when the user has not identified the the character encoding for the document,
    // but the encoding has been determined by scanning a portion of the document.
    //
    //
    Utf8CharacterProcessing(DocumentEncoder.DataSource DataSourceIn, DocumentEncoder.DataTarget DataTargetIn,
                            Queue<BufferObject<byte[]>> bufferQueueIn) {

        super(DataSourceIn, DataTargetIn, bufferQueueIn);
    }
}

/*
 * This class is responsible for all UTF-8 character processing
 * as well as testing the document to determine the character encoding.
 */
class Utf8InputProcessing extends Utf8CharacterProcessing implements CharacterInput {

    //
    // CONSTRUCTOR -- one of two
    //
    // This is the first of the two constructors for the CharacterProcessing base class.
    // It is used to support creation of a encoding based character processing class
    // when the user has identified the the character encoding for the document.
    //
    Utf8InputProcessing(DocumentEncoder.DataSource DataSourceIn) {

        super(DataSourceIn, null);
    }

    //
    // CONSTRUCTOR -- two of two
    //
    // This is the second of the two constructors for the CharacterProcessing base class.
    // It is used to support creation of a encoding based character processing class
    // when the user has not identified the the character encoding for the document,
    // but the encoding has been determined by scanning a portion of the document.
    //
    //
    Utf8InputProcessing(DocumentEncoder.DataSource DataSourceIn, Queue<BufferObject<byte[]>> bufferQueueIn) {

        super(DataSourceIn, null, bufferQueueIn);
    }

    //
    // This routine gets the next character from the stream. For UTF-8 processing, each
    // character can be represented by anywhere from one to six bytes. Patterns within
    // the high order bits are used to identify the number of bytes defining the
    // character and the order of the individual byte within that list.
    //
    public Integer inputCharacter() {

        Integer myValue = null;
        int myChar = getByte();

        if (noCharacterByte != myChar) {

            // If the character does not have the top bit set,
            // it is simply a single byte ASCII value.
            if (_multipleFlag > myChar) {

                myValue = myChar;

            } else {

                // Use appropriate masks to extract information from the first byte
                // and use it to determine the number of bytes defining the character.
                int myKey = (_countMask & (myChar >> _countShift));
                int myCount = _continuationMap[myKey];
                int i = 0;

                // Extract the data portion from the byte and shove it into the character value.
                int myTemp = _dataMask[myCount] & myChar;

                // For each of the remaining bytes defining the character,
                // extract the data portion from the byte and shove those
                // bits into the character value below any previous bits.
                for (i = 0; myCount > i; i++) {

                    int myByte =  getByte();

                    if (noCharacterByte != myByte) {

                        myTemp = (myTemp << _continueShift) | (_continueDataMask & myByte);

                    } else {

                        break;
                    }
                }
                if (myCount == i) {

                    myValue = myTemp;
                }
            }
        }

        return myValue;
    }

    //
    // Method for stepping over the Byte Order Mark
    // found at the beginning of the document.
    //
    @Override
    public void stepOverBOM() {

        int myByte1 = getByte();
        int myByte2 = getByte();
        int myByte3 = getByte();

        // If a byte mark was not encountered
        // rewind to the beginning of the stream.
        if (((_bomUtf81 != myByte1) || (_bomUtf82 != myByte2) || (_bomUtf83 != myByte3))) {

            rewindInput();
        }
    }
}

/*
 * This class is responsible for all UTF-8 character processing
 * as well as testing the document to determine the character encoding.
 */
class Utf8OutputProcessing extends Utf8CharacterProcessing implements CharacterOutput {

    static final int _oneByteLimit = 0x7F;
    static final int _twoByteLimit = 0x07FF;
    static final int _threeByteLimit = 0xFFFF;
    static final int _fourByteLimit = 0x1FFFFF;
    static final int _fiveByteLimit = 0x3FFFFFF;
    static final int _sixByteLimit = 0x7FFFFFFF;

    static final int _twoByteFlag = 0xC0;
    static final int _threeByteFlag = 0xE0;
    static final int _fourByteFlag = 0xF0;
    static final int _fiveByteFlag = 0xF8;
    static final int _sixByteFlag = 0xFC;

    static final int _continuationDataBits = 6;

    static final int[] _utf8Limits = new int[]
            { _twoByteLimit, _threeByteLimit, _fourByteLimit, _fiveByteLimit, _sixByteLimit };

    static final int[] _firstByteFlag = new int[]
            { _twoByteFlag, _threeByteFlag, _fourByteFlag, _fiveByteFlag, _sixByteFlag };

    static final int[] firstByteMask = new int[]
            { _twoDataMask, _threeDataMask, _fourDataMask, _fiveDataMask, _sixDataMask };

    //
    // CONSTRUCTOR -- one of two
    //
    // This is the first of the two constructors for the CharacterProcessing base class.
    // It is used to support creation of a encoding based character processing class
    // when the user has identified the the character encoding for the document.
    //
    Utf8OutputProcessing(DocumentEncoder.DataTarget DataTargetIn) {

        super(null, DataTargetIn);
    }

    //
    // CONSTRUCTOR -- two of two
    //
    // This is the second of the two constructors for the CharacterProcessing base class.
    // It is used to support creation of a encoding based character processing class
    // when the user has not identified the the character encoding for the document,
    // but the encoding has been determined by scanning a portion of the document.
    //
    //
    Utf8OutputProcessing(DocumentEncoder.DataTarget DataTargetIn, Queue<BufferObject<byte[]>> bufferQueueIn) {

        super(null, DataTargetIn, bufferQueueIn);
    }

    public boolean outputCharacter(int valueIn) {

        if (_oneByteLimit >= valueIn) {

            putByte(valueIn);

        } else {

            for (int i = 0; _utf8Limits.length >i; i++) {

                if (_utf8Limits[i] > valueIn) {

                    int myBitShift = (1 + i) * _continuationDataBits;

                    putByte(_firstByteFlag[i] | (firstByteMask[i] & (valueIn >> myBitShift)));

                    for (myBitShift -= _continuationDataBits; 0 <= myBitShift; myBitShift -= _continuationDataBits) {

                        putByte(_continueFlag | (_continueDataMask & (valueIn >> myBitShift)));
                    }
                    break;
                }
            }
        }
        return true;
    }

    public void generateBOM() {

        putByte(_bomUtf81);
        putByte(_bomUtf82);
        putByte(_bomUtf83);
    }
}

/*
 * This class is responsible for all UTF-8 character processing
 * as well as testing the document to determine the character encoding.
 */
class EncodingSearch extends Utf8CharacterProcessing {

    //
    // CONSTRUCTOR -- the only one
    //
    // This is the only constructor for the CharacterProcessing base class.
    // It is used to support creation of a encoding based character processing class
    // when the user has identified the the character encoding for the document.
    //
    EncodingSearch(DocumentEncoder.DataSource streamIn) {

        super(streamIn, null);
    }

    public DocumentEncoder.EncodingMethod determineEncoding(List<DocumentEncoder.EncodingMethod> negativeListIn, boolean ignoreNullsIn) {

        // Instantiate an object for keeping track of evidence
        // used to determine the document encoding method.
        // Each time it is called it returns a boolean value indicating
        // whether or not an encoding method has been verified. Once the
        // result is true, we abandon the effort and return an instantiated
        // processing object for that encoding method.
        EncodingElimination myEliminator = new EncodingElimination(negativeListIn);

        // Begin with three bytes
        // -- the most needed to test for a Byte Order Mark.
        int myByte1 = getByte();
        int myByte2 = getByte();
        int myByte3 = getByte();
        int myByte4 = getByte();

        String myDebug = Format.value(myByte1) + " " + Format.value(myByte2) + " " + Format.value(myByte3) + " " + Format.value(myByte4);

        if (myDebug.equals("  ==  ")) {
         return null;
      }

        boolean myExitFlag = false;

        // Flag that we are testing for the encoding
        // method to avoid going beyond one buffer.
        _testing = true;

        // If we recognize the UTF-8 Byte Order Marker (unlikely)
        // we know the encoding is UTF-8
        if ((_bomUtf81 == myByte1) && (_bomUtf82 == myByte2) && (_bomUtf83 == myByte3)) {

            return DocumentEncoder.EncodingMethod.utf8;

            // If we recognize the Big Endian Unicode Byte Order Marker
            // we know the encoding is Big Endian Unicode
        } else if (((_bomUnicode2 == myByte2) && (_bomUnicode1 == myByte1))
                    || ((_bomUnicode2A == myByte3) && (_bomUnicode2B == myByte4)
                        && (_bomUnicode1A == myByte1) && (_bomUnicode1B == myByte2))){

            return DocumentEncoder.EncodingMethod.utf16be;

            // If we recognize the Little Endian Unicode Byte Order Marker
            // we know the encoding is Little Endian Unicode
        } else if (((_bomUnicode2 == myByte1) && (_bomUnicode1 == myByte2))
                || ((_bomUnicode2A == myByte1) && (_bomUnicode2B == myByte2)
                && (_bomUnicode1A == myByte3) && (_bomUnicode1B == myByte4))){

            return DocumentEncoder.EncodingMethod.utf16le;

            // Otherwise we must try to hone in on the proper encoding (good luck).
        } else {

            // Start over at the top of the buffer.
            // We only process a single buffer and take our best guest.
            rewindInput();

            myByte1 = 0;
            myByte2 = 0;
            myByte3 = 0;
            myByte4 = 0;

            for (int myByte = getByte(); !_atEof; myByte = getByte()) {
                myByte1 = myByte2;
                myByte2 = myByte3;
                myByte3 = myByte4;
                myByte4 = myByte;

                switch (myByte) {

                    // If a null byte is encountered this must be either
                    // Little Endian or Big Endian Unicode.
                    case _nullByte:

                        if (!ignoreNullsIn) {

                            // Reject ANSI, Ext Unix and UTF-8, and exit loop
                            // if an encoding method determined.
                            myEliminator.notAnsi();
                            myEliminator.notExtUnix();
                            myEliminator.notUft8();


                            // Record the Unicode character encountered,
                            // and exit loop if an encoding method determined.
                            if (isFirstByte()) {

                                myExitFlag = myEliminator.recordBeCharacter(getNextByte());

                            } else {

                                myExitFlag = myEliminator.recordLeCharacter(getLastByte());
                            }
                        }
                        break;

                    // If we encounter an illegal UTF-8 byte value or a continuation
                    // byte from a UTF-8 byte sequence with no preceding first byte,
                    // we know we do not have UTF-8 encoding.
                    case _illegalUtf8:

                        myExitFlag = myEliminator.notUft8();
                        break;

                    case _escapeChar:
                    case _shiftOut:
                    case _shiftIn:

                        myExitFlag = myEliminator.notAnsi();
                        break;

                    default :

                        if ((_illegalExtRangeLo <= myByte) && (_illegalExtRangeHi >= myByte)) {

                            myEliminator.notExtUnix();
                        }

                        // If we encounter a continuation byte from a UTF-8
                        // byte sequence with no preceding first byte,
                        // we know we do not have UTF-8 encoding.
                        if (_continueFlag == (_continueTestMask & myByte)) {

                            myExitFlag = myEliminator.notUft8();

                            // TODO:
                            // If we have a possible first byte of a multi-byte UTF-8 sequence,
                            // attempt to verify the entire sequence.
                        } else if (0 != (_multipleFlag & myByte)) {

                            int myKey = (_countMask & (myByte >> _countShift));
                            int myCount = _continuationMap[myKey];

                            for (int i = 0; myCount > i; i++) {

                                myByte = getByte();

                                myByte1 = myByte2;
                                myByte2 = myByte3;
                                myByte3 = myByte4;
                                myByte4 = myByte;

                                // If this is not a continuation byte within a multi-byte
                                // UTF-8 sequence we do not have UTF-8 encoding.
                                if (_continueFlag != (_continueTestMask & myByte)) {

                                    myExitFlag = myEliminator.notUft8();
                                    retryByte();
                                    break;
                                }
                            }
                        }
                        break;
                }
                if (myExitFlag) {

                    break;
                }
            }
        }

        // Return the identified encoding method or the
        // best guess based upon the evidence collected.
        return myEliminator.identifyProcessing();
    }

    //
    //
    protected CharacterInput testEncodings() {

        switch (determineEncoding(null, false)) {

            case utf8:

                return spawnUtf8InputProcessing();

            case utf16be:

                return spawnUtf16InputProcessing(new BigEndianProcessing());

            case utf16le:

                return spawnUtf16InputProcessing(new LittleEndianProcessing());

            case ansi:

                return spawnAnsiInputProcessing();

            case extunix:

                return spawnExtUnixInputProcessing();

        }
        return null;
    }

    public CharacterInput spawnDecoder(DocumentEncoder.EncodingMethod encodingMethodIn) {

        switch (encodingMethodIn) {

            case utf8:

                return spawnUtf8InputProcessing();

            case utf16be:

                return spawnUtf16InputProcessing(new BigEndianProcessing());

            case utf16le:

                return spawnUtf16InputProcessing(new LittleEndianProcessing());

            case ansi:

                return spawnAnsiInputProcessing();

            case extunix:

                return spawnExtUnixInputProcessing();

        }
        return null;
    }
}

/*
 * This class is responsible for all UTF-16 character processing.
 * Characters are two bytes each, read directly from the file
 * without any special processing except substitution.
 */
class Utf16InputProcessing extends CharacterProcessing implements CharacterInput {

    ByteOrderIO _io;

    //
    // CONSTRUCTOR -- one of two
    //
    // This is the first of the two constructors for the CharacterProcessing base class.
    // It is used to support creation of a encoding based character processing class
    // when the user has identified the the character encoding for the document.
    //
    Utf16InputProcessing(DocumentEncoder.DataSource streamIn, ByteOrderIO ioIn) {

        super(streamIn, null);

        _io = ioIn;
        _io.setInput(this);
    }

    //
    // CONSTRUCTOR -- two of two
    //
    // This is the second of the two constructors for the CharacterProcessing base class.
    // It is used to support creation of a encoding based character processing class
    // when the user has not identified the the character encoding for the document,
    // but the encoding has been determined by scanning a portion of the document.
    //
    Utf16InputProcessing(DocumentEncoder.DataSource streamIn, Queue<BufferObject<byte[]>> bufferQueueIn, ByteOrderIO ioIn) {

        super(streamIn, null, bufferQueueIn);

        _io = ioIn;
        _io.setInput(this);
    }

    //
    // This routine gets the next character from the stream. For Unicode, each character
    // consists of two bytes.
    //
    public Integer inputCharacter() {

        Integer myCodePoint = _io.get();

        if (null != myCodePoint) {

            if ((_surrogateBase1 <= myCodePoint) && (_surrogateTop1 >= myCodePoint)) {

                Integer myNextCharacter = _io.get();

                if (null != myNextCharacter) {

                    if ((_surrogateBase2 <= myNextCharacter) && (_surrogateTop2 >= myNextCharacter)) {

                        myCodePoint = _supplementaryBase +
                                (((myCodePoint - _surrogateBase1) << 16)
                                        | (myNextCharacter - _surrogateBase2));
                    } else {

                        myCodePoint = null;
                    }
                }
            }
        }

        return myCodePoint;
    }

    //
    // Method for stepping over the Byte Order Mark
    // found at the beginning of the document.
    //
    @Override
    public void stepOverBOM() {

        int myByte1 = getByte();
        int myByte2 = getByte();

        // If a byte mark was not encountered
        // rewind to the beginning of the stream.
        if ((((_bomUnicode1 != myByte1) || (_bomUnicode2 != myByte2)) && ((_bomUnicode2 != myByte1) || (_bomUnicode1 != myByte2)))) {

            int myByte3 = getByte();
            int myByte4 = getByte();

            if ((((_bomUnicode1A != myByte1) || (_bomUnicode1B != myByte2) || (_bomUnicode2A != myByte3) || (_bomUnicode2B != myByte4))
                  && ((_bomUnicode2A != myByte1) || (_bomUnicode2B != myByte2) || (_bomUnicode1A != myByte3) || (_bomUnicode1B != myByte4)))) {

                rewindInput();
            }
        }
    }
}

/*
 * This class is responsible for all UTF-16 character processing.
 * Characters are two bytes each, read directly from the file
 * without any special processing except substitution.
 */
class Utf16OutputProcessing extends CharacterProcessing implements CharacterOutput{

    ByteOrderIO _io;

    //
    // CONSTRUCTOR -- one of two
    //
    // This is the first of the two constructors for the CharacterProcessing base class.
    // It is used to support creation of a encoding based character processing class
    // when the user has identified the the character encoding for the document.
    //
    Utf16OutputProcessing(DocumentEncoder.DataTarget streamIn, ByteOrderIO ioIn) {

        super(null, streamIn);

        _io = ioIn;
        _io.setInput(this);
    }

    //
    // CONSTRUCTOR -- two of two
    //
    // This is the second of the two constructors for the CharacterProcessing base class.
    // It is used to support creation of a encoding based character processing class
    // when the user has not identified the the character encoding for the document,
    // but the encoding has been determined by scanning a portion of the document.
    //
    Utf16OutputProcessing(DocumentEncoder.DataTarget streamIn, Queue<BufferObject<byte[]>> bufferQueueIn, ByteOrderIO ioIn) {

        super(null, streamIn, bufferQueueIn);

        _io = ioIn;
    }

    public boolean outputCharacter(int valueIn) {

        boolean mySuccess = false;

        if ((_supplementaryBase <= valueIn) && (_supplementaryTop >= valueIn)) {

            int myValue = valueIn - _supplementaryBase;

            _io.put(_surrogateBase1 + ((myValue >> _tenBitShift) & _tenBitMask));
            _io.put(_surrogateBase2 + (myValue & _tenBitMask));
            mySuccess = true;

        } else if ((_range1top >= valueIn)
                || ((_range2base <= valueIn) && (_range2top >= valueIn))) {

            _io.put(valueIn);
            mySuccess = true;
        }
        return mySuccess;
    }

    public void generateBOM() {

        _io.put(_bomUnicode);
    }
}

/*
 * Class for processing ANSI (Windows-1252) text files
 * Characters are one byte each, read directly from the file
 * without any special processing except substitution.
 */
class AnsiInputProcessing extends CharacterProcessing implements CharacterInput {

    private Map<Integer, Integer> _ansiMap = null;

    //
    // CONSTRUCTOR -- one of two
    //
    // This is the first of the two constructors for the CharacterProcessing base class.
    // It is used to support creation of a encoding based character processing class
    // when the user has identified the the character encoding for the document.
    //
    AnsiInputProcessing(DocumentEncoder.DataSource streamIn) {

        super(streamIn, null);
    }

    //
    // CONSTRUCTOR -- two of two
    //
    // This is the second of the two constructors for the CharacterProcessing base class.
    // It is used to support creation of a encoding based character processing class
    // when the user has not identified the the character encoding for the document,
    // but the encoding has been determined by scanning a portion of the document.
    //
    AnsiInputProcessing(DocumentEncoder.DataSource streamIn, Queue<BufferObject<byte[]>> bufferQueueIn) {

        super(streamIn, null, bufferQueueIn);
    }

    //
    // This routine gets the next character from the stream. For ANSI encoded documents
    // such as ASCII or Windows-1252 each character consists of a single byte.
    // We need to allow for having both mapped and unmapped characters as the final output.
    // If the codepoint is not found in the map, use it directly.
    //
    public Integer inputCharacter() {

        int myCharacterIn = getByte();

        return (noCharacterByte != myCharacterIn) ? myCharacterIn : null;
    }
}

/*
 * Class for processing ANSI (Windows-1252) text files
 * Characters are one byte each, read directly from the file
 * without any special processing except substitution.
 */
class AnsiOutputProcessing extends CharacterProcessing implements CharacterOutput {

    private Map<Integer, Integer> _ansiMap = null;

    //
    // CONSTRUCTOR -- one of two
    //
    // This is the first of the two constructors for the CharacterProcessing base class.
    // It is used to support creation of a encoding based character processing class
    // when the user has identified the the character encoding for the document.
    //
    AnsiOutputProcessing(DocumentEncoder.DataTarget streamIn) {

        super(null, streamIn);
    }

    //
    // CONSTRUCTOR -- two of two
    //
    // This is the second of the two constructors for the CharacterProcessing base class.
    // It is used to support creation of a encoding based character processing class
    // when the user has not identified the the character encoding for the document,
    // but the encoding has been determined by scanning a portion of the document.
    //
    AnsiOutputProcessing(DocumentEncoder.DataTarget streamIn, Queue<BufferObject<byte[]>> bufferQueueIn) {

        super(null, streamIn, bufferQueueIn);
    }

    //
    // We need to allow for having both mapped and unmapped characters.
    // If the codepoint is not found in the map, use it directly.
    //
    public boolean outputCharacter(int valueIn) {

        boolean mySuccess = false;
        Integer myValue = (null != _ansiMap) ? _ansiMap.get(valueIn) : valueIn;

        if (null == myValue) {

            myValue = valueIn;
        }

        if (_byteMask >= myValue) {

            putByte(myValue);
            mySuccess = true;

        }
        return mySuccess;
    }

    //
    // There is no Byte Order Mark for any ANSI encodings.
    //
    public void generateBOM() {

    }
}

/*
 * Class for processing ANSI (Windows-1252) text files
 * Characters are one byte each, read directly from the file
 * without any special processing except substitution.
 */
class ExtUnixInputProcessing extends CharacterProcessing implements CharacterInput {

    private int characterAugment = 0x00;

    //
    // CONSTRUCTOR -- one of two
    //
    // This is the first of the two constructors for the CharacterProcessing base class.
    // It is used to support creation of a encoding based character processing class
    // when the user has identified the the character encoding for the document.
    //
    ExtUnixInputProcessing(DocumentEncoder.DataSource streamIn) {

        super(streamIn, null);
    }

    //
    // CONSTRUCTOR -- two of two
    //
    // This is the second of the two constructors for the CharacterProcessing base class.
    // It is used to support creation of a encoding based character processing class
    // when the user has not identified the the character encoding for the document,
    // but the encoding has been determined by scanning a portion of the document.
    //
    ExtUnixInputProcessing(DocumentEncoder.DataSource streamIn, Queue<BufferObject<byte[]>> bufferQueueIn) {

        super(streamIn, null, bufferQueueIn);
    }

    //
    // TODO: Add proper support
    //
    // We only are interested in correctly extracting ascii
    // characters so that we may determine field delimeters.
    //
    // To achieve this, we remove all escape sequences and recognize
    // changes to GL. When GL does not contain G0 we set the high order bit
    // to force the character into GR. This works since multiple byte
    // characters always have both bytes falling within GR.
    //
    // TODO:
    //
    public Integer inputCharacter() {

        int myByte = getByte();

        switch (myByte) {

            case noCharacterByte:

                break;

            case _escapeChar:

                // Get next byte and test it
                myByte = getByte();

                switch(myByte) {

                    case noCharacterByte:

                        break;

                    case 0x6E:
                    case 0x6F:
                    case 0x7C:
                    case 0x7D:
                    case 0x7E:

                        characterAugment = 0x80;
                        break;

                    case 0x21:
                    case 0x22:
                    case 0x28:
                    case 0x29:
                    case 0x2A:
                    case 0x2B:
                    case 0x2D:
                    case 0x2E:
                    case 0x2F:

                        // Get next byte and discard
                        myByte = getByte();

                        // Get next byte and set high bit
                        // so that it is not mistaken for a delimeter
                        // if GL does not contain ascii characters
                        myByte = characterAugment | getByte();
                        break;

                    case 0x24:

                        // Get next byte and test it
                        myByte = getByte();

                        switch(myByte) {

                            case noCharacterByte:

                                break;

                            case 0x28:
                            case 0x29:
                            case 0x2A:
                            case 0x2B:
                            case 0x2D:
                            case 0x2E:
                            case 0x2F:

                                // Get next byte and discard
                                myByte = getByte();
                                break;
                        }

                        // Get next byte and set high bit
                        // so that it is not mistaken for a delimeter
                        // if GL does not contain ascii characters
                        myByte = characterAugment | getByte();
                        break;

                    case 0x25:

                        // Get next byte and test it
                        myByte = getByte();

                        if (noCharacterByte != myByte) {

                            if (0x2F == myByte) {

                                // Get next byte and discard
                                myByte = getByte();
                            }
                        }

                        if (noCharacterByte != myByte) {

                            // Get next byte and set high bit
                            // so that it is not mistaken for a delimeter
                            // if GL does not contain ascii characters
                            myByte = getByte();

                            if (noCharacterByte != myByte) {

                                myByte |= characterAugment;
                            }
                        }
                        break;
                }
                break;

            case 0x8E:

                // Retrieve and discard escape sequence
                if (noCharacterByte != getByte()) {

                    if (noCharacterByte != getByte()) {

                        // Get next byte and set high bit
                        // so that it is not mistaken for a delimeter
                        myByte = getByte();

                        if (noCharacterByte != myByte) {

                            myByte |= 0x80;
                        }
                    }
                }
                break;

            case _shiftOut:

                characterAugment = 0x80;

                // Get next byte and set high bit
                // so that it is not mistaken for a delimeter
                // if GL does not contain ascii characters
                myByte = getByte();

                if (noCharacterByte != myByte) {

                    myByte |= characterAugment;
                }
                break;

            case _shiftIn:

                characterAugment = 0x00;

                // Get next byte and set high bit
                // so that it is not mistaken for a delimeter
                // if GL does not contain ascii characters
                myByte = getByte();

                if (noCharacterByte != myByte) {

                    myByte |= characterAugment;
                }
                break;
        }
        return (noCharacterByte != myByte) ? myByte : null;
    }
}

/*
 * Class for processing ANSI (Windows-1252) text files
 * Characters are one byte each, read directly from the file
 * without any special processing except substitution.
 */
class ExtUnixOutputProcessing extends CharacterProcessing implements CharacterOutput {

    private Map<Integer, Integer> _extMap = null;

    //
    // CONSTRUCTOR -- one of two
    //
    // This is the first of the two constructors for the CharacterProcessing base class.
    // It is used to support creation of a encoding based character processing class
    // when the user has identified the the character encoding for the document.
    //
    ExtUnixOutputProcessing(DocumentEncoder.DataTarget streamIn) {

        super(null, streamIn);
    }

    //
    // CONSTRUCTOR -- two of two
    //
    // This is the second of the two constructors for the CharacterProcessing base class.
    // It is used to support creation of a encoding based character processing class
    // when the user has not identified the the character encoding for the document,
    // but the encoding has been determined by scanning a portion of the document.
    //
    ExtUnixOutputProcessing(DocumentEncoder.DataTarget streamIn, Queue<BufferObject<byte[]>> bufferQueueIn) {

        super(null, streamIn, bufferQueueIn);
    }

    //
    // TODO:
    //
    // We need to allow for having both mapped and unmapped characters,
    // as well as escape characters used to point to installed character
    // maps, and escape sequences used to install new character maps.
    //
    // TODO:
    //
    public boolean outputCharacter(int valueIn) {

        boolean mySuccess = false;

        //TODO: Do something -- must maintain a state for encoding.

        return mySuccess;
    }

    //
    // There is no Byte Order Mark for any ANSI encodings.
    //
    public void generateBOM() {

    }
}

class EncodingElimination {

    // Masks used to set and check flag bits within the elimination mask.
    static final private int _notAnsiFlag = 1 << 0;
    static final private int _notUtf8Flag = 1 << 1;
    static final private int _notLeUFlag = 1 << 2;
    static final private int _notBeUFlag = 1 << 3;
    static final private int _notExtUFlag = 1 << 4;

    // Offsets into the solution map array used to identify an
    // encoding method selected by being the only one not rejected.
    static final private int _ansiSelected = 30;	// Only bit 0 not set
    static final private int _utf8Selected = 29;	// Only bit 1 not set
    static final private int _leuSelected = 27;		// Only bit 2 not set
    static final private int _beuSelected = 23;		// Only bit 3 not set
    static final private int _extuSelected = 15;	// Only bit 4 not set

    static final private int _rejectAll = 31;		// All bits set

    static final private DocumentEncoder.EncodingMethod _defaultEncoding = DocumentEncoder.EncodingMethod.ansi;

    static final private boolean[] _solutionMap = {

            //                                                                                                              15
            //  00000  00001  00010  00011  00100  00101  00110  00111  01000  01001  01010  01011  01100  01101  01110  01111
            false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,  true,
            //                                                      23                          27            29     30     31
            //  10000  10001  10010  10011  10100  10101  10110  10111  11000  11001  11010  11011  11100  11101  11110  11111
            false, false, false, false, false, false, false,  true, false, false, false,  true, false,  true,  true,  true
    };

    static final private Map<DocumentEncoder.EncodingMethod, Integer> _rejectionMap
            = new TreeMap<DocumentEncoder.EncodingMethod, Integer>();

    static {

        _rejectionMap.put(DocumentEncoder.EncodingMethod.utf16be, _notBeUFlag);
        _rejectionMap.put(DocumentEncoder.EncodingMethod.utf16le, _notLeUFlag);
        _rejectionMap.put(DocumentEncoder.EncodingMethod.utf8, _notUtf8Flag);
        _rejectionMap.put(DocumentEncoder.EncodingMethod.ansi, _notAnsiFlag);
        _rejectionMap.put(DocumentEncoder.EncodingMethod.extunix, _notExtUFlag);
    }
    private int _eliminationMask = 0;

    private Map<Integer, Integer> _beCharacters = new TreeMap<Integer, Integer>();
    private Map<Integer, Integer> _leCharacters = new TreeMap<Integer, Integer>();

    EncodingElimination(List<DocumentEncoder.EncodingMethod> negativeListIn) {
        if (negativeListIn != null) {
            for (DocumentEncoder.EncodingMethod myMethod : negativeListIn) {
                if (myMethod != null) {
                    _eliminationMask |= _rejectionMap.get(myMethod);
                }
            }
        }
    }

    //
    // Reject ANSI as the encoding
    // and return true if only a single encoding method remains
    //
    boolean notAnsi() {

        _eliminationMask |= _notAnsiFlag;

        return _solutionMap[_eliminationMask];
    }

    //
    // Reject ANSI as the encoding
    // and return true if only a single encoding method remains
    //
    boolean notExtUnix() {

        _eliminationMask |= _notExtUFlag;

        return _solutionMap[_eliminationMask];
    }

    //
    // Reject UTF-8 as the encoding
    // and return true if only a single encoding method remains
    //
    boolean notUft8() {

        _eliminationMask |= _notUtf8Flag;

        return _solutionMap[_eliminationMask];
    }

    //
    // Reject Little Endian Unicode as the encoding
    // and return true if only a single encoding method remains
    //
    boolean notLeUnicode() {

        _eliminationMask |= _notLeUFlag;

        return _solutionMap[_eliminationMask];
    }

    //
    // Reject Big Endian Unicode as the encoding
    // and return true if only a single encoding method remains
    //
    boolean notBeUnicode() {

        _eliminationMask |= _notBeUFlag;

        return _solutionMap[_eliminationMask];
    }

    //
    // Return true if only a single encoding method remains
    //
    boolean isCertain() {

        return _solutionMap[_eliminationMask];
    }

    //
    // Record an encounter with a possible Big Endian character
    // and return true if only a single encoding method remains
    //
    boolean recordBeCharacter(int byteIn) {

        if (CharacterProcessing.noCharacterByte != byteIn) {

            _beCharacters.put(byteIn,  0);

        } else {

            _eliminationMask |= _notBeUFlag;
        }
        return _solutionMap[_eliminationMask];
    }

    //
    // Record an encounter with a possible Little Endian character
    // and return true if only a single encoding method remains
    //
    boolean recordLeCharacter(int byteIn) {

        if (CharacterProcessing.noCharacterByte != byteIn) {

            _leCharacters.put(byteIn,  0);

        } else {

            _eliminationMask |= _notLeUFlag;
        }
        return _solutionMap[_eliminationMask];
    }

    //
    // This routine is used to select the most probable encoding method used
    // in the document based upon the evidence gathered.
    //
    DocumentEncoder.EncodingMethod identifyProcessing() {

        DocumentEncoder.EncodingMethod mySelection;

        switch (_eliminationMask) {

            // All but Big Endian Unicode rejected
            case _beuSelected:

                mySelection = DocumentEncoder.EncodingMethod.utf16be;
                break;

            // All but Little Endian Unicode rejected
            case _leuSelected:

                mySelection = DocumentEncoder.EncodingMethod.utf16le;
                break;

            // All but UTF-8 rejected
            case _utf8Selected:

                mySelection = DocumentEncoder.EncodingMethod.utf8;
                break;

            // All but ANSI rejected
            case _ansiSelected:

                mySelection = DocumentEncoder.EncodingMethod.ansi;
                break;

            // All but Ext Unix rejected
            case _extuSelected:

                mySelection = DocumentEncoder.EncodingMethod.extunix;
                break;

            // All encodings rejected -- use default
            case _rejectAll:

                mySelection = _defaultEncoding;
                break;

            // Not conclusive -- use a little more logic
            default:

                mySelection = bestGuess();
                break;
        }

        return mySelection;
    }

    //
    // This routine is used to settle on a probable encoding format for the
    // document when an encoding format has not been successfully recognized.
    //
    private DocumentEncoder.EncodingMethod bestGuess() {

        DocumentEncoder.EncodingMethod myChoice = _defaultEncoding; // default if all encodings rejected

        if (_rejectAll != _eliminationMask) {

            // Select UTF-8 as a first choice if not already rejected.
            if (0 == (_notUtf8Flag & _eliminationMask)) {

                myChoice = DocumentEncoder.EncodingMethod.utf8;

                // Select ANSI as a second choice if not already rejected.
            } else if (0 == (_notAnsiFlag & _eliminationMask)) {

                myChoice = DocumentEncoder.EncodingMethod.ansi;

                // Select extended unix as a third choice if not already rejected.
            } else if (0 == (_notExtUFlag & _eliminationMask)) {

                myChoice = DocumentEncoder.EncodingMethod.extunix;

                // Select Unicode LE as a fourth choice if Unicode BE has been rejected.
            } else if (0 != (_notBeUFlag & _eliminationMask)) {

                myChoice = DocumentEncoder.EncodingMethod.utf16le;

                // Select Unicode BE as a fifth choice if Unicode LE has been rejected.
            } else if (0 != (_notLeUFlag & _eliminationMask)) {

                myChoice = DocumentEncoder.EncodingMethod.utf16be;

                // Select Unicode BE as a fifth choice if neither Unicode BE
                // nor Unicode LE has been rejected, and the number of different
                // likely BE first page characters exceeds the number of different
                // likely LE first page characters
            } else if (_beCharacters.size() > _leCharacters.size()) {

                myChoice = DocumentEncoder.EncodingMethod.utf16be;

                // Settle for Unicode LE since it has not been rejected
            } else {

                myChoice = DocumentEncoder.EncodingMethod.utf16le;
            }
        }
        return myChoice;
    }
}

/*
 * This class is used for the sole purpose of retrieving
 * or outputing a byte pair in BigEndian order.
*/
abstract class ByteOrderIO {

    static final int _byteShift = 8;
    static final int _byteMask = 0xFF;

    ByteInput _input;
    ByteOutput _output;

    void setInput(ByteInput inputIn) {

        _input = inputIn;
    }

    void setOutput(ByteOutput outputIn) {

        _output = outputIn;
    }

    abstract void put(int valueIn);
    abstract Integer get();
}

/*
 * This class is used for the sole purpose of retrieving
 * or outputing a byte pair in BigEndian order.
*/
class BigEndianProcessing extends ByteOrderIO {

    void put(int valueIn) {

        _output.putByte(_byteMask & (valueIn >> _byteShift));
        _output.putByte(_byteMask & valueIn);
    }

    Integer get() {

        int myByteOne = _input.getByte();
        int myByteTwo = _input.getByte();

        return ((CharacterProcessing.noCharacterByte == myByteOne)
                || (CharacterProcessing.noCharacterByte == myByteTwo))
                ? null : (myByteOne << 8) | myByteTwo;
    }
}

/*
 * This class is used for the sole purpose of retrieving
 * or outputing a byte pair in LittleEndian order.
*/
class LittleEndianProcessing extends ByteOrderIO {

    void put(int valueIn) {

        _output.putByte(_byteMask & valueIn);
        _output.putByte(_byteMask & (valueIn >> _byteShift));
    }

    Integer get() {

        int myByteOne = _input.getByte();
        int myByteTwo = _input.getByte();

        return ((CharacterProcessing.noCharacterByte == myByteOne)
                || (CharacterProcessing.noCharacterByte == myByteTwo))
                ? null : myByteOne | (myByteTwo << 8);
    }
}
