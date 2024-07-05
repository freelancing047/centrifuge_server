package csi.client.gwt.viz.shared.export.view.widget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;
import com.sencha.gxt.widget.core.client.form.FieldLabel;
import com.sencha.gxt.widget.core.client.form.TextField;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.viz.shared.export.model.ExportType;
import csi.client.gwt.viz.shared.export.settings.ExportImageSettings;
import csi.client.gwt.viz.shared.export.settings.ExportSettings;
import csi.client.gwt.widget.boot.Dialog;
import csi.client.gwt.widget.buttons.Button;
import csi.client.gwt.widget.combo_boxes.ExportSizeComboBox;
import csi.client.gwt.widget.combo_boxes.StringComboBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * View used to get the options for a viz export
 */
public class VizExport extends Composite {
    private CentrifugeConstants _constants = CentrifugeConstantsLocator.get();
    private static VizExport.vizExportUIBinder uiBinder = GWT.create(VizExport.vizExportUIBinder.class);

    interface vizExportUIBinder extends UiBinder<Widget, VizExport> {
    }
    //Main Dialog

    final private String DIALOG_TITLE_PREFIX = _constants.table_export_titlePrefix();
    @UiField
    Dialog dialog;

    @UiField
    InlineLabel txtDialogName;


    @UiField
    HorizontalPanel pnlName;

    @UiField
    TextBox txtName;

    /**
     * This panel has the label for export type, and the dropdown. Hide this to hide it.
     */
    @UiField
    HorizontalPanel pnlExportType;
    @UiField
    FieldLabel lblExportType;
    @UiField
    StringComboBox exportType;

    /**
     * This panel has the label for export size, and the dropdown. Hide this to hide it.
     */
    @UiField
    HorizontalPanel pnlExportSize;
    @UiField
    FieldLabel lblExportSize;
    @UiField
    ExportSizeComboBox exportSize;

    /**
     * Panel Containing the stuff for Image Size, label, and two TextFields. This will be hidden by default
     */
    @UiField
    HorizontalPanel pnlExportImageSize;
    @UiField
    FieldLabel lblExportImageSize;

    @UiField
    TextField txtWidth;
    @UiField
    TextField txtHeight;

    private int defaultWidth = 0;
    private int defaultHeight = 0;


    /**
     * Dialog Configurable Settings
     */

    private List<ExportType> allowedExportTypes = new ArrayList<ExportType>();
    private List<ExportSize> sizes = new ArrayList<ExportSize>();
    private boolean _showImageExportSize = false;
    private boolean _showExportSize = true;

    private String ACTION_BUTTON_TEXT = _constants.table_export_button();

    /**
     * Based on the allowedExportTypes parameter, the first combo box will be populated.
     */
    public VizExport(List<ExportType> allowedExportTypes) {
        this(allowedExportTypes, false);
    }

    public VizExport(List<ExportType> allowedExportTypes, boolean showImageExportSize, boolean showExportSize) {
        this(allowedExportTypes, showImageExportSize);

        this._showExportSize = showExportSize;

        if (showExportSize) {
            showExportSize();
        } else {
            hideExportSize();
        }
    }

    /**
     * Creates Export View with Image Size Control and handler onSelChange to toggle visibility of control.
     *
     * @param allowedExportTypes  -
     * @param showImageExportSize -  display Image Size Dialog on PNG if true
     */
    public VizExport(List<ExportType> allowedExportTypes, boolean showImageExportSize) {
//        StyleInjector.inject(".radioButtonDisplay {  padding-left: 5em; }");
        uiBinder.createAndBindUi(this);

        // get some style, can't seem to use the one from the *.ui.xml
        this.allowedExportTypes = allowedExportTypes;
        this._showImageExportSize = showImageExportSize;

        initDefaults();
    }

    /**
     * Will use w, h as default values in the text boxes for width and height
     *
     * @param allowedExportTypes
     * @param showImageExportSize
     * @param w
     * @param h
     */
    public VizExport(List<ExportType> allowedExportTypes, boolean showImageExportSize, int w, int h) {
        this(allowedExportTypes, showImageExportSize);

        if (h != 0 && w != 0) {
            defaultHeight = h;
            defaultWidth = w;

            txtHeight.setText(String.valueOf(defaultHeight));
            txtWidth.setText(String.valueOf(defaultWidth));
        }
    }

    private void initDefaults() {

        // always false by default - unless we select PNG
        pnlExportImageSize.setVisible(false);

        sizes.add(ExportSize.ALL_DATA);
        sizes.add(ExportSize.SELECTION_ONLY);

        getActionButton().setText(ACTION_BUTTON_TEXT);

        initExportTypesSel();
        initExportSizeSel();

    }

