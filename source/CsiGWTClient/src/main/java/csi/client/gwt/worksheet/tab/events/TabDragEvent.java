package csi.client.gwt.worksheet.tab.events;

import com.google.gwt.event.shared.GwtEvent;

import csi.client.gwt.etc.BaseCsiEvent;

public class TabDragEvent extends BaseCsiEvent<TabDragEventHandler> {

	 private int newIndex;
	 private int oldIndex;

	    public static final GwtEvent.Type<TabDragEventHandler> type = new GwtEvent.Type<TabDragEventHandler>();

	    @Override
	    public com.google.gwt.event.shared.GwtEvent.Type<TabDragEventHandler> getAssociatedType() {
	        return type;
	    }
	    

	    @Override
	    protected void dispatch(TabDragEventHandler handler) {
	        handler.onDrag(newIndex, oldIndex);
	    }


		public int getNewIndex() {
			return newIndex;
		}


		public void setNewIndex(int newIndex) {
			this.newIndex = newIndex;
		}


		public int getOldIndex() {
			return oldIndex;
		}


		public void setOldIndex(int oldIndex) {
			this.oldIndex = oldIndex;
		}

}
