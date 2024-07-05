package csi.shared.gwt.viz.timeline;

import java.io.Serializable;
import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

public class TimeUnit implements IsSerializable, Serializable{

	public static final int MIN_TICS = 5;
	public static final double BAR_MIN_WIDTH = 5D;
	public static final int MAX_TICS = 60;
	private static final int CALENDAR_YEAR = 0;
	private static final int CALENDAR_MONTH= 1;
	private static final int CALENDAR_WEEK_OF_YEAR = 2;
	private static final int CALENDAR_DAY_OF_MONTH = 3;
	private static final int CALENDAR_DAY_OF_WEEK = 4;
	private static final int CALENDAR_HOUR_OF_DAY = 5;
	private static final int CALENDAR_MINUTE = 6;
	private static final int CALENDAR_SECOND = 7;
	private static final int CALENDAR_DAY_OF_YEAR = 8;
    private static final int CALENDAR_WEEK_OF_MONTH = 9;
    private static final int CALENDAR_MILLISECOND = 10;
    private static final int CALENDAR_QUARTER_MILLISECOND = 11;
    private static final double TIME_PADDING = .05;


	public static final TimeUnit YEAR=new TimeUnit("Years", CALENDAR_YEAR, 365*24*60*60*1000L, "yyyy", "yyyy");
	public static final TimeUnit MONTH=new TimeUnit("Months", CALENDAR_MONTH, 30*24*60*60*1000L, "MMM", "MMM yyyy");
	//Don't use this, causes issues
	public static final TimeUnit WEEK=new TimeUnit("Weeks", CALENDAR_WEEK_OF_YEAR, 7*24*60*60*1000L, "d", "MMM d yyyy");
	public static final TimeUnit DAY=new TimeUnit("Days", CALENDAR_DAY_OF_MONTH, 24*60*60*1000L, "d", "MMM d yyyy");
	//Don't use this, causes issues
	public static final TimeUnit DAY_OF_WEEK=new TimeUnit("Days", CALENDAR_DAY_OF_WEEK, 24*60*60*1000L, "d", "MMM d yyyy");
	public static final TimeUnit HOUR=new TimeUnit("Hours", CALENDAR_HOUR_OF_DAY, 60*60*1000L, "kk:mm", "MMM d yyyy kk:mm");
	public static final TimeUnit MINUTE=new TimeUnit("Minutes", CALENDAR_MINUTE, 60*1000L, "kk:mm", "MMM d yyyy kk:mm");
    public static final TimeUnit SECOND=new TimeUnit("Seconds", CALENDAR_SECOND, 1000L, "kk:mm:ss", "MMM d yyyy kk:mm:ss");
    public static final TimeUnit MILLISECOND=new TimeUnit("Milliseconds", CALENDAR_MILLISECOND, 1L, "ss.SSS", "MMM d yyyy kk:mm:ss.SSS");
    public static final TimeUnit QUARTER_MILLISECOND=new TimeUnit("Quarter Milliseconds", CALENDAR_QUARTER_MILLISECOND, 25L, "ss.SSS", "MMM d yyyy kk:mm:ss.SSS");
	public static final TimeUnit DECADE=multipleYears(10);
	public static final TimeUnit CENTURY=multipleYears(100);
	
	private static final double DAY_SIZE=24*60*60*1000L;
	
	private int quantity;	
	private long roughSize;
	private String format, fullFormat;
	private String name;
	private int calendarCode;
	
	private TimeUnit()
	{		
	}
	
	private TimeUnit(String name, int calendarCode, long roughSize, String formatPattern, String fullFormatPattern)
	{
		this.name=name;
		this.calendarCode=calendarCode;
		this.roughSize=roughSize;
		format= formatPattern;
		fullFormat= fullFormatPattern;
		quantity=1;
	}
	
	public String toString()
	{
		return "[TimeUnit: "+name+"]";
	}

	public static TimeUnit multipleYears(int numYears)
	{
		TimeUnit t=new TimeUnit();
		t.name=numYears+" Years";
		t.calendarCode=CALENDAR_YEAR;
		t.roughSize=YEAR.roughSize*numYears;
		t.format=YEAR.format;
		t.fullFormat=YEAR.fullFormat;
		t.quantity=numYears;
		return t;
	}
	
