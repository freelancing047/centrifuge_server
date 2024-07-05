package csi.client.gwt.viz.shared;

import com.github.gwtbootstrap.client.ui.Alert;
import com.github.gwtbootstrap.client.ui.base.AlertBase;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.AttachEvent.Handler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Panel;
import com.sencha.gxt.core.client.dom.XDOM;

public class BroadcastAlert extends Alert {

	private static final int CLOSE_TIME = 4000;
	private static final int FADE_IN_TIME = 50;
	private static final int FADE_OUT_TIME = 3000;
	public static final int DEFAULT_WIDTH = 184;

	public BroadcastAlert(String text){
		super(text);
		Style style = getElement().getStyle();
		style.setZIndex(XDOM.getTopZIndex());
		style.setRight(BroadcastAlert.DEFAULT_WIDTH, Unit.PX);
		style.setPosition(Position.ABSOLUTE);
		addStyleName("broadcast-alert");
		addStyleName("broadcast-alert-out");
		setAnimation(false);
		fadeInTimer();
		closeTimer();
		
		//Some visualizations add an extra div when they attach,
		//the div has 100% height and can block functionality like scrolling,
		//this resizes it.
		this.addAttachHandler(new Handler(){
            @Override
            public void onAttachOrDetach(AttachEvent event) {
                if(event.isAttached()){
                    try{
                        
                        Element parent = getElement().getParentElement();
                        if(parent.getChildCount() == 1){
                            parent.addClassName("broadcast-alert-div");
                        }
                        
                    }
                    catch(NullPointerException e){
                        
                    }
                }
            }});
		
		//Adding this functionality to a closeHandler as well, apparently
		//When closing with the icon, it does not use the close method.
		this.addCloseHandler(new CloseHandler<AlertBase>(){

            @Override
            public void onClose(CloseEvent<AlertBase> event) {
                try{
                    Panel panel = ((Panel)getParent());
                    if(panel != null)
                        panel.remove(BroadcastAlert.this);
                } catch(Exception castException){
                    //This means it's not a panel, no special remove needed
                    removeFromParent();
                }
            }});
	}
	
	/**
	 * This timer is used to fade the broadcast in. Set on a slight delay to ensure
	 * the broadcast has been added to a parent.
	 * 
	 */
	private void fadeInTimer() {
		Timer t = new Timer() {
			public void run() {
				if(BroadcastAlert.this != null){
					//LayoutPanels are causing me nightmares
					try{
						if(getParent() != null && getParent() instanceof LayoutPanel)
							getElement().getStyle().clearLeft();
							getElement().getStyle().setRight(0, Unit.PX);
							getElement().getStyle().setWidth(DEFAULT_WIDTH, Unit.PX);
//							((LayoutPanel)getParent()).setWidgetRightWidth(BroadcastAlert.this, 0, Unit.PX, DEFAULT_WIDTH, Unit.PX);
					
					} catch(Exception exception){
						//Probably not a layout panel, other css styles should be sufficient 
						//if that is the case.
					}
					removeStyleName("broadcast-alert-out");
					addStyleName("broadcast-alert-in");		
				}
			}

		};
		t.schedule(FADE_IN_TIME);
	}

	/**
	 * closeTimer() is set on initialization and controls how long the broadcast is
	 * visible.
	 * 
	 */
	private void closeTimer(){
		Timer t = new Timer() {
			public void run() {
				if(BroadcastAlert.this != null){
					removeStyleName("broadcast-alert-in");
					addStyleName("broadcast-alert-out");
					fadeOutTimer();
					
				}
			}

		};
		
		t.schedule(CLOSE_TIME);
	}
	
	/**
	 *  fadeOutTimer() waits so that the broadcast fully fades before calling close.
	 * 
	 */
	private void fadeOutTimer(){
		Timer t = new Timer() {
			public void run(){
				close();
			}
		};
		
		t.schedule(FADE_OUT_TIME);
	}

	/**
	 * close is overriden because panels leave HTML elements if you use the
	 * removeFromParent(). So a check is made first to see if we can remove
	 * from panel.
	 * 
	 */
	@Override
	public void close(){
	    try{
            ((Panel)getParent()).remove(BroadcastAlert.this);
        } catch(Exception castException){
            //This means it's not a panel, no special remove needed
            removeFromParent();
        }
		super.close();
	}
}
