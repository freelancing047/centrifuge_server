package csi.client.gwt.widget.gxt.form;

import java.util.Date;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.sencha.gxt.widget.core.client.Slider;

public class DateSlider extends Slider {

    private Date fromDate;

    public DateSlider(Date fromDate, Date toDate, DateTimeFormat format) {
        super(new DateCell(fromDate, format));

        this.fromDate = fromDate;

        long deltaInSeconds = (toDate.getTime() - fromDate.getTime()) / 1000;
        setMaxValue((int) deltaInSeconds);
        setMinValue(0);
        setValue(0);
    }

    public void setValue(Date date) {
        long deltaInSeconds = (date.getTime() - fromDate.getTime()) / 1000;
        super.setValue((int) deltaInSeconds);
    }
}