	public static TimeUnit multipleWeeks(int num)
	{
		TimeUnit t=new TimeUnit();
		t.name=num+" Weeks";
		t.calendarCode=CALENDAR_WEEK_OF_YEAR;
		t.roughSize=WEEK.roughSize*num;
		t.format=WEEK.format;
		t.fullFormat=WEEK.fullFormat;
		t.quantity=num;
		return t;
	}
	
	public TimeUnit times(int quantity)
	{
		TimeUnit t=new TimeUnit();
		t.name=quantity+" "+this.name;
		t.calendarCode=this.calendarCode;
		t.roughSize=this.roughSize*quantity;
		t.format=this.format;
		t.fullFormat=this.fullFormat;
		t.quantity=quantity;
		return t;
		
	}

	
	public int numUnitsIn(TimeUnit u)
	{
	    if(getRoughSize() == 0){
	        return 0;
	    }
		return (int)Math.round(u.getRoughSize()/(double)getRoughSize());
	}
	
	public boolean isDayOrLess()
	{
		return roughSize <= 24*60*60*1000L;
	}
	
	public Long roundDown(long timestamp)
	{
		return round(timestamp, false);
	}
	
	public Long roundUp(long timestamp)
	{
		return round(timestamp, true);
	}
	
	private static final int[] calendarUnits={CALENDAR_MILLISECOND, CALENDAR_QUARTER_MILLISECOND, CALENDAR_SECOND, CALENDAR_MINUTE, CALENDAR_HOUR_OF_DAY, CALENDAR_DAY_OF_MONTH, CALENDAR_MONTH, CALENDAR_YEAR};
  
	@SuppressWarnings("deprecation")
	public Long round(Long timestamp, boolean up)
	{
		Date date=TimeUtils.toDate(timestamp);
		
		if (calendarCode==CALENDAR_WEEK_OF_YEAR )
		{
			//Resets the date to the first day of its given week.
			date.setDate(date.getDate() - date.getDay());
			//c.set(CALENDAR_DAY_OF_WEEK, c.getMinimum(CALENDAR_DAY_OF_WEEK));
		}
		else
		{
		
			// set to minimum all fields of finer granularity.
			int roundingCode=calendarCode;
			if (calendarCode==CALENDAR_WEEK_OF_YEAR || calendarCode==CALENDAR_DAY_OF_WEEK)
				roundingCode=CALENDAR_DAY_OF_MONTH;
			for (int i=0; i<calendarUnits.length; i++)
			{
				if (calendarUnits[i]==roundingCode)
					break;
				if (i==calendarUnits.length-1)
					throw new IllegalArgumentException("Unsupported Calendar Unit: "+calendarCode);
				
				minimum(calendarUnits[i], date);
			}
			if (quantity>1)
			{
				//Round down to the closest quantity(usually 10/100 years)
				double dateRatio = date.getYear()/((double)quantity);
				int dateCount = (int) Math.floor(dateRatio);
                date.setYear(dateCount*quantity);
			}
		}
		
		// if rounding up, then add a unit at current granularity.
		if (up){
	        add(calendarCode, date, quantity);
		}
		
		return new Long(date.getTime());
	}
	
	@SuppressWarnings("deprecation")
	private void minimum(int ii, Date date) {
		if(ii == CALENDAR_SECOND){
			date.setSeconds(0);
		} else if(ii == CALENDAR_MINUTE){
			date.setMinutes(0);
		} else if(ii == CALENDAR_HOUR_OF_DAY){
			date.setHours(0);
		} else if(ii == CALENDAR_DAY_OF_MONTH){
			date.setDate(1);
		} else if(ii == CALENDAR_MONTH){
			date.setMonth(0);
		} else if(ii == CALENDAR_YEAR){
			date.setYear(1);
		} else if(ii == CALENDAR_MILLISECOND){
		    Long diff = date.getTime() % 1000L;
            date.setTime(date.getTime() - diff);
        } else if(ii == CALENDAR_QUARTER_MILLISECOND){
            Long diff = date.getTime() % 1000L;
            date.setTime(date.getTime() - diff);
        } else {
            
        }
	}
	
	@SuppressWarnings("deprecation")
	private void add(int ii, Date date) {
		add(ii, date, 1);
	}
	
