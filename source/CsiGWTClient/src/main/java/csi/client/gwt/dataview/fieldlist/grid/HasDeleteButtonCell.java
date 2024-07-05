package csi.client.gwt.dataview.fieldlist.grid;

import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.github.gwtbootstrap.client.ui.resources.ButtonSize;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.sencha.gxt.data.shared.ListStore;

import csi.client.gwt.dataview.fieldlist.FieldCommand;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;

/**
 * @author Centrifuge Systems, Inc.
 * Provides a delete button that may or may not be rendered
 */
public class HasDeleteButtonCell implements HasCell<Boolean, String> {

    private static final CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
    private final ListStore<FieldGridModel> listStore;
    private final FieldCommand deleteFieldCommand;

    public HasDeleteButtonCell(ListStore<FieldGridModel> listStore, FieldCommand deleteFieldCommand) {
        this.listStore = listStore;
        this.deleteFieldCommand = deleteFieldCommand;
    }

    @Override
    public Cell<String> getCell() {
        ClickButtonCell deleteButtonCell = new ClickButtonCell(new ClickCellCommand() {
            @Override
            public void execute(Cell.Context context) {
                FieldGridModel model = listStore.get(context.getIndex());
                deleteFieldCommand.execute(model.getUuid());
            }
        }) {
            @Override
            public void render(Context context, SafeHtml data, SafeHtmlBuilder sb) {
                FieldGridModel model = listStore.get(context.getIndex());
                if (model.isDeletable())
                    super.render(context, data, sb);
            }
        };

        deleteButtonCell.setIcon(IconType.REMOVE);
        deleteButtonCell.setSize(ButtonSize.MINI);
        deleteButtonCell.setType(ButtonType.DANGER);
        return deleteButtonCell;
    }

    @Override
    public FieldUpdater<Boolean, String> getFieldUpdater() {
        return null;
    }

    @Override
    public String getValue(Boolean object) {
        return i18n.delete();
    }
}
