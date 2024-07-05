package csi.client.gwt.dataview.fieldlist.housing;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

import csi.client.gwt.dataview.fieldlist.editor.FieldEditorView;
import csi.client.gwt.dataview.fieldlist.grid.FieldGrid;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;

/**
 * @author Centrifuge Systems, Inc.
 * View inside the FieldListDialog. Contains either a FieldGrid or a FieldEditor.
 */
public class FieldListHousingView extends Composite {
    private static final CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
    private final VerticalPanel panel = new VerticalPanel();
    private final FieldGrid fieldGrid;

    private HTML titleHtml = new HTML();
    private Label duplicateError = new Label();

    private FieldEditorView _editorView = null;

    public FieldListHousingView(FieldGrid fieldGrid){
        this.fieldGrid = fieldGrid;

        panel.add(createTitlePanel());
        panel.add(this.fieldGrid);
        initWidget(panel);
    }

    public void editorMode(FieldEditorView view, String title) {
        _editorView = view;
        view.setTitle(title);
        panel.remove(1);
        panel.add(view);
    }

    public void gridMode() {
        _editorView = null;
        panel.remove(1);
        panel.setTitle(i18n.fieldList_Title());
        panel.add(this.fieldGrid);
    }

    public void setEnabled(boolean enabledIn) {

        fieldGrid.setEnabled(enabledIn);
    }

    public Label getDuplicateError() {
        return duplicateError;
    }

    public boolean checkValidity() {

        if (null != _editorView) {

            return _editorView.checkValidity();

        } else {

            return true;
        }
    }

    private HorizontalPanel createTitlePanel() {
        HorizontalPanel hp = new HorizontalPanel();
        hp.add(titleHtml);
        hp.add(duplicateError);
        hp.setCellWidth(duplicateError, "140px"); //$NON-NLS-1$
        hp.setCellWidth(titleHtml, "413px"); //$NON-NLS-1$
        return hp;
    }

}
