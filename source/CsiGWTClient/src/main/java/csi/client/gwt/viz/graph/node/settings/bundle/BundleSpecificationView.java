package csi.client.gwt.viz.graph.node.settings.bundle;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.FluidRow;
import com.google.common.base.Strings;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.util.FieldDefUtils;
import csi.client.gwt.viz.graph.node.settings.NodeSettings;
import csi.client.gwt.widget.combo_boxes.FieldDefComboBox;
import csi.server.common.model.DataModelDef;
import csi.server.common.model.FieldDef;

/**
 * This class presents a bundle specification, and relays user events back to presenter.
 */
public class BundleSpecificationView implements IsWidget {
    static CentrifugeConstants constants = CentrifugeConstantsLocator.get();
    private static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);
    private final NodeSettings nodeSettings;
    private final NodeBundle nodeBundle;
    @UiField
    Button remove;
    @UiField
    InlineLabel label;
    FieldDef groupTheseValues;
    @UiField
    FieldDefComboBox groupByFieldDefComboBox;
    @UiField
    FluidRow rowThatWrapsEverything;
    private BundleSpecificationPresenter presenter;

    public BundleSpecificationView(final NodeBundle nodeBundle, NodeSettings nodeSettings, final FieldDef groupTheseValues) {
        checkNotNull(nodeSettings);
        checkNotNull(nodeBundle);
        checkNotNull(groupTheseValues);
        this.nodeSettings = nodeSettings;
        this.groupTheseValues = groupTheseValues;
        this.nodeBundle = nodeBundle;
        uiBinder.createAndBindUi(this);
        initializeUiElements();
    }

    private void initializeUiElements() {
        initializeFieldDefComboBox();
        initializeLabel();
        initializeRemove();
    }

    private void initializeRemove() {
        if (nodeBundle.getField() == null) {
            remove.setVisible(false);
        }
    }

    private void initializeLabel() {
        label.setText(constants.group_x_by(groupTheseValues.getFieldName()));
    }

    private void initializeFieldDefComboBox() {
        populateFieldDefComboBox();
        groupByFieldDefComboBox.setValue(nodeBundle.getField());
    }

    private void populateFieldDefComboBox() {
        DataModelDef modelDef = nodeSettings.getGraphSettings().getDataViewDef().getModelDef();
        List<FieldDef> fieldDefs = FieldDefUtils.getAllSortedFields(modelDef, FieldDefUtils.SortOrder.ALPHABETIC);
        for (FieldDef fieldDef : fieldDefs) {
            String fieldName = fieldDef.getFieldName();
            if (!Strings.isNullOrEmpty(fieldName) && !(null == fieldDef.getValueType())) {
                groupByFieldDefComboBox.getStore().add(fieldDef);
            }
        }
    }


    @UiHandler("remove")
    public void onRemove(ClickEvent event) {
        presenter.remove(nodeBundle);
    }
    
    @UiHandler("groupByFieldDefComboBox")
    public void onFieldChange(SelectionEvent<FieldDef> event) {
        nodeBundle.setField(event.getSelectedItem());
        presenter.fieldChange(nodeBundle);
    }

    @Override
    public Widget asWidget() {
        return rowThatWrapsEverything;
    }

    public void setPresenter(BundleSpecificationPresenter presenter) {
        this.presenter = presenter;
    }

    interface SpecificUiBinder extends UiBinder<Widget, BundleSpecificationView> {
    }
}
