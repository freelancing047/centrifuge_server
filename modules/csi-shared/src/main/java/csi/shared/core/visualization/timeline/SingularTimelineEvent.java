package csi.shared.core.visualization.timeline;

import com.google.gwt.user.client.rpc.IsSerializable;

public class SingularTimelineEvent extends BaseTimelineEvent implements IsSerializable{
	
	private int rowId;
	private String label;
	
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}


    public int getRowId() {
        return rowId;
    }

    public void setRowId(int rowId) {
        this.rowId = rowId;
    }

    @Override
    public void combine(BaseTimelineEvent event) {
        //no-op
    }

    

}
