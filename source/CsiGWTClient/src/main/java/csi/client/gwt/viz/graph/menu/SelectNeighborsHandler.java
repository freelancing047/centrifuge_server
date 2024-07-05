package csi.client.gwt.viz.graph.menu;

import com.github.gwtbootstrap.client.ui.Column;
import com.github.gwtbootstrap.client.ui.ControlLabel;
import com.github.gwtbootstrap.client.ui.Row;
import com.github.gwtbootstrap.client.ui.base.TextBox;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.button.ToolButton;

import csi.client.gwt.WebMain;
import csi.client.gwt.i18n.CentrifugeConstants;
import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.client.gwt.validation.feedback.ValueBoxErrorLabelValidationFeedback;
import csi.client.gwt.validation.multi.MultiValidator;
import csi.client.gwt.validation.multi.MultiValidatorShowingFirstFeedback;
import csi.client.gwt.validation.multi.ValidationAndFeedbackPair;
import csi.client.gwt.validation.validator.NotBlankValidator;
import csi.client.gwt.validation.validator.PositiveIntegerValidator;
import csi.client.gwt.viz.graph.Graph;
import csi.client.gwt.viz.shared.menu.AbstractMenuEventHandler;
import csi.client.gwt.viz.shared.menu.CsiMenuEvent;
import csi.client.gwt.vortex.VortexFuture;
import csi.client.gwt.widget.buttons.Button;
import csi.server.common.service.api.GraphActionServiceProtocol;

public class SelectNeighborsHandler extends AbstractMenuEventHandler<Graph, GraphMenuManager> {

    private static final CentrifugeConstants i18n = CentrifugeConstantsLocator.get();
    private ContentPanel contentPanel = new ContentPanel();
    private final TextBox textBox = new TextBox();
    private final Label errorLabel = new Label();
    private ClickHandler closeHandler = new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {
            contentPanel.setVisible(false);
        }
    };

    private MultiValidator validator = new MultiValidatorShowingFirstFeedback();

    public SelectNeighborsHandler(Graph graph, GraphMenuManager mgr) {
        super(graph, mgr);
        buildContentPanel();
        initValidator();
        graph.getChrome().addWindow(contentPanel);
    }

    private void initValidator() {
        ValidationAndFeedbackPair blank = new ValidationAndFeedbackPair(new NotBlankValidator(textBox), new ValueBoxErrorLabelValidationFeedback(textBox, errorLabel, i18n.validator_RequiredValue()));
        ValidationAndFeedbackPair positive = new ValidationAndFeedbackPair(new PositiveIntegerValidator(textBox), new ValueBoxErrorLabelValidationFeedback(textBox, errorLabel, i18n.validator_MustBePositiveInteger()));
        validator.addValidationAndFeedback(blank);
        validator.addValidationAndFeedback(positive);
    }

    private void buildContentPanel() {
        configureContentPanel();
        buildUI();
    }

    private void buildUI() {
        textBox.setWidth("35px");//NON-NLS

        VerticalPanel vp = new VerticalPanel();
        vp.add(createRow(i18n.selectNeighbor_numberOfNeighbors(), textBox, errorLabel));
        vp.add(buildButtonRow());
        contentPanel.setWidget(vp);
    }

    private Row buildButtonRow() {
        Row row = new Row();
        Column buttonColumn = new Column(2, 2);
        Button selectButton = createButton();
        buttonColumn.add(selectButton);
        row.add(buttonColumn);
        row.setWidth("300px");//NON-NLS
        return row;
    }

    private Button createButton() {
        Button selectButton = new Button(i18n.selectNeighbor_actionButton());
        selectButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                VortexFuture<Integer> future = WebMain.injector.getVortex().createFuture();

                if(validator.validate()){
                    future.execute(GraphActionServiceProtocol.class).selectVisibleNeighbors(getPresenter().getUuid(), Integer.parseInt(textBox.getValue()));
                    getPresenter().getGraphSurface().refresh(future);
                }
            }
        });
        return selectButton;
    }

    private Row createRow(String label, Widget control, Label errorLabel) {
        Row row = new Row();
        Column column1 = new Column(2);
        column1.add(new ControlLabel(label));

        Column column2 = new Column(1);
        column2.add(control);

        Column column3 = new Column(4);
        column3.add(errorLabel);

        row.add(column1);
        row.add(column2);
        row.add(column3);
        row.setWidth("300px");//NON-NLS

        return row;
    }

    private void configureContentPanel() {
        contentPanel.setCollapsible(true);
        contentPanel.setVisible(false);
        contentPanel.setHeading(i18n.selectNeighbors());
        contentPanel.getHeader().setBorders(false);
        contentPanel.getElement().getStyle().setProperty("boxShadow", "rgba(0, 0, 0, 0.2) 0px 5px 10px 0px");//NON-NLS
        contentPanel.getElement().getStyle().setProperty("MozBoxSizing", "border-box");//NON-NLS
        contentPanel.setBodyBorder(false);
        ToolButton closeButton = new ToolButton(ToolButton.CLOSE);
        closeButton.addDomHandler(closeHandler, ClickEvent.getType());
        contentPanel.addTool(closeButton);
//        contentPanel.setAllowTextSelection(false);

    }

    @Override
    public void onMenuEvent(CsiMenuEvent event) {
        textBox.setValue("1");
        validator.validate();

        contentPanel.setVisible(true);
        contentPanel.setWidth(230);
        contentPanel.setHeight(120);

    }
}
