package csi.client.gwt.viz.viewer.lens.field;

import com.github.gwtbootstrap.client.ui.FluidRow;
import com.github.gwtbootstrap.client.ui.ValueListBox;
import com.google.common.collect.Lists;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.ui.Composite;
import csi.client.gwt.WebMain;
import csi.client.gwt.viz.viewer.Viewer;
import csi.client.gwt.viz.viewer.lens.LensImageViewer;
import csi.client.gwt.viz.viewer.lens.shared.ExpandableItem;
import csi.client.gwt.viz.viewer.lens.shared.ListWithMore;
import csi.client.gwt.viz.viewer.lens.shared.MiniViewer;
import csi.client.gwt.widget.combo_boxes.FieldDefComboBox;
import csi.server.common.model.FieldDef;
import csi.server.common.model.visualization.viewer.Objective;
import csi.shared.gwt.viz.viewer.LensImage.FieldLensImage;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class FieldLensImageViewer extends Composite implements LensImageViewer {
    private FieldLensImage lensImage;
    private Objective objective;
    private Viewer viewer;
    private boolean isVisible;
    private ExpandableItem container;
    private String lensDef;
    private FieldDefComboBox fdcb;

    public FieldLensImageViewer(FieldLensImage _result, String lensDef, Objective objective, Viewer viewer) {
        lensImage = _result;
        this.lensDef =  lensDef;
        this.objective = objective;
        this.viewer = viewer;
        container = new MiniViewer(_result.getLabel());
        build();
        initWidget(container);
    }

    @Override
    public void setObjective(Objective objective) {
        this.objective = objective;
    }

    @Override
    public boolean handles(Objective objective) {
        return true;
    }

    @Override
    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    public void build() {
        if (container != null) {
            {
                for (Map.Entry<String, Map<String, String>> stringMapEntry : lensImage.table.rowMap().entrySet()) {
                    String key = stringMapEntry.getKey();
                    String column = "";
                    for (FieldDef fieldDef : WebMain.injector.getMainPresenter().getDataViewPresenter(true).getDataView().getMeta().getModelDef().getFieldDefs()) {
                        if (fieldDef.getUuid().equals(key)) {
                            column = fieldDef.getName();
                        }
                    }
                    FluidRow w = new FluidRow();
                    List<String> strings = Lists.newArrayList();
                    if (lensImage.fieldValueMapMap == null || lensImage.fieldValueMapMap.get(key) == null) {
                    } else {
                        strings = Lists.newArrayList(lensImage.fieldValueMapMap.get(key).keySet());
                    }

                    Map<String, String> value = stringMapEntry.getValue();
                    for (Map.Entry<String, String> stringStringEntry : value.entrySet()) {
                        try {
                            Double.parseDouble(stringStringEntry.getValue());
                            w.add(new SingleFieldView(column, value, strings, objective, lensDef));
                        } catch (NumberFormatException e) {
                            //ignore
                        }
                    }
                    container.add(w);
                }
            }

            {
                for (Map.Entry<String, Map<String, Integer>> stringMapEntry : lensImage.fieldValueMapMap.entrySet()) {

                    FluidRow w = new FluidRow();

                    ExpandableItem expandable = new ExpandableItem(lensImage.getLabel());
                    container.add(expandable);
                    expandable.add(new ListWithMore(Lists.newArrayList(stringMapEntry.getValue().keySet()), objective, lensDef, "valueField", 50, null, null, 3, null));
                }
            }

            {

                List<String> strings1 = Lists.newArrayList();
                for (FieldDef fieldDef : WebMain.injector.getMainPresenter().getDataViewPresenter(true).getDataView().getMeta().getModelDef().getFieldDefs()) {
                    strings1.add(fieldDef.getName());
                }
                ValueListBox<String> stringValueListBox = new ValueListBox<>(new Renderer <String>() {
                    @Override
                    public String render(String object) {
                        return object;
                    }

                    @Override
                    public void render(String object, Appendable appendable) throws IOException {

                    }

                });


                stringValueListBox.setAcceptableValues(strings1);
//                container.add(stringValueListBox);
            }
               //TODO:review... commented out for compile error
            {
                /*
                fdcb = new FieldDefComboBox();
                fdcb.getStore().addAll(
                        FieldDefUtils.getSortedNonStaticFields(
                                WebMain.injector.getMainPresenter().getDataViewPresenter(true).getDataView().getMeta().getModelDef(),
                                FieldDefUtils.SortOrder.ALPHABETIC));
               fdcb.addSelectionHandler(new SelectionHandler<FieldDef>() {
                    @Override
                    public void onSelection(SelectionEvent<FieldDef> event) {
                        String uuid = event.getSelectedItem().getUuid();
                        if(lensDef.localIds.contains(uuid)){
                            return;
                        }
                        lensDef.localIds.add(uuid);
                        viewer.save();

                        addSingleFieldLens(uuid);
                    }
                });
                container.add(fdcb);
                for (String localId : lensDef.localIds) {
                    addSingleFieldLens(localId);
                }*/
            }
        }
    }

    private void addSingleFieldLens(String uuid) {

        for (Map.Entry<String, Map<String, String>> stringMapEntry : lensImage.table.rowMap().entrySet()) {
            String key = stringMapEntry.getKey();
            if(!key.equals(uuid)){
                continue;

            }
            String column = "";
            for (FieldDef fieldDef : WebMain.injector.getMainPresenter().getDataViewPresenter(true).getDataView().getMeta().getModelDef().getFieldDefs()) {
                if (fieldDef.getUuid().equals(key)) {
                    column = fieldDef.getName();
                }
            }
            FluidRow w = new FluidRow();
            List<String> strings = Lists.newArrayList(lensImage.fieldValueMapMap.get(key).keySet());
            w.add(new SingleFieldView(column, stringMapEntry.getValue(), strings, objective, lensDef));
            container.add(w);
            container.insert(w,container.getWidgetIndex(fdcb));
        }
    }
}
