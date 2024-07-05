package csi.client.gwt.widget.gxt.form;

import java.util.Date;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.sencha.gxt.cell.core.client.SliderCell;


public class DateCell extends SliderCell {

    private DateTimeFormat format;    
    private long fromDateMillis;

    public DateCell(Date fromDate, DateTimeFormat format) {
        super();

        this.fromDateMillis = fromDate.getTime();
        this.format = format;
    }

    @Override
    protected String onFormatValue(int value) {
        Date formattedDate = new Date(fromDateMillis + (value * 1000L));
        return format.format(formattedDate);
    }
}