	@SuppressWarnings("deprecation")
	private void add(int ii, Date date, long times) {
		if(ii == CALENDAR_SECOND){
			date.setSeconds((int) (date.getSeconds() + times));
		} else if(ii == CALENDAR_MINUTE){
			date.setMinutes((int) (date.getMinutes() + times));
		} else if(ii == CALENDAR_HOUR_OF_DAY){
			date.setHours((int) (date.getHours() + times));
		} else if(ii == CALENDAR_DAY_OF_MONTH){
			date.setDate((int) (date.getDate() + times));
		} else if(ii == CALENDAR_MONTH){
			date.setMonth((int) (date.getMonth() + times));
		} else if(ii == CALENDAR_YEAR){
			date.setYear((int) (date.getYear() + times));
        } else if(ii == CALENDAR_MILLISECOND){
            date.setTime(date.getTime() + times);
        } else if(ii == CALENDAR_QUARTER_MILLISECOND){
            date.setTime(date.getTime() + times * 25L);
		} else {
		    
		}
	}
	
	public int getCodeValue(int calendarCode, Date date){
		int n;
		if(calendarCode == CALENDAR_SECOND){
			n = date.getSeconds();
		} else if(calendarCode == CALENDAR_MINUTE){
			n = date.getMinutes();
		} else if(calendarCode == CALENDAR_HOUR_OF_DAY){
			n = date.getHours();
		} else if(calendarCode == CALENDAR_DAY_OF_MONTH){
			n = date.getDate();
		} else if(calendarCode == CALENDAR_MONTH){
			n = date.getMonth();
		} else if(calendarCode == CALENDAR_YEAR){
			n = date.getYear();
		} else if(calendarCode == CALENDAR_MILLISECOND){
            n = (int) (date.getTime() % 1000L);
        }  else if(calendarCode == CALENDAR_QUARTER_MILLISECOND){
            n = (int) (date.getTime() % 1000L);
        } else {
			n = 0;
		}
		
		return n;
	}

	public int get(long timestamp)
	{
		Date date = new Date(timestamp);
		int n;
		if(calendarCode == CALENDAR_SECOND){
			n = date.getSeconds();
		} else if(calendarCode == CALENDAR_MINUTE){
			n = date.getMinutes();
		} else if(calendarCode == CALENDAR_HOUR_OF_DAY){
			n = date.getHours();
		} else if(calendarCode == CALENDAR_DAY_OF_MONTH){
			n = date.getDate();
		} else if(calendarCode == CALENDAR_MONTH){
			n = date.getMonth();
		} else if(calendarCode == CALENDAR_YEAR){
			n = date.getYear();
		} else if(calendarCode == CALENDAR_MILLISECOND){
            n = (int) (date.getTime() % 1000L);
        } else if(calendarCode == CALENDAR_QUARTER_MILLISECOND){
            n = (int) (date.getTime() % 1000L);
        } else {
			n = 0;
		}
		
		return quantity==1 ? n : n%quantity;
	}
	
	public Long addTo(Long r)
	{
		return addTo(r,1);
	}
	
	public Long addTo(Long r, int times)
	{
		Date date = TimeUtils.toDate(r);
		
		add(calendarCode, date, quantity* times);
		
		//c.add(calendarCode, quantity*times);
		r = (date.getTime());
		return r;
	}
	
