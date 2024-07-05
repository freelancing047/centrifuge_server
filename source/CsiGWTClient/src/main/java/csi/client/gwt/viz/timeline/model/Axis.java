package csi.client.gwt.viz.timeline.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;

import csi.client.gwt.viz.timeline.events.RenderFooterEvent;
import csi.client.gwt.viz.timeline.events.TimeScaleChangeEvent;
import csi.client.gwt.widget.drawing.BaseRenderable;
import csi.client.gwt.widget.drawing.Layer;
import csi.shared.gwt.viz.timeline.TimeUnit;


public class Axis extends BaseRenderable{
	private static final int MIN_FIDELITY_TICS = 5;
    private TimeUnit unit;
	private List<Long> tics;
	private Layer layer;
	private TimeScale timeScale;
	private int x;
	private int y;
	public static int HEIGHT = 20;
	private boolean full;
	List<HandlerRegistration> handlers = new ArrayList<HandlerRegistration>();
	private EventBus eventBus;
    private int hoverPosition = -1;
	private ClickHandler clickHandler = new ClickHandler(){

        @Override
        public void onClick(ClickEvent event) {

            List<Long> tics = Axis.this.getTics();
            
            int x = event.getX();
            
            Long start = null;
            Long end = null;
            
            TimeScale timeScale = Axis.this.getTimeScale();
            Long time = timeScale.toTime(x);
            
            for(Long tic: tics){
                if(time >= tic){
                    start = tic;
                }
                
                if(time < tic){
                    end = tic;
                    break;
                }
            }
            
            TimeScaleChangeEvent changeEvent = new TimeScaleChangeEvent((timeScale.toNum(start)), (timeScale.toNum(end)));
            eventBus.fireEvent(changeEvent);
            
        }};
        
        private MouseMoveHandler hoverHandler = new MouseMoveHandler(){

            @Override
            public void onMouseMove(MouseMoveEvent event) {
                List<Long> tics = Axis.this.getTics();
                
                int x = event.getX();
                                
                TimeScale timeScale = Axis.this.getTimeScale();
                Long time = timeScale.toTime(x);
                
                for(int ii=0; ii < tics.size(); ii++){
                    long tic = tics.get(ii);
                    if(time <= tic){
                        hoverPosition = ii-1;
                        break;
                    }
//                    
                }
                
                eventBus.fireEvent(new RenderFooterEvent());
            }
        };
            
        private MouseOutHandler mouseOutHandler = new MouseOutHandler() {

            @Override
            public void onMouseOut(MouseOutEvent event) {

                hoverPosition = -1;
                eventBus.fireEvent(new RenderFooterEvent());
            }
            
        };
	
	private static final TimeUnit[] units={
		TimeUnit.YEAR, TimeUnit.MONTH, TimeUnit.DAY, TimeUnit.HOUR, TimeUnit.MINUTE, TimeUnit.SECOND, TimeUnit.QUARTER_MILLISECOND, TimeUnit.MILLISECOND 
	};
	
	private static final List<TimeUnit> smallFidelityUnits= new ArrayList<TimeUnit>(){{
	    add(TimeUnit.MINUTE);
	    add(TimeUnit.SECOND);
	    add(TimeUnit.QUARTER_MILLISECOND);
	    add(TimeUnit.MILLISECOND);
	    }};
	
	private static final TimeUnit[] histUnits={
		TimeUnit.YEAR.times(100), TimeUnit.YEAR.times(50), TimeUnit.YEAR.times(25), 
			TimeUnit.YEAR.times(10), TimeUnit.YEAR.times(5), TimeUnit.YEAR.times(2), TimeUnit.YEAR,
		TimeUnit.MONTH.times(6), TimeUnit.MONTH.times(3), TimeUnit.MONTH.times(2), TimeUnit.MONTH, 
		TimeUnit.WEEK, TimeUnit.DAY.times(2), TimeUnit.DAY,

		TimeUnit.HOUR, 
		TimeUnit.MINUTE, 
		TimeUnit.SECOND,
		TimeUnit.QUARTER_MILLISECOND,
		TimeUnit.MILLISECOND
	};
	
