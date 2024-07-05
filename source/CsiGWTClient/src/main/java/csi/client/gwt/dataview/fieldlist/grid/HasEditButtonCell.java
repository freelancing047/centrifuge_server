package csi.client.gwt.dataview.fieldlist.grid;

import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.github.gwtbootstrap.client.ui.resources.ButtonSize;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.HasCell;
import com.sencha.gxt.data.shared.ListStore;

import csi.client.gwt.dataview.fieldlist.FieldCommand;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;

/**
 * @author Centrifuge Systems, Inc.
 * Provides an Edit button cell for each row in a grid
 */
public class HasEditButtonCell implements HasCell<Boolean, String> {

    private static final CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
    private final ListStore<FieldGridModel> listStore;
    private final FieldCommand editFieldCommand;

    public HasEditButtonCell(ListStore<FieldGridModel> listStore, FieldCommand command) {
        this.listStore = listStore;
        this.editFieldCommand = command;
    }

    @Override
    public Cell<String> getCell() {
        ClickButtonCell editButtonCell = new ClickButtonCell(new ClickCellCommand() {
            @Override
            public void execute(Cell.Context context) {
                FieldGridModel model = listStore.get(context.getIndex());
                editFieldCommand.execute(model.getUuid());
            }
        });
        editButtonCell.setIcon(IconType.PENCIL);
        editButtonCell.setSize(ButtonSize.MINI);
        return editButtonCell;
    }

    @Override
    public FieldUpdater<Boolean, String> getFieldUpdater() {
        return null;
    }

    @Override
    public String getValue(Boolean object) {
        return i18n.edit();
    }
}