    /**
     * Attaches the handler to exportType combobox to show/hide the exportImageSize
     */
    private void initImageExportSizePanel() {
        exportType.addSelectionHandler(event -> {
            // check if we need the image size and if we have the right one selected.
            if (ExportType.valueOf(event.getSelectedItem()).equals(ExportType.PNG)) {
                if (_showImageExportSize) {
                    pnlExportImageSize.setVisible(true);
                }
                pnlExportSize.setVisible(false);
            } else if (ExportType.valueOf(event.getSelectedItem()).equals(ExportType.CSV)) {
                if (_showExportSize) {
                    pnlExportSize.setVisible(true);
                }
                pnlExportImageSize.setVisible(false);
            }
        });
    }

    /**
     * this is called by the handler to set the default item.
     *
     * @param sel
     */
    public void selectType(ExportType sel) {
        exportType.select(sel.getDisplayName());

        if (sel.equals(ExportType.PNG)) {
            if (_showImageExportSize) {
                pnlExportImageSize.setVisible(true);
            }
            pnlExportSize.setVisible(false);

        } else if (sel.equals(ExportType.CSV)) {
            if (_showExportSize) {
                pnlExportSize.setVisible(true);
            }
            pnlExportImageSize.setVisible(false);
        }


    }

    private void hideExportSize() {
        pnlExportSize.setVisible(false);
    }

    // force
    public void showExportSize() {
        pnlExportSize.setVisible(true);
    }

    /**
     * Returns the information needed to call export from the view.
     *
     * @return
     */
    public ExportSettings getExportSettings() {
        ExportSettings exportSets = new ExportSettings();

        exportSets.setName(txtName.getText());
        exportSets.setExportType(getExportType());
        exportSets.setUseSelectionOnly(getExportSize() == ExportSize.SELECTION_ONLY);

        if (_showImageExportSize && pnlExportImageSize.isVisible()) {
            exportSets.setImageSettings(createImageExportSettings());
        }

        return exportSets;
    }

    private ExportImageSettings createImageExportSettings() {
        ExportImageSettings imageSets = new ExportImageSettings();

        if (!Objects.isNull(txtHeight.getText()) && !Objects.isNull(txtWidth.getText())) {
            imageSets.setDesiredHeight(Integer.parseInt(txtHeight.getText()));
            imageSets.setDesiredWidth(Integer.parseInt(txtWidth.getText()));
        } else if (defaultHeight != 0 && defaultWidth != 0) {

            imageSets.setDesiredHeight(defaultHeight);
            imageSets.setDesiredWidth(defaultWidth);
        }

        return imageSets;
    }

    /**
     * @return current string value of the export type dropdown.
     */
    public String getStringExportType() {
        return exportType.getCurrentValue();
    }

    /**
     * @return current string value of the export size dropdown.
     */
    public String getStringExportSize() {
        return exportSize.getCurrentValue().getSizeDescription();
    }

    public ExportSize getExportSize() {
        return ExportSize.getEnumByString(getStringExportSize());
    }

    public ExportType getExportType() {
        return ExportType.valueOf(getStringExportType());
    }

    /**
     * Populates the export type dropdown list, selecting the first item.
     */
    private void initExportTypesSel() {
        for (ExportType et : allowedExportTypes) {
            exportType.getStore().add(et.getDisplayName());
        }

        setDefaultForCombo(exportType);

        if (allowedExportTypes.size() >= 1) {
            exportType.setSelectedIndex(0);
        }

        initImageExportSizePanel();
    }

    /**
     * Populates the export size dropdown lin, selecting the first item
     */
    private void initExportSizeSel() {

        exportSize.getStore().addAll(sizes);

        setDefaultForCombo(exportType);

        if (sizes.size() >= 1) {
            exportSize.setSelectedIndex(0);
        }
    }

    /**
     * Add shared defaults for both of the combo boxes, currently will disable editing and disallow blank
     *
     * @param combo
     */
    private void setDefaultForCombo(StringComboBox combo) {
        combo.setEditable(false);
        combo.setAllowBlank(false);
    }

    public void setExportFileName(String name) {
        txtName.setText(name);
    }

    public String getExportFileName() {
        return txtName.getText();
    }

    /**
     * @param vizName Pass in the viz name, dialog will be named "Export <VIZNAME>"
     */
    public void setExportDialogTitle(String vizName) {
        vizName = vizName == null ? "Visualization" : vizName;
        txtDialogName.setText(DIALOG_TITLE_PREFIX + "\"" + vizName + "\"");

    }

    // convenience methods
    public void show() {
        dialog.show();
        if (allowedExportTypes.size() == 1 && allowedExportTypes.get(0).equals(ExportType.CSV)) {
            showExportSize();
        }

    }

    public void destroy() {
        dialog.destroy();
    }

    public Button getActionButton() {
        return dialog.getActionButton();
    }

    ;

    public Button getCancelButton() {
        return dialog.getCancelButton();
    }

    ;


}
