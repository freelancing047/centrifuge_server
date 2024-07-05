package csi.client.gwt.dataview.fieldlist.grid;

import com.sencha.gxt.data.shared.StringLabelProvider;
import com.sencha.gxt.widget.core.client.event.TriggerClickEvent;

import csi.client.gwt.widget.ui.form.CsiSimpleComboBox;

/**
 * @author Centrifuge Systems, Inc.
 * This combo box will clear the selection when the user clicks the dropdown arrow,
 * so that all the options appear. Useful for editors.
 */
public class ClearOnClickCsiSimpleComboBox extends CsiSimpleComboBox<String> {

    public ClearOnClickCsiSimpleComboBox(){
        super(new StringLabelProvider<String>());

        addTriggerClickHandler(new TriggerClickEvent.TriggerClickHandler() {
            @Override
            public void onTriggerClick(TriggerClickEvent event) {
                setText(""); //$NON-NLS-1$
            }
        });
    }
}
