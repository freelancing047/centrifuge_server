package csi.client.gwt.widget.ui.uploader.wizards.support;

import java.util.ArrayList;
import java.util.List;

import csi.server.common.enumerations.CsiColumnDelimiter;
import csi.server.common.enumerations.CsiColumnQuote;
import csi.server.common.exception.CentrifugeException;

/**
 * Created by centrifuge on 12/30/2015.
 */
public class BlockAnalysis {

    private class FileScan {

        private boolean _pass;
        private Character _delimiter;
        private Character _quote;
        private int _quoteCount;
        private int _columnCount;
        private int _rowCount;
        private int _offset;

        public FileScan(ReadCsvBlock dataReaderIn, Character delimiterIn, Character quoteIn)
                throws Exception {

            initializeValues(delimiterIn, quoteIn);

            dataReaderIn.restart();
            dataReaderIn.setDelimiter(_delimiter);
            if (null != _quote) {

                dataReaderIn.setQuote(_quote);

            } else {

                dataReaderIn.disableQuoting();
            }
            _columnCount = dataReaderIn.countColumns();

            if (1 < _columnCount) {

                for (int myColumnCount = _columnCount;
                     myColumnCount == _columnCount; myColumnCount = dataReaderIn.countColumns()) {

                    _rowCount++;
                }
                if (dataReaderIn.isAtEOD()) {

                    _pass = true;
                    _quoteCount = dataReaderIn.getQuoteCount();
                }
            }
        }

        public FileScan(ReadTextBlock dataReaderIn, Character delimiterIn) throws Exception {

            initializeValues(delimiterIn, null);

            if (1 < _columnCount) {

                dataReaderIn.restart();
                dataReaderIn.setDelimiter(_delimiter);

                for (int myColumnCount = (_columnCount = dataReaderIn.countColumns());
                     myColumnCount == _columnCount; myColumnCount = dataReaderIn.countColumns()) {

                    _rowCount++;
                }
                _pass = dataReaderIn.isAtEOD();
            }
        }

        public boolean doesPass() {

            return _pass;
        }

        public boolean hasQuote() {

            return (0 < _quoteCount);
        }

        public int lineCount() {

            return _rowCount;
        }

        public Character getDelimiter() {

            return _delimiter;
        }

        public Character getQuote() {

            return _quote;
        }

        private void initializeValues(Character delimiterIn, Character quoteIn) {

            _delimiter = delimiterIn;
            _quote = quoteIn;
            _rowCount = 0;
            _quoteCount = 0;
            _columnCount = 0;
            _offset = 0;
            _pass = false;
        }
    }

    private Character _delimiter = null;
    private Character _quote = null;

    public BlockAnalysis(ReadCsvBlock dataReaderIn, Character delimiterIn, Character quoteIn) throws Exception {

        List<FileScan> myResults = new ArrayList<FileScan>();

        _delimiter = delimiterIn;
        _quote = quoteIn;

        if (null == _delimiter) {

            if (null == _quote) {

                for (int i = 0; CsiColumnDelimiter.values().length > i; i++) {

                    myResults.add(new FileScan(dataReaderIn, CsiColumnDelimiter.values()[i].getCharacter(), null));

                    for (int j = 0; CsiColumnQuote.values().length > j; j++) {

                        myResults.add(new FileScan(dataReaderIn, CsiColumnDelimiter.values()[i].getCharacter(), CsiColumnQuote.values()[j].getCharacter()));
                    }
                }

            } else {

                for (int i = 0; CsiColumnDelimiter.values().length > i; i++) {

                    myResults.add(new FileScan(dataReaderIn, CsiColumnDelimiter.values()[i].getCharacter(), _quote));
                }
            }

        } else {

            if (null == _quote) {

                myResults.add(new FileScan(dataReaderIn, _delimiter, null));

                for (int i = 0; CsiColumnQuote.values().length > i; i++) {

                    myResults.add(new FileScan(dataReaderIn, _delimiter, CsiColumnQuote.values()[i].getCharacter()));
                }
            }
        }
        analyzeScans(myResults);
    }

    public BlockAnalysis(ReadTextBlock dataReaderIn) throws Exception {

        List<FileScan> myResults = new ArrayList<FileScan>();

        for (int i = 0; CsiColumnDelimiter.values().length > i; i++) {

            myResults.add(new FileScan(dataReaderIn, CsiColumnDelimiter.values()[i].getCharacter()));
        }
        analyzeScans(myResults);
    }

    public Character getDelimiter() {

        return (null != _delimiter) ? _delimiter : null;
    }

    public Character getQuotingCharacter() {

        return (null != _quote) ? _quote : null;
    }

    private void analyzeScans(List<FileScan> resultsIn) {

        FileScan mySelection = null;

        for (int myLimit = 5; 0 < myLimit; myLimit--) {

            for (int i = 0; resultsIn.size() > i; i++) {

                FileScan myResult = resultsIn.get(i);

                if (myResult.doesPass() && myResult.hasQuote() && (myResult.lineCount() >= myLimit)) {

                    mySelection = myResult;
                    break;
                }
            }
            if (null != mySelection) {

                break;
            }

            for (int i = 0; resultsIn.size() > i; i++) {

                FileScan myResult = resultsIn.get(i);

                if (myResult.doesPass() && (myResult.lineCount() >= myLimit)) {

                    mySelection = myResult;
                    break;
                }
            }
            if (null != mySelection) {

                break;
            }
        }
        if (null != mySelection) {

            _delimiter = mySelection.getDelimiter();
            _quote = mySelection.getQuote();
        }
    }
}
