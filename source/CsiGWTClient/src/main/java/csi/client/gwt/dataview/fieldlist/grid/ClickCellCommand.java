package csi.client.gwt.dataview.fieldlist.grid;

import com.google.gwt.cell.client.Cell;

/**
 * @author Centrifuge Systems, Inc.
 * A command to invoke on a ClickButtonCell
 */
public interface ClickCellCommand {

    public void execute(Cell.Context context);
}
