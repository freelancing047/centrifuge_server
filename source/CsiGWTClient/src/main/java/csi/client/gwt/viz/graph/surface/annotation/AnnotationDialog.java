package csi.client.gwt.viz.graph.surface.annotation;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sencha.gxt.widget.core.client.form.HtmlEditor;

import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.widget.boot.Dialog;

/**
 * Wraps a dialog for naming and typing a new node.
 * @author Centrifuge Systems, Inc.
 */
public class AnnotationDialog {

    private static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);
    private final AnnotationPresenter presenter;

    @UiField
    Dialog dialog;

    private VerticalPanel vp;

    private HtmlEditor htmlEditor;

    private HTML titleHtml = new HTML("<h4>"+ CentrifugeConstantsLocator.get().annotation_title()+"</h4>");

    public AnnotationDialog(AnnotationPresenter presenter) {
        this.presenter = presenter;
        uiBinder.createAndBindUi(this);

        buildUI();
        setupButtons();
        initializeHandlers();
    }

    private void initializeHandlers() {
        dialog.getActionButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                String value = addTarget(htmlEditor.getValue());
                presenter.addAnnotation(value);
                hide();
            }
        });
        
        dialog.getCancelButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                hide();
            }
        });

    }

    private void setupButtons() {

    }


    public void show() {
        dialog.show();
        htmlEditor.enable();
        htmlEditor.enableEvents();
    }

    public void hide() {
        htmlEditor.clear();
        htmlEditor.disable();
        htmlEditor.flush();
        htmlEditor.disableEvents();
        dialog.hide();
    }

    private void buildUI() {
        dialog.setHeight("395px");//NON-NLS
        dialog.setBodyHeight("325px");//NON-NLS
        vp = new VerticalPanel();
        vp.add(titleHtml);

        createHTMLEditor(vp);

        dialog.add(vp);

    }

    public void createHTMLEditor(VerticalPanel vp) {
        htmlEditor = new HtmlEditor();
        htmlEditor.setWidth(505);
        String html = presenter.getCurrentHtml();
        if (!((html == null) || html.trim().isEmpty())) {
            htmlEditor.setValue(html);
        }
        htmlEditor.setEnableAlignments(false);
        htmlEditor.setEnableFontSize(false);
        htmlEditor.setEnableLists(false);
        htmlEditor.setEnableSourceEditMode(false);
        htmlEditor.setHeight(275);

        vp.add(htmlEditor);
    }

    private String addTarget(String value) {
        value = value.replaceAll("<a target=\"_BLANK\" ", "<a ");
        value = value.replaceAll("<a ", "<a target=\"_BLANK\" ");

        return value;
    }

    interface SpecificUiBinder extends UiBinder<Dialog, AnnotationDialog> {
    }

}
