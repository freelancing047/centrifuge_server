package csi.client.gwt.viz.graph.tab.pattern.result;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.CheckBox;
import com.github.gwtbootstrap.client.ui.FluidRow;
import com.github.gwtbootstrap.client.ui.NavLink;
import com.github.gwtbootstrap.client.ui.SplitDropdownButton;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.ColorPalette;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent;

import csi.client.gwt.widget.gxt.grid.GridContainer;
import csi.server.business.visualization.graph.pattern.model.PatternMeta;
import csi.server.common.dto.graph.pattern.PatternResultSet;
import csi.shared.core.color.ClientColorHelper;

public class PatternResultPanel implements PatternResultWidget.PatternResultWidgetView {
    private static final PatternResultPanelUiBinder uiBinder = (PatternResultPanelUiBinder) GWT.create(PatternResultPanelUiBinder.class);
    private final PatternResultWidget patternResultWidget;
    private final Map<NavLink, PatternResultSet> navlinkToResultMap = Maps.newHashMap();
    @UiField
    NavLink addSelectNavLink;
    @UiField
    CheckBox autoHighlightCheckBox;
    @UiField
    FluidRow controlLayer;
    @UiField
    NavLink highlightNavLink;
    @UiField
    SplitDropdownButton searchButton;
    @UiField
    NavLink selectNavLink;
    @UiField
    NavLink showNavLink;
    @UiField
    CheckBox nodeCheckBox;
    @UiField
    CheckBox linkCheckBox;
    @UiField
    Button colorButton;
    @UiField
    ColorPalette colorPalette;
    /*@UiField
    TextBox colorTextBox;*/
    @UiField(provided = true)
    GridContainer gridContainer;
    @UiField(provided = true)
    LayoutPanel layoutPanel;
    private PatternGrid patternGrid;

    public PatternResultPanel(PatternResultWidget patternResultWidget) {
        this.patternResultWidget = patternResultWidget;

        createProvidedUiFields();

        PatternResultPanel.uiBinder.createAndBindUi(this);

        initializeSerachButton();
        initializeLayout();
        initializeGrid();

        controlLayer.getElement().getParentElement().getStyle().setOverflow(Style.Overflow.VISIBLE);
    }

    private void createProvidedUiFields() {
        this.gridContainer = new GridContainer(0, 0);
        this.layoutPanel = new LayoutPanel();
    }

    private void initializeLayout() {
        layoutPanel.setWidth("100%");
        layoutPanel.setHeight("100%");
    }

    @UiHandler("colorPalette")
    public void onSelection(SelectionEvent<String> event) {
        patternResultWidget.setColor(ClientColorHelper.get().makeFromHex(event.getSelectedItem()));
        colorPalette.setVisible(false);
        /*colorTextBox.setVisible(false);*/
    }

    private void initializeSerachButton() {
        //TODO: move to style?
        searchButton.getTriggerWidget().getElement().getStyle().setPaddingLeft(4, Style.Unit.PX);
        searchButton.getTriggerWidget().getElement().getStyle().setPaddingRight(4, Style.Unit.PX);

    }

    private void initializeGrid() {
        this.patternGrid = new PatternGrid();
        Grid<PatternMeta> patternMetaGrid = patternGrid.provideGrid();
        patternMetaGrid.getSelectionModel().addSelectionChangedHandler(new SelectionChangedEvent.SelectionChangedHandler<PatternMeta>() {

            private List<PatternMeta> oldSelection = Lists.newArrayList();

            @Override
            public void onSelectionChanged(SelectionChangedEvent<PatternMeta> event) {
                if (oldSelection.containsAll(event.getSelection()) && event.getSelection().containsAll(oldSelection)) {
                    return;
                }
                oldSelection.clear();
                for(PatternMeta item: event.getSelection()){
                    oldSelection.add(item);
                }
                if (autoHighlightCheckBox.getValue()) {
                    onHighlight((ClickEvent) null);
                }
            }
        });
        gridContainer.setGrid(patternMetaGrid);
        PatternResultSet result = patternResultWidget.getResult();
        if (result != null) {
            patternGrid.setPatterns(result);
        }
    }

    @UiHandler("searchButton")
    void onClick(ClickEvent event) {
        patternResultWidget.search();
    }

    @UiHandler("colorButton")
    void onColor(ClickEvent event) {
        colorPalette.setVisible(!colorPalette.isVisible());
        /*colorTextBox.setVisible(!colorTextBox.isVisible());*/
    }

    @UiHandler("highlightNavLink")
    void onHighlight(ClickEvent event) {
        patternResultWidget.highlightPattern(patternGrid.getSelectedItems());
    }

    @UiHandler("showNavLink")
    void onShowOnly(ClickEvent event) {
        List<PatternMeta> selectedItems = patternGrid.getSelectedItems();
        patternResultWidget.showOnly(selectedItems);
    }


    @UiHandler("clearHighlightsButton")
    void onClearHighlights(ClickEvent event) {
        List<PatternMeta> selectedPatterns = Lists.newArrayList();
        patternResultWidget.clearHighlight(selectedPatterns);
    }

    @UiHandler("addSelectNavLink")
    void onAddSelect(ClickEvent event) {
        List<PatternMeta> selectedPatterns = patternGrid.getSelectedItems();

        patternResultWidget.addSelect(selectedPatterns, nodeCheckBox.getValue(), linkCheckBox.getValue());
    }

    @UiHandler("selectNavLink")
    void onSelect(ClickEvent event) {
        List<PatternMeta> selectedPatterns = patternGrid.getSelectedItems();

        patternResultWidget.select(selectedPatterns, nodeCheckBox.getValue(), linkCheckBox.getValue());
    }

    @Override
    public Widget asWidget() {
        return layoutPanel;
    }

    public SplitDropdownButton getSearchButton() {
        return searchButton;
    }

    public void setLoading(boolean isLoading) {
        patternGrid.setLoading(isLoading);
    }

    public void setColor(ClientColorHelper.Color color) {
        colorButton.getElement().getStyle().setColor(color.toString());
    }

    @Override
    public Collection<PatternMeta> getSelection() {
        return patternGrid.getSelectedItems();
    }

    @Override
    public void clearSelection(){
        patternGrid.clearSelectedItems();
    }

    @SuppressWarnings("WeakerAccess")
    interface PatternResultPanelUiBinder extends UiBinder<LayoutPanel, PatternResultPanel> {
    }
}
