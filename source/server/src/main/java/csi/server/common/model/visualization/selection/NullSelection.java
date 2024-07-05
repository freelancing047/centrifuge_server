package csi.server.common.model.visualization.selection;

import java.io.Serializable;

/**
 * @author Centrifuge Systems, Inc.
 */
public class NullSelection implements Selection, Serializable {

    public static NullSelection instance = new NullSelection();

    protected NullSelection() {
        
    }
    
    @Override
    public boolean isCleared() {
        return true;
    }

    @Override
    public void clearSelection() {
    }

    @Override
    public void setFromSelection(Selection selection) {

    }

	@Override
	public Selection copy() {
		// TODO Auto-generated method stub
		return instance;
	}

}
