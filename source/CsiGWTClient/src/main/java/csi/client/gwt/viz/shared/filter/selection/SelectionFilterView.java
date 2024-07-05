package csi.client.gwt.viz.shared.filter.selection;


import com.github.gwtbootstrap.client.ui.*;
import com.github.gwtbootstrap.client.ui.base.DivWidget;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.shared.AbstractVisualizationPresenter;
import csi.client.gwt.viz.shared.filter.selection.criteria.CriteriaView;
import csi.client.gwt.widget.boot.SizeProvidingModal;

/***
 * This is the view for the SelectDDD
 */
public class SelectionFilterView {
    private SelectionFilterViewPresenter presenter;
    private static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
    //containers
    private SizeProvidingModal dialog;
    private CriteriaView criteriaList;
    // actions
    private SplitDropdownButton actionButton = new SplitDropdownButton(i18n.chartSelectDialog_actionButton());
    private NavLink addToSelection = new NavLink(i18n.addToSelection());
    private NavLink removeFromSelection = new NavLink(i18n.removeFromSelection());

    private Button cancelButton = new Button(i18n.cancel());

    public SelectionFilterView(AbstractVisualizationPresenter vizPresenter) {
        presenter = new SelectionFilterViewPresenter(this, vizPresenter);
        buildView();

    }

    private void buildView() {
        dialog = new SizeProvidingModal();

        DivWidget dialogContainer = new DivWidget();
        dialog.add(dialogContainer);

        Heading heading = new Heading(4);
        heading.setText(i18n.chartSelectDialog_title());
        heading.addStyleName("selectDialogHeading");
        dialogContainer.add(heading);

        criteriaList = new CriteriaView(presenter.getMeasureSource());

        criteriaList.setValidationListener(() -> {
            setButtonsEnabled(criteriaList.isCriteriaValid());
        });

        dialogContainer.add(criteriaList);

        createFooter();

        // attach handles
        attachHandlers();
    }

    private void createFooter() {
        ModalFooter footer = new ModalFooter();
        dialog.add(footer);

        actionButton.add(addToSelection);
        actionButton.add(removeFromSelection);

        footer.add(actionButton);
        footer.add(cancelButton);
        setButtonsEnabled(criteriaList.isCriteriaValid());
    }

    private void attachHandlers() {

        addToSelection.addClickHandler(event -> {
            presenter.requestSelection(criteriaList.getCriteria(), true, false);
            this.hide();
        });

        cancelButton.addClickHandler(event -> this.hide());
        removeFromSelection.addClickHandler(event -> {
            presenter.requestSelection(criteriaList.getCriteria(), false, false);
            this.hide();
        });


        actionButton.addClickHandler(event -> {
            presenter.requestSelection(criteriaList.getCriteria(), true, true);
            this.hide();
        });
    }

    public void setButtonsEnabled(boolean isValid) {
        actionButton.setVisible(isValid);
    }

    public void hide() {
        dialog.hide();
    }

    public void show() {
        dialog.show();
    }
}
