package csi.server.common.dto.installed_tables;


/**
 * Created by centrifuge on 8/25/2015.
 */
public class CsvParameters extends TableParameters {

    private Integer _quote = null;
    private Integer _delimiter = null;
    private Integer _escape = null;
    private String _nullIndicator;
    private int _dataStart = 0;

    public CsvParameters() {

        super();
    }

    public void setQuote(Character quoteIn) {

        _quote = (null != quoteIn) ? (int)quoteIn.charValue() : null;
    }

    public void setQuote(Integer quoteIn) {

        _quote = quoteIn;
    }

    public Integer getQuote() {

        return _quote;
    }

    public void setDelimiter(Character delimiterIn) {

        _delimiter = (null != delimiterIn) ? (int)delimiterIn.charValue() : null;
    }

    public void setDelimiter(Integer delimiterIn) {

        _delimiter = delimiterIn;
    }

    public Integer getDelimiter() {

        return _delimiter;
    }

    public void setEscape(Character escapeIn) {

        _escape = (null != escapeIn) ? (int)escapeIn.charValue() : null;
    }

    public void setEscape(Integer escapeIn) {

        _escape = escapeIn;
    }

    public Integer getEscape() {

        return _escape;
    }

    public void setNullIndicator(String nullIn) {

        _nullIndicator = nullIn;
    }

    public String getNullIndicator() {

        return _nullIndicator;
    }

    public void setDataStart(int dataStartIn) {

        _dataStart = dataStartIn;
    }

    public int getDataStart() {

        return _dataStart;
    }
}
