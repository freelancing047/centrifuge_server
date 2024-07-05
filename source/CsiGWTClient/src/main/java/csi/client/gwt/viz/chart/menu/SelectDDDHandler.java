/**
 * Copyright (c) 2008 Centrifuge Systems, Inc.
 * All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of
 * Centrifuge Systems, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered
 * into with Centrifuge Systems.
 **/
package csi.client.gwt.viz.chart.menu;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.github.gwtbootstrap.client.ui.*;
import com.github.gwtbootstrap.client.ui.base.DivWidget;
import com.google.common.collect.Lists;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.chart.presenter.ChartPresenter;
import csi.client.gwt.viz.chart.selection.MeasuresSource;
import csi.client.gwt.viz.shared.filter.selection.criteria.CriteriaView;
import csi.client.gwt.viz.shared.menu.CsiMenuEvent;
import csi.client.gwt.vortex.AbstractVortexEventHandler;
import csi.client.gwt.vortex.VortexEventHandler;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.boot.SizeProvidingModal;
import csi.server.common.model.visualization.chart.ChartCriterion;
import csi.server.common.model.visualization.chart.ChartSettings;
import csi.server.common.model.visualization.chart.MeasureDefinition;
import csi.server.common.model.visualization.selection.ChartSelectionState;
import csi.server.common.model.visualization.selection.DrillCategory;
import csi.server.common.service.api.ChartActionsServiceProtocol;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class SelectDDDHandler extends AbstractChartMenuEventHandler {
    private static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();

    private ChartPresenter presenter;
    private CriteriaView criteriaView;

    private SplitDropdownButton actionButton = new SplitDropdownButton(i18n.chartSelectDialog_actionButton());
    private NavLink addToSelection = new NavLink(i18n.addToSelection());
    private NavLink removeFromSelection = new NavLink(i18n.removeFromSelection());
    private Button cancelButton = new Button(i18n.cancel());
    private SizeProvidingModal dialog;

    private AbstractVortexEventHandler<List<String>> selectionHandler = new AbstractVortexEventHandler<List<String>>() {

        @Override
        public void onSuccess(List<String> result) {
            handleSelectionFromServer(result);
        }
    };

    private AbstractVortexEventHandler<List<String>> removeHandler = new AbstractVortexEventHandler<List<String>>() {

        @Override
        public void onSuccess(List<String> result) {
            handleRemoveSelectionFromServer(result);
        }
    };

    private AbstractVortexEventHandler<List<String>> addToHandler = new AbstractVortexEventHandler<List<String>>() {

        @Override
        public void onSuccess(List<String> result) {
            handleAddSelectionFromServer(result);
        }
    };

    SelectDDDHandler(ChartPresenter presenter, ChartMenuManager menuManager) {
        super(presenter, menuManager);
        this.presenter = presenter;
        setupStructure();
        addHandlers();
    }

    private void setupStructure() {
        dialog = new SizeProvidingModal();
        ModalFooter footer = new ModalFooter();
        dialog.add(footer);
        DivWidget dialogContainer = new DivWidget();
        criteriaView = new CriteriaView(measuresSource);

        criteriaView.setValidationListener(() -> {
            actionButton.setVisible(criteriaView.isCriteriaValid());
        });

        Heading heading = new Heading(4);
        heading.setText(i18n.chartSelectDialog_title());
        heading.addStyleName("selectDialogHeading");
        dialogContainer.add(heading);

        dialogContainer.add(criteriaView);
        dialog.add(dialogContainer);

        actionButton.add(addToSelection);
        actionButton.add(removeFromSelection);
        footer.add(actionButton);
        footer.add(cancelButton);
    }

    private void addHandlers() {
        addToSelection.addClickHandler(event -> {
            AbstractVortexEventHandler<List<String>> eventHandler = addToHandler;
            retrieveCriteriaCategories(eventHandler);
        });
        removeFromSelection.addClickHandler(event -> {
            AbstractVortexEventHandler<List<String>> eventHandler = removeHandler;
            retrieveCriteriaCategories(eventHandler);
        });
        actionButton.addClickHandler(event -> {
            AbstractVortexEventHandler<List<String>> eventHandler = selectionHandler;
            retrieveCriteriaCategories(eventHandler);
        });
        cancelButton.addClickHandler(event -> dialog.hide());
        dialog.addShowHandler(showEvent -> {
            if (criteriaView.getCriteria().size() > 0) {

                criteriaView.updateMeasureLists();
            } else {
                criteriaView.addCriterionCard();
            }
        });
    }

    private void handleAddSelectionFromServer(List<String> categories) {
        Set<String> selectedKeySet = new TreeSet<String>();
        selectedKeySet.addAll(categories);

        ChartSelectionState selection = presenter.getChartModel().getChartSelectionState();

        for (DrillCategory item : selection.getSelectedItems()) {
            categories = item.getCategories();
            selectedKeySet.add(categories.get(categories.size() - 1));
        }


        presenter.selectCategories(Lists.newArrayList(selectedKeySet));
        //presenter.applySelection(selection);
        //view.selectRows(new ArrayList<String>(selectedKeySet));

        dialog.hide();
    }

    private void handleRemoveSelectionFromServer(List<String> serverSelection) {

        ChartSelectionState selection = presenter.getChartModel().getChartSelectionState();
        Set<String> removeKeySet = new HashSet<String>(serverSelection);
        List<String> selectedKeys = new ArrayList<String>();
        for (DrillCategory item : selection.getSelectedItems()) {
            List<String> categories = item.getCategories();
            String key = categories.get(categories.size() - 1);
            if (!removeKeySet.contains(key)) {
                selectedKeys.add(key);
            }
        }

        presenter.selectCategories(selectedKeys);
        //view.selectRows(selectedKeys);

        dialog.hide();
    }

    private void handleSelectionFromServer(List<String> serverSelection) {
        presenter.selectCategories(serverSelection);
        dialog.hide();
    }

    private List<ChartCriterion> getCriteria() {
        return criteriaView.getCriteria();
    }

    private MeasuresSource measuresSource = new MeasuresSource() {

        @Override
        public void fillHeaders(List<String> headers) {
            ChartSettings settings = presenter.getVisualizationDef().getChartSettings();
            if (settings.isUseCountStarForMeasure() || settings.getMeasureDefinitions().size() == 0) {
                headers.add("Count (*)");
            } else {
                for (MeasureDefinition measureDefinition : settings.getMeasureDefinitions()) {
                    headers.add(measureDefinition.getComposedName());
                }
            }
        }

    };

    @Override
    public void onMenuEvent(CsiMenuEvent event) {
        criteriaView.updateMeasureLists();
        dialog.show();
    }

    private void retrieveCriteriaCategories(VortexEventHandler<List<String>> eventHandler) {

        VortexFuture<List<String>> future = presenter.getVortex().createFuture();

        future.addEventHandler(eventHandler);

        future.execute(ChartActionsServiceProtocol.class).selectCategories(getCriteria(), presenter.getUuid(),
                presenter.getChartModel().getDrillSelections());
    }


}
