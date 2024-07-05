package csi.client.gwt.viz.timeline.model;

import java.util.Date;

public class TimeScale {
	
	private double low,high;
	private Interval interval;
	
	public TimeScale()
	{
		low=0;
		high=100;
		interval=new Interval(new Date(0).getTime(),new Date().getTime());
	}
	
	public Interval getInterval()
	{
		return interval;
	}
	
	public void setNumberRange(double low, double high)
	{
		this.low=low;
		this.high=high;		
	}
	
	public void setDateRange(Interval t)
	{
		setDateRange(t.start, t.end);
	}
	
	public void setDateRange(long first, long last)
	{	
		interval.setTo(first, last);
	}

	
	public boolean containsDate(long date)
	{
		return interval.contains(date);
	}
	
	public boolean containsNum(double x)
	{
		return x>=low && x<=high;
	}
	
	public long duration()
	{
		return interval.length();
	}
	
	public double toNum(long time)
	{
	    if(duration() == 0){
	        return 0;
	    }
		return low+(high-low)*(time-interval.start)/(double)duration();
	}
	
	public long spaceToTime(double space)
	{
	    if(high == low){
	        return 0;
	    }
		return (long)(space*duration()/(high-low));
	}
	
	public int toInt(long time)
	{
		return (int)toNum(time);
	}


	public long toTime(double num)
	{
        if(high == low){
            return 0;
        }
		double millis=(double)interval.start+(double)duration()*(num-low)/(high-low);
		return (long)millis;
	}

	public double getLow() {
		return low;
	}

	public void setLow(double low) {
		this.low = low;
	}

	public double getHigh() {
		return high;
	}

	public void setHigh(double high) {
		this.high = high;
	}

	public long getEnd() {
		// TODO Auto-generated method stub
		return interval.end;
	}

	public long getStart() {
		// TODO Auto-generated method stub
		return interval.start;
	}

    public boolean contains(Interval currentInterval) {
       return (getStart() <= currentInterval.start && getEnd() >= currentInterval.end);

    }

    public TimeScale copy() {
        // TODO Auto-generated method stub
        TimeScale timeScale = new TimeScale();
        timeScale.setDateRange(interval);
        timeScale.setNumberRange(low, high);
        
        return timeScale;
    }
	
}

