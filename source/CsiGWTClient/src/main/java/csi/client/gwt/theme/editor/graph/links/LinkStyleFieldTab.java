package csi.client.gwt.theme.editor.graph.links;

import java.util.ArrayList;
import java.util.List;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.IconCell;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.github.gwtbootstrap.client.ui.resources.ButtonSize;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.dom.client.Style.TextDecoration;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.widget.core.client.event.BlurEvent;
import com.sencha.gxt.widget.core.client.event.BlurEvent.BlurHandler;
import com.sencha.gxt.widget.core.client.event.CellClickEvent;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.util.Display;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.combo_boxes.StringComboBox;
import csi.client.gwt.widget.gxt.grid.GridContainer;
import csi.client.gwt.widget.gxt.grid.ResizeableGrid;
import csi.server.common.model.themes.graph.GraphTheme;
import csi.server.common.model.themes.graph.LinkStyle;
import csi.server.common.model.themes.graph.NodeStyle;
import csi.server.common.util.Format;

public class LinkStyleFieldTab extends Composite {

    private static LinkStyleFieldTabUiBinder uiBinder = GWT.create(LinkStyleFieldTabUiBinder.class);

    interface LinkStyleFieldTabUiBinder extends UiBinder<Widget, LinkStyleFieldTab> {
    }
    

    private static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();

    @UiField
    Button addButton;
    

    @UiField(provided=true)
    StringComboBox availableFields;
    

    @UiField
    TextBox name;
    
    
    @UiField
    GridContainer fieldGridContainer;
    
    private LinkPresenter presenter;

    private ResizeableGrid<String> selectedFieldsGrid;
    private String typedText = "";
    private GraphTheme currentTheme = null;
    private LinkStyle currentStyle = null;
    private LinkStyle alternateStyle = null;

    public LinkStyleFieldTab() {
        ListStore<String> listStore = new ListStore<String>(new ModelKeyProvider<String>(){

            @Override
            public String getKey(String item) {
                return item.toString();
            }});
        
        availableFields = new StringComboBox(new ComboBoxCell<String>(listStore, new LabelProvider<String>(){

            @Override
            public String getLabel(String item) {
                // TODO Auto-generated method stub
                return item.toString();
            }}
        ){
            @Override
            protected String selectByValue(String value) {
              String val = super.selectByValue(value);
              if (val == null) {
                // custom logic for creating new objects goes here
                val = (value);
              }
              availableFields.setText(value);
              return val;
            }
        });
        
        initWidget(uiBinder.createAndBindUi(this));
        
        availableFields.setForceSelection(false);
        availableFields.setValidateOnBlur(false);
        availableFields.setClearValueOnParseError(false);
        availableFields.setAutoValidate(false);
        availableFields.setEditable(true);
        availableFields.setAllowTextSelection(true);
        availableFields.setReadOnly(false);
        availableFields.addKeyUpHandler(new KeyUpHandler(){

            @Override
            public void onKeyUp(KeyUpEvent event) {
                typedText = availableFields.getText();
                if(KeyCodes.KEY_ENTER == event.getNativeKeyCode()){
                    if(!selectedFieldsGrid.getStore().getAll().contains(typedText))
                        placeItemCorrectly();
                        
                    //selectedFieldsGrid.getView().refresh(false);
                    availableFields.setText(typedText);
                }
            }});
        
        availableFields.addBlurHandler(new BlurHandler(){

            @Override
            public void onBlur(BlurEvent event) {
                availableFields.setText(typedText);
            }});
        
        availableFields.addSelectionHandler(new SelectionHandler<String>(){

            @Override
            public void onSelection(SelectionEvent<String> event) {
                typedText = event.getSelectedItem();
            }});
        

        addButton.setIcon(IconType.CIRCLE_ARROW_RIGHT);
        addButton.setType(ButtonType.LINK);
        addButton.setSize(ButtonSize.LARGE);
        addButton.getElement().getStyle().setFontSize(25.0D, Unit.PX);
        addButton.getElement().getStyle().setTextDecoration(TextDecoration.NONE);
        addButton.addClickHandler(new ClickHandler(){

            @Override
            public void onClick(ClickEvent event) {
                
                if(typedText != null && !typedText.isEmpty()){
                    if(!selectedFieldsGrid.getStore().getAll().contains(typedText))
                        placeItemCorrectly();
                        
                    availableFields.setText(typedText);

                    if(name.getText().isEmpty()){
                        name.setText(typedText);
                    }
                }
            }
            
        });
        
        List<ColumnConfig<String, ?>> columns = new ArrayList<ColumnConfig<String, ?>>();
        ColumnConfig<String, String> nameColumn = new ColumnConfig<String, String>(new ValueProvider<String, String>(){

            @Override
            public String getValue(String object) {
                return object;
            }

            @Override
            public void setValue(String object, String value) {

                selectedFieldsGrid.getStore().add(value);
            }

            @Override
            public String getPath() {
                return "toString";
            }});
        nameColumn.setHeader(i18n.themeEditor_graph_link_style_general_fieldname());
        nameColumn.setWidth(150);
        columns.add(nameColumn);
        
        final ColumnConfig<String, Void> deleteColumn = new ColumnConfig<String, Void>(new ValueProvider<String, Void>(){

            @Override
            public Void getValue(String object) {
                return null;
            }

            @Override
            public String getPath() {
                return "";
            }

            @Override
            public void setValue(String object, Void value) {
                
            }});
        IconCell deleteCell = new IconCell(IconType.REMOVE);
        deleteCell.setTooltip(i18n.kmlExportDialogdeleteTooltip()); //$NON-NLS-1$
        deleteColumn.setCell(deleteCell);
        deleteColumn.setWidth(20);
        columns.add(deleteColumn);
        
        selectedFieldsGrid = new ResizeableGrid<String>(new ListStore<String>(new ModelKeyProvider<String>(){

            @Override
            public String getKey(String item) {
                return item;
            }
            
        }), new ColumnModel<String>(columns));
        fieldGridContainer.setGrid(selectedFieldsGrid);
        selectedFieldsGrid.setHeight("100%");

        fieldGridContainer.getElement().getStyle().setBorderStyle(BorderStyle.SOLID);
        fieldGridContainer.getElement().getStyle().setBorderWidth(1, Unit.PX);
        fieldGridContainer.getElement().getStyle().setBorderColor("lightgray");
        fieldGridContainer.getElement().getStyle().setMarginTop(53, Unit.PX);
        
        selectedFieldsGrid.addCellClickHandler(new CellClickEvent.CellClickHandler() {

            @Override
            public void onCellClick(CellClickEvent event) {
                int rowIndex = event.getRowIndex();
                int cellIndex = event.getCellIndex();
                ListStore<String> store = selectedFieldsGrid.getStore();
                String  fieldName = store.get(rowIndex);
                int delColIndex = selectedFieldsGrid.getColumnModel().indexOf(deleteColumn);
                if (cellIndex == delColIndex) {
                    store.remove(rowIndex);
                    //presenter.deleteEvent(eventDefinition);
                    //presenter.deleteTheme(theme);
                }
            }
        });
    }
    
