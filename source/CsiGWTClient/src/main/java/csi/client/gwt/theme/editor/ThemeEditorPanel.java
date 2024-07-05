package csi.client.gwt.theme.editor;

import java.util.List;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.IconCell;
import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Float;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;
import com.sencha.gxt.widget.core.client.button.ButtonBar;
import com.sencha.gxt.widget.core.client.event.CellClickEvent;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.widget.boot.WarningDialog;
import csi.client.gwt.widget.boot.YesNoDialog;
import csi.client.gwt.widget.gxt.grid.GridContainer;
import csi.client.gwt.widget.gxt.grid.ResizeableGrid;
import csi.client.gwt.widget.misc.EmptyValueProvider;
import csi.server.common.dto.SelectionListData.ResourceBasics;
import csi.server.common.model.themes.graph.GraphTheme;
import csi.server.common.model.themes.map.MapTheme;

public class ThemeEditorPanel extends ResizeComposite {

    private static final String CSI_THEME_JOINING_PANEL_STYLE = "csi-theme-joining-panel"; //$NON-NLS-1$
    private static final String CSI_THEME_BUTTON_CONTAINER_STYLE = "csi-theme-button-container"; //$NON-NLS-1$
    private static final String CSI_THEME_BUTTONS_STYLE = "csi-theme-buttons"; //$NON-NLS-1$
    private static ThemeEditorPanelUiBinder uiBinder = GWT.create(ThemeEditorPanelUiBinder.class);
    private static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();

    ThemeModelProperties props = GWT.create(ThemeModelProperties.class);
    private ThemeEditorPresenter presenter;
    
    @UiField
    LayoutPanel layoutPanel;
    
    @UiField
    GridContainer gridContainer;
    
//    @UiField
//    Button addButton;
    
    @UiField
    ButtonBar toolBar;
    
    @UiField
    ButtonBar subBar;
    
    @UiField
    AbsolutePanel joiningPanel = new AbsolutePanel();

    ResizeableGrid<ResourceBasics> grid;
    
    interface ThemeEditorPanelUiBinder extends UiBinder<Widget, ThemeEditorPanel> {
    }

