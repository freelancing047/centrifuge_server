package csi.client.gwt.viz.graph.window.annotation;

import com.github.gwtbootstrap.client.ui.Heading;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sencha.gxt.widget.core.client.form.HtmlEditor;

import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.widget.boot.Dialog;

/**
 * Wraps a dialog for naming and typing a new node.
 * @author Centrifuge Systems, Inc.
 */
public class GraphAnnotationDialog {

    private static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);
    private final GraphAnnotation presenter;

    @UiField
    Dialog dialog;

    private HtmlEditor htmlEditor;

    private Heading titleHtml = new Heading(4, CentrifugeConstantsLocator.get().annotationDialog_Heading());

    public GraphAnnotationDialog(GraphAnnotation presenter) {
        this.presenter = presenter;
        uiBinder.createAndBindUi(this);

        buildUI();
        setupButtons();
        initializeHandlers();
    }

    private void initializeHandlers() {
        dialog.hideOnAction();
        dialog.hideOnCancel();
        dialog.getActionButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                String html = GraphAnnotationDialog.addTarget(htmlEditor.getValue());
                presenter.addAnnotation(html);
            }
        });
    }

    private void setupButtons() {

    }


    public void show() {
        String html = presenter.getHtml();
        if (!((html == null) || html.trim().isEmpty())) {
            htmlEditor.setValue(html);
        }
        dialog.show();
    }

    public void hide() {
        dialog.hide();
    }

    private void buildUI() {
        dialog.setHeight("395px");//NON-NLS
        dialog.setBodyHeight("325px");//NON-NLS
        VerticalPanel vp = new VerticalPanel();
        vp.add(titleHtml);

        htmlEditor = new HtmlEditor();
        htmlEditor.setWidth(505);
        String html = presenter.getHtml();
        if (!((html == null) || html.trim().isEmpty())) {
            htmlEditor.setValue(html);
        }
        htmlEditor.setEnableAlignments(false);
        htmlEditor.setEnableFontSize(false);
        htmlEditor.setEnableLists(false);
        htmlEditor.setEnableSourceEditMode(false);
        htmlEditor.setHeight(275);

        vp.add(htmlEditor);

        dialog.add(vp);

    }

    interface SpecificUiBinder extends UiBinder<Dialog, GraphAnnotationDialog> {
    }

    private static String addTarget(String value) {
        value = value.replaceAll("<a target=\"_BLANK\" ", "<a ");//NON-NLS
        value = value.replaceAll("<a ", "<a target=\"_BLANK\" ");//NON-NLS
        return value;
    }
}
