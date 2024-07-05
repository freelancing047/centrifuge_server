package csi.client.gwt.dataview.linkup;

import java.util.Comparator;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell;
import com.sencha.gxt.core.client.XTemplates;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.SortDir;
import com.sencha.gxt.data.shared.Store;

import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.util.Display;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.boot.ValidatingDialog;
import csi.client.gwt.widget.combo_boxes.FilteredComboBox;
import csi.client.gwt.widget.input_boxes.FilteredTextBox;
import csi.client.gwt.widget.ui.form.CsiSimpleComboBox;
import csi.server.common.model.FieldDef;
import csi.server.common.model.FieldType;
import csi.server.common.util.SynchronizeChanges;

/**
 * Created by centrifuge on 12/19/2014.
 */
public class AddLinkupFieldDialog {

    private static final CentrifugeConstants i18n = CentrifugeConstantsLocator.get();

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                  Embedded Interfaces                                   //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    interface AddLinkupFieldDialogUiBinder extends UiBinder<Widget, AddLinkupFieldDialog> {
    }

    interface ComboBoxTemplates extends XTemplates {

        @XTemplate("<span title=\"{name}\">{name}</span>")
        SafeHtml display(String name);

    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                             GUI Objects from the XML File                              //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    @UiField
    ValidatingDialog dialog;

    @UiField
    Label instructionOne;
    @UiField
    Label instructionTwo;

    @UiField
    Label fieldNameLabel;
    @UiField
    Label templateFieldLabel;
    @UiField(provided = true)
    FilteredTextBox fieldName;
    @UiField(provided = true)
    FilteredComboBox<CsiSimpleComboBox<String>> templateFields;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                    Class Variables                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    private static AddLinkupFieldDialogUiBinder uiBinder = GWT.create(AddLinkupFieldDialogUiBinder.class);

    private static CentrifugeConstants _constants = CentrifugeConstantsLocator.get();

    private LinkupFieldMapper _fieldMapper;
    private final String _txtDialogTitle = _constants.linkupNewFieldDialog_DialogTitle();
    private final String _txtHelpTarget = null;//_constants.linkupNewFieldDialog_HelpTarget();
    private final String _txtInstructionOne = _constants.linkupNewFieldDialog_InstructionOne();
    private final String _txtInstructionTwo = _constants.linkupNewFieldDialog_InstructionTwo();
    private FieldDef _templateField = null;
    private String _autoName = null;


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Event Handlers                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    //
    // Handle valid free text entry or selection from linkup selection combobox drop-down
    //
    private SelectionHandler<String> handleDataTypeSelection
            = new SelectionHandler<String>() {
        @Override
        public void onSelection(SelectionEvent<String> eventIn) {

            selectTemplateField(eventIn.getSelectedItem());
        }
    };

    //
    // Handle clicking the action button which may be labeled as either "Add" or "Update"
    //
    private ClickHandler handleActionButtonClick
            = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {

            String myTemplateFieldName = fieldName.getText();

            if (null == _fieldMapper.findDataViewFieldDef(null, myTemplateFieldName)) {

                _fieldMapper.addFieldPair(new FieldDef(myTemplateFieldName, FieldType.LINKUP_REF, _templateField.getValueType()), _templateField);
                _fieldMapper.scrollToBottom();
                fieldName.setText(null);

            } else {

                Display.error(i18n.addLinkupFieldDialogErrorTitle(), i18n.addLinkupFieldDialogErrorMessage()); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
    };

    //
    // Handle clicking the cancel button
    //
    private ClickHandler handleCancelButtonClick
            = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
            dialog.removeFromParent();
            dialog.hide();
        }
    };


    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                        //
    //                                     Public Methods                                     //
    //                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////

    public AddLinkupFieldDialog(LinkupFieldMapper fieldMapperIn) {

        List<FieldDef> myTemplateFields = fieldMapperIn.getTemplateFields();
        ListStore<String> myListStore;

        _fieldMapper = fieldMapperIn;
        fieldName = new FilteredTextBox(fieldMapperIn.getFieldNameMap());
        fieldName.setRequired(true);
        templateFields = new FilteredComboBox<CsiSimpleComboBox<String>>(setUpComboBox(handleDataTypeSelection));
        myListStore = templateFields.getStore(""); //$NON-NLS-1$
        myListStore.clear();
        for (FieldDef myFieldName : myTemplateFields) {
            myListStore.add(myFieldName.getFieldName());
        }

        templateFields.setRequired(true);
        templateFields.setEnabled(true);

        //
        // Link UI XML code to this file and all GWT to create remaining components
        //
        uiBinder.createAndBindUi(this);

        //
        // Set up the action button
        //
        dialog.getActionButton().setText(Dialog.txtCreateButton);
        dialog.getActionButton().addClickHandler(handleActionButtonClick);
        dialog.getActionButton().setEnabled(false);

        //
        // Set up the cancel button -- it is always active
        //
        dialog.getCancelButton().setText(i18n.iconPanelDoneButton());
        dialog.getCancelButton().addClickHandler(handleCancelButtonClick);
        dialog.getCancelButton().setEnabled(true);

        dialog.defineHeader(_txtDialogTitle, _txtHelpTarget, true);

        instructionOne.setText(_txtInstructionOne);
        instructionTwo.setText(_txtInstructionTwo);
        instructionTwo.setVisible(false);
    }

    //
    //
    //
    public void show() {

        dialog.addObject(fieldName, false);
        dialog.addObject(templateFields, false);
        dialog.show(70);
    }

    //
    //
    //
    private CsiSimpleComboBox<String> setUpComboBox(SelectionHandler<String> selectionHandlerIn) {

        CsiSimpleComboBox<String> myComboBox = new CsiSimpleComboBox<String>(

                new LabelProvider<String>() {
                    @Override
                    public String getLabel(String stringIn) {
                        return stringIn;
                    }
                });

        myComboBox.getStore().addSortInfo(new Store.StoreSortInfo<String>(new Comparator<String>() {
            @Override
            public int compare(String stringOneIn, String stringTwoIn) {
                if (null != stringOneIn) {
                    if (null != stringTwoIn) {
                        return stringOneIn.compareTo(stringTwoIn);
                    } else {
                        return 1;
                    }
                } else if (null != stringTwoIn) {
                    return -1;
                }
                return 0;
            }
        }, SortDir.ASC));
        myComboBox.setTriggerAction(ComboBoxCell.TriggerAction.ALL);
        myComboBox.setForceSelection(true);
        myComboBox.setEditable(false);
        myComboBox.setClearValueOnParseError(false);
        if (null != selectionHandlerIn) {
            myComboBox.addSelectionHandler(selectionHandlerIn);
        }
        myComboBox.setEnabled(false);

        return myComboBox;
    }

    private void selectTemplateField(String selectionIn) {

        if ((null != selectionIn) && (0 < selectionIn.length())) {

            String myName = fieldName.getText();

            _templateField = _fieldMapper.findTemplateFieldDef(null, selectionIn);
            templateFields.setEmptyText(selectionIn);
            fieldName.setEnabled(true);

            if ((null == myName) || (0 == myName.length()) || myName.equals(_autoName)) {

                _autoName = SynchronizeChanges.fieldName(selectionIn, _fieldMapper.getFieldNameMap());
                fieldName.setText(_autoName);
            }
            instructionTwo.setVisible(true);
        }
    }
}
