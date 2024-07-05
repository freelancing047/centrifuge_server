package csi.client.gwt.viz.shared;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.user.client.ui.Label;

import csi.client.gwt.WebMain;
import csi.client.gwt.dataview.DataViewRegistry;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.util.name.UniqueNameUtil;
import csi.client.gwt.viz.Visualization;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.boot.MaskDialog;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.filter.Filter;
import csi.server.common.service.api.DataViewActionServiceProtocol;

public class CreateSelectionFilterDialog {
    private Visualization visualization;
    private Dialog dialog;
    private CentrifugeConstants _constants = CentrifugeConstantsLocator.get();
    private TextBox filterNameTextBox;
    private Label helpTextLabel = new Label();
    private boolean isCreating = false;

    public CreateSelectionFilterDialog(Visualization visualization) {
        this.visualization = visualization;
        dialog = new Dialog();
        dialog.add(new Label(_constants.createSelectionFilterDialog_filterNameLabel()));
        filterNameTextBox = new TextBox();
        String name = UniqueNameUtil.getDistinctName(
                UniqueNameUtil.getFilterNames(visualization.getDataViewUuid()),
                _constants.selection_filter_name()
        );
        filterNameTextBox.setText(name);
        filterNameTextBox.addFocusHandler(new FocusHandler() {
            @Override
            public void onFocus(FocusEvent event) {
                filterNameTextBox.getElement().getStyle().setColor(null);
                helpTextLabel.setVisible(false);

            }
        });
        dialog.hideOnCancel();
        dialog.add(filterNameTextBox);
        dialog.setTitle(_constants.createSelectionFilterDialog_createFilter());

        helpTextLabel.getElement().getStyle().setColor("red");
        helpTextLabel.setVisible(false);
        dialog.add(helpTextLabel);

        hookupActionButton();
    }

    private void hookupActionButton() {
        Button actionButton = dialog.getActionButton();
        actionButton.setText(_constants.createSelectionFilterDialog_createFilterButton());
        actionButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                if (filterNameTextBox.getValue() == null || filterNameTextBox.getValue().equals("")) {
                    helpTextLabel.setText(_constants.createSelectionFilterDialog_filterMustHaveName());
                    helpTextLabel.setVisible(true);
                    return;
                }

                if (isCreating) {
                    return;
                } else {
                    isCreating = true;
                    final String filterName = filterNameTextBox.getValue();

                    VortexFuture<Boolean> nameFuture = WebMain.injector.getVortex().createFuture();
                    try {
                        nameFuture.execute(DataViewActionServiceProtocol.class).filterNameExists(visualization.getDataViewUuid(), filterName);
                    } catch (CentrifugeException e1) {
                        e1.printStackTrace();
                    }

                    nameFuture.addEventHandler(new AbstractVortexEventHandler<Boolean>() {

                        @Override
                        public void onSuccess(Boolean result) {
                            if (result) {
                                filterNameTextBox.getElement().getStyle().setColor("#FF0000");
                                helpTextLabel.setText(_constants.createSelectionFilterDialog_filterNameExists());
                                helpTextLabel.setVisible(true);
                                isCreating = false;
                            } else {
                                createFilter();
                                dialog.hide();
                            }
                        }
                    });
                }
            }
        });
    }

    private void createFilter() {
        final MaskDialog mask = new MaskDialog(_constants.createSelectionFilterDialog_creatingFilter());
        mask.show();
        String filterName = filterNameTextBox.getValue();
        VortexFuture<Filter> future = WebMain.injector.getVortex().createFuture();
        future.addEventHandler(new AbstractVortexEventHandler<Filter>() {
            @Override
            public void onSuccess(final Filter result) {
                mask.hide();
                DataViewRegistry.getInstance().getDataViewByUuid(visualization.getDataViewUuid()).getMeta().getFilters().add(result);
                Dialog.showSuccess(_constants.createSelectionFilterDialog_filterCreated());
                isCreating = false;
            }

            @Override
            public boolean onError(Throwable t) {
                mask.hide();
                isCreating = false;
                return true;
            }
        });
        try {
            future.execute(DataViewActionServiceProtocol.class).createSelectionFilter(visualization.getDataViewUuid(), visualization.getUuid(), filterName, visualization.getVisualizationDef().getSelection());
        } catch (CentrifugeException e) {
            mask.hide();
            isCreating = false;
            e.printStackTrace();
        }
    }

    public void show() {
        dialog.show();
    }

}