    public void display(LinkStyle linkStyle) {
        clear();
        
        selectedFieldsGrid.getStore().addAll(linkStyle.getFieldNames());
        name.setText(linkStyle.getName());
        
    }
    
    void clear() {
        availableFields.getStore().clear();
        if(presenter.getFieldNames() != null){
            availableFields.getStore().addAll(presenter.getFieldNames());
        }
        selectedFieldsGrid.getStore().clear();
        name.setText("");
    }

    public void display() {
        clear();
    }

    public void save(LinkStyle linkStyle) {
        linkStyle.getFieldNames().addAll(selectedFieldsGrid.getStore().getAll());
        linkStyle.setName(name.getValue());
    }

    public LinkPresenter getPresenter() {
        return presenter;
    }

    public void setPresenter(LinkPresenter presenter) {
        this.presenter = presenter;
    }

    private void placeItemCorrectly() {

        currentTheme = presenter.getParentTheme();
        currentStyle = (null != presenter) ? presenter.getStyle() : null;
        alternateStyle = (null != presenter) ? presenter.findLinkConflict(typedText) : null;

        if (null != alternateStyle) {

            String myName = Format.value(alternateStyle.getName());
            String myId = Format.value(alternateStyle.getUuid());

            Display.continueDialog(i18n.themeStyle_ConflictTitle(),
                    i18n.themeStyle_ConflictMessage(myName, myId, Dialog.txtContinueButton,
                            Dialog.txtCancelButton), changeItemStyle);

        } else {

            selectedFieldsGrid.getStore().add(typedText);
            currentTheme.addLinkStyle(currentStyle, typedText);
            alternateStyle = null;
            currentStyle = null;
            currentTheme = null;
        }
    }

    private ClickHandler changeItemStyle = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {

            currentTheme.removeLinkStyle(alternateStyle, typedText);
            selectedFieldsGrid.getStore().add(typedText);
            currentTheme.addLinkStyle(currentStyle, typedText);
            alternateStyle = null;
            currentStyle = null;
            currentTheme = null;
        }
    };
}
