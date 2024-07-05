package csi.client.gwt.events;

import java.util.Map;
import java.util.TreeMap;

import com.google.gwt.event.shared.GwtEvent;

/**
 * 
 * This class is designed for the generic message bus. It has a CsiEventHeader to determine which
 * channel the event is fired on The event pay load/body is a sting:string map. If another pay load
 * is needed it should be implemented as a subclass the CsiEvent.
 * 
 */
public class CsiEvent extends GwtEvent<CsiEventHandler> {

    private CsiEventHeader header;
    private TreeMap<String, String> payload;

    public static final Type<CsiEventHandler> TYPE = new Type<CsiEventHandler>();


    @Override
    protected void dispatch(CsiEventHandler handler) {
        handler.onCsiEvent(this);
    }


    public CsiEvent(Map<String, String> _header, Map<String, String> _payload) {
        this(new CsiEventHeader(_header), _payload);
    }


    public CsiEvent(CsiEventHeader _header, Map<String, String> _payload) {
        setHeader(_header);
        if (_payload != null) {
            setPayload(new TreeMap<String, String>(_payload));
        }
    }


    public CsiEvent() {
    }


    public CsiEvent(CsiEventHeader eventHeader) {
        setHeader(eventHeader);
    }


    public void fire() {
        CsiEventCommander.getInstance().fireEvent(header, this);
    }


    public CsiEventHeader getHeader() {
        return header;
    }


    public void setHeader(CsiEventHeader header) {
        this.header = header;
    }


    public TreeMap<String, String> getPayload() {
        return payload;
    }


    public void setPayload(TreeMap<String, String> payload) {
        this.payload = payload;
    }


    @Override
    public Type<CsiEventHandler> getAssociatedType() {
        return TYPE;
    }


    public static Type<CsiEventHandler> getType() {
        return TYPE;
    }
}
