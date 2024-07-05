package csi.client.gwt.viz.table;

import com.google.gwt.event.shared.GwtEvent;

import csi.client.gwt.etc.BaseCsiEvent;

public class PageEvent extends BaseCsiEvent<PageEventHandler> {

        private int activePage;
        
        public static final GwtEvent.Type<PageEventHandler> type = new GwtEvent.Type<PageEventHandler>();
        @Override
        public com.google.gwt.event.shared.GwtEvent.Type<PageEventHandler> getAssociatedType() {
            // TODO Auto-generated method stub
            return type;
        }

        @Override
        protected void dispatch(PageEventHandler handler) {
            handler.onPage(this);
        }
        
        public void setActivePage(int offset){
            this.activePage = offset;
        }
        
        public int getActivePage(){
            return activePage;
        }
    }