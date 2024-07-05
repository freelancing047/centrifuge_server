package csi.client.gwt.viz.table;

import com.google.gwt.event.shared.EventHandler;

import csi.client.gwt.etc.BaseCsiEventHandler;

public abstract class PageEventHandler extends BaseCsiEventHandler implements EventHandler {
        
        public abstract void onPage(PageEvent event);

    }