	// Finding the difference between two dates, in a given unit of time,
	// is much subtler than you'd think! And annoyingly, the Calendar class does not do
	// this for you, even though it actually "knows" how to do so since it
	// can add fields.
	//
	// The most vexing problem is dealing with daylight savings time,
	// which means that one day a year has 23 hours and one day has 25 hours.
	// We also have to handle the fact that months and years aren't constant lengths.
	//
	// Rather than write all this ourselves, in this code we
	// use the Calendar class to do the heavy lifting.
	public long difference(long x, long y)
	{
		// If this is not one of the hard cases,
		// just divide the timespan by the length of time unit.
		// Note that we're not worrying about hours and daylight savings time.
		if (calendarCode!=CALENDAR_YEAR && calendarCode!=CALENDAR_MONTH && 
		   calendarCode!=CALENDAR_DAY_OF_MONTH && calendarCode!=CALENDAR_DAY_OF_WEEK &&
		   calendarCode!=CALENDAR_WEEK_OF_YEAR)
		{
			return (x-y)/roughSize;
		}
			
		Date date1 = TimeUtils.toDate(x);
		Date date2 = TimeUtils.toDate(y);
		
		//Calendar c1=TimeUtils.cal(x), c2=TimeUtils.cal(y); 
		
		int diff=0;
		switch (calendarCode)
		{
			case CALENDAR_YEAR:
				return (date1.getYear()-date2.getYear())/quantity;
				
			case CALENDAR_MONTH:
				diff= 12*(date1.getYear()-date2.getYear())+
				              date1.getMonth()-date2.getMonth();
				return diff/quantity;
				
			case CALENDAR_DAY_OF_MONTH:
			case CALENDAR_DAY_OF_WEEK:
			case CALENDAR_DAY_OF_YEAR:
			case CALENDAR_WEEK_OF_MONTH:
            case CALENDAR_WEEK_OF_YEAR:
				// This is ugly, but believe me, it beats the alternative methods :-)
				// We use the Calendar class's knowledge of daylight savings time.
				// and also the fact that if we calculate this naively, then we aren't going
				// to be off by more than one in either direction.
				int naive=(int)Math.round((x-y)/(double)roughSize);
				
				add(calendarCode, date2, naive*quantity);
				
				if (getCodeValue(calendarCode, date1)==getCodeValue(calendarCode, date2)){
					return naive/quantity;
				}
				
				add(calendarCode, date2, quantity);
				
				if (getCodeValue(calendarCode, date1)==getCodeValue(calendarCode, date2)){
					return naive/quantity+1;
				}
				
				return naive/quantity-1;
		}
		throw new IllegalArgumentException("Unexpected calendar code: "+calendarCode);
	}

	public long approxNumInRange(long start, long end)
	{
		return 1+(end-start)/roughSize;
	}
	
	public long getRoughSize() {
		return roughSize;
	}

	

	public String getName() {
		return name;
	}
	
	public static TimeUnit next(TimeUnit unit){
	    if(unit.equals(TimeUnit.MILLISECOND)){
	        return SECOND;
	    } else if(unit.equals(TimeUnit.SECOND)){
            return MINUTE;
        } else if(unit.equals(TimeUnit.MINUTE)){
            return HOUR;
        } else if(unit.equals(TimeUnit.HOUR)){
            return DAY;
        } else if(unit.equals(TimeUnit.DAY)){
            return MONTH;
        } else if(unit.equals(TimeUnit.MONTH)){
            return YEAR;
        } else if(unit.equals(TimeUnit.YEAR)){
            return DECADE;
        } else if(unit.equals(TimeUnit.DECADE)){
            return CENTURY;
        } else {
            return multipleYears((int) (unit.getRoughSize()/(TimeUnit.YEAR.getRoughSize()) * 100));
        }
	}
	
	public static TimeUnit last(TimeUnit unit){
        if(unit.equals(TimeUnit.MILLISECOND)){
            return MILLISECOND;
        } else if(unit.equals(TimeUnit.SECOND)){
            return MILLISECOND;
        } else if(unit.equals(TimeUnit.MINUTE)){
            return SECOND;
        } else if(unit.equals(TimeUnit.HOUR)){
            return MINUTE;
        } else if(unit.equals(TimeUnit.DAY)){
            return HOUR;
        } else if(unit.equals(TimeUnit.MONTH)){
            return DAY;
        } else if(unit.equals(TimeUnit.YEAR)){
            return MONTH;
        } else if(unit.equals(TimeUnit.DECADE)){
            return YEAR;
        } else if(unit.equals(TimeUnit.CENTURY)){
            return DECADE;
        } else {
            return CENTURY;
        }
    }
	
	public static double calculateTimePadding(long duration){
	    double padTime = duration * TIME_PADDING;
        if (padTime < 1) {
            padTime = 1;
        }
        
        return padTime;
	}

    public int getCalendarCode() {
        return calendarCode;
    }

    public void setCalendarCode(int calendarCode) {
        this.calendarCode = calendarCode;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getFullFormat() {
        return fullFormat;
    }

    public void setFullFormat(String fullFormat) {
        this.fullFormat = fullFormat;
    }
}
