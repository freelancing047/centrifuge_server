package csi.client.gwt.viz.graph.tab.pattern.settings;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.CheckBox;
import com.github.gwtbootstrap.client.ui.Column;
import com.github.gwtbootstrap.client.ui.FluidContainer;
import com.github.gwtbootstrap.client.ui.FluidRow;
import com.github.gwtbootstrap.client.ui.Heading;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.github.gwtbootstrap.client.ui.resources.ButtonSize;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.TextDecoration;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.InlineLabel;

import csi.client.gwt.i18n.CentrifugeConstantsLocator;
import csi.shared.gwt.viz.graph.tab.pattern.settings.HasPatternCriteria;
import csi.shared.gwt.viz.graph.tab.pattern.settings.NullNodePatternCriterion;
import csi.shared.gwt.viz.graph.tab.pattern.settings.PatternCriterion;

public class CriteriaPanel extends Composite {
    private final HasPatternCriteria node;
    private final FluidContainer criteriaContainer;
    private final FluidRow criteriaHeaderRow;
    private PatternSettings patternSettings;
    private CheckBox showInResults;

    public CriteriaPanel(final PatternSettings patternSettings, final HasPatternCriteria item) {
        this.patternSettings = patternSettings;
        this.node = item;
        FluidContainer container = new FluidContainer();
        container.getElement().getStyle().setPadding(0.0D, Unit.PX);
        container.getElement().getStyle().setHeight(400, Unit.PX);

        FluidContainer outsideContainer = new FluidContainer();
        FluidRow nameRow = new FluidRow();
        final TextBox nameTextBox = new TextBox();
        nameTextBox.setPlaceholder(CentrifugeConstantsLocator.get().pattern_criteria_namePlaceholder());
        nameTextBox.setValue(item.getName());
        nameTextBox.addDomHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                item.setName(nameTextBox.getValue());
            }
        }, ChangeEvent.getType());
        InlineLabel nameLabel = new InlineLabel(CentrifugeConstantsLocator.get().pattern_criteria_nameInput());
        nameLabel.getElement().getStyle().setPaddingRight(10.0D, Unit.PX);
        nameRow.add(nameLabel);
        nameRow.add(nameTextBox);
        criteriaHeaderRow = new FluidRow();
        Heading heading = new Heading(4, CentrifugeConstantsLocator.get().pattern_criteria_criteriaListHeading());
        heading.getElement().getStyle().setDisplay(Display.INLINE);
        criteriaHeaderRow.add(heading);
        Button newCriteriaButton = new Button("");
        newCriteriaButton.setIcon(IconType.PLUS);
        newCriteriaButton.setType(ButtonType.LINK);
        newCriteriaButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                PatternCriterion criterion = new NullNodePatternCriterion();
                item.addCriterion(criterion);
                criterion.setName(CentrifugeConstantsLocator.get().pattern_criteria_defaultCriterionName());
                addCriterion(criterion);
            }
        });
        criteriaHeaderRow.add(newCriteriaButton);

        FluidRow criteriaContainerRow = new FluidRow();
        this.criteriaContainer = new FluidContainer();
        criteriaContainer.getElement().getStyle().setOverflowY(Style.Overflow.AUTO);
        criteriaContainer.getElement().getStyle().setHeight(260, Unit.PX);
        criteriaContainerRow.add(criteriaContainer);
        addCriteria();
        outsideContainer.getElement().getStyle().setMarginTop(5.0D, Unit.PX);
        FluidRow backRow = new FluidRow();
        Button backButton = new Button();
        backButton.setIcon(IconType.CIRCLE_ARROW_LEFT);
        backButton.setType(ButtonType.LINK);
        backButton.setSize(ButtonSize.LARGE);
        backButton.getElement().getStyle().setFontSize(25.0D, Unit.PX);
        backButton.getElement().getStyle().setTextDecoration(TextDecoration.NONE);
        backButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                patternSettings.hideCriteria();
            }
        });
        backRow.add(backButton);

        Button removeButton = new Button("");
        removeButton.setIcon(IconType.TRASH);
        removeButton.setType(ButtonType.LINK);
        {
            Style style = removeButton.getElement().getStyle();
            style.setFloat(Style.Float.RIGHT);
            style.setFontSize(25.0D, Unit.PX);
            style.setTextDecoration(TextDecoration.NONE);
        }
        removeButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                patternSettings.removeFromPattern(item);
            }
        });
        backRow.add(removeButton);

        container.add(backRow);
        outsideContainer.add(nameRow);
        addResultControls(outsideContainer);
        outsideContainer.add(criteriaHeaderRow);
        outsideContainer.add(criteriaContainerRow);
        FluidRow outsideContainerFR = new FluidRow();
        outsideContainerFR.add(outsideContainer);
        container.add(outsideContainerFR);

        showInResults.setValue(item.showInResults());
        showInResults.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                item.setShowInResults(showInResults.getValue());
            }
        });

        initWidget(container);
    }

    private void addResultControls(FluidContainer container) {
        FluidRow fluidRow = new FluidRow();
        Column column = new Column(8, 2);
        showInResults = new CheckBox(CentrifugeConstantsLocator.get().patttern_criteria_showLabelInResults());
        column.add(showInResults);
        fluidRow.add(column);
        container.add(fluidRow);
    }

    private void addCriteria() {
        for (PatternCriterion patternCriterion : node.getCriteria()) {
            addCriterion(patternCriterion);
        }
    }

    private void addCriterion(final PatternCriterion criterion) {
        final FluidRow myRow = new FluidRow();
        myRow.addStyleName("criterionGridItem");//NON-NLS
        Column c1 = new Column(6);
//        Column c2 = new Column(1);
//        Column c3 = new Column(1);
        c1.add(new InlineLabel(criterion.getName()));
//        Button deleteButton = new Button();
//        deleteButton.setType(ButtonType.LINK);
//        deleteButton.setIcon(IconType.REMOVE);
//        c2.add(deleteButton);
//        deleteButton.addClickHandler(new ClickHandler() {
//            public void onClick(ClickEvent event) {
//                CriteriaPanel.this.node.removeCriterion(criterion);
//                myRow.removeFromParent();
//            }
//        });
//        Button editButton = new Button();
//        editButton.setType(ButtonType.LINK);
//        editButton.setIcon(IconType.EDIT);
//        editButton.addClickHandler(new ClickHandler() {
//            public void onClick(ClickEvent event) {
//                CriteriaPanel.this.patternSettings.editCriterion(CriteriaPanel.this.node, criterion);
//            }
//        });
//        c3.add(editButton);
        myRow.add(c1);
//        myRow.add(c2);
//        myRow.add(c3);
        myRow.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                patternSettings.editCriterion(node, criterion);
            }
        }, ClickEvent.getType());
        criteriaContainer.add(myRow);
    }

    public void allowAddCriteria(boolean b) {
        criteriaHeaderRow.setVisible(b);
    }
}