	public Axis(TimeUnit unit, long start, long end, int x, int y, EventBus eventBus)
	{
		this.unit=unit;
		this.x = x;
		this.y = y;
		this.eventBus = eventBus;
		tics=new ArrayList<Long>();
		Long time=unit.roundDown(start);
		tics.add(time);
		int count = 0;
		do
		{
			time = unit.addTo(time);
			tics.add(time);
			count++;
		} while (time<end && count < 90000);	
	}
	
	public void startHandlers(){
	    if(this.handlers.size() == 0) {
            handlers.add(this.addClickHandler(clickHandler));
            handlers.add(this.addMouseMoveHandler(hoverHandler));
            handlers.add(this.addMouseOutHandler(mouseOutHandler));
	    }
	}
	
	
	public static List<Axis> allRelevant(Interval interval, int x, int y, EventBus eventBus)
	{
		return allRelevant(interval.start, interval.end, x, y, eventBus);
	}
	
	public static List<Axis> allRelevant(long start, long end, int x, int y, EventBus eventBus)
	{
		return allRelevant(start, end, TimeUnit.MAX_TICS, x, y, eventBus);
	}
	
	public static Axis histoTics(long start, long end, int x, int y, EventBus eventBus)
	{
		for (int i=histUnits.length-1; i>=0; i--)
		{
			TimeUnit u=histUnits[i];
			long estimate=u.approxNumInRange(start, end);		
			if (estimate<200 || i==0)
			{
				Axis t=new Axis(u, start, end, x, y, eventBus);
				return t;
			}
		}
		return null;
	}
	
	public static List<Axis> allRelevant(long start, long end, long maxTics, int x, int y, EventBus eventBus)
	{
		List<Axis> list=new ArrayList<Axis>();
		
		
		for (int i=0; i<units.length; i++)
		{
			TimeUnit u=units[i];
			long estimate=u.approxNumInRange(start, end);
			
			if (estimate<maxTics)
			{
				Axis t=new Axis(u, start, end, x, y, eventBus);
				if (list.size()>0)
				{
					Axis last=list.get(0);
					if (last.tics.size()==t.tics.size()){
						list.remove(0);
					}
				}
				list.add(t);
				
			}
		}
		while (list.size()>2){
			list.remove(0);
		}
		
		
		
		
		if (list.size()==0) // uh oh! must be many years. we will add in bigger increments.
		{
			long length=end-start;
			long size=365*24*60*60*1000L;
			int m=1;
			maxTics=15;
			if(m == 0 || size == 0){
			    return list;
			}
			while (m<2000000000 && length/(m*size)>maxTics)
			{
				if (length/(2*m*size)<=maxTics)
				{
					m*=2;
					break;
				}
				if (length/(5*m*size)<=maxTics)
				{
					m*=5;
					break;
				}
				m*=10;
			}	
			Axis t=new Axis(TimeUnit.multipleYears(m), start, end, x, y, eventBus);
			list.add(t);
		} else if(list.size() == 2){ //minutes, seconds, and subseconds handled a bit differently
		    Axis smallest = list.get(list.size()-1);
		    TimeUnit u = smallest.unit;
		    if(smallest.tics.size() < MIN_FIDELITY_TICS && smallFidelityUnits.contains(u)){
	            list.remove(0);
	        }
		}
		
		for(Axis axis: list){
			axis.setY(y);
			y=y+axis.HEIGHT;
		}
		
		//we move the axis down if it's the only one
//		if(list.size() == 1){
//		    Axis axis = list.get(0);
//		    axis.setY(axis.HEIGHT);
//		}
		
		return list;
	}



	private void setY(int y) {
		this.y = y;
	}



	@Override
	public void render(Context2d context2d) {
	    renderFooter(context2d);
	}
	

