package csi.client.gwt.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public enum BrowserEvents {

    BLUR("blur"), //$NON-NLS-1$
    CANPLAYTHROUGH("canplaythrough"), //$NON-NLS-1$
    CHANGE("change"), //$NON-NLS-1$
    CLICK("click"), //$NON-NLS-1$
    CONTEXTMENU("contextmenu"), //$NON-NLS-1$
    DBLCLICK("dblclick"), //$NON-NLS-1$
    DRAG("drag"), //$NON-NLS-1$
    DRAGEND("dragend"), //$NON-NLS-1$
    DRAGENTER("dragenter"), //$NON-NLS-1$
    DRAGLEAVE("dragleave"), //$NON-NLS-1$
    DRAGOVER("dragover"), //$NON-NLS-1$
    DRAGSTART("dragstart"), //$NON-NLS-1$
    DROP("drop"), //$NON-NLS-1$
    ENDED("ended"), //$NON-NLS-1$
    ERROR("error"), //$NON-NLS-1$
    FOCUS("focus"), //$NON-NLS-1$
    FOCUSIN("focusin"), //$NON-NLS-1$
    FOCUSOUT("focusout"), //$NON-NLS-1$
    GESTURECHANGE("gesturechange"), //$NON-NLS-1$
    GESTUREEND("gestureend"), //$NON-NLS-1$
    GESTURESTART("gesturestart"), //$NON-NLS-1$
    KEYDOWN("keydown"), //$NON-NLS-1$
    KEYPRESS("keypress"), //$NON-NLS-1$
    KEYUP("keyup"), //$NON-NLS-1$
    LOAD("load"), //$NON-NLS-1$
    LOADEDMETADATA("loadedmetadata"), //$NON-NLS-1$
    LOSECAPTURE("losecapture"), //$NON-NLS-1$
    MOUSEDOWN("mousedown"), //$NON-NLS-1$
    MOUSEMOVE("mousemove"), //$NON-NLS-1$
    MOUSEOUT("mouseout"), //$NON-NLS-1$
    MOUSEOVER("mouseover"), //$NON-NLS-1$
    MOUSEUP("mouseup"), //$NON-NLS-1$
    MOUSEWHEEL("mousewheel"), //$NON-NLS-1$
    PROGRESS("progress"), //$NON-NLS-1$
    SCROLL("scroll"), //$NON-NLS-1$
    TOUCHCANCEL("touchcancel"), //$NON-NLS-1$
    TOUCHEND("touchend"), //$NON-NLS-1$
    TOUCHMOVE("touchmove"), //$NON-NLS-1$
    TOUCHSTART("touchstart"), //$NON-NLS-1$
    UNSUPPORTED("unsupported"); //$NON-NLS-1$


    private String _key;
    private static List<BrowserEvents> sortedDataTypes = new ArrayList<BrowserEvents>();

    private BrowserEvents(String keyIn) {
        _key = keyIn;
    }

    public String getKey() {
        return _key;
    }

    private static Map<String, BrowserEvents> codeToEnumMapping = new HashMap<String, BrowserEvents>();

    static {
        for (BrowserEvents e : values()) {
            codeToEnumMapping.put(e._key, e);
            sortedDataTypes.add(e);
        }
        Collections.sort(sortedDataTypes, new Comparator<BrowserEvents>() {

            @Override
            public int compare(BrowserEvents o1, BrowserEvents o2) {
                return o1.getKey().compareTo(o2.getKey());
    }
        });
    }

    public static BrowserEvents getValue(String s) {
        if (s == null) {
            return null;
        }

        BrowserEvents type = codeToEnumMapping.get(s.toLowerCase());

        if (type == null) {
            type = BrowserEvents.UNSUPPORTED;
        }

        return type;
    }

    public static String toString(BrowserEvents type) {
        if (type == null) {
            return ""; //$NON-NLS-1$
        }
        return type.getKey();
    }

    public static List<BrowserEvents> sortedValuesByLabel() {
        return sortedDataTypes;
    }
}
