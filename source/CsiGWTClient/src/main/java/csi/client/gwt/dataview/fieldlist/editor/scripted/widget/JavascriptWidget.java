package csi.client.gwt.dataview.fieldlist.editor.scripted.widget;

import java.util.ArrayList;
import java.util.List;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.resources.ButtonSize;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;

import csi.client.gwt.dataview.fieldlist.FieldList;
import csi.client.gwt.dataview.fieldlist.editor.scripted.JavascriptFunction;
import csi.client.gwt.dataview.fieldlist.editor.scripted.ScriptedFunctions;
import csi.client.gwt.dataview.fieldlist.editor.scripted.ScriptedFunctionsEditorModel;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.util.Display;
import csi.client.gwt.validation.multi.ValidationAndFeedbackPair;
import csi.client.gwt.widget.boot.Dialog;
import csi.server.common.dto.CompilationResult;

/**
 * @author Centrifuge Systems, Inc.
 * An arbitrary js function
 */
public class JavascriptWidget extends Composite implements ScriptedFunctionsWidget {

    private static final String NONE = "none";
	private static final String RESIZE = "resize";
	private static final CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
    private final FieldList fieldList;

    private final TextArea textArea = new TextArea();
    private final DisclosurePanel disclosurePanel = new DisclosurePanel(i18n.fieldList_Javascript_Toggle());
    private final Button testButton = new Button();
    private final Label testSuccessLabel = new Label();

    public JavascriptWidget(FieldList fieldListIn, String currentModelName) {
        this.fieldList = fieldListIn;

        VerticalPanel mainPanel = new VerticalPanel();

        mainPanel.add(disclosurePanel);
        mainPanel.add(textArea);
        mainPanel.add(buildTestButtonPanel());


        initWidget(mainPanel);
        configureHelp(currentModelName);
        configureTextArea();
        configureTestButton();
    }

    private void configureTestButton() {
        testButton.setVisible(false);
    }

    public boolean isValid() {

        String myScript = textArea.getText().trim();

        return (null != myScript) && (0 < myScript.length());
    }

    @Override
    public void setUIFromModel(ScriptedFunctionsEditorModel dynamicFunctionsModel) {
        JavascriptFunction javascriptFunction = (JavascriptFunction) dynamicFunctionsModel.getScriptFunction();

        if (null != javascriptFunction) {

            textArea.setText(javascriptFunction.getScript());

        } else {

            Display.error(i18n.javascriptWidgetErrorTitle(), //$NON-NLS-1$
                    i18n.javascriptWidgetErrorMessage()); //$NON-NLS-1$

            dynamicFunctionsModel.setScriptFunction(new JavascriptFunction());
        }
    }

    @Override
    public ScriptedFunctionsEditorModel getValue() {
        ScriptedFunctionsEditorModel model = new ScriptedFunctionsEditorModel();
        JavascriptFunction javascriptFunction = new JavascriptFunction();
        javascriptFunction.setScript(textArea.getValue());

        model.setFunctionType(ScriptedFunctions.ADVANCED_FUNCTION);
        model.setScriptFunction(javascriptFunction);
        return model;
    }

    @Override
    public List<ValidationAndFeedbackPair> getValidators() {
        return new ArrayList<ValidationAndFeedbackPair>();
    }

    @Override
    public void clear() {
        textArea.setText(""); //$NON-NLS-1$
    }

    @Override
    public void handleNameChange(String currentModelNameIn) {

    }

    public void addTestButtonClickHandler(ClickHandler clickHandler) {
        testButton.addClickHandler(clickHandler);
    }

    public void handleTestResult(CompilationResult result) {
        testSuccessLabel.setVisible(true);
        if(result.success){
            testSuccessLabel.setText(i18n.fieldList_Javascript_Success());
        }
        else{
            testSuccessLabel.setText(result.errorMsg);
        }

    }

    private HorizontalPanel buildTestButtonPanel() {
        HorizontalPanel hp = new HorizontalPanel();
        testButton.setText(i18n.fieldList_Javascript_Test());
        testButton.setSize(ButtonSize.SMALL);
        testSuccessLabel.setVisible(false);

        hp.add(testButton);
        hp.add(testSuccessLabel);
        hp.setCellWidth(testButton, "60px"); //$NON-NLS-1$
        return hp;
    }

    private void configureTextArea() {
        textArea.setWidth("460px"); //$NON-NLS-1$
        textArea.setVisibleLines(3);
        textArea.getElement().getStyle().setProperty(RESIZE, NONE); //$NON-NLS-1$ //$NON-NLS-2$
        textArea.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                testSuccessLabel.setVisible(false);
            }
        });
    }

    private void configureHelp(String currentModelName) {
        SampleFieldNameFinder sampleFieldNameFinder = new SampleFieldNameFinder(currentModelName).invoke();
        String field1 = sampleFieldNameFinder.getField1();
        String field2 = sampleFieldNameFinder.getField2();

        TextArea helpArea = new TextArea();
        helpArea.setReadOnly(true);
        helpArea.getElement().getStyle().setProperty(RESIZE, NONE); //$NON-NLS-1$ //$NON-NLS-2$
        helpArea.setValue("//Sample:\n" + //$NON-NLS-1$
                "var csiResult = csiRow.get('" + field1 + "') + csiRow.get('" + field2 + "');\n" + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                "//Returns the contents of " + field1 + " and " + field2 + " appended together."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        helpArea.setWidth("460px"); //$NON-NLS-1$
        helpArea.setVisibleLines(3);
        disclosurePanel.add(helpArea);
    }

    private class SampleFieldNameFinder {
        private String currentModelName;
        private String field1;
        private String field2;

        public SampleFieldNameFinder(String currentModelName) {
            this.currentModelName = currentModelName;
            this.field1 = "<field name>"; //$NON-NLS-1$
            this.field2 = "<field name>"; //$NON-NLS-1$
        }

        public String getField1() {
            return field1;
        }

        public String getField2() {
            return field2;
        }

        public SampleFieldNameFinder invoke() {
            int index = findIndexNotUsing(-1, currentModelName);
            if (index != -1) {
                field1 = fieldList.getFieldDefs().get(index).getFieldName();
            }
            index = findIndexNotUsing(index, currentModelName);
            if (index != -1) {
                field2 = fieldList.getFieldDefs().get(index).getFieldName();
            }
            return this;
        }

        private int findIndexNotUsing(int index, String currentModelName) {
            for (int i = 0; i < fieldList.getFieldDefs().size(); i++) {
                if (i == index) {
                    continue;
                }
                if (fieldList.getFieldDefs().get(i).getFieldName().equals(currentModelName)) {
                    continue;
                }
                return i;
            }
            return -1;
        }

    }
}
