package csi.server.business.selection.cache;

import java.io.Serializable;

import csi.server.common.model.visualization.selection.IntegerRowsSelection;

/**
 * Holds a broadcast as rows and whether to exclude the rows.
 * @author Centrifuge Systems, Inc.
 */
public class BroadcastResult implements Serializable {

    // this is the broadcast return for when there is no broadcast
    public static final BroadcastResult NULL_BROADCAST = new BroadcastResult(new IntegerRowsSelection(), false);


    // This is the desired result for a broadcast that results in NO ROWS.
    public static final BroadcastResult EMPTY_BROADCAST_RESULT = new BroadcastResult(new IntegerRowsSelection(), true){
        @Override
        public IntegerRowsSelection getBroadcastFilter() {
            IntegerRowsSelection integerRowsSelection = new IntegerRowsSelection();
            integerRowsSelection.getSelectedItems().add(-1);
            return integerRowsSelection;
        }

    };

    private final IntegerRowsSelection selection;
    private final boolean excludeRows;

    public BroadcastResult(IntegerRowsSelection selection, boolean excludeRows) {
        this.selection = selection;
        this.excludeRows = excludeRows;
    }

    public IntegerRowsSelection getBroadcastFilter() {
        return selection;
    }

    public boolean isExcludeRows() {
        return excludeRows;
    }

    public boolean isEmpty(){
        return getBroadcastFilter().isCleared();
    }

	public BroadcastResult copy() {
		BroadcastResult myCopy = new BroadcastResult((IntegerRowsSelection) getBroadcastFilter().copy(), excludeRows);
		return myCopy;
	}
}
