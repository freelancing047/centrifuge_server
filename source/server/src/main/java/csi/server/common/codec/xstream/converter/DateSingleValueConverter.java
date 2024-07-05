package csi.server.common.codec.xstream.converter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.thoughtworks.xstream.converters.SingleValueConverter;

public class DateSingleValueConverter implements SingleValueConverter {
   private static final Logger LOG = LogManager.getLogger(DateSingleValueConverter.class);

    private SimpleDateFormat flexFormat;

    public DateSingleValueConverter() {
        flexFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSz");
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean canConvert(Class type) {
        return Date.class.isAssignableFrom(type);
    }

    @Override
    /*
     * Convert a date string to Date.
     * We prefer milliseconds since the epoch, but
     * Flex XML marshaling renders dates
     * in xsd:datetime format,  e.g.,  2008-12-12T22:21:07.593Z
     */
    public Object fromString(String dateStr) {
        Date date;

        if (dateStr.length() == 24 && dateStr.charAt(10) == 'T' && dateStr.charAt(23) == 'Z') {
            date = xsdDateTime(dateStr);
        } else {
            date = dateFromMilliString(dateStr);
        }
        return date;
    }

    /*
     * Example of xsd:datetime format
     * 2008-12-12T22:21:07.593Z
     */
    private Date xsdDateTime(String xsdDateTimeStr) {
        Date date;

        try {
            // FIXME: isn't there any way to get Java to handle
            // the "Z" timezone string?
            String gmtStr = xsdDateTimeStr.substring(0, 23) + "GMT";
            date = flexFormat.parse(gmtStr);
        } catch (ParseException pex) {
            LOG.info(String.format("error parsing date string '%s'", xsdDateTimeStr));
            date = null;
        }
        return date;
    }

    private Date dateFromMilliString(String milliStr) {
        Date date;

        if (milliStr == null) {
            date = null;
        } else if (milliStr.equalsIgnoreCase("NaN")) {
            // TODO: hack till client stops sending NaN
            date = null;
        } else {
            String trimStr = milliStr.trim();
            try {
                date = new Date(Long.parseLong(trimStr));
            } catch (NumberFormatException nfex) {
                LOG.info(String.format("unable to parse date from milliSec string '%s'", milliStr));
                date = null;
            }
        }

        return date;
    }

    @Override
    public String toString(Object date) {
        String milliStr = (date == null) ? "" : Long.toString(((Date) date).getTime());

        return milliStr;
    }

}
