package csi.client.gwt.widget.combo_boxes;

import com.sencha.gxt.cell.core.client.form.ComboBoxCell.TriggerAction;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.widget.core.client.form.ComboBox;

import csi.client.gwt.viz.graph.link.settings.LinkDirectionDef;
import csi.shared.gwt.viz.graph.LinkDirection;

public class LinkDirectionComboBox extends ComboBox<LinkDirection>{

    public LinkDirectionComboBox() {
        super(
                new ListStore<LinkDirection>(new ModelKeyProvider<LinkDirection>(){

                    @Override
                    public String getKey(LinkDirection item) {
                        return item.name();
                    }}),
                    new DirectionLabelProvider()
                );

        addStyleName("string-combo-style");
        
        initialize();
    }

    private void initialize() {
        for (LinkDirection direction : LinkDirection.values()) {
            getStore().add(direction);
        }
        getStore().remove(LinkDirection.BOTH);
        setTriggerAction(TriggerAction.ALL);
        setForceSelection(true);
        setWidth(200);
    }


    public void setDirectionDef(LinkDirectionDef directionDef) {
        ((DirectionLabelProvider)getLabelProvider()).setDirectionDef(directionDef);
        this.getListView().refresh();
    }

}
