package csi.client.gwt.viz.table.settings;

import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;

import csi.client.gwt.WebMain;

public class GeneralTab extends TableSettingsComposite {

    private static GeneralTabUiBinder uiBinder = GWT.create(GeneralTabUiBinder.class);
    @UiField
    TextBox nameField;
//    @UiField
//    IntegerField pageField;

    interface GeneralTabUiBinder extends UiBinder<Widget, GeneralTab> {
    }

    public GeneralTab() {
        initWidget(uiBinder.createAndBindUi(this));

//        pageField.addKeyUpHandler(new KeyUpHandler() {
//            @Override
//            public void onKeyUp(KeyUpEvent event) {
//                int currentSettings = pageField.getValue();
//                if(currentSettings > getMaxPageSize()){
//                    pageField.setAutoValidate(true);
//                    pageField.addValidator(new Validator<Integer>() {
//                        @Override
//                        public List<EditorError> validate(Editor<Integer> editor, Integer value) {
//                            List<EditorError> errors = new ArrayList<EditorError>();
//                            if(value != null) {
//                                if (value >= getMaxPageSize()) {
//                                    errors.add(new DefaultEditorError(pageField, "Please provide a value less than " + getMaxPageSize(), value));
//                                }
//                            }else{
//                                errors.add(new DefaultEditorError(pageField, "Can't be 0", 0));
//                            }
//                        return errors;
//                        }
//                    });
//                }
//            }
//        });

    }

    @Override
    public void updateViewFromModel() {
        nameField.setValue(getVisualizationSettings().getVisualizationDefinition().getName());
//        pageField.setValue(getTableViewSettings().getPageSize());
    }

    @Override
    public void updateModelWithView() {
        getVisualizationSettings().getVisualizationDefinition().setName(nameField.getValue().trim());
//        Integer pageSize = 50;// default
//        if (pageField.getValue() == null) {
//            pageSize = 50;
//        } else if (pageField.getValue() == 0) {
//            pageSize = 50;
//        } else {
//            pageSize = pageField.getValue();
//        }
//
//        getTableViewSettings().setPageSize(pageSize);

    }

    public int getMaxPageSize(){
        return WebMain.getClientStartupInfo().getTableMaxPageSize();
    }
}
