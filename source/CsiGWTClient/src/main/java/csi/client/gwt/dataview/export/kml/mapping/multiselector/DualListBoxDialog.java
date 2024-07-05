package csi.client.gwt.dataview.export.kml.mapping.multiselector;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.IdentityValueProvider;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;
import com.sencha.gxt.widget.core.client.ListView;
import com.sencha.gxt.widget.core.client.form.DualListField.Mode;
import com.sencha.gxt.widget.core.client.form.validator.EmptyValidator;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.shared.CsiDualListField;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.buttons.Button;
import csi.client.gwt.widget.cells.readonly.FieldDefNameCell;
import csi.server.common.model.FieldDef;

/**
 * Created by Patrick on 10/21/2014.
 */
public class DualListBoxDialog {
    private static DualListBoxDialogUiBinder ourUiBinder = GWT.create(DualListBoxDialogUiBinder.class);
    private final ListStore<FieldDef> availColumns;
    private final ListStore<FieldDef> selectedColumns;
    protected static final CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
//    @UiField
//    FluidRow dualListBoxRow;
    @UiField
    Dialog dialog;
//    @UiField(provided = true)
//    ListView<FieldDef, FieldDef> availableFieldDefListView;
//    @UiField(provided = true)
//    ListView<FieldDef, FieldDef> selectedFieldDefListView;
    @UiField(provided = true)
    CsiDualListField<FieldDef, FieldDef> dualListField;
    FieldDefProperties props = GWT.create(FieldDefProperties.class);
    

	@UiField(provided = true)
	String header = i18n.dualListBoxDialogHeader(); //$NON-NLS-1$
	@UiField(provided = true)
	String availableLabel = i18n.dualListBoxDialogAvailableLabel(); //$NON-NLS-1$
	@UiField(provided = true)
	String selectedLabel = i18n.dualListBoxDialogSelectedLabel(); //$NON-NLS-1$
	
	

    public DualListBoxDialog() {
        availColumns = new ListStore<FieldDef>(props.uuid());
        selectedColumns = new ListStore<FieldDef>(props.uuid());

//        availableFieldDefListView = new ListView<FieldDef, FieldDef>(availColumns, new IdentityValueProvider<FieldDef>());
//        selectedFieldDefListView = new ListView<FieldDef, FieldDef>(selectedColumns, new IdentityValueProvider<FieldDef>());
//        availableFieldDefListView.setCell(new FieldDefNameCell());
//        selectedFieldDefListView.setCell(new FieldDefNameCell());
        
//        new ListViewDragSource<FieldDef>(availableFieldDefListView).setGroup("listbox");
//        new ListViewDragSource<FieldDef>(selectedFieldDefListView).setGroup("listbox");

//        ListViewDropTarget<FieldDef> target1 = new ListViewDropTarget<FieldDef>(availableFieldDefListView);
//        target1.setFeedback(DND.Feedback.INSERT);
//        target1.setGroup("listbox");
//        target1.setAllowSelfAsSource(true);
//        ListViewDropTarget<FieldDef> target2 = new ListViewDropTarget<FieldDef>(selectedFieldDefListView);
//        target2.setFeedback(DND.Feedback.INSERT);
//        target2.setGroup("listbox");
//        target2.setAllowSelfAsSource(true);

        dualListField = new CsiDualListField<FieldDef, FieldDef>(availColumns, selectedColumns, new IdentityValueProvider<FieldDef>(), new FieldDefNameCell());

        dualListField.addValidator(new EmptyValidator<List<FieldDef>>());
        dualListField.setEnableDnd(true);
        dualListField.setMode(Mode.INSERT);

        dualListField.setWidth("100%"); //$NON-NLS-1$
        ListView<FieldDef, FieldDef> fromView = dualListField.getFromView();
        ListView<FieldDef, FieldDef> toView = dualListField.getToView();
        fromView.setWidth(185);
        toView.setWidth(180);
        
        ourUiBinder.createAndBindUi(this);
    }

    public ListStore<FieldDef> getAvailColumns() {
        return availColumns;
    }

    public List<FieldDef> getSelectedFieldDefs() {
        return selectedColumns.getAll();
    }

    public void show() {
        dialog.show();
    }

    public void addAvailableField(FieldDef fieldDef) {
        availColumns.add(fieldDef);
    }

    public void addSelectedField(FieldDef fieldDef) {
        selectedColumns.add(fieldDef);
    }

    public void close() {
        dialog.hide();
    }

    public Button getActionButton() {
        return dialog.getActionButton();
    }

    public Button getCancelButton() {
        return dialog.getCancelButton();
    }

    interface DualListBoxDialogUiBinder extends UiBinder<Widget, DualListBoxDialog> {
    }

    interface FieldDefProperties extends PropertyAccess<FieldDef> {
        ModelKeyProvider<FieldDef> uuid();

        LabelProvider<FieldDef> fieldName();
    }
}

