package csi.server.common.dto.installed_tables;


/**
 * Created by centrifuge on 8/25/2015.
 */
public class BcpParameters extends TableParameters {

    private String _columnDelimeter;
    private String _rowDelimeter;
    private int _dataStart = 0;

    public BcpParameters() {

    }

    public void setColumnDelimeter(String columnDelimeterIn) {

        _columnDelimeter = columnDelimeterIn;
    }

    public String getColumnDelimeter() {

        return _columnDelimeter;
    }

    public void setRowDelimeter(String rowDelimeterIn) {

        _rowDelimeter = rowDelimeterIn;
    }

    public String getRowDelimeter() {

        return _rowDelimeter;
    }

    public void setDataStart(int dataStartIn) {

        _dataStart = dataStartIn;
    }

    public int getDataStart() {

        return _dataStart;
    }
}
