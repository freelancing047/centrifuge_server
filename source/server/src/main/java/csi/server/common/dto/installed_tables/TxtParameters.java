package csi.server.common.dto.installed_tables;


/**
 * Created by centrifuge on 8/25/2015.
 */
public class TxtParameters extends TableParameters {

    private Integer _delimiter = null;
    private String _null;
    private int _dataStart = 0;

    public TxtParameters() {

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

    public void setNullIndicator(String nullIn) {

        _null = nullIn;
    }

    public String getNullIndicator() {

        return _null;
    }

    public void setDataStart(int dataStartIn) {

        _dataStart = dataStartIn;
    }

    public int getDataStart() {

        return _dataStart;
    }
}