    public ThemeEditorPanel() {
        initWidget(uiBinder.createAndBindUi(this));
        
        List<ColumnConfig<ResourceBasics, ?>> columnConfigs = Lists.newArrayList();
        
        ColumnConfig<ResourceBasics, String> nameColumn = new ColumnConfig<ResourceBasics, String>(props.name());
        nameColumn.setHeader(i18n.name()); //$NON-NLS-1$
        nameColumn.setWidth(260);
        columnConfigs.add(nameColumn);
//        
//        ColumnConfig<Theme, String> typeColumn = new ColumnConfig<Theme, String>(props.typeString());
//        typeColumn.setHeader(i18n.type()); //$NON-NLS-1$
//        typeColumn.setWidth(150);
//        columnConfigs.add(typeColumn);
        
        ColumnConfig<ResourceBasics, String> ownerColumn = new ColumnConfig<ResourceBasics, String>(props.owner());
        ownerColumn.setHeader(i18n.sharingDialogs_SharingColumn_5()); //$NON-NLS-1$
        ownerColumn.setWidth(150);
        columnConfigs.add(ownerColumn);
        
        final ColumnConfig<ResourceBasics, Void> exportColumn = new ColumnConfig<ResourceBasics, Void>(new EmptyValueProvider<ResourceBasics>());
        IconCell exportCell = new IconCell(IconType.SAVE);
        exportCell.setTooltip(i18n.themeEditor_themeExportTooltip());
        exportColumn.setCell(exportCell);
        exportColumn.setWidth(20);
        columnConfigs.add(exportColumn);

        final ColumnConfig<ResourceBasics, Void> editColumn = new ColumnConfig<ResourceBasics, Void>(new EmptyValueProvider<ResourceBasics>());
        IconCell editCell = new IconCell(IconType.PENCIL);
        editCell.setTooltip(i18n.kmlExportDialogeditCellTooltip()); //$NON-NLS-1$
        editColumn.setCell(editCell);
        editColumn.setWidth(20);
        columnConfigs.add(editColumn);
        
        final ColumnConfig<ResourceBasics, Void> deleteColumn = new ColumnConfig<ResourceBasics, Void>(new EmptyValueProvider<ResourceBasics>());
        IconCell deleteCell = new IconCell(IconType.REMOVE);
        deleteCell.setTooltip(i18n.kmlExportDialogdeleteTooltip()); //$NON-NLS-1$
        deleteColumn.setCell(deleteCell);
        deleteColumn.setWidth(20);
        columnConfigs.add(deleteColumn);
            

        ListStore<ResourceBasics> store = new ListStore<ResourceBasics>(props.uuid());

        ColumnModel<ResourceBasics> columnModel = new ColumnModel<ResourceBasics>(columnConfigs);
        grid = new ResizeableGrid<ResourceBasics>(store, columnModel);
        gridContainer.setGrid(grid);

        grid.addCellClickHandler(new CellClickEvent.CellClickHandler() {

            final int delColIndex = grid.getColumnModel().indexOf(deleteColumn);
            final int editColIndex = grid.getColumnModel().indexOf(editColumn);
            final int exportColIndex = grid.getColumnModel().indexOf(exportColumn);

            @Override
            public void onCellClick(CellClickEvent event) {
                final int rowIndex = event.getRowIndex();
                int cellIndex = event.getCellIndex();
                final ListStore<ResourceBasics> store = grid.getStore();
                final ResourceBasics  theme = store.get(rowIndex);

                if (delColIndex == cellIndex) {
                    WarningDialog dialog = new WarningDialog(i18n.themeDeleteTitle(), //$NON-NLS-1$
                            i18n.themeDeleteWarning()); //$NON-NLS-1$
                    dialog.addClickHandler(new ClickHandler() {

                        @Override
                        public void onClick(ClickEvent event) {
                            store.remove(rowIndex);
                            presenter.deleteTheme(theme.getUuid());
                        }
                    });
                    dialog.show();

                } else if (editColIndex == cellIndex) {

                    presenter.editTheme(theme.getUuid(), null);

                } else if (exportColIndex == cellIndex) {

                    presenter.exportTheme(theme);
                }
            }
        });

        Button createButton = new Button(i18n.themeCreateTitle()); //$NON-NLS-1$
        createButton.setIcon(IconType.PLUS);
        createButton.setType(ButtonType.PRIMARY);
        toolBar.add(createButton);
        
        createButton.addClickHandler(new ClickHandler(){

            @Override
            public void onClick(ClickEvent event) {
                subBar.setVisible(!subBar.isVisible());
                joiningPanel.setVisible(!joiningPanel.isVisible());
                
            }});
        
//        Button importButton = new Button("Import Theme");
//        importButton.setType(ButtonType.PRIMARY);
//        importButton.setIcon(IconType.INBOX);
//        toolBar.add(importButton);
        
        Button createGraphButton = new Button(i18n.themeGraphButton()); //$NON-NLS-1$
        createGraphButton.setType(ButtonType.DEFAULT);
        createGraphButton.addClickHandler(new ClickHandler(){

            @Override
            public void onClick(ClickEvent event) {
                subBar.hide();
                joiningPanel.setVisible(false);
                presenter.editTheme(null, new GraphTheme());
            }});
        subBar.add(createGraphButton);
        
        Button createMapButton = new Button(i18n.themeMapButton()); //$NON-NLS-1$
        createMapButton.setType(ButtonType.DEFAULT);
        createMapButton.addClickHandler(new ClickHandler(){

            @Override
            public void onClick(ClickEvent event) {
                subBar.hide();
                joiningPanel.setVisible(false);
                presenter.editTheme(null, new MapTheme());
            }});
        subBar.add(createMapButton);

        createMapButton.getElement().getStyle().setFloat(Float.RIGHT);
//        toolBar.getElement().getStyle().setBackgroundImage("none");
//        toolBar.getElement().getStyle().setBackgroundColor("none");
        
        subBar.setVisible(false);
        joiningPanel.setVisible(false);

        joiningPanel.addStyleName(CSI_THEME_JOINING_PANEL_STYLE);
        subBar.addStyleName(CSI_THEME_BUTTON_CONTAINER_STYLE);
        createMapButton.addStyleName(CSI_THEME_BUTTONS_STYLE);
        createGraphButton.addStyleName(CSI_THEME_BUTTONS_STYLE);
        
    }

    public ThemeEditorPresenter getPresenter() {
        return presenter;
    }

    public void setPresenter(ThemeEditorPresenter presenter) {
        this.presenter = presenter;
    }

    interface ThemeModelProperties extends PropertyAccess<ResourceBasics> {
        ValueProvider<ResourceBasics, String> name();

        ValueProvider<ResourceBasics, String> owner();
        
        ModelKeyProvider<ResourceBasics> uuid();

        ValueProvider<ResourceBasics,Void> voidFn();
    }

    public void updateGrid(List<ResourceBasics> result) {
        grid.getStore().replaceAll(result);
    }
    
}
