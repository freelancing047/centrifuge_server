package csi.client.gwt.viz.map.place.settings;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.IdentityValueProvider;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;
import com.sencha.gxt.widget.core.client.ListView;
import com.sencha.gxt.widget.core.client.form.DualListField.Mode;
import com.sencha.gxt.widget.core.client.form.validator.EmptyValidator;

import csi.client.gwt.viz.shared.CsiDualListField;
import csi.client.gwt.widget.cells.readonly.FieldDefNameCell;
import csi.server.common.model.FieldDef;
import csi.server.common.model.visualization.map.MapPlace;
import csi.server.common.model.visualization.map.MapTooltipField;

public class TooltipTab extends Composite {
	private static TooltipTabUiBinder uiBinder = GWT.create(TooltipTabUiBinder.class);

	interface TooltipTabUiBinder extends
			UiBinder<Widget, TooltipTab> {
	}

    interface FieldProperties extends PropertyAccess<FieldDef> {
        ModelKeyProvider<FieldDef> uuid();

        LabelProvider<FieldDef> fieldName();
    }

	private MapPlaceSettingsPresenter presenter;
	
	public void setPresenter(MapPlaceSettingsPresenter presenter) {
		this.presenter = presenter;
	}

    @UiField(provided = true)
    CsiDualListField<FieldDef, FieldDef> dualListField;

    private ListStore<FieldDef> availColumns;
    private ListStore<FieldDef> selectedColumns;

	public TooltipTab() {
		FieldProperties props = GWT.create(FieldProperties.class);
        availColumns = new ListStore<FieldDef>(props.uuid());
        selectedColumns = new ListStore<FieldDef>(props.uuid());
        dualListField = new CsiDualListField<FieldDef, FieldDef>(availColumns,
                selectedColumns, new IdentityValueProvider<FieldDef>(),
                new FieldDefNameCell());

        dualListField.addValidator(new EmptyValidator<List<FieldDef>>());
        dualListField.setEnableDnd(true);
        dualListField.setMode(Mode.INSERT);

        dualListField.setWidth("85%");
        ListView<FieldDef, FieldDef> fromView = dualListField.getFromView();
        ListView<FieldDef, FieldDef> toView = dualListField.getToView();
        fromView.setWidth(225);
        toView.setWidth(225);

		initWidget(uiBinder.createAndBindUi(this));
	}

	protected void clearSelection() {
		availColumns.clear();
		selectedColumns.clear();
	}

	protected void updateMapPlace() {
		MapPlace mapPlace = presenter.getMapPlace();
		List<MapTooltipField> mapTooltipFields = new ArrayList<MapTooltipField>();
        for (int i = 0; i < selectedColumns.size(); i++) {
        	MapTooltipField tooltipField = new MapTooltipField();
            tooltipField.setFieldDef(selectedColumns.get(i));
            tooltipField.setListPosition(i);
            mapTooltipFields.add(tooltipField);
        }
        mapPlace.setTooltipFields(mapTooltipFields);
	}

	protected void setSelection() {
		MapPlace mapPlace = presenter.getMapPlace();
        List<FieldDef> visibleColumns = mapPlace.getTooltipFields(presenter.getDataModel());
		Set<String> visibleFieldDefUuids = new HashSet<String>();
		for (FieldDef fieldDef : visibleColumns) {
			visibleFieldDefUuids.add(fieldDef.mapKey());
		}
        selectedColumns.clear();
        availColumns.clear();
        for (FieldDef fieldDef : presenter.getFieldDefs()) {
        	if (visibleFieldDefUuids.contains(fieldDef.mapKey())) {
        		selectedColumns.add(fieldDef);
        	} else {
                availColumns.add(fieldDef);
        	}
        }
	}
}
