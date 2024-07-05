package csi.client.gwt.events;

import java.util.TreeMap;

import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.google.web.bindery.event.shared.SimpleEventBus;

/**
 * @author Patrick
 * 
 *         To send: new CsiEvent(Map<String,String>, Map<String,String>)).fire() To recieve:
 *         CsiEventCommander.addListener(new CsiHeader(Map<String,String>), new CsiEventHandler(){
 *         public void onCsiEvent(CsiEvent ce){ //dosomething } })
 * 
 */
public class CsiEventCommander {

    private TreeMap<String, EventBus> busMap;

    // Singleton Boilerplate
    private static CsiEventCommander cec;


    private CsiEventCommander() {
        busMap = new TreeMap<String, EventBus>();
    }


    public static CsiEventCommander getInstance() {
        if (cec == null) {
            cec = new CsiEventCommander();
        }
        return cec;
    }


    public HandlerRegistration addHandler(CsiEventHeader eh, CsiEventHandler _handler) {
        EventBus bus;
        bus = busMap.get(eh.toString());
        if (bus == null) {
            bus = new SimpleEventBus();
            busMap.put(eh.toString(), bus);
        }
        return bus.addHandler(CsiEvent.getType(), _handler);
    }


    public void removeHandler(CsiEventHeader eh, CsiEventHandler _handler) {
        EventBus bus;
        bus = busMap.get(eh.toString());
        if (bus == null) {
            bus = new SimpleEventBus();
            busMap.put(eh.toString(), bus);
        }
        bus.addHandler(CsiEvent.getType(), _handler);
    }


    public void fireEvent(CsiEventHeader eh, CsiEvent ce) {
        //GWT.log("Firing Event " + eh.toString());
        if (getBus(eh) != null) {
            getBus(eh).fireEvent(ce);
        }
    }


    // helper
    private EventBus getBus(CsiEventHeader eh) {
        return busMap.get(eh.toString());
    }
    /* Instead of listening for a source add source to header */
    // public void addHandlerToSource(CsiEventHeader eh, CsiEventHandler _handler, Object o){
    // EventBus bus;
    // bus = busMap.get(eh.toString());
    // if(bus==null){
    // bus = new SimpleEventBus();
    // busMap.put(eh.toString(), bus);
    // }
    // bus.addHandlerToSource(CsiEvent.getType(), o, _handler);
    // }
    //
    // public void fireEventFromSource(CsiEventHeader eh, CsiEvent ce, Object _source){
    // getBus(eh).fireEventFromSource(ce, _source);
    // }
}