    private void renderFooter(Context2d context2d) {
        int n = tics.size();
		for (int i=0; i<n-1; i++)
		{
			
			long start = tics.get(i);
			long end = tics.get(i+1);
			
			int x0= Math.max(x, getTimeScale().toInt(start));
			int x1= getTimeScale().toInt(end);
			
			int dayOfWeek= new Date(start).getDay();
			
			context2d.setStrokeStyle(this.unit.isDayOrLess() && (dayOfWeek==0 || dayOfWeek==6) ? 
					CssColor.make(200,200,200) : CssColor.make(190,190,190));
			
			if(i == hoverPosition) {
                context2d.setFillStyle(this.unit.isDayOrLess() && (dayOfWeek==0 || dayOfWeek==6) ? 
                        CssColor.make(180,180,180) : CssColor.make(180,180,180));
			} else {
    			context2d.setFillStyle(this.unit.isDayOrLess() && (dayOfWeek==0 || dayOfWeek==6) ? 
    					CssColor.make(220,220,220) : CssColor.make(220,220,220));
			}
			context2d.fillRect(x0, y, x1-x0-1, HEIGHT);
			//g.fillRect(x0, y, x1-x0-1, height);
			context2d.setStrokeStyle(CssColor.make(255,255,255));
			
			context2d.beginPath();
			context2d.moveTo(x1-1, y);
			context2d.lineTo(x1-1, y+HEIGHT);
			
			//g.drawLine(x1-1, y, x1-1, y+height);

			context2d.moveTo(x0,y+HEIGHT);
			context2d.lineTo(x1,y+HEIGHT);
			//g.drawLine(x0,y+height,x1,y+height);

			context2d.stroke();
			context2d.closePath();
			
			//objectLocations.add(new Mouseover(new Interval(start,end), x0, y, x1-x0-1, h));
			
			//g.setFont(model.getDisplay().timeLabel());
			context2d.setFont("normal bold 9px Verdana");
			
			String label=full? formatFull(start, DateTimeFormat.getFormat(this.unit.getFullFormat())) : format(new Date(start), DateTimeFormat.getFormat(this.unit.getFormat()));
			int tx=x0+3;
			int ty=y+HEIGHT-5;
			

			context2d.setFillStyle(full ? CssColor.make(60,60,60) : CssColor.make(100,100,100));
			//g.setColor(full ? Color.darkGray : Color.gray);
			
			int sw= (int) context2d.measureText(label).getWidth();
			if (sw<x1-tx-3){

				context2d.fillText(label, tx,ty);
				//g.drawString(label, tx,ty);
			} else {
				int c=label.indexOf(':');
				if (c>0)
				{
					label=label.substring(0,c);
					sw= (int) context2d.measureText(label).getWidth();
					if (sw<x1-tx-3){

						context2d.fillText(label, tx,ty);
						//g.drawString(label, tx,ty);
					}
				}
			}
		}
    }



	public TimeScale getTimeScale() {
		return timeScale;
	}
	
	public void setTimeScale(TimeScale timeScale){
		this.timeScale = timeScale;
	}



	@Override
	public boolean hitTest(double x, double y) {
	           
        if(x >= 0 && x <= getTimeScale().toInt(getTimeScale().getEnd())){
            if(y >= this.y && y <= this.y + HEIGHT){
                return true;
            }
        }
	    
		return false;
	}



	@Override
	public void bind(Layer layer) {
		this.setLayer(layer);
	}



	@Override
	public boolean isDirty() {
		return false;
	}



	public Layer getLayer() {
		return layer;
	}



	public void setLayer(Layer layer) {
		this.layer = layer;
	}



	public void deregisterHandlers(){
        if(handlers == null){
            return;
        }
        
        for(HandlerRegistration handlerRegistration: handlers){
            handlerRegistration.removeHandler();
        }
    }



    public List<Long> getTics() {
        return tics;
    }



    public void setTics(List<Long> tics) {
        this.tics = tics;
    }

    public TimeUnit getUnit() {
        return unit;
    }

    public void setUnit(TimeUnit unit) {
        this.unit = unit;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public static TimeUnit[] getUnits() {
        return units;
    }
    
    public String format(Date date, DateTimeFormat format)
    {
        return format.format(date);
    }

    public String formatFull(Date date, DateTimeFormat fullFormat)
    {
        return fullFormat.format(date);
    }

    public String formatFull(long timestamp, DateTimeFormat fullFormat)
    {
        return fullFormat.format(new Date(timestamp));
    }
}