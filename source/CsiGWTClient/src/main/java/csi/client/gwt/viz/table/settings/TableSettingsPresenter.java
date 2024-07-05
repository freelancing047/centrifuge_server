package csi.client.gwt.viz.table.settings;

import java.util.Comparator;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;

import csi.client.gwt.WebMain;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.util.name.UniqueNameUtil;
import csi.client.gwt.validation.feedback.StringValidationFeedback;
import csi.client.gwt.validation.feedback.ValidationFeedback;
import csi.client.gwt.validation.multi.ValidationAndFeedbackPair;
import csi.client.gwt.validation.validator.NotBlankValidator;
import csi.client.gwt.validation.validator.Validator;
import csi.client.gwt.validation.validator.VisualizationUniqueNameValidator;
import csi.client.gwt.viz.shared.settings.AbstractSettingsPresenter;
import csi.client.gwt.viz.shared.settings.SettingsActionCallback;
import csi.client.gwt.viz.shared.settings.VisualizationSettingsModal;
import csi.client.gwt.viz.table.TablePresenter;
import csi.server.common.model.FieldDef;
import csi.server.common.model.visualization.table.TableCachedState;
import csi.server.common.model.visualization.table.TableViewDef;
import csi.server.common.model.visualization.table.TableViewSettings;

public class TableSettingsPresenter extends AbstractSettingsPresenter<TableViewDef> {

    public static final Comparator<FieldDef> COMPARE_BY_NAME = new Comparator<FieldDef>() {

        @Override
        public int compare(FieldDef o1, FieldDef o2) {
            return o1.getFieldName().compareToIgnoreCase(o2.getFieldName());
        }
    };


    @UiField
    GeneralTab tabGeneral;

    @UiField
    ColumnsTab tabColumns;

    @UiField
    SortTab tabSort;

    static CentrifugeConstants i18n = CentrifugeConstantsLocator.get();

    @UiTemplate("TableSettingsView.ui.xml")
    interface SpecificUiBinder extends UiBinder<VisualizationSettingsModal, TableSettingsPresenter> {
    }

    private static SpecificUiBinder uiBinder = GWT.create(SpecificUiBinder.class);

    public TableSettingsPresenter(SettingsActionCallback<TableViewDef> settingsActionCallback) {
        super(settingsActionCallback);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected TableViewDef createNewVisualizationDef() {
        TableViewDef def = new TableViewDef();
        def.setBroadcastListener(WebMain.getClientStartupInfo().isListeningByDefault());

        TableViewSettings settings = new TableViewSettings();
        def.setTableViewSettings(settings);

        String name = UniqueNameUtil.getDistinctName(UniqueNameUtil.getVisualizationNames(dataViewPresenter), i18n.tableTitle()); //$NON-NLS-1$
        def.setName(name);
        return def;
    }

    @Override
    protected void bindUI() {
        uiBinder.createAndBindUi(this);
    }

    @Override
    protected void saveVisualizationToServer() {
            //when we save settings, we reset the cache and anything that was held by presenter
        getVisualizationDef().setState(new TableCachedState());
        ((TablePresenter) (getVisualization())).setPreviousState(getVisualizationDef().getState());
        ((TablePresenter) (getVisualization())).setSaveCache(false);
        super.saveVisualizationToServer();
    }

    @Override
    protected void initiateValidator() {
        NotBlankValidator notBlankValidator = new NotBlankValidator(tabGeneral.nameField);
//        IntNotBlankValidator intNotBlankVali = new IntNotBlankValidator(tabGeneral.pageField);

        VisualizationUniqueNameValidator visualizationUniqueNameValidator = new VisualizationUniqueNameValidator(
                getDataViewDef().getModelDef().getVisualizations(), tabGeneral.nameField, getVisualizationDef().getUuid());
        Validator tabCategoriesValidator = new Validator() {
            @Override
            public boolean isValid() {
                return 0 < tabColumns.getSelectedColumnCount();
            }
        };

        ValidationFeedback tabCategoriesFeedback = new StringValidationFeedback(i18n.tableMinimumValidation());
//        Validator maxPageSize = new Validator() {
//            @Override
//            public boolean isValid() {
//                if(tabGeneral.pageField.getValue() == null){
//                    return false;
//                }
//
//                return tabGeneral.pageField.getValue() <= tabGeneral.getMaxPageSize();
//            }
//        };

        ValidationFeedback maxPageSizeFeedback = new StringValidationFeedback("Results per Page field cannot be more than " + tabGeneral.getMaxPageSize());
//        validator.addValidationAndFeedback(new ValidationAndFeedbackPair(maxPageSize, maxPageSizeFeedback));
        validator.addValidationAndFeedback(new ValidationAndFeedbackPair(tabCategoriesValidator, tabCategoriesFeedback));
        validator.addValidationAndFeedback(new ValidationAndFeedbackPair(notBlankValidator, StringValidationFeedback.getEmptyVisualizationFeedback()));
//        validator.addValidationAndFeedback(new ValidationAndFeedbackPair(intNotBlankVali, new StringValidationFeedback("Results per page cannot be blank")));
        validator.addValidationAndFeedback(new ValidationAndFeedbackPair(visualizationUniqueNameValidator, StringValidationFeedback.getDuplicateVisualizationFeedback()));
    }
}
