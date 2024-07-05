package csi.shared.gwt.viz.timeline;

import java.util.Date;

public class TimeUtils {
	
//	public static Calendar cal(long time)
//	{
//		Calendar c=new GregorianCalendar();
//		c.setTimeInMillis(time);
//		return c;
//	}
	
	public static Date toDate(long time)
	{
		Date date=new Date(time);
		return date;
	}

//	public static Calendar cal(Date date)
//	{
//		Calendar c=new GregorianCalendar();
//		c.setTime(date);
//		return c;
//	}
	
}
